package org.kiwiproject.jaxrs;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Optional;

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
