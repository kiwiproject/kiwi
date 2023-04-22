package org.kiwiproject.security;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.Getter;
import lombok.Synchronized;
import org.kiwiproject.collect.KiwiMaps;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A "simple" factory class that makes it simpler to to create {@link SSLContext} instances.
 * <p>
 * Construct using one of the public constructors or via the {@link #builder()}.
 * <p>
 * This abstracts the much lower level {@link KiwiSecurity} class.
 *
 * @see KiwiSecurity
 */
public class SimpleSSLContextFactory {

    private static final String KEY_STORE_PATH_PROPERTY = "keyStorePath";
    private static final String KEY_STORE_PASSWORD_PROPERTY = "keyStorePassword";
    private static final String TRUST_STORE_PATH_PROPERTY = "trustStorePath";
    private static final String TRUST_STORE_PASSWORD_PROPERTY = "trustStorePassword";
    private static final String PROTOCOL_PROPERTY = "protocol";
    private static final String VERIFY_HOSTNAME_PROPERTY = "verifyHostname";

    private static final List<String> REQUIRED_PROPERTIES = List.of(
            TRUST_STORE_PATH_PROPERTY, TRUST_STORE_PASSWORD_PROPERTY, PROTOCOL_PROPERTY
    );

    private final String keyStorePath;
    private final String keyStorePassword;
    private final String trustStorePath;
    private final String trustStorePassword;
    private final String protocol;
    private SSLContext sslContext;

    /**
     * This is not strictly needed when creating {@link SSLContext}s. It is here only in case this factory
     * will be supplied to other code that makes HTTPS connections and needs to create {@link SSLContext}
     * instances AND also needs to know whether it should perform hostname verification when making HTTPS
     * requests.
     */
    @Getter
    private final boolean verifyHostname;

    /**
     * Create a new {@link SimpleSSLContextFactory} with {@code verifyHostname} set to {@code true}.
     *
     * @param keyStorePath       path to the key store
     * @param keyStorePassword   password of the key store
     * @param trustStorePath     path to the trust store
     * @param trustStorePassword password of the trust store
     * @param protocol           the protocol to use
     */
    public SimpleSSLContextFactory(String keyStorePath,
                                   String keyStorePassword,
                                   String trustStorePath,
                                   String trustStorePassword,
                                   String protocol) {
        this(keyStorePath, keyStorePassword, trustStorePath, trustStorePassword, protocol, true);
    }

    /**
     * Create a new {@link SimpleSSLContextFactory}.
     *
     * @param keyStorePath       path to the key store
     * @param keyStorePassword   password of the key store
     * @param trustStorePath     path to the trust store
     * @param trustStorePassword password of the trust store
     * @param protocol           the protocol to use
     * @param verifyHostname     whether to verify host names or not
     */
    public SimpleSSLContextFactory(String keyStorePath,
                                   String keyStorePassword,
                                   String trustStorePath,
                                   String trustStorePassword,
                                   String protocol,
                                   boolean verifyHostname) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.protocol = protocol;
        this.verifyHostname = verifyHostname;
    }

    /**
     * A builder class for {@link SimpleSSLContextFactory}.
     *
     * @implNote This was implemented well before we started using Lombok, so is manual builder code, though
     * we have added both the Lombok-style xxx() as well as keeping the original setXxx() methods.
     * Subject to change in future.
     */
    public static class Builder {

        private final Map<String, Optional<String>> entries;

        protected Builder() {
            entries = KiwiMaps.newHashMap(
                    KEY_STORE_PATH_PROPERTY, Optional.empty(),
                    KEY_STORE_PASSWORD_PROPERTY, Optional.empty(),
                    TRUST_STORE_PATH_PROPERTY, Optional.empty(),
                    TRUST_STORE_PASSWORD_PROPERTY, Optional.empty(),
                    PROTOCOL_PROPERTY, Optional.empty(),
                    VERIFY_HOSTNAME_PROPERTY, Optional.empty()
            );
        }

        public Builder keyStorePath(String keyStorePath) {
            return setKeyStorePath(keyStorePath);
        }

        public Builder setKeyStorePath(String keyStorePath) {
            entries.put(KEY_STORE_PATH_PROPERTY, Optional.of(keyStorePath));
            return this;
        }

        public Builder keyStorePassword(String keyStorePassword) {
            return setKeyStorePassword(keyStorePassword);
        }

        public Builder setKeyStorePassword(String keyStorePassword) {
            entries.put(KEY_STORE_PASSWORD_PROPERTY, Optional.of(keyStorePassword));
            return this;
        }

        public Builder trustStorePath(String trustStorePath) {
            return setTrustStorePath(trustStorePath);
        }

        public Builder setTrustStorePath(String trustStorePath) {
            entries.put(TRUST_STORE_PATH_PROPERTY, Optional.of(trustStorePath));
            return this;
        }

        public Builder trustStorePassword(String trustStorePassword) {
            return setTrustStorePassword(trustStorePassword);
        }

        public Builder setTrustStorePassword(String trustStorePassword) {
            entries.put(TRUST_STORE_PASSWORD_PROPERTY, Optional.of(trustStorePassword));
            return this;
        }

        public Builder protocol(String protocol) {
            return setProtocol(protocol);
        }

        public Builder setProtocol(String protocol) {
            entries.put(PROTOCOL_PROPERTY, Optional.of(protocol));
            return this;
        }

        public Builder verifyHostname(boolean verifyHostname) {
            return setVerifyHostname(verifyHostname);
        }

        public Builder setVerifyHostname(boolean verifyHostname) {
            entries.put(VERIFY_HOSTNAME_PROPERTY, Optional.of(String.valueOf(verifyHostname)));
            return this;
        }

        public SimpleSSLContextFactory build() {
            validateBuilderState();

            return new SimpleSSLContextFactory(
                    entries.get(KEY_STORE_PATH_PROPERTY).orElse(null),
                    entries.get(KEY_STORE_PASSWORD_PROPERTY).orElse(null),
                    entries.get(TRUST_STORE_PATH_PROPERTY).orElseThrow(IllegalStateException::new),
                    entries.get(TRUST_STORE_PASSWORD_PROPERTY).orElseThrow(IllegalStateException::new),
                    entries.get(PROTOCOL_PROPERTY).orElseThrow(IllegalStateException::new),
                    Boolean.parseBoolean(entries.get(VERIFY_HOSTNAME_PROPERTY).orElse("true"))
            );
        }

        public void validateBuilderState() {
            entries.entrySet().stream()
                    .filter(entry -> entry.getValue().isEmpty())
                    .filter(entry -> REQUIRED_PROPERTIES.contains(entry.getKey()))
                    .findAny()
                    .ifPresent(entry -> throwBuildException(entry.getKey()));
        }

        private static void throwBuildException(String property) {
            throw new SSLContextException(
                    f("Required property '{}' not set; cannot build SimpleSSLContextFactory", property)
            );
        }
    }

    /**
     * Return a new builder instance.
     *
     * @return new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create/get a {@link SSLContext} instance for the key and trust store properties and protocol that this
     * {@link SimpleSSLContextFactory} instance was built with.
     *
     * @return a new {@link SSLContext} instance when first called; all subsequent calls return the same cached instance
     * @implNote This is intended to be called infrequently, e.g. once when a service/app starts. Thus, making it
     * synchronized was the easiest way to ensure thread-safety.
     */
    @Synchronized
    public SSLContext getSslContext() {
        if (isNull(sslContext)) {
            sslContext = KiwiSecurity.createSslContext(
                    keyStorePath, keyStorePassword, trustStorePath, trustStorePassword, protocol);
        }
        return sslContext;
    }
}
