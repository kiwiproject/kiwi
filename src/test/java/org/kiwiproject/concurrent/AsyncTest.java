package org.kiwiproject.concurrent;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
// import org.junitpioneer.jupiter.RetryingTest;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.concurrent.Async.Mode;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
// import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
class AsyncTest {

    private static final KiwiEnvironment ENV = new DefaultEnvironment();

    @BeforeAll
    static void beforeAll() {
        LOG.info("-------------------- beforeAll --------------------");

        var task = new ConcurrentTask();
        LOG.info("Task duration millis: {}", task.durationMillis);
        var future = Async.doAsync(task::supply);

        try {
            LOG.info("About to wait");
            var start = System.nanoTime();
            Async.waitFor(future, 250, TimeUnit.MILLISECONDS);
            var elapsed = System.nanoTime() - start;
            var millis = TimeUnit.NANOSECONDS.toMillis(elapsed);
            LOG.info(":BeforeAll: Took {} nanos ( {} millis ) for waitFor to return with 250ms timeout%n", elapsed, millis);
        } catch (Exception e) {
            LOG.error("EXCEPTION IN beforeAll", e);
        }
    }

    @BeforeEach
    void setUp() {
        System.out.println("-------------------- setUp --------------------");

        var task = new ConcurrentTask();
        LOG.info("Task duration millis: {}", task.durationMillis);
        var future = Async.doAsync(task::supply);

        try {
            LOG.info("About to wait");
            var start = System.nanoTime();
            Async.waitFor(future, 250, TimeUnit.MILLISECONDS);
            var elapsed = System.nanoTime() - start;
            var millis = TimeUnit.NANOSECONDS.toMillis(elapsed);
            LOG.info(":BeforeEach: Took {} nanos ( {} millis ) for waitFor to return with 250ms timeout%n", elapsed, millis);
        } catch (Exception e) {
            LOG.error("EXCEPTION IN setUp", e);
        }
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
            var task = new ConcurrentTask();
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
            var task = new ConcurrentTask().withException(ex);
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
            var task = new ConcurrentTask();
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

        /**
         * @implNote This test has been failing intermittently running in GitHub actions, mainly on JDK 21 but
         * sometimes on JDK 17. For now, making it a "retrying test". Also, see issue #1070.
         */
        // @RetryingTest(3)
        @Test
        void shouldSucceed_WhenTheFutureCompletes_BeforeTimeout() {
            var task = new ConcurrentTask();
            CompletableFuture<Integer> future = Async.doAsync(task::supply);

            Async.waitFor(future, 250, TimeUnit.MILLISECONDS);

            confirmCompletion(task);

            assertThat(future).isCompleted();
        }

        /*
         * @implNote This is a "retrying" test with a higher task duration because we have seen this test
         * fail (see issue #1065) when run individually, i.e. in an IDE.
         */
        // @RetryingTest(3)
        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeTheFutureCompletes() {
            var duration = Duration.ofMillis(100);
            var task = new ConcurrentTask(duration);
            CompletableFuture<Integer> future = Async.doAsync(task::supply);

            assertThatThrownBy(() -> Async.waitFor(future, 1, TimeUnit.MILLISECONDS))
                    .isExactlyInstanceOf(AsyncException.class)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 1 MILLISECONDS)")
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

        /*
         * @implNote This is a "retrying" test with a higher task duration because we have seen this test
         * fail (see issue #1065) when run individually, i.e. in an IDE.
         */
        // @RetryingTest(3)
        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeAllFuturesComplete() {
            var duration = Duration.ofMillis(200);
            var task1 = new ConcurrentTask(duration);
            CompletableFuture<Integer> future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask(duration);
            CompletableFuture<Integer> future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask(duration);
            CompletableFuture<Integer> future3 = Async.doAsync(task3::supply);

            var futures = List.of(future1, future2, future3);
            assertThatThrownBy(() -> Async.waitForAll(futures,  1, TimeUnit.MILLISECONDS))
                    .isExactlyInstanceOf(AsyncException.class)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 1 MILLISECONDS)")
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

        /*
         * @implNote This is a "retrying" test with a higher task duration because we have seen this test
         * fail (see issue #1065) when run individually, i.e. in an IDE.
         */
        @SuppressWarnings("rawtypes")
        // @RetryingTest(3)
        @Test
        void shouldThrowAsyncException_WhenTimesOut_BeforeAllFuturesComplete() {
            var duration = Duration.ofMillis(200);
            var task1 = new ConcurrentTask(duration);
            CompletableFuture future1 = Async.doAsync(task1::supply);

            var task2 = new ConcurrentTask(duration);
            CompletableFuture future2 = Async.doAsync(task2::supply);

            var task3 = new ConcurrentTask(duration);
            CompletableFuture future3 = Async.doAsync(task3::supply);

            var futures = List.of(future1, future2, future3);
            assertThatThrownBy(() -> Async.waitForAllIgnoringType(futures, 1, TimeUnit.MILLISECONDS))
                    .isExactlyInstanceOf(AsyncException.class)
                    .hasMessage("TimeoutException occurred (maximum wait was specified as 1 MILLISECONDS)")
                    .hasCauseInstanceOf(TimeoutException.class);

            assertThat(task1.getCurrentCount()).isZero();
            assertThat(task2.getCurrentCount()).isZero();
            assertThat(task3.getCurrentCount()).isZero();
        }
    }

    @Nested
    class WithMaxTimeout {

        @Test
        void shouldTimeout_WhenTaskTakesLongerThan_MaxTimeout() {
            var task = new ConcurrentTask(Duration.ofSeconds(10));
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

    private void confirmCompletion(ConcurrentTask task) {
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

        private final AtomicInteger counter;
        private final long durationMillis;

        private RuntimeException exceptionToThrow;

        ConcurrentTask() {
            this(Duration.ofMillis(10));
        }

        ConcurrentTask(Duration duration) {
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
            LOG.debug("executing concurrent task with duration of: {}ms", durationMillis);
            try {
                var startTime = System.nanoTime();
                performWait();
                long endTime = System.nanoTime();
                long elapsed = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                var completionStatus = isNull(exceptionToThrow) ? "successfully" : "exceptionally";
                LOG.debug("performed task {} in: {}ms", completionStatus, elapsed);

                var updatedCount = counter.incrementAndGet();

                if (nonNull(exceptionToThrow)) {
                    throw exceptionToThrow;
                }

                return updatedCount;
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
            ENV.sleep(durationMillis);
        }
    }
}
