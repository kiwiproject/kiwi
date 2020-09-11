package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("JaxrsConflictException")
class JaxrsConflictExceptionTest {

    private static final String MESSAGE = "I feel conflicted";
    private static final String FIELD_NAME = "someField";
    private static final String ITEM_ID = "4200";
    private static final Throwable CAUSE = new IllegalStateException("Conflict");

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldConstructWithCorrectStatusCode(JaxrsConflictException ex) {
        assertThat(ex.getStatusCode()).isEqualTo(409);
        assertThat(ex.getErrors()).hasSize(1);
    }

    private static Stream<JaxrsConflictException> exceptions() {
        return Stream.of(
                new JaxrsConflictException(CAUSE),
                new JaxrsConflictException(MESSAGE),
                new JaxrsConflictException(MESSAGE, CAUSE),
                new JaxrsConflictException(MESSAGE, FIELD_NAME),
                new JaxrsConflictException(MESSAGE, FIELD_NAME, ITEM_ID),
                new JaxrsConflictException(MESSAGE, FIELD_NAME, ITEM_ID, CAUSE)
        );
    }
}
