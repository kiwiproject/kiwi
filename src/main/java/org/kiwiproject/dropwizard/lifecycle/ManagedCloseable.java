package org.kiwiproject.dropwizard.lifecycle;

import io.dropwizard.lifecycle.Managed;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;

/**
 * A Dropwizard {@link Managed} that manages a single {@link Closeable} instance.
 */
public class ManagedCloseable implements Managed {

    @Getter
    private final Closeable closeable;

    public ManagedCloseable(Closeable closeable) {
        this.closeable = closeable;
    }

    /**
     * No-op
     */
    @Override
    public void start() {
        // Nothing to do
    }

    /**
     * Delegates to the managed {@link Closeable}'s {@code close} method.
     *
     * @see Closeable#close()
     */
    @Override
    public void stop() throws IOException {
        closeable.close();
    }
}
