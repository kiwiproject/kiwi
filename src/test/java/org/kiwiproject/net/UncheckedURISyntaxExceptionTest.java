package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

@DisplayName("UncheckedURISyntaxException")
class UncheckedURISyntaxExceptionTest {

    @Test
    void testConstructWithMessage() {
        var cause = newURISyntaxException();
        var exception = new UncheckedURISyntaxException("oops", cause);


        assertThat(exception.getMessage()).isEqualTo("oops");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void testConstructWithoutMessage() {
        var cause = newURISyntaxException();
        var exception = new UncheckedURISyntaxException(cause);

        assertThat(exception.getMessage()).contains(cause.getMessage());
        assertThat(exception.getCause()).isSameAs(cause);
    }

    private static URISyntaxException newURISyntaxException() {
        return new URISyntaxException("foo\\bar\\baz", "foo\\bar\\baz could not be parsed as a URI");
    }
}
