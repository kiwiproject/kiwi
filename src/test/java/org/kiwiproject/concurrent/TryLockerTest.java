package org.kiwiproject.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;

import com.google.common.util.concurrent.Runnables;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

        @SuppressWarnings("UnstableApiUsage")
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
}