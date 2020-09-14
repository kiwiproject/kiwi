package org.kiwiproject.jaxrs.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Map {@link IllegalStateException} to {@link Response}.
 * <p>
 * The mapped response has status code 500 (Bad Request) and media type JSON.
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
