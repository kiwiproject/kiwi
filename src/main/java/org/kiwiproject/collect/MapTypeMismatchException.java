package org.kiwiproject.collect;

import static org.kiwiproject.base.KiwiStrings.f;

/**
 * Exception thrown when a value in a {@link java.util.Map} cannot be cast to the expected type.
 */
public class MapTypeMismatchException extends RuntimeException {

    /**
     * Constructs a new instance with no detail message.
     */
    public MapTypeMismatchException() {
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public MapTypeMismatchException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public MapTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance with the specified cause and a detail message
     * of {@code (cause==null ? null : cause.toString())}.
     *
     * @param cause the cause
     */
    public MapTypeMismatchException(Throwable cause) {
        super(cause);
    }

    /**
     * Factory method to create a new instance with a standardized message for a type mismatch.
     *
     * @param key       the map key whose value could not be cast to the expected type
     * @param valueType the expected type of the value
     * @param cause     the ClassCastException that occurred during the cast attempt
     * @return a new instance with a descriptive message
     */
    public static MapTypeMismatchException forTypeMismatch(Object key, Class<?> valueType, ClassCastException cause) {
        return new MapTypeMismatchException(
                f("Cannot cast value for key '{}' to type {}", key, valueType.getName()),
                cause);
    }
}
