package org.kiwiproject.dropwizard.lifecycle;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import org.kiwiproject.io.KiwiIO;

import java.io.Closeable;
import java.io.IOException;

/**
 * A Dropwizard {@link Managed} that manages a single {@link Closeable} instance.
 * By default, {@link #stop()} propagates any {@link IOException} thrown during close.
 * Use {@link #closingQuietly(Closeable)} to create an instance that suppresses close
 * errors instead.
 */
public class ManagedCloseable implements Managed {

    @Getter
    private final Closeable closeable;

    private final boolean closeQuietly;

    /**
     * Creates a {@code ManagedCloseable} with default behavior: {@link #stop()} will
     * propagate any {@link IOException} thrown when closing the given {@link Closeable}.
     *
     * @param closeable the {@link Closeable} to manage
     * @throws IllegalArgumentException if closeable is null
     */
    public ManagedCloseable(Closeable closeable) {
        this(closeable, false);
    }

    private ManagedCloseable(Closeable closeable, boolean closeQuietly) {
        checkArgumentNotNull(closeable, "closeable must not be null");
        this.closeable = closeable;
        this.closeQuietly = closeQuietly;
    }

    /**
     * Creates a {@code ManagedCloseable} with default behavior: {@link #stop()} will
     * propagate any {@link IOException} thrown when closing the given {@link Closeable}.
     *
     * @param closeable the {@link Closeable} to manage
     * @return a new ManagedCloseable
     * @throws IllegalArgumentException if closeable is null
     */
    public static ManagedCloseable of(Closeable closeable) {
        return new ManagedCloseable(closeable, false);
    }

    /**
     * Creates a {@code ManagedCloseable} that suppresses any errors thrown when closing
     * the given {@link Closeable}, logging them instead of propagating them.
     *
     * @param closeable the {@link Closeable} to manage
     * @return a new ManagedCloseable
     * @throws IllegalArgumentException if closeable is null
     */
    public static ManagedCloseable closingQuietly(Closeable closeable) {
        return new ManagedCloseable(closeable, true);
    }

    /**
     * No-op.
     */
    @Override
    public void start() {
        // Nothing to do
    }

    /**
     * Closes the managed {@link Closeable}. If this instance was created with
     * {@link #closingQuietly(Closeable)}, any errors thrown during close are suppressed
     * and logged rather than propagated. Otherwise, any {@link IOException} thrown by
     * {@link Closeable#close()} is propagated to the caller.
     *
     * @throws IOException if the close fails and this instance was not created with
     *                     {@link #closingQuietly(Closeable)}
     * @see KiwiIO#closeQuietly(Closeable)
     */
    @Override
    public void stop() throws IOException {
        if (closeQuietly) {
            KiwiIO.closeQuietly(closeable);
        } else {
            closeable.close();
        }
    }
}
