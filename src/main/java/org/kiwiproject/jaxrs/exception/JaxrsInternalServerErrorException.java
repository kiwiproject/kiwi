package org.kiwiproject.jaxrs.exception;

/**
 * Exception representing a 500 Internal Server Error.
 * It extends {@link JaxrsException} to use Kiwi's {@link ErrorMessage}.
 */
public class JaxrsInternalServerErrorException extends JaxrsException {

    /**
     * The status code for all instances of this exception.
     */
    public static final int CODE = 500;

    /**
     * New instance with given cause and 500 status code.
     *
     * @param cause the cause of this exception
     */
    public JaxrsInternalServerErrorException(Throwable cause) {
        super(cause, CODE);
    }

    /**
     * New instance with given message and 500 status code.
     *
     * @param message the message for this exception
     */
    public JaxrsInternalServerErrorException(String message) {
        super(message, CODE);
    }

    /**
     * New instance with given message, cause, and 500 status code.
     *
     * @param message the message for this exception
     * @param cause   the cause of this exception
     */
    public JaxrsInternalServerErrorException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    /**
     * New instance with the given message and field name, and 500 status code.
     *
     * @param message   the message for this exception
     * @param fieldName the field/property name that caused this error
     */
    public JaxrsInternalServerErrorException(String message, String fieldName) {
        this(message, fieldName, null);
    }

    /**
     * New instance with the given message, field name, item ID, and 500 status code.
     *
     * @param message   the message for this exception
     * @param fieldName the field/property name that caused this error
     * @param itemId    the unique ID of the item that caused this error
     */
    public JaxrsInternalServerErrorException(String message, String fieldName, String itemId) {
        this(message, fieldName, itemId, null);
    }

    /**
     * New instance with the given message, field name, item ID, cause, and 500 status code.
     *
     * @param message   the message for this exception
     * @param fieldName the field/property name that caused this error
     * @param itemId    the unique ID of the item that caused this error
     * @param cause     the cause of this exception
     */
    public JaxrsInternalServerErrorException(String message, String fieldName, String itemId, Throwable cause) {
        super(new ErrorMessage(itemId, CODE, message, fieldName), cause);
    }
}
