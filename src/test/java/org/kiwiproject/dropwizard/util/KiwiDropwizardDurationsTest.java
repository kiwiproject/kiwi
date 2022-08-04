package org.kiwiproject.dropwizard.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
                "1 nanosecond, 1, NANOSECONDS",
                "1000 nanoseconds, 1000, NANOSECONDS",
                "1 microsecond, 1, MICROSECONDS",
                "500 microseconds, 500, MICROSECONDS",
                "1 millisecond, 1, MILLISECONDS",
                "42000 milliseconds, 42000, MILLISECONDS",
                "1 second, 1, SECONDS",
                "420 seconds, 420, SECONDS",
                "84 minutes, 84, MINUTES",
                "42 hours, 42, HOURS",
                "1 day, 1, DAYS",
                "252 days, 252, DAYS",
        })
        void shouldConvertToJavaDuration(String durationSpec, long duration, TimeUnit unit) {
            var dwDuration = Duration.parse(durationSpec);

            var nanos = TimeUnit.NANOSECONDS.convert(duration, unit);
            var expectedJavaDuration = java.time.Duration.ofNanos(nanos);

            assertThat(KiwiDropwizardDurations.fromDropwizardDuration(dwDuration)).isEqualTo(expectedJavaDuration);
        }
    }
}
