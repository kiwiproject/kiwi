package org.kiwiproject.xml;

/**
 * Runtime wrapper exception for XML processing errors.
 */
public class XmlRuntimeException extends RuntimeException {

    /**
     * Create an instance with the given message.
     *
     * @param message the message
     */
    public XmlRuntimeException(String message) {
        super(message);
    }

    /**
     * Create an instance with the given message and cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public XmlRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create instance with given cause.
     *
     * @param cause the cause
     */
    public XmlRuntimeException(Throwable cause) {
        super(cause);
    }
}
