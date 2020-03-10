package org.kiwiproject.base;

import static java.util.Objects.requireNonNull;

/**
 * Wraps an {@link InterruptedException} with an unchecked exception. Basically, copied Java's
 * {@link java.io.UncheckedIOException}, renamed it, and modified it.
 */
public class UncheckedInterruptedException extends RuntimeException {

    /**
     * Constructs an instance of this class.
     *
     * @param message the detail message, can be null
     * @param cause   the {@code InterruptedException}
     * @throws NullPointerException if the cause is {@code null}
     */
    public UncheckedInterruptedException(String message, InterruptedException cause) {
        super(message, requireNonNull(cause, "cause cannot be null"));
    }

    /**
     * Constructs an instance of this class.
     *
     * @param cause the {@code InterruptedException}
     * @throws NullPointerException if the cause is {@code null}
     */
    public UncheckedInterruptedException(InterruptedException cause) {
        super(requireNonNull(cause, "cause cannot be null"));
    }

    /**
     * Returns the cause of this exception.
     *
     * @return the {@link InterruptedException} which is the cause of this exception
     */
    @Override
    public synchronized InterruptedException getCause() {
        return (InterruptedException) super.getCause();
    }
}
