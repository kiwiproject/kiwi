package org.kiwiproject.dropwizard.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.temporal.ChronoUnit;

@DisplayName("KiwiDropwizardDurations")
class KiwiDropwizardDurationsTest {

    @Nested
    class FromDropwizardDuration {

        @Test
        void shouldConvertSeconds() {
            assertThat(KiwiDropwizardDurations.fromDropwizardDuration(Duration.seconds(500)))
                    .isEqualTo(java.time.Duration.ofSeconds(500));
        }

        @Test
        void shouldConvertMaxLongMillis() {
            assertThat(KiwiDropwizardDurations.fromDropwizardDuration(Duration.milliseconds(Long.MAX_VALUE)))
                    .isEqualTo(java.time.Duration.ofMillis(Long.MAX_VALUE));
        }

        @Test
        void shouldConvertNanoDuration() {
            assertThat(KiwiDropwizardDurations.fromDropwizardDuration(Duration.nanoseconds(5)))
                    .isEqualTo(java.time.Duration.ofNanos(5));
        }

        @Test
        void shouldConvertZeroValue() {
            assertThat(KiwiDropwizardDurations.fromDropwizardDuration(Duration.seconds(0)))
                    .isEqualTo(java.time.Duration.ofNanos(0));
        }

        @ParameterizedTest
        @CsvSource({
                "1 nanosecond, 1, NANOS",
                "1000 nanoseconds, 1000, NANOS",
                "1 microsecond, 1, MICROS",
                "500 microseconds, 500, MICROS",
                "1 millisecond, 1, MILLIS",
                "42000 milliseconds, 42000, MILLIS",
                "1 second, 1, SECONDS",
                "420 seconds, 420, SECONDS",
                "84 minutes, 84, MINUTES",
                "42 hours, 42, HOURS",
                "1 day, 1, DAYS",
                "252 days, 252, DAYS"
        })
        void shouldConvertToJavaDuration(String durationSpec, long duration, ChronoUnit unit) {
            var dwDuration = Duration.parse(durationSpec);

            var expectedJavaDuration = java.time.Duration.of(duration, unit);

            assertThat(KiwiDropwizardDurations.fromDropwizardDuration(dwDuration)).isEqualTo(expectedJavaDuration);
        }
    }
}
