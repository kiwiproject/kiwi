package org.kiwiproject.regex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@DisplayName("MatchSpliterator")
class MatchSpliteratorTest {

    @Nested
    class Constructor {

        @Test
        void shouldConstructFromMatcher() {
            var input = "The quick brown fox jumped over the fence and the lazy brown dog";
            var matcher = Pattern.compile("the").matcher(input);
            var matchSpliterator = new MatchSpliterator(matcher);

            var startIndexes = new ArrayList<Integer>();
            matchSpliterator.forEachRemaining(matchResult -> startIndexes.add(matchResult.start()));

            assertThat(startIndexes).containsExactly(32, 46);
        }

        @Test
        void shouldNotAllowNullMatcher() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new MatchSpliterator(null));
        }
    }

    @Nested
    class StreamInstanceMethod {

        @Test
        void shouldCreateMatchResultStream() {
            var input = "The quick brown fox jumped over the fence and the lazy brown dog";
            var matcher = Pattern.compile("the|(lazy cat)", Pattern.CASE_INSENSITIVE).matcher(input);
            var matchSpliterator = new MatchSpliterator(matcher);

            var matchResults = matchSpliterator.stream().toList();
            assertThat(matchResults)
                    .extracting(MatchResult::group, MatchResult::start)
                    .containsExactly(
                            tuple("The", 0),
                            tuple("the", 32),
                            tuple("the", 46)
                    );
        }
    }

    @Nested
    class StreamFromRegex {

        @Test
        void shouldCreateMatchResultStream_FromStringRegularExpression() {
            var input = "The quick brown fox jumped over the fence and the lazy brown dog";
            var matchResults = MatchSpliterator.stream("the", input).toList();

            assertThat(matchResults)
                    .extracting(MatchResult::group, MatchResult::start)
                    .containsExactly(
                            tuple("the", 32),
                            tuple("the", 46)
                    );
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotAllowBlankRegex(String regex) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MatchSpliterator.stream(regex, "the input"));
        }
    }

    @Nested
    class StreamFromPattern {

        @Test
        void shouldCreateMatchResultStream_FromPattern() {
            var input = "The quick brown fox jumped over the fence and the lazy brown dog and the lazy cat";
            var pattern = Pattern.compile("the|(lazy cat)", Pattern.CASE_INSENSITIVE);
            var matchResults = MatchSpliterator.stream(pattern, input).toList();

            assertThat(matchResults)
                    .extracting(MatchResult::group, MatchResult::start)
                    .containsExactly(
                            tuple("The", 0),
                            tuple("the", 32),
                            tuple("the", 46),
                            tuple("the", 69),
                            tuple("lazy cat", 73)
                    );
        }

        @Test
        void shouldNotAllowNullPattern() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MatchSpliterator.stream((Pattern) null, "the input"));
        }
    }

    @Nested
    class StreamFromMatcher {

        @Test
        void shouldCreateMatchResultStream_FromMatcher() {
            var pattern = Pattern.compile("(\\d{1,3}).(\\d{1,3}).(\\d{1,3}).(\\d{1,3})");
            var matcher = pattern.matcher("192.168.1.108  192.168.1.107  192.168.5.102  192.168.6.112");
            var matchResults = MatchSpliterator.stream(matcher);

            assertThat(matchResults)
                    .extracting(MatchResult::group)
                    .containsExactly(
                            "192.168.1.108",
                            "192.168.1.107",
                            "192.168.5.102",
                            "192.168.6.112");
        }

        @Test
        void shouldNotAllowNullMatcher() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MatchSpliterator.stream(null));
        }
    }
}