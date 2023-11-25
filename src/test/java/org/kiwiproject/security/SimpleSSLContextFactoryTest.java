package org.kiwiproject.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SimpleSSLContextFactory")
class SimpleSSLContextFactoryTest {

    private String path;
    private String password;
    private String protocol;

    @BeforeEach
    void setUp() {
        path = SecureTestConstants.JKS_FILE_PATH;
        password = SecureTestConstants.JKS_PASSWORD;
        protocol = SecureTestConstants.TLS_PROTOCOL;
    }

    @Test
    void shouldDefaultVerifyHostNameToTrue() {
        var contextFactory = new SimpleSSLContextFactory(path, password, path, password, protocol);

        assertThat(contextFactory.isVerifyHostname()).isTrue();
    }

    @Test
    void shouldDefaultVerifyHostNameToTrue_ForBuilder() {
        var contextFactory = SimpleSSLContextFactory.builder()
                .trustStorePath(path)
                .trustStorePassword(password)
                .protocol(protocol)
                .build();

        assertThat(contextFactory.isVerifyHostname()).isTrue();
    }

    @Test
    void shouldDefaultDisableSniHostCheckToFalse() {
        var contextFactory = new SimpleSSLContextFactory(path, password, path, password, protocol);

        assertThat(contextFactory.isDisableSniHostCheck()).isFalse();
    }

    @Test
    void shouldDefaultDisableSniHostCheckToFalse_ForBuilder() {
        var contextFactory = SimpleSSLContextFactory.builder()
                .trustStorePath(path)
                .trustStorePassword(password)
                .protocol(protocol)
                .build();

        assertThat(contextFactory.isDisableSniHostCheck()).isFalse();
    }

    @Test
    void shouldCreateSimpleSSLContextFactory() {
        var contextFactory = new SimpleSSLContextFactory(path, password, path, password, protocol);

        var sslContext = contextFactory.getSslContext();
        assertThat(sslContext).isNotNull();
    }

    @Test
    void shouldBuildSimpleSSLContextFactory() {
        var contextFactory = SimpleSSLContextFactory.builder()
                .keyStorePath(path)
                .keyStorePassword(password)
                .trustStorePath(path)
                .trustStorePassword(password)
                .protocol(protocol)
                .verifyHostname(false)
                .disableSniHostCheck(true)
                .build();

        assertThat(contextFactory.getSslContext()).isNotNull();
        assertThat(contextFactory.isVerifyHostname()).isFalse();
        assertThat(contextFactory.isDisableSniHostCheck()).isTrue();
    }

    @Test
    void shouldBuildWithNoKeyStoreProperties() {
        var contextFactory = SimpleSSLContextFactory.builder()
                .trustStorePath(path)
                .trustStorePassword(password)
                .protocol(protocol)
                .build();

        assertThat(contextFactory.getSslContext()).isNotNull();
    }

    @Nested
    class ShouldThrowExceptionBuilding {

        @Test
        void whenMissing_TrustStorePath() {
            assertThatThrownBy(() ->
                    SimpleSSLContextFactory.builder()
                            .trustStorePassword(password)
                            .protocol(protocol)
                            .build())
                    .isExactlyInstanceOf(SSLContextException.class)
                    .hasNoCause()
                    .hasMessage("Required property 'trustStorePath' not set; cannot build SimpleSSLContextFactory");
        }

        @Test
        void whenMissing_TrustStorePassword() {
            assertThatThrownBy(() ->
                    SimpleSSLContextFactory.builder()
                            .trustStorePath(path)
                            .protocol(protocol)
                            .build())
                    .isExactlyInstanceOf(SSLContextException.class)
                    .hasNoCause()
                    .hasMessage("Required property 'trustStorePassword' not set; cannot build SimpleSSLContextFactory");
        }

        @Test
        void whenMissing_Protocol() {
            assertThatThrownBy(() ->
                    SimpleSSLContextFactory.builder()
                            .trustStorePath(path)
                            .trustStorePassword(password)
                            .build())
                    .isExactlyInstanceOf(SSLContextException.class)
                    .hasNoCause()
                    .hasMessage("Required property 'protocol' not set; cannot build SimpleSSLContextFactory");
        }
    }

    @Nested
    class Configuration {

        @Test
        void shouldReturnUnmodifiableMap() {
            var factory = SimpleSSLContextFactory.builder()
                    .trustStorePath("/path/to/trust_store")
                    .trustStorePassword("password_12345")
                    .protocol("TLSv1.3")
                    .build();

            var config = factory.configuration();

            assertThat(config).isUnmodifiable();
        }

        @Test
        void shouldSetDefaultsForSomeProperties() {
            var factory = SimpleSSLContextFactory.builder()
                    .trustStorePath("/path/to/trust_store")
                    .trustStorePassword("password_12345")
                    .protocol("TLSv1.2")
                    .build();

            var config = factory.configuration();

            assertThat(config)
                    .describedAs("should have default keyStoreType, trustStoreType, verifyHostname")
                    .containsOnly(
                            entry("keyStorePath", null),
                            entry("keyStorePassword", null),
                            entry("keyStoreType", "JKS"),
                            entry("trustStorePath", "/path/to/trust_store"),
                            entry("trustStorePassword", "password_12345"),
                            entry("trustStoreType", "JKS"),
                            entry("protocol", "TLSv1.2"),
                            entry("verifyHostname", true),
                            entry("disableSniHostCheck", false)
                    );
        }

        @Test
        void shouldSetAllProperties() {
            var factory = SimpleSSLContextFactory.builder()
                    .keyStorePath("/path/to/key_store")
                    .keyStorePassword("password_xyz")
                    .keyStoreType(KeyStoreType.PKCS11.value)
                    .trustStorePath("/path/to/trust_store")
                    .trustStorePassword("password_12345")
                    .trustStoreType(KeyStoreType.PKCS12.value)
                    .protocol("TLSv1.1")
                    .verifyHostname(false)
                    .disableSniHostCheck(true)
                    .build();

            var config = factory.configuration();

            assertThat(config).containsOnly(
                    entry("keyStorePath", "/path/to/key_store"),
                    entry("keyStorePassword", "password_xyz"),
                    entry("keyStoreType", "PKCS11"),
                    entry("trustStorePath", "/path/to/trust_store"),
                    entry("trustStorePassword", "password_12345"),
                    entry("trustStoreType", "PKCS12"),
                    entry("protocol", "TLSv1.1"),
                    entry("verifyHostname", false),
                    entry("disableSniHostCheck", true)
            );
        }
    }
}
