package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.net.KiwiInternetAddresses.InetAddressFinder;
import org.kiwiproject.net.KiwiInternetAddresses.SimpleHostInfo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.function.Supplier;

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
        var defaultHostInetAddress = InetAddress.getLoopbackAddress();
        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress(defaultHostInetAddress);
        assertThat(inetAddress).isNotEqualTo(defaultHostInetAddress);
    }

    @Test
    void testGetLocalHostInetAddress_WithDefaultValue_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        var defaultHostInetAddress = InetAddress.getLoopbackAddress();
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
        Supplier<InetAddress> supplier = InetAddress::getLoopbackAddress;

        var inetAddress = KiwiInternetAddresses.getLocalHostInetAddress(supplier);
        assertThat(inetAddress).isNotEqualTo(supplier.get());
    }

    @Test
    void testGetLocalHostInetAddress_WithSupplier_WhenUnknownHostException() throws UnknownHostException {
        KiwiInternetAddresses.setAddressFinder(finder);
        when(finder.getLocalHost()).thenThrow(new UnknownHostException("cannot get localhost"));

        Supplier<InetAddress> supplier = InetAddress::getLoopbackAddress;
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
}
