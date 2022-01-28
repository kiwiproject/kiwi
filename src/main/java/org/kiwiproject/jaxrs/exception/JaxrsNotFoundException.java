package org.kiwiproject.jaxrs.exception;

/**
 * Exception representing a 404 Not Found that extends {@link JaxrsException} to use Kiwi's {@link ErrorMessage}.
 */
public class JaxrsNotFoundException extends JaxrsException {

    /**
     * The status code for all instances of this exception.
     */
    public static final int CODE = 404;

    /**
     * New instance with given cause and 404 status code.
     *
     * @param cause the cause of this exception
     */
    public JaxrsNotFoundException(Throwable cause) {
        super(cause, CODE);
    }

    /**
     * New instance with given message and 404 status code.
     *
     * @param message the message for this exception
     */
    public JaxrsNotFoundException(String message) {
        super(message, CODE);
    }

    /**
     * New instance with given message, cause, and 404 status code.
     *
     * @param message the message for this exception
     * @param cause   the cause of this exception
     */
    public JaxrsNotFoundException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    /**
     * New instance with given type and item ID. Resulting message is built using {@link #buildMessage(String, Object)}.
     *
     * @param type   the type of object that was not found
     * @param itemId the unique key/identifier of the object that was not found
     */
    public JaxrsNotFoundException(String type, String itemId) {
        super(buildMessage(type, itemId), CODE);
    }

    /**
     * New instance with given type and item ID. Resulting message is built using {@link #buildMessage(String, Object)}.
     *
     * @param type   the type of object that was not found
     * @param itemId the unique key/identifier of the object that was not found
     */
    public JaxrsNotFoundException(String type, Object itemId) {
        super(buildMessage(type, itemId), CODE);
    }

    /**
     * Build a generic "not found" message using the given type and key.
     * <p>
     * Format: [type] [key] was not found.
     * <p>
     * Example: User 42 was not found.
     *
     * @param type the type of object that was not found
     * @param key  the unique key/identifier of the object that was not found
     * @return the generic "not found" message
     */
    public static String buildMessage(String type, Object key) {
        return type + " " + key + " was not found.";
    }
}
