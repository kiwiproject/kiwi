package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.base.CaseFormat;
import com.google.common.base.Enums;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.EnumUtils;
import org.jspecify.annotations.Nullable;
import org.kiwiproject.collect.KiwiArrays;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Static utilities for working with {@link Enum}.
 */
@UtilityClass
public class KiwiEnums {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern MULTIPLE_UNDERSCORES = Pattern.compile("_+");
    @SuppressWarnings("java:S5998")
    private static final Pattern LOWER_CAMEL = Pattern.compile("^[a-z][a-z0-9]*([A-Z][a-z0-9]*)*$");
    @SuppressWarnings("java:S5998")
    private static final Pattern UPPER_CAMEL = Pattern.compile("^[A-Z][a-z0-9]*(?:[A-Z][a-z0-9]*)*$");
    
    /**
     * Returns a list of the constants in the specified enum type.
     *
     * @param <E>       the type of the enum
     * @param enumClass the enum class; must not be null
     * @return a list containing all the constants of the specified enum type
     * @throws IllegalArgumentException if the provided class is null or not an enum type
     */
    public static <E extends Enum<E>> List<E> listOf(Class<E> enumClass) {
        return streamOf(enumClass).toList();
    }

    /**
     * Returns a stream of constants in the specified enum type.
     *
     * @param <E>       the type of the enum
     * @param enumClass the enum class; must not be null
     * @return a stream containing all the constants of the specified enum type
     * @throws IllegalArgumentException if the provided class is null or not an enum type
     */
    public static <E extends Enum<E>> Stream<E> streamOf(Class<E> enumClass) {
        checkEnumClass(enumClass);
        return Stream.of(enumClass.getEnumConstants());
    }

    /**
     * Return an optional enum constant for the given enum type and value. This performs an exact match of the enum
     * constant names. For a case-insensitive comparison, you can use {@link #getIfPresentIgnoreCase(Class, String)}.
     *
     * @param <E> the enum type
     * @param enumClass the enum class
     * @param value the string value to compare against; may be blank
     * @return an Optional that may contain an {@link Enum} constant or be empty
     * @implNote This wraps Guava's {@link Enums#getIfPresent(Class, String)} and adapts the Guava Optional return
     * type to a Java Optional. Unlike Guava, it permits blank values, in which case an empty Optional is returned.
     */
    public static <E extends Enum<E>> Optional<E> getIfPresent(Class<E> enumClass, @Nullable String value) {
        checkEnumClass(enumClass);
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
     * @param value the string value to compare against; may be blank
     * @return an Optional that may contain an {@link Enum} constant or be empty
     * @implNote This wraps Apache Commons' {@link EnumUtils#getEnumIgnoreCase(Class, String)} but returns
     * am Optional instead of null if no enum constant is found. In addition, it ignores leading and trailing
     * whitespace.
     */
    public static <E extends Enum<E>> Optional<E> getIfPresentIgnoreCase(Class<E> enumClass, @Nullable String value) {
        checkEnumClass(enumClass);
        if (isBlank(value)) {
            return Optional.empty();
        }

        // First try the value as-is, then fallback to the value with leading and trailing whitespace stripped
        return Optional.ofNullable(EnumUtils.getEnumIgnoreCase(enumClass, value))
            .or(() -> Optional.ofNullable(EnumUtils.getEnumIgnoreCase(enumClass, value.strip())));
    }

    /**
     * Return an optional enum constant for the given enum type and value, compared against the enum constants
     * in a fuzzy manner.
     * <p>
     * This method uses the following algorithm:
     * <ol>
     * <li>
     *   Check for a case-insensitive match against the enum constant {@code name()}. Return if match.
     * </li>
     * <li>
     *    Strip leading and trailing whitespace from the input.
     * </li>
     * <li>
     *   Replace internal white space, dashes, and periods with underscores
     *   (e.g., {@code "pending payment"} becomes {@code "pending_payment"}).
     * </li>
     * <li>
     *   Collapse multiple consecutive underscores into a single underscore.
     * </li>
     * <li>
     *   Check for a case-insensitive match of the normalized text against {@code name()}. Return if match.
     * </li>
     * <li>
     *   Detect if the normalized input is in {@code lowerCamel} or {@code upperCamel} format.
     *   If so, convert it to {@code UPPER_UNDERSCORE} and check for a case-insensitive match of the
     *   converted {@code camelCase} text against {@code name()}. Return if match.
     * </li>
     * <li>
     *   Check for a case-insensitive match of the original input against {@code toString()}. Return if match.
     * </li>
     * <li>
     *   Return an empty {@code Optional} since there is no match.
     * </li>
     * </ol>
     * 
     * @param <E> the enum type
     * @param enumClass the enum class
     * @param value the string value to compare against; may be blank
     * @return an Optional that may contain an {@link Enum} constant or be empty
     * @implNote This is inspired by and adapted from Dropwizard's {@code Enums#fromStringFuzzy} method.
     * It is even more permissive than the Dropwizard method.
     */
    public static <E extends Enum<E>> Optional<E> getIfPresentFuzzy(Class<E> enumClass, @Nullable String value) {
        checkEnumClass(enumClass);
        
        if (isBlank(value)) {
            return Optional.empty();
        }

         E[] constants = enumClass.getEnumConstants();

        // Check for exact matches (case-insensitive) before modifying the input
        var exactMatch = matchOrNull(constants, Enum::name, value);
        if (nonNull(exactMatch)) {
            return Optional.of(exactMatch);
        }

        // Replace internal whitespace, dashes, and periods with underscores
        var text = normalizeInput(value);

        // Check for matches on enum name() against text
        var nameMatch = matchOrNull(constants, Enum::name, text);
        if (nonNull(nameMatch)) {
            return Optional.of(nameMatch);
        }

        // Check if the text is in lowerCamel or upperCamel format. There is no need
        // to check for lower-hyphen since dashes (hyphens) were already changed to
        // underscores. If the format is one of these, convert to UPPER_UNDERSCORE,
        // then check for matches.
        var camelCaseFormat = detectCamelCaseFormatOrNull(text);

        if (nonNull(camelCaseFormat)) {
            var convertedCamel = camelCaseFormat
                    .converterTo(CaseFormat.UPPER_UNDERSCORE)
                    .convert(text);
        
            // Check for matches on enum name() against camelText
            var convertedCamelMatch = matchOrNull(constants, Enum::name, convertedCamel);
            if (nonNull(convertedCamelMatch)) {
                return Optional.of(convertedCamelMatch);
            }
        }

        // Last, check against enum toString() using the original input value
        var toStringMatch = matchOrNull(constants, Enum::toString, value);

        return Optional.ofNullable(toStringMatch);
    }

    private static String normalizeInput(String input) {
        var modified = WHITESPACE.matcher(input.strip()).replaceAll("_")
                .replace('-', '_')
                .replace('.', '_');
        return MULTIPLE_UNDERSCORES.matcher(modified).replaceAll("_");
    }

    @Nullable
    private static <E extends Enum<E>> E matchOrNull(E[] constants, Function<E, String> accessor, String candidate) {
        return Arrays.stream(constants)
                .filter(constant -> accessor.apply(constant).equalsIgnoreCase(candidate))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    static CaseFormat detectCamelCaseFormatOrNull(String input) {
        if (LOWER_CAMEL.matcher(input).matches()) {
            return CaseFormat.LOWER_CAMEL;
        } else if (UPPER_CAMEL.matcher(input).matches()) {
            return CaseFormat.UPPER_CAMEL;
        }
        return null;
    }

    private static <E extends Enum<E>> void checkEnumClass(Class<E> enumClass) {
        checkArgumentNotNull(enumClass, "enumClass must not be null");
        checkArgument(enumClass.isEnum(), "%s is not an enum", enumClass);
    }

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value for equality.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison; may be null
     * @return true if the enum name equals the value, false otherwise
     */
    public static <E extends Enum<E>> boolean equals(Enum<E> enumValue, @Nullable CharSequence value) {
        checkEnumNotNull(enumValue);
        return enumValue.name().equals(stringOrNull(value));
    }

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value, ignoring
     * the case, for equality.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison; may be null
     * @return if the enum name equals the value in a case-insensitive manner, false otherwise
     */
    public static <E extends Enum<E>> boolean equalsIgnoreCase(Enum<E> enumValue, @Nullable CharSequence value) {
        checkEnumNotNull(enumValue);
        return enumValue.name().equalsIgnoreCase(stringOrNull(value));
    }

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value for
     * inverse equality, i.e., they are not equal.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison; may be null
     * @return true if the enum name does not equal the value, false otherwise
     */
    public static <E extends Enum<E>> boolean notEquals(Enum<E> enumValue, @Nullable CharSequence value) {
        return !equals(enumValue, value);
    }

    /**
     * Compares the given enum's {@link Enum#name() name} with the given value, ignoring the case, for
     * inverse equality, i.e., they are not equal.
     *
     * @param <E> the enum type
     * @param enumValue the enum to use for the comparison
     * @param value the value to use for the comparison; may be null
     * @return true if the enum name does not equal the value in a case-insensitive manner, false otherwise
     */
    public static <E extends Enum<E>> boolean notEqualsIgnoreCase(Enum<E> enumValue, @Nullable CharSequence value) {
        return !equalsIgnoreCase(enumValue, value);
    }

    /**
     * Checks whether the given value matches the {@link Enum#name() name} of the given enums.
     *
     * @param <E> the enum type
     * @param value the value to use for the comparison; may be null
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
     * enums, ignoring the case.
     *
     * @param <E> the enum type
     * @param value the value to use for the comparison; may be null
     * @param enumValues the enums to use for the comparison
     * @return true if the value equals the name of the enums in a case-insensitive manner, false otherwise
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

    /**
     * Return the lowercase name of the enum value, using {@link Locale#ENGLISH} as the locale
     * for the conversion.
     *
     * @param <E>       the enum type
     * @param enumValue the enum value to lowercase
     * @return the lowercase value of the enum value
     * @see #lowercaseName(Enum, Locale)
     */
    public static <E extends Enum<E>> String lowercaseName(Enum<E> enumValue) {
        return lowercaseName(enumValue, Locale.ENGLISH);
    }

    /**
     * Return the lowercase name of the enum value.
     *
     * @param <E>       the enum type
     * @param enumValue the enum value to lowercase
     * @param locale    the Locale to use for the lowercase conversion
     * @return the lowercase value of the enum value
     * @throws IllegalArgumentException if enumValue or locale is null
     */
    public static <E extends Enum<E>> String lowercaseName(Enum<E> enumValue, Locale locale) {
        checkEnumNotNull(enumValue);
        checkArgumentNotNull(locale, "locale must not be null");
        return enumValue.name().toLowerCase(locale);
    }

    private static <E extends Enum<E>> void checkEnumNotNull(Enum<E> enumValue) {
        checkArgumentNotNull(enumValue, "enumValue must not be null");
    }
}
