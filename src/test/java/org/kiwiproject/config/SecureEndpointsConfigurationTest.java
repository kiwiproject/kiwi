package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.security.SecureTestConstants.JKS_FILE_PATH;
import static org.kiwiproject.security.SecureTestConstants.JKS_PASSWORD;
import static org.kiwiproject.security.SecureTestConstants.STORE_TYPE;
import static org.kiwiproject.security.SecureTestConstants.TLS_PROTOCOL;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SSLContextProtocol;
import org.kiwiproject.util.YamlTestHelper;

import java.util.List;
import java.util.stream.IntStream;

@DisplayName("SecureEndpointsConfiguration")
class SecureEndpointsConfigurationTest {

    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";
    private static final String EXAMPLE_DOMAIN = "example.org";
    private static final String PORT = "4567";
    private static final String PATH_PREFIX = "/path";

    @Nested
    class FromYaml {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.config.SecureEndpointsConfigurationTest#configs")
        void shouldDeserializeWithEndpoints(SecureEndpointsConfiguration secureEndpoints) {
            assertThat(secureEndpoints.getKeyStorePath()).isEqualTo("/path/to/keystore.pkcs12");
            assertThat(secureEndpoints.getKeyStorePassword()).isEqualTo("ksPassWd");
            assertThat(secureEndpoints.getKeyStoreType()).isEqualTo("PKCS12");
            assertThat(secureEndpoints.getTrustStorePath()).isEqualTo("/path/to/truststore.pkcs12");
            assertThat(secureEndpoints.getTrustStorePassword()).isEqualTo("tsPass100");
            assertThat(secureEndpoints.getTrustStoreType()).isEqualTo("PKCS12");
            assertThat(secureEndpoints.getProtocol()).isEqualTo("TLSv1.3");
            assertThat(secureEndpoints.isVerifyHostname()).isFalse();

            assertThat(secureEndpoints.getEndpoints())
                    .extracting("tag", "URI")
                    .containsExactly(
                            tuple("getUsers", "https://user.everythingstore.com:5678/users"),
                            tuple("getOrders", "https://order.everythingstore.com:7890/orders"),
                            tuple("paymentGatewayValidate", "https://pay.pal.com/pay-proxy/validate"),
                            tuple("paymentGatewayCharge", "https://pay.pal.com/pay-proxy/charge"));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.config.SecureEndpointsConfigurationTest#minimalConfigs")
        void shouldDeserializeWithMinimalConfigAndEndpoints(SecureEndpointsConfiguration secureEndpoints) {
            assertThat(secureEndpoints.getKeyStorePath()).isEqualTo("/path/to/keystore.jks");
            assertThat(secureEndpoints.getKeyStorePassword()).isEqualTo("ksPassWd");
            assertThat(secureEndpoints.getKeyStoreType()).isEqualTo("JKS");
            assertThat(secureEndpoints.getTrustStorePath()).isEqualTo("/path/to/truststore.jks");
            assertThat(secureEndpoints.getTrustStorePassword()).isEqualTo("tsPass100");
            assertThat(secureEndpoints.getTrustStoreType()).isEqualTo("JKS");
            assertThat(secureEndpoints.getProtocol()).isEqualTo(SSLContextProtocol.TLS_1_3.getValue());
            assertThat(secureEndpoints.isVerifyHostname()).isTrue();

            assertThat(secureEndpoints.getEndpoints())
                    .extracting("tag", "URI")
                    .containsExactly(
                            tuple("getUsers", "https://user.everythingstore.com:5678/users"),
                            tuple("getOrders", "https://order.everythingstore.com:7890/orders"),
                            tuple("paymentGatewayValidate", "https://pay.pal.com/pay-proxy/validate"),
                            tuple("paymentGatewayCharge", "https://pay.pal.com/pay-proxy/charge"));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.config.SecureEndpointsConfigurationTest#configsWithoutEndpoints")
        void shouldDeserializeWithNoEndpoints(SecureEndpointsConfiguration secureEndpoints) {
            assertThat(secureEndpoints.getEndpoints()).isEmpty();
        }
    }

    @SuppressWarnings("WeakerAccess")  // must be public for YAML to instantiate
    @Getter
    @Setter
    public static class SampleAppConfig {
        private SecureEndpointsConfiguration secureEndpoints;
    }

    static List<SecureEndpointsConfiguration> minimalConfigs() {
        var filename = "SecureEndpointsConfigurationTest/secure-endpoints-config-minimal.yml";
        return List.of(
                loadFromYamlSnakeYaml(filename),
                loadFromYamlDropwizard(filename),
                loadFromYamlJackson(filename)
        );
    }

    static List<SecureEndpointsConfiguration> configs() {
        var filename = "SecureEndpointsConfigurationTest/secure-endpoints-config.yml";
        return List.of(
                loadFromYamlSnakeYaml(filename),
                loadFromYamlDropwizard(filename),
                loadFromYamlJackson(filename)
        );
    }

    static List<SecureEndpointsConfiguration> configsWithoutEndpoints() {
        var filename = "SecureEndpointsConfigurationTest/secure-endpoints-config-no-endpoints.yml";
        return List.of(
                loadFromYamlSnakeYaml(filename),
                loadFromYamlDropwizard(filename),
                loadFromYamlJackson(filename)
        );
    }

    private static SecureEndpointsConfiguration loadFromYamlSnakeYaml(String filename) {
        var appConfig = YamlTestHelper.loadFromYamlWithSnakeYaml(filename, SampleAppConfig.class);
        return appConfig.getSecureEndpoints();
    }

    private static SecureEndpointsConfiguration loadFromYamlDropwizard(String filename) {
        var appConfig = YamlTestHelper.loadFromYamlWithDropwizard(filename, SampleAppConfig.class);
        return appConfig.getSecureEndpoints();
    }

    private static SecureEndpointsConfiguration loadFromYamlJackson(String filename) {
        var appConfig = YamlTestHelper.loadFromYamlWithJackson(filename, SampleAppConfig.class);
        return appConfig.getSecureEndpoints();
    }

    @Nested
    class Builder {

        @Test
        void shouldAddEndpoints() {
            var secureEndpointsConfig = SecureEndpointsConfiguration.builder()
                    .keyStorePath(JKS_FILE_PATH)
                    .keyStorePassword(JKS_PASSWORD)
                    .trustStorePath(JKS_FILE_PATH)
                    .trustStorePassword(JKS_PASSWORD)
                    .protocol(TLS_PROTOCOL)
                    .verifyHostname(false)
                    .addEndpoint()
                    .tag("cool-endpoint")
                    .scheme(HTTPS_SCHEME)
                    .domain("foo.com")
                    .path("path1")
                    .port("8999")
                    .buildEndpoint()
                    .addEndpoint()
                    .tag("awesome-endpoint")
                    .scheme(HTTPS_SCHEME)
                    .domain("bar.com")
                    .path("path2")
                    .port("6666")
                    .buildEndpoint()
                    .build();

            assertThat(secureEndpointsConfig.getProtocol()).isEqualTo(TLS_PROTOCOL);
            assertThat(secureEndpointsConfig.isVerifyHostname()).isFalse();
            assertThat(secureEndpointsConfig.getKeyStorePath()).isEqualTo(JKS_FILE_PATH);
            assertThat(secureEndpointsConfig.getKeyStorePassword()).isEqualTo(JKS_PASSWORD);
            assertThat(secureEndpointsConfig.getKeyStoreType())
                    .describedAs("should use default from SSLContextConfiguration")
                    .isEqualTo(STORE_TYPE);
            assertThat(secureEndpointsConfig.getTrustStorePath()).isEqualTo(JKS_FILE_PATH);
            assertThat(secureEndpointsConfig.getTrustStorePassword()).isEqualTo(JKS_PASSWORD);
            assertThat(secureEndpointsConfig.getTrustStoreType())
                    .describedAs("should use default from SSLContextConfiguration")
                    .isEqualTo(STORE_TYPE);

            assertThat(secureEndpointsConfig.getEndpoints())
                    .extracting("domain")
                    .containsExactly("foo.com", "bar.com");

            var coolEndpoint = secureEndpointsConfig.getEndpointByTag("cool-endpoint");
            assertThat(coolEndpoint.getURI()).isEqualTo("https://foo.com:8999/path1");

            var awesomeEndpoint = secureEndpointsConfig.getEndpointByTag("awesome-endpoint");
            assertThat(awesomeEndpoint.getURI()).isEqualTo("https://bar.com:6666/path2");

            assertThat(secureEndpointsConfig.anyEndpointSecure()).isTrue();
            assertThat(secureEndpointsConfig.allEndpointsSecure()).isTrue();
        }

        @Test
        void shouldAddEndpointsWithCustomKeyAndTrustStoreType() {
            var secureEndpointsConfig = SecureEndpointsConfiguration.builder()
                    .keyStorePath("/data/certs/ks.pkcs12")
                    .keyStorePassword("ksPass123")
                    .keyStoreType(KeyStoreType.PKCS12.value)
                    .trustStorePath("/data/certs/ts.pkcs12")
                    .trustStorePassword("tsPass456")
                    .trustStoreType(KeyStoreType.PKCS12.value)
                    .protocol(TLS_PROTOCOL)
                    .verifyHostname(true)
                    .addEndpoint()
                    .tag("endpoint-1")
                    .scheme(HTTPS_SCHEME)
                    .domain("acme.com")
                    .path("path1")
                    .port("8999")
                    .buildEndpoint()
                    .build();

            assertThat(secureEndpointsConfig.getProtocol()).isEqualTo(TLS_PROTOCOL);
            assertThat(secureEndpointsConfig.isVerifyHostname()).isTrue();
            assertThat(secureEndpointsConfig.getKeyStorePath()).isEqualTo("/data/certs/ks.pkcs12");
            assertThat(secureEndpointsConfig.getKeyStorePassword()).isEqualTo("ksPass123");
            assertThat(secureEndpointsConfig.getKeyStoreType()).isEqualTo(KeyStoreType.PKCS12.value);
            assertThat(secureEndpointsConfig.getTrustStorePath()).isEqualTo("/data/certs/ts.pkcs12");
            assertThat(secureEndpointsConfig.getTrustStorePassword()).isEqualTo("tsPass456");
            assertThat(secureEndpointsConfig.getTrustStoreType()).isEqualTo(KeyStoreType.PKCS12.value);

            assertThat(secureEndpointsConfig.getEndpoints())
                    .extracting("domain")
                    .containsExactly("acme.com");

            var coolEndpoint = secureEndpointsConfig.getEndpointByTag("endpoint-1");
            assertThat(coolEndpoint.getURI()).isEqualTo("https://acme.com:8999/path1");

            assertThat(secureEndpointsConfig.anyEndpointSecure()).isTrue();
            assertThat(secureEndpointsConfig.allEndpointsSecure()).isTrue();
        }
    }

    @Nested
    class GetEndpoint {

        private SecureEndpointsConfiguration secureEndpointsConfig;

        @BeforeEach
        void setUp() {
            secureEndpointsConfig = newSecureEndpointsConfigurationWithEndpoints();
        }

        @Nested
        class ByTag {

            @Test
            void shouldReturnEndpoint_WhenFound() {
                newTagStream().forEach(tag -> {
                    var endpoint = secureEndpointsConfig.getEndpointByTag(String.valueOf(tag));
                    assertEndpointForTag(tag, endpoint);
                });
            }

            @Test
            void shouldThrowException_WhenNotFound() {
                assertThatThrownBy(() -> secureEndpointsConfig.getEndpointByTag("foo"))
                        .isExactlyInstanceOf(IllegalStateException.class)
                        .hasMessage("No endpoint by tag with value [foo] was found");
            }
        }

        @Nested
        class ByTagOrEmpty {

            @Test
            void shouldReturnEndpoint_WhenFound() {
                newTagStream().forEach(tag -> {
                    var endpoint = secureEndpointsConfig.getEndpointByTagOrEmpty(String.valueOf(tag)).orElseThrow();
                    assertEndpointForTag(tag, endpoint);
                });
            }

            @Test
            void shouldThrowException_WhenNotFound() {
                assertThat(secureEndpointsConfig.getEndpointByTagOrEmpty("foo")).isEmpty();
            }
        }

        @Nested
        class ByPathEnding {

            @Test
            void shouldReturnEndpoint_WhenFound() {
                newTagStream().forEach(tag -> {
                    var endpoint = secureEndpointsConfig.getEndpointByPathEnding(PATH_PREFIX + tag);
                    assertEndpointForTag(tag, endpoint);
                });
            }

            @Test
            void shouldThrowException_WhenNotFound() {
                assertThatThrownBy(() -> secureEndpointsConfig.getEndpointByPathEnding("bar"))
                        .isExactlyInstanceOf(IllegalStateException.class)
                        .hasMessage("No endpoint by pathEnding with value [bar] was found");
            }
        }

        @Nested
        class ByPathEndingOrEmpty {

            @Test
            void shouldReturnEndpoint_WhenFound() {
                newTagStream().forEach(tag -> {
                    var endpoint = secureEndpointsConfig.getEndpointByPathEndingOrEmpty(PATH_PREFIX + tag).orElseThrow();
                    assertEndpointForTag(tag, endpoint);
                });
            }

            @Test
            void shouldThrowException_WhenNotFound() {
                assertThat(secureEndpointsConfig.getEndpointByPathEndingOrEmpty("bar")).isEmpty();
            }
        }

        private void assertEndpointForTag(int tag, EndpointConfiguration endpoint) {
            assertThat(endpoint.getTag()).isEqualTo(String.valueOf(tag));
            assertThat(endpoint.getScheme()).isEqualTo(HTTP_SCHEME);
            assertThat(endpoint.getDomain()).isEqualTo(EXAMPLE_DOMAIN);
            assertThat(endpoint.getPort()).isEqualTo(PORT);
            assertThat(endpoint.getPath()).isEqualTo(PATH_PREFIX + tag);
        }
    }

    @Nested
    class AnyEndpointSecure {

        @Test
        void shouldBeFalse_WhenNoEndpointsAreSecure() {
            var secureEndpointsConfig = newSecureEndpointsConfigurationWithEndpoints();

            assertThat(secureEndpointsConfig.anyEndpointSecure()).isFalse();
            assertThat(secureEndpointsConfig.isSecure()).isFalse();
        }

        @Test
        void shouldBeTrue_WhenOneEndpointIsSecure() {
            var secureEndpointsConfig = newSecureEndpointsConfigurationWithNoEndpoints();

            var someHttpEndpoints = newTagStream(1, 5)
                    .mapToObj(SecureEndpointsConfigurationTest.this::newHttpEndpointConfiguration)
                    .toList();
            secureEndpointsConfig.getEndpoints().addAll(someHttpEndpoints);

            secureEndpointsConfig.getEndpoints().add(newHttpsEndpointConfiguration(6));

            var moreHttpEndpoints = newTagStream(7, 10)
                    .mapToObj(SecureEndpointsConfigurationTest.this::newHttpEndpointConfiguration)
                    .toList();
            secureEndpointsConfig.getEndpoints().addAll(moreHttpEndpoints);

            assertThat(secureEndpointsConfig.anyEndpointSecure()).isTrue();
            assertThat(secureEndpointsConfig.isSecure()).isTrue();
        }
    }

    @Nested
    class AllEndpointsSecure {

        @Test
        void shouldBeFalse_WhenOneEndpointsIsNotSecure() {
            var secureEndpointsConfig = newSecureEndpointsConfigurationWithNoEndpoints();

            var someHttpsEndpoints = newTagStream(1, 5)
                    .mapToObj(SecureEndpointsConfigurationTest.this::newHttpsEndpointConfiguration)
                    .toList();
            secureEndpointsConfig.getEndpoints().addAll(someHttpsEndpoints);

            secureEndpointsConfig.getEndpoints().add(newHttpEndpointConfiguration(6));

            assertThat(secureEndpointsConfig.allEndpointsSecure()).isFalse();
        }

        @Test
        void shouldBeTrue_WhenAllEndpointsAreSecure() {
            var secureEndpointsConfig = newSecureEndpointsConfigurationWithNoEndpoints();

            var someHttpsEndpoints = newTagStream(1, 8)
                    .mapToObj(SecureEndpointsConfigurationTest.this::newHttpsEndpointConfiguration)
                    .toList();
            secureEndpointsConfig.getEndpoints().addAll(someHttpsEndpoints);

            assertThat(secureEndpointsConfig.allEndpointsSecure()).isTrue();
        }
    }

    private SecureEndpointsConfiguration newSecureEndpointsConfigurationWithEndpoints() {
        return newSecureEndpointsConfiguration(newTagStream());
    }

    private SecureEndpointsConfiguration newSecureEndpointsConfiguration(IntStream tagStream) {
        var endpointConfigs = tagStream.mapToObj(this::newHttpEndpointConfiguration).toList();

        var config = newSecureEndpointsConfigurationWithNoEndpoints();
        config.setEndpoints(endpointConfigs);

        return config;
    }

    private SecureEndpointsConfiguration newSecureEndpointsConfigurationWithNoEndpoints() {
        return SecureEndpointsConfiguration.builder()
                .keyStorePath(JKS_FILE_PATH)
                .keyStorePassword(JKS_PASSWORD)
                .keyStoreType(STORE_TYPE)
                .setTrustStorePath(JKS_FILE_PATH)
                .trustStorePassword(JKS_PASSWORD)
                .protocol(TLS_PROTOCOL)
                .build();
    }

    private IntStream newTagStream() {
        return newTagStream(1, 10);
    }

    private IntStream newTagStream(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive);
    }

    private EndpointConfiguration newHttpEndpointConfiguration(int tag) {
        return newEndpointConfiguration(tag, HTTP_SCHEME);
    }

    private EndpointConfiguration newHttpsEndpointConfiguration(int tag) {
        return newEndpointConfiguration(tag, HTTPS_SCHEME);
    }

    private EndpointConfiguration newEndpointConfiguration(int tag, String scheme) {
        return EndpointConfiguration.builder()
                .tag(String.valueOf(tag))
                .scheme(scheme)
                .domain(EXAMPLE_DOMAIN)
                .port(PORT)
                .path(PATH_PREFIX + tag)
                .build();
    }
}
