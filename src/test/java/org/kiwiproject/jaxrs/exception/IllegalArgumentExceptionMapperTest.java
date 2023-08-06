package org.kiwiproject.jaxrs.exception;

import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseEntityHasOneErrorMessage;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseType;

import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IllegalArgumentExceptionMapper")
class IllegalArgumentExceptionMapperTest {

    private static final int BAD_REQUEST_STATUS = 400;

    private IllegalArgumentExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IllegalArgumentExceptionMapper();
    }

    @Test
    void shouldConvertToResponse() {
        var ex = new IllegalArgumentException();
        var response = mapper.toResponse(ex);

        assertResponseStatusCode(response, BAD_REQUEST_STATUS);
        assertResponseType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, BAD_REQUEST_STATUS, ErrorMessage.DEFAULT_MSG);
    }

    @Test
    void shouldUseExceptionMessageWhenPresent() {
        var message = "The foo is not valid";
        var ex = new IllegalArgumentException(message);
        var response = mapper.toResponse(ex);

        assertResponseStatusCode(response, BAD_REQUEST_STATUS);
        assertResponseType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, BAD_REQUEST_STATUS, message);
    }
}
