package org.kiwiproject.collect;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import lombok.experimental.UtilityClass;

import java.util.Queue;

/**
 * Utility methods for working with Guava {@link EvictingQueue} instances.
 */
@UtilityClass
public class KiwiEvictingQueues {

    /**
     * The default maximum number of {@link EvictingQueue} items.
     *
     * @see #synchronizedEvictingQueue()
     */
    public static final int DEFAULT_MAX_RECENT_ITEMS = 100;

    /**
     * Create a new, synchronized {@link EvictingQueue} that can hold up to {@link #DEFAULT_MAX_RECENT_ITEMS} items.
     *
     * @param <T> the type in the queue
     * @return a synchronized {@link EvictingQueue}
     * @apiNote returns a plain {@link Queue} because Guava's {@link Queues#synchronizedQueue(Queue)} returns a {@link Queue}.
     * Any attempt to cast to an {@link EvictingQueue} will result in a {@link ClassCastException}.
     * @implNote See synchronized notes regarding manual synchronization of the returned queue's {@link java.util.Iterator}
     * in {@link Queues#synchronizedQueue(Queue)}
     * @see Queues#synchronizedQueue(Queue)
     */
    public static <T> Queue<T> synchronizedEvictingQueue() {
        return synchronizedEvictingQueue(DEFAULT_MAX_RECENT_ITEMS);
    }

    /**
     * Create a new, synchronized {@link EvictingQueue} that can hold up to {@code maxSize} items.
     *
     * @param maxSize maximum size for the queue
     * @param <T>     the type in the queue
     * @return a synchronized {@link EvictingQueue}
     * @apiNote returns a plain {@link Queue} because Guava's {@link Queues#synchronizedQueue(Queue)} returns a {@link Queue}.
     * Any attempt to cast to an {@link EvictingQueue} will result in a {@link ClassCastException}.
     * @implNote See synchronized notes regarding manual synchronization of the returned queue's {@link java.util.Iterator}
     * in {@link Queues#synchronizedQueue(Queue)}
     * @see Queues#synchronizedQueue(Queue)
     */
    public static <T> Queue<T> synchronizedEvictingQueue(int maxSize) {
        return Queues.synchronizedQueue(EvictingQueue.create(maxSize));
    }
}
