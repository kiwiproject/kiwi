package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

@DisplayName("KiwiDurations")
class KiwiDurationsTest {

    @ParameterizedTest
    @MethodSource("durations")
    void shouldCheckIsPositive(Duration duration) {
        var expectPositive = duration.toNanos() > 0;
        assertThat(KiwiDurations.isPositive(duration)).isEqualTo(expectPositive);
    }

    @ParameterizedTest
    @MethodSource("durations")
    void shouldCheckIsPositiveOrZero(Duration duration) {
        var expectPositiveOrZero = duration.toNanos() >= 0;
        assertThat(KiwiDurations.isPositiveOrZero(duration)).isEqualTo(expectPositiveOrZero);
    }

    @ParameterizedTest
    @MethodSource("durations")
    void shouldCheckIsNegativeOrZero(Duration duration) {
        var expectNegativeOrZero = duration.toNanos() <= 0;
        assertThat(KiwiDurations.isNegativeOrZero(duration)).isEqualTo(expectNegativeOrZero);
    }

    static Stream<Duration> durations() {
        return Stream.of(
                Duration.ofNanos(randomPositiveInt()),
                Duration.ofNanos(0),
                Duration.ofNanos(randomPositiveInt()),

                Duration.ofMillis(randomPositiveInt()),
                Duration.ofMillis(0),
                Duration.ofMillis(randomNegativeInt()),

                Duration.ofSeconds(randomPositiveInt()),
                Duration.ofSeconds(0),
                Duration.ofSeconds(randomNegativeInt())
        );
    }

    private static int randomNegativeInt() {
        return -randomPositiveInt();
    }

    private static int randomPositiveInt() {
        return Math.abs(RandomGenerator.getDefault().nextInt());
    }
}
