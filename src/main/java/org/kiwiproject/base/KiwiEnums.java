package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.collect.KiwiArrays;

import java.util.Arrays;

import lombok.experimental.UtilityClass;

/**
 * Static utilities for working with {@link Enum}.
 */
@UtilityClass
public class KiwiEnums {

    public static <E extends Enum<E>> boolean equals(Enum<E> enumValue, @Nullable CharSequence value) {
        checkEnumNotNull(enumValue);
        return enumValue.name().equals(stringOrNull(value));
    }

    public static <E extends Enum<E>> boolean equalsIgnoreCase(Enum<E> enumValue, @Nullable CharSequence value) {
        checkEnumNotNull(enumValue);
        return enumValue.name().equalsIgnoreCase(stringOrNull(value));
    }

    private static <E extends Enum<E>> void checkEnumNotNull(Enum<E> enumValue) {
        checkArgumentNotNull(enumValue, "enumValue must not be null");
    }

    public static <E extends Enum<E>> boolean notEquals(Enum<E> enumValue, @Nullable CharSequence value) {
        return !equals(enumValue, value);
    }

    public static <E extends Enum<E>> boolean notEqualsIgnoreCase(Enum<E> enumValue, @Nullable CharSequence value) {
        return !equalsIgnoreCase(enumValue, value);
    }

    @SafeVarargs
    public static <E extends Enum<E>> boolean equalsAny(@Nullable CharSequence value, Enum<E>... enumValues) {
        checkEnumsNotNullOrEmpty(enumValues);
        return Arrays.stream(enumValues).anyMatch(enumValue -> equals(enumValue, stringOrNull(value)));
    }

    @SafeVarargs
    public static <E extends Enum<E>> boolean equalsAnyIgnoreCase(@Nullable CharSequence value, Enum<E>... enumValues) {
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
