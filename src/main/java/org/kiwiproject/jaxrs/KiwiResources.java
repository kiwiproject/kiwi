package org.kiwiproject.jaxrs;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.jaxrs.KiwiJaxrsValidations.assertNotNull;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;
import org.kiwiproject.jaxrs.exception.JaxrsNotFoundException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Static utilities for use in JAX-RS resource classes. Contains utilities for verifying entities (e.g. obtained from
 * a service or data access class), factories for creating new responses, and for validating query parameters.
 *
 * @apiNote Some methods in this class accept {@link Optional} arguments, which we know is considered a code smell
 * by various people and analysis tools such as IntelliJ's inspections, Sonar, etc. However, we also like to return
 * {@link Optional} from data access code (e.g. a DAO "findById" method where the object might not exist if it was
 * recently deleted). In such cases, we can simply take the Optional returned by those finder methods and pass them
 * directly to the utilities provided here without needing to call additional methods, for example without needing to
 * call {@code orElse(null)}. So, we acknowledge that it is generally not good to accept {@link Optional} arguments,
 * but we're trading off convenience in this class against "generally accepted" practice.
 * @see KiwiResponses
 * @see KiwiStandardResponses
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@UtilityClass
@Slf4j
public class KiwiResources {

    private static final Map<String, Object> EMPTY_HEADERS = Map.of();

    /**
     * Verifies that {@code resourceEntity} is not null, otherwise throws a {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity the resource entity to verify
     * @param <T>            the object type
     * @throws JaxrsNotFoundException if the entity is null
     */
    public static <T> void verifyExistence(T resourceEntity) {
        verifyExistence(resourceEntity, null);
    }

    /**
     * Verifies that {@code resourceEntity} contains a value, otherwise throws a {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity the resource entity to verify
     * @param <T>            the object type
     * @return the entity if the Optional contains a value
     * @throws JaxrsNotFoundException if the entity is empty
     */
    public static <T> T verifyExistence(Optional<T> resourceEntity) {
        verifyExistence(resourceEntity.orElse(null), null);

        return resourceEntity.orElseThrow();
    }

    /**
     * Verifies that {@code resourceEntity} is not null, otherwise throws a {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity the resource entity to verify
     * @param entityType     a Class representing the entity type, used in error messages
     * @param identifier     the unique identifier of the resource identity
     * @param <T>            the object type
     * @throws JaxrsNotFoundException if the entity is null
     */
    public static <T> void verifyExistence(T resourceEntity, Class<T> entityType, Object identifier) {
        var notFoundMessage = JaxrsNotFoundException.buildMessage(entityType.getSimpleName(), identifier);
        verifyExistence(resourceEntity, notFoundMessage);
    }

    /**
     * Verifies that {@code resourceEntity} contains a value, otherwise throws {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity the resource entity to verify
     * @param entityType     a Class representing the entity type, used in error messages
     * @param identifier     the unique identifier of the resource identity
     * @param <T>            the object type
     * @return the entity if the Optional contains a value
     * @throws JaxrsNotFoundException if the entity is empty
     */
    public static <T> T verifyExistence(Optional<T> resourceEntity, Class<T> entityType, Object identifier) {
        var notFoundMessage = JaxrsNotFoundException.buildMessage(entityType.getSimpleName(), identifier);
        verifyExistence(resourceEntity.orElse(null), notFoundMessage);

        return resourceEntity.orElseThrow();
    }

    /**
     * Verifies that {@code resourceEntity} is not null, otherwise throws {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity  the resource entity to verify
     * @param notFoundMessage the error message to include in the response entity
     * @param <T>             the object type
     * @throws JaxrsNotFoundException if the entity is null
     */
    public static <T> void verifyExistence(T resourceEntity, String notFoundMessage) {
        if (isNull(resourceEntity)) {
            throw new JaxrsNotFoundException(notFoundMessage);
        }
    }

    /**
     * Verifies that {@code resourceEntity} contains a value, otherwise throws {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity  the resource entity to verify
     * @param notFoundMessage the error message to include in the response entity
     * @param <T>             the object type
     * @return the entity if the Optional contains a value
     * @throws JaxrsNotFoundException if the entity is empty
     */
    public static <T> T verifyExistence(Optional<T> resourceEntity, String notFoundMessage) {
        verifyExistence(resourceEntity.orElse(null), notFoundMessage);

        return resourceEntity.orElseThrow();
    }

    /**
     * Builds a {@link Response} having the given status and entity.
     *
     * @param status the response status
     * @param entity the response entity
     * @return a response
     */
    public static Response newResponse(Response.Status status, Object entity) {
        return newResponseBuilder(status, entity).build();
    }

    /**
     * Creates a {@link Response.ResponseBuilder} having the given status and entity.
     * You can further modify the returned build, e.g. add custom headers, set cookies. etc.
     *
     * @param status the response status
     * @param entity the response entity
     * @return a response builder
     */
    public static Response.ResponseBuilder newResponseBuilder(Response.Status status, Object entity) {
        return newResponseBuilder(status, entity, EMPTY_HEADERS);
    }

    /**
     * Builds a {@link Response} having the given status, entity, and content type.
     *
     * @param status      the response status
     * @param entity      the response entity
     * @param contentType the value for the Content-Type header
     * @return a response
     */
    public static Response newResponse(Response.Status status,
                                       Object entity,
                                       String contentType) {
        return newResponseBuilder(status, entity, contentType).build();
    }

    /**
     * Creates a {@link Response.ResponseBuilder} having the given status, entity, and content type.
     * You can further modify the returned build, e.g. add custom headers, set cookies. etc.
     *
     * @param status      the response status
     * @param entity      the response entity
     * @param contentType the value for the Content-Type header
     * @return a response builder
     */
    public static Response.ResponseBuilder newResponseBuilder(Response.Status status,
                                                              Object entity,
                                                              String contentType) {
        return newResponseBuilder(status, entity).type(contentType);
    }

    /**
     * Builds a {@link Response} having the given status, entity, and (single-valued) headers.
     *
     * @param status              the response status
     * @param entity              the response entity
     * @param singleValuedHeaders map containing single-valued response headers
     * @return a response
     */
    public static Response newResponse(Response.Status status,
                                       Object entity,
                                       Map<String, Object> singleValuedHeaders) {
        return newResponseBuilder(status, entity, singleValuedHeaders).build();
    }

    /**
     * Creates a {@link Response.ResponseBuilder} having the given status, entity, and (single-valued) headers.
     * You can further modify the returned build, e.g. add custom headers, set cookies. etc.
     *
     * @param status              the response status
     * @param entity              the response entity
     * @param singleValuedHeaders map containing single-valued response headers
     * @return a response builder
     */
    public static Response.ResponseBuilder newResponseBuilder(Response.Status status,
                                                              Object entity,
                                                              Map<String, Object> singleValuedHeaders) {
        var responseBuilder = Response.status(status).entity(entity);
        singleValuedHeaders.forEach(responseBuilder::header);

        return responseBuilder;
    }

    /**
     * Builds a {@link Response} having the given status, entity, and headers.
     *
     * @param status  the response status
     * @param entity  the response entity
     * @param headers map containing response headers
     * @return a response
     */
    public static Response newResponse(Response.Status status,
                                       Object entity,
                                       MultivaluedMap<String, Object> headers) {
        return newResponseBuilder(status, entity, headers).build();
    }

    /**
     * Creates a {@link Response.ResponseBuilder} having the given status, entity, and headers.
     * You can further modify the returned build, e.g. add custom headers, set cookies. etc.
     *
     * @param status  the response status
     * @param entity  the response entity
     * @param headers map containing response headers
     * @return a response builder
     */
    public static Response.ResponseBuilder newResponseBuilder(Response.Status status,
                                                              Object entity,
                                                              MultivaluedMap<String, Object> headers) {
        var responseBuilder = Response.status(status).entity(entity);

        headers.forEach((name, values) ->
                values.forEach(value -> responseBuilder.header(name, value)));

        return responseBuilder;
    }

    /**
     * Builds a {@link Response} with 201 Created status and a specified Location header and entity.
     *
     * @param location the value for the Location header
     * @param entity   the response entity
     * @return a 202 Created response
     */
    public static Response createdResponse(URI location, Object entity) {
        return createdResponseBuilder(location, entity).build();
    }

    /**
     * Creates a {@link Response.ResponseBuilder} having 201 Created status and a specified Location header and entity.
     * You can further modify the returned build, e.g. add custom headers, set cookies. etc.
     *
     * @param location the value for the Location header
     * @param entity   the response entity
     * @return a 201 Created response builder
     */
    public static Response.ResponseBuilder createdResponseBuilder(URI location, Object entity) {
        return Response.created(location).entity(entity);
    }

    /**
     * Builds a {@link Response} with 200 OK status and a specified entity.
     *
     * @param entity the response entity
     * @return a 200 OK response
     */
    public static Response okResponse(Object entity) {
        return okResponseBuilder(entity).build();
    }

    /**
     * Creates a {@link Response.ResponseBuilder} having 200 OK status and a specified entity.
     * You can further modify the returned build, e.g. add custom headers, set cookies. etc.
     *
     * @param entity the response entity
     * @return a 200 OK response builder
     */
    public static Response.ResponseBuilder okResponseBuilder(Object entity) {
        return Response.ok(entity);
    }

    /**
     * Convenience wrapper around {@link Response#fromResponse(Response)} that also buffers the response entity by
     * calling {@link Response#bufferEntity()} on the given response. This returns a {@link Response} instead of a
     * response builder.
     * <p>
     * NOTE: The reason this method exists is due to the note in the Javadoc of {@link Response#fromResponse(Response)}
     * which states: <em>"Note that if the entity is backed by an un-consumed input stream, the reference to the stream
     * is copied. In such case make sure to buffer the entity stream of the original response instance before passing
     * it to this method."</em> So, rather than having the same boilerplate code in various locations (or as we've
     * seen many times, people forgetting to buffer the response entity), this provides a single method to perform
     * the same logic <em>and</em> ensure the entity is buffered.
     *
     * @param originalResponse a Response from which the status code, entity and response headers will be copied.
     * @return a new response
     * @see Response#fromResponse(Response)
     * @see Response#bufferEntity()
     */
    public static Response newResponseBufferingEntityFrom(Response originalResponse) {
        return newResponseBuilderBufferingEntityFrom(originalResponse).build();
    }

    /**
     * Convenience wrapper around {@link Response#fromResponse(Response)} that also buffers the response entity by
     * calling {@link Response#bufferEntity()} on the given response.
     * <p>
     * NOTE: The reason this method exists is due to the note in the Javadoc of {@link Response#fromResponse(Response)}
     * which states: <em>"Note that if the entity is backed by an un-consumed input stream, the reference to the stream
     * is copied. In such case make sure to buffer the entity stream of the original response instance before passing
     * it to this method."</em> So, rather than having the same boilerplate code in various locations (or as we've
     * seen many times, people forgetting to buffer the response entity), this provides a single method to perform
     * the same logic <em>and</em> ensure the entity is buffered.
     *
     * @param originalResponse a Response from which the status code, entity and response headers will be copied.
     * @return a new response builder
     * @see Response#fromResponse(Response)
     * @see Response#bufferEntity()
     */
    public static Response.ResponseBuilder newResponseBuilderBufferingEntityFrom(Response originalResponse) {
        var wasBuffered = originalResponse.bufferEntity();

        if (!wasBuffered) {
            LOG.warn("Attempt to buffer entity in original response returned false; possible causes:" +
                    " it was not backed by an unconsumed input stream; the input stream was already consumed; or, it did not have an entity");
        }

        return Response.fromResponse(originalResponse);
    }

    /**
     * Checks whether {@code parameters} contains parameter named {@code parameterName} that is an integer or
     * something that can be converted into an integer.
     *
     * @param parameters    the parameters to check
     * @param parameterName name of the parameter which should be present
     * @return the int value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present, or is not an integer
     */
    public static int validateIntParameter(Map<String, Object> parameters, String parameterName) {
        assertNotNull(parameterName, parameters);

        var value = parameters.get(parameterName);
        assertNotNull(parameterName, value);

        //noinspection UnstableApiUsage
        var result = Optional.ofNullable(Ints.tryParse(value.toString()));

        return result.orElseThrow(() ->
                new JaxrsBadRequestException(f("{} must be an integer", value), parameterName));
    }

    /**
     * Checks whether {@code parameters} contains parameter named {@code parameterName} that is a long or
     * something that can be converted into a long.
     *
     * @param parameters    the parameters to check
     * @param parameterName name of the parameter which should be present
     * @return the long value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present, or is not long
     */
    public static long validateLongParameter(Map<String, Object> parameters, String parameterName) {
        assertNotNull(parameterName, parameters);

        var value = parameters.get(parameterName);
        assertNotNull(parameterName, value);

        //noinspection UnstableApiUsage
        var result = Optional.ofNullable(Longs.tryParse(value.toString()));

        return result.orElseThrow(() ->
                new JaxrsBadRequestException(f("{} must be a long", value), parameterName));
    }
}
