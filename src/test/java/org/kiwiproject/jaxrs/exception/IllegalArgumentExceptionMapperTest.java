package org.kiwiproject.jaxrs.exception;

import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertResponseEntityHasOneErrorMessage;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertResponseMediaType;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertStatusCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

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

        assertStatusCode(response, BAD_REQUEST_STATUS);
        assertResponseMediaType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, BAD_REQUEST_STATUS, ErrorMessage.DEFAULT_MSG);
    }

    @Test
    void shouldUseExceptionMessageWhenPresent() {
        var message = "The foo is not valid";
        var ex = new IllegalArgumentException(message);
        var response = mapper.toResponse(ex);

        assertStatusCode(response, BAD_REQUEST_STATUS);
        assertResponseMediaType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, BAD_REQUEST_STATUS, message);
    }
}
