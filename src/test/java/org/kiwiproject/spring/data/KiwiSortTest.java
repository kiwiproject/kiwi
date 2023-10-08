package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.json.JsonHelper.OutputFormat;
import org.kiwiproject.spring.data.KiwiSort.Direction;
import org.kiwiproject.util.BlankStringSource;

import java.util.stream.Stream;

@DisplayName("KiwiSort")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiSortTest {

    @Test
    void shouldCreateNewInstanceUsing_DirectionEnumFactoryMethod(SoftAssertions softly) {
        var sort = KiwiSort.of("someProperty", Direction.DESC);

        softly.assertThat(sort.getProperty()).isEqualTo("someProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("DESC");
        softly.assertThat(sort.getDirectionObject()).isEqualTo(Direction.DESC);
        softly.assertThat(sort.isAscending()).isFalse();
        softly.assertThat(sort.isDescending()).isTrue();
        softly.assertThat(sort.isIgnoreCase()).isFalse();
    }

    @Test
    void shouldCreateNewInstanceUsing_DirectionStringFactoryMethod(SoftAssertions softly) {
        var sort = KiwiSort.of("someProperty", "asc");

        softly.assertThat(sort.getProperty()).isEqualTo("someProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("ASC");
        softly.assertThat(sort.getDirectionObject()).isEqualTo(Direction.ASC);
        softly.assertThat(sort.isAscending()).isTrue();
        softly.assertThat(sort.isDescending()).isFalse();
        softly.assertThat(sort.isIgnoreCase()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            " , ASC",
            " '' , ASC",
            " ' ' , DESC",
            " lastName, ",
    })
    void shouldValidateArgumentsWhenUsing_DirectionEnumFactoryMethod(String property, Direction direction) {
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
    void shouldCreateNewAscendingInstance(SoftAssertions softly) {
        var sort = KiwiSort.ofAscending("sortedProperty");

        softly.assertThat(sort.getProperty()).isEqualTo("sortedProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("ASC");
        softly.assertThat(sort.getDirectionObject()).isEqualTo(Direction.ASC);
        softly.assertThat(sort.isAscending()).isTrue();
        softly.assertThat(sort.isDescending()).isFalse();
        softly.assertThat(sort.isIgnoreCase()).isFalse();
    }

    @ParameterizedTest
    @BlankStringSource
    void shouldNotPermitBlankPropertyForAscendingSort(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiSort.ofAscending(value));
    }

    @Test
    void shouldCreateNewDescendingInstance(SoftAssertions softly) {
        var sort = KiwiSort.ofDescending("sortedProperty");

        softly.assertThat(sort.getProperty()).isEqualTo("sortedProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("DESC");
        softly.assertThat(sort.getDirectionObject()).isEqualTo(Direction.DESC);
        softly.assertThat(sort.isAscending()).isFalse();
        softly.assertThat(sort.isDescending()).isTrue();
        softly.assertThat(sort.isIgnoreCase()).isFalse();
    }

    @ParameterizedTest
    @BlankStringSource
    void shouldNotPermitBlankPropertyForDescendingSort(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiSort.ofDescending(value));
    }

    @Test
    void shouldSetIgnoringCase(SoftAssertions softly) {
        var sort = KiwiSort.of("otherProperty", Direction.ASC).ignoringCase();

        softly.assertThat(sort.getProperty()).isEqualTo("otherProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("ASC");
        softly.assertThat(sort.getDirectionObject()).isEqualTo(Direction.ASC);
        softly.assertThat(sort.isAscending()).isTrue();
        softly.assertThat(sort.isDescending()).isFalse();
        softly.assertThat(sort.isIgnoreCase()).isTrue();
    }

    @Test
    void shouldCallToString() {
        var sort = KiwiSort.of("someProperty", Direction.DESC);
        var str = sort.toString();
        assertThat(str).isNotBlank();
    }

    @DisplayName("Direction#fromString")
    @Nested
    class DirectionFromString {

        @ParameterizedTest
        @BlankStringSource
        void shouldRequireNonBlankValue(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Direction.fromString(value))
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
            assertThatIllegalArgumentException()
                    .describedAs("expecting IllegalArgumentException with same message as from Enum#valueOf")
                    .isThrownBy(() -> Direction.fromString(value))
                    .withMessage("no matching enum constant found in Direction");
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.spring.data.KiwiSortTest#directionEnumValues")
        void shouldCreateFromStrings(String value, Direction expected) {
            assertThat(Direction.fromString(value)).isEqualTo(expected);
        }
    }

    @Nested
    class DirectionEnum {

        @Test
        void shouldHaveExpectedAscendingValue() {
            assertThat(Direction.ASC.isAscending()).isTrue();
            assertThat(Direction.DESC.isAscending()).isFalse();
        }

        @Test
        void shouldHaveExpectedDescendingValue() {
            assertThat(Direction.ASC.isDescending()).isFalse();
            assertThat(Direction.DESC.isDescending()).isTrue();
        }
    }

    static Stream<Arguments> directionEnumValues() {
        return Stream.of(
                arguments("asc", Direction.ASC),
                arguments(" asc ", Direction.ASC),
                arguments("  \r\n\tasc\r\n\t  ", Direction.ASC),
                arguments("ASC", Direction.ASC),
                arguments("Asc", Direction.ASC),
                arguments("AsC", Direction.ASC),
                arguments("desc", Direction.DESC),
                arguments("\r\ndesc\t \t \r\n", Direction.DESC),
                arguments("DESC", Direction.DESC),
                arguments("Desc", Direction.DESC),
                arguments("DeSc", Direction.DESC)
        );
    }

    @Test
    void shouldSerializeToJson(SoftAssertions softly) {
        var jsonHelper = JsonHelper.newDropwizardJsonHelper();
        var json = jsonHelper.toJson(KiwiSort.ofAscending("lastName"), OutputFormat.PRETTY);

        softly.assertThat(json).containsIgnoringWhitespaces("\"direction\" : \"ASC\"");
        softly.assertThat(json).containsIgnoringWhitespaces("\"property\" : \"lastName\"");
        softly.assertThat(json).containsIgnoringWhitespaces("\"ignoreCase\" : false");
        softly.assertThat(json).containsIgnoringWhitespaces("\"ascending\" : true");
        softly.assertThat(json).containsIgnoringWhitespaces("\"descending\" : false");
        softly.assertThat(json).doesNotContain("\"directionObject\" :");
    }
}
