package org.kiwiproject.base;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class CatchingRunnableTest {

    private AtomicInteger callCount;

    @BeforeEach
    void setUp() {
        callCount = new AtomicInteger();
    }

    @Test
    void testRun_WhenNoExceptions() {
        CatchingRunnable safeRunnable = callCount::incrementAndGet;
        safeRunnable.run();

        assertThat(callCount).hasValue(1);
    }

    @Test
    void testRun_WhenExceptionIsThrown() {
        CatchingRunnable safeRunnable = () -> {
            callCount.incrementAndGet();
            throw new IllegalStateException("The bar is not ready to baz because corge is unavailable");
        };

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
    }

    @Test
    void testRun_WhenExceptionThrown_AndErrorHandlingException() {
        var handleExceptionCount = new AtomicInteger();
        var safeRunnable = new CatchingRunnable() {
            @Override
            public void runSafely() {
                callCount.incrementAndGet();
                throw new IllegalStateException("This is the original error");
            }

            @Override
            public void handleExceptionSafely(Throwable throwable) {
                handleExceptionCount.incrementAndGet();
                throw new RuntimeException("This is the error handling the error!");
            }
        };

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
        assertThat(handleExceptionCount).hasValue(1);
    }

    @Test
    void testRun_WhenErrorIsThrown() {
        CatchingRunnable safeRunnable = () -> {
            callCount.incrementAndGet();
            throw new Error("The bar is not ready to baz because corge is unavailable");
        };

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
    }

    @SuppressWarnings("CatchMayIgnoreException")
    @Test
    void testRun_WithScheduledExecutor_DoesNotTerminateExecution() {
        var scheduledExecutorService = Executors.newScheduledThreadPool(1);
        var errorCount = new AtomicInteger();

        Future<?> scheduledFuture = null;
        try {
            CatchingRunnable safeRunnable = () -> {
                callCount.incrementAndGet();
                if (anErrorOccurred()) {
                    errorCount.incrementAndGet();
                    throw new RuntimeException("Chance dictated this...");
                }
            };
            LOG.debug("Perform action at: {}", System.currentTimeMillis());
            scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                    safeRunnable, 0L, 10L, TimeUnit.MILLISECONDS);

            var minimumCallCount = 15;
            await().atMost(FIVE_SECONDS).until(() -> callCount.get() >= minimumCallCount);

            LOG.debug("call count: {}, error count: {}", callCount.get(), errorCount.get());

            assertThat(scheduledFuture)
                    .describedAs("Should still be executing")
                    .isNotCancelled();
            assertThat(callCount).hasValueGreaterThanOrEqualTo(minimumCallCount);
            assertThat(errorCount)
                    .describedAs("We should have received at least one error for proper verification")
                    .hasPositiveValue();

        } catch (Exception e) {
            fail("No exceptions should have escaped", e);
        } finally {
            if (nonNull(scheduledFuture)) {
                scheduledFuture.cancel(true);
            }
        }
    }

    private boolean anErrorOccurred() {
        return ThreadLocalRandom.current().nextInt(10) < 5;
    }
}