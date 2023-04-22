package org.kiwiproject.validation;

import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertViolations;

import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.Validator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZonedDateTime;

@DisplayName("FieldRangeValidator")
class FieldRangeValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Nested
    class WhenNotAllowingNull {

        @Nested
        class ShouldBeValid {

            @Test
            void whenStartAndEndFieldsAreNonNull() {
                var obj = NotAllowNull.builder().a1(1).a2(10).build();
                assertNoViolations(validator, obj);
            }
        }

        @Nested
        class ShouldBeInvalid {

            @Test
            void whenStartFieldIsNull() {
                var obj = NotAllowNull.builder().a1(null).a2(42).build();
                assertViolations(validator, obj, "a1 is required");
            }

            @Test
            void whenEndFieldIsNull() {
                var obj = NotAllowNull.builder().a1(42).a2(null).build();
                assertViolations(validator, obj, "a2 is required");
            }

            @Test
            void whenBothFieldsAreNull() {
                var obj = NotAllowNull.builder().a1(null).a2(null).build();
                assertViolations(validator, obj, "a1 is required", "a2 is required");
            }
        }
    }

    @Nested
    class WhenAllowingNull {

        @Nested
        class ShouldBeValid {

            @Test
            void whenStartFieldIsNull() {
                var obj = AllowNull.builder().b1(null).b2(8).build();
                assertNoViolations(validator, obj);
            }

            @Test
            void whenEndFieldIsNull() {
                var obj = AllowNull.builder().b1(96).b2(null).build();
                assertNoViolations(validator, obj);
            }

            @Test
            void whenBothFieldsAreNull() {
                var obj = AllowNull.builder().b1(null).b2(null).build();
                assertNoViolations(validator, obj);
            }
        }
    }

    @Nested
    class UnsupportedTypes {

        @Test
        void shouldBeInvalid_WhenMinMaxForcesConversionToComparable() {
            var today = LocalDate.now();
            var obj = HasUnsupportedType.builder().date1(today).date2(today.plusDays(2)).build();
            assertViolations(validator, obj,
                    "date1 unknown validation error (type may be unsupported or may not be Comparable)");
        }

        @Test
        void shouldBeInvalid_WhenNotComparable() {
            var obj = HasAnIncomparable.builder().ic1(new AnIncomparable()).ic2(new AnIncomparable()).build();
            assertViolations(validator, obj,
                    "ic1 unknown validation error (type may be unsupported or may not be Comparable)");
        }
    }

    @Nested
    class WhenNoMinOrMax {

        @Test
        void shouldBeAbleToSupportAnyComparable() {
            var nowZdt = ZonedDateTime.now();

            // Hard-code time to ensure we don't fail when between midnight and 1AM. See #376
            var localTime1 = LocalTime.of(14, 35);

            var obj = NoMinOrMax.builder()
                    .localDate1(LocalDate.now())
                    .localDate2(LocalDate.now().plusDays(1))  // valid
                    .localTime1(localTime1)
                    .localTime2(localTime1.minusHours(1))  // invalid
                    .zdt1(nowZdt)
                    .zdt2(nowZdt)  // invalid
                    .zdt3(nowZdt)
                    .zdt4(nowZdt)  // valid
                    .year1(Year.of(1995))
                    .year2(Year.of(2000))  // valid
                    .season1(Season.SUMMER)
                    .season2(Season.SPRING)  // invalid (summer ordinal > spring ordinal)
                    .build();

            assertViolations(validator, obj,
                    "localTime1 must occur before localTime2",
                    "zdt1 must occur before zdt2",
                    "season1 must occur before season2");
        }
    }

    @Nested
    class WhenFieldRangeHasOnlyMin {

        @ParameterizedTest
        @ValueSource(ints = {10, 11, 12, 15, 18})
        void shouldBeValid_WhenStartEqualsOrAboveMin(int start) {
            var obj = HasOnlyMin.builder().m1(start).m2(19).build();
            assertNoViolations(validator, obj);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 5, 8, 9})
        void shouldBeInvalid_WhenBelowMin(int start) {
            var obj = HasOnlyMin.builder().m1(start).m2(19).build();
            assertViolations(validator, obj, "m1 must be equal to or above 10");
        }
    }

    @Nested
    class WhenFieldRangeHasOnlyMax {

        @ParameterizedTest
        @ValueSource(ints = {0, 5, 10, 19, 20})
        void shouldBeValid_WhenEndBelowOrEqualToMax(int end) {
            var obj = HasOnlyMax.builder().m1(-10).m2(end).build();
            assertNoViolations(validator, obj);
        }

        @ParameterizedTest
        @ValueSource(ints = {21, 22, 30, 50})
        void shouldBeInvalid_WhenAboveMax(int end) {
            var obj = HasOnlyMax.builder().m1(-8).m2(end).build();
            assertViolations(validator, obj, "m2 must be below or equal to 20");
        }
    }

    @Nested
    class WhenFieldRangeHasMinAndMax {

        @ParameterizedTest
        @CsvSource({
                "10,20",
                "11,19",
                "13,18",
                "10,11",
                "19,20",
        })
        void shouldBeValid_WhenStartEqualsOrAboveMin_AndEndBelowOrEqualToMax(int start, int end) {
            var obj = HasMinAndMax.builder().m1(start).m2(end).build();
            assertNoViolations(validator, obj);
        }

        @ParameterizedTest
        @CsvSource({
                "9,20",
                "8,15",
                "0,11",
        })
        void shouldBeInvalid_WhenStartBelowMin(int start, int end) {
            var obj = HasMinAndMax.builder().m1(start).m2(end).build();
            assertViolations(validator, obj, "m1 must be between 10 and 20");
        }

        @ParameterizedTest
        @CsvSource({
                "10,21",
                "15,25",
                "19,21",
        })
        void shouldBeInvalid_WhenEndAboveMin(int start, int end) {
            var obj = HasMinAndMax.builder().m1(start).m2(end).build();
            assertViolations(validator, obj, "m2 must be between 10 and 20");
        }
    }

    @Nested
    class RangeCheck {

        @Nested
        class WhenNotAllowingStartToEqualStop {

            @ParameterizedTest
            @ValueSource(ints = {5, 10, 20, 23, 24})
            void shouldBeValid_WhenStartLessThanEnd(int start) {
                var obj = NotAllowStartToEqualEnd.builder().s1(start).s2(25).build();
                assertNoViolations(validator, obj);
            }

            @ParameterizedTest
            @ValueSource(ints = {25, 26, 50, 100})
            void shouldBeInvalid_WhenStartEqualOrAboveEnd(int start) {
                var obj = NotAllowStartToEqualEnd.builder().s1(start).s2(25).build();
                assertViolations(validator, obj, "s1 must occur before s2");
            }
        }

        @Nested
        class WhenAllowingStartToEqualStop {

            @ParameterizedTest
            @ValueSource(ints = {5, 10, 20, 24, 25})
            void shouldBeValid_WhenStartLessThanOrEqualToEnd(int start) {
                var obj = AllowStartToEqualEnd.builder().s1(start).s2(25).build();
                assertNoViolations(validator, obj);
            }

            @ParameterizedTest
            @ValueSource(ints = {26, 27, 50, 100})
            void shouldBeInvalid_WhenStartAboveEnd(int start) {
                var obj = AllowStartToEqualEnd.builder().s1(start).s2(25).build();
                assertViolations(validator, obj, "s1 must occur before or match s2");
            }
        }
    }

    @Nested
    class WhenHasLabels {

        @Test
        void shouldUseMinAndMaxLabelsInValidationErrorMessages() {
            var obj = HasLabels.builder().mm1(50).mm2(600).build();
            assertViolations(validator, obj,
                    "mm1 must be between one hundred and two hundred",
                    "mm2 must be between one hundred and two hundred");
        }

        @Test
        void shouldUseEndFieldLabel() {
            var obj = HasLabels.builder().mm1(150).mm2(140).build();
            assertViolations(validator, obj, "mm1 must occur before Em-Em-2");
        }
    }

    @Builder
    @Getter
    @FieldRange(startField = "a1", endField = "a2")
    private static class NotAllowNull {
        private final Integer a1;
        private final Integer a2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "b1", allowNullStart = true, endField = "b2", allowNullEnd = true)
    private static class AllowNull {
        private final Integer b1;
        private final Integer b2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "m1", min = "10", endField = "m2")
    private static class HasOnlyMin {
        private final Integer m1;
        private final Integer m2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "m1", endField = "m2", max = "20")
    private static class HasOnlyMax {
        private final Integer m1;
        private final Integer m2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "m1", min = "10", endField = "m2", max = "20")
    private static class HasMinAndMax {
        private final Integer m1;
        private final Integer m2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "s1", endField = "s2")
    private static class NotAllowStartToEqualEnd {
        private final Integer s1;
        private final Integer s2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "s1", endField = "s2", allowStartToEqualEnd = true)
    private static class AllowStartToEqualEnd {
        private final Integer s1;
        private final Integer s2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "ic1", endField = "ic2")
    static class HasAnIncomparable {
        AnIncomparable ic1;
        AnIncomparable ic2;
    }

    private static class AnIncomparable {
    }

    @Builder
    @Getter
    @FieldRange(startField = "date1", min = "2020-01-01", endField = "date2", max = "2020-12-31")
    static class HasUnsupportedType {
        LocalDate date1;
        LocalDate date2;
    }

    @Builder
    @Getter
    @FieldRange(
            startField = "mm1", min = "100", minLabel = "one hundred",
            endField = "mm2", endFieldLabel = "Em-Em-2", max = "200", maxLabel = "two hundred")
    private static class HasLabels {
        private final Integer mm1;
        private final Integer mm2;
    }

    @Builder
    @Getter
    @FieldRange(startField = "localDate1", endField = "localDate2")
    @FieldRange(startField = "localTime1", endField = "localTime2")
    @FieldRange(startField = "zdt1", endField = "zdt2")
    @FieldRange(startField = "zdt3", endField = "zdt4", allowStartToEqualEnd = true)
    @FieldRange(startField = "year1", endField = "year2")
    @FieldRange(startField = "season1", endField = "season2")
    private static class NoMinOrMax {

        LocalDate localDate1;
        LocalDate localDate2;

        LocalTime localTime1;
        LocalTime localTime2;

        ZonedDateTime zdt1;
        ZonedDateTime zdt2;

        ZonedDateTime zdt3;
        ZonedDateTime zdt4;

        Year year1;
        Year year2;

        Season season1;
        Season season2;
    }

    // Enums are Comparable (using their ordinal number)
    enum Season {
        WINTER, SPRING, SUMMER, FALL
    }
}
