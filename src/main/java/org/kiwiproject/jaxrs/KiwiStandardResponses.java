package org.kiwiproject.jaxrs;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.jaxrs.exception.ErrorMessage;
import org.kiwiproject.jaxrs.exception.JaxrsException;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

/**
 * A set of "standard" JAX-RS responses for various HTTP methods. The "standard" is simply Kiwi's view of
 * what should be in responses for common HTTP methods in a REST-based interface using JSON as the primary
 * data format.
 * <p>
 * These utilities are intended for use within JAX-RS resource classes.
 * <p>
 * One specific thing to note is that the content type is always set to {@link MediaType#APPLICATION_JSON}, since
 * the primary use case of this class assumes JSON-based REST interfaces. You can change the content type by
 * using the methods that return a response builder and call one of the {@code type()} methods with a
 * {@link MediaType} or String argument. This will let you override the default JSON content type in situations
 * where you need to return a different content type.
 *
 * @apiNote Some methods in this class accept {@link Optional} arguments, which we know is considered a code smell
 * by various people and analysis tools such as IntelliJ's inspections, Sonar, etc. However, we also like to return
 * {@link Optional} from data access code (e.g. a DAO "findById" method where the object might not exist if it was
 * recently deleted). In such cases, we can simply take the Optional returned by those finder methods and pass them
 * directly to the utilities provided here without needing to call additional methods, for example without needing to
 * call {@code orElse(null)}. So, we acknowledge that it is generally not good to accept {@link Optional} arguments,
 * but we're trading off convenience in this class against "generally accepted" practice.
 * @see KiwiResources
 * @see KiwiResponses
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@UtilityClass
public class KiwiStandardResponses {

    /**
     * Returns a 200 OK response if the entity is non-null. Otherwise, returns a 404 Not Found response with
     * a message stating that the entity having type "entityType" was not found using the given identifier field
     * and value.
     *
     * @param identifierField the field which identifies the entity being looked up, e.g. "id"
     * @param identifier      the value of the identifier field, e.g. 42  the value of the identifier field, e.g. 42
     * @param entity          the entity or null
     * @param entityType      the entity type
     * @param <T>             the entity type
     * @return a 200 or 404 response with {@code application/json} content type
     */
    public static <T> Response standardGetResponse(String identifierField,
                                                   Object identifier,
                                                   @Nullable T entity,
                                                   Class<T> entityType) {
        if (nonNull(entity)) {
            return Response.ok(entity).type(MediaType.APPLICATION_JSON).build();
        }

        var message = f("{} with {} {} not found", entityType.getSimpleName(), identifierField, identifier);
        return standardNotFoundResponse(message);
    }

    /**
     * Returns a 200 OK response if the entity contains a value. Otherwise, returns a 404 Not Found response with
     * a message stating that the entity having type "entityType" was not found using the given identifier field
     * and value.
     *
     * @param identifierField the field which identifies the entity being looked up, e.g. "id"
     * @param identifier      the value of the identifier field, e.g. 42
     * @param entity          an Optional that may or may not contain an entity
     * @param entityType      the entity type
     * @param <T>             the entity type
     * @return a 200 or 404 response with {@code application/json} content type
     */
    public static <T> Response standardGetResponse(String identifierField,
                                                   Object identifier,
                                                   Optional<T> entity,
                                                   Class<T> entityType) {
        return standardGetResponse(identifierField, identifier, entity.orElse(null), entityType);
    }

    /**
     * Returns a 200 OK response if the entity is non-null. Otherwise, returns a 404 Not Found response with
     * a message stating that the entity was not found using the given identifier field and value.
     *
     * @param identifierField the field which identifies the entity being looked up, e.g. "id"
     * @param identifier      the value of the identifier field, e.g. 42
     * @param entity          the entity or null
     * @param <T>             the entity type
     * @return a 200 or 404 response with {@code application/json} content type
     */
    public static <T> Response standardGetResponse(String identifierField,
                                                   Object identifier,
                                                   @Nullable T entity) {
        if (nonNull(entity)) {
            return Response.ok(entity).type(MediaType.APPLICATION_JSON).build();
        }

        var message = f("Object with {} {} not found", identifierField, identifier);
        return standardNotFoundResponse(message);
    }

    /**
     * Returns a 200 OK response if the entity contains a value. Otherwise, returns a 404 Not Found response with
     * a message stating that the entity was not found using the given identifier field and value.
     *
     * @param identifierField the field which identifies the entity being looked up, e.g. "id"
     * @param identifier      the value of the identifier field, e.g. 42
     * @param entity          an Optional that may or may not contain an entity
     * @param <T>             the entity type
     * @return a 200 or 404 response with {@code application/json} content type
     */
    public static <T> Response standardGetResponse(String identifierField,
                                                   Object identifier,
                                                   Optional<T> entity) {
        return standardGetResponse(identifierField, identifier, entity.orElse(null));
    }

    /**
     * Returns a 200 OK response if the entity is non-null. Otherwise, returns a 404 Not Found response with
     * the given detail message.
     *
     * @param entity          the entity or null
     * @param notFoundMessage the specific message to use in the 404 response (if entity is null)
     * @param <T>             the entity type
     * @return a 200 or 404 response with {@code application/json} content type
     */
    public static <T> Response standardGetResponse(@Nullable T entity, String notFoundMessage) {
        if (nonNull(entity)) {
            return Response.ok(entity).type(MediaType.APPLICATION_JSON).build();
        }

        return standardNotFoundResponse(notFoundMessage);
    }

    /**
     * Returns a 200 OK response if the entity contains a value. Otherwise, returns a 404 Not Found response with
     * the given detail message.
     *
     * @param entity          an Optional that may or may not contain an entity
     * @param notFoundMessage the specific message to use in the 404 response (if entity Optional is empty)
     * @param <T>             the entity type
     * @return a 200 or 404 response with {@code application/json} content type
     */
    public static <T> Response standardGetResponse(Optional<T> entity, String notFoundMessage) {
        return standardGetResponse(entity.orElse(null), notFoundMessage);
    }

    /**
     * Returns a 201 Created response having the specified Location header and response entity.
     *
     * @param location the value for the Location header
     * @param entity   the new entity
     * @return a 201 response with {@code application/json} content type
     */
    public static Response standardPostResponse(URI location, Object entity) {
        return standardPostResponseBuilder(location, entity).build();
    }

    /**
     * Returns a 201 Created response builder having the specified Location header and response entity.
     *
     * @param location the value for the Location header
     * @param entity   the new entity
     * @return a 201 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardPostResponseBuilder(URI location, Object entity) {
        return KiwiResources.createdResponseBuilder(location, entity).type(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a 200 OK response having the specified response entity.
     *
     * @param entity the updated entity
     * @return a 200 response with {@code application/json} content type
     */
    public static Response standardPutResponse(Object entity) {
        return standardPutResponseBuilder(entity).build();
    }

    /**
     * Returns a 200 OK response builder having the specified response entity.
     *
     * @param entity the updated entity
     * @return a 200 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardPutResponseBuilder(Object entity) {
        return KiwiResources.okResponseBuilder(entity).type(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a 200 OK response having the specified response entity.
     *
     * @param entity the updated/patched entity
     * @return a 200 response with {@code application/json} content type
     */
    public static Response standardPatchResponse(Object entity) {
        return standardPatchResponseBuilder(entity).build();
    }

    /**
     * Returns a 200 OK response builder having the specified response entity.
     *
     * @param entity the updated/patched entity
     * @return a 200 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardPatchResponseBuilder(Object entity) {
        return standardPutResponseBuilder(entity);
    }

    /**
     * Returns a 204 No Content response for DELETE requests that do not return an entity.
     *
     * @return a 204 response with {@code application/json} content type
     */
    public static Response standardDeleteResponse() {
        return standardDeleteResponseBuilder().build();
    }

    /**
     * Returns a 204 No Content response builder for DELETE requests that do not return an entity.
     *
     * @return a 204 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardDeleteResponseBuilder() {
        return Response.noContent().type(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a 200 OK response for DELETE requests that return an entity.
     *
     * @param deletedEntity the deleted entity
     * @return a 200 response with {@code application/json} content type
     */
    public static Response standardDeleteResponse(Object deletedEntity) {
        return standardDeleteResponseBuilder(deletedEntity).build();
    }

    /**
     * Returns a 200 OK response builder for DELETE requests that return an entity.
     *
     * @param deletedEntity the deleted entity
     * @return a 200 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardDeleteResponseBuilder(Object deletedEntity) {
        return Response.ok(deletedEntity).type(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a 400 Bad Request response containing an {@link ErrorMessage} entity which uses {@code errorDetails}
     * as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 400 response with {@code application/json} content type
     */
    public static Response standardBadRequestResponse(String errorDetails) {
        return standardBadRequestResponseBuilder(errorDetails).build();
    }

    /**
     * Returns a 400 Bad Request response builder containing an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 400 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardBadRequestResponseBuilder(String errorDetails) {
        return standardErrorResponseBuilder(Response.Status.BAD_REQUEST, errorDetails);
    }

    /**
     * Returns a 401 Unauthorized response containing an {@link ErrorMessage} entity which uses {@code errorDetails}
     * as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 401 response with {@code application/json} content type
     */
    public static Response standardUnauthorizedResponse(String errorDetails) {
        return standardUnauthorizedResponseBuilder(errorDetails).build();
    }

    /**
     * Returns a 401 Unauthorized response builder containing an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 401 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardUnauthorizedResponseBuilder(String errorDetails) {
        return standardErrorResponseBuilder(Response.Status.UNAUTHORIZED, errorDetails);
    }

    /**
     * Returns a 404 Not Found response containing an {@link ErrorMessage} entity which uses {@code errorDetails}
     * as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 404 response with {@code application/json} content type
     */
    public static Response standardNotFoundResponse(String errorDetails) {
        return standardNotFoundResponseBuilder(errorDetails).build();
    }

    /**
     * Returns a 404 Not Found response builder containing an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 404 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardNotFoundResponseBuilder(String errorDetails) {
        return standardErrorResponseBuilder(Response.Status.NOT_FOUND, errorDetails);
    }

    /**
     * Returns a response having the given status and an {@link ErrorMessage} entity which uses {@code errorDetails}
     * as the detailed error message.
     * <p>
     * Does not verify that the given status is actually an error status.
     *
     * @param status       the error status to use
     * @param errorDetails the error message to use
     * @return a response with the given status code and {@code application/json} content type
     */
    public static Response standardErrorResponse(Response.Status status, String errorDetails) {
        return standardErrorResponseBuilder(status, errorDetails).build();
    }

    /**
     * Returns a response builder having the given status and an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     * <p>
     * Does not verify that the given status is actually an error status.
     *
     * @param status       the error status to use
     * @param errorDetails the error message to use
     * @return a response builder with the given status code and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardErrorResponseBuilder(Response.Status status, String errorDetails) {
        return JaxrsExceptionMapper.buildResponseBuilder(new JaxrsException(errorDetails, status.getStatusCode()));
    }

    /**
     * Returns a 202 Accepted response having the specified response entity.
     * <p>
     * This generally applies to POST, PUT, and PATCH requests that might take a while and are processed asynchronously.
     *
     * @param entity the accepted entity
     * @return a 202 response with {@code application/json} content type
     */
    public static Response standardAcceptedResponse(Object entity) {
        return standardAcceptedResponseBuilder(entity).build();
    }

    /**
     * Returns a 202 Accepted response builder having the specified response entity.
     * <p>
     * This generally applies to POST, PUT, and PATCH requests that might take a while and are processed asynchronously.
     *
     * @param entity the accepted entity
     * @return a 202 response builder with {@code application/json} content type
     */
    public static Response.ResponseBuilder standardAcceptedResponseBuilder(Object entity) {
        return Response.accepted(entity).type(MediaType.APPLICATION_JSON);
    }
}
