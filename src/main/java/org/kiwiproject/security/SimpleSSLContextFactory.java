package org.kiwiproject.security;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.Getter;
import lombok.Synchronized;
import org.kiwiproject.collect.KiwiMaps;

import javax.net.ssl.SSLContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A "simple" factory class that makes it simpler to create {@link SSLContext} instances.
 * <p>
 * Construct using one of the public constructors or via the {@link #builder()}.
 * <em>Prefer using the builder, as the constructors may be deprecated
 * (most likely for removal) in the future.</em>
 * <p>
 * This abstracts the much lower level {@link KiwiSecurity} class.
 *
 * @see KiwiSecurity
 */
public class SimpleSSLContextFactory {

    private static final String KEY_STORE_PATH_PROPERTY = "keyStorePath";
    private static final String KEY_STORE_PASSWORD_PROPERTY = "keyStorePassword";
    private static final String KEY_STORE_TYPE_PROPERTY = "keyStoreType";
    private static final String TRUST_STORE_PATH_PROPERTY = "trustStorePath";
    private static final String TRUST_STORE_PASSWORD_PROPERTY = "trustStorePassword";
    private static final String TRUST_STORE_TYPE_PROPERTY = "trustStoreType";
    private static final String PROTOCOL_PROPERTY = "protocol";
    private static final String VERIFY_HOSTNAME_PROPERTY = "verifyHostname";
    private static final String DISABLE_SNI_HOST_CHECK_PROPERTY = "disableSniHostCheck";

    private static final List<String> REQUIRED_PROPERTIES = List.of(
            TRUST_STORE_PATH_PROPERTY, TRUST_STORE_PASSWORD_PROPERTY, PROTOCOL_PROPERTY
    );

    private final String keyStorePath;
    private final String keyStorePassword;
    private final String keyStoreType;
    private final String trustStorePath;
    private final String trustStorePassword;
    private final String trustStoreType;
    private final String protocol;
    private SSLContext sslContext;

    /**
     * This is not strictly necessary when creating {@link SSLContext}s. It is here only in case this factory
     * will be supplied to other code that makes HTTPS connections and needs to create {@link SSLContext}
     * instances AND also needs to know whether it should perform hostname verification when making HTTPS
     * requests.
     */
    @Getter
    private final boolean verifyHostname;

    /**
     * This is not strictly necessary when creating {@link SSLContext}s. It is here only in case this factory
     * will be supplied to other code that makes HTTPS connections and needs to create {@link SSLContext}
     * instances AND also needs to know whether it should perform SNI host checking.
     */
    @Getter
    private boolean disableSniHostCheck;

    /**
     * Create a new {@link SimpleSSLContextFactory} with {@code verifyHostname} set to {@code true} and "JKS" as
     * the key and trust store type.
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
     * Create a new {@link SimpleSSLContextFactory} with "JKS" as the key and trust store type.
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
        this(keyStorePath,
                keyStorePassword,
                KeyStoreType.JKS.value,
                trustStorePath,
                trustStorePassword,
                KeyStoreType.JKS.value,
                protocol,
                verifyHostname);
    }

    /**
     * Create a new {@link SimpleSSLContextFactory}.
     *
     * @param keyStorePath       path to the key store
     * @param keyStorePassword   password of the key store
     * @param keyStoreType       the keystore type
     * @param trustStorePath     path to the trust store
     * @param trustStorePassword password of the trust store
     * @param trustStoreType     the trust store type
     * @param protocol           the protocol to use
     * @param verifyHostname     whether to verify host names or not
     */
    @SuppressWarnings("java:S107")
    public SimpleSSLContextFactory(String keyStorePath,
                                   String keyStorePassword,
                                   String keyStoreType,
                                   String trustStorePath,
                                   String trustStorePassword,
                                   String trustStoreType,
                                   String protocol,
                                   boolean verifyHostname) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreType = keyStoreType;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.trustStoreType = trustStoreType;
        this.protocol = protocol;
        this.verifyHostname = verifyHostname;
    }

    /**
     * Create a new {@link SimpleSSLContextFactory}.
     *
     * @param keyStorePath        path to the key store
     * @param keyStorePassword    password of the key store
     * @param keyStoreType        the keystore type
     * @param trustStorePath      path to the trust store
     * @param trustStorePassword  password of the trust store
     * @param trustStoreType      the trust store type
     * @param protocol            the protocol to use
     * @param verifyHostname      whether to verify host names or not
     * @param disableSniHostCheck whether to disable SNI host checking
     */
    @SuppressWarnings("java:S107")
    public SimpleSSLContextFactory(String keyStorePath,
                                   String keyStorePassword,
                                   String keyStoreType,
                                   String trustStorePath,
                                   String trustStorePassword,
                                   String trustStoreType,
                                   String protocol,
                                   boolean verifyHostname,
                                   boolean disableSniHostCheck) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreType = keyStoreType;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.trustStoreType = trustStoreType;
        this.protocol = protocol;
        this.verifyHostname = verifyHostname;
        this.disableSniHostCheck = disableSniHostCheck;
    }

    /**
     * A builder class for {@link SimpleSSLContextFactory}.
     * <p>
     * If not specified, key and trust store type default to "JKS", and {@code verifyHostname} defaults to {@code true}.
     *
     * @implNote This was implemented well before we started using Lombok, so is manual builder code, though
     * we have added both the Lombok-style xxx() and keeping the original setXxx() methods.
     * Subject to change in the future.
     */
    public static class Builder {

        private final Map<String, Optional<String>> entries;

        protected Builder() {
            entries = KiwiMaps.newHashMap(
                    KEY_STORE_PATH_PROPERTY, Optional.empty(),
                    KEY_STORE_PASSWORD_PROPERTY, Optional.empty(),
                    KEY_STORE_TYPE_PROPERTY, Optional.of(KeyStoreType.JKS.value),
                    TRUST_STORE_PATH_PROPERTY, Optional.empty(),
                    TRUST_STORE_PASSWORD_PROPERTY, Optional.empty(),
                    TRUST_STORE_TYPE_PROPERTY, Optional.of(KeyStoreType.JKS.value),
                    PROTOCOL_PROPERTY, Optional.empty(),
                    VERIFY_HOSTNAME_PROPERTY, Optional.of("true"),
                    DISABLE_SNI_HOST_CHECK_PROPERTY, Optional.of("false")
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

        public Builder keyStoreType(String keyStoreType) {
            return setKeyStoreType(keyStoreType);
        }

        public Builder setKeyStoreType(String keyStoreType) {
            entries.put(KEY_STORE_TYPE_PROPERTY, Optional.of(keyStoreType));
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

        public Builder trustStoreType(String trustStoreType) {
            return setTrustStoreType(trustStoreType);
        }

        public Builder setTrustStoreType(String trustStoreType) {
            entries.put(TRUST_STORE_TYPE_PROPERTY, Optional.of(trustStoreType));
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
            entries.put(VERIFY_HOSTNAME_PROPERTY, optionalStringOf(verifyHostname));
            return this;
        }

        public Builder disableSniHostCheck(boolean disableSniHostCheck) {
            return setDisableSniHostCheck(disableSniHostCheck);
        }

        public Builder setDisableSniHostCheck(boolean disableSniHostCheck) {
            entries.put(DISABLE_SNI_HOST_CHECK_PROPERTY, optionalStringOf(disableSniHostCheck));
            return this;
        }

        private static Optional<String> optionalStringOf(boolean value) {
            return Optional.of(String.valueOf(value));
        }

        public SimpleSSLContextFactory build() {
            validateBuilderState();

            return new SimpleSSLContextFactory(
                    stringOrNull(KEY_STORE_PATH_PROPERTY),
                    stringOrNull(KEY_STORE_PASSWORD_PROPERTY),
                    stringOrThrow(KEY_STORE_TYPE_PROPERTY),
                    stringOrThrow(TRUST_STORE_PATH_PROPERTY),
                    stringOrThrow(TRUST_STORE_PASSWORD_PROPERTY),
                    stringOrThrow(TRUST_STORE_TYPE_PROPERTY),
                    stringOrThrow(PROTOCOL_PROPERTY),
                    toBooleanOrThrow(VERIFY_HOSTNAME_PROPERTY),
                    toBooleanOrThrow(DISABLE_SNI_HOST_CHECK_PROPERTY)
            );
        }

        public void validateBuilderState() {
            entries.entrySet().stream()
                    .filter(entry -> entry.getValue().isEmpty())
                    .filter(entry -> REQUIRED_PROPERTIES.contains(entry.getKey()))
                    .findAny()
                    .ifPresent(entry -> throwBuildException(entry.getKey()));
        }

        private String stringOrNull(String propertyName) {
            return entries.get(propertyName).orElse(null);
        }

        private String stringOrThrow(String propertyName) {
            return entries.get(propertyName).orElseThrow(IllegalStateException::new);
        }

        private boolean toBooleanOrThrow(String propertyName) {
            var boolString = entries.get(propertyName).orElseThrow(IllegalStateException::new);
            return Boolean.parseBoolean(boolString);
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
     * @return a new {@link SSLContext} instance when first called; all later calls return the same cached instance
     * @implNote This is intended to be called infrequently, e.g., once when a service/app starts.
     * It is internally synchronized to ensure thread-safety when creating the {@link SSLContext}.
     */
    @Synchronized
    public SSLContext getSslContext() {
        if (isNull(sslContext)) {
            sslContext = KiwiSecurity.createSslContext(
                    keyStorePath, keyStorePassword, keyStoreType, trustStorePath, trustStorePassword, trustStoreType, protocol);
        }
        return sslContext;
    }

    /**
     * Get the properties this factory was configured with, <strong>including passwords</strong>.
     * Callers are responsible for securely handling the result, and not unnecessarily exposing it.
     *
     * @return a map containing the configuration of this factory
     * @apiNote This is publicly exposed but should not generally be necessary except in tests, and perhaps debugging.
     * @implNote Uses {@link KiwiMaps#newHashMap(Object...)} because some values may be {@code null}, e.g., the key
     * store path, and wraps that using {@link Collections#unmodifiableMap(Map)} to prevent modification of the
     * returned map.
     */
    public Map<String, Object> configuration() {
        return Collections.unmodifiableMap(KiwiMaps.newHashMap(
                KEY_STORE_PATH_PROPERTY, keyStorePath,
                KEY_STORE_PASSWORD_PROPERTY, keyStorePassword,
                KEY_STORE_TYPE_PROPERTY, keyStoreType,
                TRUST_STORE_PATH_PROPERTY, trustStorePath,
                TRUST_STORE_PASSWORD_PROPERTY, trustStorePassword,
                TRUST_STORE_TYPE_PROPERTY, trustStoreType,
                PROTOCOL_PROPERTY, protocol,
                VERIFY_HOSTNAME_PROPERTY, verifyHostname,
                DISABLE_SNI_HOST_CHECK_PROPERTY, disableSniHostCheck
        ));
    }
}
