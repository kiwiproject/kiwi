package org.kiwiproject.jaxrs.exception;

/**
 * Exception representing a 400 Bad Request that extends {@link JaxrsException} to use Kiwi's {@link ErrorMessage}.
 */
public class JaxrsBadRequestException extends JaxrsException {

    /**
     * The status code for all instances of this exception.
     */
    public static final int CODE = 400;

    /**
     * New instance with given cause and 400 status code.
     *
     * @param cause the cause of this exception
     */
    public JaxrsBadRequestException(Throwable cause) {
        super(cause, CODE);
    }

    /**
     * New instance with given message and 400 status code.
     *
     * @param message the message for this exception
     */
    public JaxrsBadRequestException(String message) {
        super(message, CODE);
    }

    /**
     * New instance with given message, cause, and a 400 status code.
     *
     * @param message the message for this exception
     * @param cause   the cause of this exception
     */
    public JaxrsBadRequestException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    /**
     * New instance with the given message and field name, and a 400 status code.
     *
     * @param message   the message for this exception
     * @param fieldName the field/property name that caused this error
     */
    public JaxrsBadRequestException(String message, String fieldName) {
        this(message, fieldName, null);
    }

    /**
     * New instance with the given message, field name, item ID, and a 400 status code.
     *
     * @param message   the message for this exception
     * @param fieldName the field/property name that caused this error
     * @param itemId    the unique ID of the item that caused this error
     */
    public JaxrsBadRequestException(String message, String fieldName, String itemId) {
        this(message, fieldName, itemId, null);
    }

    /**
     * New instance with the given message, field name, item ID, cause, and 400 status code.
     *
     * @param message   the message for this exception
     * @param fieldName the field/property name that caused this error
     * @param itemId    the unique ID of the item that caused this error
     * @param cause     the cause of this exception
     */
    public JaxrsBadRequestException(String message, String fieldName, String itemId, Throwable cause) {
        super(new ErrorMessage(itemId, CODE, message, fieldName), cause);
    }
}
