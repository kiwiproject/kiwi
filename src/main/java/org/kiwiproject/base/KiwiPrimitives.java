package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.experimental.UtilityClass;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

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
     * @return the parsed {@code int} value if successful; otherwise if it cannot be parsed, this method always throws an exception
     * @throws IllegalStateException if the value cannot be parsed
     */
    public static int tryParseIntOrThrow(CharSequence cs) {
        try {
            return Integer.parseInt(cs.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@link Long}.
     *
     * @param cs the value to parse
     * @return the value as a Long or {@code null} if the value cannot be parsed
     */
    public static Long tryParseLongOrNull(CharSequence cs) {
        try {
            return Long.valueOf(cs.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@code long}.
     *
     * @param cs the value to parse
     * @return an {@link OptionalLong} that will contain the parsed value or will be empty if the input cannot be parsed
     */
    public static OptionalLong tryParseLong(CharSequence cs) {
        try {
            return OptionalLong.of(Long.parseLong(cs.toString()));
        } catch (Exception e) {
            return OptionalLong.empty();
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@code long}.
     *
     * @param cs the value to parse
     * @return the parsed {@code long} value if successful; otherwise if it cannot be parsed this method always throws an exception
     * @throws IllegalStateException if the value cannot be parsed
     */
    public static long tryParseLongOrThrow(CharSequence cs) {
        try {
            return Long.parseLong(cs.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@link Double}.
     *
     * @param cs the value to parse
     * @return the value as a Double or {@code null} if the value cannot be parsed
     */
    public static Double tryParseDoubleOrNull(CharSequence cs) {
        try {
            return Double.valueOf(cs.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@code double}.
     *
     * @param cs the value to parse
     * @return an {@link OptionalDouble} that will contain the parsed value or will be empty if the input cannot be parsed
     */
    public static OptionalDouble tryParseDouble(CharSequence cs) {
        try {
            return OptionalDouble.of(Double.parseDouble(cs.toString()));
        } catch (Exception e) {
            return OptionalDouble.empty();
        }
    }

    /**
     * Attempt to parse the given {@link CharSequence} to an {@code double}.
     *
     * @param cs the value to parse
     * @return the parsed {@code double} value if successful; otherwise if it cannot be parsed this method always throws an exception
     * @throws IllegalStateException if the value cannot be parsed
     */
    public static double tryParseDoubleOrThrow(CharSequence cs) {
        try {
            return Double.parseDouble(cs.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Enum representing options for converting a numeric value into a boolean.
     */
    public enum BooleanConversionOption {

        /**
         * Convert numeric values into boolean where one represents true and zero
         * represents false. No other values are allowed.
         */
        ZERO_OR_ONE,

        /**
         * Convert numeric values into boolean where any non-zero value
         * represents true, and zero represents false.
         */
        NON_ZERO_AS_TRUE
    }

    /**
     * Converts the given long value to a boolean. The value must be zero or one.
     *
     * @param value the value to convert
     * @return true if the value is one, or false if zero
     * @throws IllegalArgumentException if the value in the column is not zero, one, or NULL
     * @see #booleanFromLong(long, BooleanConversionOption)
     */
    public static boolean booleanFromLong(long value) {
        return booleanFromLong(value, BooleanConversionOption.ZERO_OR_ONE);
    }

    /**
     * Converts the given long value to a boolean using the specified {@link BooleanConversionOption}.
     *
     * @param value the value to convert
     * @param option how to convert the long value into a boolean
     * @return true if the value is non-zero, otherwise false
     */
    public static boolean booleanFromLong(long value, BooleanConversionOption option) {
        return switch (option) {
            case ZERO_OR_ONE -> {
                checkArgument(value == 0 || value == 1, "value must be 0 or 1, but found %s", value);
                yield value == 1;
            }
            case NON_ZERO_AS_TRUE -> value != 0;
        };
    }
}
