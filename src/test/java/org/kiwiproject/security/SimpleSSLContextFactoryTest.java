package org.kiwiproject.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .build();

        assertThat(contextFactory.getSslContext()).isNotNull();
        assertThat(contextFactory.isVerifyHostname()).isFalse();
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
}