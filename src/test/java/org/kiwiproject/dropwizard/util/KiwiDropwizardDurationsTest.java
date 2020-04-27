package org.kiwiproject.dropwizard.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    }
}
