package org.kiwiproject.jaxrs;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Represents the result of a {@link KiwiEntities#safeReadEntityResult} call, containing either
 * a successfully read entity or the exception that prevented it from being read.
 * <p>
 * At most one of {@code entity} or {@code exception} will be non-null at any given time.
 * If the read succeeded, {@code exception} will be null, though {@code entity} may also
 * be null if the response did not have a body (e.g., a 204 No Content response).
 * If the read failed, {@code exception} will be non-null and {@code entity} will be null.
 * <p>
 * Use {@link #hasEntity()} or {@link #hasException()} to determine the outcome
 * before accessing the value.
 *
 * @param <T>       the type of the entity
 * @param entity    the entity read from the response, or null if the response did not have a body
 *                  or an exception occurred
 * @param exception the exception that occurred while reading the entity, or null if successful
 */
public record ReadEntityResult<T>(T entity, Exception exception) {

    /**
     * Compact constructor that enforces the invariant that entity and exception
     * cannot both be non-null.
     *
     * @throws IllegalArgumentException if both entity and exception are non-null
     */
    public ReadEntityResult {
        checkArgument(!(nonNull(entity) && nonNull(exception)),
                "entity and exception cannot both be non-null");
    }

    /**
     * Returns true if the read succeeded, i.e., no exception occurred.
     * Note that the entity itself may still be null if the response did not
     * have a body, such as a 204 No Content response.
     *
     * @return true if no exception occurred, false otherwise
     */
    public boolean hasEntity() {
        return isNull(exception);
    }

    /**
     * Returns true if an exception occurred while reading the entity.
     *
     * @return true if an exception is present, false otherwise
     */
    public boolean hasException() {
        return nonNull(exception);
    }
}
