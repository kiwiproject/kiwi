package org.kiwiproject.net;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.HostAndPort;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.UncheckedIOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utilities for {@link InetAddress} and other things related to internet addresses. Note that Google Guava also has
 * the {@link com.google.common.net.InetAddresses} class, so if something is not in here, check there. This set of
 * utilities only contains things that are not in the JDK or Guava.
 * <p>
 * Note that some of the methods accept (e.g. as default value) or return {@link SimpleHostInfo}. This is because
 * the {@link InetAddress} class has no public constructors and can only be instantiated by its static factory
 * methods. As a result, if you want to specify your own custom host name and/or IP address as the default value, for
 * example to use a different host alias or subnet IP, then you must use the methods that work with {@link SimpleHostInfo}.
 * Otherwise when working with {@link InetAddress} instances, using {@link InetAddress#getLoopbackAddress()} is about the
 * only other default value you could use.
 * <p>
 * Last, note that {@link SimpleHostInfo} <em>only contains the host name and host address as a string</em>.
 */
@UtilityClass
@Slf4j
public class KiwiInternetAddresses {

    @VisibleForTesting
    @Setter(AccessLevel.PACKAGE)
    static InetAddressFinder addressFinder = new InetAddressFinder();

    /**
     * Get local host as an optional of {@link SimpleHostInfo}.
     *
     * @return optional containing local host info, or empty if <em>any</em> error occurred
     */
    public static Optional<SimpleHostInfo> getLocalHostInfo() {
        try {
            var address = addressFinder.getLocalHost();
            var hostInfo = SimpleHostInfo.fromInetAddress(address);
            return Optional.of(hostInfo);
        } catch (UnknownHostException e) {
            LOG.warn("Unable to get local host", e);
            return Optional.empty();
        }
    }

    /**
     * Get local host as an optional of {@link InetAddress}
     *
     * @return optional containing local host info, or empty if <em>any</em> error occurred
     */
    public static Optional<InetAddress> getLocalHostInetAddress() {
        try {
            var address = addressFinder.getLocalHost();
            return Optional.of(address);
        } catch (UnknownHostException e) {
            LOG.warn("Unable to get local host", e);
            return Optional.empty();
        }
    }

    /**
     * Get local host as a {@link SimpleHostInfo}.
     *
     * @param defaultValue the default value to use if local host could not be obtained for any reason
     * @return the local host info, or the specified {@code defaultValue} if <em>any</em> error occurred
     */
    public static SimpleHostInfo getLocalHostInfo(SimpleHostInfo defaultValue) {
        return getLocalHostInfo().orElse(defaultValue);
    }

    /**
     * Get local host as a {@link InetAddress}.
     *
     * @param defaultValue the default value to use if local host address could not be obtained for any reason
     * @return the local host info, or the specified {@code defaultValue} if <em>any</em> error occurred
     */
    public static InetAddress getLocalHostInetAddress(InetAddress defaultValue) {
        return getLocalHostInetAddress().orElse(defaultValue);
    }

    /**
     * Get local host as a {@link SimpleHostInfo}.
     *
     * @param defaultValueSupplier supplier for the default value to use if local host could not be obtained
     *                             for any reason
     * @return the local host info, or the value obtained from the {@code defaultValueSupplier}
     * if <em>any</em> error occurred
     */
    public static SimpleHostInfo getLocalHostInfo(Supplier<SimpleHostInfo> defaultValueSupplier) {
        return getLocalHostInfo().orElseGet(defaultValueSupplier);
    }

    /**
     * Get local host as a {@link InetAddress}.
     *
     * @param defaultValueSupplier supplier for the default value to use if local host address could not be obtained
     *                             for any reason
     * @return the local host info, or the value obtained from the {@code defaultValueSupplier}
     * if <em>any</em> error occurred
     */
    public static InetAddress getLocalHostInetAddress(Supplier<InetAddress> defaultValueSupplier) {
        return getLocalHostInetAddress().orElseGet(defaultValueSupplier);
    }

    /**
     * Returns the port in the given URL, or an empty optional otherwise.
     *
     * @param url a URL
     * @return an optional containing the port, or empty
     * @throws UncheckedMalformedURLException if the given string URL is not valid
     * @see URL#URL(String)
     */
    public static Optional<Integer> portFrom(String url) {
        try {
            return portFrom(new URL(nullToEmpty(url)));
        } catch (MalformedURLException e) {
            throw new UncheckedMalformedURLException(e);
        }
    }

    /**
     * Returns the port in the given URL, or an empty optional otherwise.
     *
     * @param url a URL
     * @return an optional containing the port, or empty if the URL did not have a port
     * @implNote The {@link URL#getPort()} returns {@code -1} if there is no port. Here we change that to an {@link Optional}
     * @see URL#getPort()
     */
    public static Optional<Integer> portFrom(URL url) {
        int port = url.getPort();
        return port == -1 ? Optional.empty() : Optional.of(port);
    }

    /**
     * Returns a {@link HostAndPort} from the given URL.
     *
     * @param url a URL as a string
     * @return HostAndPort instance
     */
    public static HostAndPort hostAndPortFrom(String url) {
        try {
            return hostAndPortFrom(new URL(nullToEmpty(url)));
        } catch (MalformedURLException e) {
            throw new UncheckedMalformedURLException(e);
        }
    }

    /**
     * Returns a {@link HostAndPort} from the given URL.
     *
     * @param url a URL
     * @return HostAndPort instance
     */
    public static HostAndPort hostAndPortFrom(URL url) {
        if (url.getPort() == -1) {
            return HostAndPort.fromHost(url.getHost());
        }

        return HostAndPort.fromParts(url.getHost(), url.getPort());
    }

    /**
     * Finds the first IP address on the machine that matches one of the given subnet CIDRs or returns null if not found.
     * The {@link IpScheme} is used to filter the IP addresses by IPv4 or IPv6.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipScheme      Whether to filter by IPv4 or IPv6.
     * @return the first found matching IP address or null.
     */
    public static String findFirstMatchingAddressOrNull(List<String> subnetCidrs, IpScheme ipScheme) {
        return findFirstMatchingAddress(subnetCidrs, ipScheme).orElse(null);
    }

    /**
     * Finds the first IP address from a given list of ip addresses that matches one of the given subnet CIDRs or returns
     * null if not found.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipAddresses   A list of IP addresses to search for a match.
     * @return the first found matching IP address or null.
     */
    public static String findFirstMatchingAddressOrNull(List<String> subnetCidrs, List<String> ipAddresses) {
        return findFirstMatchingAddress(subnetCidrs, ipAddresses).orElse(null);
    }

    /**
     * Finds the first IP address on the machine that matches one of the given subnet CIDRs or throws
     * IllegalStateException if not found. The {@link IpScheme} is used to filter the IP addresses by IPv4 or IPv6.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipScheme      Whether to filter by IPv4 or IPv6.
     * @return the first found matching IP address.
     * @throws IllegalStateException if a matching IP address can not be found.
     */
    public static String findFirstMatchingAddressOrThrow(List<String> subnetCidrs, IpScheme ipScheme) {
        return findFirstMatchingAddress(subnetCidrs, ipScheme).orElseThrow(() ->
                new IllegalStateException("Unable to find IP address matching a valid subnet CIDR in: " + subnetCidrs));
    }

    /**
     * Finds the first IP address on the machine that matches one of the given subnet CIDRs or throws
     * IllegalStateException if not found.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipAddresses   A list of IP addresses to search for a match.
     * @return the first found matching IP address.
     * @throws IllegalStateException if a matching IP address can not be found.
     */
    public static String findFirstMatchingAddressOrThrow(List<String> subnetCidrs, List<String> ipAddresses) {
        return findFirstMatchingAddress(subnetCidrs, ipAddresses).orElseThrow(() ->
                new IllegalStateException("Unable to find IP address matching a valid subnet CIDR in: " + subnetCidrs));
    }

    /**
     * Attempts to find the first IP address on the machine that matches one of the given subnet CIDRs. The
     * {@link IpScheme} is used to filter the IP addresses by IPv4 or IPv6.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipScheme      Whether to filter by IPv4 or IPv6
     * @return an Optional containing the first found matching IP address or {@link Optional#empty()} if not found.
     */
    public static Optional<String> findFirstMatchingAddress(List<String> subnetCidrs, IpScheme ipScheme) {
        var ipAddresses = getEnumeratedNetworkAddresses(ipScheme);
        return findFirstMatchingAddress(subnetCidrs, ipAddresses);
    }

    /**
     * Attempts to find the first IP address from a given list of ip addresses that matches one of the given subnet CIDRs.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipAddresses   A list of IP addresses to search for a match.
     * @return an Optional containing the first found matching IP address or {@link Optional#empty()} if not found.
     */
    public static Optional<String> findFirstMatchingAddress(List<String> subnetCidrs, List<String> ipAddresses) {
        return findMatchingAddresses(subnetCidrs, ipAddresses)
                .stream()
                .findFirst();
    }

    /**
     * Finds all IP addresses on the machine that matches one of the given subnet CIDRs. The {@link IpScheme} is used to
     * filter the IP addresses by IPv4 or IPv6.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipScheme      Whether to filter by IPv4 or IPv6
     * @return a list of matching IP addresses.
     */
    public static List<String> findMatchingAddresses(List<String> subnetCidrs, IpScheme ipScheme) {
        var ipAddresses = getEnumeratedNetworkAddresses(ipScheme);
        return findMatchingAddresses(subnetCidrs, ipAddresses);
    }

    /**
     * Finds all IP addresses from a given list of ip addresses that matches one of the given subnet CIDRs.
     *
     * @param subnetCidrs   A list of CIDRs used to match against the machine's IP addresses.
     * @param ipAddresses   A list of IP addresses to search for a match.
     * @return a list of matching IP addresses.
     */
    public static List<String> findMatchingAddresses(List<String> subnetCidrs, List<String> ipAddresses) {
        return subnetCidrs.stream()
                .map(CidrRange::new)
                .map(cidr -> ipAddresses.stream().filter(cidr::isInRange).findFirst())
                .flatMap(Optional::stream)
                .collect(toList());
    }

    @VisibleForTesting
    static List<String> getEnumeratedNetworkAddresses(IpScheme ipScheme) {
        try {
            var interfaces = NetworkInterface.getNetworkInterfaces();
            return Collections.list(interfaces)
                    .stream()
                    .map(networkInterface -> getInterfaceIps(networkInterface, ipScheme))
                    .flatMap(List::stream)
                    .collect(toList());
        } catch (SocketException e) {
            throw new UncheckedIOException("Error getting enumeration of network interfaces.", e);
        }
    }

    private static List<String> getInterfaceIps(NetworkInterface networkInterface, IpScheme ipScheme) {
        var addresses = networkInterface.getInetAddresses();

        return Collections.list(addresses)
                .stream()
                .filter(address -> ipScheme.getInetAddressClass().isAssignableFrom(address.getClass()))
                .map(InetAddress::getHostAddress)
                .collect(toList());
    }

    /**
     * Simple value class encapsulating a host name and IP address
     */
    @Getter
    @EqualsAndHashCode
    @Builder
    @ToString
    public static class SimpleHostInfo {
        private final String hostName;
        private final String ipAddr;

        /**
         * Create instance from an {@link InetAddress}
         *
         * @param address the internet address
         * @return a new SimpleHostInfo instance
         */
        public static SimpleHostInfo fromInetAddress(InetAddress address) {
            return from(address.getHostName(), address.getHostAddress());
        }

        /**
         * Create instance from host name and ip address
         *
         * @param hostName the host name
         * @param ipAddr   the IP address
         * @return a new SimpleHostInfo instance
         */
        public static SimpleHostInfo from(String hostName, String ipAddr) {
            return SimpleHostInfo.builder()
                    .hostName(hostName)
                    .ipAddr(ipAddr)
                    .build();
        }
    }

    @VisibleForTesting
    static class InetAddressFinder {
        InetAddress getLocalHost() throws UnknownHostException {
            return InetAddress.getLocalHost();
        }
    }

    /**
     * Enum that defines the IP scheme to use when looking up a machine's IP addresses.
     */
    public enum IpScheme {
        IPV6(Inet6Address.class), IPV4(Inet4Address.class);

        @Getter
        private final Class<? extends InetAddress> inetAddressClass;

        IpScheme(Class<? extends InetAddress> inetAddressClass) {
            this.inetAddressClass = inetAddressClass;
        }
    }
}
