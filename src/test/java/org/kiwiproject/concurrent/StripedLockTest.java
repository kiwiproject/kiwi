package org.kiwiproject.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@DisplayName("StripedLock")
@Slf4j
class StripedLockTest {

    private static final KiwiEnvironment ENV = new DefaultEnvironment();

    @ParameterizedTest
    @NullAndEmptySource
    void testRunWithReadLock_OnNullAndEmptyKeys(String value) {
        var flag = new AtomicBoolean();
        new StripedLock().runWithReadLock(value, () -> flag.set(true));

        assertThat(flag).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testRunWithWriteLock_OnNullAndEmptyKeys(String value) {
        var flag = new AtomicBoolean();
        new StripedLock().runWithWriteLock(value, () -> flag.set(true));

        assertThat(flag).isTrue();
    }

    @Test
    void testRunWithReadLock_InSameThread_WithMultipleReadLocks() {
        var stripedLock = new StripedLock(1);
        var flag = new AtomicBoolean();

        stripedLock.runWithReadLock("test", () ->
                stripedLock.runWithReadLock("test", () ->
                        flag.set(true)));

        assertThat(flag).isTrue();
    }

    @Test
    void testRunWithWriteLock_InSameThread_WithMultipleWriteLocks() {
        var stripedLock = new StripedLock(1);
        var flag = new AtomicBoolean();

        stripedLock.runWithWriteLock("test", () ->
                stripedLock.runWithWriteLock("test", () ->
                        flag.set(true)));

        assertThat(flag).isTrue();
    }

    @Test
    void testRunWithLock_WhenBlocking_ForWriteAndWriteLocks() {
        var lock = new StripedLock(1);
        var task = createRunnableTask();
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        var completableFutures = List.of(
                Async.doAsync(() -> lock.runWithWriteLock(recorder1.id, () -> recorder1.runTask(task))),
                Async.doAsync(() -> lock.runWithWriteLock(recorder2.id, () -> recorder2.runTask(task)))
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, false);
    }

    @Test
    void testRunWithLock_WhenNonBlocking_ForWriteAndWriteLocks() {
        var lock = new StripedLock(2);
        var task = createRunnableTask();
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        var completableFutures = List.of(
                Async.doAsync(() -> lock.runWithWriteLock(recorder1.id, () -> recorder1.runTask(task))),
                Async.doAsync(() -> lock.runWithWriteLock(recorder2.id, () -> recorder2.runTask(task)))
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, true);
    }

    @Test
    void testRunWithLock_WhenBlocking_ForReadAndWriteLocks() {
        var lock = new StripedLock(1);  // ensures blocking execution
        var task = createRunnableTask();
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        var completableFutures = List.of(
                Async.doAsync(() -> lock.runWithWriteLock(recorder1.id, () -> recorder1.runTask(task))),
                Async.doAsync(() -> lock.runWithReadLock(recorder2.id, () -> recorder2.runTask(task)))
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, false);
    }

    @Test
    void testRunWithLock_WhenNonBlocking_ForReadAndReadLocks() {
        var lock = new StripedLock(1);
        var task = createRunnableTask();
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        var completableFutures = List.of(
                Async.doAsync(() -> lock.runWithReadLock(recorder1.id, () -> recorder1.runTask(task))),
                Async.doAsync(() -> lock.runWithReadLock(recorder2.id, () -> recorder2.runTask(task)))
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, true);
    }

    @Test
    void testSupplyWithLock_WhenNonBlocking_ForReadAndReadLocks() {
        var lock = new StripedLock(1);
        var task1 = createSupplierTask(42L);
        var task2 = createSupplierTask(84L);
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        var completableFutures = List.of(
                Async.doAsync(() -> lock.supplyWithReadLock(recorder1.id, () -> recorder1.runTaskAndSupply(task1))),
                Async.doAsync(() -> lock.supplyWithReadLock(recorder2.id, () -> recorder2.runTaskAndSupply(task2)))
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, true);

        assertThat(first(completableFutures).getNow(-1L)).isEqualTo(42L);
        assertThat(second(completableFutures).getNow(-1L)).isEqualTo(84L);
    }

    @Test
    void testSupplyWithLock_WhenBlocking_ForWriteAndReadLocks() {
        var lock = new StripedLock(1);
        var task1 = createSupplierTask(42L);
        var task2 = createSupplierTask(84L);
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        var completableFutures = List.of(
                Async.doAsync(() -> lock.supplyWithWriteLock(recorder1.id, () -> recorder1.runTaskAndSupply(task1))),
                Async.doAsync(() -> lock.supplyWithReadLock(recorder2.id, () -> recorder2.runTaskAndSupply(task2)))
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, false);

        assertThat(first(completableFutures).getNow(-1L)).isEqualTo(42L);
        assertThat(second(completableFutures).getNow(-1L)).isEqualTo(84L);
    }

    @Test
    void testSupplyWithLock_WhenNonBlocking_ForReadAndWriteLocks() {
        var lock = new StripedLock(2);
        var task1 = createSupplierTask(42L);
        var task2 = createSupplierTask(84L);
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        var completableFutures = List.of(
                Async.doAsync(() -> lock.supplyWithReadLock(recorder1.id, () -> recorder1.runTaskAndSupply(task1))),
                Async.doAsync(() -> lock.supplyWithWriteLock(recorder2.id, () -> recorder2.runTaskAndSupply(task2)))
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, true);

        assertThat(first(completableFutures).getNow(-1L)).isEqualTo(42L);
        assertThat(second(completableFutures).getNow(-1L)).isEqualTo(84L);
    }

    private Runnable createRunnableTask() {
        return this::sleep10ms;
    }

    private Supplier<Long> createSupplierTask(Long result) {
        return () -> {
            sleep10ms();
            return result;
        };
    }

    private void sleep10ms() {
        try {
            ENV.sleep(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.error("Unexpected InterruptedException", e);
            fail("Failed due to InterruptedException");
        }
    }

    private <T> void waitForAll(List<CompletableFuture<T>> futures) {
        Async.waitForAll(futures, 300, TimeUnit.MILLISECONDS);
    }

    private void logAndCheckExecutionTimes(TaskRecorder recorder1, TaskRecorder recorder2, boolean expectedOverlap) {
        LOG.debug("task 1 execution time range: {}", recorder1.timeRange());
        LOG.debug("task 2 execution time range: {}", recorder2.timeRange());

        assertThat(recorder1.overlaps(recorder2)).isEqualTo(expectedOverlap);
    }

    static class TaskRecorder {

        private final String id;
        private Instant start;
        private Instant stop;

        TaskRecorder(String id) {
            this.id = id;
        }

        void runTask(Runnable task) {
            start = Instant.now();
            task.run();
            stop = Instant.now();
        }

        <T> T runTaskAndSupply(Supplier<T> supplier) {
            start = Instant.now();
            var result = supplier.get();
            stop = Instant.now();
            return result;
        }

        Range<Long> timeRange() {
            return Range.open(start.toEpochMilli(), stop.toEpochMilli());
        }

        boolean overlaps(TaskRecorder other) {
            return timeRange().isConnected(other.timeRange());
        }
    }
}
