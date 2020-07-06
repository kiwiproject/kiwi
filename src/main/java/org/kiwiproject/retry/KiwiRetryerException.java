package org.kiwiproject.retry;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import com.github.rholder.retry.RetryException;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Exception thrown by {@link KiwiRetryer}.
 */
public class KiwiRetryerException extends RuntimeException {

    /**
     * Construct an instance with the given information.
     *
     * @param message the detail message
     * @param cause   the (non-null) cause of the failure
     */
    public KiwiRetryerException(String message, Throwable cause) {
        super(message, requireNotNull(cause, "cause is required"));
    }

    /**
     * Unwrap the given {@link KiwiRetryerException} to reveal the underlying exception that caused it.
     * <p>
     * If the given exception is an {@link ExecutionException} (i.e. something bad and unexpected occurred during
     * the retry attempts) then its cause will be returned. Otherwise return the direct cause, which is most
     * likely a {@link RetryException}.
     * <p>
     * Since it is possible for an attempt to fail without an exception, or for an {@link ExecutionException} to have
     * a null cause, this method returns an {@link Optional} to indicate there might not actually be an exception
     * cause.
     *
     * @param kiwiRetryerEx the exception to unwrap
     * @return the unwrapped cause
     * @see #unwrapKiwiRetryerExceptionFully(KiwiRetryerException)
     */
    public static Optional<Throwable> unwrapKiwiRetryerException(KiwiRetryerException kiwiRetryerEx) {
        var cause = kiwiRetryerEx.getCause();

        // Unwrap execution exceptions
        if (cause instanceof ExecutionException) {
            var executionException = (ExecutionException) cause;
            return Optional.ofNullable(executionException.getCause());
        }

        // If we are here, it's presumably a RetryException so return it
        return Optional.of(cause);
    }

    /**
     * Unwrap the given {@link KiwiRetryerException} to reveal the underlying exception that caused it.
     * <p>
     * If the given exception is an {@link ExecutionException} (i.e. something bad and unexpected occurred during
     * the retry attempts) then its cause will be returned. If it is a {@link RetryException}, then unwrap it
     * and return its cause.
     * <p>
     * The difference between {@link #unwrapKiwiRetryerException(KiwiRetryerException)} and this method and is that the
     * former does not unwrap the {@link RetryException}, whereas the latter (this method) does.
     * <p>
     * Since it is possible for an attempt to fail without an exception, or for an {@link ExecutionException} to have
     * a null cause, this method returns an {@link Optional} to indicate there might not actually be an exception
     * cause.
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
}
