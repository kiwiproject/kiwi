package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiStrings.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Static utility methods similar to those found in {@link Preconditions}, but with a lovely
 * Kiwi flavor to them. That class has good documentation, so go read it if you need more information on the
 * intent and general usage.
 *
 * @implNote Many of the methods in this class use Lombok's {@link SneakyThrows} so that methods do not need to declare
 * that they throw exceptions of type T, <em>for the case that T is a checked exception</em>. Read more details about
 * how this works in {@link SneakyThrows}. Most notably, this should give you more insight into how the JVM (versus
 * Java the language) actually work: <em>"The JVM does not check for the consistency of the checked exception system;
 * javac does, and this annotation lets you opt out of its mechanism."</em>
 */
@UtilityClass
public class KiwiPreconditions {

    @VisibleForTesting
    static final int MAX_PORT_NUMBER = 65_535;

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * <p>
     * Throws an exception of type T if {@code expression} is false.
     *
     * @param expression    a boolean expression
     * @param exceptionType the type of exception to be thrown if {@code expression} is false
     * @param <T>           the type of exception
     * @implNote This uses Lombok's {@link SneakyThrows} to throw any checked exceptions without declaring them.
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
     * Throws an exception of type T if {@code expression} is false.
     *
     * @param expression    a boolean expression
     * @param exceptionType the type of exception to be thrown if {@code expression} is false
     * @param errorMessage  the exception message to use if the check fails
     * @param <T>           the type of exception
     * @implNote This uses Lombok's {@link SneakyThrows} to throw any checked exceptions without declaring them.
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
     * Throws an exception of type T if {@code expression} is false.
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
     * @implNote This uses Lombok's {@link SneakyThrows} to throw any checked exceptions without declaring them.
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
     * and {@link IllegalArgumentException} if blank or returning the String otherwise.
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
     * and {@link IllegalArgumentException} if blank or returning the String otherwise.
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
     * and {@link IllegalArgumentException} if blank or returning the String otherwise.
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
     * and {@link IllegalArgumentException} if null or returning the (non null) reference otherwise.
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
     * and {@link IllegalArgumentException} if null or returning the (non null) reference otherwise.
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

    private static IllegalArgumentException newIllegalArgumentException(String errorMessageTemplate,
                                                                        Object... errorMessageArgs) {
        var errorMessage = format(errorMessageTemplate, errorMessageArgs);
        return new IllegalArgumentException(errorMessage);
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
     * Ensures that a collection of items has an even count.
     *
     * @param items items to count
     * @param <T>   the object type
     */
    @SafeVarargs
    public static <T> void checkEvenItemCount(T... items) {
        requireNonNull(items);
        checkEvenItemCount(() -> items.length);
    }

    /**
     * Ensures that a collection of items has an even count.
     *
     * @param items items to count
     * @param <T>   the object type
     */
    public static <T> void checkEvenItemCount(Collection<T> items) {
        requireNonNull(items);
        checkEvenItemCount(items::size);
    }

    /**
     * Ensures that a collection of items has an even count.
     *
     * @param countSupplier an {@link IntSupplier} that returns the count to evaluate
     */
    public static void checkEvenItemCount(IntSupplier countSupplier) {
        requireNonNull(countSupplier);
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
     * @throws IllegalStateException if the value is not positive (e.g. greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(int value) {
        checkState(value > 0, "value must be a positive number");
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero).
     *
     * @param value the value to check for positivity
     * @throws IllegalStateException if the value is not positive (e.g. greater than zero)
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositive(long value) {
        checkState(value > 0, "value must be a positive number");
    }

    /**
     * Ensures int {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value the value to check for positivity
     * @throws IllegalStateException if the value is not positive or zero
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(int value) {
        checkState(value >= 0, "value must be positive or zero");
    }

    /**
     * Ensures long {@code value} is a positive number (greater than zero) or zero.
     *
     * @param value the value to check for positivity
     * @throws IllegalStateException if the value is not positive or zero
     * @see Preconditions#checkState(boolean, Object)
     */
    public static void checkPositiveOrZero(long value) {
        checkState(value >= 0, "value must be positive or zero");
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
     * Returns the int value if it is positive or zero, throwing an {@link IllegalStateException} if not positive or zero.
     *
     * @param value the value to check for positivity or zero
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive zero
     */
    public static int requirePositiveOrZero(int value) {
        checkPositiveOrZero(value);
        return value;
    }

    /**
     * Returns the long value if it is positive or zero, throwing an {@link IllegalStateException} if not positive or zero.
     *
     * @param value the value to check for positivity or zero
     * @return the given value if positive or zero
     * @throws IllegalStateException if the value is not positive zero
     */
    public static long requirePositiveOrZero(long value) {
        checkPositiveOrZero(value);
        return value;
    }

    /**
     * Ensures given port is valid, between 0 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port the port to check for validity
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidPort(int port) {
        checkState(port >= 0 && port <= MAX_PORT_NUMBER, "port must be between 0 and %s", MAX_PORT_NUMBER);
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
     * Ensures given port is valid (excluding zero), between 1 and {@link #MAX_PORT_NUMBER}.
     *
     * @param port the port to check for validity
     * @throws IllegalStateException if port is not valid
     */
    public static void checkValidNonZeroPort(int port) {
        checkState(port > 0 && port <= MAX_PORT_NUMBER, "port must be between 1 and %s", MAX_PORT_NUMBER);
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

}
