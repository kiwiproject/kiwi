package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("KiwiEnums")
class KiwiEnumsTest {

    @Nested
    class Equals {

        @ParameterizedTest
        @EnumSource(Color.class)
        void shouldBeTrueWhenEqualAndCaseMatchesExactly(Color color) {
            assertThat(KiwiEnums.equals(color, color.name())).isTrue();
        }

        @ParameterizedTest
        @EnumSource(Color.class)
        void shouldBeFalseWhenNotEqual(Color color) {
            assertThat(KiwiEnums.equals(color, "BROWN")).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullAndEmptyStringsToCompare(String value) {
            assertThat(KiwiEnums.equals(Color.BLUE, value)).isFalse();
        }

        @Test
        void shouldNotAllowNullEnumValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.equals(null, "RED"))
                    .withMessage("enumValue must not be null");
        }
    }

    @Nested
    class EqualsIgnoreCase {

        @ParameterizedTest
        @ValueSource(strings = {"YELLOW", "yellow", "Yellow", "YeLLoW", "YellOW"})
        void shouldBeTrueWhenEqualButDifferentCase(String value) {
            assertThat(KiwiEnums.equalsIgnoreCase(Color.YELLOW, value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"YELLOW", "orange", "green", "violet"})
        void shouldBeFalseWhenNotEqual(String value) {
            assertThat(KiwiEnums.equalsIgnoreCase(Color.BLUE, value)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullAndEmptyStringsToCompare(String value) {
            assertThat(KiwiEnums.equalsIgnoreCase(Color.VIOLET, value)).isFalse();
        }

        @Test
        void shouldNotAllowNullEnumValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.equalsIgnoreCase(null, "GREEN"))
                    .withMessage("enumValue must not be null");
        }
    }

    @Nested
    class NotEquals {

        @ParameterizedTest
        @ValueSource(strings = {"ORANGE", "RED", "Blue", "indigo", "Green"})
        void shouldBeTrueWhenNotEqual(String value) {
            assertThat(KiwiEnums.notEquals(Color.VIOLET, value)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(Color.class)
        void shouldBeFalseWhenEqual(Color color) {
            assertThat(KiwiEnums.notEquals(color, color.name())).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullAndEmptyStringsToCompare(String value) {
            assertThat(KiwiEnums.notEquals(Color.VIOLET, value)).isTrue();
        }

        @Test
        void shouldNotAllowNullEnumValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.notEquals(null, "BLUE"))
                    .withMessage("enumValue must not be null");
        }
    }

    @Nested
    class NotEqualsIgnoreCase {

        @ParameterizedTest
        @ValueSource(strings = {"YELLOW", "red", "Blue", "GREEN", "indigo"})
        void shouldBeTrueWhenNotEqual(String value) {
            assertThat(KiwiEnums.notEqualsIgnoreCase(Color.ORANGE, value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ORANGE", "orange", "Orange", "OranGE", "OrAngE"})
        void shouldBeFalseWhenEqualButDifferentCase(String value) {
            assertThat(KiwiEnums.notEqualsIgnoreCase(Color.ORANGE, value)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullAndEmptyStringsToCompare(String value) {
            assertThat(KiwiEnums.notEqualsIgnoreCase(Color.INDIGO, value)).isTrue();
        }

        @Test
        void shouldNotAllowNullEnumValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.notEqualsIgnoreCase(null, "BLUE"))
                    .withMessage("enumValue must not be null");
        }
    }

    @Nested
    class EqualsAny {

        @ParameterizedTest
        @ValueSource(strings = {"RED", "GREEN", "INDIGO"})
        void shouldBeTrueWhenValueIsOneOfGivenEnumValues(String value) {
            assertThat(KiwiEnums.equalsAny(value, Color.RED, Color.GREEN, Color.INDIGO)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"RED", "GREEN", "INDIGO", "BROWN", "MAROON"})
        void shouldBeFalseWhenValueIsNotOneOfGivenEnumValues(String value) {
            assertThat(KiwiEnums.equalsAny(value, Color.VIOLET, Color.YELLOW)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullAndEmptyStringsToCompare(String value) {
            assertThat(KiwiEnums.equalsAny(value, Color.GREEN, Color.BLUE, Color.INDIGO)).isFalse();
        }

        @Test
        void shouldRequireAtLeastOneNonNullEnumValue() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAny("indigo"))
                            .withMessage("enumValues must not be null or empty"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAny("indigo", new Color[0]))
                            .withMessage("enumValues must not be null or empty"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAny("violet", (Color[]) null))
                            .withMessage("enumValues must not be null or empty"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAny("green", (Color) null))
                            .withMessage("enumValue must not be null")
            );
        }
    }

    @Nested
    class EqualsAnyIgnoreCase {

        @ParameterizedTest
        @ValueSource(strings = {"red", "RED", "green", "Green", "Indigo", "indigo"})
        void shouldBeTrueWhenValueIsOneOfGivenEnumValuesButDifferentCase(String value) {
            assertThat(KiwiEnums.equalsAnyIgnoreCase(value, Color.RED, Color.GREEN, Color.INDIGO)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"red", "RED", "green", "Green", "Indigo", "indigo"})
        void shouldBeFalseWhenValueIsNotOneOfGivenEnumValues(String value) {
            assertThat(KiwiEnums.equalsAnyIgnoreCase(value, Color.ORANGE, Color.BLUE)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullAndEmptyStringsToCompare(String value) {
            assertThat(KiwiEnums.equalsAnyIgnoreCase(value, Color.GREEN, Color.BLUE, Color.INDIGO)).isFalse();
        }

        @Test
        void shouldRequireAtLeastOneNonNullEnumValue() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAnyIgnoreCase("red"))
                            .withMessage("enumValues must not be null or empty"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAnyIgnoreCase("indigo", new Color[0]))
                            .withMessage("enumValues must not be null or empty"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAnyIgnoreCase("yellow", (Color[]) null))
                            .withMessage("enumValues must not be null or empty"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiEnums.equalsAnyIgnoreCase("orange", (Color) null))
                            .withMessage("enumValue must not be null")
            );
        }
    }

    enum Color {
        RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET
    }
}
