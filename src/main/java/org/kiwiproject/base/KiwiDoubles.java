package org.kiwiproject.base;

import com.google.common.math.DoubleMath;
import lombok.experimental.UtilityClass;

/**
 * {@link Double} utilities. Mainly wrappers around existing method in {@link Double} and {@link DoubleMath} that
 * are (in our opinion anyway) easier to work with or read the code, e.g. return a boolean for comparisons instead
 * of an int.
 */
@UtilityClass
public class KiwiDoubles {

    /**
     * Default tolerance to use for comparisons when not explicitly specified.
     */
    public static final double DEFAULT_FUZZY_EQUALS_TOLERANCE = 1E-9;

    /**
     * Return true if value is exactly equal to zero.
     *
     * @param value the value to check
     * @return true if the value is zero
     * @see Double#compare(double, double)
     */
    public static boolean isZero(double value) {
        return areEqual(value, 0.0);
    }

    /**
     * Return true if value is not exactly equal to zero.
     *
     * @param value the value to check
     * @return true if the value is not zero
     * @see Double#compare(double, double)
     */
    public static boolean isNotZero(double value) {
        return areNotEqual(value, 0.0);
    }

    /**
     * Return true if value is close to zero, within the default tolerance.
     *
     * @param value the value to check
     * @return true if the value is close to zero using the {@link #DEFAULT_FUZZY_EQUALS_TOLERANCE}
     * @see DoubleMath#fuzzyEquals(double, double, double)
     * @see #DEFAULT_FUZZY_EQUALS_TOLERANCE
     */
    public static boolean isCloseToZero(double value) {
        return isCloseToZero(value, DEFAULT_FUZZY_EQUALS_TOLERANCE);
    }

    /**
     * Return true if value is close to zero, using the given tolerance.
     *
     * @param value     the value to check
     * @param tolerance how far away the value can be from zero while still considered equal to zero
     * @return true if the value is close to zero
     * @see DoubleMath#fuzzyEquals(double, double, double)
     */
    public static boolean isCloseToZero(double value, double tolerance) {
        return DoubleMath.fuzzyEquals(value, 0.0, tolerance);
    }

    /**
     * Return true if value1 is numerically equal to value2.
     *
     * @param value1 the first value to compare
     * @param value2 the second value to compare
     * @return true if value1 equals value2
     * @see Double#compare(double, double)
     */
    public static boolean areEqual(double value1, double value2) {
        return Double.compare(value1, value2) == 0;
    }

    /**
     * Return true if value1 is not numerically equal to value2.
     *
     * @param value1 the first value to compare
     * @param value2 the second value to compare
     * @return true if value1 does not equal value2
     * @see Double#compare(double, double)
     */
    public static boolean areNotEqual(double value1, double value2) {
        return !areEqual(value1, value2);
    }

    /**
     * Return true if value1 is close to value2, within the default tolerance.
     *
     * @param value1 the first value to compare
     * @param value2 the second value to compare
     * @return true if value1 is close to value2 using the {@link #DEFAULT_FUZZY_EQUALS_TOLERANCE}
     * @see DoubleMath#fuzzyEquals(double, double, double)
     * @see #DEFAULT_FUZZY_EQUALS_TOLERANCE
     */
    public static boolean areClose(double value1, double value2) {
        return areClose(value1, value2, DEFAULT_FUZZY_EQUALS_TOLERANCE);
    }

    /**
     * Return true if value1 is close to value2, within the given tolerance.
     *
     * @param value1    the first value to compare
     * @param value2    the second value to compare
     * @param tolerance how far away the values can be from each other while still considered equal
     * @return true if value1 is close to value2 using the {@link #DEFAULT_FUZZY_EQUALS_TOLERANCE}
     * @see DoubleMath#fuzzyEquals(double, double, double)
     */
    public static boolean areClose(double value1, double value2, double tolerance) {
        return DoubleMath.fuzzyEquals(value1, value2, tolerance);
    }
}
