package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("KiwiPrimitives")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiPrimitivesTest {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testFirstNonZero_Int(SoftAssertions softly) {
        softly.assertThat(KiwiPrimitives.firstNonZero(42, 84)).isEqualTo(42);
        softly.assertThat(KiwiPrimitives.firstNonZero(0, 42)).isEqualTo(42);
        softly.assertThatThrownBy(() -> KiwiPrimitives.firstNonZero(0, 0))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testFirstNonZero_Long(SoftAssertions softly) {
        softly.assertThat(KiwiPrimitives.firstNonZero(42L, 84L)).isEqualTo(42L);
        softly.assertThat(KiwiPrimitives.firstNonZero(0L, 42L)).isEqualTo(42L);
        softly.assertThatThrownBy(() -> KiwiPrimitives.firstNonZero(0L, 0L))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    class TryParseIntOrNull {

        @Test
        void shouldReturnInteger_WhenArgumentIsParseableToInt() {
            assertThat(KiwiPrimitives.tryParseIntOrNull("42")).isEqualTo(42);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNull_WhenArgumentIsNullOrEmpty(String string) {
            assertThat(KiwiPrimitives.tryParseIntOrNull(string)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "abcd", "42a", "a42", "4,200", "420.0"})
        void shouldReturnNull_WhenArgumentIsNotParseableToInt(String string) {
            assertThat(KiwiPrimitives.tryParseIntOrNull(string)).isNull();
        }
    }

    @Nested
    class TryParseInt {

        @Test
        void shouldReturnOptionalHavingValue_WhenArgumentIsParseableToInt() {
            assertThat(KiwiPrimitives.tryParseInt("84")).hasValue(84);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_WhenArgumentIsNulOrEmpty(String string) {
            assertThat(KiwiPrimitives.tryParseInt(string)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "abcd", "42a", "a42", "4,200", "420.0"})
        void shouldReturnEmptyOptional_WhenArgumentIsNotParseableToInt(String string) {
            assertThat(KiwiPrimitives.tryParseInt(string)).isEmpty();
        }
    }

    @Nested
    class TryParseIntOrThrow {

        @Test
        void shouldReturInt_WhenArgumentIsParseableToInt() {
            assertThat(KiwiPrimitives.tryParseIntOrThrow("126")).isEqualTo(126);
        }

        @Test
        void shouldThrow_WhenArgumentIsNull() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiPrimitives.tryParseIntOrThrow(null))
                    .withMessageContaining("java.lang.NullPointerException")
                    .withCauseExactlyInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "abcd", "42a", "a42", "4,200", "420.0"})
        void shouldThrow_WhenArgumentIsNotParseableToInt(String string) {
            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiPrimitives.tryParseIntOrThrow(string))
                    .withMessageContaining(string)
                    .withCauseExactlyInstanceOf(NumberFormatException.class);
        }
    }
}
