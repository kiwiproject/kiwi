package org.kiwiproject.collect;

import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * Utility methods for working with {@link Collection} instances.
 */
@UtilityClass
public class KiwiCollections {

    /**
     * Checks whether the specified collection is null or empty.
     *
     * @param collection the collection
     * @param <T>        the type of items in the collection
     * @return {@code true} if collection is null or empty; {@code false} otherwise
     */
    public static <T> boolean isNullOrEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks whether the specified collection is neither null nor empty.
     *
     * @param collection the collection
     * @param <T>        the type of items in the collection
     * @return {@code true} if collection is neither null nor empty; {@code false} otherwise
     */
    public static <T> boolean isNotNullOrEmpty(Collection<T> collection) {
        return !isNullOrEmpty(collection);
    }

    /**
     * Checks whether the specified collection is non-null and has only one item.
     *
     * @param collection the collection
     * @param <T>        the type of items in the collection
     * @return {@code true} if collection is non-null and has exactly one item; {@code false}
     */
    public static <T> boolean hasOneElement(Collection<T> collection) {
        return nonNull(collection) && collection.size() == 1;
    }
}
