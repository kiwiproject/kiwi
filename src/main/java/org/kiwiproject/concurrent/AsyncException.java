package org.kiwiproject.concurrent;

import java.util.concurrent.CompletableFuture;

/**
 * A subclass {@link RuntimeException} used to indicate problems in asynchronous code.
 *
 * @implNote Sadly, we cannot make the {@link CompletableFuture} generic. It will not compile, and the compiler
 * reports the following error: "a generic class may not extend java.lang.Throwable". However, we can fake it
 * by declaring {@link #getFuture()} in a generic manner (and suppressing the unchecked warning). This means you
 * could still receive a {@link ClassCastException} at runtime if you attempt a cast to an invalid type.
 */
@SuppressWarnings("rawtypes")
public class AsyncException extends RuntimeException {

    private final transient CompletableFuture future;

    /**
     * Construct instance with given message and future.
     */
    public AsyncException(String message, CompletableFuture future) {
        super(message);
        this.future = future;
    }

    /**
     * Construct instance with given message, cause, and future.
     */
    public AsyncException(String message, Throwable cause, CompletableFuture future) {
        super(message, cause);
        this.future = future;
    }

    /**
     * The future which causes the exception. May be null.
     *
     * @return the future causing this exception, or null
     */
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> getFuture() {
        return future;
    }
}
