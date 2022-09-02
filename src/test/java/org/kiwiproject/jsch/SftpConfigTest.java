package org.kiwiproject.jsch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.validation.ValidationTestHelper.DEFAULT_VALIDATOR;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.validation.ValidationTestHelper;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

@DisplayName("SftpConfig")
class SftpConfigTest {

    private static final ObjectMapper DW_OBJECT_MAPPER = Jackson.newObjectMapper();

    @Nested
    class DefaultValues {

        private static final String SFTP_CONFIG_FACTORY = "org.kiwiproject.jsch.SftpConfigTest#defaultSftpConfigs";

        @ParameterizedTest
        @MethodSource(SFTP_CONFIG_FACTORY)
        void shouldHaveDefaultPort(SftpConfig config) {
            assertThat(config.getPort()).isEqualTo(22);
        }

        @ParameterizedTest
        @MethodSource(SFTP_CONFIG_FACTORY)
        void shouldHaveDefaultPreferredAuthentications(SftpConfig config) {
            assertThat(config.getPreferredAuthentications()).isEqualTo("publickey,password");
        }

        @ParameterizedTest
        @MethodSource(SFTP_CONFIG_FACTORY)
        void shouldHaveDefaultTimeout(SftpConfig config) {
            assertThat(config.getTimeout()).isEqualTo(Duration.seconds(5));
        }

        @ParameterizedTest
        @MethodSource(SFTP_CONFIG_FACTORY)
        void shouldHaveNullDefaults(SftpConfig config) {
            assertThat(config.getHost()).isNull();
            assertThat(config.getUser()).isNull();
            assertThat(config.getPassword()).isNull();
            assertThat(config.getPrivateKeyFilePath()).isNull();
            assertThat(config.getRemoteBasePath()).isNull();
            assertThat(config.getErrorPath()).isNull();
            assertThat(config.getKnownHostsFile()).isNull();
        }
    }

    /**
     * Factory for {@link SftpConfig} instances using the default constructor, all-args constructor, and builder.
     */
    @SuppressWarnings("unused") // IntelliJ says this is unused when referenced via a constant. It is wrong.
    static Stream<SftpConfig> defaultSftpConfigs() {
        return Stream.of(
                new SftpConfig(),
                newConfigUsingAllArgsConstructor(),
                SftpConfig.builder().build()
        );
    }

    private static SftpConfig newConfigUsingAllArgsConstructor() {
        return new SftpConfig(0, null, null, null, null, null, null, null, null, false, null);
    }

    @Nested
    class Deserializing {

        @Nested
        class FromYaml {

            private static final String DW_PROPERTY_PREFIX = "dw";

            @Test
            void shouldAllowOverridingDefaultValues() {
                var sftpConfig = deserializeAndExtractSftpConfig("config-all-properties.yml");
                assertExplicitlySpecifiedProperties(sftpConfig);
                assertDefaultValuesOverridden(sftpConfig);
            }

            @Test
            void shouldRespectDefaultValues() {
                var sftpConfig = deserializeAndExtractSftpConfig("config-minimal-properties.yml");
                assertExplicitlySpecifiedProperties(sftpConfig);
                assertDefaultValuesUsed(sftpConfig);
            }

            private SftpConfig deserializeAndExtractSftpConfig(String configFileName) {
                var factory = new YamlConfigurationFactory<>(
                        AppConfiguration.class,
                        DEFAULT_VALIDATOR,
                        DW_OBJECT_MAPPER,
                        DW_PROPERTY_PREFIX
                );
                var path = Paths.get("SftpConfigTest", configFileName).toString();

                try {
                    var config = factory.build(new ResourceConfigurationSourceProvider(), path);
                    return config.getSftpConfig();
                } catch (Exception e) {
                    throw new AssertionFailedError("Error de-serializing config at path: " + path, e);
                }
            }
        }

        @Nested
        class FromJson {

            @Test
            void shouldAllowOverridingDefaultValues() throws IOException {
                var json = Fixtures.fixture("SftpConfigTest/config-all-properties.json");
                var sftpConfig = DW_OBJECT_MAPPER.readValue(json, SftpConfig.class);
                assertExplicitlySpecifiedProperties(sftpConfig);
                assertDefaultValuesOverridden(sftpConfig);
            }

            @Test
            void shouldRespectDefaultValues() throws IOException {
                var json = Fixtures.fixture("SftpConfigTest/config-minimal-properties.json");
                var sftpConfig = DW_OBJECT_MAPPER.readValue(json, SftpConfig.class);
                assertExplicitlySpecifiedProperties(sftpConfig);
                assertDefaultValuesUsed(sftpConfig);
            }
        }

        private void assertExplicitlySpecifiedProperties(SftpConfig sftpConfig) {
            assertThat(sftpConfig.getHost()).isEqualTo("localhost");
            assertThat(sftpConfig.getUser()).isEqualTo("bob");
            assertThat(sftpConfig.getPrivateKeyFilePath()).isEqualTo("/home/bob/.ssh/id_rsa");
            assertThat(sftpConfig.getKnownHostsFile()).isEqualTo("/home/bob/.ssh/known_hosts");
            assertThat(sftpConfig.getRemoteBasePath()).isEqualTo("/data_shared/development/my-service/remote-base-path");
            assertThat(sftpConfig.getErrorPath()).isEqualTo("/data_shared/development/my-service/transfer-errors");
        }

        private void assertDefaultValuesOverridden(SftpConfig sftpConfig) {
            assertThat(sftpConfig.getPort()).isEqualTo(42);
            assertThat(sftpConfig.getPreferredAuthentications()).isEqualTo("publickey");
            assertThat(sftpConfig.isDisableStrictHostChecking()).isTrue();
            assertThat(sftpConfig.getTimeout()).isEqualTo(Duration.seconds(10));
        }

        private void assertDefaultValuesUsed(SftpConfig sftpConfig) {
            assertThat(sftpConfig.getPort()).isEqualTo(22);
            assertThat(sftpConfig.getPassword()).isNull();
            assertThat(sftpConfig.getPreferredAuthentications()).isEqualTo("publickey,password");
            assertThat(sftpConfig.isDisableStrictHostChecking()).isFalse();
            assertThat(sftpConfig.getTimeout()).isEqualTo(Duration.seconds(5));
        }
    }

    @Nested
    class Validation {

        private static final String BLANK_STRINGS_FACTORY = "org.kiwiproject.jsch.SftpConfigTest#someBlankStrings";

        private SftpConfig config;

        @BeforeEach
        void setUp() {
            config = new SftpConfig();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 65_536})
        void shouldRequireValidPort(int port) {
            config.setPort(port);
            assertOnePropertyViolation(config, "port");
        }

        @ParameterizedTest
        @MethodSource(BLANK_STRINGS_FACTORY)
        void shouldRequireHost(String value) {
            config.setHost(value);
            assertOnePropertyViolation(config, "host");
        }

        @ParameterizedTest
        @MethodSource(BLANK_STRINGS_FACTORY)
        void shouldRequirePreferredAuthentications(String value) {
            config.setPreferredAuthentications(value);
            assertOnePropertyViolation(config, "preferredAuthentications");
        }

        @Test
        void shouldNotRequirePassword() {
            assertNoPropertyViolations(config, "password");
        }

        @Test
        void shouldNotRequirePrivateKeyFilePath() {
            assertNoPropertyViolations(config, "privateKeyFilePath");
        }

        @ParameterizedTest
        @MethodSource(BLANK_STRINGS_FACTORY)
        void shouldNotRequireRemoteBasePath(String value) {
            config.setRemoteBasePath(value);
            assertNoPropertyViolations(config, "remoteBasePath");
        }

        @ParameterizedTest
        @MethodSource(BLANK_STRINGS_FACTORY)
        void shouldRequireErrorPath(String value) {
            config.setErrorPath(value);
            assertOnePropertyViolation(config, "errorPath");
        }

        @ParameterizedTest
        @MethodSource(BLANK_STRINGS_FACTORY)
        void shouldRequireKnownHostsFile(String value) {
            config.setKnownHostsFile(value);
            assertOnePropertyViolation(config, "knownHostsFile");
        }

        @Test
        void shouldRequireTimeout() {
            config.setTimeout(null);
            assertOnePropertyViolation(config, "timeout");
        }

        @ParameterizedTest
        @CsvSource({
                "0, 1",
                "1, 1",
                "48, 1",
                "49, 1",
                "50, 0",
                "500, 0",
                "5000, 0",
                "50000, 0",
        })
        void shouldRequireMinimumTimeout(long millis, int numExpectedViolations) {
            config.setTimeout(Duration.milliseconds(millis));
            assertPropertyViolations(DEFAULT_VALIDATOR, config, "timeout", numExpectedViolations);
        }
    }

    private static Stream<String> someBlankStrings() {
        return Stream.of(
                // Basics
                null,
                "",
                " ",
                "    ",

                // ASCII characters
                "\t", // HORIZONTAL TABULATION (U+0009
                "\n", // LINE FEED (U+000A
                "\u000B", // VERTICAL TABULATION
                "\f", // FORM FEED (U+000C
                "\r", // CARRIAGE RETURN (U+000D
                "\u001C", // FILE SEPARATOR
                "\u001D", // GROUP SEPARATOR
                "\u001E",
                "\u001F",

                // Multiple character tests
                // ASCII chars
                "\t \n \u000B \f \r \u001C \u001D \u001E \u001F"
        );
    }

    private static void assertOnePropertyViolation(SftpConfig config, String propertyName) {
        ValidationTestHelper.assertOnePropertyViolation(DEFAULT_VALIDATOR, config, propertyName);
    }

    private static void assertNoPropertyViolations(SftpConfig config, String propertyName) {
        ValidationTestHelper.assertNoPropertyViolations(DEFAULT_VALIDATOR, config, propertyName);
    }
}
