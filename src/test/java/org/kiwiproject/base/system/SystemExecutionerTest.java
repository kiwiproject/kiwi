package org.kiwiproject.base.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.DefaultEnvironment;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@DisplayName("SystemExecutioner")
@Slf4j
class SystemExecutionerTest {

    @Test
    void shouldRequireExecutionStrategy() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SystemExecutioner(null))
                .withMessage("executionStrategy must not be null");
    }

    @Test
    void shouldRequireWaitTime() {
        var executioner = new SystemExecutioner(ExecutionStrategies.noOp());
        assertThatIllegalArgumentException()
                .isThrownBy(() -> executioner.exit(1, null))
                .withMessage("waitTime must not be null");
    }

    @Test
    void shouldUseSystemExitStrategyByDefault() {
        var executioner = new SystemExecutioner();
        assertThat(executioner.getExecutionStrategy())
                .isExactlyInstanceOf(ExecutionStrategies.SystemExitExecutionStrategy.class);
    }

    @RepeatedTest(3)
    void shouldExitImmediately() {
        var executionStrategy = new ExecutionStrategies.ExitFlaggingExecutionStrategy();
        var executioner = new SystemExecutioner(executionStrategy);
        long startTime = System.nanoTime();
        executioner.exit(1);
        var elapsedNanos = System.nanoTime() - startTime;

        assertThat(executionStrategy.didExit()).isTrue();
        assertThat(executionStrategy.exitCode()).hasValue(1);

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        LOG.info("elapsedMillis: {} (elapsedNanos: {})", elapsedMillis, elapsedNanos);
        assertThat(elapsedMillis).isLessThan(10);
    }

    @RepeatedTest(3)
    void shouldExitWithWaitTime() {
        var executorService = Executors.newSingleThreadExecutor();

        try {
            var waitTime = Duration.ofMillis(25);
            var executionStrategy = new ExecutionStrategies.ExitFlaggingExecutionStrategy();
            var executioner = new SystemExecutioner(executionStrategy);
            var startTime = new AtomicLong();

            var executionFuture = executorService.submit(() -> {
                LOG.info("Calling executioner...");
                startTime.set(System.nanoTime());
                executioner.exit(1, waitTime);
            });

            await().pollInterval(5, TimeUnit.MILLISECONDS)
                    .atMost(ONE_SECOND)
                    .until(executionFuture::isDone);

            long elapsedNanos = System.nanoTime() - startTime.get();

            assertThat(executionStrategy.didExit())
                    .describedAs("Execution strategy exit() should have been called")
                    .isTrue();
            assertThat(executionStrategy.exitCode()).hasValue(1);

            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
            logElapsed(elapsedNanos, elapsedMillis);
            var fudgedWaitTimeMillis = waitTime.toMillis() - 2; // Allow for some slop in timing
            assertThat(elapsedMillis)
                    .describedAs("Elapsed millis must be greater than or equal to %d", waitTime.toMillis())
                    .isGreaterThanOrEqualTo(fudgedWaitTimeMillis);
        } finally {
            executorService.shutdownNow();
        }
    }

    @RepeatedTest(3)
    void shouldExitBeforeGivenWaitTime_WhenWaitingThreadInterrupted() {
        var executorService = Executors.newFixedThreadPool(2);

        try {
            var executionStrategy = new ExecutionStrategies.ExitFlaggingExecutionStrategy();
            var executioner = new SystemExecutioner(executionStrategy);
            var startTime = new AtomicLong();
            var executionFuture = executorService.submit(() -> {
                LOG.info("Calling executioner with 5 second wait");
                startTime.set(System.nanoTime());
                executioner.exit(1, Duration.ofSeconds(5));
            });

            var killerSleepTimeMillis = 100;
            var killerFuture = executorService.submit(() -> {
                LOG.info("Sleeping for {} milliseconds...", killerSleepTimeMillis);
                new DefaultEnvironment().sleepQuietly(killerSleepTimeMillis, TimeUnit.MILLISECONDS);
                LOG.info("I'm awake and will now interrupt executionThread");
                var canceled = executionFuture.cancel(true);
                LOG.info("executionFuture was canceled? {}", canceled);
            });

            await().pollInterval(25, TimeUnit.MILLISECONDS)
                    .atMost(ONE_SECOND)
                    .until(() -> executionFuture.isDone() && killerFuture.isDone() && executionStrategy.didExit());

            var elapsedNanos = System.nanoTime() - startTime.get();
            var elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
            logElapsed(elapsedNanos, elapsedMillis);
            var fudgedKillerSleepTimeMillis = killerSleepTimeMillis - 2; // Allow for some slop in timing
            assertThat(elapsedMillis)
                    .describedAs("Elapsed millis must be at least %d", fudgedKillerSleepTimeMillis)
                    .isGreaterThanOrEqualTo(fudgedKillerSleepTimeMillis);
        } finally {
            executorService.shutdownNow();
        }
    }

    private static void logElapsed(long elapsedNanos, long elapsedMillis) {
        LOG.info("Actual elapsed time: {} nanoseconds ; {} milliseconds", elapsedNanos, elapsedMillis);
    }
}
