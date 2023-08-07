package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.base.Enums;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.EnumUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.collect.KiwiArrays;

import java.util.Arrays;
import java.util.Optional;

/**
 * Static utilities for working with {@link Enum}.
 */
@UtilityClass
public class KiwiEnums {

    /**
     * Return an optional enum constant for the given enum type and value. This performs an exact match of the enum
     * constant names. For a case-insensitive comparison, you can use {@link #getIfPresentIgnoreCase(Class, String)}.
     *
     * @param <E> the enum type
     * @param enumClass the enum class
     * @param value the string value to compare against, may be blank
     * @return an Optional that may contain an {@link Enum} constant or be empty
     * @implNote This wraps Guava's {@link Enums#getIfPresent(Class, String)} and adapts the Guava Optional return
     * type to a Java Optional. Unlike Guava, it permits blank values, in which case an empty Optional is returned.
     */
    public static <E extends Enum<E>> Optional<E> getIfPresent(Class<E> enumClass, @Nullable String value) {
        checkArgumentNotNull(enumClass);
        if (isBlank(value)) {
            return Optional.empty();
        }

        return Enums.getIfPresent(enumClass, value).toJavaUtil();
    }

    /**
     * Return an optional enum constant for the given enum type and value, compared against the enum constants
     * in a case-insensitive manner, and ignoring any leading or trailing whitespace.
     *
     * @param <E> the enum type
     * @param enumClass the enum class
     * @param value the string value to compare against, may be blank
     * @return an Optional that may contain an {@link Enum} constant or be empty
     * @implNote This wraps Apache Commons' {@link EnumUtils#getEnumIgnoreCase(Class, String)} but returns
     * am Optional instead of null if no enum constant is found. In addition, it ignores leading and trailing
     * whitespace.
     */
    public static <E extends Enum<E>> Optional<E> getIfPresentIgnoreCase(Class<E> enumClass, @Nullable String value) {
        checkArgumentNotNull(enumClass);
        if (isBlank(value)) {
            return Optional.empty();
        }

        // First try the value as-is, then fallback to the value with leading & trailing whitespace stripped
        return Optional.ofNullable(EnumUtils.getEnumIgnoreCase(enumClass, value))
            .or(() -> Optional.ofNullable(EnumUtils.getEnumIgnoreCase(enumClass, value.strip())));
    }

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
     * Checks whether the given value matches the {@link Enum#name() name} of the given enums.
     *
     * @param <E> the enum type
     * @param value the value to use for the comparison, may be null
     * @param enumValues the enums to use for the comparison
     * @return true if the value equals the name of the enums, false otherwise
     */
    @SafeVarargs
    public static <E extends Enum<E>> boolean equalsAny(@Nullable CharSequence value, Enum<E>... enumValues) {
        checkEnumsNotNullOrEmpty(enumValues);
        return Arrays.stream(enumValues).anyMatch(enumValue -> equals(enumValue, stringOrNull(value)));
    }

    /**
     * Checks whether the given value matches the {@link Enum#name() name} of the given
     * enums, ignoring case.
     *
     * @param <E> the enum type
     * @param value the value to use for the comparison, may be null
     * @param enumValues the enums to use for the comparison
     * @return true if the value equals the name of the enums in case-insensitive manner, false otherwise
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
