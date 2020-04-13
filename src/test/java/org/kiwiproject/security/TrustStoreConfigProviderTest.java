package org.kiwiproject.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TrustStoreConfigProvider")
class TrustStoreConfigProviderTest {

    private TrustStoreConfigProvider trustStoreConfig;

    @BeforeEach
    void setUp() {
        trustStoreConfig = new TrustStoreConfigProvider() {
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
        assertThat(trustStoreConfig.getTrustStoreType()).isEqualTo(KeyStoreType.JKS.value);
    }

    @Test
    void shouldDefaultVerifyHostnameToTrue() {
        assertThat(trustStoreConfig.isVerifyHostname()).isTrue();
    }

    @Test
    void shouldConvertToSslContext() {
        assertThat(trustStoreConfig.toSSLContext()).isNotNull();
    }

    @Test
    void shouldConvertToSslSocketFactory() {
        assertThat(trustStoreConfig.toSslSocketFactory()).isNotNull();
    }
}