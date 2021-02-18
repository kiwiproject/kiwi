package org.kiwiproject.validation;

import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("IntValidator")
class IntValidatorTest {

    private static final long ONE_BELOW_MAX_INTEGER = Integer.MIN_VALUE - 1L;
    private static final long ONE_ABOVE_MAX_INTEGER = Integer.MAX_VALUE + 1L;

    @Nested
    class ShouldBeValid {

        @Test
        void whenAllowingNull_AndValueIsNull() {
            var object = new SampleAllowNullObject(null);
            assertValueIsValid(object);
        }

        @ParameterizedTest
        @ValueSource(ints = {Integer.MIN_VALUE, -84, 0, 42, Integer.MAX_VALUE})
        void whenValueIsAnInt(int value) {
            var strValue = String.valueOf(value);
            var object = new SampleObject(strValue);
            assertValueIsValid(object);
        }

        @ParameterizedTest
        @ValueSource(strings = {"042", "0042", "001", "+42", "-42", "+0042", "-0084"})
        void whenValueContainingSignsAndLeadingZeroesIsAnInt(String value) {
            var object = new SampleObject(value);
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
            var object = new SampleObject(null);
            assertValueIsNotValid(object);
            assertPropertyViolations(object, "value", "must be convertible to an integer");
        }

        @ParameterizedTest
        @ValueSource(strings = {"foo", "bar", "baz", "spam", "eggs", "ham", "0xCAFEBABE"})
        void whenNotAnInt(String value) {
            var object = new SampleObject(value);
            assertValueIsNotValid(object);
        }

        @ParameterizedTest
        @ValueSource(longs = {ONE_BELOW_MAX_INTEGER, ONE_ABOVE_MAX_INTEGER})
        void whenOutsideIntRange(long value) {
            var strValue = String.valueOf(value);
            var object = new SampleObject(strValue);
            assertValueIsNotValid(object);
        }

        private void assertValueIsNotValid(SampleObject object) {
            assertOnePropertyViolation(object, "value");
        }
    }

    @Value
    private static class SampleObject {
        @Int
        String value;
    }

    @Value
    private static class SampleAllowNullObject {
        @Int(allowNull = true)
        String value;
    }

}