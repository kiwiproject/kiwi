package org.kiwiproject.validation;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.base.KiwiThrowables;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;

@DisplayName("InEnumValidator")
class InEnumValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Nested
    class WhenNullValue {

        @Test
        void shouldBeInvalid_ByDefault() {
            var weather = new Weather(null);
            assertPropertyViolations(validator, weather, "season", expectedErrorMessage());
        }

        @Test
        void shouldBeValid_WhenAllowingNulls() {
            var weather = new WeatherAllowingNullSeason(null);
            assertNoPropertyViolations(validator, weather, "season");
        }
    }

    @Nested
    class ShouldBeValid {

        @Test
        void whenValueIsInEnumConstants() {
            var weather = new Weather(Season.SPRING.name());
            assertNoViolations(validator, weather);
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPRING", "spring", "Spring", "SpRiNg"})
        void whenIgnoringCase(String season) {
            var weather = new WeatherIgnoringCase(season);
            assertNoViolations(validator, weather);
        }

        @Test
        void whenUsingValueMethod() {
            var weather = new WeatherWithValueMethod(Season.FALL.readableValue());
            assertNoViolations(validator, weather);
        }

        @ParameterizedTest
        @ValueSource(strings = {"AUTUMN", "autumn", "Autumn", "AuTuMn"})
        void whenUsingValueMethodAndIgnoringCase(String season) {
            var weather = new WeatherWithValueMethodIgnoringCase(season);
            assertNoViolations(validator, weather);
        }
    }

    @Nested
    class ShouldBeInvalid {

        @Test
        void whenNotIgnoringCase() {
            var weather = new Weather("spring");
            assertPropertyViolations(validator, weather, "season", expectedErrorMessage());
        }

        @Test
        void whenValueIsNotInTheEnum() {
            var weather = new Weather("WhatIsThis");
            assertPropertyViolations(validator, weather, "season", expectedErrorMessage());
        }

        @Test
        void whenUsingValueMethod_ButValueNotInTheEnum() {
            var weather = new WeatherWithValueMethod("Other");
            var expectedMessage = f("is not in the list {}", Season.readableValues());
            assertPropertyViolations(validator, weather, "season", expectedMessage);
        }

        @Test
        void whenEnumHasNoValues() {
            var odd = new VeryOdd("someValue");

            var thrown = catchThrowable(() -> validator.validate(odd));
            assertThat(thrown).isExactlyInstanceOf(ValidationException.class);

            var cause = KiwiThrowables.nextCauseOf(thrown).orElseThrow();
            assertThat(cause)
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Enum Description has no values to validate against!");
        }

        // Unable to invoke method exceptionalReadableValue on class org.kiwiproject.validation.InEnumValidatorTest$Season
        @Test
        void whenUsingExceptionThrowingValueMethod() {
            var weather = new WeatherWithExceptionalValueMethod("SPRING");

            var thrown = catchThrowable(() -> validator.validate(weather));
            assertThat(thrown).isExactlyInstanceOf(ValidationException.class);

            var cause = KiwiThrowables.nextCauseOf(thrown).orElseThrow();
            var expectedMessage =
                    f("Unable to invoke valueMethod 'exceptionalReadableValue' on class {}. Is it a public no-arg method?",
                            Season.class.getName());
            assertThat(cause)
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedMessage);
        }
    }

    private static String expectedErrorMessage() {
        return f("is not in the list {}", List.of(Season.values()));
    }

    enum Season {
        FALL("Autumn"), WINTER("Winter"), SPRING("Spring"), SUMMER("Summer");

        private final String readableValue;

        Season(String value) {
            this.readableValue = value;
        }

        String readableValue() {
            return readableValue;
        }

        String exceptionalReadableValue() {
            throw new IllegalStateException("I'm not ready to be called!");
        }

        static List<String> readableValues() {
            return Arrays.stream(values()).map(Season::readableValue).collect(toList());
        }
    }

    @AllArgsConstructor
    static class Weather {

        private final String season;

        @InEnum(enumClass = Season.class)
        public String getSeason() {
            return season;
        }
    }

    @AllArgsConstructor
    static class WeatherIgnoringCase {

        private final String season;

        @InEnum(enumClass = Season.class, ignoreCase = true)
        public String getSeason() {
            return season;
        }
    }

    @AllArgsConstructor
    static class WeatherAllowingNullSeason {

        private final String season;

        @InEnum(enumClass = Season.class, allowNull = true)
        public String getSeason() {
            return season;
        }
    }

    @AllArgsConstructor
    static class WeatherWithValueMethod {

        private final String season;

        @InEnum(enumClass = Season.class, valueMethod = "readableValue")
        public String getSeason() {
            return season;
        }
    }

    @AllArgsConstructor
    static class WeatherWithValueMethodIgnoringCase {

        private final String season;

        @InEnum(enumClass = Season.class, ignoreCase = true, valueMethod = "readableValue")
        public String getSeason() {
            return season;
        }
    }

    @AllArgsConstructor
    static class WeatherWithExceptionalValueMethod {

        private final String season;

        @InEnum(enumClass = Season.class, valueMethod = "exceptionalReadableValue")
        public String getSeason() {
            return season;
        }
    }

    enum Description {}

    @AllArgsConstructor
    static class VeryOdd {

        @InEnum(enumClass = Description.class)
        private String description;
    }
}
