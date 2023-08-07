package org.kiwiproject.concurrent;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.util.concurrent.Striped;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

/**
 * {@link StripedLock} provides simple lambdas for encapsulating a block of code with a read/write lock.
 * <p>
 * The number of "stripes" indicates the maximum concurrent processes where the lock "key" is hashed and is used to
 * select a lock. This is useful when you want tasks that operate in the same "context" to block one another without
 * blocking unrelated tasks.
 *
 * @implNote This {@link StripedLock} uses Guava's {@link Striped} under the covers. The {@link ReadWriteLock}s
 * are re-entrant, and read locks can be held by multiple readers, while write locks are exclusive.
 * @see Striped
 * @see ReadWriteLock
 */
@Slf4j
public class StripedLock {

    private static final String DEFAULT_KEY_WHEN_BLANK = "BLANK-LOCK-KEY";
    private static final int DEFAULT_NUM_STRIPES = Runtime.getRuntime().availableProcessors() * 4;

    private final Striped<ReadWriteLock> lock;

    /**
     * Creates a new {@link StripedLock}, using {@link #DEFAULT_KEY_WHEN_BLANK} as the number of stripes.
     */
    public StripedLock() {
        this(Striped.readWriteLock(DEFAULT_NUM_STRIPES));
    }

    /**
     * Creates a new {@link StripedLock} with the given number of stripes.
     *
     * @param numStripes number of stripes
     * @see Striped#readWriteLock(int)
     */
    public StripedLock(int numStripes) {
        this(Striped.readWriteLock(numStripes));
    }

    /**
     * Create a new {@link StripedLock} using the given {@link ReadWriteLock}. This is useful if you have
     * multiple interdependent usages that you want to share across the same set of locks.
     *
     * @param lock the striped lock to use
     */
    public StripedLock(Striped<ReadWriteLock> lock) {
        this.lock = lock;
    }

    /**
     * Execute a {@link Runnable} task using the provided lock key and associated a READ lock.
     * <p>
     * <em>This implementation will block until the read lock is acquired.</em>
     *
     * @param lockKey the lock key
     * @param task    the task to run
     */
    public void runWithReadLock(String lockKey, Runnable task) {
        supplyWithReadLock(lockKey, () -> {
            task.run();
            return null;
        });
    }

    /**
     * Execute a {@link Supplier} using the provided lock key and associated READ lock.
     * <p>
     * <em>This implementation will block until the read lock is acquired.</em>
     *
     * @param lockKey the lock key
     * @param task    the task to supply a value
     * @param <T>     the type of object being supplied
     * @return the supplied value
     */
    public <T> T supplyWithReadLock(String lockKey, Supplier<T> task) {
        var nonNullKey = ensureNonBlankKey(lockKey);
        var readWriteLock = lock.get(nonNullKey);
        var lockHashCode = extractHashCode(readWriteLock);
        LOG.trace("Locking read lock {} for key {}", lockHashCode, nonNullKey);
        readWriteLock.readLock().lock();

        try {
            LOG.trace("Running task with read lock {} for key {}", lockHashCode, nonNullKey);
            return task.get();
        } finally {
            LOG.trace("Unlocking read lock {} for key {}", lockHashCode, nonNullKey);
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Execute a {@link Runnable} task using the provided lock key and associated a WRITE lock.
     * <p>
     * <em>This implementation will block until the write lock is acquired.</em>
     *
     * @param lockKey the lock key
     * @param task    the task to run
     */
    public void runWithWriteLock(String lockKey, Runnable task) {
        supplyWithWriteLock(lockKey, () -> {
            task.run();
            return null;
        });
    }

    /**
     * Execute a {@link Supplier} using the provided lock key and associated WRITE lock.
     * <p>
     * <em>This implementation will block until the write lock is acquired.</em>
     *
     * @param lockKey the lock key
     * @param task    the task to supply a value
     * @param <T>     the type of object being supplied
     * @return the supplied value
     */
    public <T> T supplyWithWriteLock(String lockKey, Supplier<T> task) {
        var nonNullKey = ensureNonBlankKey(lockKey);
        var readWriteLock = lock.get(nonNullKey);
        var lockHashCode = extractHashCode(readWriteLock);
        LOG.trace("Locking write lock {} for key {}", lockHashCode, nonNullKey);
        readWriteLock.writeLock().lock();

        try {
            LOG.trace("Running task with write lock {} for key {}", lockHashCode, nonNullKey);
            return task.get();
        } finally {
            LOG.trace("Unlocking write lock {} for key {}", lockHashCode, nonNullKey);
            readWriteLock.writeLock().unlock();
        }
    }

    private String ensureNonBlankKey(String key) {
        return isBlank(key) ? DEFAULT_KEY_WHEN_BLANK : key;
    }

    private String extractHashCode(Object obj) {
        return "@" + Integer.toHexString(System.identityHashCode(obj));
    }
}
