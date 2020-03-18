package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.experimental.UtilityClass;

/**
 * Static utilities that operate on primitive values, and are not already provided by classes in Guava's
 * {@link com.google.common.primitives} package.
 */
@UtilityClass
public class KiwiPrimitives {

    /**
     * Returns the first non-zero argument, otherwise throws {@link IllegalArgumentException} if both arguments
     * are zero.
     */
    public static int firstNonZero(int first, int second) {
        return first != 0 ? first : (int) checkNonZero(second);
    }

    /**
     * Returns the first non-zero argument, otherwise throws {@link IllegalArgumentException} if both arguments
     * are zero.
     */
    public static long firstNonZero(long first, long second) {
        return first != 0 ? first : checkNonZero(second);
    }

    private static long checkNonZero(long value) {
        checkArgument(value != 0, "One of the arguments must be non-zero");
        return value;
    }
}
