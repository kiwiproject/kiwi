package org.kiwiproject.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        return items.stream().sorted().toList();
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
        return items.stream().sorted(comparator).toList();
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
        return  distinctListFrom(collection);
    }

    /**
     * Returns a list of the collection elements with duplicates stripped out or `null` if a null value is passed in.
     *
     * @param collection the collection of values
     * @param <T>        the type of items in the collection
     * @return a new list with only unique elements or null.
     */
    public static <T> List<T> distinctOrNull(Collection<T> collection) {
        return nonNull(collection) ? distinctListFrom(collection) : null;
    }

    /**
     * Returns a list of the collection elements with duplicates stripped out or an empty list if the input collection
     * is null (or empty).
     *
     * @param collection the collection of values
     * @param <T>        the type of items in the collection
     * @return a new list with only unique elements or an empty list
     */
    public static <T> List<T> distinctOrEmpty(Collection<T> collection) {
        return nonNull(collection) ? distinctListFrom(collection) : new ArrayList<>();
    }

    private static <T> List<T> distinctListFrom(Collection<T> collection) {
        return collection.stream().distinct().toList();
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
        return IntStream.range(0, size).mapToObj(i -> input.get((int) (startOffset + i) % size)).toList();
    }

    /**
     * Returns a view of the portion of the given list excluding the first element.
     * <p>
     * This method has the same semantics as {@link List#subList(int, int)} since it calls that method.
     *
     * @param items the list
     * @param <T>   the type of the items in the list
     * @return a view of the given list excluding the first item backed by the original list
     * @throws NullPointerException if the list is null
     * @see List#subList(int, int)
     */
    public static <T> List<T> subListExcludingFirst(List<T> items) {
        checkNonNullInputList(items);
        if (items.isEmpty()) {
            return zeroSubList(items);
        }
        return items.subList(1, items.size());
    }

    /**
     * Returns a view of the portion of the given list excluding the last element.
     * <p>
     * This method has the same semantics as {@link List#subList(int, int)} since it calls that method.
     *
     * @param items the list
     * @param <T>   the type of the items in the list
     * @return a view of the given list excluding the last item backed by the original list
     * @throws NullPointerException if the list is null
     * @see List#subList(int, int)
     */
    public static <T> List<T> subListExcludingLast(List<T> items) {
        checkNonNullInputList(items);
        if (items.isEmpty()) {
            return zeroSubList(items);
        }
        return items.subList(0, items.size() - 1);
    }

    private static <T> List<T> zeroSubList(List<T> items) {
        return items.subList(0, 0);
    }

    /**
     * Returns a view of the portion of the given list starting at the given logical element number, where the
     * numbers start at one, until and including the last element in the list. This is useful if something is using
     * one-based element numbers for some reason. Use {@link #subListFromIndex(List, int)} if you want to use zero-based
     * list indices.
     * <p>
     * This method has the same semantics as {@link List#subList(int, int)} since it calls that method.
     *
     * @param items  the list
     * @param number the number of the element to start the sublist, starting at one (<i>not zero</i>)
     * @param <T>    the type of items in the list
     * @return a view of the given list backed by the original list, starting at the given one-based number
     * @throws NullPointerException     if the list is null
     * @throws IllegalArgumentException if the given number is negative or is higher than the size of the list
     * @see List#subList(int, int)
     */
    public static <T> List<T> subListFrom(List<T> items, int number) {
        checkMinimumSize(items, number);
        return items.subList(number - 1, items.size());
    }

    /**
     * Returns a view of the portion of the given list starting at the given index, until and including the last
     * element in the list.
     * <p>
     * This method has the same semantics as {@link List#subList(int, int)} since it calls that method.
     *
     * @param items the list
     * @param index the index in the list to start the sublist, zero-based like normal List methods
     * @param <T>   the type of items in the list
     * @return a view of the given list backed by the original list, starting at the given zero-based index
     * @throws NullPointerException     if the list is null
     * @throws IllegalArgumentException if the given index is negative or is higher than the last index in the list
     * @see List#subList(int, int)
     */
    public static <T> List<T> subListFromIndex(List<T> items, int index) {
        checkMinimumSize(items, index + 1);
        return items.subList(index, items.size());
    }

    /**
     * Returns a view of the "first N" elements of the input list.
     * <p>
     * If the given number is larger than the size of the list, the entire list is returned, rather than throw
     * an exception. In this case, the input list is returned directly, i.e. {@code return items}.
     * <p>
     * This method has the same semantics as {@link List#subList(int, int)} since it calls that method.
     *
     * @param items  the list
     * @param number the number of items wanted from the start of the list
     * @param <T>    the type of items in the list
     * @return a view of the given list, backed by the original list, containing the last {@code number} elements
     * @see List#subList(int, int)
     */
    public static <T> List<T> firstN(List<T> items, int number) {
        checkNonNullInputList(items);
        checkMinSizeIsPositive(number);
        if (number > items.size()) {
            return items;
        }
        return items.subList(0, number);
    }

    /**
     * Returns a view of the "last N" elements of the input list.
     * <p>
     * If the given number is larger than the size of the list, the entire list is returned, rather than throw
     * an exception. In this case, the input list is returned directly, i.e. {@code return items}.
     * <p>
     * This method has the same semantics as {@link List#subList(int, int)} since it calls that method.
     *
     * @param items  the list
     * @param number the number of items wanted from the end of the list
     * @param <T>    the type of items in the list
     * @return a view of the given list, backed by the original list, containing the first {@code number} elements
     * @see List#subList(int, int)
     */
    public static <T> List<T> lastN(List<T> items, int number) {
        checkNonNullInputList(items);
        checkMinSizeIsPositive(number);
        if (number > items.size()) {
            return items;
        }
        var startIndex = items.size() - number;
        return items.subList(startIndex, items.size());
    }

    /**
     * Checks that the given list is not null and has the given minimum size.
     *
     * @param items   the list
     * @param minSize the minimum required size
     * @param <T>     the type of the items in the list
     * @throws NullPointerException     if the list is null
     * @throws IllegalArgumentException if minSize is not positive or the list does not contain minSize elements
     */
    public static <T> void checkMinimumSize(List<T> items, int minSize) {
        checkNonNullInputList(items);
        checkMinSizeIsPositive(minSize);
        checkArgument(items.size() >= minSize,
                "expected at least %s items (actual size: %s)",
                minSize, items.size());
    }

    private static void checkMinSizeIsPositive(int minSize) {
        checkArgument(minSize > 0, "number must be positive");
    }

    /**
     * Checks that the given list is not null.
     *
     * @param items the list
     * @param <T>   the type of the items in the list
     * @throws NullPointerException if the list is null
     */
    public static <T> void checkNonNullInputList(List<T> items) {
        checkNotNull(items, "items cannot be null");
    }

    /**
     * Create a new unmodifiable {@link List} with the given items shuffled using {@link Collections#shuffle(List)}.
     *
     * @param items the items to include in the new list
     * @param <T>   the type of the items in the list
     * @return an unmodifiable List with the given items in pseudorandom order
     */
    @SafeVarargs
    public static <T> List<T> shuffledListOf(T... items) {
        return List.copyOf(shuffledArrayListOf(items));
    }

    /**
     * Create a new {@link ArrayList} with the given items shuffled using {@link Collections#shuffle(List)}.
     *
     * @param items the items to include in the new list
     * @param <T>   the type of the items in the list
     * @return an ArrayList with the given items in pseudorandom order
     */
    @SafeVarargs
    public static <T> List<T> shuffledArrayListOf(T... items) {
        var list = new ArrayList<T>(items.length);
        Collections.addAll(list, items);
        Collections.shuffle(list);
        return list;
    }
}
