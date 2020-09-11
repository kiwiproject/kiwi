package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("JaxrsForbiddenException")
class JaxrsForbiddenExceptionTest {

    private static final String MESSAGE = "I forbid you!";
    private static final String FIELD_NAME = "someField";
    private static final String ITEM_ID = "24";
    private static final Throwable CAUSE = new IllegalStateException("You ar forbidden to be here");

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldConstructWithCorrectStatusCode(JaxrsForbiddenException ex) {
        assertThat(ex.getStatusCode()).isEqualTo(403);
        assertThat(ex.getErrors()).hasSize(1);
    }

    private static Stream<JaxrsForbiddenException> exceptions() {
        return Stream.of(
                new JaxrsForbiddenException(CAUSE),
                new JaxrsForbiddenException(MESSAGE),
                new JaxrsForbiddenException(MESSAGE, CAUSE),
                new JaxrsForbiddenException(MESSAGE, FIELD_NAME),
                new JaxrsForbiddenException(MESSAGE, FIELD_NAME, ITEM_ID),
                new JaxrsForbiddenException(MESSAGE, FIELD_NAME, ITEM_ID, CAUSE)
        );
    }
}
