package org.kiwiproject.base;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@DisplayName("CatchingRunnable")
@Slf4j
class CatchingRunnableTest {

    private AtomicInteger callCount;

    @BeforeEach
    void setUp() {
        callCount = new AtomicInteger();
    }

    @Test
    void shouldRunNormally_WhenNoExceptionIsThrown() {
        CatchingRunnable safeRunnable = callCount::incrementAndGet;
        safeRunnable.run();

        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldSuppressIt_WhenAnExceptionIsThrown() {
        CatchingRunnable safeRunnable = () -> {
            callCount.incrementAndGet();
            throw new IllegalStateException("The bar is not ready to baz because corge is unavailable");
        };

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldSuppressIt_WhenAnExceptionThrown_AndErrorOccursHandlingTheException() {
        var handleExceptionCount = new AtomicInteger();
        var safeRunnable = new CatchingRunnable() {
            @Override
            public void runSafely() {
                callCount.incrementAndGet();
                throw new IllegalStateException("This is the original error");
            }

            @Override
            public void handleExceptionSafely(Exception exception) {
                handleExceptionCount.incrementAndGet();
                throw new RuntimeException("This is the error handling the error!");
            }
        };

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
        assertThat(handleExceptionCount).hasValue(1);
    }

    @Test
    void shouldNotSuppressIt_WhenErrorIsThrown() {
        CatchingRunnable safeRunnable = () -> {
            callCount.incrementAndGet();
            throw new Error("The bar is not ready to baz because corge is unavailable");
        };

        assertThatThrownBy(safeRunnable::run)
                .describedAs("Error should not be caught by CatchingRunnable")
                .isExactlyInstanceOf(Error.class)
                .hasMessageStartingWith("The bar is not ready");
        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldNotSuppressIt_WhenSneakyCatchingRunnable_ThrowsThrowable() {
        var runnable = new SneakyThrowableThrowingRunnable();

        assertThatThrownBy(runnable::run)
                .describedAs("Sneakily thrown Throwable should not be caught by CatchingRunnable")
                .isExactlyInstanceOf(Throwable.class)
                .hasMessage("I am really, really, very bad");
        assertThat(runnable.callCount).hasValue(1);
    }

    private static class SneakyThrowableThrowingRunnable implements CatchingRunnable {

        AtomicInteger callCount = new AtomicInteger();

        @SneakyThrows
        @Override
        public void runSafely() {
            callCount.incrementAndGet();
            throw new Throwable("I am really, really, very bad");
        }
    }

    @SuppressWarnings("CatchMayIgnoreException")
    @Test
    void shouldNotTerminateExecution_OfScheduledExecutor_WhenExceptionsAreThrown() throws InterruptedException {
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
            LOG.debug("Perform action at: {}", Instant.now());
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
            scheduledExecutorService.shutdown();
            var terminated = scheduledExecutorService.awaitTermination(100, TimeUnit.MILLISECONDS);
            LOG.info("Terminated successfully: {}", terminated);
        }
    }

    private boolean anErrorOccurred() {
        return ThreadLocalRandom.current().nextInt(10) < 5;
    }
}