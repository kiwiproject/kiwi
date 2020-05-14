package org.kiwiproject.concurrent;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * A subclass {@link RuntimeException} used to indicate problems in asynchronous code.
 *
 * @implNote Sadly, we cannot make the {@link CompletableFuture} generic. It will not compile, and the compiler
 * reports the following error: "a generic class may not extend java.lang.Throwable". However, we can fake it
 * by declaring {@link #getFuture()} in a generic manner (and suppressing the unchecked warning). This means you
 * could still receive a {@link ClassCastException} at runtime if you attempt a cast to an invalid type.
 */
@SuppressWarnings({"rawtypes", "java:S3740"})
public class AsyncException extends RuntimeException {

    private final transient CompletableFuture future;

    /**
     * Construct instance with given message and future.
     *
     * @param message the exception message
     * @param future  the {@link CompletableFuture} that caused the error, may be null
     */
    public AsyncException(String message, @Nullable CompletableFuture future) {
        super(message);
        this.future = future;
    }

    /**
     * Construct instance with given message, cause, and future.
     *
     * @param message the exception message
     * @param cause   the original cause of the exception
     * @param future  the {@link CompletableFuture} that caused the error, may be null
     */
    public AsyncException(String message, Throwable cause, @Nullable CompletableFuture future) {
        super(message, cause);
        this.future = future;
    }

    /**
     * The future which causes the exception. May be null.
     *
     * @param <T> the generic type of the CompletableFuture
     * @return the future causing this exception, or null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> CompletableFuture<T> getFuture() {
        return future;
    }
}
