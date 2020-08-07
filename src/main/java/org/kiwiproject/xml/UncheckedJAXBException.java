package org.kiwiproject.xml;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import javax.xml.bind.JAXBException;

/**
 * Wraps a {@link JAXBException} with an unchecked exception.
 */
public class UncheckedJAXBException extends RuntimeException {

    /**
     * Construct an instance.
     *
     * @param message the message, which can be null
     * @param cause   the cause, which cannot be null
     * @throws IllegalArgumentException if cause is null
     */
    public UncheckedJAXBException(String message, JAXBException cause) {
        super(message, requireNotNull(cause));
    }

    /**
     * Construct an instance.
     *
     * @param cause the cause, which cannot be null
     * @throws IllegalArgumentException if cause is null
     */
    public UncheckedJAXBException(JAXBException cause) {
        super(requireNotNull(cause));
    }

    /**
     * Returns the cause of this exception.
     *
     * @return the {@link JAXBException} which is the cause of this exception
     */
    @Override
    public synchronized JAXBException getCause() {
        return (JAXBException) super.getCause();
    }
}
