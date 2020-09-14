package org.kiwiproject.jaxrs.exception;

import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertResponseEntityHasOneErrorMessage;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertResponseMediaType;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertStatusCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

@DisplayName("IllegalStateExceptionMapper")
class IllegalStateExceptionMapperTest {

    private static final int SERVER_ERROR_STATUS = 500;

    private IllegalStateExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IllegalStateExceptionMapper();
    }

    @Test
    void shouldConvertToResponse() {
        var ex = new IllegalStateException();
        var response = mapper.toResponse(ex);

        assertStatusCode(response, SERVER_ERROR_STATUS);
        assertResponseMediaType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, SERVER_ERROR_STATUS, ErrorMessage.DEFAULT_MSG);
    }

    @Test
    void shouldUseExceptionMessageWhenPresent() {
        var message = "The foo is in a bad state";
        var ex = new IllegalStateException(message);
        var response = mapper.toResponse(ex);

        assertStatusCode(response, SERVER_ERROR_STATUS);
        assertResponseMediaType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, SERVER_ERROR_STATUS, message);
    }
}
