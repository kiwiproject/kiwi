package org.kiwiproject.dropwizard.lifecycle;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import org.kiwiproject.io.KiwiIO;

import java.io.Closeable;
import java.io.IOException;

/**
 * A Dropwizard {@link Managed} that manages a single {@link Closeable} instance.
 * The wrapped instance is accessible via {@link #getCloseable()}.
 * <p>
 * Use {@link #of(Closeable)} or the constructor for default behavior, where
 * {@link #stop()} propagates any {@link IOException} thrown during close.
 * Use {@link #closingQuietly(Closeable)} to create an instance that suppresses any
 * {@link IOException} thrown during close instead of propagating it.
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
        this.closeable = requireNotNull(closeable, "closeable must not be null");
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
     * Creates a {@code ManagedCloseable} that suppresses any {@link IOException} thrown
     * when closing the given {@link Closeable}, logging it instead of propagating it.
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
     *
     * @apiNote The {@link Closeable} is expected to already be open when this
     * managed object is created.
     */
    @Override
    public void start() {
        // Nothing to do
    }

    /**
     * Closes the managed {@link Closeable}. If this instance was created with
     * {@link #closingQuietly(Closeable)}, any {@link IOException} thrown during close is
     * suppressed and logged rather than propagated. Otherwise, any {@link IOException}
     * thrown by {@link Closeable#close()} is propagated to the caller.
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
