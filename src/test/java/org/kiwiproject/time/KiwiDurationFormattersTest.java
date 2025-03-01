package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@DisplayName("KiwiDurationFormatters")
class KiwiDurationFormattersTest {

    private static final String SEVEN_HOURS_TWENTY_FIVE_MINUTES = "7 hours 25 minutes";

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
    }

    @Nested
    class FormatMillisecondsDurationWords {

        @Test
        void shouldFormatMilliseconds() {
            var words = KiwiDurationFormatters.formatMillisecondDurationWords(Duration.ofMinutes(445).toMillis());
            assertThat(words).isEqualTo(SEVEN_HOURS_TWENTY_FIVE_MINUTES);
        }
    }
}
