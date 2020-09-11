package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("JaxrsBadRequestException")
class JaxrsBadRequestExceptionTest {

    private static final String MESSAGE = "Client error";
    private static final String FIELD_NAME = "lastName";
    private static final String ITEM_ID = "42";
    private static final Throwable CAUSE = new IllegalStateException("Bad State");

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldConstructWithCorrectStatusCode(JaxrsBadRequestException ex) {
        assertThat(ex.getStatusCode()).isEqualTo(400);
        assertThat(ex.getErrors()).hasSize(1);
    }

    private static Stream<JaxrsBadRequestException> exceptions() {
        return Stream.of(
                new JaxrsBadRequestException(CAUSE),
                new JaxrsBadRequestException(MESSAGE),
                new JaxrsBadRequestException(MESSAGE, CAUSE),
                new JaxrsBadRequestException(MESSAGE, FIELD_NAME),
                new JaxrsBadRequestException(MESSAGE, FIELD_NAME, ITEM_ID),
                new JaxrsBadRequestException(MESSAGE, FIELD_NAME, ITEM_ID, CAUSE)
        );
    }
}
