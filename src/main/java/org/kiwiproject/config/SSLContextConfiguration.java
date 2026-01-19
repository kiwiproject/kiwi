package org.kiwiproject.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kiwiproject.security.KeyAndTrustStoreConfigProvider;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SSLContextProtocol;
import org.kiwiproject.security.SimpleSSLContextFactory;

import javax.net.ssl.SSLContext;

/**
 * Configuration for standard/common properties required for secure (i.e., SSL/TLS) connections.
 * <p>
 * The default protocol is {@link SSLContextProtocol#TLS_1_3}, and the default key
 * and trust store types are {@link KeyStoreType#JKS}.
 */
@Getter
@Setter
public class SSLContextConfiguration implements KeyAndTrustStoreConfigProvider {

    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;
    private String protocol = SSLContextProtocol.TLS_1_3.getValue();
    private String keyStoreType = KeyStoreType.JKS.value;
    private String trustStoreType = KeyStoreType.JKS.value;
    private boolean verifyHostname = true;
    private boolean disableSniHostCheck;

    /**
     * A builder class for {@link SSLContextConfiguration}.
     *
     * @implNote This was implemented well before we started using Lombok, so is manual builder code, though
     * we have added both the Lombok-style xxx() and keeping the original setXxx() methods.
     * Subject to change in the future.
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

        public Builder trustStoreType(String trustStoreType) {
            return setTrustStoreType(trustStoreType);
        }

        public Builder setTrustStoreType(String trustStoreType) {
            configuration.setTrustStoreType(trustStoreType);
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

        /**
         * Whether the SNI (Server Name Indication) host check is disabled. Default is {@code false}
         *
         * @see <a href="https://www.cloudflare.com/learning/ssl/what-is-sni/">What is SNI? How TLS server name indication works</a>
         */
        public Builder disableSniHostCheck(boolean disableSniHostCheck) {
            return setDisableSniHostCheck(disableSniHostCheck);
        }

        public Builder setDisableSniHostCheck(boolean disableSniHostCheck) {
            configuration.setDisableSniHostCheck(disableSniHostCheck);
            return this;
        }

        public SSLContextConfiguration build() {
            return configuration;
        }
    }

    /**
     * Return a new builder instance.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Convert this instance to a new {@link SSLContext}.
     * <p>
     * If you would rather not create a new instance every time, use {@link #toSimpleSSLContextFactory()}
     * to get a factory that will always return the same {@link SSLContext} instance.
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
        return new SimpleSSLContextFactory(keyStorePath,
                keyStorePassword,
                keyStoreType,
                trustStorePath,
                trustStorePassword,
                trustStoreType,
                protocol,
                verifyHostname,
                disableSniHostCheck);
    }

    /**
     * Convert this {@link SSLContextConfiguration} to a {@link TlsContextConfiguration}.
     *
     * @return a new {@link TlsContextConfiguration} instance
     */
    public TlsContextConfiguration toTlsContextConfiguration() {
        return TlsContextConfiguration.builder()
                .keyStorePath(keyStorePath)
                .keyStorePassword(keyStorePassword)
                .keyStoreType(keyStoreType)
                .trustStorePath(trustStorePath)
                .trustStorePassword(trustStorePassword)
                .trustStoreType(trustStoreType)
                .protocol(protocol)
                .verifyHostname(verifyHostname)
                .disableSniHostCheck(disableSniHostCheck)
                .build();
    }

}
