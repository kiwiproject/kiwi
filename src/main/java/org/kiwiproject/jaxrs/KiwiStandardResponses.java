package org.kiwiproject.jaxrs;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiStrings.f;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import org.kiwiproject.jaxrs.exception.ErrorMessage;
import org.kiwiproject.jaxrs.exception.JaxrsException;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;

import java.net.URI;
import java.util.Optional;

/**
 * A set of "standard" Jakarta REST responses for various HTTP methods. The "standard" is simply Kiwi's view of
 * what should be in responses for common HTTP methods in a REST-based interface using JSON as the primary
 * data format.
 * <p>
 * These utilities are intended for use within Jakarta REST resource classes.
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
 * directly to the utilities provided here without needing to call additional methods, for example, without needing to
 * call {@code orElse(null)}. So, we acknowledge that it is generally not good to accept {@link Optional} arguments,
 * but we're trading off convenience in this class against "generally accepted" practice.
 * @see KiwiResources
 * @see KiwiResponses
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@UtilityClass
public class KiwiStandardResponses {

    /**
     * Returns a  {@code 200 OK} response if the entity is non-null. Otherwise, returns a 404 Not Found response with
     * a message stating that the entity having type "entityType" was not found using the given identifier field
     * and value.
     *
     * @param identifierField the field which identifies the entity being looked up, e.g. "id"
     * @param identifier      the value of the identifier field, e.g., 42 the value of the identifier field, e.g., 42
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
     * @param identifier      the value of the identifier field, e.g., 42
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
     * Returns a {@code 200 OK} response if the entity is non-null. Otherwise, returns a 404 Not Found response with
     * a message stating that the entity was not found using the given identifier field and value.
     *
     * @param identifierField the field which identifies the entity being looked up, e.g. "id"
     * @param identifier      the value of the identifier field, e.g., 42
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
     * @param identifier      the value of the identifier field, e.g., 42
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
     * Returns a {@code 200 OK} response if the entity is non-null. Otherwise, returns a 404 Not Found response with
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
     * Returns a {@code 201 Created} response builder having the specified Location header and response entity.
     *
     * @param location the value for the Location header
     * @param entity   the new entity
     * @return a response builder with status code 201 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardPostResponseBuilder(URI location, Object entity) {
        return KiwiResources.createdResponseBuilder(location, entity).type(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a {@code 200 OK} response having the specified response entity.
     *
     * @param entity the updated entity
     * @return a 200 response with {@code application/json} content type
     */
    public static Response standardPutResponse(Object entity) {
        return standardPutResponseBuilder(entity).build();
    }

    /**
     * Returns a {@code 200 OK} response builder having the specified response entity.
     *
     * @param entity the updated entity
     * @return a response builder with status code 200 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardPutResponseBuilder(Object entity) {
        return KiwiResources.okResponseBuilder(entity).type(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a {@code 200 OK} response having the specified response entity.
     *
     * @param entity the updated/patched entity
     * @return a 200 response with {@code application/json} content type
     */
    public static Response standardPatchResponse(Object entity) {
        return standardPatchResponseBuilder(entity).build();
    }

    /**
     * Returns a {@code 200 OK} response builder having the specified response entity.
     *
     * @param entity the updated/patched entity
     * @return a response builder with status code 200 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardPatchResponseBuilder(Object entity) {
        return standardPutResponseBuilder(entity);
    }

    /**
     * Returns a {@code 204 No Content} response for DELETE requests that do not return an entity.
     *
     * @return a 204 response with {@code application/json} content type
     */
    public static Response standardDeleteResponse() {
        return standardDeleteResponseBuilder().build();
    }

    /**
     * Returns a 204 No Content response builder for DELETE requests that do not return an entity.
     *
     * @return a response builder with status code 204 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardDeleteResponseBuilder() {
        return Response.noContent().type(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a {@code 200 OK} response for DELETE requests that return an entity.
     *
     * @param deletedEntity the deleted entity
     * @return a 200 response with {@code application/json} content type
     */
    public static Response standardDeleteResponse(Object deletedEntity) {
        return standardDeleteResponseBuilder(deletedEntity).build();
    }

    /**
     * Returns a {@code 200 OK} response builder for DELETE requests that return an entity.
     *
     * @param deletedEntity the deleted entity
     * @return a response builder with a 200 status code and {@code application/json} content type
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
     * Returns a {@code 400 Bad Request} response builder containing an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a response builder with status code 400 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardBadRequestResponseBuilder(String errorDetails) {
        return standardErrorResponseBuilder(Response.Status.BAD_REQUEST, errorDetails);
    }

    /**
     * Returns a {@code 401 Unauthorized} response containing an {@link ErrorMessage} entity which uses {@code errorDetails}
     * as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 401 response with {@code application/json} content type
     */
    public static Response standardUnauthorizedResponse(String errorDetails) {
        return standardUnauthorizedResponseBuilder(errorDetails).build();
    }

    /**
     * Returns a {@code 401 Unauthorized} response builder containing an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a response builder with status code 401 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardUnauthorizedResponseBuilder(String errorDetails) {
        return standardErrorResponseBuilder(Response.Status.UNAUTHORIZED, errorDetails);
    }

    /**
     * Returns a {@code 404 Not Found} response containing an {@link ErrorMessage} entity which uses {@code errorDetails}
     * as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a 404 response with {@code application/json} content type
     */
    public static Response standardNotFoundResponse(String errorDetails) {
        return standardNotFoundResponseBuilder(errorDetails).build();
    }

    /**
     * Returns a {@code 404 Not Found} response builder containing an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a response builder with status code 404 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardNotFoundResponseBuilder(String errorDetails) {
        return standardErrorResponseBuilder(Response.Status.NOT_FOUND, errorDetails);
    }

    /**
     * Returns a {@code 500 Internal Server Error} response containing an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a {@code 500 Internal Server Error} response with {@code application/json} content type
     */
    public static Response standardInternalServerErrorResponse(String errorDetails) {
        return standardInternalServerErrorResponseBuilder(errorDetails).build();
    }

    /**
     * Returns a response builder with {@code 500 Internal Server Error} status and an {@link ErrorMessage} entity
     * which uses {@code errorDetails} as the detailed error message.
     *
     * @param errorDetails the error message to use
     * @return a response builder with a 500 status code and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardInternalServerErrorResponseBuilder(String errorDetails) {
        return standardErrorResponseBuilder(Response.Status.INTERNAL_SERVER_ERROR, errorDetails);
    }

    /**
     * Returns a response having the given status and an {@link ErrorMessage} entity which uses {@code errorDetails}
     * as the detailed error message.
     * <p>
     * Verifies that the given status is actually an error status (4xx or 5xx).
     *
     * @param status       the error status to use
     * @param errorDetails the error message to use
     * @return a response with the given status code and {@code application/json} content type
     * @throws IllegalArgumentException if the given status is not a client or server error
     */
    public static Response standardErrorResponse(Response.Status status, String errorDetails) {
        return standardErrorResponseBuilder(status, errorDetails).build();
    }

    /**
     * Returns a response builder having the given status and an {@link ErrorMessage} entity which uses
     * {@code errorDetails} as the detailed error message.
     * <p>
     * Verifies that the given status is actually an error status (4xx or 5xx).
     *
     * @param status       the error status to use
     * @param errorDetails the error message to use
     * @return a response builder with the given status code and {@code application/json} content type
     * @throws IllegalArgumentException if the given status is not a client or server error
     */
    public static Response.ResponseBuilder standardErrorResponseBuilder(Response.Status status, String errorDetails) {
        var family = status.getFamily();
        var statusCode = status.getStatusCode();
        checkArgument(family == Family.CLIENT_ERROR || family == Family.SERVER_ERROR,
                "status %s is not a client error (4xx) or server error (5xx)", statusCode);

        return JaxrsExceptionMapper.buildResponseBuilder(new JaxrsException(errorDetails, statusCode));
    }

    /**
     * Returns a {@code 202 Accepted} response having the specified response entity.
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
     * Returns a {@code 202 Accepted} response builder having the specified response entity.
     * <p>
     * This generally applies to POST, PUT, and PATCH requests that might take a while and are processed asynchronously.
     *
     * @param entity the accepted entity
     * @return a response builder with status code 202 and {@code application/json} content type
     */
    public static Response.ResponseBuilder standardAcceptedResponseBuilder(Object entity) {
        return Response.accepted(entity).type(MediaType.APPLICATION_JSON);
    }
}
