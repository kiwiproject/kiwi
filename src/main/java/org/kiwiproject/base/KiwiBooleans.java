package org.kiwiproject.base;

import static java.util.Objects.isNull;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utilities for working with {@link Boolean} wrapper objects.
 */
@UtilityClass
public class KiwiBooleans {

    /**
     * Return the {@code boolean} value of the {@link Boolean} when non-null,
     * otherwise return {@code true}.
     *
     * @param booleanObject the possibly null Boolean object
     * @return the boolean value of the Boolean object when non-null, otherwise {@code true}
     */
    public static boolean toBooleanOrTrue(@Nullable Boolean booleanObject) {
        return toBooleanOrDefault(booleanObject, true);
    }

     /**
     * Return the {@code boolean} value of the {@link Boolean} when non-null,
     * otherwise return {@code false}.
     *
     * @param booleanObject the possibly null Boolean object
     * @return the boolean value of the Boolean object when non-null, otherwise {@code false}
     */
    public static boolean toBooleanOrFalse(@Nullable Boolean booleanObject) {
        return toBooleanOrDefault(booleanObject, false);
    }

    /**
     * Return the {@code boolean} value of the {@link Boolean} when non-null,
     * otherwise return the default value.
     *
     * @param booleanObject the possibly null Boolean object
     * @param defaultValue the value to use when the Boolean argument is null
     * @return the boolean value of the Boolean object when non-null, otherwise {@code defaultValue}
     */
    public static boolean toBooleanOrDefault(@Nullable Boolean booleanObject, boolean defaultValue) {
        return isNull(booleanObject) ? defaultValue : booleanObject.booleanValue();
    }
}
