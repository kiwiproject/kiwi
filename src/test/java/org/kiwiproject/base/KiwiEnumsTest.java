package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.util.BlankStringSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@DisplayName("KiwiEnums")
class KiwiEnumsTest {

    @Nested
    class GetIfPresent {

        @Test
        void shouldThrowIllegalArgumentWhenEnumClassIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.getIfPresent(null, "WINTER"));
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldReturnEmptyOptionalWhenGivenBlankInput(String input) {
            assertThat(KiwiEnums.getIfPresent(Season.class, input)).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(Season.class)
        void shouldReturnOptionalContainingEnumConstant(Season season) {
            var name = season.name();
            assertThat(KiwiEnums.getIfPresent(Season.class, name)).containsSame(season);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "fall",
            "Winter",
            "spring",
            "SummER",
            "summer",
            "foo",
            "bar"
        })
        void shouldReturnEmptyOptionalWhenNoMatchingEnumConstantExists(String input) {
            assertThat(KiwiEnums.getIfPresent(Season.class, input)).isEmpty();
        }
    }

    @Nested
    class GetIfPresentIgnoreCase {

        @Test
        void shouldThrowIllegalArgumentWhenEnumClassIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.getIfPresentIgnoreCase(null, "FALL"));
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldReturnEmptyOptionalWhenGivenBlankInput(String input) {
            assertThat(KiwiEnums.getIfPresentIgnoreCase(Season.class, input)).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
            "FALL, FALL",
            "fall, FALL",
            "Fall, FALL ",
            "winter, WINTER ",
            " '  winter', WINTER ",
            " 'spring  ', SPRING",
            "Spring, SPRING",
            "SuMmEr, SUMMER",
            " '  summer  ', SUMMER",
            " '\nSUMMER\t\t', SUMMER",
            " '  \tsummer  \t\t\r\n', SUMMER",
            " '\t\r\n\r\nFall\r\n\r\n\f\t  \t', FALL"
        })
        void shouldReturnOptionalContainingEnumConstantIgnoringCaseAndLeadingAndTrailingWhitespace(String input, Season expectedSeason) {
            assertThat(KiwiEnums.getIfPresentIgnoreCase(Season.class, input)).containsSame(expectedSeason);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "_winter_",
            "the summer",
            "AUTUMN",
            "foo",
            "SUM_MER",
            "Win.ter",
            "bar",
            "baz"
        })
        void shouldReturnEmptyOptionalWhenNoMatchingEnumConstantExists(String input) {
            assertThat(KiwiEnums.getIfPresentIgnoreCase(Season.class, input)).isEmpty();
        }
    }

    @Nested
    class GetIfPresentFuzzy {

        @Test
        void shouldThrowIllegalArgumentWhenEnumClassIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.getIfPresentFuzzy(null, "FALL"));
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldReturnEmptyOptionalWhenGivenBlankInput(String input) {
            assertThat(KiwiEnums.getIfPresentFuzzy(Season.class, input)).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
            "FALL, FALL",
            "fall, FALL",
            "Fall, FALL ",
            "winter, WINTER ",
            " '  winter', WINTER ",
            " 'spring  ', SPRING",
            "Spring, SPRING",
            "SuMmEr, SUMMER",
            " '  summer  ', SUMMER",
            " '\nSUMMER\t\t', SUMMER",
            " '  \tsummer  \t\t\r\n', SUMMER",
            " '\t\r\n\r\nFall\r\n\r\n\f\t  \t', FALL"
        })
        void shouldReturnOptionalContainingEnumConstantIgnoringCaseAndLeadingAndTrailingWhitespace(String input, Season expectedSeason) {
            assertThat(KiwiEnums.getIfPresentFuzzy(Season.class, input)).containsSame(expectedSeason);
        }

        @ParameterizedTest
        @CsvSource({
            "PENDING_PAYMENT, PENDING_PAYMENT",
            "processing_order, PROCESSING_ORDER",
            "CANCELLED_BY_CUSTOMER, CANCELLED_BY_CUSTOMER",
            "Cancelled_by_Customer, CANCELLED_BY_CUSTOMER"
        })
        void shouldReturnOptionalContainingEnumConstant_WhenMatchesExactIgnoringCase(String input, OrderStatus expectedOrderStatus) {
            assertThat(KiwiEnums.getIfPresentFuzzy(OrderStatus.class, input)).containsSame(expectedOrderStatus);
        }

        @ParameterizedTest
        @CsvSource({
            "' PENDING PAYMENT ', PENDING_PAYMENT",
            "PENDING____PAYMENT, PENDING_PAYMENT",
            "'\r\n\t   SHIPPED     \r\n\t  OUT\r\n\t\t    ', SHIPPED_OUT",
            "shipped-out, SHIPPED_OUT",
            "shipped__out, SHIPPED_OUT",
            "'  shipped - out ' , SHIPPED_OUT",
            "delivered.successfully, DELIVERED_SUCCESSFULLY",
            "' Delivered...--...Successfully  ', DELIVERED_SUCCESSFULLY",
            "'cancelled by customer', CANCELLED_BY_CUSTOMER",
        })
        void shouldReturnOptionalContainingEnumConstant_WhenMatchesAfterNormalizingInput(String input, OrderStatus expectedOrderStatus) {
             assertThat(KiwiEnums.getIfPresentFuzzy(OrderStatus.class, input)).containsSame(expectedOrderStatus);
        }

        @ParameterizedTest
        @CsvSource({
            "pendingPayment, PENDING_PAYMENT",
            "PendingPayment, PENDING_PAYMENT",
            "' PendingPayment ' , PENDING_PAYMENT",
            "ProcessingOrder, PROCESSING_ORDER",
            "returnRequested, RETURN_REQUESTED",
            "\r\nreturnRequested\t, RETURN_REQUESTED",
            "ShippedOut, SHIPPED_OUT"
        })
        void shouldReturnOptionalContainingEnumConstant_WhenMatchesForCamelCase(String input, OrderStatus expectedOrderStatus) {
             assertThat(KiwiEnums.getIfPresentFuzzy(OrderStatus.class, input)).containsSame(expectedOrderStatus);
        }

        @ParameterizedTest
        @CsvSource({
            "'Irish stepdance', IRISH_DANCE",
            "'Irish Stepdance', IRISH_DANCE",
            "'irish stepdance', IRISH_DANCE",
            "futbol, SOCCER",
            "Futbol, SOCCER",
            "FUTBOL, SOCCER",
            "'American football', FOOTBALL",
             "'American Football', FOOTBALL",
        })
        void shouldReturnOptionalContainingEnumConstant_WhenMatchesToString(String input, Sport expectedSport) {
            assertThat(KiwiEnums.getIfPresentFuzzy(Sport.class, input)).containsSame(expectedSport);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "_winter_",
            "the summer",
            "AUTUMN",
            "foo",
            "SUM_MER",
            "_SUMMER_",
            "Win.ter",
            "bar",
            "baz"
        })
        void shouldReturnEmptyOptionalWhenNoMatchingEnumConstantExists(String input) {
            assertThat(KiwiEnums.getIfPresentFuzzy(Season.class, input)).isEmpty();
        }
    }

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

   

    @Nested
    class LowercaseName {

        // IntelliJ warning about "No implicit conversion found to convert 'Xyz' to 'Enum<E>' is NOT correct"
        @SuppressWarnings("JUnitMalformedDeclaration")
        @ParameterizedTest
        @EnumSource(Season.class)
        @EnumSource(Color.class)
        @EnumSource(DrinkType.class)
        @EnumSource(OrderStatus.class)
        <E extends Enum<E>> void shouldConvertTheEnumNamesToLowercase(Enum<E> enumValue) {
            var value = KiwiEnums.lowercaseName(enumValue);
            assertThat(value).isEqualTo(enumValue.name().toLowerCase(Locale.ENGLISH));
        }

        @Test
        void shouldNotAllowNullEnumValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.lowercaseName(null))
                    .withMessage("enumValue must not be null");
        }

        @Test
        void shouldNotAllowNullEnumValueWithLocale() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.lowercaseName(null, Locale.ENGLISH))
                    .withMessage("enumValue must not be null");
        }

        @Test
        void shouldNotAllowNullLocale() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.lowercaseName(Season.FALL, null))
                    .withMessage("locale must not be null");
        }

        @SuppressWarnings("JUnitMalformedDeclaration")
        @ParameterizedTest
        @EnumSource(Season.class)
        @EnumSource(Color.class)
        @EnumSource(DrinkType.class)
        @EnumSource(OrderStatus.class)
        <E extends Enum<E>> void shouldConvertTheEnumNamesToLowercaseWithSpecificLocale(Enum<E> enumValue) {
            assertAll(
                    () -> {
                        var value = KiwiEnums.lowercaseName(enumValue, Locale.ENGLISH);
                        assertThat(value).isEqualTo(enumValue.name().toLowerCase(Locale.ENGLISH));
                    },
                    () -> {
                        var value = KiwiEnums.lowercaseName(enumValue, Locale.FRENCH);
                        assertThat(value).isEqualTo(enumValue.name().toLowerCase(Locale.FRENCH));
                    },
                    () -> {
                        var value = KiwiEnums.lowercaseName(enumValue, Locale.GERMAN);
                        assertThat(value).isEqualTo(enumValue.name().toLowerCase(Locale.GERMAN));
                    },
                    () -> {
                        var value = KiwiEnums.lowercaseName(enumValue, Locale.JAPANESE);
                        assertThat(value).isEqualTo(enumValue.name().toLowerCase(Locale.JAPANESE));
                    }
            );
        }
    }

    @Nested
    class ListOf {

        @Test
        void shouldThrowIllegalArgumentWhenEnumClassIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.listOf(null))
                    .withMessage("enumClass must not be null");
        }

        /**
         * @implNote This declares the argument type to be a class E that extends Enum, which works due
         * to type erasure. None of the arguments are classes that extend Enum since the point of
         * this test is to ensure we don't allow classes that aren't Enums.
         */
        @ParameterizedTest
        @ValueSource(classes = { Object.class, String.class, Map.class, List.class })
        <E extends Enum<E>> void shouldRequireEnumClasses(Class<E> clazz) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.listOf(clazz))
                    .withMessage("%s is not an enum", clazz);
        }

        @Test
        void shouldReturnListOfEnumConstants() {
            assertAll(
                    () -> {
                        var seasons = KiwiEnums.listOf(Season.class);
                        assertThat(seasons).containsExactly(Season.FALL, Season.WINTER, Season.SPRING, Season.SUMMER);
                    },
                    () -> {
                        var colors = KiwiEnums.listOf(Color.class);
                        assertThat(colors).containsExactly(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.INDIGO, Color.VIOLET);
                    }
            );
        }
    }

    @Nested
    class StreamOf {

        @Test
        void shouldThrowIllegalArgumentWhenEnumClassIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.streamOf(null))
                    .withMessage("enumClass must not be null");
        }

        /**
         * @implNote This declares the argument type to be a class E that extends Enum, which works due
         * to type erasure. None of the arguments are classes that extend Enum since the point of
         * this test is to ensure we don't allow classes that aren't Enums.
         */
        @ParameterizedTest
        @ValueSource(classes = { Object.class, String.class, Map.class, List.class })
        <E extends Enum<E>> void shouldRequireEnumClasses(Class<E> clazz) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums.streamOf(clazz))
                    .withMessage("%s is not an enum", clazz);
        }

        @Test
        void shouldReturnStreamOfEnumConstants() {
            assertAll(
                    () -> {
                        var seasons = KiwiEnums.streamOf(Season.class).toList();
                        assertThat(seasons).containsExactly(Season.FALL, Season.WINTER, Season.SPRING, Season.SUMMER);
                    },
                    () -> {
                        var colors = KiwiEnums.streamOf(Color.class).toList();
                        assertThat(colors).containsExactly(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.INDIGO, Color.VIOLET);
                    }
            );
        }
    }

    enum Color {
        RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET
    }

    enum DrinkType {
        BEER, JUICE, SODA, WATER, WINE
    }

    enum OrderStatus {
        PENDING_PAYMENT,
        PROCESSING_ORDER,
        SHIPPED_OUT,
        DELIVERED_SUCCESSFULLY,
        CANCELLED_BY_CUSTOMER,
        RETURN_REQUESTED
    }

    enum Season {
        FALL, WINTER, SPRING, SUMMER
    }

    enum Sport {
        IRISH_DANCE("Irish stepdance"),
        SOCCER("futbol"),
        FOOTBALL("American football");

        final String value;

        Sport(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
