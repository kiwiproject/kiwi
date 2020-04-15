package org.kiwiproject.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kiwiproject.security.KeyAndTrustStoreConfigProvider;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SimpleSSLContextFactory;

import javax.net.ssl.SSLContext;

/**
 * Configuration for standard/common properties required for secure (i.e. SSL/TLS) connections.
 */
@Getter
@Setter
public class SSLContextConfiguration implements KeyAndTrustStoreConfigProvider {

    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;
    private String protocol;
    private String keyStoreType = KeyStoreType.JKS.value;
    private boolean verifyHostname = true;

    /**
     * A builder class for {@link SSLContextConfiguration}.
     *
     * @implNote This was implemented well before we started using Lombok, so is manual builder code, though
     * we have added both the Lombok-style xxx() as well as keeping the original setXxx() methods.
     * Subject to change in future.
     */
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Builder {

        private final SSLContextConfiguration configuration = new SSLContextConfiguration();

        public Builder keyStorePath(String keyStorePath) {
            return setKeyStorePath(keyStorePath);
        }

        public Builder setKeyStorePath(String keyStorePath) {
            configuration.setKeyStorePath(keyStorePath);
            return this;
        }

        public Builder keyStorePassword(String keyStorePassword) {
            return setKeyStorePassword(keyStorePassword);
        }

        public Builder setKeyStorePassword(String keyStorePassword) {
            configuration.setKeyStorePassword(keyStorePassword);
            return this;
        }

        public Builder trustStorePath(String trustStorePath) {
            return setTrustStorePath(trustStorePath);
        }

        public Builder setTrustStorePath(String trustStorePath) {
            configuration.setTrustStorePath(trustStorePath);
            return this;
        }

        public Builder trustStorePassword(String trustStorePassword) {
            return setTrustStorePassword(trustStorePassword);
        }

        public Builder setTrustStorePassword(String trustStorePassword) {
            configuration.setTrustStorePassword(trustStorePassword);
            return this;
        }

        public Builder protocol(String protocol) {
            return setProtocol(protocol);
        }

        public Builder setProtocol(String protocol) {
            configuration.setProtocol(protocol);
            return this;
        }

        public Builder keyStoreType(String keyStoreType) {
            return setKeyStoreType(keyStoreType);
        }

        public Builder setKeyStoreType(String keyStoreType) {
            configuration.setKeyStoreType(keyStoreType);
            return this;
        }

        public Builder verifyHostname(boolean verifyHostname) {
            return setVerifyHostname(verifyHostname);
        }

        public Builder setVerifyHostname(boolean verifyHostname) {
            configuration.setVerifyHostname(verifyHostname);
            return this;
        }

        public SSLContextConfiguration build() {
            return configuration;
        }
    }

    /**
     * Return a new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Overrides and defines trust store type to be the same as {@code keyStoreType}, since this is
     * the most common case.
     * <p>
     * This may be changed in the future by adding an explicit trust store type property, though we would
     * keep the same default value.
     */
    @Override
    public String getTrustStoreType() {
        return keyStoreType;
    }

    /**
     * Convert this instance to a new {@link SSLContext}.
     * <p>
     * If you would rather not create a new instance every time, use {@link #toSimpleSSLContextFactory()}
     * to obtain a factory that will always return the same {@link SSLContext} instance.
     *
     * @return a new {@link SSLContext} instance
     * @implNote This will always create a new instance, first creating a new {@link SimpleSSLContextFactory}
     * and then using that to create the {@link SSLContext}.
     */
    @Override
    public SSLContext toSSLContext() {
        return toSimpleSSLContextFactory().getSslContext();
    }

    /**
     * Convert this configuration to a {@link SimpleSSLContextFactory}.
     *
     * @return a new instance
     */
    public SimpleSSLContextFactory toSimpleSSLContextFactory() {
        return new SimpleSSLContextFactory(
                keyStorePath, keyStorePassword, trustStorePath, trustStorePassword, protocol, verifyHostname);
    }

    /**
     * Convert this {@link SSLContextConfiguration} to a {@link TlsContextConfiguration}, using the
     * {@code keyStoreType} for both the key and trust store type in the returned object.
     * <p>
     * Use {@link #toTlsContextConfiguration(String)} if you need to specify a different trust store type.
     *
     * @return a new {@link TlsContextConfiguration} instance
     * @see #toTlsContextConfiguration(String)
     */
    public TlsContextConfiguration toTlsContextConfiguration() {
        return toTlsContextConfiguration(keyStoreType);
    }

    /**
     * Convert this {@link SSLContextConfiguration} to a {@link TlsContextConfiguration}, using the
     * {@code keyStoreType} for the key and the specified {@code trustStoreType} as the trust store type
     * in the returned object.
     *
     * @return a new {@link TlsContextConfiguration} instance
     */
    public TlsContextConfiguration toTlsContextConfiguration(String trustStoreType) {
        return TlsContextConfiguration.builder()
                .keyStorePath(keyStorePath)
                .keyStorePassword(keyStorePassword)
                .keyStoreType(keyStoreType)
                .trustStorePath(trustStorePath)
                .trustStorePassword(trustStorePassword)
                .trustStoreType(trustStoreType)
                .protocol(protocol)
                .verifyHostname(verifyHostname)
                .build();
    }
}
