package org.kiwiproject.reflect;

/**
 * A custom runtime exception for wrapping (checked or unchecked) exceptions related to reflection.
 * <p>
 * There are no restrictions on the wrapped {@link Throwable}, but the <em>intent</em> is that they should be
 * some checked or unchecked exception that was thrown by a reflective operation, e.g., reflectively
 * finding or invoking a method, or accessing a field.
 */
@SuppressWarnings("unused")
public class RuntimeReflectionException extends RuntimeException {

    /**
     * Construct a new instance with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public RuntimeReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new instance with and cause.
     *
     * @param cause the underlying cause
     */
    public RuntimeReflectionException(Throwable cause) {
        super(cause);
    }
}
