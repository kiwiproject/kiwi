package org.kiwiproject.validation;

import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("LongValueValidator")
class LongValueValidatorTest {

    @Nested
    class ShouldBeValid {

        @Test
        void whenAllowingNull_AndValueIsNull() {
            var object = new SampleAllowNullObject(null, 42L);
            assertValueIsValid(object);
        }

        @ParameterizedTest
        @ValueSource(longs = {Long.MIN_VALUE, -84, 0, 42, Long.MAX_VALUE})
        void whenValueIsALong(long value) {
            var strValue = String.valueOf(value);
            var object = new SampleObject(strValue, 1024L);
            assertValueIsValid(object);
        }

        @ParameterizedTest
        @ValueSource(strings = {"042", "0042", "001", "+42", "-42", "+0042", "-0084"})
        void whenValueContainingSignsAndLeadingZeroesIsALong(String value) {
            var object = new SampleObject(value, 84L);
            assertValueIsValid(object);
        }

        private void assertValueIsValid(Object object) {
            assertNoPropertyViolations(object, "value");
        }
    }

    @Nested
    class ShouldNotBeValid {

        @Test
        void whenNotAllowingNull_AndValueIsNull() {
            var object = new SampleObject(null, 168L);
            assertValueIsNotValid(object);
            assertPropertyViolations(object, "value", "must be convertible to a long");
        }

        @ParameterizedTest
        @ValueSource(strings = {"foo", "bar", "baz", "spam", "eggs", "ham", "0xCAFEBABE"})
        void whenNotALong(String value) {
            var object = new SampleObject(value, 252L);
            assertValueIsNotValid(object);
        }

        @ParameterizedTest
        @ValueSource(strings = {"-9223372036854775809", "9223372036854775808"})
        void whenOutsideLongRange(String value) {
            var object = new SampleObject(value, 420L);
            assertValueIsNotValid(object);
        }

        private void assertValueIsNotValid(SampleObject object) {
            assertOnePropertyViolation(object, "value");
        }
    }

    @Value
    private static class SampleObject {
        @LongValue
        String value;

        @NotNull
        Long otherValue;
    }

    @Value
    private static class SampleAllowNullObject {
        @LongValue(allowNull = true)
        String value;

        @NotNull
        Long otherValue;
    }
}
