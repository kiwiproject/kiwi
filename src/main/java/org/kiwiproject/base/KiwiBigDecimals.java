package org.kiwiproject.base;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Utilities for working with {@link BigDecimal}.
 * <p>
 * Neither Guava (e.g. {@code DoubleMath}) nor Apache Commons (e.g. {@code NumberUtils}) contained these exact
 * conversions when this was originally implemented. The Apache Commons {@code NumberUtils} does contain conversion
 * methods that accept a default value for null input, or if zero is OK as the default.
 */
@UtilityClass
public class KiwiBigDecimals {

    /**
     * Converts a nullable {@link BigDecimal} to an {@link OptionalDouble}. Use this if you need a primitive double
     * and want to avoid boxing.
     *
     * @param value the BigDecimal or null
     * @return an {@link OptionalDouble}
     */
    public static OptionalDouble toOptionalDouble(@Nullable BigDecimal value) {
        return Optional.ofNullable(value)
                .map(bigDecimal -> OptionalDouble.of(bigDecimal.doubleValue()))
                .orElseGet(OptionalDouble::empty);
    }

    /**
     * Converts a nullable {@link BigDecimal} to an {@link Optional} containing a {@link Double}. Use this if you
     * need a Double or if you need to perform further map operations on the value. {@link Optional} has map, filter, etc.
     * while {@link OptionalDouble} does not.
     *
     * @param value the BigDecimal or null
     * @return an {@link Optional} of {@link Double}
     */
    public static Optional<Double> toOptionalDoubleObject(@Nullable BigDecimal value) {
        return Optional.ofNullable(value)
                .map(BigDecimal::doubleValue);
    }

    /**
     * Converts a nullable {@link BigDecimal} to a {@link Double} or returns null if the given value is null.
     *
     * @param value the BigDecimal or null
     * @return converted {@link Double} or null
     */
    public static Double toDoubleOrNull(@Nullable BigDecimal value) {
        return toOptionalDoubleObject(value).orElse(null);
    }

    /**
     * Converts the given {@link BigDecimal} to a primitive double.
     *
     * @param value the non-null BigDecimal
     * @return a primitive double
     * @throws IllegalArgumentException if the given value is null
     */
    public static double requireDouble(@Nonnull BigDecimal value) {
        checkArgumentNotNull(value, "value cannot be null");
        return value.doubleValue();
    }
}
