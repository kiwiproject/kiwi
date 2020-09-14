package org.kiwiproject.jaxrs.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Map {@link IllegalArgumentException} to {@link Response}.
 * <p>
 * The mapped response has status code 400 (Bad Request) and media type JSON.
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    /**
     * Convert the given {@link IllegalArgumentException} to a 400 Bad Request response containing a JSON entity.
     *
     * @param exception the exception to convert
     * @return a response
     * @see JaxrsExceptionMapper#buildResponse(JaxrsException)
     */
    @Override
    public Response toResponse(IllegalArgumentException exception) {
        return JaxrsExceptionMapper.buildResponse(new JaxrsBadRequestException(exception));
    }
}
