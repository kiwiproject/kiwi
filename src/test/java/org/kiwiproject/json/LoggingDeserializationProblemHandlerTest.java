package org.kiwiproject.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.collect.KiwiMaps.newHashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.Getter;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

@DisplayName("LoggingDeserializationProblemHandler")
@ExtendWith(SoftAssertionsExtension.class)
class LoggingDeserializationProblemHandlerTest {

    private AtomicInteger consumerCount;
    private JsonHelper jsonHelper;

    @BeforeEach
    void setUp() {
        consumerCount = new AtomicInteger();
    }

    /**
     * The only reason for these tests without a handler is as a sanity check on the behavior we expect, so that if
     * Dropwizard or Jackson ever change their defaults, these tests would presumably fail.
     */
    @Nested
    class WithNoProblemHandlerAssigned {

        @Nested
        class UsingDefaultObjectMapper {

            @Test
            void shouldThrowUnrecognizedPropertyException() {
                var vanillaMapper = new ObjectMapper();
                assertThat(isFailOnUnknownPropertiesEnabled(vanillaMapper))
                        .describedAs("Expected vanilla ObjectMapper to have FAIL_ON_UNKNOWN_PROPERTIES enabled")
                        .isTrue();
                jsonHelper = new JsonHelper(vanillaMapper);

                assertThatThrownBy(() -> jsonHelper.convert(new Foo(), Bar.class))
                        .hasCauseInstanceOf(UnrecognizedPropertyException.class)
                        .hasMessageContaining("Unrecognized field \"foo\"")
                        .hasMessageContaining("not marked as ignorable");
            }
        }

        @Nested
        class UsingDropwizardObjectMapper {

            @Test
            void shouldNotThrowAnyException() {
                var dropwizardMapper = JsonHelper.newDropwizardObjectMapper();
                assertThat(isFailOnUnknownPropertiesEnabled(dropwizardMapper))
                        .describedAs("Expected 'Dropwizard' ObjectMapper to have FAIL_ON_UNKNOWN_PROPERTIES disabled")
                        .isFalse();
                jsonHelper = new JsonHelper(dropwizardMapper);

                assertThatCode(() -> jsonHelper.convert(new Foo(), Bar.class))
                        .doesNotThrowAnyException();
            }
        }

        private boolean isFailOnUnknownPropertiesEnabled(ObjectMapper mapper) {
            return mapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
    }

    @Nested
    class WithProblemHandlerAssigned {

        private LoggingDeserializationProblemHandler handler;

        @BeforeEach
        void setUp() {
            BiConsumer<String, Class<?>> countingConsumer = (propertyName, aClass) -> consumerCount.incrementAndGet();
            handler = new LoggingDeserializationProblemHandler(countingConsumer);
        }

        @Test
        void shouldHandleUnknownProperties_WithNoUnknownPropertyBiConsumer() {
            handler = new LoggingDeserializationProblemHandler();
            var mapper = JsonHelper.newDropwizardObjectMapper();
            mapper.addHandler(handler);
            jsonHelper = new JsonHelper(mapper);

            assertThatCode(() -> jsonHelper.convert(new Foo(), Bar.class))
                    .doesNotThrowAnyException();

            assertThat(handler.getUnknownPropertyCount()).isEqualTo(2);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.json.LoggingDeserializationProblemHandlerTest#objectMappers")
        void shouldIgnoreUnknownProperties_WhenIgnoreUnknownIsTrue(ObjectMapper mapper) {
            mapper.addHandler(handler);
            jsonHelper = new JsonHelper(mapper);

            assertThat(jsonHelper.convert(new Foo(), Ignored.class)).isInstanceOf(Ignored.class);

            assertThat(consumerCount).hasValue(0);
            assertNoUnexpectedPathInformation();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.json.LoggingDeserializationProblemHandlerTest#objectMappers")
        void shouldHandleUnknownProperties(ObjectMapper mapper) {
            mapper.addHandler(handler);
            jsonHelper = new JsonHelper(mapper);

            assertUnexpectedPathsWhenDeserializeCountIs(1);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.json.LoggingDeserializationProblemHandlerTest#objectMappers")
        void shouldNotifyConsumerForEveryUnknownProperty(ObjectMapper mapper) {
            mapper.addHandler(handler);
            jsonHelper = new JsonHelper(mapper);

            assertUnexpectedPathsWhenDeserializeCountIs(10);
        }

        @Test
        void shouldCatchExceptionsThrownByUnknownPropertyBiConsumer() {
            BiConsumer<String, Class<?>> throwingConsumer = (propertyName, aClass) -> {
                throw new RuntimeException("I am not well-behaved!");
            };
            handler = new LoggingDeserializationProblemHandler(throwingConsumer);
            var mapper = JsonHelper.newDropwizardObjectMapper();
            mapper.addHandler(handler);
            jsonHelper = new JsonHelper(mapper);

            assertThatCode(() -> jsonHelper.convert(new Foo(), Bar.class))
                    .doesNotThrowAnyException();

            assertThat(handler.getUnknownPropertyCount()).isEqualTo(2);
        }

        @Test
        void shouldClearUnexpectedPaths() {
            var mapper = JsonHelper.newDropwizardObjectMapper();
            mapper.addHandler(handler);
            jsonHelper = new JsonHelper(mapper);

            assertUnexpectedPathsWhenDeserializeCountIs(8);

            var totalCount = consumerCount.get();

            handler.clearUnexpectedPaths();

            assertThat(consumerCount)
                    .describedAs("consumerCount should not have changed")
                    .hasValue(totalCount);

            assertNoUnexpectedPathInformation();
        }

        private void assertUnexpectedPathsWhenDeserializeCountIs(int numTimes) {
            deserializeThisManyTimes(numTimes);

            assertThat(consumerCount)
                    .describedAs("the consumer should be called for every instance")
                    .hasValue(2 * numTimes);

            assertThat(handler.getUnknownPropertyCount())
                    .describedAs("expecting only two unique unknown properties")
                    .isEqualTo(2);

            assertThat(handler.getUnexpectedPaths()).containsExactlyInAnyOrder(
                    "org.kiwiproject.json.LoggingDeserializationProblemHandlerTest$Bar -> .foo",
                    "org.kiwiproject.json.LoggingDeserializationProblemHandlerTest$Buz -> .sub.baz"
            );

            assertThat(handler.getUnexpectedPropertyPaths()).containsExactlyInAnyOrder(
                    "org.kiwiproject.json.LoggingDeserializationProblemHandlerTest$Bar.foo",
                    "org.kiwiproject.json.LoggingDeserializationProblemHandlerTest$Buz.sub.baz"
            );
        }

        private void deserializeThisManyTimes(int numTimes) {
            IntStream.rangeClosed(1, numTimes)
                    .forEach(ignored -> jsonHelper.convert(new Foo(), Bar.class));
        }

        private void assertNoUnexpectedPathInformation() {
            assertThat(handler.getUnknownPropertyCount()).isZero();
            assertThat(handler.getUnexpectedPaths()).isEmpty();
            assertThat(handler.getUnexpectedPropertyPaths()).isEmpty();
        }
    }

    /**
     * Test a plain ObjectMapper and one configured via Dropwizard's {@link io.dropwizard.jackson.Jackson#newObjectMapper()}
     */
    static List<ObjectMapper> objectMappers() {
        return List.of(
                new ObjectMapper(),
                JsonHelper.newDropwizardObjectMapper()
        );
    }

    @Getter
    static class Foo {
        public String foo = "foo";
        public Baz sub = new Baz();
    }

    @Getter
    static class Baz {
        public Map<String, Object> baz = newHashMap("val", "baz");
    }

    @Getter
    static class Bar {
        public String bar = "bar";
        public Buz sub = new Buz();
    }

    @Getter
    static class Buz {
        public Map<String, Object> buz = newHashMap("val", "buz");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Ignored {
    }
}