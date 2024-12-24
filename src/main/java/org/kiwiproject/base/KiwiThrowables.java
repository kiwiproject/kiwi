package org.kiwiproject.base;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.errorprone.annotations.Immutable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Utility class for working with {@link Throwable} instances.
 */
@UtilityClass
public final class KiwiThrowables {

    /**
     * Immutable "struct" that contains information from a {@link Throwable} instance.
     *
     * @see #throwableInfoOfNonNull(Throwable)
     * @see #throwableInfoOfNullable(Throwable)
     */
    @Immutable
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ThrowableInfo {

        public final String type;
        public final String message;
        public final String stackTrace;
        public final Throwable cause;

        /**
         * @return a new instance representing the "empty" ThrowableInfo
         * @apiNote This is currently not public, but this might change in the future. For now, we assume that the
         * singleton instance returned by {@link #emptyThrowableInfo()} is the correct method to use. This class is
         * immutable, so creating new instances instead of using the singleton instance doesn't seem to make sense.
         */
        private static ThrowableInfo emptyInstance() {
            return new ThrowableInfo(null, null, null, null);
        }

        /**
         * Factory method. If {@code throwable} is null, the returned instance will contain all null values, and
         * {@link  #isEmptyInstance()} will return true.
         *
         * @param throwable original throwable, possibly null
         * @return constructed instance
         */
        public static ThrowableInfo of(@Nullable Throwable throwable) {
            if (isNull(throwable)) {
                return emptyThrowableInfo();
            }

            return new ThrowableInfo(
                    typeOf(throwable),
                    messageOf(throwable).orElse(null),
                    stackTraceOf(throwable),
                    nextCauseOf(throwable).orElse(null));
        }

        /**
         * Use this method to check whether this instance was created from a null Throwable object.
         *
         * @return true to indicate that this instance was created from a null Throwable; otherwise false
         */
        public boolean isEmptyInstance() {
            return isNull(type);
        }

        /**
         * @return true, if the Throwable this instance came from was not null and contained a message, otherwise false
         */
        public boolean hasMessage() {
            return isNotBlank(message);
        }

        /**
         * @return true, if the Throwable this instance came from was not null and had a cause, otherwise false
         */
        public boolean hasCause() {
            return nonNull(cause);
        }

        /**
         * @return an Optional containing the type of Throwable this instance came from, or an empty Optional
         */
        public Optional<String> getType() {
            return Optional.ofNullable(type);
        }

        /**
         * @return an Optional containing the message from the Throwable this instance came from, or an empty Optional
         */
        public Optional<String> getMessage() {
            return Optional.ofNullable(message);
        }

        /**
         * @return an Optional containing the stack trace of the Throwable this instance came from, or an empty Optional
         */
        public Optional<String> getStackTrace() {
            return Optional.ofNullable(stackTrace);
        }

        /**
         * @return an Optional containing the cause from the Throwable this instance came from, or an empty Optional
         * if the original Throwable did not have a cause or this instance came from a null Throwable
         */
        public Optional<Throwable> getCause() {
            return Optional.ofNullable(cause);
        }
    }

    /**
     * Represents an "empty" {@link ThrowableInfo}, which is when the Throwable it comes from is null.
     */
    public static final ThrowableInfo EMPTY_THROWABLE_INFO = ThrowableInfo.emptyInstance();

    /**
     * Get the singleton "empty" instance.
     *
     * @return the "empty" ThrowableInfo singleton instance
     * @see #EMPTY_THROWABLE_INFO
     */
    public static ThrowableInfo emptyThrowableInfo() {
        return EMPTY_THROWABLE_INFO;
    }

    /**
     * Create a {@link ThrowableInfo} from given {@link Throwable}.
     *
     * @param throwable a {@link Throwable} instance
     * @return a {@link ThrowableInfo} instance
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static ThrowableInfo throwableInfoOfNonNull(Throwable throwable) {
        checkArgumentNotNull(throwable, "Cannot generate throwableInfoOf from a null object");

        return ThrowableInfo.of(throwable);
    }

    /**
     * Create a {@link ThrowableInfo} from given {@link Throwable}.
     *
     * @param throwable a {@link Throwable} instance
     * @return a {@link ThrowableInfo} instance
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static Optional<ThrowableInfo> throwableInfoOfNullable(@Nullable Throwable throwable) {
        return Optional.ofNullable(throwable).map(KiwiThrowables::throwableInfoOfNonNull);
    }

    /**
     * Get the direct cause of the {@link Throwable}.
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the direct cause
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static Optional<Throwable> nextCauseOf(Throwable throwable) {
        checkArgumentNotNull(throwable, "Cannot generate nextCauseOf from a null object");

        return Optional.ofNullable(throwable.getCause());
    }

    /**
     * Get the direct cause of the {@link Throwable} or {@link Optional#empty()} if {@code throwable} is {@code null}.
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the direct cause
     */
    public static Optional<Throwable> nextCauseOfNullable(@Nullable Throwable throwable) {
        return Optional.ofNullable(throwable).map(Throwable::getCause);
    }

    /**
     * Get the root cause of the {@link Throwable}.
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the root cause
     * @throws IllegalArgumentException if {@code throwable} if {@code null}
     */
    public static Optional<Throwable> rootCauseOf(Throwable throwable) {
        checkArgumentNotNull(throwable, "Cannot generate rootCauseOf from a null object");

        return Optional.ofNullable(ExceptionUtils.getRootCause(throwable));
    }

    /**
     * Get the root cause of the {@link Throwable} or {@link Optional#empty()} if {@code throwable} is {@code null}.
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the root cause
     * @throws IllegalArgumentException if {@code throwable} if {@code null}
     */
    public static Optional<Throwable> rootCauseOfNullable(@Nullable Throwable throwable) {
        return Optional.ofNullable(throwable).map(ExceptionUtils::getRootCause);
    }

    /**
     * Get the type of {@link Throwable}
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the type
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static String typeOf(Throwable throwable) {
        checkArgumentNotNull(throwable, "Cannot generate typeOf from a null object");

        return throwable.getClass().getName();
    }

    /**
     * Get the type of {@link Throwable} or {@link Optional#empty()} if {@code throwable} is {@code null}
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the type
     */
    public static Optional<String> typeOfNullable(@Nullable Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(Object::getClass)
                .map(Class::getName);
    }

    /**
     * Get the message of {@link Throwable}
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the message, if present, or {@link Optional#empty()} if not
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static Optional<String> messageOf(Throwable throwable) {
        checkArgumentNotNull(throwable, "Cannot generate messageOf from a null object");

        return Optional.ofNullable(throwable.getMessage());
    }

    /**
     * Get the message from {@link Throwable} or {@link Optional#empty()}  if {@code throwable} is {@code null}.
     *
     * @param throwable the {@link Throwable} to analyze
     * @return the message, if present or {@link Optional#empty()} if not
     */
    public static Optional<String> messageOfNullable(@Nullable Throwable throwable) {
        return Optional.ofNullable(throwable).map(Throwable::getMessage);
    }

    /**
     * Simply wraps the Apache Commons' {@link ExceptionUtils#getStackTrace(Throwable)} method. Mainly here to make it
     * easy to get a stack trace while staying in the same {@link KiwiThrowables} API.
     *
     * @param throwable the {@link Throwable} instance
     * @return the stack trace of the {@code throwable}, or {@code null}
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static String stackTraceOf(Throwable throwable) {
        checkArgumentNotNull(throwable, "Cannot generate stackTraceOf from a null object");

        return ExceptionUtils.getStackTrace(throwable);
    }

    /**
     * Simply wraps the Apache Commons' {@link ExceptionUtils#getStackTrace(Throwable)} method. Mainly here to make it
     * easy to get a stack trace while staying in the same {@link KiwiThrowables} API.
     *
     * @param throwable the {@link Throwable} instance
     * @return the stack trace of the {@code throwable}, or {@code null}
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static Optional<String> stackTraceOfNullable(@Nullable Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(ExceptionUtils::getStackTrace);
    }

    /**
     * If {@code throwable} is of type {@code wrapperClass}, then "unwrap" the cause and return it. Otherwise return
     * {@code throwable}
     *
     * @param throwable    the {@link Throwable} to unwrap if its class matches
     * @param wrapperClass the typ eof class to match
     * @return the unwrapped {@link Throwable} or the original {@code throwable}
     */
    public static Throwable unwrap(Throwable throwable, Class<?> wrapperClass) {
        checkArgumentNotNull(throwable, "throwable cannot be null");
        checkArgumentNotNull(wrapperClass, "wrapperClass cannot be null");

        if (throwable.getClass().equals(wrapperClass)) {
            return throwable.getCause();
        }

        return throwable;
    }
}
