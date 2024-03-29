package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.beans.BeanConverter;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SecureTestConstants;

import java.util.Map;

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
        void shouldDefaultTrustStoreTypeToJKS() {
            assertThat(baselineConfig.getTrustStoreType()).isEqualTo(KeyStoreType.JKS.value);
        }

        @Test
        void shouldAllowDifferentTrustStoreType() {
            baselineConfig.setTrustStoreType(KeyStoreType.PKCS12.value);
            assertThat(baselineConfig.getTrustStoreType()).isEqualTo(KeyStoreType.PKCS12.value);
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

        assertAll(
                () -> assertThat(tlsConfig.getKeyStorePath()).isEqualTo(sslConfig.getKeyStorePath()),
                () -> assertThat(tlsConfig.getKeyStorePassword()).isEqualTo(sslConfig.getKeyStorePassword()),
                () -> assertThat(tlsConfig.getKeyStoreType()).isEqualTo(sslConfig.getKeyStoreType()),
                () -> assertThat(tlsConfig.getTrustStorePath()).isEqualTo(sslConfig.getTrustStorePath()),
                () -> assertThat(tlsConfig.getTrustStorePassword()).isEqualTo(sslConfig.getTrustStorePassword()),
                () -> assertThat(tlsConfig.getTrustStoreType()).isEqualTo(sslConfig.getTrustStoreType()),
                () -> assertThat(tlsConfig.isVerifyHostname()).isEqualTo(sslConfig.isVerifyHostname()),
                () -> assertThat(tlsConfig.isDisableSniHostCheck()).isEqualTo(sslConfig.isDisableSniHostCheck()),
                () -> assertThat(tlsConfig.getProtocol()).isEqualTo(sslConfig.getProtocol()),
                () -> assertThat(tlsConfig.getSupportedProtocols()).isNull()
        );
    }

    @Test
    void shouldConvertToSimpleSSLContextFactory() {
        var sslConfig = newSampleSslContextConfiguration();
        var sslContextFactory = sslConfig.toSimpleSSLContextFactory();

        // Since SimpleSSLContextFactory does not have getters for most properties, get its
        // configuration as a Map, convert it to an SSLContextConfiguration, and verify that
        // it is equal to the original sslConfig
        var converter = new BeanConverter<Map<String, Object>>();
        var expectedConfig = converter.convert(sslContextFactory.configuration(), new SSLContextConfiguration());
        assertThat(expectedConfig)
                .describedAs("round-trip conversion should result in equal SSLContextConfiguration")
                .usingRecursiveComparison()
                .isEqualTo(sslConfig);
    }

    private SSLContextConfiguration newSampleSslContextConfiguration() {
        return SSLContextConfiguration.builder()
                .keyStorePath(path)
                .keyStorePassword(password)
                .keyStoreType(type)
                .trustStorePath(path)
                .trustStorePassword(password)
                .trustStoreType(type)
                .protocol(protocol)
                .verifyHostname(false)
                .disableSniHostCheck(true)
                .build();
    }
}
