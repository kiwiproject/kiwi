package org.kiwiproject.validation;

import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@DisplayName("RangeValidator")
class RangeValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Nested
    class NullObjects {

        @Test
        void shouldBeValid_WhenAllowNullIsTrue() {
            var obj = new NullableValue();
            assertNoViolations(validator, obj);
        }

        @Test
        void shouldBeInvalid_WhenAllowNullIsFalse() {
            var obj = new NotNullableValue();
            assertPropertyViolations(validator, obj, "value", "is required");
        }
    }

    /**
     * @implNote In these cases, there should be a WARN level log, but we're not going to try to test
     * that side effect, and will just rely instead on manual inspection.
     */
    @Nested
    class UnsupportedTypes {

        @Test
        void shouldBeInvalid() {
            var obj = new HasUnsupportedType(LocalDate.now());
            assertPropertyViolations(validator, obj, "value",
                    "unknown validation error (type may be unsupported or may not be Comparable)");
        }

        @Test
        void shouldBeInvalid_WhenNotComparable() {
            var obj = new HasAnIncomparable(new AnIncomparable());
            assertPropertyViolations(validator, obj, "value",
                    "unknown validation error (type may be unsupported or may not be Comparable)");
        }
    }


    @Nested
    class ValidationMessage {

        @Test
        void shouldBe_RequiredMessage_WhenNull() {
            var obj = new NotNullableValue();
            assertPropertyViolations(validator, obj, "value", "is required");
        }

        @Test
        void shouldBe_BetweenMessage_WhenOutsideRange() {
            var obj = new HasMinAndMax(0);
            assertPropertyViolations(validator, obj, "value", "must be between 1 and 9");
        }

        @Test
        void shouldBe_GreaterThanOrEqualMessage_WhenOnlyHasMin() {
            var obj = new HasOnlyMin(9L);
            assertPropertyViolations(validator, obj, "value", "must be greater than or equal to 10");
        }

        @Test
        void shouldBe_LessThanOrEqualMessage_WhenOnlyHasMax() {
            var obj = new HasOnlyMax(12.0);
            assertPropertyViolations(validator, obj, "value", "must be less than or equal to 11.0");
        }

        @Test
        void shouldUseMinAndMaxLabels_WhenHasMinAndMax() {
            var obj = Hydra.builder().int1(11).build();
            assertPropertyViolations(validator, obj, "int1", "must be between one and ten");
        }

        @Test
        void shouldUseMinLabel_WhenHasOnlyMin() {
            var obj = Hydra.builder().int2(4).build();
            assertPropertyViolations(validator, obj, "int2", "must be greater than or equal to five");
        }

        @Test
        void shouldUseMaxLabel_WhenHasOnlyMax() {
            var obj = Hydra.builder().int3(11).build();
            assertPropertyViolations(validator, obj, "int3", "must be less than or equal to ten");
        }

        @Test
        void shouldUseLabels_ToMakeInstantErrorMessagesReadable() {
            var instant = Instant.from(ZonedDateTime.of(2020, 8, 21, 14, 30, 0, 0, ZoneId.of("America/New_York")));
            var obj = Hydra.builder().instant1(instant).build();
            assertPropertyViolations(validator, obj, "instant1", "must be between 2020-09-01 and 2020-09-30");
        }
    }

    @Nested
    class SupportedTypes {

        @ParameterizedTest
        @CsvSource({
                "-0.01, false",
                "0.0, true",
                "1.0, true",
                "9.999, true",
                "9.9991, false",
                "10.0, false",
        })
        void shouldValidateDoubles(double value, boolean expectedValid) {
            var hydra = Hydra.builder().double1(value).build();
            assertValidOrNot(hydra, "double1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "1.9999, false",
                "2.0, true",
                "4.9999, true",
                "5.0, true",
                "5.0001, false",
                "10.0, false",
        })
        void shouldValidateFloats(float value, boolean expectedValid) {
            var hydra = Hydra.builder().float1(value).build();
            assertValidOrNot(hydra, "float1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "0, false",
                "1, true",
                "2, true",
                "49, true",
                "50, true",
                "51, false",
                "127, false"
        })
        void shouldValidateBytes(byte value, boolean expectedValid) {
            var hydra = Hydra.builder().byte1(value).build();
            assertValidOrNot(hydra, "byte1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "99, false",
                "100, true",
                "150, true",
                "199, true",
                "200, true",
                "201, false",
                "30000, false"
        })
        void shouldValidateShorts(short value, boolean expectedValid) {
            var hydra = Hydra.builder().short1(value).build();
            assertValidOrNot(hydra, "short1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "0, false",
                "1, true",
                "2, true",
                "9, true",
                "10, true",
                "11, false",
                "25, false"
        })
        void shouldValidateIntegers(int value, boolean expectedValid) {
            var hydra = Hydra.builder().int1(value).build();
            assertValidOrNot(hydra, "int1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "-1, false",
                "0, true",
                "1, true",
                "500, true",
                "9999, true",
                "10000, false",
                "150000, false"
        })
        void shouldValidateLongs(long value, boolean expectedValid) {
            var hydra = Hydra.builder().long1(value).build();
            assertValidOrNot(hydra, "long1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "0.0, false",
                "0.999, false",
                "1.0, true",
                "500.0, true",
                "9999999.0, true",
                "9999999.0001, false",
                "99999999.0, false"
        })
        void shouldValidateBigDecimals(String value, boolean expectedValid) {
            var hydra = Hydra.builder().bigDecimal1(new BigDecimal(value)).build();
            assertValidOrNot(hydra, "bigDecimal1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "-1, false",
                "9999, false",
                "10000, true",
                "100000, true",
                "10000000, true",
                "10000001, false",
                "100000000, false"
        })
        void shouldValidateBigIntegers(String value, boolean expectedValid) {
            var hydra = Hydra.builder().bigInteger1(new BigInteger(value)).build();
            assertValidOrNot(hydra, "bigInteger1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "1596257999999, false",  // 1 milli before min
                "1596258000000, true",   // equal to min
                "1597554000000, true",   // 15 days after min
                "1598849999999, true",   // 1 milli before max
                "1598850000000, true",   // equal to max
                "1598850000001, false",  // 1 milli after max
        })
        void shouldValidateDates(long epochMilli, boolean expectedValid) {
            var hydra = Hydra.builder().date1(new Date(epochMilli)).build();
            assertValidOrNot(hydra, "date1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "1598936399999, false",  // 1 milli before min
                "1598936400000, true",   // equal to min
                "1600232400000, true",   // 15 days after min
                "1601441999999, true",   // 1 milli before max
                "1601442000000, true",   // equal to max
                "1601442000001, false",  // 1 milli after max
        })
        void shouldValidateInstants(long epochMilli, boolean expectedValid) {
            var hydra = Hydra.builder().instant1(Instant.ofEpochMilli(epochMilli)).build();
            assertValidOrNot(hydra, "instant1", expectedValid);
        }

        @ParameterizedTest
        @CsvSource({
                "1, false",
                "2, true",
                "12, true",
                "13, true",
                "14, false"
        })
        void shouldValidateComparableFromJson(int value, boolean expectedValid) {
            var hydra = Hydra.builder().intHolder1(new IntHolder(value)).build();
            assertValidOrNot(hydra, "intHolder1", expectedValid);
        }

        private void assertValidOrNot(Hydra hydra, String propertyName, boolean expectedValid) {
            if (expectedValid) {
                assertNoPropertyViolations(validator, hydra, propertyName);
            } else {
                assertOnePropertyViolation(validator, hydra, propertyName);
            }
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class NullableValue {
        @Range(min = "0")
        Integer value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class NotNullableValue {
        @Range(max = "100", allowNull = false)
        Integer value;
    }

    @AllArgsConstructor
    static class HasMinAndMax {
        @Range(min = "1", max = "9")
        Integer value;
    }

    @AllArgsConstructor
    static class HasOnlyMin {
        @Range(min = "10")
        Long value;
    }

    @AllArgsConstructor
    static class HasOnlyMax {
        @Range(max = "11.0")
        Double value;
    }

    @AllArgsConstructor
    static class HasUnsupportedType {
        @Range(min = "2020-08-20")
        LocalDate value;
    }

    @AllArgsConstructor
    static class HasAnIncomparable {
        @Range(min = "A")
        AnIncomparable value;
    }

    private static class AnIncomparable {
    }

    @Builder
    @Getter
    static class Hydra {

        @Range(min = "0.0", max = "9.999")
        Double double1;

        @Range(min = "2.0", max = "5.0")
        Float float1;

        @Range(min = "1", max = "50")
        Byte byte1;

        @Range(min = "100", max = "200")
        Short short1;

        @Range(min = "1", minLabel = "one", max = "10", maxLabel = "ten")
        Integer int1;

        @Range(min = "5", minLabel = "five")
        Integer int2;

        @Range(max = "10", maxLabel = "ten")
        Integer int3;

        @Range(min = "0", max = "9999")
        Long long1;

        @Range(min = "1.0", max = "9999999.0")
        BigDecimal bigDecimal1;

        @Range(min = "10000", max = "10000000")
        BigInteger bigInteger1;

        /**
         * min: 2020-08-01, max: 2020-08-31
         */
        @Range(min = "1596258000000", minLabel = "2020-08-01", max = "1598850000000", maxLabel = "2020-08-31")
        Date date1;

        /**
         * min: 2020-09-01, max: 2020-09-30
         */
        @Range(min = "1598936400000", minLabel = "2020-09-01", max = "1601442000000", maxLabel = "2020-09-30")
        Instant instant1;

        @Range(
                min = " { \"value\": 2 } ",
                maxLabel = "two",
                max = " { \"value\": 13 } ",
                minLabel = "thirteen"
        )
        IntHolder intHolder1;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    static class IntHolder implements Comparable<IntHolder> {

        int value;

        @Override
        public int compareTo(IntHolder o) {
            return Integer.compare(value, o.value);
        }
    }
}
