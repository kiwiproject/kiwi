package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.net.KiwiInternetAddresses.InetAddressFinder;
import org.kiwiproject.net.KiwiInternetAddresses.IpScheme;
import org.kiwiproject.net.KiwiInternetAddresses.SimpleHostInfo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
@DisplayName("KiwiInternetAddresses")
class KiwiInternetAddressesTest {

    private InetAddressFinder finder;

    @BeforeEach
    void setUp() {
        finder = mock(InetAddressFinder.class);
    }

    @AfterEach
    void tearDown() {
        KiwiInternetAddresses.setAddressFinder(new InetAddressFinder());
    }

    @Test
    void testGetLocalHostInfo_WhenNoErrors() {
        var hostInfo = KiwiInternetAddresses.getLocalHostInfo().orElse(null);
        assertThat(hostInfo).isNotNull();
    }

    @Test
    void testGetLocalHostInfo_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        var hostInfo = KiwiInternetAddresses.getLocalHostInfo().orElse(null);
        assertThat(hostInfo).isNull();
    }

    @Test
    void testGetLocalHostInetAddress_WhenNoErrors() {
        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress().orElse(null);
        assertThat(inetAddress).isNotNull();
    }

    @Test
    void testGetLocalHostInetAddress_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress().orElse(null);
        assertThat(inetAddress).isNull();
    }

    @Test
    void testGetLocalHostInfo_WithDefaultValue_WhenNoErrors() {
        var defaultHostInfo = newSimpleHostInfo();
        var hostInfo = KiwiInternetAddresses.getLocalHostInfo(defaultHostInfo);
        assertThat(hostInfo).isNotEqualTo(defaultHostInfo);
    }

    @Test
    void testGetLocalHostInfo_WithDefaultValue_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        var defaultHostInfo = newSimpleHostInfo();
        var hostInfo = KiwiInternetAddresses.getLocalHostInfo(defaultHostInfo);
        assertThat(hostInfo).isEqualTo(defaultHostInfo);
    }

    @Test
    void testGetLocalHostInetAddress_WithDefaultValue_WhenNoErrors() {
        var defaultHostInetAddress = dummyInetAddress();
        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress(defaultHostInetAddress);
        assertThat(inetAddress).isNotEqualTo(defaultHostInetAddress);
    }

    @Test
    void testGetLocalHostInetAddress_WithDefaultValue_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        var defaultHostInetAddress = dummyInetAddress();
        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress(defaultHostInetAddress);
        assertThat(inetAddress).isEqualTo(defaultHostInetAddress);
    }

    @Test
    void testGetLocalHostInfo_WithSupplier_WhenNoErrors() {
        Supplier<SimpleHostInfo> supplier = this::newSimpleHostInfo;

        var hostInfo = KiwiInternetAddresses.getLocalHostInfo(supplier);
        assertThat(hostInfo).isNotEqualTo(supplier.get());
    }

    @Test
    void testGetLocalHostInfo_WithSupplier_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        Supplier<SimpleHostInfo> supplier = this::newSimpleHostInfo;
        var hostInfo = KiwiInternetAddresses.getLocalHostInfo(supplier);
        assertThat(hostInfo).isEqualTo(supplier.get());
    }

    @Test
    void testGetLocalHostInetAddress_WithSupplier_WhenNoErrors() {
        Supplier<InetAddress> supplier = this::dummyInetAddress;

        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress(supplier);
        assertThat(inetAddress).isNotEqualTo(supplier.get());
    }

    @Test
    void testGetLocalHostInetAddress_WithSupplier_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        Supplier<InetAddress> supplier = this::dummyInetAddress;
        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress(supplier);
        assertThat(inetAddress).isEqualTo(supplier.get());
    }

    @Test
    void testSimpleHostInfo_ToStringSanityCheck() {
        assertThat(newSimpleHostInfo().toString())
                .contains("hostName=test-host")
                .contains("ipAddr=127.0.0.1");
    }

    @Test
    void testSimpleHostInfo_EqualsAndHashCodeSanityCheck() {
        var hostInfo1 = newSimpleHostInfo();
        var hostInfo2 = newSimpleHostInfo();

        assertThat(hostInfo1).isEqualTo(hostInfo2).hasSameHashCodeAs(hostInfo2);
    }

    @Test
    void testPortFromStringUrl_WhenValidUrl() {
        assertThat(KiwiInternetAddresses.portFrom("http://localhost:4567/path").orElse(-1)).isEqualTo(4567);
    }

    @Test
    void testPortFromStringUrl_WhenNoPortOnUrl() {
        assertThat(KiwiInternetAddresses.portFrom("http://localhost/path").orElse(80)).isEqualTo(80);
    }

    @Test
    void testPortFromStringUrl_WhenMalformedUrl() {
        assertThatThrownBy(() -> KiwiInternetAddresses.portFrom("bad-url"))
                .isExactlyInstanceOf(UncheckedMalformedURLException.class)
                .hasCauseExactlyInstanceOf(MalformedURLException.class);
    }

    @Test
    void testPortFromStringUrl_WhenNullUrl() {
        assertThatThrownBy(() -> KiwiInternetAddresses.portFrom((String) null))
                .isExactlyInstanceOf(UncheckedMalformedURLException.class)
                .hasCauseExactlyInstanceOf(MalformedURLException.class);
    }

    @Test
    void testPortFromUrl_WhenPortInUrl() throws MalformedURLException {
        var url = new URL("http://localhost:4567/path");
        assertThat(KiwiInternetAddresses.portFrom(url).orElse(-1)).isEqualTo(4567);
    }

    @Test
    void testPortFromUrl_WhenNoPortOnUrl() throws MalformedURLException {
        var url = new URL("http://localhost/path");
        assertThat(KiwiInternetAddresses.portFrom(url).orElse(80)).isEqualTo(80);
    }

    @Test
    void testPortFromUrl_WhenMalformedUrl() {
        assertThatThrownBy(() -> KiwiInternetAddresses.portFrom("bad-url"))
                .isExactlyInstanceOf(UncheckedMalformedURLException.class)
                .hasCauseExactlyInstanceOf(MalformedURLException.class);
    }

    @Test
    void testHostAndPortFrom_WhenPortInURL() {
        var url = "http://fake.host.test:4567/path";

        var hostAndPort = KiwiInternetAddresses.hostAndPortFrom(url);
        assertThat(hostAndPort.getHost()).isEqualTo("fake.host.test");
        assertThat(hostAndPort.hasPort()).isTrue();
        assertThat(hostAndPort.getPort()).isEqualTo(4567);
    }

    @Test
    void testHostAndPortFrom_WhenBadStringUrl_ShouldThrowException() {
        assertThatThrownBy(() -> KiwiInternetAddresses.hostAndPortFrom("bad-url"))
                .isExactlyInstanceOf(UncheckedMalformedURLException.class)
                .hasCauseExactlyInstanceOf(MalformedURLException.class);
    }

    @Test
    void testHostAndPortFrom_WhenNoPortInURL() throws MalformedURLException {
        var url = new URL("http://fake.host.test/path");

        var hostAndPort = KiwiInternetAddresses.hostAndPortFrom(url);
        assertThat(hostAndPort.getHost()).isEqualTo("fake.host.test");
        assertThat(hostAndPort.hasPort()).isFalse();
    }

    private SimpleHostInfo newSimpleHostInfo() {
        return SimpleHostInfo.from("test-host", "127.0.0.1");
    }

    /**
     * @implNote Turns out travis and very likely personal machines could have their hostname resolve to the
     * loopback address.  This causes the default checks to fail because the equals method only checks the
     * IP address and both getLocalHost and getLoopbackAddress are resolving to the same IP.
     */
    private InetAddress dummyInetAddress() {
        try {
            return InetAddress.getByName("192.168.1.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldGetEnumeratedNetworkAddresses() {
        var ipv4Addresses = KiwiInternetAddresses.getEnumeratedNetworkAddresses(IpScheme.IPV4);
        assertThat(ipv4Addresses).isNotEmpty();

        var ipv6Addresses = KiwiInternetAddresses.getEnumeratedNetworkAddresses(IpScheme.IPV6);
        assertThat(ipv6Addresses).isNotEmpty();
    }

    @Nested
    class FindFirstMatchingAddress {

        @Test
        void shouldReturnOptionalEmptyWhenAddressIsNotFound() {
            var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
            var ipAddresses = List.of("192.168.200.5");

            var address = KiwiInternetAddresses.findFirstMatchingAddress(subnetCidrs, ipAddresses);

            assertThat(address).isEmpty();
        }

        @Test
        void shouldReturnOptionalWithAddressWhenAddressIsFound() {
            var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
            var ipAddresses = List.of("192.168.100.5", "192.168.200.5", "192.168.10.5");

            var address = KiwiInternetAddresses.findFirstMatchingAddress(subnetCidrs, ipAddresses);

            assertThat(address).hasValue("192.168.100.5");
        }

        @Test
        void shouldReturnOptionalWithFoundAddressThatMatchesAGivenIpv4CidrByLookingUpAddresses() {
            var subnetCidrs = List.of("0.0.0.0/0");

            var address = KiwiInternetAddresses.findFirstMatchingAddress(subnetCidrs, IpScheme.IPV4);

            assertThat(address).isPresent();
        }

        @Test
        void shouldReturnOptionalWithFoundAddressThatMatchesAGivenIpv6CidrByLookingUpAddresses() {
            var subnetCidrs = List.of("::/0");

            var address = KiwiInternetAddresses.findFirstMatchingAddress(subnetCidrs, IpScheme.IPV6);

            assertThat(address).isPresent();
        }

        @Nested
        class OrNull {

            @Test
            void shouldReturnNullWhenAddressIsNotFound() {
                var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
                var ipAddresses = List.of("192.168.200.5");

                var address = KiwiInternetAddresses.findFirstMatchingAddressOrNull(subnetCidrs, ipAddresses);

                assertThat(address).isNull();
            }

            @Test
            void shouldReturnAddressWhenAddressIsFound() {
                var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
                var ipAddresses = List.of("192.168.100.5", "192.168.200.5", "192.168.10.5");

                var address = KiwiInternetAddresses.findFirstMatchingAddressOrNull(subnetCidrs, ipAddresses);

                assertThat(address).isEqualTo("192.168.100.5");
            }
        }

        @Nested
        class OrThrow {

            @Test
            void shouldThrowWhenAddressIsNotFound() {
                var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
                var ipAddresses = List.of("192.168.200.5");

                assertThatIllegalStateException()
                        .isThrownBy(() -> KiwiInternetAddresses.findFirstMatchingAddressOrThrow(subnetCidrs, ipAddresses))
                        .withMessageStartingWith("Unable to find IP address matching a valid subnet CIDR in: ");
            }

            @Test
            void shouldReturnAddressWhenAddressIsFound() {
                var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
                var ipAddresses = List.of("192.168.100.5", "192.168.200.5", "192.168.10.5");

                var address = KiwiInternetAddresses.findFirstMatchingAddressOrThrow(subnetCidrs, ipAddresses);

                assertThat(address).isEqualTo("192.168.100.5");
            }

            @Test
            void shouldThrowIllegalStateWhenRequestedIPDoesNotMatchCidrScheme() {
                assertThatIllegalStateException()
                        .isThrownBy(() -> KiwiInternetAddresses.findFirstMatchingAddressOrThrow(List.of("127.0.0.1/8"), IpScheme.IPV6))
                        .withMessageStartingWith("Unable to find IP address matching a valid subnet CIDR in: ");
            }
        }

    }

    @Nested
    class FindMatchingAddresses {

        @Test
        void shouldReturnListOfMatchingAddresses() {
            var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
            var ipAddresses = List.of("192.168.100.5", "192.168.200.5", "192.168.150.5");

            var addresses = KiwiInternetAddresses.findMatchingAddresses(subnetCidrs, ipAddresses);

            assertThat(addresses).contains("192.168.100.5", "192.168.150.5");
        }

        @Test
        void shouldReturnEmptyListOfMatchingAddressesWhenNoMatchesFound() {
            var subnetCidrs = List.of("192.168.50.0/24", "192.168.100.0/24", "192.168.150.0/24");
            var ipAddresses = List.of("192.168.10.5", "192.168.20.5", "192.168.30.5");

            var addresses = KiwiInternetAddresses.findMatchingAddresses(subnetCidrs, ipAddresses);

            assertThat(addresses).isEmpty();
        }

        @Test
        void shouldReturnListWithFoundAddressThatMatchesAGivenIpv4CidrByLookingUpAddresses() {
            var subnetCidrs = List.of("0.0.0.0/0");

            var address = KiwiInternetAddresses.findMatchingAddresses(subnetCidrs, IpScheme.IPV4);

            assertThat(address).isNotEmpty();
        }

        @Test
        void shouldReturnListWithFoundAddressThatMatchesAGivenIpv6CidrByLookingUpAddresses() {
            var subnetCidrs = List.of("::/0");

            var address = KiwiInternetAddresses.findMatchingAddresses(subnetCidrs, IpScheme.IPV6);

            assertThat(address).isNotEmpty();
        }
    }

}
