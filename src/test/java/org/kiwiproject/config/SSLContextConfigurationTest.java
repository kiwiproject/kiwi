package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.beans.BeanConverter;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SSLContextProtocol;
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
        void shouldHaveDefaultProtocol() {
            assertThat(baselineConfig.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_3.getValue());
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
                () -> assertThat(tlsConfig.getKeyStoreProvider()).isNull(),
                () -> assertThat(tlsConfig.getTrustStorePath()).isEqualTo(sslConfig.getTrustStorePath()),
                () -> assertThat(tlsConfig.getTrustStorePassword()).isEqualTo(sslConfig.getTrustStorePassword()),
                () -> assertThat(tlsConfig.getTrustStoreType()).isEqualTo(sslConfig.getTrustStoreType()),
                () -> assertThat(tlsConfig.getTrustStoreProvider()).isNull(),
                () -> assertThat(tlsConfig.isVerifyHostname()).isEqualTo(sslConfig.isVerifyHostname()),
                () -> assertThat(tlsConfig.isDisableSniHostCheck()).isEqualTo(sslConfig.isDisableSniHostCheck()),
                () -> assertThat(tlsConfig.getProtocol()).isEqualTo(sslConfig.getProtocol()),
                () -> assertThat(tlsConfig.getSupportedProtocols()).isNull()
        );
    }

    @Test
    void shouldPreserveProviderPropertiesWhenConvertingToTlsContextConfiguration() {
        var sslConfig = SSLContextConfiguration.builder()
                .keyStorePath(path)
                .keyStorePassword(password)
                .keyStoreType(type)
                .keyStoreProvider("SUN")
                .trustStorePath(path)
                .trustStorePassword(password)
                .trustStoreType(type)
                .trustStoreProvider("SUN")
                .protocol(protocol)
                .build();

        var tlsConfig = sslConfig.toTlsContextConfiguration();

        assertAll(
                () -> assertThat(tlsConfig.getKeyStoreProvider()).isEqualTo("SUN"),
                () -> assertThat(tlsConfig.getTrustStoreProvider()).isEqualTo("SUN")
        );
    }

    @Test
    void shouldPreserveProviderPropertiesWhenConvertingToSimpleSSLContextFactory() {
        var sslConfig = SSLContextConfiguration.builder()
                .keyStorePath(path)
                .keyStorePassword(password)
                .keyStoreType(KeyStoreType.JKS.value)
                .keyStoreProvider("SUN")
                .trustStorePath(path)
                .trustStorePassword(password)
                .trustStoreType(KeyStoreType.JKS.value)
                .trustStoreProvider("SUN")
                .protocol(protocol)
                .build();

        var factoryConfig = sslConfig.toSimpleSSLContextFactory().configuration();

        assertAll(
                () -> assertThat(factoryConfig).containsEntry("keyStoreProvider", "SUN"),
                () -> assertThat(factoryConfig).containsEntry("trustStoreProvider", "SUN")
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
