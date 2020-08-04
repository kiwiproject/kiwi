package org.kiwiproject.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility methods for working with {@link Iterator} instances. Analogous to Guava's {@link Iterators}
 */
@UtilityClass
public class KiwiIterators {

    public static final String NOT_ENOUGH_VALUES_ERROR = "need at least 2 elements to cycle";

    /**
     * Returns a <em>thread-safe</em> iterator that cycles indefinitely over the elements of {@code iterable}, base
     * on Guava's {@link com.google.common.collect.Iterables#cycle(Iterable)}. The differences from Guava is that the
     * returned iterator provides thread-safety; makes an immutable copy of the provided iterable; and does not
     * support element removal regardless of whether the original iterable does.
     * <p>
     * Typical use cases include round-robin scenarios, such as round-robin between replicated service registries
     * like Netflix Eureka.
     * <p>
     * The returned iterator does <em>not</em> support {@link Iterator#remove()} nor does it support
     * {@link Iterator#forEachRemaining(Consumer)}, as the entire point is to cycle <em>forever</em>.
     *
     * @param iterable the Iterable to cycle
     * @param <T> the type of objects in the Iterable
     * @return an Iterator that cycles through the given Iterable without terminating
     */
    public static <T> Iterator<T> cycleForever(Iterable<T> iterable) {
        ImmutableList<T> elements = ImmutableList.copyOf(iterable);
        checkArgument(elements.size() > 1, NOT_ENOUGH_VALUES_ERROR);
        return new ThreadSafeCyclicIterator<>(elements);
    }

    /**
     * Returns a <em>thread-safe</em> iterator that cycles indefinitely over the elements of {@code iterable}, base
     * on Guava's {@link com.google.common.collect.Iterables#cycle(Iterable)}. The differences from Guava is that the
     * returned iterator provides thread-safety; makes an immutable copy of the provided iterable; and does not
     * support element removal regardless of whether the original iterable does.
     * <p>
     * Typical use cases include round-robin scenarios, such as round-robin between replicated service registries
     * like Netflix Eureka.
     * <p>
     * The returned iterator does <em>not</em> support {@link Iterator#remove()} nor does it support
     * {@link Iterator#forEachRemaining(Consumer)}, as the entire point is to cycle <em>forever</em>.
     *
     * @param elements the values to cycle
     * @param <T> the type of the elements
     * @return an Iterator that cycles through the given iterable without terminating
     */
    @SafeVarargs
    public static <T> Iterator<T> cycleForever(T... elements) {
        checkArgument(elements.length > 1, NOT_ENOUGH_VALUES_ERROR);
        return new ThreadSafeCyclicIterator<>(newArrayList(elements));
    }

    // TODO Should this be made public and/or a top-level class???
    private static class ThreadSafeCyclicIterator<E> implements Iterator<E> {

        private final Iterator<E> cycler;
        private final Lock cyclerLock;

        ThreadSafeCyclicIterator(Iterable<E> iterable) {
            this.cycler = Iterators.cycle(iterable);
            this.cyclerLock = new ReentrantLock();
        }

        @Override
        public boolean hasNext() {
            return withLock(cycler::hasNext);
        }

        @Override
        public E next() {
            return withLock(() -> {
                if (!hasNext()) {
                    throw new NoSuchElementException("there are no elements to cycle");
                }
                return cycler.next();
            });
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("unsupported; cannot remove from an infinite cycling iterator");
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            throw new UnsupportedOperationException("unsupported; infinite loop would occur unless an exception is thrown");
        }

        private <T> T withLock(Supplier<T> supplier) {
            try {
                cyclerLock.lock();
                return supplier.get();
            } finally {
                cyclerLock.unlock();
            }
        }
    }

}
