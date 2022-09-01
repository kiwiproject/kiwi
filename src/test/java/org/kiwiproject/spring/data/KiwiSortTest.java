package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.kiwiproject.base.KiwiStrings.f;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.util.BlankStringArgumentsProvider;

import java.util.Locale;
import java.util.stream.Stream;

@DisplayName("KiwiSort")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiSortTest {

    @Test
    void shouldCreateNewInstanceUsing_DirectionEnumFactoryMethod(SoftAssertions softly) {
        var sort = KiwiSort.of("someProperty", KiwiSort.Direction.DESC);

        softly.assertThat(sort.getProperty()).isEqualTo("someProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("DESC");
        softly.assertThat(sort.isAscending()).isFalse();
        softly.assertThat(sort.isIgnoreCase()).isFalse();
    }

    @Test
    void shouldCreateNewInstanceUsing_DirectionStringFactoryMethod(SoftAssertions softly) {
        var sort = KiwiSort.of("someProperty", "asc");

        softly.assertThat(sort.getProperty()).isEqualTo("someProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("ASC");
        softly.assertThat(sort.isAscending()).isTrue();
        softly.assertThat(sort.isIgnoreCase()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            " , ASC",
            " '' , ASC",
            " ' ' , DESC",
            " lastName, ",
    })
    void shouldValidateArgumentsWhenUsing_DirectionEnumFactoryMethod(String property, KiwiSort.Direction direction) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiSort.of(property, direction));
    }

    @ParameterizedTest
    @CsvSource({
            " , ASC",
            " '' , ASC",
            " ' ' , DESC",
            " lastName, ",
            " lastName, DIAGONALLY",
    })
    void shouldValidateArgumentsWhenUsing_DirectionStringFactoryMethod(String property, String direction) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiSort.of(property, direction));
    }

    @Test
    void shouldSetIgnoringCase(SoftAssertions softly) {
        var sort = KiwiSort.of("otherProperty", KiwiSort.Direction.ASC).ignoringCase();

        softly.assertThat(sort.getProperty()).isEqualTo("otherProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("ASC");
        softly.assertThat(sort.isAscending()).isTrue();
        softly.assertThat(sort.isIgnoreCase()).isTrue();
    }

    @Test
    void shouldCallToString() {
        var sort = KiwiSort.of("someProperty", KiwiSort.Direction.DESC);
        var str = sort.toString();
        assertThat(str).isNotBlank();
    }

    @DisplayName("Direction#fromString")
    @Nested
    class DirectionFromString {

        @ParameterizedTest
        @ArgumentsSource(BlankStringArgumentsProvider.class)
        void shouldRequireNonBlankValue(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiSort.Direction.fromString(value))
                    .withMessage("direction value must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "a",
                "ascending",
                "as",
                "d",
                "descending",
                "des"
        })
        void shouldThrowIllegalArgument_ForInvalidValues(String value) {
            var expectedMessage = getExpectedExceptionMessage(value);

            assertThatIllegalArgumentException()
                    .describedAs("expecting IllegalArgumentException with same message as from Enum#valueOf")
                    .isThrownBy(() -> KiwiSort.Direction.fromString(value))
                    .withMessage(expectedMessage);
        }

        private String getExpectedExceptionMessage(String invalidValue) {
            try {
                KiwiSort.Direction.fromString(invalidValue);
            } catch (Exception e) {
                return e.getMessage();
            }

            throw new IllegalStateException(f("Expected value '%s' to be invalid!", invalidValue));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.spring.data.KiwiSortTest#directionEnumValues")
        void shouldCreateFromStringsInDefaultLocale(String value, KiwiSort.Direction expected) {
            assertThat(KiwiSort.Direction.fromString(value)).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.spring.data.KiwiSortTest#directionEnumValues")
        void shouldCreateFromSpecificLocale(String value, KiwiSort.Direction expected) {
            assertThat(KiwiSort.Direction.fromString(value, Locale.ENGLISH)).isEqualTo(expected);
        }
    }

    static Stream<Arguments> directionEnumValues() {
        return Stream.of(
                arguments("asc", KiwiSort.Direction.ASC),
                arguments(" asc ", KiwiSort.Direction.ASC),
                arguments("  \r\n\tasc\r\n\t  ", KiwiSort.Direction.ASC),
                arguments("ASC", KiwiSort.Direction.ASC),
                arguments("Asc", KiwiSort.Direction.ASC),
                arguments("AsC", KiwiSort.Direction.ASC),
                arguments("desc", KiwiSort.Direction.DESC),
                arguments("\r\ndesc\t \t \r\n", KiwiSort.Direction.DESC),
                arguments("DESC", KiwiSort.Direction.DESC),
                arguments("Desc", KiwiSort.Direction.DESC),
                arguments("DeSc", KiwiSort.Direction.DESC)
        );
    }
}
