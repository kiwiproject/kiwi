package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.collect.KiwiArrays;

import java.util.Arrays;

/**
 * Static utilities for working with {@link Enum}.
 */
@UtilityClass
public class KiwiEnums {

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value for equality.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison, may be null
     * @return true if the enum name equals the value, false otherwise
     */
    public static <E extends Enum<E>> boolean equals(Enum<E> enumValue, @Nullable CharSequence value) {
        checkEnumNotNull(enumValue);
        return enumValue.name().equals(stringOrNull(value));
    }

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value, ignoring
     * case, for equality.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison, may be null
     * @return if the enum name equals the value in case-insensitive manner, false otherwise
     */
    public static <E extends Enum<E>> boolean equalsIgnoreCase(Enum<E> enumValue, @Nullable CharSequence value) {
        checkEnumNotNull(enumValue);
        return enumValue.name().equalsIgnoreCase(stringOrNull(value));
    }

    private static <E extends Enum<E>> void checkEnumNotNull(Enum<E> enumValue) {
        checkArgumentNotNull(enumValue, "enumValue must not be null");
    }

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value for
     * inverse equality, i.e. they are not equal.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison, may be null
     * @return true if the enum name does not equal the value, false otherwise
     */
    public static <E extends Enum<E>> boolean notEquals(Enum<E> enumValue, @Nullable CharSequence value) {
        return !equals(enumValue, value);
    }

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value, ignoring case, for
     * inverse equality, i.e. they are not equal.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison, may be null
     * @return true if the enum name does not equal the value in case-insensitive manner, false otherwise
     */
    public static <E extends Enum<E>> boolean notEqualsIgnoreCase(Enum<E> enumValue, @Nullable CharSequence value) {
        return !equalsIgnoreCase(enumValue, value);
    }

    /**
     * Checks whether the given value matches the {@link Enum#name() name} of any of the given enums.
     *
     * @param <E> the enum type
     * @param value the value to use for the comparison, may be null
     * @param enumValues the enums to use for the comparison
     * @return true if the value equals the name of any of the enums, false otherwise
     */
    @SafeVarargs
    public static <E extends Enum<E>> boolean equalsAny(@Nullable CharSequence value, Enum<E>... enumValues) {
        checkEnumsNotNullOrEmpty(enumValues);
        return Arrays.stream(enumValues).anyMatch(enumValue -> equals(enumValue, stringOrNull(value)));
    }

    /**
     * Checks whether the given value matches the {@link Enum#name() name} of any of the given
     * enums, ignoring case.
     *
     * @param <E> the enum type
     * @param value the value to use for the comparison, may be null
     * @param enumValues the enums to use for the comparison
     * @return true if the value equals the name of any of the enums in case-insensitive manner, false otherwise
     */
    @SafeVarargs
    public static <E extends Enum<E>> boolean equalsAnyIgnoreCase(@Nullable CharSequence value,
                                                                  Enum<E>... enumValues) {
        checkEnumsNotNullOrEmpty(enumValues);
        return Arrays.stream(enumValues).anyMatch(enumValue -> equalsIgnoreCase(enumValue, stringOrNull(value)));
    }

    private static String stringOrNull(@Nullable CharSequence value) {
        return isNull(value) ? null : value.toString();
    }

    @SafeVarargs
    private static <E extends Enum<E>> void checkEnumsNotNullOrEmpty(Enum<E>... enumValues) {
        checkArgument(KiwiArrays.isNotNullOrEmpty(enumValues), "enumValues must not be null or empty");
    }
}
