package org.kiwiproject.jaxrs;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;
import static org.kiwiproject.jaxrs.KiwiJaxrsValidations.assertNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;
import org.kiwiproject.jaxrs.exception.JaxrsNotFoundException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Static utilities for use in Jakarta REST resource classes. Contains utilities for verifying entities (e.g. obtained
 * from a service or data access class), factories for creating new responses, and for validating query parameters.
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
    private static final String PARAMETERS_MUST_NOT_BE_NULL = "parameters must not be null";
    private static final String PARAMETER_NAME_MUST_NOT_BE_BLANK = "parameterName must not be blank";

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
     * Verifies that {@code resourceEntity} is not null and returns it,
     * otherwise throws a {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity the resource entity to verify
     * @param <T>            the object type
     * @return the entity, if it is not null
     * @throws JaxrsNotFoundException if the entity is null
     */
    @NonNull
    public static <T> T verifyExistenceAndReturn(T resourceEntity) {
        return verifyExistence(Optional.ofNullable(resourceEntity));
    }

    /**
     * Verifies that {@code resourceEntity} contains a value, otherwise throws a {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity the resource entity to verify
     * @param <T>            the object type
     * @return the entity if the Optional contains a value
     * @throws JaxrsNotFoundException if the entity is empty
     */
    @NonNull
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
     * Verifies that {@code resourceEntity} is not null and returns it,
     * otherwise throws a {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity the resource entity to verify
     * @param entityType     a Class representing the entity type, used in error messages
     * @param identifier     the unique identifier of the resource identity
     * @param <T>            the object type
     * @return the entity, if it is not null
     * @throws JaxrsNotFoundException if the entity is null
     */
    public static <T> T verifyExistenceAndReturn(T resourceEntity, Class<T> entityType, Object identifier) {
        return verifyExistence(Optional.ofNullable(resourceEntity), entityType, identifier);
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
    @NonNull
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
     * Verifies that {@code resourceEntity} is not null and returns it,
     * otherwise throws {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity  the resource entity to verify
     * @param notFoundMessage the error message to include in the response entity
     * @param <T>             the object type
     * @return the entity, if it is not null
     * @throws JaxrsNotFoundException if the entity is null
     */
    @NonNull
    public static <T> T verifyExistenceAndReturn(T resourceEntity, String notFoundMessage) {
        return verifyExistence(Optional.ofNullable(resourceEntity), notFoundMessage);
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
    @NonNull
    public static <T> T verifyExistence(Optional<T> resourceEntity, String notFoundMessage) {
        verifyExistence(resourceEntity.orElse(null), notFoundMessage);

        return resourceEntity.orElseThrow();
    }

    /**
     * Verifies that {@code resourceEntity} is not null, otherwise throws {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity          the resource entity to verify
     * @param notFoundMessageTemplate template for the error message to include in the response entity; uses
     *                                {@link KiwiStrings#format(String, Object...) KiwiStrings.format}
     *                                to construct the message
     * @param args                    the arguments to be substituted into the message template
     * @param <T>                     the object type
     * @throws JaxrsNotFoundException if the entity is null
     */
    public static <T> void verifyExistence(T resourceEntity, String notFoundMessageTemplate, Object... args) {
        if (isNull(resourceEntity)) {
            var message = f(notFoundMessageTemplate, args);
            throw new JaxrsNotFoundException(message);
        }
    }

    /**
     * Verifies that {@code resourceEntity} is not null and returns it,
     * otherwise throws {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity          the resource entity to verify
     * @param notFoundMessageTemplate template for the error message to include in the response entity; uses
     *                                {@link KiwiStrings#format(String, Object...) KiwiStrings.format}
     *                                to construct the message
     * @param args                    the arguments to be substituted into the message template
     * @param <T>                     the object type
     * @return the entity, if it is not null
     * @throws JaxrsNotFoundException if the entity is empty
     */
    @NonNull
    public static <T> T verifyExistenceAndReturn(T resourceEntity, String notFoundMessageTemplate, Object... args) {
        return verifyExistence(Optional.ofNullable(resourceEntity), notFoundMessageTemplate, args);
    }

    /**
     * Verifies that {@code resourceEntity} contains a value, otherwise throws {@link JaxrsNotFoundException}.
     *
     * @param resourceEntity          the resource entity to verify
     * @param notFoundMessageTemplate template for the error message to include in the response entity; uses
     *                                {@link KiwiStrings#format(String, Object...) KiwiStrings.format}
     *                                to construct the message
     * @param args                    the arguments to be substituted into the message template
     * @param <T>                     the object type
     * @return the entity if the Optional contains a value
     * @throws JaxrsNotFoundException if the entity is empty
     */
    @NonNull
    public static <T> T verifyExistence(Optional<T> resourceEntity, String notFoundMessageTemplate, Object... args) {
        verifyExistence(resourceEntity.orElse(null), notFoundMessageTemplate, args);

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
     * @param <V>           the type of values in the map
     * @return the int value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present, or is not an integer
     */
    public static <V> int validateIntParameter(Map<String, V> parameters, String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var value = parameters.get(parameterName);
        assertNotNull(parameterName, value);

        return parseIntOrThrowBadRequest(value, parameterName);
    }

    /**
     * Checks whether {@code parameters} contains a parameter named {@code parameterName} that has at least one
     * value that can be converted into an integer. If there is more than one value, then the first one returned
     * by {@link MultivaluedMap#getFirst(Object)} is returned.
     *
     * @param parameters    the multivalued parameters to check
     * @param parameterName name of the parameter which should be present
     * @param <V>           the type of values in the multivalued map
     * @return the int value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present with at least one value, or is
     *                                  not an integer
     */
    public static <V> int validateOneIntParameter(MultivaluedMap<String, V> parameters,
                                                  String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var value = parameters.getFirst(parameterName);
        assertNotNull(parameterName, value);

        return parseIntOrThrowBadRequest(value, parameterName);
    }

    /**
     * Checks whether {@code parameters} contains a parameter named {@code parameterName} that has exactly one
     * value that can be converted into an integer. If there is more than one value, this is considered a bad request
     * and a {@link JaxrsBadRequestException} is thrown.
     *
     * @param parameters    the multivalued parameters to check
     * @param parameterName name of the parameter which should be present
     * @param <V>           the type of values in the multivalued map
     * @return the int value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present with only one value, or is
     *                                  not an integer
     */
    public static <V> int validateExactlyOneIntParameter(MultivaluedMap<String, V> parameters,
                                                         String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var values = parameters.get(parameterName);
        assertOneElementOrThrowBadRequest(values, parameterName);

        var value = first(values);
        return parseIntOrThrowBadRequest(value, parameterName);
    }

    /**
     * Checks whether {@code parameters} contains a parameter named {@code parameterName} that has at least one
     * value that can be converted into an integer. All the values must be convertible to integer, and they are
     * all converted and returned in a List.
     *
     * @param parameters    the multivalued parameters to check
     * @param parameterName name of the parameter which should be present
     * @param <V>           the type of values in the multivalued map
     * @return an unmodifiable List containing the int values of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present with at least one value, or is
     *                                  not an integer
     */
    public static <V> List<Integer> validateOneOrMoreIntParameters(MultivaluedMap<String, V> parameters,
                                                                   String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var values = parameters.get(parameterName);
        assertOneOrMoreElementsOrThrowBadRequest(values, parameterName);

        return values.stream()
                .map(value -> parseIntOrThrowBadRequest(value, parameterName))
                .toList();
    }

    @VisibleForTesting
    static int parseIntOrThrowBadRequest(Object value, String parameterName) {
        var result = Optional.ofNullable(value).map(Object::toString).map(Ints::tryParse);

        return result.orElseThrow(() -> newJaxrsBadRequestException("'{}' is not an integer", value, parameterName));
    }

    /**
     * Checks whether {@code parameters} contains parameter named {@code parameterName} that is a long or
     * something that can be converted into a long.
     *
     * @param parameters    the parameters to check
     * @param parameterName name of the parameter which should be present
     * @param <V>           the type of values in the map
     * @return the long value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present, or is not a long
     */
    public static <V> long validateLongParameter(Map<String, V> parameters, String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var value = parameters.get(parameterName);
        assertNotNull(parameterName, value);

        return parseLongOrThrowBadRequest(value, parameterName);
    }

    /**
     * Checks whether {@code parameters} contains a parameter named {@code parameterName} that has at least one
     * value that can be converted into a long. If there is more than one value, then the first one returned
     * by {@link MultivaluedMap#getFirst(Object)} is returned.
     *
     * @param parameters    the multivalued parameters to check
     * @param parameterName name of the parameter which should be present
     * @param <V>           the type of values in the multivalued map
     * @return the long value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present with at least one value, or is
     *                                  not a long
     */
    public static <V> long validateOneLongParameter(MultivaluedMap<String, V> parameters,
                                                    String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var value = parameters.getFirst(parameterName);
        assertNotNull(parameterName, value);

        return parseLongOrThrowBadRequest(value, parameterName);
    }

    /**
     * Checks whether {@code parameters} contains a parameter named {@code parameterName} that has exactly one
     * value that can be converted into a long. If there is more than one value, this is considered a bad request
     * and a {@link JaxrsBadRequestException} is thrown.
     *
     * @param parameters    the multivalued parameters to check
     * @param parameterName name of the parameter which should be present
     * @param <V>           the type of values in the multivalued map
     * @return the long value of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present with at least one value, or is
     *                                  not a long
     */
    public static <V> long validateExactlyOneLongParameter(MultivaluedMap<String, V> parameters,
                                                           String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var values = parameters.get(parameterName);
        assertOneElementOrThrowBadRequest(values, parameterName);

        var value = first(values);
        return parseLongOrThrowBadRequest(value, parameterName);
    }

    /**
     * Checks whether {@code parameters} contains a parameter named {@code parameterName} that has at least one
     * value that can be converted into a long. All the values must be convertible to long, and they are
     * all converted and returned in a List.
     *
     * @param parameters    the multivalued parameters to check
     * @param parameterName name of the parameter which should be present
     * @param <V>           the type of values in the multivalued map
     * @return an unmodifiable List containing the long values of the validated parameter
     * @throws JaxrsBadRequestException if the specified parameter is not present with only one value, or is
     *                                  not a long
     */
    public static <V> List<Long> validateOneOrMoreLongParameters(MultivaluedMap<String, V> parameters,
                                                                 String parameterName) {
        checkArgumentNotNull(parameters, PARAMETERS_MUST_NOT_BE_NULL);
        checkArgumentNotBlank(parameterName, PARAMETER_NAME_MUST_NOT_BE_BLANK);

        var values = parameters.get(parameterName);
        assertOneOrMoreElementsOrThrowBadRequest(values, parameterName);

        return values.stream()
                .map(value -> parseLongOrThrowBadRequest(value, parameterName))
                .toList();
    }

    @VisibleForTesting
    static long parseLongOrThrowBadRequest(Object value, String parameterName) {
        var result = Optional.ofNullable(value).map(Object::toString).map(Longs::tryParse);

        return result.orElseThrow(() -> newJaxrsBadRequestException("'{}' is not a long", value, parameterName));
    }

    @VisibleForTesting
    static <V> void assertOneElementOrThrowBadRequest(List<V> values, String parameterName) {
        String message = null;

        if (isNullOrEmpty(values)) {
            message = parameterName + " has no values, but exactly one was expected";
        } else if (values.size() > 1) {
            message = parameterName + " has " + values.size() + " values, but only one was expected";
        }

        if (nonNull(message)) {
            throw new JaxrsBadRequestException(message, parameterName);
        }
    }

    @VisibleForTesting
    static <V> void assertOneOrMoreElementsOrThrowBadRequest(List<V> values, String parameterName) {
        if (isNullOrEmpty(values)) {
            var message = parameterName + " has no values, but expected at least one";
            throw new JaxrsBadRequestException(message, parameterName);
        }
    }

    @VisibleForTesting
    static JaxrsBadRequestException newJaxrsBadRequestException(String messageTemplate,
                                                                Object value,
                                                                String parameterName) {

        var message = f(messageTemplate, value);
        return new JaxrsBadRequestException(message, parameterName);
    }
}
