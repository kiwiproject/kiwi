package org.kiwiproject.security;

import static java.util.Objects.requireNonNull;

import java.security.GeneralSecurityException;

/**
 * Wraps a {@link GeneralSecurityException} with an unchecked exception.
 */
public class UncheckedGeneralSecurityException extends RuntimeException {

    /**
     * Constructs a new instance.
     *
     * @param cause the {@link GeneralSecurityException}
     */
    public UncheckedGeneralSecurityException(GeneralSecurityException cause) {
        super(requireNonNull(cause));
    }

    /**
     * Constructs a new instance.
     *
     * @param message the detail message; may be null
     * @param cause   the {@link GeneralSecurityException}
     */
    public UncheckedGeneralSecurityException(String message, GeneralSecurityException cause) {
        super(message, requireNonNull(cause));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized GeneralSecurityException getCause() {
        return (GeneralSecurityException) super.getCause();
    }
}
