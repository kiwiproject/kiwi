package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.experimental.UtilityClass;

import java.util.OptionalInt;

/**
 * Static utilities that operate on primitive values, and are not already provided by classes in Guava's
 * {@link com.google.common.primitives} package.
 */
@UtilityClass
public class KiwiPrimitives {

    /**
     * Returns the first non-zero argument, otherwise throws {@link IllegalArgumentException} if both arguments
     * are zero.
     *
     * @param first  the first int to check
     * @param second the second int to check
     * @return the first non-zero value
     * @throws IllegalArgumentException if both arguments are zero
     */
    public static int firstNonZero(int first, int second) {
        return first != 0 ? first : (int) checkNonZero(second);
    }

    /**
     * Returns the first non-zero argument, otherwise throws {@link IllegalArgumentException} if both arguments
     * are zero.
     *
     * @param first  the first int to check
     * @param second the second int to check
     * @return the first non-zero value
     * @throws IllegalArgumentException if both arguments are zero
     */
    public static long firstNonZero(long first, long second) {
        return first != 0 ? first : checkNonZero(second);
    }

    private static long checkNonZero(long value) {
        checkArgument(value != 0, "One of the arguments must be non-zero");
        return value;
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@link Integer}.
     *
     * @param cs the value to parse
     * @return the value as an Integer or {@code null} if the value cannot be parsed
     */
    public static Integer tryParseIntOrNull(CharSequence cs) {
        try {
            return Integer.valueOf(cs.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@code int}.
     *
     * @param cs the value to parse
     * @return an {@link OptionalInt} that will contain the parsed value or will be empty if the input cannot be parsed
     */
    public static OptionalInt tryParseInt(CharSequence cs) {
        try {
            return OptionalInt.of(Integer.parseInt(cs.toString()));
        } catch (Exception e) {
            return OptionalInt.empty();
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@code int}.
     *
     * @param cs the value to parse
     * @return the parsed {@code int} value if successful; if it cannot be parsed this method always throws an exception
     * @throws IllegalStateException if the value cannot be parsed
     */
    public static int tryParseIntOrThrow(CharSequence cs) {
        try {
            return Integer.parseInt(cs.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
