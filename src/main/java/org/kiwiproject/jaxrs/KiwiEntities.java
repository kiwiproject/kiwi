package org.kiwiproject.jaxrs;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Static utilities related to reading entities from a {@link Response}.
 */
@UtilityClass
@Slf4j
public class KiwiEntities {

    /**
     * Read an entity as a String from the given response. If it cannot be read, return an empty {@link Optional}.
     *
     * @param response the response object
     * @return an Optional that contains the response as a String or an empty Optional
     */
    public static Optional<String> safeReadEntity(Response response) {
        return safeReadEntity(response, String.class);
    }

    /**
     * Read an entity as a String from the given response. If it cannot be read, return a default message.
     *
     * @param response       the response object
     * @param defaultMessage a default message to return if the response entity cannot be read (can be null)
     * @return the response entity as a String or the given default message
     */
    public static String safeReadEntity(Response response, @Nullable String defaultMessage) {
        return safeReadEntity(response).orElse(defaultMessage);
    }

    /**
     * Read an entity as a String from the given response. If it cannot be read, call the given {@link Supplier} to
     * get the default message.
     *
     * @param response               the response object
     * @param defaultMessageSupplier supplies a default message if the response entity cannot be read
     *                               (the Supplier can return null, but the Supplier itself must not be null)
     * @return the response entity as a String or the message supplied by the given Supplier
     */
    public static String safeReadEntity(Response response, Supplier<String> defaultMessageSupplier) {
        checkArgumentNotNull(defaultMessageSupplier, "defaultMessageSupplier must not be null");
        return safeReadEntity(response).orElseGet(defaultMessageSupplier);
    }

    /**
     * Read an entity as an instance of the given {@link Class} specified by {@code entityType}.
     * If it cannot be read, return an empty {@link Optional}.
     *
     * @param response   the response object
     * @param entityType the type of entity the response is expected to contain
     * @param <T>        the entity type
     * @return an Optional that contains the response as a specific type or an empty Optional
     */
    public static <T> Optional<T> safeReadEntity(Response response, Class<T> entityType) {
        try {
            var entity = response.readEntity(entityType);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            return emptyOptional(e);
        }
    }

    /**
     * Read an entity as an instance of the given {@link GenericType} specified by {@code entityType}.
     * If it cannot be read, return an empty {@link Optional}.
     *
     * @param response   the response object
     * @param entityType the type of entity the response is expected to contain
     * @param <T>        the entity type
     * @return an Optional that contains the response as a specific type or an empty Optional
     */
    public static <T> Optional<T> safeReadEntity(Response response, GenericType<T> entityType) {
        try {
            var entity = response.readEntity(entityType);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            return emptyOptional(e);
        }
    }

    private static <T> Optional<T> emptyOptional(Exception e) {
        LOG.error("Error reading response entity", e);
        return Optional.empty();
    }
}
