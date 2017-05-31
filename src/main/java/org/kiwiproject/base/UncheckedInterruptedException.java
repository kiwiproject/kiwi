package org.kiwiproject.base;

import static java.util.Objects.requireNonNull;

public class UncheckedInterruptedException extends RuntimeException {

    public UncheckedInterruptedException(String message, InterruptedException cause) {
        super(message, requireNonNull(cause, "cause cannot be null"));
    }

    public UncheckedInterruptedException(InterruptedException cause) {
        super(requireNonNull(cause, "cause cannot be null"));
    }

    @Override
    public InterruptedException getCause() {
        return (InterruptedException) super.getCause();
    }
}
