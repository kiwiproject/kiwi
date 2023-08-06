package org.kiwiproject.jaxrs.exception;

import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseEntityHasOneErrorMessage;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WebApplicationExceptionMapper")
class WebApplicationExceptionMapperTest {

    private WebApplicationExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebApplicationExceptionMapper();
    }

    @Test
    void shouldConvertToResponse() {
        var exceptionResponse = mock(Response.class);
        var statusCode = 400;
        when(exceptionResponse.getStatus()).thenReturn(statusCode);
        when(exceptionResponse.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);

        var ex = new WebApplicationException(exceptionResponse);
        var response = mapper.toResponse(ex);

        assertResponseStatusCode(response, statusCode);
        assertResponseType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, statusCode, ex.getMessage());
    }

    @Test
    void shouldUseExceptionMessageWhenPresent() {
        var exceptionResponse = mock(Response.class);
        var statusCode = 400;
        when(exceptionResponse.getStatus()).thenReturn(statusCode);
        when(exceptionResponse.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);

        var message = "Custom error message";
        var ex = new WebApplicationException(message, exceptionResponse);
        var response = mapper.toResponse(ex);

        assertResponseStatusCode(response, statusCode);
        assertResponseType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, statusCode, message);
    }
}
