package org.kiwiproject.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Utility methods for working with Array instances.
 */
@UtilityClass
public class KiwiArrays {

    /**
     * Checks whether the specified array is null or empty.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return {@code true} if the array is null or empty; {@code false} otherwise
     */
    public static <T> boolean isNullOrEmpty(T[] items) {
        return items == null || items.length == 0;
    }

    /**
     * Checks whether the specified array is neither null nor empty.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return {@code true} if array is <em>NOT</em> null or empty; {@code false} otherwise
     */
    public static <T> boolean isNotNullOrEmpty(T[] items) {
        return !isNullOrEmpty(items);
    }

    /**
     * Checks whether the specified array is non-null and has only one item.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return {@code true} if the array is non-null and has exactly one item; {@code false} otherwise
     */
    public static <T> boolean hasOneElement(T[] items) {
        return nonNull(items) && items.length == 1;
    }

    /**
     * Given an array, sort it according to the natural order, returning a new list.
     *
     * @param items the array
     * @param arrayType   the type of the items in the array. Needed to ensure the new array is not {@code Object[]}
     * @param <T>   the type of items in the array
     * @return a new sorted array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] sorted(T[] items, Class<T> arrayType) {
        checkNonNullInputArray(items);
        return Arrays.stream(items)
                .sorted()
                .toArray(size -> (T[]) Array.newInstance(arrayType, size));
    }

    /**
     * Given an array, sort it according to the provided {@link Comparator} returning a new array.
     *
     * @param items      the array
     * @param comparator a Comparator to be used to compare stream elements
     * @param arrayType   the type of the items in the array. Needed to ensure the new array is not {@code Object[]}
     * @param <T>        the type of items in the array
     * @return a new sorted array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] sorted(T[] items, Comparator<T> comparator, Class<T> arrayType) {
        checkNonNullInputArray(items);
        checkNotNull(comparator, "Comparator cannot be null");
        return Arrays.stream(items)
                .sorted(comparator)
                .toArray(size -> (T[]) Array.newInstance(arrayType, size));
    }

    /**
     * Return the first element in the specified array of items.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the first item in items
     * @throws IllegalArgumentException if the array does not contain at least one item
     * @throws NullPointerException     if the array is null
     */
    public static <T> T first(T[] items) {
        return nth(items, 1);
    }

    /**
     * Returns an {@link Optional} containing the first element in the specified array of items, or an empty optional
     * if the array is null or empty.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return Optional containing the first element if exists, otherwise Optional.empty()
     */
    public static <T> Optional<T> firstIfPresent(T[] items) {
        return isNotNullOrEmpty(items) ? Optional.of(first(items)) : Optional.empty();
    }

    /**
     * Return the second element in the specified array of items.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the second item in items
     * @throws IllegalArgumentException if the array does not contain at least two items
     * @throws NullPointerException     if the array is null
     */
    public static <T> T second(T[] items) {
        return nth(items, 2);
    }

    /**
     * Return the third element in the specified array of items.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the third item in items
     * @throws IllegalArgumentException if the array does not contain at least three items
     * @throws NullPointerException     if the array is null
     */
    public static <T> T third(T[] items) {
        return nth(items, 3);
    }

    /**
     * Return the fourth element in the specified array of items.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the fourth item in items
     * @throws IllegalArgumentException if the array does not contain at least four items
     * @throws NullPointerException     if the array is null
     */
    public static <T> T fourth(T[] items) {
        return nth(items, 4);
    }

    /**
     * Return the fifth element in the specified array of items.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the fifth item in items
     * @throws IllegalArgumentException if the array does not contain at least five items
     * @throws NullPointerException     if the array is null
     */
    public static <T> T fifth(T[] items) {
        return nth(items, 5);
    }

    /**
     * Returns the penultimate (second to last) element in the specified array.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the penultimate item in items
     * @throws IllegalArgumentException if the array does not contain at least two items
     * @throws NullPointerException     if the array is null
     * @see #secondToLast(Object[])
     */
    public static <T> T penultimate(T[] items) {
        checkMinimumSize(items, 2);
        return nth(items, items.length - 1);
    }

    /**
     * Synonym for {@link #penultimate(Object[])}.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the penultimate item in items
     * @throws IllegalArgumentException if the array does not contain at least two items
     * @throws NullPointerException     if the array is null
     * @see #penultimate(Object[])
     */
    public static <T> T secondToLast(T[] items) {
        return penultimate(items);
    }

    /**
     * Returns the last element in the specified array of items.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return the last item in the list
     * @throws IllegalArgumentException if the array does not contain at least one item
     * @throws NullPointerException     if the array is null
     */
    public static <T> T last(T[] items) {
        return nth(items, items.length);
    }

    /**
     * Returns an {@link Optional} containing the last element in the specified array of items, or an empty optional
     * if the array is null or empty.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @return Optional containing last element if exists, otherwise Optional.empty()
     */
    public static <T> Optional<T> lastIfPresent(T[] items) {
        return isNotNullOrEmpty(items) ? Optional.of(last(items)) : Optional.empty();
    }

    /**
     * Return the nth element in the specified array of items, starting at one for the first element, two for the
     * second, etc.
     *
     * @param items  the array
     * @param number the number of the element to retrieve, starting at one (<i>not zero</i>)
     * @param <T>    the type of items in the array
     * @return the nth item in items
     * @throws IllegalArgumentException if the array does not contain at least number items
     * @throws NullPointerException     if the array is null
     */
    public static <T> T nth(T[] items, int number) {
        checkMinimumSize(items, number);
        return items[number - 1];
    }

    /**
     * Returns an array of the collection elements with duplicates stripped out.
     *
     * @param collection the collection of values
     * @param arrayType   the type of the items in the array. Needed to ensure the new array is not {@code Object[]}
     * @param <T>        the type of items in the collection
     * @return a new array with only unique elements
     * @throws IllegalArgumentException if the collection is null
     */
    public static <T> T[] distinct(T[] collection, Class<T> arrayType) {
        checkArgument(nonNull(collection), "collection can not be null");
        return distinctOrNull(collection, arrayType);
    }

    /**
     * Returns an array of the collection elements with duplicates stripped out or `null` if a null value is passed in.
     *
     * @param collection the collection of values
     * @param arrayType   the type of the items in the array. Needed to ensure the new array is not {@code Object[]}
     * @param <T>        the type of items in the collection
     * @return a new array with only unique elements or null.
     */
    @SuppressWarnings({"unchecked", "java:S1168"}) //Ignoring Sonar warning to return an empty array since this method's name says it will return null
    public static <T> T[] distinctOrNull(T[] collection, Class<T> arrayType) {
        if (isNull(collection)) {
            return null;
        }

        return Arrays.stream(collection)
                .distinct()
                .toArray(val -> (T[]) Array.newInstance(arrayType, val));
    }

    /**
     * Returns a new array with the same elements and the same size as the original.
     * However, the initial position in the array is now the element specified by the "startOffset"
     * and the array wraps around through the contents to end with ("startOffset" - 1).
     *
     * @param input       the original array
     * @param startOffset the desired offset to start the new array
     * @param arrayType   the type of the items in the array. Needed to ensure the new array is not {@code Object[]}
     * @param <T>         the type of the items in the array
     * @return a new array starting at the desired offset
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArrayStartingAtCircularOffset(T[] input, long startOffset, Class<T> arrayType) {
        var size = input.length;
        return IntStream.range(0, size)
                .mapToObj(i -> input[(int) (startOffset + i) % size])
                .toArray(val -> (T[]) Array.newInstance(arrayType, val));
    }

    /**
     * Returns a new array containing the portion of the given array excluding the first element.
     *
     * @param items the array
     * @param <T>   the type of the items in the array
     * @return a new array containing the given array excluding the first item
     * @throws NullPointerException if the array is null
     */
    public static <T> T[] subArrayExcludingFirst(T[] items) {
        checkNonNullInputArray(items);
        if (items.length == 0) {
            return zeroSubArray(items);
        }
        return Arrays.copyOfRange(items, 1, items.length);
    }

    /**
     * Returns a new array containing the portion of the given array excluding the last element.
     *
     * @param items the array
     * @param <T>   the type of the items in the array
     * @return a new array containing the given array excluding the last item
     * @throws NullPointerException if the array is null
     */
    public static <T> T[] subArrayExcludingLast(T[] items) {
        checkNonNullInputArray(items);
        if (items.length == 0) {
            return zeroSubArray(items);
        }
        return Arrays.copyOfRange(items, 0, items.length-1);
    }

    private static <T> T[] zeroSubArray(T[] items) {
        return Arrays.copyOf(items, 0);
    }

    /**
     * Returns a new array containing the portion of the given array starting at the given logical element number, where the
     * numbers start at one, until and including the last element in the array. This is useful if something is using
     * one-based element numbers for some reason. Use {@link #subArrayFromIndex(Object[], int)} if you want to use zero-based
     * list indices.
     *
     * @param items  the array
     * @param number the number of the element to start the subarray, starting at one (<i>not zero</i>)
     * @param <T>    the type of items in the array
     * @return a new array containing the given array, starting at the given one-based number
     * @throws NullPointerException     if the array is null
     * @throws IllegalArgumentException if the given number is negative or is higher than the size of the array
     */
    public static <T> T[] subArrayFrom(T[] items, int number) {
        checkMinimumSize(items, number);
        return Arrays.copyOfRange(items, number - 1, items.length);
    }

    /**
     * Returns a new array containing the portion of the given array starting at the given index, until and including the last
     * element in the array.
     *
     * @param items the array
     * @param index the index in the array to start the subarray, zero-based like normal Array accessors
     * @param <T>   the type of items in the array
     * @return a new array containing the given array, starting at the given zero-based index
     * @throws NullPointerException     if the array is null
     * @throws IllegalArgumentException if the given index is negative or is higher than the last index in the array
     */
    public static <T> T[] subArrayFromIndex(T[] items, int index) {
        checkMinimumSize(items, index + 1);
        return Arrays.copyOfRange(items, index, items.length);
    }

    /**
     * Returns a new array containing the "first N" elements of the input array.
     * <p>
     * If the given number is larger than the size of the array, the entire array is returned, rather than throw
     * an exception. In this case, the input array is returned directly, i.e. {@code return items}.
     *
     * @param items  the array
     * @param number the number of items wanted from the start of the array
     * @param <T>    the type of items in the array
     * @return a new array containing the given array containing the last {@code number} elements
     */
    public static <T> T[] firstN(T[] items, int number) {
        checkNonNullInputArray(items);
        checkMinSizeIsPositive(number);
        if (number > items.length) {
            return items;
        }
        return Arrays.copyOfRange(items, 0, number);
    }

    /**
     * Returns a new array containing the "last N" elements of the input array.
     * <p>
     * If the given number is larger than the size of the array, the entire array is returned, rather than throw
     * an exception. In this case, the input array is returned directly, i.e. {@code return items}.
     *
     * @param items  the array
     * @param number the number of items wanted from the end of the array
     * @param <T>    the type of items in the array
     * @return a new array containing the given array containing the first {@code number} elements
     */
    public static <T> T[] lastN(T[] items, int number) {
        checkNonNullInputArray(items);
        checkMinSizeIsPositive(number);
        if (number > items.length) {
            return items;
        }
        var startIndex = items.length - number;
        return Arrays.copyOfRange(items, startIndex, items.length);
    }

    /**
     * Checks that the given array is not null and has the given minimum size.
     *
     * @param items   the array
     * @param minSize the minimum required size
     * @param <T>     the type of the items in the array
     * @throws NullPointerException     if the array is null
     * @throws IllegalArgumentException if minSize is not positive or the array does not contain minSize elements
     */
    public static <T> void checkMinimumSize(T[] items, int minSize) {
        checkNonNullInputArray(items);
        checkMinSizeIsPositive(minSize);
        checkArgument(items.length >= minSize,
                "expected at least %s items (actual size: %s)",
                minSize, items.length);
    }

    private static void checkMinSizeIsPositive(int minSize) {
        checkArgument(minSize > 0, "number must be positive");
    }

    /**
     * Checks that the given array is not null.
     *
     * @param items the array
     * @param <T>   the type of items in the array
     * @throws NullPointerException if the array is null
     */
    public static <T> void checkNonNullInputArray(T[] items) {
        checkNotNull(items, "items cannot be null");
    }
}
