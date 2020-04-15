package org.kiwiproject.net;

import static java.util.Objects.requireNonNull;

import java.net.URISyntaxException;

/**
 * Wraps a {@link java.net.URISyntaxException} with an unchecked exception.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class UncheckedURISyntaxException extends RuntimeException {

    public UncheckedURISyntaxException(String message, URISyntaxException cause) {
        super(message, requireNonNull(cause));
    }

    public UncheckedURISyntaxException(URISyntaxException cause) {
        super(requireNonNull(cause));
    }

    @Override
    public synchronized URISyntaxException getCause() {
        return (URISyntaxException) super.getCause();
    }
}
