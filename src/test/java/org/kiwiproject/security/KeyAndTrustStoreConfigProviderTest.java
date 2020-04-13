package org.kiwiproject.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KeyAndTrustStoreConfigProvider")
class KeyAndTrustStoreConfigProviderTest {

    private KeyAndTrustStoreConfigProvider keyAndTrustStoreConfig;

    @BeforeEach
    void setUp() {
        keyAndTrustStoreConfig = new KeyAndTrustStoreConfigProvider() {
            @Override
            public String getKeyStorePath() {
                return SecureTestConstants.JKS_FILE_PATH;
            }

            @Override
            public String getKeyStorePassword() {
                return SecureTestConstants.JKS_PASSWORD;
            }

            @Override
            public String getProtocol() {
                return SecureTestConstants.TLS_PROTOCOL;
            }

            @Override
            public String getTrustStorePath() {
                return SecureTestConstants.JKS_FILE_PATH;
            }

            @Override
            public String getTrustStorePassword() {
                return SecureTestConstants.JKS_PASSWORD;
            }
        };
    }

    @Test
    void shouldDefaultTrustStoreTypeToJKS() {
        assertThat(keyAndTrustStoreConfig.getTrustStoreType()).isEqualTo(KeyStoreType.JKS.value);
    }

    @Test
    void shouldDefaultVerifyHostnameToTrue() {
        assertThat(keyAndTrustStoreConfig.isVerifyHostname()).isTrue();
    }

    @Test
    void shouldDefaultKeyStoreTypeToJKS() {
        assertThat(keyAndTrustStoreConfig.getKeyStoreType()).isEqualTo(KeyStoreType.JKS.value);
    }

    @Test
    void shouldConvertToSslContext() {
        assertThat(keyAndTrustStoreConfig.toSSLContext()).isNotNull();
    }

    @Test
    void shouldConvertToSslSocketFactory() {
        assertThat(keyAndTrustStoreConfig.toSslSocketFactory()).isNotNull();
    }
}