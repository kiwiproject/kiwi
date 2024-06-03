package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("JaxrsInternalServerErrorException")
public class JaxrsInternalServerErrorExceptionTest {

    private static final String MESSAGE = "Aw snap!";
    private static final String FIELD_NAME = "someField";
    private static final String ITEM_ID = "420";
    private static final Throwable CAUSE = new IllegalStateException("An aw snap happened on the server!");

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldConstructWithCorrectStatusCode(JaxrsInternalServerErrorException ex) {
        assertThat(ex.getStatusCode()).isEqualTo(500);
        assertThat(ex.getErrors()).hasSize(1);
    }

    private static Stream<JaxrsInternalServerErrorException> exceptions() {
        return Stream.of(
                new JaxrsInternalServerErrorException(CAUSE),
                new JaxrsInternalServerErrorException(MESSAGE),
                new JaxrsInternalServerErrorException(MESSAGE, CAUSE),
                new JaxrsInternalServerErrorException(MESSAGE, FIELD_NAME),
                new JaxrsInternalServerErrorException(MESSAGE, FIELD_NAME, ITEM_ID),
                new JaxrsInternalServerErrorException(MESSAGE, FIELD_NAME, ITEM_ID, CAUSE)
        );
    }
}
