package org.kiwiproject.net;

import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;

/**
 * Wraps a {@link MalformedURLException} with an unchecked exception
 */
@SuppressWarnings("unused")
public class UncheckedMalformedURLException extends RuntimeException {

    public UncheckedMalformedURLException(MalformedURLException cause) {
        super(requireNonNull(cause));
    }

    public UncheckedMalformedURLException(String message, MalformedURLException cause) {
        super(message, requireNonNull(cause));
    }

    @Override
    public synchronized MalformedURLException getCause() {
        return (MalformedURLException) super.getCause();
    }
}
