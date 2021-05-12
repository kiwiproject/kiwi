package org.kiwiproject.base;

import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utilities for any object which are not in {@link java.util.Objects} or {@link com.google.common.base.MoreObjects}.
 */
@UtilityClass
public class KiwiObjects {

    /**
     * Return the first non-null object, or {@code null} if all are null.
     *
     * @param first  the first object
     * @param second the second object
     * @param rest   zero or more additional objects
     * @param <T>    the type of objects
     * @return the first non-null object or null if all are null
     */
    @SafeVarargs
    public static <T> T firstNonNullOrNull(T first, T second, T... rest) {
        return firstNonNull(first, second, rest).orElse(null);
    }

    /**
     * Return the first supplied non-null object, or {@code null} if all all suppliers return null.
     *
     * @param first  the first object supplier
     * @param second the second object supplier
     * @param rest   zero or more additional object suppliers
     * @param <T>    the type of objects
     * @return the first non-null supplied object or null if all suppliers return null
     * @implNote once a non-null object is supplied, no further suppliers are called
     */
    @SafeVarargs
    public static <T> T firstSuppliedNonNullOrNull(Supplier<T> first, Supplier<T> second, Supplier<T>... rest) {
        return firstSuppliedNonNull(first, second, rest).orElse(null);
    }

    /**
     * Return an {@link Optional} containing the first non-null object, or an empty {@link Optional} if all are null.
     *
     * @param first  the first object
     * @param second the second object
     * @param rest   zero or more additional objects
     * @param <T>    the type of objects
     * @return an optional containing the first non-null object or empty optional if all are null
     */
    @SafeVarargs
    public static <T> Optional<T> firstNonNull(T first, T second, T... rest) {
        if (nonNull(first)) {
            return Optional.of(first);
        }

        if (nonNull(second)) {
            return Optional.of(second);
        }

        return Arrays.stream(rest)
                .filter(Objects::nonNull)
                .findFirst();
    }

    /**
     * Return an {@link Optional} containing the first non-null object, or an empty {@link Optional} if all suppliers
     * return null.
     *
     * @param first  the first object supplier
     * @param second the second object supplier
     * @param rest   zero or more additional object suppliers
     * @param <T>    the type of objects
     * @return an optional containing the first non-null supplied object or empty optional if all suppliers return null
     * @implNote once a non-null object is supplied, no further suppliers are called
     */
    @SafeVarargs
    public static <T> Optional<T> firstSuppliedNonNull(Supplier<T> first, Supplier<T> second, Supplier<T>... rest) {
        var firstT = first.get();
        if (nonNull(firstT)) {
            return Optional.of(firstT);
        }

        var secondT = second.get();
        if (nonNull(secondT)) {
            return Optional.of(secondT);
        }

        return Arrays.stream(rest)
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
    }
}
