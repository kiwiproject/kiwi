package org.kiwiproject.jaxrs.exception;

import static java.util.Objects.isNull;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.util.NoSuchElementException;

/**
 * Map {@link NoSuchElementException} to {@link Response}.
 * <p>
 * The mapped response has status code 404 (Bad Request) and media type JSON.
 */
public class NoSuchElementExceptionMapper implements ExceptionMapper<NoSuchElementException> {

    private static final String DEFAULT_MESSAGE = "Requested element does not exist";

    /**
     * Convert the given {@link NoSuchElementException} to a 404 Not Found response containing a JSON entity.
     *
     * @param exception the exception to convert
     * @return a response
     * @see JaxrsExceptionMapper#buildResponse(JaxrsException)
     */
    @Override
    public Response toResponse(NoSuchElementException exception) {
        var message = isNull(exception.getMessage()) ? DEFAULT_MESSAGE : exception.getMessage();
        return JaxrsExceptionMapper.buildResponse(new JaxrsNotFoundException(message, exception));
    }
}
