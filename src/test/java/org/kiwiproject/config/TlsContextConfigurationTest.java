package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.newValidator;

import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.testing.FixtureHelpers;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SSLContextProtocol;
import org.kiwiproject.security.SecureTestConstants;
import org.yaml.snakeyaml.Yaml;

import javax.validation.Validator;
import java.io.File;
import java.util.List;

@DisplayName("TlsContextConfiguration")
class TlsContextConfigurationTest {

    private static final Validator VALIDATOR = newValidator();

    @Nested
    class NoArgsConstructor {

        @Test
        void shouldHaveDefaultValues() {
            assertDefaultValues(new TlsContextConfiguration());
        }
    }

    @Nested
    class Builder {

        @Test
        void shouldHaveDefaultValues() {
            assertDefaultValues(TlsContextConfiguration.builder().build());
        }
    }

    @Nested
    class ToString {

        private TlsContextConfiguration config;

        @BeforeEach
        void setUp() {
            config = loadTlsContextConfiguration("TlsContextConfigurationTest/full-tls-config.yml");
        }

        @Test
        void shouldNotContainKeyStorePassword() {
            var string = config.toString();
            assertThat(string)
                    .doesNotContain("keyStorePassword")
                    .doesNotContain(config.getKeyStorePassword());
        }

        @Test
        void shouldNotContainTrustKeyStorePassword() {
            var string = config.toString();
            assertThat(string)
                    .doesNotContain("trustStorePassword")
                    .doesNotContain(config.getTrustStorePassword());
        }
    }

    @SuppressWarnings("WeakerAccess")  // must be public for Yaml to instantiate
    @Getter
    @Setter
    public static class SampleAppConfig {
        private TlsContextConfiguration tlsConfig;
    }

    @Nested
    class FromYaml {

        @Test
        void shouldDeserializeMinimalConfig() {
            var tlsConfig = loadTlsContextConfiguration("TlsContextConfigurationTest/minimal-tls-config.yml");

            assertDefaultValues(tlsConfig);

            assertThat(tlsConfig.getKeyStorePath()).isEqualTo("/path/to/keystore.jks");
            assertThat(tlsConfig.getKeyStorePassword()).isEqualTo("ksPassWd");
            assertThat(tlsConfig.getTrustStorePath()).isEqualTo("/path/to/truststore.jks");
            assertThat(tlsConfig.getTrustStorePassword()).isEqualTo("tsPass100");
        }

        @Test
        void shouldDeserializeMinimalWithNoKeyStorePropertiesConfig() {
            var tlsConfig = loadTlsContextConfiguration("TlsContextConfigurationTest/minimal-no-key-props-tls-config.yml");

            assertDefaultValues(tlsConfig);

            assertThat(tlsConfig.getKeyStorePath()).isNull();
            assertThat(tlsConfig.getKeyStorePassword()).isNull();
        }

        @Test
        void shouldDeserializeFullConfig() {
            var tlsConfig = loadTlsContextConfiguration("TlsContextConfigurationTest/full-tls-config.yml");

            assertThat(tlsConfig.getProtocol()).isEqualTo("TLSv1.3");
            assertThat(tlsConfig.getKeyStorePath()).isEqualTo("/path/to/keystore.pkcs12");
            assertThat(tlsConfig.getKeyStorePassword()).isEqualTo("ksPassWd");
            assertThat(tlsConfig.getKeyStoreType()).isEqualTo("PKCS12");
            assertThat(tlsConfig.getTrustStorePath()).isEqualTo("/path/to/truststore.pkcs12");
            assertThat(tlsConfig.getTrustStorePassword()).isEqualTo("tsPass100");
            assertThat(tlsConfig.getTrustStoreType()).isEqualTo("PKCS12");
            assertThat(tlsConfig.isVerifyHostname()).isFalse();
            assertThat(tlsConfig.getSupportedProtocols()).containsOnly(
                    SSLContextProtocol.TLS_1_2.value,
                    SSLContextProtocol.TLS_1_3.value
            );
        }
    }

    private static void assertDefaultValues(TlsContextConfiguration config) {
        assertThat(config.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_2.value);
        assertThat(config.getKeyStoreType()).isEqualTo(KeyStoreType.JKS.value);
        assertThat(config.getTrustStoreType()).isEqualTo(KeyStoreType.JKS.value);
        assertThat(config.isVerifyHostname()).isTrue();
        assertThat(config.getSupportedProtocols()).isNull();
    }

    @Nested
    class Validation {

        private TlsContextConfiguration config;

        @BeforeEach
        void setUp() {
            config = new TlsContextConfiguration();
        }

        @Nested
        class ShouldPass {

            @Test
            void whenConfigurationIsValid() {
                config.setTrustStorePath("/data/vault/etc/pki/ts/jks");
                config.setTrustStorePassword("passwd67890");

                assertNoViolations(VALIDATOR, config);
            }
        }

        @Nested
        class ShouldFail {

            @Test
            void whenProtocolIsBlank() {
                config.setProtocol("");
                assertOnePropertyViolation(VALIDATOR, config, "protocol");
            }

            @Test
            void whenKeyStoreTypeIsBlank() {
                config.setKeyStoreType("");
                assertOnePropertyViolation(VALIDATOR, config, "keyStoreType");
            }

            @Test
            void whenTrustStorePathIsBlank() {
                config.setTrustStorePath("");
                assertOnePropertyViolation(VALIDATOR, config, "trustStorePath");
            }

            @Test
            void whenTrustStorePasswordIsNull() {
                config.setTrustStorePassword(null);
                assertOnePropertyViolation(VALIDATOR, config, "trustStorePassword");
            }

            @Test
            void whenTrustStoreTypeIsBlank() {
                config.setTrustStoreType("");
                assertOnePropertyViolation(VALIDATOR, config, "trustStoreType");
            }
        }
    }

    @Nested
    class ConversionMethods {

        private TlsContextConfiguration config;
        private String protocol;
        private String path;
        private String password;
        private String type;

        @BeforeEach
        void setUp() {
            protocol = SecureTestConstants.TLS_PROTOCOL;
            path = SecureTestConstants.JKS_FILE_PATH;
            password = SecureTestConstants.JKS_PASSWORD;
            type = SecureTestConstants.STORE_TYPE;

            config = TlsContextConfiguration.builder()
                    .protocol(protocol)
                    .keyStorePath(path)
                    .keyStorePassword(password)
                    .keyStoreType(type)
                    .trustStorePath(path)
                    .trustStorePassword(password)
                    .trustStoreType(type)
                    .verifyHostname(false)
                    .build();
        }

        @Nested
        class FromDropwizardTlsConfiguration {

            private TlsConfiguration dwTlsConfig;

            @BeforeEach
            void setUp() {
                dwTlsConfig = new TlsConfiguration();
                dwTlsConfig.setProtocol("TLSv1.3");
                dwTlsConfig.setKeyStorePath(new File("/pki/test.ks"));
                dwTlsConfig.setKeyStorePassword("ks-pass");
                dwTlsConfig.setKeyStoreType("PKCS12");
                dwTlsConfig.setTrustStorePath(new File("/pki/test.ts"));
                dwTlsConfig.setTrustStorePassword("ts-pass");
                dwTlsConfig.setTrustStoreType("PKCS12");
                dwTlsConfig.setVerifyHostname(false);
                dwTlsConfig.setSupportedProtocols(List.of("TLSv1.3"));
            }

            @Test
            void shouldNotAllowNullArguments() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> TlsContextConfiguration.fromDropwizardTlsConfiguration(null))
                        .withMessage("TlsConfiguration cannot be null");
            }

            @Test
            void shouldReturnTlsContextConfiguration_FromDefaultDropwizardTlsConfiguration() {
                dwTlsConfig = new TlsConfiguration();

                var tlsContextConfig = TlsContextConfiguration.fromDropwizardTlsConfiguration(dwTlsConfig);

                // The following also test assumptions about defaults of Dropwizard's TlsConfiguration, for example
                // which are null. This isn't wonderful but at the same time we'll find out quickly what Dropwizard
                // has changed.
                assertThat(tlsContextConfig.getProtocol()).isNotNull().isEqualTo(dwTlsConfig.getProtocol());
                assertThat(tlsContextConfig.getKeyStorePath()).isNull();
                assertThat(tlsContextConfig.getKeyStorePassword()).isNull();
                assertThat(tlsContextConfig.getKeyStoreType()).isNotNull().isEqualTo(dwTlsConfig.getKeyStoreType());
                assertThat(tlsContextConfig.getTrustStorePath()).isNull();
                assertThat(tlsContextConfig.getTrustStorePassword()).isNull();
                assertThat(tlsContextConfig.getTrustStoreType()).isNotNull().isEqualTo(dwTlsConfig.getTrustStoreType());
                assertThat(tlsContextConfig.isVerifyHostname()).isTrue();
                assertThat(tlsContextConfig.getSupportedProtocols()).isNull();
            }

            @Test
            void shouldReturnTlsContextConfiguration() {
                var tlsContextConfig = TlsContextConfiguration.fromDropwizardTlsConfiguration(dwTlsConfig);

                assertThat(tlsContextConfig.getProtocol()).isEqualTo("TLSv1.3");
                assertThat(tlsContextConfig.getKeyStorePath()).isEqualTo("/pki/test.ks");
                assertThat(tlsContextConfig.getKeyStorePassword()).isEqualTo("ks-pass");
                assertThat(tlsContextConfig.getKeyStoreType()).isEqualTo("PKCS12");
                assertThat(tlsContextConfig.getTrustStorePath()).isEqualTo("/pki/test.ts");
                assertThat(tlsContextConfig.getTrustStorePassword()).isEqualTo("ts-pass");
                assertThat(tlsContextConfig.getTrustStoreType()).isEqualTo("PKCS12");
                assertThat(tlsContextConfig.isVerifyHostname()).isFalse();
                assertThat(tlsContextConfig.getSupportedProtocols()).containsOnly("TLSv1.3");
            }

            @Test
            void shouldHandleNullKeyStorePathAndPassword() {
                dwTlsConfig.setKeyStorePath(null);
                dwTlsConfig.setKeyStorePassword(null);

                var tlsContextConfig = TlsContextConfiguration.fromDropwizardTlsConfiguration(dwTlsConfig);

                assertThat(tlsContextConfig.getKeyStorePath()).isNull();
                assertThat(tlsContextConfig.getKeyStorePassword()).isNull();
            }

            @Test
            void shouldHandleNullTrustStorePathAndPassword() {
                dwTlsConfig.setTrustStorePath(null);
                dwTlsConfig.setTrustStorePassword(null);

                var tlsContextConfig = TlsContextConfiguration.fromDropwizardTlsConfiguration(dwTlsConfig);

                assertThat(tlsContextConfig.getTrustStorePath()).isNull();
                assertThat(tlsContextConfig.getTrustStorePassword()).isNull();
            }

            @Test
            void shouldHandleNullSupportedProtocols() {
                dwTlsConfig.setSupportedProtocols(null);

                var tlsContextConfig = TlsContextConfiguration.fromDropwizardTlsConfiguration(dwTlsConfig);

                assertThat(tlsContextConfig.getSupportedProtocols()).isNull();
            }
        }

        @Nested
        class ToSslContext {

            @Test
            void shouldReturnSslContext() {
                var sslContext = config.toSSLContext();

                assertThat(sslContext).isNotNull();
                assertThat(sslContext.getProtocol()).isEqualTo(config.getProtocol());
            }
        }

        @Nested
        class ToSslSocketFactory {

            @Test
            void shouldReturnSslSocketFactory() {
                var sslSocketFactory = config.toSslSocketFactory();
                assertThat(sslSocketFactory).isNotNull();
            }
        }

        @Nested
        class ToDropwizardTlsConfiguration {

            @SuppressWarnings("ConstantConditions")  // b/c IntelliJ sees Dropwizard [key|trust]StorePath are @Nullable
            @Test
            void shouldReturnTlsConfiguration() {
                var dwTlsConfig = config.toDropwizardTlsConfiguration();

                assertThat(dwTlsConfig.getProtocol()).isEqualTo(protocol);
                assertThat(dwTlsConfig.getKeyStorePath().getAbsolutePath()).isEqualTo(path);
                assertThat(dwTlsConfig.getKeyStorePassword()).isEqualTo(password);
                assertThat(dwTlsConfig.getKeyStoreType()).isEqualTo(type);
                assertThat(dwTlsConfig.getTrustStorePath().getAbsolutePath()).isEqualTo(path);
                assertThat(dwTlsConfig.getTrustStorePassword()).isEqualTo(password);
                assertThat(dwTlsConfig.getTrustStoreType()).isEqualTo(type);
                assertThat(dwTlsConfig.isVerifyHostname()).isFalse();
            }

            @SuppressWarnings("ConstantConditions")  // b/c IntelliJ sees Dropwizard [key|trust]StorePath are @Nullable
            @Test
            void shouldCorrectlyHandleWhenOnlyTrustStoreIsPresent() {
                config = TlsContextConfiguration.builder()
                        .trustStorePath("/data/pki/trust-store.pkcs12")
                        .trustStorePassword("mySuperSecretTsPassword")
                        .trustStoreType(KeyStoreType.PKCS12.value)
                        .build();

                var dwTlsConfig = config.toDropwizardTlsConfiguration();

                assertThat(dwTlsConfig.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_2.value);
                assertThat(dwTlsConfig.getKeyStorePath()).isNull();
                assertThat(dwTlsConfig.getKeyStorePassword()).isNull();
                assertThat(dwTlsConfig.getKeyStoreType()).isEqualTo(KeyStoreType.JKS.value);
                assertThat(dwTlsConfig.getTrustStorePath().getAbsolutePath()).isEqualTo("/data/pki/trust-store.pkcs12");
                assertThat(dwTlsConfig.getTrustStorePassword()).isEqualTo("mySuperSecretTsPassword");
                assertThat(dwTlsConfig.getTrustStoreType()).isEqualTo(KeyStoreType.PKCS12.value);
                assertThat(dwTlsConfig.isVerifyHostname()).isTrue();
            }
        }
    }

    private static TlsContextConfiguration loadTlsContextConfiguration(String filename) {
        var yaml = new Yaml();
        var yamlConfig = FixtureHelpers.fixture(filename);
        var appConfig = yaml.loadAs(yamlConfig, SampleAppConfig.class);
        return appConfig.getTlsConfig();
    }
}