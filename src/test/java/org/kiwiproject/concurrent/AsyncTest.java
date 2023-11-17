package org.kiwiproject.concurrent;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.concurrent.Async.Mode;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class AsyncTest {

    private static final KiwiEnvironment ENV = new DefaultEnvironment();

    private String testName;

    @BeforeEach
    void setUp(TestInfo info) {
        testName = f("{}#{}",
                info.getTestClass().orElseThrow().getSimpleName(),
                info.getTestMethod().orElseThrow().getName());
    }

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
            var task = new ConcurrentTask(testName);
            CompletableFuture<Void> future = Async.runAsync(task::run);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();
            confirmCompletion(task);

            assertThat(future).isCompleted();

            // verify the count is one after completing successfully
            assertThat(task.getCurrentCount()).isOne();
        }

        @Test
        void shouldNotThrowExceptionWhenCompletesExceptionally() {
            var ex = new RuntimeException("oops");
            var task = new ConcurrentTask(testName).withException(ex);
            CompletableFuture<Void> future = Async.runAsync(task::run);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();
            confirmCompletion(task);

            assertThat(future).isCompletedExceptionally();

            // verify the count is one after completing exceptionally
            assertThat(task.getCurrentCount()).isOne();
        }

        @Test
        void shouldBlock_WhenAsyncModeIsDisabled() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);
            var task = new ConcurrentTask(testName);
            CompletableFuture<Void> future = Async.runAsync(task::run);

            // verify that immediately after triggering run with mode=DISABLED, the count is 1
            assertThat(task.getCurrentCount()).isOne();

            assertThat(future).isCompleted();

            // sanity check that the count remains 1
            assertThat(task.getCurrentCount()).isOne();
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
            var task = new ConcurrentTask(testName);
            CompletableFuture<Void> future = Async.runAsync(task::run, executor);

            // verify that immediately after triggering run, the count is still 0
            assertThat(task.getCurrentCount()).isZero();
            confirmCompletion(task);

            assertThat(future).isCompleted();
        }

        @Test
        void shouldBlock_WhenAsyncModeIsDisabled() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);
            var task = new ConcurrentTask(testName);
            CompletableFuture<Void> future = Async.runAsync(task::run, executor);

            // verify that immediately after triggering run with mode=DISABLED, the count is 1
            assertThat(task.getCurrentCount()).isOne();

            assertThat(future).isCompleted();
        }
    }

    @Nested
    class WaitIfAsyncDisabled {

        @BeforeEach
        void setUp() {
            Async.setUnitTestAsyncMode(Mode.DISABLED);
        }

        @Test
        void shouldReturnTheSameFuture() {
            var future = CompletableFuture.completedFuture(42);

            var returnedFuture = Async.waitIfAsyncDisabled(future);
            assertThat(returnedFuture).isSameAs(future);
        }

        @Test
        void shouldNotThrowAnException_WhenFutureCompletesExceptionally() {
            var future = CompletableFuture.failedFuture(new RuntimeException("boom"));

            assertThatCode(() -> Async.waitIfAsyncDisabled(future)).doesNotThrowAnyException();
        }

        @Test
        void shouldAllowAddingMoreBehavior_WhenFutureCompletesExceptionally() {
            var ex = new RuntimeException("ka-bloom!");
            var future = CompletableFuture.failedFuture(ex);

            var throwableFromTheFuture = new AtomicReference<Throwable>();
            assertThatCode(() -> {
                var sameFuture = Async.waitIfAsyncDisabled(future);
                sameFuture.whenComplete((result, throwable) -> throwableFromTheFuture.set(throwable));
            }).doesNotThrowAnyException();

            assertThat(throwableFromTheFuture.get()).isSameAs(ex);
        }
    }

    /**
     * These tests call supplyAsync(Runnable) since it simply delegates to doAsync(Runnable).
     */
    @Nested
    class DoAsyncWithSupplier {

        @Test
        void shouldNotBlock_WhenAsyncModeIsEnabled() {
            var task = new ConcurrentTask(testName);
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
            var task = new ConcurrentTask(testName);
            CompletableFuture<Integer> future = Async.supplyAsync(task::supply);

            // verify that immediately after triggering run with mode=DISABLED, the count is 1
            assertThat(task.getCurrentCount()).isOne();

            assertThat(future).isCompleted();
            assertThat(future.getNow(-1)).isEqualTo(task.getCurrentCount());
        }
    }

    /**
     * These tests call supplyAsync(Runnable, Executor) since it simply delegates to doAsync(Runnable, Executor).
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
            var task = new ConcurrentTask(testName);
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
            var task = new ConcurrentTask(testName);
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
            var task = new ConcurrentTask(testName);
            CompletableFuture<Integer> future = Async.doAsync(task::supply);

            Async.waitFor(future, 250, TimeUnit.MILLISECONDS);

            confirmCompletion(task);

            assertThat(future).isCompleted();
        }

        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeTheFutureCompletes() {
            var duration = Duration.ofMillis(100);
            var task = new ConcurrentTask(testName, duration);
            CompletableFuture<Integer> future = Async.doAsync(task::supply);

            try {
                assertThatThrownBy(() -> Async.waitFor(future, 1, TimeUnit.MILLISECONDS))
                        .isExactlyInstanceOf(AsyncException.class)
                        .hasMessage("TimeoutException occurred (maximum wait was specified as 1 MILLISECONDS)")
                        .hasCauseInstanceOf(TimeoutException.class);

                assertThat(task.getCurrentCount()).isZero();
            } finally {
                cancel(future);
            }
        }
    }

    @Nested
    class WaitForAll {

        @Test
        void shouldSucceed_WhenAllTheFuturesComplete_BeforeTimeout() {
            var task1 = new ConcurrentTask(testName + "#task1");
            CompletableFuture<Integer> future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask(testName + "#task2");
            CompletableFuture<Integer> future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask(testName + "#task3");
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
            var duration = Duration.ofMillis(200);
            var task1 = new ConcurrentTask(testName + "#task1", duration);
            CompletableFuture<Integer> future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask(testName + "#task2", duration);
            CompletableFuture<Integer> future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask(testName + "#task3", duration);
            CompletableFuture<Integer> future3 = Async.doAsync(task3::supply);

            try {
                var futures = List.of(future1, future2, future3);
                assertThatThrownBy(() -> Async.waitForAll(futures, 1, TimeUnit.MILLISECONDS))
                        .isExactlyInstanceOf(AsyncException.class)
                        .hasMessage("TimeoutException occurred (maximum wait was specified as 1 MILLISECONDS)")
                        .hasCauseInstanceOf(TimeoutException.class);

                assertThat(task1.getCurrentCount()).isZero();
                assertThat(task2.getCurrentCount()).isZero();
                assertThat(task3.getCurrentCount()).isZero();
            } finally {
                cancel(future1);
                cancel(future2);
                cancel(future3);
            }
        }
    }

    @Nested
    class WaitForAllIgnoringType {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Test
        void shouldSucceed_WhenAllTheFuturesComplete_BeforeTimeout() {
            var task1 = new ConcurrentTask(testName + "#task1");
            CompletableFuture future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask(testName + "#task2");
            CompletableFuture future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask(testName + "#task3");
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

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeAllFuturesComplete() {
            var duration = Duration.ofMillis(200);
            var task1 = new ConcurrentTask(testName + "#task1", duration);
            CompletableFuture future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask(testName + "#task2", duration);
            CompletableFuture future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask(testName + "#task3", duration);
            CompletableFuture future3 = Async.doAsync(task3::supply);

            try {
                var futures = List.of(future1, future2, future3);
                assertThatThrownBy(() -> Async.waitForAllIgnoringType(futures, 1, TimeUnit.MILLISECONDS))
                        .isExactlyInstanceOf(AsyncException.class)
                        .hasMessage("TimeoutException occurred (maximum wait was specified as 1 MILLISECONDS)")
                        .hasCauseInstanceOf(TimeoutException.class);

                assertThat(task1.getCurrentCount()).isZero();
                assertThat(task2.getCurrentCount()).isZero();
                assertThat(task3.getCurrentCount()).isZero();
            } finally {
                cancel(future1);
                cancel(future2);
                cancel(future3);
            }
        }
    }

    @Nested
    class WithMaxTimeout {

        @Test
        void shouldTimeout_WhenTaskTakesLongerThan_MaxTimeout() {
            var task = new ConcurrentTask(testName, Duration.ofSeconds(10));
            CompletableFuture<Integer> future = Async.doAsync(task::supply);
            CompletableFuture<Integer> futureWithTimeout = Async.withMaxTimeout(future, 5, TimeUnit.MILLISECONDS);

            assertThat(task.getCurrentCount())
                    .describedAs("immediately after triggering run, the count should still be 0")
                    .isZero();

            awaitAtMost500msWith25MsPoll().until(futureWithTimeout::isCompletedExceptionally);

            assertThat(task.getCurrentCount())
                    .describedAs("the count should still be 0, since we expect to have timed out before the counter is incremented")
                    .isZero();

            assertThat(futureWithTimeout).isNotCancelled();

            var thrown = catchThrowable(futureWithTimeout::get);
            assertThat(thrown)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseExactlyInstanceOf(AsyncException.class);

            var executionException = (ExecutionException) thrown;
            var asyncException = (AsyncException) executionException.getCause();

            assertThat(asyncException)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 5 MILLISECONDS)")
                    .hasCauseExactlyInstanceOf(TimeoutException.class);
        }
    }

    private static <T> void cancel(CompletableFuture<T> future) {
        var wasCancelled = future.cancel(true);
        assertThat(wasCancelled).isTrue();
    }

    private static void confirmCompletion(ConcurrentTask task) {
        awaitAtMost500msWith25MsPoll().until(() -> task.getCurrentCount() == 1);
    }

    private static ConditionFactory awaitAtMost500msWith25MsPoll() {
        return await()
                .atMost(FIVE_HUNDRED_MILLISECONDS)
                .pollDelay(Duration.ofMillis(25));
    }

    /**
     * Simple task implementation that provides a runnable and supplier interface method and a counter that can
     * be checked for number of completed executions. It simulates a task that takes some time to complete.
     */
    @Slf4j
    static class ConcurrentTask {

        private final String name;
        private final AtomicInteger counter;
        private final long durationMillis;

        private RuntimeException exceptionToThrow;

        ConcurrentTask(String name) {
            this(name, Duration.ofMillis(10));
        }

        ConcurrentTask(String name, Duration duration) {
            this.name = name;
            this.counter = new AtomicInteger();
            this.durationMillis = duration.toMillis();
        }

        ConcurrentTask withException(RuntimeException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
            return this;
        }

        void run() {
            supply();
        }

        Integer supply() {
            LOG.debug("executing concurrent task {} with duration of: {}ms", name, durationMillis);
            try {
                var startTime = System.nanoTime();
                performWait();
                long endTime = System.nanoTime();
                long elapsed = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                var completionStatus = isNull(exceptionToThrow) ? "successfully" : "exceptionally";
                LOG.debug("performed task {} {} in: {}ms", name, completionStatus, elapsed);

                var updatedCount = counter.incrementAndGet();

                if (nonNull(exceptionToThrow)) {
                    throw exceptionToThrow;
                }

                return updatedCount;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.debug("Wait interrupted for task {}", name, e);
            }

            return counter.get();
        }

        Integer getCurrentCount() {
            return counter.get();
        }

        private void performWait() throws InterruptedException {
            ENV.sleep(durationMillis);
        }
    }
}
