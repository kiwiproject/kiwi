package org.kiwiproject.jaxrs.client;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import jakarta.ws.rs.client.Client;
import lombok.experimental.UtilityClass;
import org.glassfish.jersey.client.ClientProperties;

import java.time.Duration;

/**
 * Static utilities related to <em>Jersey</em> {@link Client} instances. If these methods are used with a Jakarta REST
 * implementation other than Jersey, you should not expect anything to work. Some might fail silently; others could
 * throw unexpected exceptions, etc.
 * <p>
 * <em>We do not check</em> to make sure the {@link Client} instances are in fact Jersey client instances; we expect if
 * you are using a class named {@code KiwiJerseyClients} you understand this, and if not then, as they say, all bets are
 * off.
 */
@UtilityClass
public class KiwiJerseyClients {

    /**
     * Set connect timeout.
     *
     * @param client  the Jersey {@link Client} instance
     * @param timeout the timeout as a Dropwizard {@link io.dropwizard.util.Duration}
     * @return the provided Client instance
     */
    public static Client connectTimeout(Client client, io.dropwizard.util.Duration timeout) {
        checkTimeoutNotNull(timeout);
        return connectTimeout(client, timeout.toMilliseconds());
    }

    /**
     * Set connect timeout.
     *
     * @param client  the Jersey {@link Client} instance
     * @param timeout the timeout as a Java {@link Duration}
     * @return the provided Client instance
     */
    public static Client connectTimeout(Client client, Duration timeout) {
        checkTimeoutNotNull(timeout);
        return connectTimeout(client, timeout.toMillis());
    }

    /**
     * Set connect timeout.
     *
     * @param client        the Jersey {@link Client} instance
     * @param timeoutMillis the {@code long} timeout in milliseconds
     * @return the provided Client instance
     */
    public static Client connectTimeout(Client client, long timeoutMillis) {
        checkTimeout(timeoutMillis);
        return connectTimeout(client, Math.toIntExact(timeoutMillis));
    }

    /**
     * Set connect timeout.
     *
     * @param client        the Jersey {@link Client} instance
     * @param timeoutMillis the {@code int} timeout in milliseconds
     * @return the provided Client instance
     * @see ClientProperties#CONNECT_TIMEOUT
     */
    public static Client connectTimeout(Client client, int timeoutMillis) {
        client.property(ClientProperties.CONNECT_TIMEOUT, timeoutMillis);
        return client;
    }

    /**
     * Set read timeout.
     *
     * @param client  the Jersey {@link Client} instance
     * @param timeout the timeout as a Dropwizard {@link io.dropwizard.util.Duration}
     * @return the provided Client instance
     */
    public static Client readTimeout(Client client, io.dropwizard.util.Duration timeout) {
        checkTimeoutNotNull(timeout);
        return readTimeout(client, timeout.toMilliseconds());
    }

    /**
     * Set read timeout.
     *
     * @param client  the Jersey {@link Client} instance
     * @param timeout the timeout as a Java {@link Duration}
     * @return the provided Client instance
     */
    public static Client readTimeout(Client client, Duration timeout) {
        checkTimeoutNotNull(timeout);
        return readTimeout(client, timeout.toMillis());
    }

    private static void checkTimeoutNotNull(Object timeout) {
        checkArgumentNotNull(timeout, "timeout must not be null");
    }

    /**
     * Set read timeout.
     *
     * @param client        the Jersey {@link Client} instance
     * @param timeoutMillis the {@code long} timeout in milliseconds
     * @return the provided Client instance
     */
    public static Client readTimeout(Client client, long timeoutMillis) {
        checkTimeout(timeoutMillis);
        return readTimeout(client, Math.toIntExact(timeoutMillis));
    }

    /**
     * Set read timeout.
     *
     * @param client        the Jersey {@link Client} instance
     * @param timeoutMillis the {@code int} timeout in milliseconds
     * @return the provided Client instance
     * @see ClientProperties#READ_TIMEOUT
     */
    public static Client readTimeout(Client client, int timeoutMillis) {
        client.property(ClientProperties.READ_TIMEOUT, timeoutMillis);
        return client;
    }

    /**
     * Check the given timeout, in milliseconds, to be used for a Jersey connect and/or read timeout.
     *
     * @param timeoutMillis the timeout to check, in milliseconds
     * @throws IllegalArgumentException if the given number of milliseconds is greater than {@link Integer#MAX_VALUE}
     * @see ClientProperties#CONNECT_TIMEOUT
     * @see ClientProperties#READ_TIMEOUT
     */
    public static void checkTimeout(long timeoutMillis) {
        checkArgument(timeoutMillis <= Integer.MAX_VALUE,
                "timeout must be convertible to an int but %s is more than Integer.MAX_VALUE." +
                        " See Jersey API docs for CONNECT_TIMEOUT and READ_TIMEOUT in ClientProperties",
                timeoutMillis);
    }
}
