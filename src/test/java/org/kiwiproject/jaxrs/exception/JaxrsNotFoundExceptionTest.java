package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("JaxrsNotFoundException")
class JaxrsNotFoundExceptionTest {

    private static final String MESSAGE = "Not Found";
    private static final String TYPE = "User";
    private static final String ITEM_ID = "84";
    private static final Throwable CAUSE = new IllegalStateException("Not found");

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldConstructWithCorrectStatusCode(JaxrsNotFoundException ex) {
        assertThat(ex.getStatusCode()).isEqualTo(404);
        assertThat(ex.getErrors()).hasSize(1);
    }

    @Test
    void shouldConstructWithMessageIncludingTypeAndItemId() {
        var ex = new JaxrsNotFoundException(TYPE, ITEM_ID);
        assertThat(ex.getErrors()).containsExactly(new ErrorMessage(404, TYPE + " " + ITEM_ID + " was not found."));
    }

    @Test
    void shouldConstructWithMessageIncludingTypeAndObjectItemId() {
        var ex = new JaxrsNotFoundException(TYPE, Integer.valueOf(ITEM_ID));
        assertThat(ex.getErrors()).containsExactly(new ErrorMessage(404, TYPE + " " + ITEM_ID + " was not found."));
    }

    private static Stream<JaxrsNotFoundException> exceptions() {
        return Stream.of(
                new JaxrsNotFoundException(CAUSE),
                new JaxrsNotFoundException(MESSAGE),
                new JaxrsNotFoundException(MESSAGE, CAUSE),
                new JaxrsNotFoundException(TYPE, ITEM_ID),
                new JaxrsNotFoundException(TYPE, (Object) ITEM_ID)
        );
    }
}
