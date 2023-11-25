package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.util.YamlTestHelper.loadFromYaml;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.newValidator;

import io.dropwizard.client.ssl.TlsConfiguration;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SSLContextProtocol;
import org.kiwiproject.security.SecureTestConstants;

import java.io.File;
import java.util.List;

@DisplayName("TlsContextConfiguration")
class TlsContextConfigurationTest {

    private static final Validator VALIDATOR = newValidator();

    @Nested
    class NoArgsConstructor {

        @Test
        void shouldHaveDefaultValues() {
            assertAllDefaultValues(new TlsContextConfiguration());
        }
    }

    @Nested
    class Builder {

        @Test
        void shouldHaveDefaultValues() {
            assertAllDefaultValues(TlsContextConfiguration.builder().build());
        }
    }

    private static void assertAllDefaultValues(TlsContextConfiguration config) {
        assertAll(
                () -> assertThat(config.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_2.value),
                () -> assertThat(config.getProvider()).isNull(),
                () -> assertThat(config.getKeyStorePath()).isNull(),
                () -> assertThat(config.getKeyStorePassword()).isNull(),
                () -> assertThat(config.getKeyStoreType()).isEqualTo(KeyStoreType.JKS.value),
                () -> assertThat(config.getKeyStoreProvider()).isNull(),
                () -> assertThat(config.getTrustStorePath()).isNull(),
                () -> assertThat(config.getTrustStorePassword()).isNull(),
                () -> assertThat(config.getTrustStoreType()).isEqualTo(KeyStoreType.JKS.value),
                () -> assertThat(config.getTrustStoreProvider()).isNull(),
                () -> assertThat(config.isTrustSelfSignedCertificates()).isFalse(),
                () -> assertThat(config.isVerifyHostname()).isTrue(),
                () -> assertThat(config.isDisableSniHostCheck()).isFalse(),
                () -> assertThat(config.getSupportedProtocols()).isNull(),
                () -> assertThat(config.getSupportedCiphers()).isNull(),
                () -> assertThat(config.getCertAlias()).isNull()
        );
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

            assertAll(
                    () -> assertThat(tlsConfig.getKeyStorePath()).isEqualTo("/path/to/keystore.jks"),
                    () -> assertThat(tlsConfig.getKeyStorePassword()).isEqualTo("ksPassWd"),
                    () -> assertThat(tlsConfig.getTrustStorePath()).isEqualTo("/path/to/truststore.jks"),
                    () -> assertThat(tlsConfig.getTrustStorePassword()).isEqualTo("tsPass100")
            );
        }

        @Test
        void shouldDeserializeMinimalWithNoKeyStorePropertiesConfig() {
            var tlsConfig = loadTlsContextConfiguration("TlsContextConfigurationTest/minimal-no-key-props-tls-config.yml");

            assertDefaultValues(tlsConfig);

            assertAll(
                    () -> assertThat(tlsConfig.getKeyStorePath()).isNull(),
                    () -> assertThat(tlsConfig.getKeyStorePassword()).isNull()
            );
        }

        @Test
        void shouldDeserializeFullConfig() {
            var tlsConfig = loadTlsContextConfiguration("TlsContextConfigurationTest/full-tls-config.yml");

            assertAll(
                    () -> assertThat(tlsConfig.getProtocol()).isEqualTo("TLSv1.3"),
                    () -> assertThat(tlsConfig.getKeyStorePath()).isEqualTo("/path/to/keystore.pkcs12"),
                    () -> assertThat(tlsConfig.getKeyStorePassword()).isEqualTo("ksPassWd"),
                    () -> assertThat(tlsConfig.getKeyStoreType()).isEqualTo("PKCS12"),
                    () -> assertThat(tlsConfig.getTrustStorePath()).isEqualTo("/path/to/truststore.pkcs12"),
                    () -> assertThat(tlsConfig.getTrustStorePassword()).isEqualTo("tsPass100"),
                    () -> assertThat(tlsConfig.getTrustStoreType()).isEqualTo("PKCS12"),
                    () -> assertThat(tlsConfig.isVerifyHostname()).isFalse(),
                    () -> assertThat(tlsConfig.isDisableSniHostCheck()).isTrue(),
                    () -> assertThat(tlsConfig.getSupportedProtocols()).containsOnly(
                            SSLContextProtocol.TLS_1_2.value,
                            SSLContextProtocol.TLS_1_3.value
                    )
            );
        }
    }

    private static void assertDefaultValues(TlsContextConfiguration config) {
        assertAll(
                () -> assertThat(config.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_2.value),
                () -> assertThat(config.getKeyStoreType()).isEqualTo(KeyStoreType.JKS.value),
                () -> assertThat(config.getTrustStoreType()).isEqualTo(KeyStoreType.JKS.value),
                () -> assertThat(config.isVerifyHostname()).isTrue(),
                () -> assertThat(config.isDisableSniHostCheck()).isFalse(),
                () -> assertThat(config.getSupportedProtocols()).isNull()
        );
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

        private TlsContextConfiguration tlsConfig;
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

            tlsConfig = TlsContextConfiguration.builder()
                    .protocol(protocol)
                    .provider("BC")
                    .keyStorePath(path)
                    .keyStorePassword(password)
                    .keyStoreType(type)
                    .keyStoreProvider("BC")
                    .trustStorePath(path)
                    .trustStorePassword(password)
                    .trustStoreType(type)
                    .trustStoreProvider("BC")
                    .trustSelfSignedCertificates(true)
                    .verifyHostname(false)
                    .disableSniHostCheck(true)
                    .supportedProtocols(List.of("TLSv1.3"))
                    .supportedCiphers(List.of("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"))
                    .certAlias("cert84")
                    .build();
        }

        @Nested
        class FromDropwizardTlsConfiguration {

            private TlsConfiguration dwTlsConfig;

            @BeforeEach
            void setUp() {
                dwTlsConfig = new TlsConfiguration();
                dwTlsConfig.setProtocol("TLSv1.3");
                dwTlsConfig.setProvider("BC");
                dwTlsConfig.setKeyStorePath(new File("/pki/test.ks"));
                dwTlsConfig.setKeyStorePassword("ks-pass");
                dwTlsConfig.setKeyStoreType("PKCS12");
                dwTlsConfig.setKeyStoreProvider("BC");
                dwTlsConfig.setTrustStorePath(new File("/pki/test.ts"));
                dwTlsConfig.setTrustStorePassword("ts-pass");
                dwTlsConfig.setTrustStoreType("PKCS12");
                dwTlsConfig.setTrustStoreProvider("BC");
                dwTlsConfig.setTrustSelfSignedCertificates(true);
                dwTlsConfig.setVerifyHostname(false);
                dwTlsConfig.setSupportedProtocols(List.of("TLSv1.3"));
                dwTlsConfig.setSupportedCiphers(List.of("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"));
                dwTlsConfig.setCertAlias("cert42");
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

                // The following also tests assumptions about defaults of Dropwizard's TlsConfiguration, for example,
                // which ones are null. This isn't wonderful, but at the same time we'll find out quickly what Dropwizard
                // has changed.
                assertAll(
                        () -> assertThat(tlsContextConfig.getProtocol()).isNotNull().isEqualTo(dwTlsConfig.getProtocol()),
                        () -> assertThat(tlsContextConfig.getKeyStorePath()).isNull(),
                        () -> assertThat(tlsContextConfig.getKeyStorePassword()).isNull(),
                        () -> assertThat(tlsContextConfig.getKeyStoreType()).isNotNull().isEqualTo(dwTlsConfig.getKeyStoreType()),
                        () -> assertThat(tlsContextConfig.getKeyStoreProvider()).isNull(),
                        () -> assertThat(tlsContextConfig.getTrustStorePath()).isNull(),
                        () -> assertThat(tlsContextConfig.getTrustStorePassword()).isNull(),
                        () -> assertThat(tlsContextConfig.getTrustStoreType()).isNotNull().isEqualTo(dwTlsConfig.getTrustStoreType()),
                        () -> assertThat(tlsContextConfig.getTrustStoreProvider()).isNull(),
                        () -> assertThat(tlsContextConfig.isTrustSelfSignedCertificates()).isFalse(),
                        () -> assertThat(tlsContextConfig.isVerifyHostname()).isTrue(),
                        () -> assertThat(tlsContextConfig.isDisableSniHostCheck()).isFalse(),
                        () -> assertThat(tlsContextConfig.getSupportedProtocols()).isNull(),
                        () -> assertThat(tlsContextConfig.getSupportedCiphers()).isNull(),
                        () -> assertThat(tlsContextConfig.getCertAlias()).isNull()
                );
            }

            @Test
            void shouldReturnTlsContextConfiguration() {
                var tlsContextConfig = TlsContextConfiguration.fromDropwizardTlsConfiguration(dwTlsConfig);

                assertAll(
                        () -> assertThat(tlsContextConfig.getProtocol()).isEqualTo("TLSv1.3"),
                        () -> assertThat(tlsContextConfig.getProvider()).isEqualTo("BC"),
                        () -> assertThat(tlsContextConfig.getKeyStorePath()).isEqualTo("/pki/test.ks"),
                        () -> assertThat(tlsContextConfig.getKeyStorePassword()).isEqualTo("ks-pass"),
                        () -> assertThat(tlsContextConfig.getKeyStoreType()).isEqualTo("PKCS12"),
                        () -> assertThat(tlsContextConfig.getKeyStoreProvider()).isEqualTo("BC"),
                        () -> assertThat(tlsContextConfig.getTrustStorePath()).isEqualTo("/pki/test.ts"),
                        () -> assertThat(tlsContextConfig.getTrustStorePassword()).isEqualTo("ts-pass"),
                        () -> assertThat(tlsContextConfig.getTrustStoreType()).isEqualTo("PKCS12"),
                        () -> assertThat(tlsContextConfig.getTrustStoreProvider()).isEqualTo("BC"),
                        () -> assertThat(tlsContextConfig.isTrustSelfSignedCertificates()).isTrue(),
                        () -> assertThat(tlsContextConfig.isVerifyHostname()).isFalse(),
                        () -> assertThat(tlsContextConfig.isDisableSniHostCheck()).isFalse(),
                        () -> assertThat(tlsContextConfig.getSupportedProtocols()).containsOnly("TLSv1.3"),
                        () -> assertThat(tlsContextConfig.getSupportedCiphers()).containsOnly("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"),
                        () -> assertThat(tlsContextConfig.getCertAlias()).isEqualTo("cert42")
                );
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
                var sslContext = tlsConfig.toSSLContext();

                assertThat(sslContext).isNotNull();
                assertThat(sslContext.getProtocol()).isEqualTo(tlsConfig.getProtocol());
            }
        }

        @Nested
        class ToSslSocketFactory {

            @Test
            void shouldReturnSslSocketFactory() {
                var sslSocketFactory = tlsConfig.toSslSocketFactory();
                assertThat(sslSocketFactory).isNotNull();
            }
        }

        @Nested
        class ToDropwizardTlsConfiguration {

            @SuppressWarnings("DataFlowIssue")  // b/c IntelliJ sees Dropwizard [key|trust]StorePath are @Nullable
            @Test
            void shouldReturnTlsConfiguration() {
                var dwTlsConfig = tlsConfig.toDropwizardTlsConfiguration();

                // Note: Dropwizard TlsConfiguration does not contain disableSniHostCheck, so we cannot check it
                
                assertAll(
                        () -> assertThat(dwTlsConfig.getProtocol()).isEqualTo(protocol),
                        () -> assertThat(dwTlsConfig.getProvider()).isEqualTo("BC"),
                        () -> assertThat(dwTlsConfig.getKeyStorePath().getAbsolutePath()).isEqualTo(path),
                        () -> assertThat(dwTlsConfig.getKeyStorePassword()).isEqualTo(password),
                        () -> assertThat(dwTlsConfig.getKeyStoreType()).isEqualTo(type),
                        () -> assertThat(dwTlsConfig.getKeyStoreProvider()).isEqualTo("BC"),
                        () -> assertThat(dwTlsConfig.getTrustStorePath().getAbsolutePath()).isEqualTo(path),
                        () -> assertThat(dwTlsConfig.getTrustStorePassword()).isEqualTo(password),
                        () -> assertThat(dwTlsConfig.getTrustStoreType()).isEqualTo(type),
                        () -> assertThat(dwTlsConfig.getTrustStoreProvider()).isEqualTo("BC"),
                        () -> assertThat(dwTlsConfig.isTrustSelfSignedCertificates()).isTrue(),
                        () -> assertThat(dwTlsConfig.isVerifyHostname()).isFalse(),
                        () -> assertThat(dwTlsConfig.getSupportedProtocols()).containsOnly("TLSv1.3"),
                        () -> assertThat(dwTlsConfig.getSupportedCiphers()).containsOnly("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"),
                        () -> assertThat(dwTlsConfig.getCertAlias()).isEqualTo("cert84")
                );
            }

            @SuppressWarnings("DataFlowIssue")  // b/c IntelliJ sees Dropwizard [key|trust]StorePath are @Nullable
            @Test
            void shouldCorrectlyHandleWhenOnlyTrustStoreIsPresent() {
                tlsConfig = TlsContextConfiguration.builder()
                        .trustStorePath("/data/pki/trust-store.pkcs12")
                        .trustStorePassword("mySuperSecretTsPassword")
                        .trustStoreType(KeyStoreType.PKCS12.value)
                        .build();

                var dwTlsConfig = tlsConfig.toDropwizardTlsConfiguration();

                assertAll(
                        () -> assertThat(dwTlsConfig.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_2.value),
                        () -> assertThat(dwTlsConfig.getKeyStorePath()).isNull(),
                        () -> assertThat(dwTlsConfig.getKeyStorePassword()).isNull(),
                        () -> assertThat(dwTlsConfig.getKeyStoreType()).isEqualTo(KeyStoreType.JKS.value),
                        () -> assertThat(dwTlsConfig.getTrustStorePath().getAbsolutePath()).isEqualTo("/data/pki/trust-store.pkcs12"),
                        () -> assertThat(dwTlsConfig.getTrustStorePassword()).isEqualTo("mySuperSecretTsPassword"),
                        () -> assertThat(dwTlsConfig.getTrustStoreType()).isEqualTo(KeyStoreType.PKCS12.value),
                        () -> assertThat(dwTlsConfig.isVerifyHostname()).isTrue()
                );
            }
        }

        @Nested
        class ToSSLContextConfiguration {

            @Test
            void shouldReturnSSLContextConfiguration() {
                var tlsContextConfig = TlsContextConfiguration.builder()
                        .protocol(SSLContextProtocol.TLS_1_3.value)
                        .verifyHostname(false)
                        .disableSniHostCheck(true)
                        .keyStorePath("/data/pki/key-store.pkcs12")
                        .keyStorePassword("myKsPassword")
                        .keyStoreType(KeyStoreType.PKCS12.value)
                        .trustStorePath("/data/pki/trust-store.pkcs12")
                        .trustStorePassword("myTsPassword")
                        .trustStoreType(KeyStoreType.PKCS12.value)
                        .supportedProtocols(List.of(SSLContextProtocol.TLS_1_2.value, SSLContextProtocol.TLS_1_3.value))
                        .build();

                var sslConfig = tlsContextConfig.toSslContextConfiguration();

                assertAll(
                        () -> assertThat(sslConfig.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_3.value),
                        () -> assertThat(sslConfig.isVerifyHostname()).isFalse(),
                        () -> assertThat(sslConfig.isDisableSniHostCheck()).isTrue(),
                        () -> assertThat(sslConfig.getKeyStorePath()).isEqualTo("/data/pki/key-store.pkcs12"),
                        () -> assertThat(sslConfig.getKeyStorePassword()).isEqualTo("myKsPassword"),
                        () -> assertThat(sslConfig.getKeyStoreType()).isEqualTo(KeyStoreType.PKCS12.value),
                        () -> assertThat(sslConfig.getTrustStorePath()).isEqualTo("/data/pki/trust-store.pkcs12"),
                        () -> assertThat(sslConfig.getTrustStorePassword()).isEqualTo("myTsPassword"),
                        () -> assertThat(sslConfig.getTrustStoreType()).isEqualTo(KeyStoreType.PKCS12.value)
                );
            }

            @Test
            void shouldSupportDifferentTrustStoreType() {
                var tlsContextConfig = TlsContextConfiguration.builder()
                        .protocol(SSLContextProtocol.TLS_1_3.value)
                        .verifyHostname(false)
                        .keyStorePath("/data/pki/key-store.jks")
                        .keyStorePassword("myKsPassword")
                        .keyStoreType(KeyStoreType.JKS.value)
                        .trustStorePath("/data/pki/trust-store.pkcs12")
                        .trustStorePassword("myTsPassword")
                        .trustStoreType(KeyStoreType.PKCS12.value)
                        .supportedProtocols(List.of(SSLContextProtocol.TLS_1_2.value, SSLContextProtocol.TLS_1_3.value))
                        .build();

                var sslConfig = tlsContextConfig.toSslContextConfiguration();

                assertThat(sslConfig.getTrustStoreType())
                        .describedAs("We should now support setting trust store type in SSLContextConfiguration")
                        .isEqualTo(KeyStoreType.PKCS12.value);
            }
        }
    }

    private static TlsContextConfiguration loadTlsContextConfiguration(String filename) {
        var appConfig = loadFromYaml(filename, SampleAppConfig.class);
        return appConfig.getTlsConfig();
    }
}
