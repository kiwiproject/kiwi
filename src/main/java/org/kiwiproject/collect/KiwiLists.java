package org.kiwiproject.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Utility methods for working with {@link List} instances.
 */
@UtilityClass
public class KiwiLists {

    /**
     * Checks whether the specified list is null or empty.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return {@code true} if list is null or empty; {@code false} otherwise
     */
    public static <T> boolean isNullOrEmpty(List<T> items) {
        return items == null || items.isEmpty();
    }

    /**
     * Checks whether the specified list is neither null nor empty.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return {@code true} if list is <em>NOT</em> null or empty; {@code false} otherwise
     */
    public static <T> boolean isNotNullOrEmpty(List<T> items) {
        return !isNullOrEmpty(items);
    }

    /**
     * Checks whether the specified list is non-null and has only one item.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return {@code true} if list is non-null and has exactly one item; {@code false} otherwise
     */
    public static <T> boolean hasOneElement(List<T> items) {
        return nonNull(items) && items.size() == 1;
    }

    /**
     * Given a list, sort it according to the natural order, returning a new list.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return a new sorted list
     */
    public static <T> List<T> sorted(List<T> items) {
        checkNonNullInputList(items);
        return items.stream().sorted().collect(toList());
    }

    /**
     * Given a list, sort it according to the provided {@link Comparator} returning a new list.
     *
     * @param items      the list
     * @param comparator a Comparator to be used to compare stream elements
     * @param <T>        the type of items in the list
     * @return a new sorted list
     */
    public static <T> List<T> sorted(List<T> items, Comparator<T> comparator) {
        checkNonNullInputList(items);
        checkNotNull(comparator, "Comparator cannot be null");
        return items.stream().sorted(comparator).collect(toList());
    }

    /**
     * Return the first element in the specified list of items.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the first item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least one item
     * @throws java.lang.NullPointerException     if the list is null
     */
    public static <T> T first(List<T> items) {
        return nth(items, 1);
    }

    /**
     * Returns an {@link Optional} containing the first element in specified list of items, or an empty optional
     * if the list is null or empty.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return Optional containing first element if exists, otherwise Optional.empty()
     */
    public static <T> Optional<T> firstIfPresent(List<T> items) {
        return isNotNullOrEmpty(items) ? Optional.of(first(items)) : Optional.empty();
    }

    /**
     * Return the second element in the specified list of items.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the second item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least two items
     * @throws java.lang.NullPointerException     if the list is null
     */
    public static <T> T second(List<T> items) {
        return nth(items, 2);
    }

    /**
     * Return the third element in the specified list of items.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the third item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least three items
     * @throws java.lang.NullPointerException     if the list is null
     */
    public static <T> T third(List<T> items) {
        return nth(items, 3);
    }

    /**
     * Return the fourth element in the specified list of items.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the fourth item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least four items
     * @throws java.lang.NullPointerException     if the list is null
     */
    public static <T> T fourth(List<T> items) {
        return nth(items, 4);
    }

    /**
     * Return the fifth element in the specified list of items.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the fifth item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least five items
     * @throws java.lang.NullPointerException     if the list is null
     */
    public static <T> T fifth(List<T> items) {
        return nth(items, 5);
    }

    /**
     * Returns the penultimate (second to last) element in the specified list.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the penultimate item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least two items
     * @throws java.lang.NullPointerException     if the list is null
     * @see #secondToLast(List)
     */
    public static <T> T penultimate(List<T> items) {
        checkMinimumSize(items, 2);
        return nth(items, items.size() - 1);
    }

    /**
     * Synonym for {@link #penultimate(List)}.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the penultimate item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least two items
     * @throws java.lang.NullPointerException     if the list is null
     * @see #penultimate(List)
     */
    public static <T> T secondToLast(List<T> items) {
        return penultimate(items);
    }

    /**
     * Returns the last element in the specified list of items.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return the last item in the list
     * @throws java.lang.IllegalArgumentException if the list does not contain at least one item
     * @throws java.lang.NullPointerException     if the list is null
     */
    public static <T> T last(List<T> items) {
        return nth(items, items.size());
    }

    /**
     * Returns an {@link Optional} containing the last element in specified list of items, or an empty optional
     * if the list is null or empty.
     *
     * @param items the list
     * @param <T>   the type of items in the list
     * @return Optional containing last element if exists, otherwise Optional.empty()
     */
    public static <T> Optional<T> lastIfPresent(List<T> items) {
        return isNotNullOrEmpty(items) ? Optional.of(last(items)) : Optional.empty();
    }

    /**
     * Return the nth element in the specified list of items, starting at one for the first element, two for the
     * second, etc.
     *
     * @param items  the list
     * @param number the number of the element to retrieve, starting at one (<i>not zero</i>)
     * @param <T>    the type of items in the list
     * @return the nth item in items
     * @throws java.lang.IllegalArgumentException if the list does not contain at least number items
     * @throws java.lang.NullPointerException     if the list is null
     */
    public static <T> T nth(List<T> items, int number) {
        checkMinimumSize(items, number);
        return items.get(number - 1);
    }

    /**
     * Returns a list of the collection elements with duplicates stripped out.
     *
     * @param collection the collection of values
     * @param <T>        the type of items in the collection
     * @return a new list with only unique elements
     * @throws java.lang.IllegalArgumentException if the collection is null
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        checkArgument(nonNull(collection), "collection can not be null");
        return distinctOrNull(collection);
    }

    /**
     * Returns a list of the collection elements with duplicates stripped out or `null` if a null value is passed in.
     *
     * @param collection the collection of values
     * @param <T>        the type of items in the collection
     * @return a new list with only unique elements or null.
     */
    public static <T> List<T> distinctOrNull(Collection<T> collection) {
        return nonNull(collection) ? collection.stream().distinct().collect(toList()) : null;
    }

    @VisibleForTesting
    static <T> void checkMinimumSize(List<T> items, int minSize) {
        checkNonNullInputList(items);
        checkArgument(minSize > 0, "number must be positive");
        checkArgument(items.size() >= minSize,
                "expected at least %s items (actual size: %s)",
                minSize, items.size());
    }

    public static <T> void checkNonNullInputList(List<T> items) {
        checkNotNull(items, "items cannot be null");
    }

    /**
     * Returns a new list with the same elements and the same size as the original, however the initial position in the list
     * is now the element specified by the "startOffset" and the list wraps around through the contents to end with "startOffset" - 1
     *
     * @param input       the original list
     * @param startOffset the desired offset to start the new list
     * @param <T>         the type of the items in the list
     * @return a new list starting at the desired offset
     */
    public static <T> List<T> newListStartingAtCircularOffset(List<T> input, long startOffset) {
        var size = input.size();
        return IntStream.range(0, size).mapToObj(i -> input.get((int) (startOffset + i) % size)).collect(toList());
    }
}
