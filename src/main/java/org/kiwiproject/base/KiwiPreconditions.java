package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiStrings.format;
import static org.kiwiproject.collect.KiwiCollections.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiCollections.isNullOrEmpty;
import static org.kiwiproject.collect.KiwiMaps.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiMaps.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Static utility methods similar to those found in {@link Preconditions}, but with a lovely
 * Kiwi flavor to them. That class has good documentation, so go read it if you need more information on the
 * intent and general usage.
 * <p>
 * If you're looking for preconditions related to validating arguments using Jakarta Beans Validation, they
 * are in {@link org.kiwiproject.validation.KiwiValidations KiwiValidations}.
 *
 * @implNote Several methods in this class use Lombok {@link lombok.SneakyThrows} so that they do not need to declare
 * that they throw {@code Exception}s of type T, <em>for the case that T is a checked exception</em>. Read more details about
 * how this works in {@link lombok.SneakyThrows}. Most notably, this should give you more insight into how the JVM (versus
 * Java the language) actually works: <em>"The JVM does not check for the consistency of the checked exception system;
 * javac does, and this annotation lets you opt out of its mechanism."</em>
 */
@UtilityClass
public class KiwiPreconditions {

    @VisibleForTesting
    static final int MAX_PORT_NUMBER = 65_535;

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * <p>
     * Throws an {@code Exception} of type T if {@code expression} is false.
     *
     * @param expression    a boolean expression
     * @param exceptionType the type of exception to be thrown if {@code expression} is false
     * @param <T>           the type of exception
     * @implNote This uses Lombok {@link lombok.SneakyThrows} to throw any checked exceptions without declaring them.
     */
    @SneakyThrows(Throwable.class)
    public static <T extends Throwable> void checkArgument(boolean expression, Class<T> exceptionType) {
        if (!expression) {
            throw exceptionType.getDeclaredConstructor().newInstance();
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * <p>
     * Throws an {@code Exception} of type T if {@code expression} is false.
     *
     * @param expression    a boolean expression
     * @param exceptionType the type of exception to be thrown if {@code expression} is false
     * @param errorMessage  the exception message to use if the check fails
     * @param <T>           the type of exception
     * @implNote This uses Lombok {@link lombok.SneakyThrows} to throw any checked exceptions without declaring them.
     */
    @SneakyThrows(Throwable.class)
    public static <T extends Throwable> void checkArgument(boolean expression,
                                                           Class<T> exceptionType,
                                                           String errorMessage) {
        if (!expression) {
            var constructor = exceptionType.getConstructor(String.class);
            throw constructor.newInstance(errorMessage);
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * <p>
     * Throws an {@code Exception} of type T if {@code expression} is false.
     *
     * @param expression           a boolean expression
     * @param exceptionType        the type of exception to be thrown if {@code expression} is false
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to strings using {@link String#valueOf(Object)}.
     * @param <T>                  the type of exception
     * @throws NullPointerException if the check fails and either {@code errorMessageTemplate} or
     *                              {@code errorMessageArgs} is null (don't let this happen)
     * @implNote This uses Lombok {@link lombok.SneakyThrows} to throw any checked exceptions without declaring them.
     */
    @SneakyThrows(Throwable.class)
    public static <T extends Throwable> void checkArgument(boolean expression,
                                                           Class<T> exceptionType,
                                                           String errorMessageTemplate,
                                                           Object... errorMessageArgs) {
        if (!expression) {
            var constructor = exceptionType.getConstructor(String.class);
            throw constructor.newInstance(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    /**
     * Ensures that a String passed as a parameter to the calling method is not blank, throwing
     * an {@link IllegalArgumentException} if blank or returning the String otherwise.
     *
     * @param value the String value to check
     * @return the given String
     * @see #checkArgumentNotBlank(String)
     */
    public static String requireNotBlank(String value) {
        checkArgumentNotBlank(value);
        return value;
    }

    /**
     * Ensures that a String passed as a parameter to the calling method is not blank, throwing
     * an {@link IllegalArgumentException} if blank or returning the String otherwise.
     *
     * @param value        the String value to check
     * @param errorMessage the error message for the exception
     * @return the given String
     * @see #checkArgumentNotBlank(String, String)
     */
    public static String requireNotBlank(String value, String errorMessage) {
        checkArgumentNotBlank(value, errorMessage);
        return value;
    }

    /**
     * Ensures that a String passed as a parameter to the calling method is not blank, throwing
     * an {@link IllegalArgumentException} if blank or returning the String otherwise.
     *
     * @param value                the String value to check
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to strings using {@link String#valueOf(Object)}.
     * @return the given String
     * @see #checkArgumentNotBlank(String, String, Object...)
     */
    public static String requireNotBlank(String value, String errorMessageTemplate, Object... errorMessageArgs) {
        checkArgumentNotBlank(value, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null, throwing
     * an {@link IllegalArgumentException} if null or returning the (non-null) reference otherwise.
     *
     * @param reference an object reference
     * @param <T>       the type of object
     * @return the object type
     */
    public static <T> T requireNotNull(T reference) {
        checkArgumentNotNull(reference);
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null, throwing
     * an {@link IllegalArgumentException} if null or returning the (non-null) reference otherwise.
     *
     * @param reference            an object reference
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to strings using {@link String#valueOf(Object)}.
     * @param <T>                  the type of object
     * @return the object type
     */
    public static <T> T requireNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        checkArgumentNotNull(reference, errorMessageTemplate, errorMessageArgs);
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null, throwing
     * an {@link IllegalArgumentException} if null.
     *
     * @param reference an object reference
     * @param <T>       the object type
     */
    public static <T> void checkArgumentNotNull(T reference) {
        Preconditions.checkArgument(nonNull(reference));
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null, throwing
     * an {@link IllegalArgumentException} if null.
     *
     * @param reference    an object reference
     * @param errorMessage the error message for the exception
     * @param <T>          the object type
     */
    public static <T> void checkArgumentNotNull(T reference, String errorMessage) {
        Preconditions.checkArgument(nonNull(reference), errorMessage);
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null, throwing
     * an {@link IllegalArgumentException} if null.
     *
     * @param reference            an object reference
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to strings using {@link String#valueOf(Object)}.
     * @param <T>                  the object type
     */
    public static <T> void checkArgumentNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (isNull(reference)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures that only one of two given arguments is null.
     * Throws {@link IllegalArgumentException} if both are null or both are non-null.
     *
     * @param first  the first argument
     * @param second the second argument
     * @param <T>    the object type
     */
    public static <T> void checkOnlyOneArgumentIsNull(T first, T second) {
        Preconditions.checkArgument(isOnlyOneNull(first, second));
    }

    /**
     * Ensures that only one of two given arguments is null.
     * Throws {@link IllegalArgumentException} if both are null or both are non-null.
     *
     * @param first   the first argument
     * @param second  the second argument
     * @param message the error message to use if the check fails
     * @param <T>     the object type
     */
    public static <T> void checkOnlyOneArgumentIsNull(T first, T second, String message) {
        Preconditions.checkArgument(isOnlyOneNull(first, second), message);
    }

    /**
     * Ensures that only one of two given arguments is null.
     * Throws {@link IllegalArgumentException} if both are null or both are non-null.
     *
     * @param first                the first argument
     * @param second               the second argument
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to strings using {@link String#valueOf(Object)}.
     * @param <T>                  the object type
     */
    public static <T> void checkOnlyOneArgumentIsNull(T first,
                                                      T second,
                                                      String errorMessageTemplate,
                                                      Object... errorMessageArgs) {
        Preconditions.checkArgument(isOnlyOneNull(first, second), errorMessageTemplate, errorMessageArgs);
    }

    private static <T> boolean isOnlyOneNull(T left, T right) {
        return (nonNull(left) && isNull(right)) || (isNull(left) && nonNull(right));
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is null, throwing
     * an {@link IllegalArgumentException} if not null.
     *
     * @param reference an object reference
     * @param <T>       the object type
     */
    public static <T> void checkArgumentIsNull(T reference) {
        Preconditions.checkArgument(isNull(reference));
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is null, throwing
     * an {@link IllegalArgumentException} if not null.
     *
     * @param reference    an object reference
     * @param errorMessage the error message for the exception
     * @param <T>          the object type
     */
    public static <T> void checkArgumentIsNull(T reference, String errorMessage) {
        Preconditions.checkArgument(isNull(reference), errorMessage);
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is null, throwing
     * an {@link IllegalArgumentException} if not null.
     *
     * @param reference            an object reference
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @param <T>                  the object type
     */
    public static <T> void checkArgumentIsNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (nonNull(reference)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures that the string passed as a parameter to the calling method is not null, empty or blank, throwing
     * an {@link IllegalArgumentException} if it is null, empty, or blank.
     *
     * @param string a string
     */
    public static void checkArgumentNotBlank(String string) {
        Preconditions.checkArgument(isNotBlank(string));
    }

    /**
     * Ensures that the string passed as a parameter to the calling method is not null, empty or blank, throwing
     * an {@link IllegalArgumentException} if it is null, empty, or blank.
     *
     * @param string       a string
     * @param errorMessage the error message for the exception
     */
    public static void checkArgumentNotBlank(String string, String errorMessage) {
        Preconditions.checkArgument(isNotBlank(string), errorMessage);
    }

    /**
     * Ensures that the string passed as a parameter to the calling method is not null, empty or blank, throwing
     * an {@link IllegalArgumentException} if it is null, empty, or blank.
     *
     * @param string               a string
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to strings using {@link String#valueOf(Object)}.
     */
    public static void checkArgumentNotBlank(String string, String errorMessageTemplate, Object... errorMessageArgs) {
        if (isBlank(string)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures that the string passed as a parameter to the calling method is null, empty or blank, throwing
     * an {@link IllegalArgumentException} if it is not null, empty, or blank.
     *
     * @param string a string
     */
    public static void checkArgumentIsBlank(String string) {
        Preconditions.checkArgument(isBlank(string));
    }

    /**
     * Ensures that the string passed as a parameter to the calling method is null, empty or blank, throwing
     * an {@link IllegalArgumentException} if it is not null, empty, or blank.
     *
     * @param string       a string
     * @param errorMessage the error message for the exception
     */
    public static void checkArgumentIsBlank(String string, String errorMessage) {
        Preconditions.checkArgument(isBlank(string), errorMessage);
    }

    /**
     * Ensures that the string passed as a parameter to the calling method is null, empty or blank, throwing
     * an {@link IllegalArgumentException} if it is not null, empty, or blank.
     *
     * @param string               a string
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     */
    public static void checkArgumentIsBlank(String string, String errorMessageTemplate, Object... errorMessageArgs) {
        if (isNotBlank(string)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty.
     * Throws an {@link IllegalArgumentException} if the collection is null or empty.
     *
     * @param collection a collection, possibly null
     * @param <T>        the type of object in the collection
     */
    public static <T> void checkArgumentNotEmpty(Collection<T> collection) {
        Preconditions.checkArgument(isNotNullOrEmpty(collection));
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty.
     * Throws an {@link IllegalArgumentException} if the collection is null or empty.
     *
     * @param collection   a collection, possibly null
     * @param errorMessage the error message for the exception
     * @param <T>          the type of object in the collection
     */
    public static <T> void checkArgumentNotEmpty(Collection<T> collection, String errorMessage) {
        Preconditions.checkArgument(isNotNullOrEmpty(collection), errorMessage);
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty.
     * Throws an {@link IllegalArgumentException} if the collection is null or empty.
     *
     * @param collection           a collection, possibly null
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @param <T>                  the type of object in the collection
     */
    public static <T> void checkArgumentNotEmpty(Collection<T> collection,
                                                 String errorMessageTemplate,
                                                 Object... errorMessageArgs) {
        if (isNullOrEmpty(collection)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty,
     * and that none of its elements are null.
     * <p>
     * Throws an {@link IllegalArgumentException} if the collection is null or empty,
     * or if any elements are null.
     *
     * @param collection a collection, possibly null
     * @param <T>        the type of object in the collection
     */
    public static <T> void checkArgumentContainsOnlyNotNull(Collection<T> collection) {
        checkCollectionNotNullOrEmpty(collection);
        var anyNull = anyNullElementsIn(collection);
        Preconditions.checkArgument(!anyNull, "collection must not contain null elements");
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty,
     * and that none of its elements are null.
     * <p>
     * Throws an {@link IllegalArgumentException} if the collection is null or empty,
     * or if any elements are null.
     *
     * @param collection   a collection, possibly null
     * @param errorMessage the error message for the exception
     * @param <T>          the type of object in the collection
     */
    public static <T> void checkArgumentContainsOnlyNotNull(Collection<T> collection,
                                                            String errorMessage) {
        checkCollectionNotNullOrEmpty(collection);
        var anyNull = anyNullElementsIn(collection);
        Preconditions.checkArgument(!anyNull, errorMessage);
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty,
     * and that none of its elements are null.
     * <p>
     * Throws an {@link IllegalArgumentException} if the collection is null or empty,
     * or if any elements are null.
     *
     * @param collection           a collection, possibly null
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @param <T>                  the type of object in the collection
     */
    public static <T> void checkArgumentContainsOnlyNotNull(Collection<T> collection,
                                                            String errorMessageTemplate,
                                                            Object... errorMessageArgs) {
        checkCollectionNotNullOrEmpty(collection);
        if (anyNullElementsIn(collection)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    // This uses anyMatch to return as soon as a null element is found.
    private static <T> boolean anyNullElementsIn(Collection<T> collection) {
        return collection.stream().anyMatch(Objects::isNull);
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty,
     * and that none of its elements are blank strings.
     * <p>
     * Throws an {@link IllegalArgumentException} if the collection is null or empty,
     * or if any elements are blank strings.
     *
     * @param collection a collection, possibly null
     * @implNote uses {@link StringUtils#isBlank(CharSequence)} to check for blank elements
     */
    public static void checkArgumentContainsOnlyNotBlank(Collection<String> collection) {
        checkCollectionNotNullOrEmpty(collection);
        var anyBlank = anyBlankElementsIn(collection);
        Preconditions.checkArgument(!anyBlank, "collection must not contain blank elements");
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty,
     * and that none of its elements are blank strings.
     * <p>
     * Throws an {@link IllegalArgumentException} if the collection is null or empty,
     * or if any elements are blank strings.
     *
     * @param collection   a collection, possibly null
     * @param errorMessage the error message for the exception
     * @implNote uses {@link StringUtils#isBlank(CharSequence)} to check for blank elements
     */
    public static void checkArgumentContainsOnlyNotBlank(Collection<String> collection,
                                                         String errorMessage) {
        checkCollectionNotNullOrEmpty(collection);
        var anyBlank = anyBlankElementsIn(collection);
        Preconditions.checkArgument(!anyBlank, errorMessage);
    }

    /**
     * Ensures that the collection passed as a parameter to the calling method is not null or empty,
     * and that none of its elements are blank strings.
     * <p>
     * Throws an {@link IllegalArgumentException} if the collection is null or empty,
     * or if any elements are blank strings.
     *
     * @param collection           a collection, possibly null
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @implNote uses {@link StringUtils#isBlank(CharSequence)} to check for blank elements
     */
    public static void checkArgumentContainsOnlyNotBlank(Collection<String> collection,
                                                         String errorMessageTemplate,
                                                         Object... errorMessageArgs) {
        checkCollectionNotNullOrEmpty(collection);
        if (anyBlankElementsIn(collection)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    private static <T> void checkCollectionNotNullOrEmpty(Collection<T> collection) {
        checkArgumentNotEmpty(collection, "collection must not be null or empty");
    }

    // This uses anyMatch to return as soon as a blank element is found.
    private static boolean anyBlankElementsIn(Collection<String> collection) {
        return collection.stream().anyMatch(StringUtils::isBlank);
    }

    /**
     * Ensures that the map passed as a parameter to the calling method is not null or empty.
     * Throws an {@link IllegalArgumentException} if the map is null or empty.
     *
     * @param map a map, possibly null
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     */
    public static <K, V> void checkArgumentNotEmpty(Map<K, V> map) {
        Preconditions.checkArgument(isNotNullOrEmpty(map));
    }

    /**
     * Ensures that the map passed as a parameter to the calling method is not null or empty.
     * Throws an {@link IllegalArgumentException} if the map is null or empty.
     *
     * @param map          a map, possibly null
     * @param errorMessage the error message for the exception
     * @param <K>          the type of keys in the map
     * @param <V>          the type of values in the map
     */
    public static <K, V> void checkArgumentNotEmpty(Map<K, V> map, String errorMessage) {
        Preconditions.checkArgument(isNotNullOrEmpty(map), errorMessage);
    }

    /**
     * Ensures that the map passed as a parameter to the calling method is not null or empty.
     * Throws an {@link IllegalArgumentException} if the map is null or empty.
     *
     * @param map                  a map, possibly null
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @param <K>                  the type of keys in the map
     * @param <V>                  the type of values in the map
     */
    public static <K, V> void checkArgumentNotEmpty(Map<K, V> map,
                                                    String errorMessageTemplate,
                                                    Object... errorMessageArgs) {
        if (isNullOrEmpty(map)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures that a collection of items has an even count, throwing an {@link IllegalArgumentException} if
     * {@code items} is null or there is an odd number of items.
     *
     * @param items items to count
     * @param <T>   the object type
     */
    @SafeVarargs
    public static <T> void checkEvenItemCount(T... items) {
        requireNotNull(items);
        checkEvenItemCount(() -> items.length);
    }

    /**
     * Ensures that a collection of items has an even count, throwing an {@link IllegalArgumentException} if
     * {@code items} is null or there is an odd number of items.
     *
     * @param items items to count
     * @param <T>   the object type
     */
    public static <T> void checkEvenItemCount(Collection<T> items) {
        requireNotNull(items);
        checkEvenItemCount(items::size);
    }

    /**
     * Ensures that a collection of items has an even count, throwing an {@link IllegalArgumentException} if
     * countSupplier is null or returns an odd number.
     *
     * @param countSupplier an {@link IntSupplier} that returns the count to evaluate
     */
    public static void checkEvenItemCount(IntSupplier countSupplier) {
        requireNotNull(countSupplier);
        var count = countSupplier.getAsInt();
        Preconditions.checkArgument(count % 2 == 0, "must be an even number of items (received %s)", count);
    }

    /**
     * Returns the first argument if it is not {@code null}, otherwise the second argument (which must not be {@code null}).
     * <p>
     * The main reason for this method instead of the {@link java.util.Objects#requireNonNullElse(Object, Object)} is that
     * this method throws an {@link IllegalArgumentException} instead of a {@link NullPointerException}, which was an
     * unfortunate choice by the JDK.
     *
     * @param obj        an object, possibly {@code null}
     * @param defaultObj a non-{@code null} object
     * @param <T>        the type of the reference
     * @return the first non-{@code null} argument
     */
    public static <T> T requireNotNullElse(T obj, T defaultObj) {
        checkArgumentNotNull(defaultObj);

        return isNull(obj) ? defaultObj : obj;
    }

    /**
     * Returns the first argument if it is not {@code null}, otherwise the value supplied by the {@link Supplier}, which
     * must not be {@code null}.
     * <p>
     * The main reason for this method instead of the {@link java.util.Objects#requireNonNullElse(Object, Object)} is that
     * this method throws an {@link IllegalArgumentException} instead of a {@link NullPointerException}, which was an
     * unfortunate choice by the JDK.
     *
     * @param obj      an object, possibly {@code null}
     * @param supplier creates a non-{@code null} object
     * @param <T>      the type of the reference
     * @return the first argument, or the value from {@code supplier}
     */
    public static <T> T requireNotNullElseGet(T obj, Supplier<? extends T> supplier) {
        checkArgumentNotNull(supplier);
        T value = requireNotNull(supplier.get(), "supplier must return a non-null object");

        return isNull(obj) ? value : obj;
    }

    /**
     * Ensures int {@code value} is a positive number (greater than zero).
     *
     * @param value the value to check for positivity
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(int value) {
        checkPositive(value, "value must be a positive number");
    }

    /**
     * Ensures int {@code value} is a positive number (greater than zero).
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(int value, String errorMessage) {
        checkState(value > 0, errorMessage);
    }

    /**
     * Ensures int {@code value} is a positive number (greater than zero).
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(int value, String errorMessageTemplate, Object... errorMessageArgs) {
        if (value <= 0) {
            throw newIllegalStateException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero).
     *
     * @param value the value to check for positivity
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(long value) {
        checkPositive(value, "value must be a positive number");
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero).
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(long value, String errorMessage) {
        checkState(value > 0, errorMessage);
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero).
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(long value, String errorMessageTemplate, Object... errorMessageArgs) {
        if (value <= 0) {
            throw newIllegalStateException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures int {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value the value to check for positivity
     * @throws IllegalStateException if the value is not positive or zero
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(int value) {
        checkPositiveOrZero(value, "value must be positive or zero");
    }

    /**
     * Ensures int {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(int value, String errorMessage) {
        checkState(value >= 0, errorMessage);
    }

    /**
     * Ensures int {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not zero or positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(int value, String errorMessageTemplate, Object... errorMessageArgs) {
        if (value < 0) {
            throw newIllegalStateException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value the value to check for positivity
     * @throws IllegalStateException if the value is not positive or zero
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(long value) {
        checkPositiveOrZero(value, "value must be positive or zero");
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(long value, String errorMessage) {
        checkState(value >= 0, errorMessage);
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not zero or positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(long value, String errorMessageTemplate, Object... errorMessageArgs) {
        if (value < 0) {
            throw newIllegalStateException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Returns the int value if it is positive, throwing an {@link IllegalStateException} if not positive.
     *
     * @param value the value to check for positivity
     * @return the given value if positive
     * @throws IllegalStateException if the value is not positive
     */
    public static int requirePositive(int value) {
        checkPositive(value);
        return value;
    }

    /**
     * Returns the int {@code value} if it is a positive number (greater than zero), throwing an {@link IllegalStateException} if not positive.
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @return the given value if positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static int requirePositive(int value, String errorMessage) {
        checkPositive(value, errorMessage);
        return value;
    }

    /**
     * Returns the int {@code value} if it is a positive number (greater than zero), throwing an {@link IllegalStateException} if not positive.
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @return the given value if positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static int requirePositive(int value, String errorMessageTemplate, Object... errorMessageArgs) {
        checkPositive(value, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Returns the long value if it is positive, throwing an {@link IllegalStateException} if not positive.
     *
     * @param value the value to check for positivity
     * @return the given value if positive
     * @throws IllegalStateException if the value is not positive
     */
    public static long requirePositive(long value) {
        checkPositive(value);
        return value;
    }

    /**
     * Returns the long {@code value} if it is a positive number (greater than zero), throwing an {@link IllegalStateException} if not positive.
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @return the given value if positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static long requirePositive(long value, String errorMessage) {
        checkPositive(value, errorMessage);
        return value;
    }

    /**
     * Returns the long {@code value} if it is a positive number (greater than zero), throwing an {@link IllegalStateException} if not positive.
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @return the given value if positive
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static long requirePositive(long value, String errorMessageTemplate, Object... errorMessageArgs) {
        checkPositive(value, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Returns the int value if it is positive or zero, throwing an {@link IllegalStateException} if not positive or zero.
     *
     * @param value the value to check for positivity or zero
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive or zero
     */
    public static int requirePositiveOrZero(int value) {
        checkPositiveOrZero(value);
        return value;
    }

    /**
     * Returns the int {@code value} if it is a positive number (greater than zero) or zero, throwing an {@link IllegalStateException} if not positive.
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static int requirePositiveOrZero(int value, String errorMessage) {
        checkPositiveOrZero(value, errorMessage);
        return value;
    }

    /**
     * Returns the int {@code value} if it is a positive number (greater than zero) or zero, throwing an {@link IllegalStateException} if not positive.
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not zero or positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static int requirePositiveOrZero(int value, String errorMessageTemplate, Object... errorMessageArgs) {
        checkPositiveOrZero(value, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Returns the long value if it is positive or zero, throwing an {@link IllegalStateException} if not positive or zero.
     *
     * @param value the value to check for positivity or zero
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive or zero
     */
    public static long requirePositiveOrZero(long value) {
        checkPositiveOrZero(value);
        return value;
    }

    /**
     * Returns the long {@code value} if it is a positive number (greater than zero) or zero, throwing an {@link IllegalStateException} if not positive.
     *
     * @param value        the value to check for positivity
     * @param errorMessage the error message to put in the exception if not positive
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static long requirePositiveOrZero(long value, String errorMessage) {
        checkPositiveOrZero(value, errorMessage);
        return value;
    }

    /**
     * Returns the long {@code value} if it is a positive number (greater than zero) or zero, throwing an {@link IllegalStateException} if not positive.
     *
     * @param value                the value to check for positivity
     * @param errorMessageTemplate a template for the exception message if value is not zero or positive, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive (e.g., greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static long requirePositiveOrZero(long value, String errorMessageTemplate, Object... errorMessageArgs) {
        checkPositiveOrZero(value, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Ensures given port is valid, between 0 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port the port to check for validity
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidPort(int port) {
        checkValidPort(port, "port must be between 0 and %s", MAX_PORT_NUMBER);
    }

    /**
     * Ensures given port is valid, between 0 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port         the port to check for validity
     * @param errorMessage the error message to put in the exception if the port is not valid
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidPort(int port, String errorMessage) {
        checkState(isValidPort(port), errorMessage);
    }

    /**
     * Ensures given port is valid, between 0 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port                 the port to check for validity
     * @param errorMessageTemplate a template for the exception message if port is not valid, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidPort(int port, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!isValidPort(port)) {
            throw newIllegalStateException(errorMessageTemplate, errorMessageArgs);
        }
    }

    private static boolean isValidPort(int port) {
        return port >= 0 && lessThanOrEqualToMaxPort(port);
    }

    /**
     * Returns the given port if it is valid
     *
     * @param port the port to check for validity
     * @return the given port if valid
     * @throws IllegalStateException if port is not valid
     */
    public static int requireValidPort(int port) {
        checkValidPort(port);
        return port;
    }

    /**
     * Returns the given port if it is valid
     *
     * @param port         the port to check for validity
     * @param errorMessage the error message to put in the exception if the port is not valid
     * @return the given port if valid
     * @throws IllegalStateException if port is not valid
     */
    public static int requireValidPort(int port, String errorMessage) {
        checkValidPort(port, errorMessage);
        return port;
    }

    /**
     * Returns the given port if it is valid
     *
     * @param port                 the port to check for validity
     * @param errorMessageTemplate a template for the exception message if port is not valid, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @return the given port if valid
     * @throws IllegalStateException if port is not valid
     */
    public static int requireValidPort(int port, String errorMessageTemplate, Object... errorMessageArgs) {
        checkValidPort(port, errorMessageTemplate, errorMessageArgs);
        return port;
    }

    /**
     * Ensures given port is valid (excluding zero), between 1 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port the port to check for validity
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidNonZeroPort(int port) {
        checkValidNonZeroPort(port, "port must be between 1 and %s", MAX_PORT_NUMBER);
    }

    /**
     * Ensures given port is valid (excluding zero), between 1 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port         the port to check for validity
     * @param errorMessage the error message to put in the exception if the port is not valid
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidNonZeroPort(int port, String errorMessage) {
        checkState(isValidPortAboveZero(port), errorMessage);
    }

    /**
     * Ensures given port is valid (excluding zero), between 1 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port                 the port to check for validity
     * @param errorMessageTemplate a template for the exception message if port is not valid, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidNonZeroPort(int port, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!isValidPortAboveZero(port)) {
            throw newIllegalStateException(errorMessageTemplate, errorMessageArgs);
        }
    }

    private static boolean isValidPortAboveZero(int port) {
        return port > 0 && lessThanOrEqualToMaxPort(port);
    }

    private static boolean lessThanOrEqualToMaxPort(int port) {
        return port <= MAX_PORT_NUMBER;
    }

    /**
     * Returns the given port if it is valid (excluding zero)
     *
     * @param port the port to check for validity
     * @return the given port if valid
     * @throws IllegalStateException if port is not valid
     */
    public static int requireValidNonZeroPort(int port) {
        checkValidNonZeroPort(port);
        return port;
    }

    /**
     * Returns the given port if it is valid (excluding zero)
     *
     * @param port         the port to check for validity
     * @param errorMessage the error message to put in the exception if the port is not valid
     * @return the given port if valid
     * @throws IllegalStateException if port is not valid
     */
    public static int requireValidNonZeroPort(int port, String errorMessage) {
        checkValidNonZeroPort(port, errorMessage);
        return port;
    }

    /**
     * Returns the given port if it is valid (excluding zero)
     *
     * @param port                 the port to check for validity
     * @param errorMessageTemplate a template for the exception message if port is not valid, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @return the given port if valid
     * @throws IllegalStateException if port is not valid
     */
    public static int requireValidNonZeroPort(int port, String errorMessageTemplate, Object... errorMessageArgs) {
        checkValidNonZeroPort(port, errorMessageTemplate, errorMessageArgs);
        return port;
    }

    /**
     * Ensures the argument has the expected type.
     *
     * @param argument the argument to check
     * @param requiredType the type that the argument is required to be
     * @param <T> the class of the required type
     */
    public static <T> void checkArgumentInstanceOf(T argument, Class<?> requiredType) {
        Preconditions.checkArgument(isInstanceOf(argument, requiredType));
    }

    /**
     * Ensures the argument has the expected type.
     *
     * @param argument the argument to check
     * @param requiredType the type that the argument is required to be
     * @param errorMessage the error message to put in the exception if the argument is not the required type
     * @param <T> the class of the required type
     */
    public static <T> void checkArgumentInstanceOf(T argument, Class<?> requiredType, String errorMessage) {
        Preconditions.checkArgument(isInstanceOf(argument, requiredType), errorMessage);
    }

    /**
     * Ensures the argument has the expected type.
     *
     * @param argument             the argument to check
     * @param requiredType         the type that the argument is required to be
     * @param errorMessageTemplate the error message template to use in the exception if the argument is not the
     *                             required type, according to how {@link KiwiStrings#format(String, Object...)}
     *                             handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @param <T>                  the class of the required type
     */
    public static <T> void checkArgumentInstanceOf(T argument,
                                                   Class<?> requiredType,
                                                   String errorMessageTemplate,
                                                   Object... errorMessageArgs) {

        if (isNotInstanceOf(argument, requiredType)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    /**
     * Ensures the argument type is not the restricted type.
     *
     * @param argument       the argument to check
     * @param restrictedType the type that the argument must not be
     * @param <T>            the class of the restricted type
     */
    public static <T> void checkArgumentNotInstanceOf(T argument, Class<?> restrictedType) {
        Preconditions.checkArgument(isNotInstanceOf(argument, restrictedType));
    }

    /**
     * Ensures the argument type is not the restricted type.
     *
     * @param argument       the argument to check
     * @param restrictedType the type that the argument must not be
     * @param errorMessage   the error message to put in the exception if the argument is of the restricted type
     * @param <T>            the class of the restricted type
     */
    public static <T> void checkArgumentNotInstanceOf(T argument, Class<?> restrictedType, String errorMessage) {
        Preconditions.checkArgument(isNotInstanceOf(argument, restrictedType), errorMessage);
    }

    /**
     * Ensures the argument type is not the restricted type.
     *
     * @param argument             the argument to check
     * @param restrictedType       the type that the argument must not be
     * @param errorMessageTemplate the error message to use in the exception if the argument is of the restricted type,
     *                             according to how {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to populate into the error message template
     * @param <T>                  the class of the restricted type
     */
    public static <T> void checkArgumentNotInstanceOf(T argument,
                                                      Class<?> restrictedType,
                                                      String errorMessageTemplate,
                                                      Object... errorMessageArgs) {

        if (isInstanceOf(argument, restrictedType)) {
            throw newIllegalArgumentException(errorMessageTemplate, errorMessageArgs);
        }
    }

    private static <T> boolean isInstanceOf(T argument, Class<?> requiredType) {
        return nonNull(argument) && requiredType.isAssignableFrom(argument.getClass());
    }

    private static <T> boolean isNotInstanceOf(T argument, Class<?> restrictedType) {
        return isNull(argument) || !restrictedType.isAssignableFrom(argument.getClass());
    }

    private static IllegalArgumentException newIllegalArgumentException(String errorMessageTemplate,
                                                                        Object... errorMessageArgs) {
        var errorMessage = format(errorMessageTemplate, errorMessageArgs);
        return new IllegalArgumentException(errorMessage);
    }

    private static IllegalStateException newIllegalStateException(String errorMessageTemplate,
                                                                  Object... errorMessageArgs) {
        var errorMessage = format(errorMessageTemplate, errorMessageArgs);
        return new IllegalStateException(errorMessage);
    }
}
