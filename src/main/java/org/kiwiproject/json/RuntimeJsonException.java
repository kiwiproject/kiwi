package org.kiwiproject.json;

/**
 * Runtime wrapper exception for JSON processing errors.
 */
public class RuntimeJsonException extends RuntimeException {

    /**
     * Create an instance with the given cause.
     *
     * @param cause the cause
     */
    public RuntimeJsonException(Throwable cause) {
        super(cause);
    }

    /**
     * Create an instance with the given message and cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public RuntimeJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
