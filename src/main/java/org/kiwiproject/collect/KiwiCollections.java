package org.kiwiproject.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

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
     * @return {@code true} if the collection is null or empty; {@code false} otherwise
     */
    public static <T> boolean isNullOrEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks whether the specified collection is neither null nor empty.
     *
     * @param collection the collection
     * @param <T>        the type of items in the collection
     * @return {@code true} if the collection is neither null nor empty; {@code false} otherwise
     */
    public static <T> boolean isNotNullOrEmpty(Collection<T> collection) {
        return !isNullOrEmpty(collection);
    }

    /**
     * Checks whether the specified collection is non-null and has only one item.
     *
     * @param collection the collection
     * @param <T>        the type of items in the collection
     * @return {@code true} if the collection is non-null and has exactly one item; {@code false}
     */
    public static <T> boolean hasOneElement(Collection<T> collection) {
        return nonNull(collection) && collection.size() == 1;
    }

    /**
     * Returns an {@link Optional} containing the first element in the given sequenced collection, or an empty optional
     * if the collection is null or empty.
     *
     * @param sequencedCollection the sequenced collection
     * @param <T>                 the type of elements in the collection
     * @return {@link Optional} containing the first element if exists, otherwise Optional.empty()
     * @throws IllegalArgumentException if sequencedCollection is not a sequenced collection
     */
    public static <T> Optional<T> firstIfPresent(Collection<T> sequencedCollection) {
        return isNotNullOrEmpty(sequencedCollection) ? Optional.of(first(sequencedCollection)) : Optional.empty();
    }

    /**
     * Return the first element in the given sequenced collection.
     *
     * @param sequencedCollection the sequenced collection
     * @param <T>                 the type of elements in the collection
     * @return the first element of the collection
     * @throws IllegalArgumentException if sequencedCollection is null, empty, or not a sequenced collection
     * @see #isSequenced(Collection)
     */
    public static <T> T first(Collection<T> sequencedCollection) {
        checkNotEmptyCollection(sequencedCollection);
        checkIsSequenced(sequencedCollection);

        if (sequencedCollection instanceof SortedSet) {
            return first((SortedSet<? extends T>) sequencedCollection);
        } else if (sequencedCollection instanceof LinkedHashSet) {
            return first((LinkedHashSet<? extends T>) sequencedCollection);
        } else if (sequencedCollection instanceof List) {
            return KiwiLists.first((List<? extends T>) sequencedCollection);
        }

        checkState(sequencedCollection instanceof Deque, "expected Deque but was %s", sequencedCollection.getClass());

        return first((Deque<? extends T>) sequencedCollection);
    }

    private static <T> T first(SortedSet<T> sortedSet) {
        return sortedSet.first();
    }

    private static <T> T first(LinkedHashSet<T> linkedHashSet) {
        return linkedHashSet.iterator().next();
    }

    private static <T> T first(Deque<T> deque) {
        return deque.peekFirst();
    }

    /**
     * Returns an {@link Optional} containing the last element in the given sequenced collection, or an empty optional
     * if the collection is null or empty.
     *
     * @param sequencedCollection the sequenced collection
     * @param <T>                 the type of elements in the collection
     * @return {@link Optional} containing last element if exists, otherwise Optional.empty()
     * @throws IllegalArgumentException if sequencedCollection is not a sequenced collection
     */
    public static <T> Optional<T> lastIfPresent(Collection<T> sequencedCollection) {
        return isNotNullOrEmpty(sequencedCollection) ? Optional.of(last(sequencedCollection)) : Optional.empty();
    }

    /**
     * Return the last element in the given sequenced collection.
     *
     * @param sequencedCollection the sequenced collection
     * @param <T>                 the type of elements in the collection
     * @return the last element of the collection
     * @throws IllegalArgumentException if sequencedCollection is null, empty, or not a sequenced collection
     * @implNote If {@code sequencedCollection} is a {@link LinkedHashSet}, there is no direct way to get the
     * last element. This implementation creates a {@link java.util.stream.Stream Stream} over the elements, skipping
     * until the last element.
     * @see #isSequenced(Collection)
     */
    public static <T> T last(Collection<T> sequencedCollection) {
        checkNotEmptyCollection(sequencedCollection);
        checkIsSequenced(sequencedCollection);

        if (sequencedCollection instanceof SortedSet) {
            return last((SortedSet<? extends T>) sequencedCollection);
        } else if (sequencedCollection instanceof LinkedHashSet) {
            return last((LinkedHashSet<? extends T>) sequencedCollection);
        } else if (sequencedCollection instanceof List) {
            return KiwiLists.last((List<? extends T>) sequencedCollection);
        }

        checkState(sequencedCollection instanceof Deque, "expected Deque but was %s", sequencedCollection.getClass());

        return last((Deque<? extends T>) sequencedCollection);
    }

    private static <T> T last(SortedSet<T> sortedSet) {
        return sortedSet.last();
    }

    private static <T> T last(LinkedHashSet<T> linkedHashSet) {
        var size = linkedHashSet.size();
        return linkedHashSet.stream().skip(size - 1L).findFirst().orElse(null);
    }

    private static <T> T last(Deque<T> deque) {
        return deque.peekLast();
    }

    /**
     * Checks that the given collection is not empty.
     *
     * @param collection the collection
     * @param <T>        the type of elements in the collection
     * @throws IllegalArgumentException if the given collection is null or empty
     */
    public static <T> void checkNotEmptyCollection(Collection<T> collection) {
        checkArgument(isNotNullOrEmpty(collection), "collection must contain at least one element");
    }

    /**
     * Checks that the given collection is not null.
     *
     * @param collection the collection
     * @param <T>        the type of elements in the collection
     * @throws NullPointerException if the collection is null
     */
    public static <T> void checkNonNullCollection(Collection<T> collection) {
        checkNotNull(collection, "collection must not be null");
    }

    private static <T> void checkIsSequenced(Collection<T> collection) {
        checkArgument(isSequenced(collection),
                "collection of type %s is not supported as a 'sequenced' collection",
                Optional.ofNullable(collection).map(coll -> coll.getClass().getName()).orElse("null collection"));
    }

    /**
     * Checks whether the given collection is "sequenced".
     * <p>
     * The definition of "sequenced" is based on <a href="https://openjdk.org/jeps/431">JEP 431: Sequenced Collections</a>
     * (as it existed on 2022-11-09).
     * <p>
     * The current {@link Collection} types (and their subtypes and implementations) that are considered sequenced
     * include {@link SortedSet}, {@link LinkedHashSet}, {@link List}, and {@link Deque}.
     *
     * @param collection the collection
     * @param <T>        the type of elements in the collection
     * @return true if the given collection is "sequenced"
     */
    public static <T> boolean isSequenced(Collection<T> collection) {
        return collection instanceof SortedSet ||
                collection instanceof LinkedHashSet ||
                collection instanceof List ||
                collection instanceof Deque;
    }
}
