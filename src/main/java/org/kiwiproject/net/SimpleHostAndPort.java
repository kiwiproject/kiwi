package org.kiwiproject.net;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Inspired by Guava's {@link com.google.common.net.HostAndPort} but <strong>much</strong> simpler in implementation.
 * (Just go look at the code in {@link com.google.common.net.HostAndPort#fromString(String)} if you don't believe me.)
 * Because it is much simpler, it also only handles a very specific host/port format, which is {@code host:port}.
 * <p>
 * It also does not attempt to validate anything about the host or port, e.g. it will happily accept a negative port
 * value or a one-character long host name.
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleHostAndPort {

    @Getter
    private final String host;

    @Getter
    private final int port;

    /**
     * Parse {@code hostPortString} assuming format {@code host:port}; if it is blank, use the specified
     * {@code defaultHost} and {@code defaultPort} values to create a link {@link SimpleHostAndPort}.
     * <p>
     * <em>No validation is performed on the default values.</em>
     *
     * @param hostPortString a string containing host and port, e.g. acme.com:8001
     * @param defaultHost    a default host if the host/port string is blank
     * @param defaultPort    a default port if the host/port string is blank
     * @return a new SimpleHostAndPort instance
     */
    public static SimpleHostAndPort from(String hostPortString, String defaultHost, int defaultPort) {
        if (isBlank(hostPortString)) {
            return new SimpleHostAndPort(defaultHost, defaultPort);
        }

        return from(hostPortString);
    }

    /**
     * Parse {@code hostPortString} assuming format {@code host:port}
     *
     * @param hostPortString a string containing host and port, e.g. foo.com:9000
     * @return a new SimpleHostAndPort instance
     * @throws IllegalStateException if not in the expected format
     * @throws IllegalArgumentException if hostPortString is blank or port is not a valid number
     * @implNote Does no validation on the host part
     */
    public static SimpleHostAndPort from(String hostPortString) {
        checkArgumentNotBlank(hostPortString, "hostAndPortString must not be blank");

        var split = hostPortString.split(":");
        checkState(split.length == 2, "%s is not in format host:port", hostPortString);

        var port = getPortOrThrow(split);
        return new SimpleHostAndPort(split[0], port);
    }

    private static int getPortOrThrow(String[] split) {
        try {
            return Integer.parseInt(split[1], 10);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Return a string in {@code host:port} format.
     */
    @Override
    public String toString() {
        return host + ":" + port;
    }
}
