package org.kiwiproject.time;

import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * Utilities related to Java's {@link Duration} class.
 */
@UtilityClass
public class KiwiDurations {

    /**
     * Check if a {@link Duration} is positive.
     *
     * @param duration the duration to check
     * @return true if the duration is strictly positive, otherwise false
     */
    public static boolean isPositive(Duration duration) {
        return !duration.isNegative() && !duration.isZero();
    }

    /**
     * Check if a {@link Duration} is positive or zero.
     *
     * @param duration the duration to check
     * @return true if the duration is positive or zero, otherwise false
     */
    public static boolean isPositiveOrZero(Duration duration) {
        return !duration.isNegative();
    }

    /**
     * Check if a {@link Duration} is negative or zero.
     *
     * @param duration the duration to check
     * @return true if the duration is negative or zero, otherwise false
     */
    public static boolean isNegativeOrZero(Duration duration) {
        return duration.isNegative() || duration.isZero();
    }
}
