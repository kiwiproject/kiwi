package org.kiwiproject.jaxrs.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Map {@link IllegalStateException} to {@link Response}.
 * <p>
 * The mapped response has status code 500 (Internal Server Error) and media type JSON.
 */
@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {

    /**
     * Convert the given {@link IllegalStateException} to a 500 Internal Server Error response containing a JSON entity.
     *
     * @param exception the exception to convert
     * @return a response
     * @see JaxrsExceptionMapper#buildResponse(JaxrsException)
     */
    @Override
    public Response toResponse(IllegalStateException exception) {
        return JaxrsExceptionMapper.buildResponse(new JaxrsException(exception, 500));
    }
}
