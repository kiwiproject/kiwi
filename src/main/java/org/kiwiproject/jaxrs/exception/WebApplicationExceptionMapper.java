package org.kiwiproject.jaxrs.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Map {@link IllegalStateException} to {@link Response}.
 * <p>
 * The mapped response has status code 500 (Bad Request) and media type JSON.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    /**
     * Convert the given {@link WebApplicationException} to a response containing a JSON entity.
     * The response status code matches the status of the {@link Response} referenced by the
     * {@link WebApplicationException}.
     *
     * @param exception the exception to convert
     * @return a response
     * @see JaxrsExceptionMapper#buildResponse(JaxrsException)
     */
    @Override
    public Response toResponse(WebApplicationException exception) {
        var message = exception.getMessage();
        var statusCode = exception.getResponse().getStatus();
        var jaxrsException = new JaxrsException(message, statusCode);

        return JaxrsExceptionMapper.buildResponse(jaxrsException);
    }
}
