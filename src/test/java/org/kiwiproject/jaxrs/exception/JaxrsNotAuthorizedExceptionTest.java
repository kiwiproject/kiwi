package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("JaxrsNotAuthorizedException")
class JaxrsNotAuthorizedExceptionTest {

    private static final String MESSAGE = "Unauthorized";
    private static final String FIELD_NAME = "someField";
    private static final String ITEM_ID = "24";
    private static final Throwable CAUSE = new IllegalStateException("You are not authorized");

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldConstructWithCorrectStatusCode(JaxrsNotAuthorizedException ex) {
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getErrors()).hasSize(1);
    }

    private static Stream<JaxrsNotAuthorizedException> exceptions() {
        return Stream.of(
                new JaxrsNotAuthorizedException(CAUSE),
                new JaxrsNotAuthorizedException(MESSAGE),
                new JaxrsNotAuthorizedException(MESSAGE, CAUSE),
                new JaxrsNotAuthorizedException(MESSAGE, FIELD_NAME),
                new JaxrsNotAuthorizedException(MESSAGE, FIELD_NAME, ITEM_ID),
                new JaxrsNotAuthorizedException(MESSAGE, FIELD_NAME, ITEM_ID, CAUSE)
        );
    }
}
