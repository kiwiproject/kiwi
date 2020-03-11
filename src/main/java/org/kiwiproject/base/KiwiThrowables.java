package org.kiwiproject.base;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.errorprone.annotations.Immutable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
         * Factory method
         *
         * @param throwable original throwable
         * @return constructed instance
         */
        public static ThrowableInfo of(Throwable throwable) {
            return new ThrowableInfo(
                    typeOfNullable(throwable).orElse(null),
                    messageOfNullable(throwable).orElse(null),
                    stackTraceOfNullable(throwable).orElse(null),
                    nextCauseOfNullable(throwable).orElse(null));
        }

        public boolean hasMessage() {
            return isNotBlank(message);
        }

        public Optional<String> getType() {
            return Optional.ofNullable(type);
        }

        public Optional<String> getMessage() {
            return Optional.ofNullable(message);
        }

        public Optional<String> getStackTrace() {
            return Optional.ofNullable(stackTrace);
        }

        public Optional<Throwable> getCause() {
            return Optional.ofNullable(cause);
        }
    }

    public static final ThrowableInfo EMPTY_THROWABLE_INFO = ThrowableInfo.of(null);

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
    public static Optional<ThrowableInfo> throwableInfoOfNullable(Throwable throwable) {
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
    public static Optional<Throwable> nextCauseOfNullable(Throwable throwable) {
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
    public static Optional<Throwable> rootCauseOfNullable(Throwable throwable) {
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
    public static Optional<String> typeOfNullable(Throwable throwable) {
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
    public static Optional<String> messageOfNullable(Throwable throwable) {
        return Optional.ofNullable(throwable).map(Throwable::getMessage);
    }

    /**
     * Simply wraps the Apache Commons' {@link ExceptionUtils#getStackTrace(Throwable)} method. Mainly here to make it
     * easy to obtain a stack trace while staying in the same {@link KiwiThrowables} API.
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
     * easy to obtain a stack trace while staying in the same {@link KiwiThrowables} API.
     *
     * @param throwable the {@link Throwable} instance
     * @return the stack trace of the {@code throwable}, or {@code null}
     * @throws IllegalArgumentException if {@code throwable} is {@code null}
     */
    public static Optional<String> stackTraceOfNullable(Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(ExceptionUtils::getStackTrace);
    }

    /**
     * If {@code throwable} is of type {@code wrapperClass}, then "unwrap" the cause and return it. Otherwise return
     * {@code throwable}
     *
     * @param throwable the {@link Throwable} to unwrap if its class matches
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
