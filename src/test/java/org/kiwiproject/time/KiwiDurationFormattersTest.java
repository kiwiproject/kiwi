package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@DisplayName("KiwiDurationFormatters")
class KiwiDurationFormattersTest {

    private static final String SEVEN_HOURS_TWENTY_FIVE_MINUTES = "7 hours 25 minutes";
    private static final String ONE_SECOND = "1 second";

    @SuppressWarnings("removal")
    @Nested
    class FormatDurationWordsDeprecated {

        @Test
        void shouldFormatMilliseconds() {
            var words = KiwiDurationFormatters.formatDurationWords(Duration.ofMinutes(445).toMillis());
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }

        @Test
        void shouldFormatJavaDuration() {
            var words = KiwiDurationFormatters.formatDurationWords(Duration.ofMinutes(445));
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }

        @Test
        void shouldFormatNullJavaDuration_AsLiteralNull() {
            var words = KiwiDurationFormatters.formatDurationWords((Duration) null);
            assertThat(words).isEqualTo("null");
        }

        @Test
        void shouldFormatDropwizardDuration() {
            var words = KiwiDurationFormatters.formatDurationWords(io.dropwizard.util.Duration.minutes(445));
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }

        @Test
        void shouldFormatNullDropwizardDuration_AsLiteralNull() {
            var words = KiwiDurationFormatters.formatDurationWords((io.dropwizard.util.Duration) null);
            assertThat(words).isEqualTo("null");
        }
    }

    @Nested
    class FormatJavaDurationWords {

        @Test
        void shouldFormatJavaDuration() {
            var words = KiwiDurationFormatters.formatJavaDurationWords(Duration.ofMinutes(445));
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }

        @Test
        void shouldFormatNullJavaDuration_AsLiteralNull() {
            var words = KiwiDurationFormatters.formatJavaDurationWords(null);
            assertThat(words).isEqualTo("null");
        }

        @ParameterizedTest
        @ValueSource(longs = {-500, -10, -1})
        void shouldRequireNonNegativeDuration(long seconds) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDurationFormatters.formatJavaDurationWords(Duration.ofSeconds(seconds)))
                    .withMessage("duration must not be negative");
        }

        @Test
        void shouldFormatZeroDuration() {
            assertThat(KiwiDurationFormatters.formatJavaDurationWords(Duration.ZERO)).isEqualTo("0 seconds");
        }

        @Test
        void shouldFormatOneSecondDuration() {
            assertThat(KiwiDurationFormatters.formatJavaDurationWords(Duration.ofMillis(1000)))
                    .isEqualTo(ONE_SECOND);
        }

        @ParameterizedTest
        @ValueSource(longs = {1, 2, 50, 500, 999})
        void shouldFormatMillisecondDurations(long milliseconds) {
            var units = milliseconds == 1 ? " millisecond" : " milliseconds";
            assertThat(KiwiDurationFormatters.formatJavaDurationWords(Duration.ofMillis(milliseconds)))
                    .isEqualTo(milliseconds + units);
        }

        @ParameterizedTest
        @ValueSource(longs = { 1, 2, 25_000, 50_000, 500_000, 999_999 })
        void shouldFormatNanosecondDurations(long nanoseconds) {
            var units = nanoseconds == 1 ? " nanosecond" : " nanoseconds";
            assertThat(KiwiDurationFormatters.formatJavaDurationWords(Duration.ofNanos(nanoseconds)))
                    .isEqualTo(nanoseconds + units);
        }
    }

    @Nested
    class FormatDropwizardDurationWords {

        @Test
        void shouldFormatDropwizardDuration() {
            var words = KiwiDurationFormatters.formatDropwizardDurationWords(io.dropwizard.util.Duration.minutes(445));
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }

        @Test
        void shouldFormatNullDropwizardDuration_AsLiteralNull() {
            var words = KiwiDurationFormatters.formatDropwizardDurationWords(null);
            assertThat(words).isEqualTo("null");
        }

        @ParameterizedTest
        @ValueSource(longs = { -500, -10, -1 })
        void shouldRequireNonNegativeDuration(long seconds) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDurationFormatters.formatDropwizardDurationWords(io.dropwizard.util.Duration.seconds(seconds)))
                    .withMessage("duration must not be negative");
        }

        @Test
        void shouldFormatZeroDuration() {
            assertThat(KiwiDurationFormatters.formatDropwizardDurationWords(io.dropwizard.util.Duration.minutes(0)))
                    .isEqualTo("0 seconds");
        }

        @Test
        void shouldFormatOneSecondDuration() {
            assertThat(KiwiDurationFormatters.formatDropwizardDurationWords(io.dropwizard.util.Duration.milliseconds(1000)))
                    .isEqualTo(ONE_SECOND);
        }

        @ParameterizedTest
        @ValueSource(longs = { 1, 2, 50, 500, 999 })
        void shouldFormatMillisecondDurations(long milliseconds) {
            var units = milliseconds == 1 ? " millisecond" : " milliseconds";
            assertThat(KiwiDurationFormatters.formatDropwizardDurationWords(io.dropwizard.util.Duration.milliseconds(milliseconds)))
                    .isEqualTo(milliseconds + units);
        }

        @ParameterizedTest
        @ValueSource(longs = { 1, 2, 25_000, 50_000, 500_000, 999_999 })
        void shouldFormatNanosecondDurations(long nanoseconds) {
            var units = nanoseconds == 1 ? " nanosecond" : " nanoseconds";
            assertThat(KiwiDurationFormatters.formatDropwizardDurationWords(io.dropwizard.util.Duration.nanoseconds(nanoseconds)))
                    .isEqualTo(nanoseconds + units);
        }
    }

    @Nested
    class FormatMillisecondDurationWords {

        @Test
        void shouldFormatMilliseconds() {
            var words = KiwiDurationFormatters.formatMillisecondDurationWords(Duration.ofMinutes(445).toMillis());
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }

        @ParameterizedTest
        @ValueSource(longs = { -500, -10, -1 })
        void shouldRequireNonNegativeDuration(long milliseconds) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDurationFormatters.formatMillisecondDurationWords(milliseconds))
                    .withMessage("duration must not be negative");
        }

        @Test
        void shouldFormatZeroDuration() {
            assertThat(KiwiDurationFormatters.formatMillisecondDurationWords(0)).isEqualTo("0 milliseconds");
        }

        @Test
        void shouldFormatOneSecondDuration() {
            assertThat(KiwiDurationFormatters.formatMillisecondDurationWords(1000))
                    .isEqualTo(ONE_SECOND);
        }

        @ParameterizedTest
        @ValueSource(longs = { 1, 2, 50, 500, 999 })
        void shouldFormatMillisecondDurations(long milliseconds) {
            var units = milliseconds == 1 ? " millisecond" : " milliseconds";
            assertThat(KiwiDurationFormatters.formatMillisecondDurationWords(milliseconds))
                    .isEqualTo(milliseconds + units);
        }
    }

    @Nested
    class FormatNanosecondDurationWords {

        @Test
        void shouldFormatNanoseconds() {
            var words = KiwiDurationFormatters.formatNanosecondDurationWords(Duration.ofMinutes(445).toNanos());
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }

        @ParameterizedTest
        @ValueSource(longs = { -500, -10, -1 })
        void shouldRequireNonNegativeDuration(long nanoseconds) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDurationFormatters.formatMillisecondDurationWords(nanoseconds))
                    .withMessage("duration must not be negative");
        }

        @Test
        void shouldFormatZeroDuration() {
            assertThat(KiwiDurationFormatters.formatNanosecondDurationWords(0)).isEqualTo("0 nanoseconds");
        }

        @Test
        void shouldFormatOneSecondDuration() {
            assertThat(KiwiDurationFormatters.formatNanosecondDurationWords(TimeUnit.SECONDS.toNanos(1)))
                    .isEqualTo(ONE_SECOND);
        }


        @ParameterizedTest
        @ValueSource(longs = { 1, 2, 50, 500, 999 })
        void shouldFormatMillisecondDurations(long milliseconds) {
            var nanos = TimeUnit.MILLISECONDS.toNanos(milliseconds);
            var units = milliseconds == 1 ? " millisecond" : " milliseconds";
            assertThat(KiwiDurationFormatters.formatNanosecondDurationWords(nanos))
                    .isEqualTo(milliseconds + units);
        }


        @ParameterizedTest
        @ValueSource(longs = { 1, 2, 25_000, 50_000, 500_000, 999_999 })
        void shouldFormatNanosecondDurations(long nanoseconds) {
            var units = nanoseconds == 1 ? " nanosecond" : " nanoseconds";
            assertThat(KiwiDurationFormatters.formatNanosecondDurationWords(nanoseconds))
                    .isEqualTo(nanoseconds + units);
        }
    }
}
