package org.kiwiproject.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

@DisplayName("KiwiIterations")
class KiwiIterationsTest {

    @Nested
    class TimesWithIntConsumer {

        @Test
        void shouldNotExecuteActionWhenNIsZero() {
            var indices = new ArrayList<Integer>();
            KiwiIterations.times(0, indices::add);
            assertThat(indices).isEmpty();
        }

        @Test
        void shouldExecuteActionExactlyOnceWhenNIsOne() {
            var indices = new ArrayList<Integer>();
            KiwiIterations.times(1, indices::add);
            assertThat(indices).containsExactly(0);
        }

        @ParameterizedTest
        @ValueSource(ints = { 2, 5, 10 })
        void shouldExecuteActionNTimesWithCorrectIndices(int n) {
            var indices = new ArrayList<Integer>();
            KiwiIterations.times(n, indices::add);

            assertThat(indices)
                    .hasSize(n)
                    .containsExactlyElementsOf(IntStream.range(0, n).boxed().toList());
        }

        @Test
        void shouldThrowWhenNIsNegative() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIterations.times(-1, i -> {
                    }))
                    .withMessage("n must be positive or zero, but was -1");
        }

        @Test
        void shouldThrowWhenActionIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIterations.times(3, (IntConsumer) null))
                    .withMessage("action must not be null");
        }
    }

    @Nested
    class TimesWithRunnable {

        @Test
        void shouldNotExecuteActionWhenNIsZero() {
            var callCount = new AtomicInteger();
            KiwiIterations.times(0, callCount::incrementAndGet);
            assertThat(callCount.get()).isZero();
        }

        @Test
        void shouldExecuteActionExactlyOnceWhenNIsOne() {
            var callCount = new AtomicInteger();
            KiwiIterations.times(1, callCount::incrementAndGet);
            assertThat(callCount.get()).isOne();
        }

        @ParameterizedTest
        @ValueSource(ints = { 2, 5, 10 })
        void shouldExecuteActionNTimes(int n) {
            var callCount = new AtomicInteger();
            KiwiIterations.times(n, callCount::incrementAndGet);
            assertThat(callCount.get()).isEqualTo(n);
        }

        @Test
        void shouldThrowWhenNIsNegative() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIterations.times(-1, () -> {
                    }))
                    .withMessage("n must be positive or zero, but was -1");
        }

        @Test
        void shouldThrowWhenActionIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIterations.times(3, (Runnable) null))
                    .withMessage("action must not be null");
        }
    }
}
