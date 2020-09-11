package org.kiwiproject.util.regex;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Static helper methods related to regular expression processing.
 */
@UtilityClass
public class KiwiRegexes {

    /**
     * Extracts a regex match as a String, or return {@code null} if no match found.
     *
     * @param pattern the {@link Pattern}
     * @param input   the input to match against
     * @return a string containing the match, or {@code null} if no match found
     */
    public static String extractMatchOrNull(Pattern pattern, String input) {
        return extractMatch(pattern, input).orElse(null);
    }

    /**
     * Extracts a regex match as a String, or throws an exception if no match found.
     *
     * @param pattern the {@link Pattern}
     * @param input   the input to match against
     * @return a string containing the match
     * @throws NoMatchesFoundException if the input does not match the pattern
     */
    public static String extractMatchOrThrow(Pattern pattern, String input) {
        return extractMatch(pattern, input)
                .orElseThrow(() -> NoMatchesFoundException.forPattern(pattern));
    }

    /**
     * Extracts a regex match as a String.
     *
     * @param pattern the {@link Pattern}
     * @param input   the input to match against
     * @return an Optional containing the match, or empty Optional if no match
     */
    public static Optional<String> extractMatch(Pattern pattern, String input) {
        return extractMatches(pattern, input)
                .map(MatchResult::group)
                .findFirst();
    }

    /**
     * Match an input String against a {@link Pattern} and convert to a {@link Stream} of {@link MatchResult}.
     *
     * @param pattern the {@link Pattern}
     * @param input   the input to match against
     * @return a stream of {@link MatchResult}
     */
    public static Stream<MatchResult> extractMatches(Pattern pattern, String input) {
        return StreamSupport.stream(new MatcherSpliterator(pattern.matcher(input)), false);
    }
}
