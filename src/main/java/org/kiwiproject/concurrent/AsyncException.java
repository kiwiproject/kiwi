package org.kiwiproject.concurrent;

import static java.util.Objects.nonNull;

import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A subclass {@link RuntimeException} used to indicate problems in asynchronous code.
 * <p>
 * The cause will generally be an {@link InterruptedException} or one of the checked exceptions thrown by Java's
 * futures, specifically either {@link java.util.concurrent.TimeoutException} or
 * {@link java.util.concurrent.ExecutionException}
 *
 * @implNote Sadly, we cannot make this class generic, i.e. {@code AsyncException<T>}. It will not compile; the compiler
 * reports the following error: "a generic class may not extend java.lang.Throwable". However, we can fake it
 * by declaring {@link #getFuture()} in a generic manner (and suppressing the unchecked warning). This means you
 * could still receive a {@link ClassCastException} at runtime if you attempt a cast to an invalid type.
 */
public class AsyncException extends RuntimeException {

    private final transient CompletableFuture<?> future;

    /**
     * Construct instance with the given message and future.
     *
     * @param message the exception message
     * @param future  the {@link CompletableFuture} that caused the error, may be null
     */
    public AsyncException(String message, @Nullable CompletableFuture<?> future) {
        super(message);
        this.future = future;
    }

    /**
     * Construct instance with the given message, cause, and future.
     *
     * @param message the exception message
     * @param cause   the original cause of the exception
     * @param future  the {@link CompletableFuture} that caused the error, may be null
     */
    public AsyncException(String message, Throwable cause, @Nullable CompletableFuture<?> future) {
        super(message, cause);
        this.future = future;
    }

    /**
     * Does this AsyncException contain a future?
     *
     * @return true if this instance contains a CompletableFuture
     * @apiNote When a single asynchronous operation is performed and there is only one future, then callers can
     * expect this to contain a CompletableFuture and return true. When multiple futures are acted upon (e.g., waiting
     * for all to complete), callers should expect this instance not to contain a CompletableFuture and this method
     * to return false.
     */
    public boolean hasFuture() {
        return nonNull(future);
    }

    /**
     * The future which causes the exception; it may be null. Use {@link #hasFuture()} to check if this instance
     * contains a future.
     *
     * @param <T> the generic type of the CompletableFuture
     * @return the future causing this exception, or null
     * @throws ClassCastException if the type you assign you is not the actual type
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> CompletableFuture<T> getFuture() {
        return (CompletableFuture<T>) future;
    }
}
