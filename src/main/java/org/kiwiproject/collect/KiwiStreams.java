package org.kiwiproject.collect;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

}
