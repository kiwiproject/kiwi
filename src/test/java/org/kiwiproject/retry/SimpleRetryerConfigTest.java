package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.validation.ValidationTestHelper.DEFAULT_VALIDATOR;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.internal.Fixtures;
import org.opentest4j.AssertionFailedError;
import org.slf4j.event.Level;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import lombok.Getter;
import lombok.Setter;

@DisplayName("SimpleRetryerConfig")
@ExtendWith(SoftAssertionsExtension.class)
class SimpleRetryerConfigTest {

    private static final ObjectMapper DROPWIZARD_OBJECT_MAPPER = Jackson.newObjectMapper();

    @Test
    void shouldHaveDefaultValues() {
        var config = new SimpleRetryerConfig();

        assertAll(
                () -> assertThat(config.getMaxAttempts()).isEqualTo(SimpleRetryer.DEFAULT_MAX_ATTEMPTS),
                () -> assertThat(config.getRetryDelayTime()).isEqualTo(SimpleRetryer.DEFAULT_RETRY_DELAY_TIME),
                () -> assertThat(config.getRetryDelayUnit()).isEqualTo(SimpleRetryer.DEFAULT_RETRY_DELAY_UNIT),
                () -> assertThat(config.getCommonType()).isEqualTo(SimpleRetryer.DEFAULT_TYPE),
                () -> assertThat(config.getLogLevelForSubsequentAttempts()).isEqualTo(SimpleRetryer.DEFAULT_RETRY_LOG_LEVEL)
        );
    }

    @Nested
    class Deserializing {

        @Nested
        class FromJson {

            @Test
            void shouldAllowOverridingDefaultValues() throws JsonMappingException, JsonProcessingException {
                var json = Fixtures.fixture("SimpleRetryerConfigTest/config-all-properties.json");
                var retryerConfig = DROPWIZARD_OBJECT_MAPPER.readValue(json, SimpleRetryerConfig.class);
                assertAll(
                    () -> assertThat(retryerConfig.getMaxAttempts()).isEqualTo(10),
                    () -> assertThat(retryerConfig.getRetryDelayTime()).isEqualTo(75_000),
                    () -> assertThat(retryerConfig.getRetryDelayUnit()).isEqualTo(TimeUnit.MICROSECONDS),
                    () -> assertThat(retryerConfig.getCommonType()).isEqualTo("an expensive object"),
                    () -> assertThat(retryerConfig.getLogLevelForSubsequentAttempts()).isEqualTo(Level.WARN)
                );
            }

            @Test
            void shouldRespectDefaultValues() throws JsonMappingException, JsonProcessingException {
                var json = Fixtures.fixture("SimpleRetryerConfigTest/config-minimal-properties.json");
                var retryerConfig = DROPWIZARD_OBJECT_MAPPER.readValue(json, SimpleRetryerConfig.class);
                var defaultRetryerConfig = new SimpleRetryerConfig();
                assertAll(
                    () -> assertThat(retryerConfig.getMaxAttempts()).isEqualTo(7),
                    () -> assertThat(retryerConfig.getRetryDelayTime()).isEqualTo(defaultRetryerConfig.getRetryDelayTime()),
                    () -> assertThat(retryerConfig.getRetryDelayUnit()).isEqualTo(defaultRetryerConfig.getRetryDelayUnit()),
                    () -> assertThat(retryerConfig.getCommonType()).isEqualTo(defaultRetryerConfig.getCommonType()),
                    () -> assertThat(retryerConfig.getLogLevelForSubsequentAttempts()).isEqualTo(Level.DEBUG)
                );
            }
        }

        @Nested
        class FromYaml {

            private static final String SYSTEM_PROPERTY_PREFIX = "dw";

            @Test
            void shouldDeserializeUsingPlainYamlObjectMapper() throws JsonMappingException, JsonProcessingException {
                var yamlMapper = new ObjectMapper(new YAMLFactory());
                var yaml = Fixtures.fixture("SimpleRetryerConfigTest/config-all-properties.yml");
                var config = yamlMapper.readValue(yaml, AppConfiguration.class);
                var retryerConfig = config.getRetryerConfig();
                assertAll(
                    () -> assertThat(retryerConfig.getMaxAttempts()).isEqualTo(5),
                    () -> assertThat(retryerConfig.getRetryDelayTime()).isEqualTo(1),
                    () -> assertThat(retryerConfig.getRetryDelayUnit()).isEqualTo(TimeUnit.SECONDS),
                    () -> assertThat(retryerConfig.getCommonType()).isEqualTo("some type"),
                    () -> assertThat(retryerConfig.getLogLevelForSubsequentAttempts()).isEqualTo(Level.DEBUG)
                );
            }

            @Test
            void shouldAllowOverridingDefaultValues() {
                var config = deserializeDropwizardConfigFromYaml("config-all-properties.yml");
                var retryerConfig = config.getRetryerConfig();
                assertAll(
                    () -> assertThat(retryerConfig.getMaxAttempts()).isEqualTo(5),
                    () -> assertThat(retryerConfig.getRetryDelayTime()).isEqualTo(1),
                    () -> assertThat(retryerConfig.getRetryDelayUnit()).isEqualTo(TimeUnit.SECONDS),
                    () -> assertThat(retryerConfig.getCommonType()).isEqualTo("some type"),
                    () -> assertThat(retryerConfig.getLogLevelForSubsequentAttempts()).isEqualTo(Level.DEBUG)
                );
            }

            @Test
            void shouldRespectDefaultValues() {
                var config = deserializeDropwizardConfigFromYaml("config-minimal-properties.yml");
                var retryerConfig = config.getRetryerConfig();
                var defaultRetryerConfig = new SimpleRetryerConfig();
                assertAll(
                    () -> assertThat(retryerConfig.getMaxAttempts()).isEqualTo(5),
                    () -> assertThat(retryerConfig.getRetryDelayTime()).isEqualTo(defaultRetryerConfig.getRetryDelayTime()),
                    () -> assertThat(retryerConfig.getRetryDelayUnit()).isEqualTo(defaultRetryerConfig.getRetryDelayUnit()),
                    () -> assertThat(retryerConfig.getCommonType()).isEqualTo(defaultRetryerConfig.getCommonType()),
                    () -> assertThat(retryerConfig.getLogLevelForSubsequentAttempts()).isEqualTo(defaultRetryerConfig.getLogLevelForSubsequentAttempts())
                );
            }

            private AppConfiguration deserializeDropwizardConfigFromYaml(String configFileName) {
                var factory = new YamlConfigurationFactory<>(
                    AppConfiguration.class,
                    DEFAULT_VALIDATOR,
                    DROPWIZARD_OBJECT_MAPPER,
                    SYSTEM_PROPERTY_PREFIX);

                var path = Paths.get("SimpleRetryerConfigTest", configFileName).toString();

                try {
                    return factory.build(new ResourceConfigurationSourceProvider(), path);
                } catch (Exception e) {
                    throw new AssertionFailedError("Error de-serializing config at path: " + path, e);
                }
            }
        }

    }

    @Nested
    class Validation {

        private SimpleRetryerConfig config;

        @BeforeEach
        void setUp() {
            config = new SimpleRetryerConfig();
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 0})
        void shouldRequirePositiveMaxAttempts(int maxAttempts) {
            config.setMaxAttempts(maxAttempts);
            assertOnePropertyViolation(config, "maxAttempts");
        }

        @ParameterizedTest
        @ValueSource(longs = {-100, -1, 0})
        void shouldRequirePositiveRetryDelayTime(long retryDelayTime) {
            config.setRetryDelayTime(retryDelayTime);
            assertOnePropertyViolation(config, "retryDelayTime");
        }

        @Test
        void shouldRequireRetryDelayUnit() {
            config.setRetryDelayUnit(null);
            assertOnePropertyViolation(config, "retryDelayUnit");
        }

        @Test
        void shouldRequireNonNullCommonType() {
            config.setCommonType(null);
            assertOnePropertyViolation(config, "commonType");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t", " \t \r\n \t"})
        void shouldRequireNonBlankCommonType(String commonType) {
            config.setCommonType(commonType);
            assertOnePropertyViolation(config, "commonType");
        }

        @Test
        void shouldRequireLogLevelForSubsequentAttempts() {
            config.setLogLevelForSubsequentAttempts(null);
            assertOnePropertyViolation(config, "logLevelForSubsequentAttempts");
        }
    }

    @Getter
    @Setter
    public static class AppConfiguration extends Configuration {

        @NotNull
        @Valid
        private SimpleRetryerConfig retryerConfig;
    }
}
