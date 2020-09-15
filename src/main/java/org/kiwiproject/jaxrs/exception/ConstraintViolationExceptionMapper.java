package org.kiwiproject.jaxrs.exception;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * Map {@link ConstraintViolationException} to a JSON {@link Response}.
 * <p>
 * The mapped response has status code 422 Unprocessable Entity, which technically falls under the WebDAV
 * extensions. See the Mozilla page on <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/422">422 Unprocessable Entity</a>
 * here. But in reality, many web frameworks use 422 to indicate an input validation failure, which is how we are
 * using it here.
 * <p>
 * It is important to note that the {@link Response.Status} does <em>not</em> contain an enum constant for 422 status,
 * so that {@link Response.Status#fromStatusCode(int)} will return {@code null} when given 422.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    /**
     * We have no way to obtain the item ID from a {@link ConstraintViolationException}. Instead, we will set
     * it to null, which is not lovely but there's not much else we can do.
     */
    private static final String ITEM_ID = null;

    /**
     * Convert the given {@link ConstraintViolationException} to a response containing a JSON entity.
     *
     * @param exception the exception to convert
     * @return a response
     * @see JaxrsExceptionMapper#buildResponse(JaxrsException)
     */
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return buildResponse(exception.getConstraintViolations());
    }

    /**
     * Given a set of constraint violations, build a 422 Unprocessable Entity response with a JSON entity.
     *
     * @param violations the constraint violations to convert
     * @return a JSON response
     */
    public static Response buildResponse(Set<? extends ConstraintViolation<?>> violations) {
        return JaxrsExceptionMapper.buildResponse(new JaxrsValidationException(ITEM_ID, violations));
    }
}
