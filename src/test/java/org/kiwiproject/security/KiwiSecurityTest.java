package org.kiwiproject.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.kiwiproject.util.BlankStringSource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Locale;

@DisplayName("KiwiSecurity")
class KiwiSecurityTest {

    private String path;
    private String password;

    @BeforeEach
    void setUp() {
        path = SecureTestConstants.JKS_FILE_PATH;
        password = SecureTestConstants.JKS_PASSWORD;
    }

    @Nested
    class KeyStoreTypeEnum {

        @ParameterizedTest
        @EnumSource(KeyStoreType.class)
        void shouldHaveValueEqualToEnumName(KeyStoreType type) {
            var expected = type.name();

            assertThat(type.value).isEqualTo(expected);
            assertThat(type.getValue()).isEqualTo(expected);
        }
    }

    @Nested
    class SSLContextProtocolEnum {

        @ParameterizedTest
        @EnumSource(SSLContextProtocol.class)
        void shouldHaveEqualValueFieldAndGetter(SSLContextProtocol protocol) {
            assertThat(protocol.value).isEqualTo(protocol.getValue());
        }
    }

    @Nested
    class CreateSslContext {

        private String type;
        private SSLContextProtocol protocol;

        @BeforeEach
        void setUp() {
            type = KeyStoreType.JKS.value;
            protocol = SSLContextProtocol.TLS;
        }

        @Nested
        class ShouldCreateSslContext {

            @Test
            void whenAssumingJksType() {
                var sslContext = KiwiSecurity.createSslContext(path, password, path, password, protocol);
                assertThat(sslContext).isNotNull();
            }

            @Test
            void whenUsingSSLContextProtocolEnum() {
                var sslContext = KiwiSecurity.createSslContext(path, password, type, path, password, type, protocol);
                assertThat(sslContext).isNotNull();
            }

            @Test
            void whenUsingStringProtocol() {
                var sslContext = KiwiSecurity.createSslContext(
                        path, password, type, path, password, type, protocol.value);
                assertThat(sslContext).isNotNull();
            }

            @Test
            void whenUsingLowerCasedStringProtocol() {
                var lowerCaseProtocol = protocol.value.toLowerCase(Locale.ENGLISH);
                var sslContext = KiwiSecurity.createSslContext(
                        path, password, type, path, password, type, lowerCaseProtocol);
                assertThat(sslContext).isNotNull();
            }

            @Test
            void whenUsingLowerCasedKeystoreAndTruststoreType() {
                var lowerCaseType = type.toLowerCase(Locale.ENGLISH);
                var sslContext = KiwiSecurity.createSslContext(
                        path, password, lowerCaseType, path, password, lowerCaseType, protocol.value);
                assertThat(sslContext).isNotNull();
            }

            @Test
            void whenAllKeyStoreArgumentsAreNull() {
                assertThat(KiwiSecurity.createSslContext(
                        null, null, null, null,
                        path, password, "JKS", TrustManagerFactory.getDefaultAlgorithm(), "TLSv1.2"))
                        .isNotNull();
            }
        }

        @Nested
        class ShouldThrowException {

            private String storeType;
            private String keyManagerAlgorithm;
            private String trustManagerAlgorithm;
            private String protocol;

            @BeforeEach
            void setUp() {
                storeType = KeyStoreType.JKS.value;
                keyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
                trustManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
                protocol = SSLContextProtocol.TLS_1_2.value;
            }

            @Nested
            class WhenKeyStorePathIsPresent {

                @Test
                void andKeyStorePasswordIsNull() {
                    assertThatIllegalArgumentException()
                            .isThrownBy(() ->
                                    KiwiSecurity.createSslContext(
                                            path, null, storeType, keyManagerAlgorithm,
                                            path, password, storeType, trustManagerAlgorithm,
                                            protocol))
                            .withMessage("keyStorePassword cannot be null");
                }

                @ParameterizedTest
                @BlankStringSource
                void andKeyStoreTypeIsBlank(String blankKeyStoreType) {
                    assertThatIllegalArgumentException()
                            .isThrownBy(() ->
                                    KiwiSecurity.createSslContext(
                                            path, password, blankKeyStoreType, keyManagerAlgorithm,
                                            path, password, storeType, trustManagerAlgorithm,
                                            protocol))
                            .withMessage("keyStoreType cannot be blank");
                }

                @ParameterizedTest
                @BlankStringSource
                void andKeyManagerAlgorithmIsBlank(String blankKeyManagerAlgorithm) {
                    assertThatIllegalArgumentException()
                            .isThrownBy(() ->
                                    KiwiSecurity.createSslContext(
                                            path, password, storeType, blankKeyManagerAlgorithm,
                                            path, password, storeType, trustManagerAlgorithm,
                                            protocol
                                    ))
                            .withMessage("keyManagerAlgorithm cannot be blank");
                }
            }

            @ParameterizedTest
            @BlankStringSource
            void whenBlankTrustStorePath(String blankTrustStorePath) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiSecurity.createSslContext(
                                        null, null, storeType, keyManagerAlgorithm,
                                        blankTrustStorePath, password, storeType, trustManagerAlgorithm,
                                        path
                                ))
                        .withMessage("trustStorePath cannot be blank");
            }

            @Test
            void whenNullTrustStorePassword() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiSecurity.createSslContext(
                                        null, null, storeType, keyManagerAlgorithm,
                                        path, null, storeType, trustManagerAlgorithm,
                                        protocol
                                ))
                        .withMessage("trustStorePassword cannot be null");
            }

            @ParameterizedTest
            @BlankStringSource
            void whenBlankTrustStoreType(String blankTrustStoreType) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiSecurity.createSslContext(
                                        null, null, storeType, keyManagerAlgorithm,
                                        path, password, blankTrustStoreType, trustManagerAlgorithm,
                                        protocol))
                        .withMessage("trustStoreType cannot be blank");
            }

            @ParameterizedTest
            @BlankStringSource
            void whenBlankTrustManagerAlgorithm(String blankTrustManagerAlgorithm) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiSecurity.createSslContext(
                                        null, null, storeType, keyManagerAlgorithm,
                                        path, password, storeType, blankTrustManagerAlgorithm,
                                        protocol))
                        .withMessage("trustManagerAlgorithm cannot be blank");
            }

            @ParameterizedTest
            @BlankStringSource
            void whenBlankProtocol(String blankProtocol) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiSecurity.createSslContext(
                                        null, null, storeType, keyManagerAlgorithm,
                                        path, password, storeType, trustManagerAlgorithm,
                                        blankProtocol))
                        .withMessage("protocol cannot be blank");
            }

            @Test
            void whenInvalidKeyStorePath() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                "/not/valid/keystore.jks", password, path, password, protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(FileNotFoundException.class);
            }

            @Test
            void whenInvalidKeyStoreCredential() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, "invalid-keystore-password", path, password, protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(IOException.class);
            }

            @Test
            void whenInvalidTrustStorePath() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, password, "/not/valid/truststore.jks", password, protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(FileNotFoundException.class);
            }

            @Test
            void whenInvalidTrustStoreCredential() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, password, path, "invalid-truststore-password", protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(IOException.class);
            }

            @Test
            void whenInvalidProtocol() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, password, path, password, "PIEv3.14159"
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(NoSuchAlgorithmException.class);
            }

            @Test
            void whenInvalidKeyStoreType() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, password, "InvalidKeyStoreType", keyManagerAlgorithm,
                                path, password, storeType, trustManagerAlgorithm,
                                protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(KeyStoreException.class);
            }

            @Test
            void whenInvalidTrustStoreType() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, password, storeType, keyManagerAlgorithm,
                                path, password, "InvalidTrustStoreType", trustManagerAlgorithm,
                                protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(KeyStoreException.class);
            }

            @Test
            void whenInvalidKeyManagerAlgorithm() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, password, storeType, "TotallyInvalidAlgorithm",
                                path, password, storeType, trustManagerAlgorithm,
                                protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(NoSuchAlgorithmException.class);
            }

            @Test
            void whenInvalidTrustManagerAlgorithm() {
                assertThatThrownBy(() ->
                        KiwiSecurity.createSslContext(
                                path, password, storeType, keyManagerAlgorithm,
                                path, password, storeType, "TotallyInvalidAlgorithm",
                                protocol
                        ))
                        .hasMessage("Error creating SSLContext")
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasCauseExactlyInstanceOf(NoSuchAlgorithmException.class);
            }
        }
    }

    @Nested
    class GetKeyStore {

        @Nested
        class ShouldReturnKeyStoreOptional {

            @Test
            void whenValidStringTypeAndPathAndCredential() {
                assertThat(KiwiSecurity.getKeyStore("JKS", path, password)).isPresent();
            }

            @Test
            void whenValidKeyStoreAndTypeAndCredential() {
                assertThat(KiwiSecurity.getKeyStore(KeyStoreType.JKS, path, password)).isPresent();
            }

            @ParameterizedTest
            @CsvSource(value = {
                "null, null",
                "/certs/my.jks, null",
                "null, the_password!"
            }, nullValues = "null")
            void whenGivenNullPathOrPassword(String path, String password) {
                assertThat(KiwiSecurity.getKeyStore(KeyStoreType.JKS, path, password)).isEmpty();
            }
        }

        @Nested
        class ShouldThrowException {

            @Test
            void whenInvalidType() {
                assertThatThrownBy(() ->
                        KiwiSecurity.getKeyStore("WhatTypeIsThis", path, password))
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasMessage("Error getting key store")
                        .hasCauseExactlyInstanceOf(KeyStoreException.class);
            }

            @Test
            void whenInvalidPath() {
                assertThatThrownBy(() ->
                        KiwiSecurity.getKeyStore(KeyStoreType.JKS, "/not/valid/keystore.jks", password))
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasMessage("Error getting key store")
                        .hasCauseExactlyInstanceOf(FileNotFoundException.class);
            }

            /**
             * @implNote Per Javadocs on {@link java.security.KeyStore#load(InputStream, char[])} the root cause of
             * an incorrect password should be {@link UnrecoverableKeyException}
             */
            @Test
            void whenInvalidCredential() {
                assertThatThrownBy(() ->
                        KiwiSecurity.getKeyStore("JKS", path, "invalid-keystore-password"))
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasMessage("Error getting key store")
                        .hasCauseExactlyInstanceOf(IOException.class)
                        .hasRootCauseExactlyInstanceOf(UnrecoverableKeyException.class);
            }
        }
    }

    @Nested
    class GetKeyManagers {

        private KeyStore keyStore;

        @BeforeEach
        void setUp() {
            keyStore = KiwiSecurity.getKeyStore(KeyStoreType.JKS, path, password).orElseThrow(IllegalStateException::new);
        }

        @Nested
        class ShouldReturnKeyManagers {

            @Test
            void whenValidKeyStoreAndCredentialAndDefaultAlgorithm() {
                assertThat(KiwiSecurity.getKeyManagers(keyStore, password)).hasSize(1);
            }

            @Test
            void whenValidKeyStoreAndCredentialAndExplicitAlgorithm() {
                assertThat(KiwiSecurity.getKeyManagers(keyStore, password, "pkix")).hasSize(1);
            }
        }

        @Nested
        class ShouldThrowException {

            @Test
            void whenInvalidCredential() {
                assertThatThrownBy(() ->
                        KiwiSecurity.getKeyManagers(keyStore, "invalid-keystore-password"))
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasMessage("Error getting key managers")
                        .hasCauseExactlyInstanceOf(UnrecoverableKeyException.class);
            }

            @Test
            void whenInvalidAlgorithm() {
                assertThatThrownBy(() ->
                        KiwiSecurity.getKeyManagers(keyStore, password, "CrazyAlgorithm_v42"))
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasMessage("Error getting key managers")
                        .hasCauseExactlyInstanceOf(NoSuchAlgorithmException.class);
            }
        }
    }

    @Nested
    class GetTrustManagers {

        private KeyStore keyStore;

        @BeforeEach
        void setUp() {
            keyStore = KiwiSecurity.getKeyStore(KeyStoreType.JKS, path, password).orElseThrow(IllegalStateException::new);
        }

        @Nested
        class ShouldReturnTrustManagers {

            @Test
            void whenValidKeyStoreAndCredentialAndDefaultAlgorithm() {
                assertThat(KiwiSecurity.getTrustManagers(keyStore)).hasSize(1);
            }

            @Test
            void whenValidKeyStoreAndCredentialAndExplicitAlgorithm() {
                assertThat(KiwiSecurity.getTrustManagers(keyStore, "pkix")).hasSize(1);
            }
        }

        @Nested
        class ShouldThrowException {

            @Test
            void whenInvalidAlgorithm() {
                assertThatThrownBy(() ->
                        KiwiSecurity.getTrustManagers(keyStore, "WeirdAlg_v42.001"))
                        .isExactlyInstanceOf(SSLContextException.class)
                        .hasMessage("Error getting trust managers")
                        .hasCauseExactlyInstanceOf(NoSuchAlgorithmException.class);
            }
        }
    }
}
