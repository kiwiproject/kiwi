package org.kiwiproject.jaxrs.exception;

import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseEntityHasOneErrorMessage;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.util.NoSuchElementException;

@DisplayName("NoSuchElementExceptionMapper")
class NoSuchElementExceptionMapperTest {

    private static final int NOT_FOUND_STATUS = 404;

    private NoSuchElementExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NoSuchElementExceptionMapper();
    }

    @Test
    void shouldConvertToResponse() {
        var ex = new NoSuchElementException();
        var response = mapper.toResponse(ex);

        assertResponseStatusCode(response, NOT_FOUND_STATUS);
        assertResponseType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, NOT_FOUND_STATUS, "Requested element does not exist");
    }

    @Test
    void shouldUseExceptionMessageWhenPresent() {
        var message = "No foo exists";
        var ex = new NoSuchElementException(message);
        var response = mapper.toResponse(ex);

        assertResponseStatusCode(response, NOT_FOUND_STATUS);
        assertResponseType(response, MediaType.APPLICATION_JSON);
        assertResponseEntityHasOneErrorMessage(response, NOT_FOUND_STATUS, message);
    }
}
