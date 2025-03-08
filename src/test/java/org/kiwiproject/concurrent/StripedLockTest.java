package org.kiwiproject.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.base.process.Processes;
import org.kiwiproject.io.KiwiIO;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@DisplayName("StripedLock")
@Slf4j
class StripedLockTest {

    private static final KiwiEnvironment ENV = new DefaultEnvironment();

    @BeforeAll
    static void beforeAll() {
        var unameProc = Processes.launch("uname", "-a");
        var exitCodeOpt = Processes.waitForExit(unameProc, 1, TimeUnit.SECONDS);
        exitCodeOpt.ifPresentOrElse(exitCode -> {
            var stdout = KiwiIO.readLinesFromInputStreamOf(unameProc);
            LOG.info("OS info: {}", stdout);
        }, () -> LOG.warn("The uname command timed out!"));

        reportCommonPoolInfo();
    }

    private static void reportCommonPoolInfo() {
        var availableProcessors = Runtime.getRuntime().availableProcessors();
        LOG.info("availableProcessors: {}", availableProcessors);

        var commonPool = ForkJoinPool.commonPool();
        LOG.info("ForkJoinPool Common Pool Parallelism: {}", commonPool.getParallelism());
        LOG.info("ForkJoinPool Common Pool Active Thread Count: {}", commonPool.getActiveThreadCount());
        LOG.info("ForkJoinPool Common Pool Running Thread Count: {}", commonPool.getRunningThreadCount());
        LOG.info("ForkJoinPool Common Pool Queued Task Count: {}", commonPool.getQueuedTaskCount());
        LOG.info("ForkJoinPool Common Pool Queued Submission Count: {}", commonPool.getQueuedSubmissionCount());
        LOG.info("ForkJoinPool Common Pool Pool Size: {}", commonPool.getPoolSize());
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        LOG.info("--- BEGIN: {} ---", testInfo.getDisplayName());
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        LOG.info("--- END: {} ---", testInfo.getDisplayName());
    }

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
        reportCommonPoolInfo();

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
        reportCommonPoolInfo();

        var lock = new StripedLock(1);
        var task = createRunnableTask();
        var recorder1 = new TaskRecorder("task1");
        var recorder2 = new TaskRecorder("task2");

        // Well damn, this made this test pass...
        var threadPool = Executors.newFixedThreadPool(10);

        var completableFutures = List.of(
                Async.doAsync(() -> lock.runWithReadLock(recorder1.id, () -> recorder1.runTask(task)), threadPool),
                Async.doAsync(() -> lock.runWithReadLock(recorder2.id, () -> recorder2.runTask(task)), threadPool)
        );

        waitForAll(completableFutures);

        logAndCheckExecutionTimes(recorder1, recorder2, true);
    }

    @Test
    void testSupplyWithLock_WhenNonBlocking_ForReadAndReadLocks() {
        reportCommonPoolInfo();

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
        reportCommonPoolInfo();
        
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
        var range1 = recorder1.timeRange();
        LOG.debug("task 1 execution time range: {}", range1);

        var range2 = recorder2.timeRange();
        LOG.debug("task 2 execution time range: {}", range2);

        var overlap = recorder1.overlaps(recorder2);
        LOG.debug("task 1 and 2 overlap? {} (expecting: {})", overlap, expectedOverlap);

        var description = expectedOverlap ?
                "expected execution time ranges to overlap but they do not (range 1: %s ; range 2: %s)" :
                "did not expect execution time ranges to overlap but they do (range 1: %s ; range 2: %s)";
        assertThat(overlap)
                .describedAs(description, range1, range2)
                .isEqualTo(expectedOverlap);
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
