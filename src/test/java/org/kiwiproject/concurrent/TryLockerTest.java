package org.kiwiproject.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.awaitility.Durations.ONE_SECOND;

import com.google.common.util.concurrent.Runnables;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@DisplayName("TryLocker")
@Slf4j
class TryLockerTest {

    @Test
    void shouldReturnWaitTime() {
        var locker = TryLocker.usingReentrantLock(2, TimeUnit.SECONDS);

        assertThat(locker.getLockWaitTime()).isEqualTo(2L);
        assertThat(locker.getLockWaitTimeUnit()).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    void shouldReturnWaitDuration() {
        var locker = TryLocker.usingReentrantLock(100_000, TimeUnit.MICROSECONDS);

        assertThat(locker.getLockWaitDuration()).isEqualByComparingTo(Duration.ofMillis(100));
    }

    @Nested
    class WithLockOrElseUsingReentrantLock {

        @Test
        void shouldCall_WithLockAction_WhenLockObtainedWithinWaitTime() {
            var locker = TryLocker.usingReentrantLock();

            var withLockCalled = new AtomicBoolean();
            Runnable withLockAction = () -> withLockCalled.set(true);

            var orElseCalled = new AtomicBoolean();
            Runnable orElseAction = () -> orElseCalled.set(true);

            locker.withLockOrElse(withLockAction, orElseAction);

            assertThat(withLockCalled).isTrue();
            assertThat(orElseCalled).isFalse();
        }

        @Test
        void shouldCall_OrElseAction_WhenLockNotObtained() {
            var locker = TryLocker.usingReentrantLock(20, TimeUnit.MILLISECONDS);

            var taskHasLock = new AtomicBoolean();
            var shouldReleaseLock = new AtomicBoolean();

            LOG.trace("Launch task that takes some time");
            new Thread(() -> doTaskThatTakesSomeTime(locker, taskHasLock, shouldReleaseLock)).start();
            await().atMost(ONE_SECOND).until(taskHasLock::get);

            var withLockCalled = new AtomicBoolean();
            Runnable withLockAction = () -> {
                LOG.trace("withLockAction running...");
                withLockCalled.set(true);
                shouldReleaseLock.set(true);
            };

            var orElseCalled = new AtomicBoolean();
            Runnable orElseAction = () -> {
                LOG.trace("orElseAction running...");
                orElseCalled.set(true);
                shouldReleaseLock.set(true);
            };

            LOG.trace("Launch new task that tries to obtain the lock");
            new Thread(() -> locker.withLockOrElse(withLockAction, orElseAction)).start();
            await().atMost(ONE_SECOND).until(shouldReleaseLock::get);

            assertThat(withLockCalled).isFalse();
            assertThat(orElseCalled).isTrue();
        }

        private void doTaskThatTakesSomeTime(TryLocker locker, AtomicBoolean hasLock, AtomicBoolean shouldReleaseLock) {
            locker.withLockOrElse(
                    () -> {
                        LOG.trace("Task that takes time has obtained the lock");
                        hasLock.set(true);
                        await().atMost(ONE_SECOND).until(shouldReleaseLock::get);
                        LOG.trace("Releasing lock...");
                    },
                    Runnables.doNothing()
            );
        }
    }

    /**
     * NOTE: THe ExecutorService should have the same number of threads as number of Suppliers in the tests.
     * Since we are testing only 2 suppliers, that is the number of threads we need (more threads would also work
     * but the extra threads would do nothing).
     */
    @Nested
    class WithLockSupply {

        private ExecutorService executor;

        @BeforeEach
        void setUp() {
            int numSuppliers = 2;
            executor = Executors.newFixedThreadPool(numSuppliers);
        }

        @AfterEach
        void tearDown() {
            executor.shutdownNow();
        }

        @Nested
        class WithLockSupplyOrNull {

            @Test
            void shouldReturn_SuppliedValue_WhenLockObtainedWithinWaitTime() {
                var locker = TryLocker.usingReentrantLock(100, TimeUnit.MILLISECONDS);

                var supplierCalled = new AtomicBoolean();
                Supplier<String> supplier = () -> {
                    supplierCalled.set(true);
                    return "foo";
                };

                var result = locker.withLockSupplyOrNull(supplier);

                assertThat(supplierCalled).isTrue();
                assertThat(result).isEqualTo("foo");
            }

            @RepeatedTest(5)
            void shouldReturn_Null_WhenLockNotObtainedWithinWaitTime() throws InterruptedException, ExecutionException, TimeoutException {
                BiFunction<TryLocker, Supplier<String>, String> fn = TryLocker::withLockSupplyOrNull;

                callAndAssertWithLockSupplyFunction(executor, null, fn);
            }
        }

        @Nested
        class WithLockSupplyOrFallbackUsingValue {

            @Test
            void shouldReturn_SuppliedValue_WhenLockObtainedWithinWaitTime() {
                var locker = TryLocker.usingReentrantLock(75, TimeUnit.MILLISECONDS);

                var supplierCalled = new AtomicBoolean();
                Supplier<String> supplier = () -> {
                    supplierCalled.set(true);
                    return "foo";
                };

                var result = locker.withLockSupplyOrFallback(supplier, "bar");

                assertThat(supplierCalled).isTrue();
                assertThat(result).isEqualTo("foo");
            }

            @RepeatedTest(5)
            void shouldReturn_FallbackValue_WhenLockNotObtainedWithinWaitTime() throws InterruptedException, ExecutionException, TimeoutException {
                BiFunction<TryLocker, Supplier<String>, String> fn =
                        (tryLocker, withLockSupplier) -> tryLocker.withLockSupplyOrFallback(withLockSupplier, "bar");

                callAndAssertWithLockSupplyFunction(executor, "bar", fn);
            }
        }

        @Nested
        class WithLockSupplyOrFallbackUsingSupplier {

            @Test
            void shouldReturn_SuppliedValue_WhenLockObtainedWithinWaitTime() {
                var locker = TryLocker.usingReentrantLock(85, TimeUnit.MILLISECONDS);

                var supplierCalled = new AtomicBoolean();
                Supplier<String> supplier = () -> {
                    supplierCalled.set(true);
                    return "foo";
                };

                var result = locker.withLockSupplyOrFallbackSupply(supplier, () -> "baz");

                assertThat(supplierCalled).isTrue();
                assertThat(result).isEqualTo("foo");
            }

            @RepeatedTest(5)
            void shouldReturn_FallbackSuppliedValue_WhenLockNotObtainedWithinWaitTime() throws InterruptedException, ExecutionException, TimeoutException {
                BiFunction<TryLocker, Supplier<String>, String> fn =
                        (tryLocker, withLockSupplier) -> tryLocker.withLockSupplyOrFallbackSupply(withLockSupplier, () -> "baz");

                callAndAssertWithLockSupplyFunction(executor, "baz", fn);
            }
        }
    }

    private void callAndAssertWithLockSupplyFunction(ExecutorService executor,
                                                     String expectedFallbackResult,
                                                     BiFunction<TryLocker, Supplier<String>, String> withLockSupplyFun)
            throws InterruptedException, ExecutionException, TimeoutException {

        // Make max wait time very small
        var locker = TryLocker.usingReentrantLock(5, TimeUnit.MILLISECONDS);

        var hasLock = new AtomicBoolean();
        var shouldReleaseLock = new AtomicBoolean();

        var supplier1Called = new AtomicBoolean();
        Supplier<String> supplier1 = () -> {
            LOG.trace("Supplier 1 has obtained the lock; wait for signal to release lock");
            hasLock.set(true);
            supplier1Called.set(true);
            await().atMost(FIVE_SECONDS).until(shouldReleaseLock::get);
            LOG.trace("Received signal to release lock. Suppler 1 is done");
            return "foo";
        };

        var supplier2Called = new AtomicBoolean();
        Supplier<String> supplier2 = () -> {
            supplier2Called.set(true);
            return "bar";
        };

        var result1Future = executor.submit(() -> withLockSupplyFun.apply(locker, supplier1));
        LOG.trace("Wait until supplier 1 has lock");
        await().atMost(ONE_SECOND).until(hasLock::get);

        LOG.trace("Launch second task that tries to obtain the lock");
        var secondTaskRunning = new AtomicBoolean();
        var result2Future = executor.submit(() -> {
            secondTaskRunning.set(true);
            return withLockSupplyFun.apply(locker, supplier2);
        });

        LOG.trace("Wait until second task us running");
        await().atMost(ONE_SECOND).until(secondTaskRunning::get);

        LOG.trace("Sending signal to release lock");
        shouldReleaseLock.set(true);

        assertThat(supplier1Called).isTrue();
        assertThat(getWithTimeout(result1Future)).isEqualTo("foo");

        assertThat(supplier2Called).isFalse();
        assertThat(getWithTimeout(result2Future)).isEqualTo(expectedFallbackResult);
    }

    private static <T> T getWithTimeout(Future<T> future) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(250, TimeUnit.MILLISECONDS);
    }
}
