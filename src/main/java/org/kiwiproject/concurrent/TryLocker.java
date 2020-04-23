package org.kiwiproject.concurrent;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Utility that aids in using {@link Lock#tryLock(long, TimeUnit)}.
 * <p>
 * This can also make unit testing easier by allowing easy mocking.
 */
@Slf4j
public class TryLocker {

    /**
     * The default lock wait time if not specified.
     */
    public static final int DEFAULT_LOCK_WAIT_TIME = 50;

    /**
     * The default lock wait time unit if not specified.
     */
    public static final TimeUnit DEFAULT_LOCK_WAIT_TIME_UNIT = TimeUnit.MILLISECONDS;

    private final Lock lock;

    @Getter
    private final long lockWaitTime;

    @Getter
    private final TimeUnit lockWaitTimeUnit;

    private TryLocker(Lock lock, long lockWaitTime, TimeUnit lockWaitTimeUnit) {
        this.lock = lock;
        this.lockWaitTime = lockWaitTime;
        this.lockWaitTimeUnit = lockWaitTimeUnit;
    }

    /**
     * Create a new {@link TryLocker} using a {@link ReentrantLock} and the default lock maximum wait time.
     *
     * @see #DEFAULT_LOCK_WAIT_TIME
     * @see #DEFAULT_LOCK_WAIT_TIME_UNIT
     */
    public static TryLocker usingReentrantLock() {
        return usingReentrantLock(DEFAULT_LOCK_WAIT_TIME, DEFAULT_LOCK_WAIT_TIME_UNIT);
    }

    /**
     * Create a new {@link TryLocker} using a {@link ReentrantLock} with the given lock wait time.
     *
     * @param maxWaitTime     maximum lock wait time
     * @param maxWaitTimeUnit maximum lock wait time value
     * @return a new instance
     */
    public static TryLocker usingReentrantLock(int maxWaitTime, TimeUnit maxWaitTimeUnit) {
        return using(new ReentrantLock(), maxWaitTime, maxWaitTimeUnit);
    }

    /**
     * Create a new {@link TryLocker} using the given lock and maximum wait time.
     *
     * @param lock            the {@link Lock} to use
     * @param maxWaitTime     maximum lock wait time
     * @param maxWaitTimeUnit maximum lock wait time value
     * @return a new instance
     */
    public static TryLocker using(Lock lock, long maxWaitTime, TimeUnit maxWaitTimeUnit) {
        return new TryLocker(lock, maxWaitTime, maxWaitTimeUnit);
    }

    /**
     * Return the maximum lock wait time as a {@link Duration}.
     */
    public Duration getLockWaitDuration() {
        return Duration.ofMillis(lockWaitTimeUnit.toMillis(lockWaitTime));
    }

    /**
     * Run {@code withLockAction} if the lock is obtained within the lock timeout period. Otherwise
     * run {@code orElseAction}.
     *
     * @param withLockAction action to run if lock is obtained
     * @param orElseAction   action to run if lock is not obtained
     */
    public void withLockOrElse(Runnable withLockAction, Runnable orElseAction) {
        var gotLock = false;
        try {
            gotLock = lock.tryLock(lockWaitTime, lockWaitTimeUnit);
            LOG.trace("Got lock {} within wait time {} {}? {}", lock, lockWaitTime, lockWaitTimeUnit, gotLock);

            var action = selectAction(gotLock, withLockAction, orElseAction);
            action.run();

        } catch (InterruptedException e) {
            LOG.warn("Interrupted waiting for lock", e);
            Thread.currentThread().interrupt();
        } finally {
            unlockOnlyIf(gotLock);
        }
    }

    private static Runnable selectAction(boolean gotLock, Runnable withLockAction, Runnable orElseAction) {
        if (gotLock) {
            return withLockAction;
        }

        return orElseAction;
    }

    /**
     * Execute the given {@code withLockSupplier} if the lock is obtained within the lock timeout period and return
     * its value. Otherwise return null.
     *
     * @param withLockSupplier supplier to execute if lock is obtained
     * @param <T>              type of object returned
     * @return the supplied value or null
     */
    public <T> T withLockSupplyOrNull(Supplier<T> withLockSupplier) {
        return getWithLockOrNull(withLockSupplier);
    }

    /**
     * Execute the given {@code withLockSupplier} if the lock is obtained within the lock timeout period and return
     * its value. Otherwise return the {@code fallbackValue}.
     *
     * @param withLockSupplier supplier to execute if lock is obtained
     * @param fallbackValue    the value to use if the lock is not obtained
     * @param <T>              type of object returned
     * @return the supplied value or the fallback value
     */
    public <T> T withLockSupplyOrFallback(Supplier<T> withLockSupplier, T fallbackValue) {
        var result = getWithLockOrNull(withLockSupplier);

        return nonNull(result) ? result : fallbackValue;
    }

    /**
     * Execute the given {@code withLockSupplier} if the lock is obtained within the lock timeout period and return
     * its value. Otherwise return the valued supplied by {@code fallbackSupplier}.
     *
     * @param withLockSupplier supplier to execute if lock is obtained
     * @param fallbackSupplier the supplier to execute if the lock is not obtained
     * @param <T>              type of object returned
     * @return the supplied value or the supplied fallback value
     */
    public <T> T withLockSupplyOrFallbackSupply(Supplier<T> withLockSupplier, Supplier<T> fallbackSupplier) {
        var result = getWithLockOrNull(withLockSupplier);

        return nonNull(result) ? result : fallbackSupplier.get();
    }

    private <T> T getWithLockOrNull(Supplier<T> withLockSupplier) {
        checkArgumentNotNull(withLockSupplier);

        boolean gotLock = false;
        T result = null;
        try {
            gotLock = lock.tryLock(lockWaitTime, lockWaitTimeUnit);
            LOG.trace("Got lock {} within wait time {} {}? {}", lock, lockWaitTime, lockWaitTimeUnit, gotLock);
            if (gotLock) {
                result = withLockSupplier.get();
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted waiting for lock", e);
            Thread.currentThread().interrupt();
        } finally {
            unlockOnlyIf(gotLock);
        }
        return result;
    }

    private void unlockOnlyIf(boolean gotLock) {
        if (gotLock) {
            lock.unlock();
        }
    }
}
