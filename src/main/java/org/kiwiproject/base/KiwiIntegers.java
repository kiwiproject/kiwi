package org.kiwiproject.base;

import static java.util.Objects.isNull;

import lombok.experimental.UtilityClass;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utilities for working with {@link Integer} wrapper objects.
 */
@UtilityClass
public class KiwiIntegers {

    /**
     * Return the {@code int} value of the {@link Integer} when non-null,
     * otherwise return zero.
     *
     * @param integerObject the possibly null Integer object
     * @return the {@code int} value of the Integer object when non-null, otherwise {@code 0} (zero)
     */
    public static int toIntOrZero(@Nullable Integer integerObject) {
        return toIntOrDefault(integerObject, 0);
    }

    /**
     * Return the {@code int} value of the {@link Integer} when non-null,
     * otherwise return the default value.
     *
     * @param integerObject the possibly null Integer object
     * @param defaultValue the value to use when the Integer argument is null
     * @return the {@code int} value of the Integer object when non-null, otherwise {@code defaultValue}
     */
    public static int toIntOrDefault(@Nullable Integer integerObject, int defaultValue) {
        return isNull(integerObject) ? defaultValue : integerObject.intValue();
    }
}
