package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SecureTestConstants;

@DisplayName("SSLContextConfiguration")
class SSLContextConfigurationTest {

    private String path;
    private String password;
    private String type;
    private String protocol;

    @BeforeEach
    void setUp() {
        path = SecureTestConstants.JKS_FILE_PATH;
        password = SecureTestConstants.JKS_PASSWORD;
        type = KeyStoreType.PKCS12.value;
        protocol = SecureTestConstants.TLS_PROTOCOL;
    }

    @Nested
    class GetKeyAndTrustStoreTypes {

        private SSLContextConfiguration baselineConfig;

        @BeforeEach
        void setUp() {
            baselineConfig = new SSLContextConfiguration();
        }

        @Test
        void shouldDefaultKeyStoreTypeToJKS() {
            assertThat(baselineConfig.getKeyStoreType()).isEqualTo(KeyStoreType.JKS.value);
        }

        @Test
        void shouldReturnTrustStoreTypeEqualToKeyStoreType() {
            assertThat(baselineConfig.getTrustStoreType()).isEqualTo(baselineConfig.getKeyStoreType());
        }
    }

    @Test
    void shouldConvertToSslSocketFactory() {
        var config = newSampleSslContextConfiguration();

        var sslSocketFactory = config.toSslSocketFactory();
        assertThat(sslSocketFactory).isNotNull();
    }

    @Test
    void shouldConvertToTlsContextConfiguration() {
        var sslConfig = newSampleSslContextConfiguration();

        var tlsConfig = sslConfig.toTlsContextConfiguration();

        assertThat(tlsConfig.getKeyStorePath()).isEqualTo(sslConfig.getKeyStorePath());
        assertThat(tlsConfig.getKeyStorePassword()).isEqualTo(sslConfig.getKeyStorePassword());
        assertThat(tlsConfig.getKeyStoreType()).isEqualTo(sslConfig.getKeyStoreType());
        assertThat(tlsConfig.getTrustStorePath()).isEqualTo(sslConfig.getTrustStorePath());
        assertThat(tlsConfig.getTrustStorePassword()).isEqualTo(sslConfig.getTrustStorePassword());
        assertThat(tlsConfig.getTrustStoreType()).isEqualTo(sslConfig.getTrustStoreType());
        assertThat(tlsConfig.isVerifyHostname()).isEqualTo(sslConfig.isVerifyHostname());
        assertThat(tlsConfig.getProtocol()).isEqualTo(sslConfig.getProtocol());
        assertThat(tlsConfig.getSupportedProtocols()).isNull();
    }

    @Test
    void shouldConvertToTlsContextConfiguration_WithDifferentTrustStoreType() {
        var sslConfig = SSLContextConfiguration.builder()
                .keyStorePath("/pki/ks.jks")
                .keyStorePassword("ks-pass")
                .trustStorePath("/pki/ts/pkcs12")
                .setTrustStorePassword("ts-pass")
                .build();

        var tlsConfig = sslConfig.toTlsContextConfiguration(KeyStoreType.PKCS12.value);

        assertThat(tlsConfig.getKeyStorePath()).isEqualTo(sslConfig.getKeyStorePath());
        assertThat(tlsConfig.getKeyStorePassword()).isEqualTo(sslConfig.getKeyStorePassword());
        assertThat(tlsConfig.getKeyStoreType()).isEqualTo(sslConfig.getKeyStoreType());
        assertThat(tlsConfig.getTrustStorePath()).isEqualTo(sslConfig.getTrustStorePath());
        assertThat(tlsConfig.getTrustStorePassword()).isEqualTo(sslConfig.getTrustStorePassword());
        assertThat(tlsConfig.getTrustStoreType()).isEqualTo(KeyStoreType.PKCS12.value);
        assertThat(tlsConfig.isVerifyHostname()).isEqualTo(sslConfig.isVerifyHostname());
        assertThat(tlsConfig.getProtocol()).isEqualTo(sslConfig.getProtocol());
        assertThat(tlsConfig.getSupportedProtocols()).isNull();
    }

    private SSLContextConfiguration newSampleSslContextConfiguration() {
        return SSLContextConfiguration.builder()
                .keyStorePath(path)
                .keyStorePassword(password)
                .keyStoreType(type)
                .trustStorePath(path)
                .trustStorePassword(password)
                .protocol(protocol)
                .verifyHostname(false)
                .build();
    }
}