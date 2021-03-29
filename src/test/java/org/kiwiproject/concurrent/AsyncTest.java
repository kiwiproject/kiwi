package org.kiwiproject.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.concurrent.Async.Mode;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

class AsyncTest {

    @AfterEach
    void tearDown() {
        Async.setUnitTestAsyncMode(Mode.ENABLED);
    }

    @Nested
    class SetUnitTestAsyncMode {

        @Test
        void shouldRequireNonNullArgument() {
            assertThatThrownBy(() -> Async.setUnitTestAsyncMode(null))
                    .isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }

    /**
     * These tests call runAsync(Runnable) since it simply delegates to doAsync(Runnable).
     */
    @Nested
    class DoAsyncWithRunnable {

        @Test
        void shouldNotBlock_WhenAsyncModeIsEnabled() {
            var task = new ConcurrentTask();
            CompletableFuture<Void> future = Async.runAsync(task::run);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();
            confirmCompletion(task);

            assertThat(future).isCompleted();
        }

        @Test
        void shouldBlock_WhenAsyncModeIsDisabled() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);
            var task = new ConcurrentTask();
            CompletableFuture<Void> future = Async.runAsync(task::run);

            // verify that immediately after triggering run with mode=DISABLED, the count is 1
            assertThat(task.getCurrentCount()).isOne();

            assertThat(future).isCompleted();
        }
    }

    /**
     * These tests call runAsync(Runnable, Executor) since it simply delegates to doAsync(Runnable, Executor).
     */
    @Nested
    class DoAsyncWithRunnableAndExecutor {

        private ExecutorService executor;

        @BeforeEach
        void setUp() {
            executor = Executors.newSingleThreadExecutor();
        }

        @AfterEach
        void tearDown() {
            executor.shutdownNow();
        }

        @Test
        void shouldNotBlock_WhenAsyncModeIsEnabled() {
            var task = new ConcurrentTask();
            CompletableFuture<Void> future = Async.runAsync(task::run, executor);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();
            confirmCompletion(task);

            assertThat(future).isCompleted();
        }

        @Test
        void shouldBlock_WhenAsyncModeIsDisabled() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);
            var task = new ConcurrentTask();
            CompletableFuture<Void> future = Async.runAsync(task::run, executor);

            // verify that immediately after triggering run with mode=DISABLED, the count is 1
            assertThat(task.getCurrentCount()).isOne();

            assertThat(future).isCompleted();
        }
    }

    @Nested
    class WaitIfAsyncDisabled {

        @Test
        void shouldNotThrowAnException_WhenFutureCompletesExceptionally() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);

            var future = CompletableFuture.failedFuture(new RuntimeException("boom"));

            assertThatCode(() -> Async.waitIfAsyncDisabled(future)).doesNotThrowAnyException();
        }
    }

    /**
     * These tests call supplyAsync(Runnable) since it simply delegates to supplyAsync(Runnable).
     */
    @Nested
    class DoAsyncWithSupplier {

        @Test
        void shouldNotBlock_WhenAsyncModeIsEnabled() {
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.supplyAsync(task::supply);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();
            confirmCompletion(task);

            assertThat(future).isCompleted();
            assertThat(future.getNow(-1)).isEqualTo(task.getCurrentCount());
        }

        @Test
        void shouldBlock_WhenAsyncModeIsDisabled() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.supplyAsync(task::supply);

            // verify that immediately after triggering run with mode=DISABLED, the count is 1
            assertThat(task.getCurrentCount()).isOne();

            assertThat(future).isCompleted();
            assertThat(future.getNow(-1)).isEqualTo(task.getCurrentCount());
        }
    }

    /**
     * These tests call supplyAsync(Runnable, Executor) since it simply delegates to supplyAsync(Runnable, Executor).
     */
    @Nested
    class DoAsyncWithSupplierAndExecutor {

        private ExecutorService executor;

        @BeforeEach
        void setUp() {
            executor = Executors.newSingleThreadExecutor();
        }

        @AfterEach
        void tearDown() {
            executor.shutdownNow();
        }

        @Test
        void shouldNotBlock_WhenAsyncModeIsEnabled() {
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.supplyAsync(task::supply, executor);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();
            confirmCompletion(task);

            assertThat(future).isCompleted();
            assertThat(future.getNow(-1)).isEqualTo(task.getCurrentCount());
        }

        @Test
        void shouldBlock_WhenAsyncModeIsDisabled() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.supplyAsync(task::supply, executor);

            // verify that immediately after triggering run with mode=DISABLED, the count is 1
            assertThat(task.getCurrentCount()).isOne();

            assertThat(future).isCompleted();
            assertThat(future.getNow(-1)).isEqualTo(task.getCurrentCount());
        }
    }

    @Nested
    class WaitFor {

        @Test
        void shouldSucceed_WhenTheFutureCompletes_BeforeTimeout() {
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.doAsync(task::supply);

            Async.waitFor(future, 250, TimeUnit.MILLISECONDS);

            confirmCompletion(task);

            assertThat(future).isCompleted();
        }

        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeTheFutureCompletes() {
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.doAsync(task::supply);

            assertThatThrownBy(() -> Async.waitFor(future, 5, TimeUnit.MILLISECONDS))
                    .isExactlyInstanceOf(AsyncException.class)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 5 MILLISECONDS)")
                    .hasCauseInstanceOf(TimeoutException.class);

            assertThat(task.getCurrentCount()).isZero();
        }
    }

    @Nested
    class WaitForAll {

        @Test
        void shouldSucceed_WhenAllTheFuturesComplete_BeforeTimeout() {
            var task1 = new ConcurrentTask();
            CompletableFuture<Integer> future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask();
            CompletableFuture<Integer> future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask();
            CompletableFuture<Integer> future3 = Async.doAsync(task3::supply);

            var futures = List.of(future1, future2, future3);
            Async.waitForAll(futures, 500, TimeUnit.MILLISECONDS);

            confirmCompletion(task1);
            confirmCompletion(task2);
            confirmCompletion(task3);

            assertThat(future1).isCompleted();
            assertThat(future2).isCompleted();
            assertThat(future3).isCompleted();
        }

        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeAllFuturesComplete() {
            var task1 = new ConcurrentTask();
            CompletableFuture<Integer> future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask();
            CompletableFuture<Integer> future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask();
            CompletableFuture<Integer> future3 = Async.doAsync(task3::supply);

            var futures = List.of(future1, future2, future3);
            assertThatThrownBy(() -> Async.waitForAll(futures, 5, TimeUnit.MILLISECONDS))
                    .isExactlyInstanceOf(AsyncException.class)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 5 MILLISECONDS)")
                    .hasCauseInstanceOf(TimeoutException.class);

            assertThat(task1.getCurrentCount()).isZero();
            assertThat(task2.getCurrentCount()).isZero();
            assertThat(task3.getCurrentCount()).isZero();
        }
    }

    @Nested
    class WaitForAllIgnoringType {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Test
        void shouldSucceed_WhenAllTheFuturesComplete_BeforeTimeout() {
            var task1 = new ConcurrentTask();
            CompletableFuture future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask();
            CompletableFuture future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask();
            CompletableFuture future3 = Async.doAsync(task3::supply);

            var futures = List.of(future1, future2, future3);
            Async.waitForAllIgnoringType(futures, 500, TimeUnit.MILLISECONDS);

            confirmCompletion(task1);
            confirmCompletion(task2);
            confirmCompletion(task3);

            assertThat(future1).isCompleted();
            assertThat(future2).isCompleted();
            assertThat(future3).isCompleted();
        }

        @SuppressWarnings("rawtypes")
        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeAllFuturesComplete() {
            var task1 = new ConcurrentTask();
            CompletableFuture future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask();
            CompletableFuture future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask();
            CompletableFuture future3 = Async.doAsync(task3::supply);

            var futures = List.of(future1, future2, future3);
            assertThatThrownBy(() -> Async.waitForAllIgnoringType(futures, 5, TimeUnit.MILLISECONDS))
                    .isExactlyInstanceOf(AsyncException.class)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 5 MILLISECONDS)")
                    .hasCauseInstanceOf(TimeoutException.class);

            assertThat(task1.getCurrentCount()).isZero();
            assertThat(task2.getCurrentCount()).isZero();
            assertThat(task3.getCurrentCount()).isZero();
        }
    }

    @Nested
    class WithMaxTimeout {

        @Test
        void testWithMaxTimeout() {
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.doAsync(task::supply);
            CompletableFuture<Integer> futureWithTimeout = Async.withMaxTimeout(future, 5, TimeUnit.MILLISECONDS);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();

            await().atMost(FIVE_HUNDRED_MILLISECONDS).until(futureWithTimeout::isCompletedExceptionally);

            assertThat(futureWithTimeout)
                    .hasFailedWithThrowableThat()
                    .isExactlyInstanceOf(AsyncException.class)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 5 MILLISECONDS)")
                    .hasCauseInstanceOf(TimeoutException.class);

            var thrown = catchThrowable(futureWithTimeout::get);
            assertThat(thrown)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseExactlyInstanceOf(AsyncException.class);

            var cause = thrown.getCause();
            assertThat(cause).hasMessage("TimeoutException occurred (maximum wait was specified as 5 MILLISECONDS)");
        }
    }

    private void confirmCompletion(ConcurrentTask task) {
        await().atMost(FIVE_HUNDRED_MILLISECONDS).until(() -> task.getCurrentCount() == 1);
    }

    /**
     * Simple task implementation that provides a runnable and supplier interface method and a counter that can
     * be checked for number of completed executions.
     */
    @Slf4j
    static class ConcurrentTask {

        private final AtomicInteger counter;
        private final long delayMillis;

        ConcurrentTask() {
            this.counter = new AtomicInteger();
            this.delayMillis = 100;
        }

        void run() {
            supply();
        }

        Integer supply() {
            LOG.debug("executing concurrent task with delay of: {}ms", delayMillis);
            try {
                var startTime = System.nanoTime();
                performWait();
                long endTime = System.nanoTime();
                long elapsed = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                LOG.debug("performed task in: {}ms", elapsed);

                return counter.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.debug("Wait interrupted", e);
            }

            return counter.get();
        }

        Integer getCurrentCount() {
            return counter.get();
        }

        private void performWait() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(delayMillis);
        }
    }
}
