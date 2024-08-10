package org.kiwiproject.collect;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utilities related to Streams that are not already in the JDKs {@link java.util.stream.Stream}
 * or Guava's {@link com.google.common.collect.Streams}.
 */
@UtilityClass
public final class KiwiStreams {

    /**
     * Find the first object having the given {@code typeToFind} in a stream of objects.
     *
     * @param stream        the stream of objects of some (unknown) type
     * @param typeToFind    the class of the object to find
     * @param <T>           the type token of the type we want to find
     * @return an Optional containing the first object of the given type, or empty
     */
    public static <T> Optional<T> findFirst(Stream<?> stream, Class<T> typeToFind) {
        return findFirst(stream, typeToFind, obj -> true);
    }

    /**
     * Find the first object having the given {@code typeToFind} and matching the supplied
     * predicate in a stream of objects.
     *
     * @param stream        the stream of objects of some (unknown) type
     * @param typeToFind    the class of the object to find
     * @param predicate     the condition that must be satisfied for a match to occur
     * @param <T>           the type token of the type we want to find
     * @return an Optional containing the first object of the given type, or empty
     */
    public static <T> Optional<T> findFirst(Stream<?> stream, Class<T> typeToFind, Predicate<T> predicate) {
        return stream
                .filter(obj -> typeToFind.isAssignableFrom(obj.getClass()))
                .map(typeToFind::cast)
                .filter(predicate)
                .findFirst();
    }

    /**
     * Return a sequential or parallel {@link Stream} over {@code collection} based on
     * the given {@link StreamMode}.
     * <p>
     * Normally you must hard code the type of stream directly in the code using either
     * {@link Collection#stream()} or {@link Collection#parallelStream()}. This method
     * provides a simple way to determine how to process a stream at runtime, for
     * example, based on number of elements or type of algorithm.
     *
     * @param <T> the type of collection elements
     * @param collection the collection to stream
     * @param mode the mode to use when streaming the collection
     * @return a sequential or parallel {@link Stream} over {@code collection}
     */
    public static <T> Stream<T> stream(Collection<T> collection, StreamMode mode) {
        checkArgumentNotNull(collection);
        checkArgumentNotNull(mode);

        return switch (mode) {
            case SEQUENTIAL -> collection.stream();
            case PARALLEL -> collection.parallelStream();
        };
    }

    /**
     * Describes the type of {@link Stream}.
     */
    public enum StreamMode {

        /**
         * A sequential stream.
         *
         * @see Collection#stream()
         */
        SEQUENTIAL,

        /**
         * A possibly parallel stream.
         *
         * @see Collection#parallelStream()
         */
        PARALLEL
    }

}
