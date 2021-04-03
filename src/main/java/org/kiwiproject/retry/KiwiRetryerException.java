package org.kiwiproject.retry;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Exception thrown by {@link KiwiRetryer}.
 * <p>
 * It will wrap either a {@link RetryException} or an {@link InterruptedException}.
 * <p>
 * A {@link RetryException} indicates that all attempts failed, while an {@link InterruptedException} occurs when
 * a thread is interrupted. Note specifically that failed attempts may have failed due to a specific result or
 * because an exception was thrown. In other words, a {@link Retryer} can be configured with result as well as
 * exception predicates to trigger a retry.
 */
public class KiwiRetryerException extends RuntimeException {

    /**
     * Construct an instance with the given information.
     *
     * @param message the detail message
     * @param cause   the (non-null) cause of the failure (a {@link RetryException} or {@link InterruptedException})
     * @throws IllegalArgumentException if the cause is null, or is not a {@link RetryException} or {@link InterruptedException}
     */
    public KiwiRetryerException(String message, Exception cause) {
        super(message, requireValidCause(cause));
    }

    private static Exception requireValidCause(Exception cause) {
        checkArgumentNotNull(cause, "cause is required");
        checkArgument(cause instanceof RetryException || cause instanceof InterruptedException,
                "cause must be a RetryException or an InterruptedException but was a %s",
                cause.getClass().getName());
        return cause;
    }

    /**
     * Unwrap the given {@link KiwiRetryerException} to reveal the underlying exception that caused it.
     * <p>
     * Since it is possible for an {@link Attempt} to fail without an exception, this method returns an {@link Optional}
     * to indicate there might not actually be an exception cause.
     *
     * @param kiwiRetryerEx the exception to unwrap
     * @return the unwrapped cause
     * @see #unwrapKiwiRetryerExceptionFully(KiwiRetryerException)
     */
    public static Optional<Throwable> unwrapKiwiRetryerException(KiwiRetryerException kiwiRetryerEx) {
        var cause = kiwiRetryerEx.getCause();
        return Optional.of(cause);
    }

    /**
     * Unwrap the given {@link KiwiRetryerException} to reveal the underlying exception that caused it.
     * <p>
     * The difference between {@link #unwrapKiwiRetryerException(KiwiRetryerException)} and this method and is that the
     * former does not unwrap the {@link RetryException}, whereas the latter (this method) does.
     * <p>
     * Since it is possible for an attempt to fail without an exception, this method returns an {@link Optional} to
     * indicate there might not actually be an exception cause.
     *
     * @param kiwiRetryerEx the exception to unwrap
     * @return the fully unwrapped cause
     */
    public static Optional<Throwable> unwrapKiwiRetryerExceptionFully(KiwiRetryerException kiwiRetryerEx) {
        var throwable = unwrapKiwiRetryerException(kiwiRetryerEx).orElse(null);

        if (throwable instanceof RetryException) {
            return Optional.ofNullable(throwable.getCause());
        }

        return Optional.ofNullable(throwable);
    }

    /**
     * Unwrap the given {@link KiwiRetryerException} to reveal the underlying exception that caused it.
     *
     * @return the unwrapped cause
     * @see #unwrapKiwiRetryerException(KiwiRetryerException)
     * @see #unwrapFully()
     */
    public Optional<Throwable> unwrap() {
        return unwrapKiwiRetryerException(this);
    }

    /**
     * Unwrap the given {@link KiwiRetryerException} to reveal the underlying exception that caused it.
     *
     * @return the fully unwrapped cause
     * @see #unwrapKiwiRetryerExceptionFully(KiwiRetryerException)
     */
    public Optional<Throwable> unwrapFully() {
        return unwrapKiwiRetryerExceptionFully(this);
    }

    /**
     * @return true if the cause of this exception is a {@link RetryException}, otherwise false
     */
    public boolean isCauseRetryException() {
        return getCause() instanceof RetryException;
    }

    /**
     * @return true if the cause of this exception is an {@link InterruptedException}, otherwise false
     */
    public boolean isCauseInterruptedException() {
        return getCause() instanceof InterruptedException;
    }

    /**
     * If the underlying cause is a {@link RetryException} return the last failed {@link Attempt}.
     *
     * @return an Optional of the last failed Attempt
     */
    @SuppressWarnings("java:S1452")  // suppress generic wildcard type, since it's in retrying-again like this
    public Optional<Attempt<?>> getLastAttempt() {
        return unwrapAsRetryException().map(RetryException::getLastFailedAttempt);
    }

    /**
     * If the underlying cause is a {@link RetryException} return the number of failed {@link Attempt}s.
     *
     * @return an OptionalInt of the number of failed attempts
     */
    public OptionalInt getNumberOfFailedAttempts() {
        return unwrapAsRetryException()
                .stream()
                .mapToInt(RetryException::getNumberOfFailedAttempts)
                .findFirst();
    }

    /**
     * If the underlying cause is a {@link RetryException}, unwrap it.
     *
     * @return an Optional of the RetryException causing the retry failure
     */
    public Optional<RetryException> unwrapAsRetryException() {
        return unwrap()
                .filter(RetryException.class::isInstance)
                .map(RetryException.class::cast);
    }
}
