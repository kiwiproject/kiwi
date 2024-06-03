package org.kiwiproject.util.regex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@DisplayName("KiwiRegexes")
class KiwiRegexesTest {

    private static final Pattern FOO_BAZ_BAZ_PATTERN = Pattern.compile("^[FB]oo.*baz$");

    private static final String EXPECTED_EXCEPTION_MESSAGE = "No match found for pattern: " + FOO_BAZ_BAZ_PATTERN;

    private static final List<String> MATCHING_STRINGS = List.of(
            "Foo baz",
            "Foo bar baz",
            "Boo baz",
            "Boo bar baz",
            "Boo bar qux baz",
            "Foobarbaz"
    );

    private static final List<String> NON_MATCHING_STRINGS = List.of(
            "FOO baz",
            "Boo bazs",
            "Boobaz.",
            "Foo bar baz qux",
            "Whatever"
    );

    private static Stream<String> matchingStrings() {
        return MATCHING_STRINGS.stream();
    }

    private static Stream<String> nonMatchingStrings() {
        return NON_MATCHING_STRINGS.stream();
    }

    @Nested
    class ExtractMatchOrThrow {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.util.regex.KiwiRegexesTest#matchingStrings")
        void shouldReturnMatch(String input) {
            var match = KiwiRegexes.extractMatchOrThrow(FOO_BAZ_BAZ_PATTERN, input);
            assertThat(match).isEqualTo(input);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.util.regex.KiwiRegexesTest#nonMatchingStrings")
        void shouldThrowException_WhenThereIsNoMatch(String input) {
            assertThatThrownBy(() -> KiwiRegexes.extractMatchOrThrow(FOO_BAZ_BAZ_PATTERN, input))
                    .isExactlyInstanceOf(NoMatchesFoundException.class)
                    .hasMessage(EXPECTED_EXCEPTION_MESSAGE);
        }
    }

    @Nested
    class ExtractMatchOrNull {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.util.regex.KiwiRegexesTest#matchingStrings")
        void shouldReturnStringContainingMatch(String input) {
            var matchOrNull = KiwiRegexes.extractMatchOrNull(FOO_BAZ_BAZ_PATTERN, input);
            assertThat(matchOrNull).isEqualTo(input);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.util.regex.KiwiRegexesTest#nonMatchingStrings")
        void shouldReturnNull_WhenThereIsNoMatch(String input) {
            var matchOrNull = KiwiRegexes.extractMatchOrNull(FOO_BAZ_BAZ_PATTERN, input);
            assertThat(matchOrNull).isNull();
        }
    }

    @Nested
    class ExtractMatch {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.util.regex.KiwiRegexesTest#matchingStrings")
        void shouldReturnOptionalContainingMatch(String input) {
            var matchOpt = KiwiRegexes.extractMatch(FOO_BAZ_BAZ_PATTERN, input);
            assertThat(matchOpt).hasValue(input);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.util.regex.KiwiRegexesTest#nonMatchingStrings")
        void shouldReturnEmptyOptional_WhenThereIsNoMatch(String input) {
            var matchOpt = KiwiRegexes.extractMatch(FOO_BAZ_BAZ_PATTERN, input);
            assertThat(matchOpt).isEmpty();
        }
    }

    @Nested
    class ExtractMatches {

        @Test
        void shouldReturnStream_ContainingMatches() {
            var input = String.join(System.lineSeparator(), lines());
            var pattern = Pattern.compile("(yellow snake|orange fox|red fox|lazy brown)", Pattern.MULTILINE);

            var matchResultStream = KiwiRegexes.extractMatches(pattern, input);
            var matches = matchResultStream
                    .map(MatchResult::group)
                    .toList();
            assertThat(matches).containsExactly("red fox", "lazy brown");
        }

        @Test
        void shouldReturnEmptyStream_WhenThereAreNoMatches() {
            var input = String.join(System.lineSeparator(), lines());

            var pattern = Pattern.compile("(blue|orange|purple|lazy violet)", Pattern.MULTILINE);

            var matchResults = KiwiRegexes.extractMatches(pattern, input).toList();
            assertThat(matchResults).isEmpty();
        }

        private List<String> lines() {
            return List.of(
                    "The red fox",
                    "jumped over",
                    "the lazy brown",
                    "dog"
            );
        }
    }
}
