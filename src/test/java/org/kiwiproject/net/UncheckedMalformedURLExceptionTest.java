package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

@DisplayName("UncheckedMalformedURLException")
class UncheckedMalformedURLExceptionTest {

    @Test
    void testConstructWithMessage() {
        var cause = newMalformedURLException();
        var exception = new UncheckedMalformedURLException("nope", cause);

        assertThat(exception.getMessage()).isEqualTo("nope");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void testConstructWithoutMessage() {
        var cause = newMalformedURLException();
        var exception = new UncheckedMalformedURLException(cause);

        assertThat(exception.getMessage()).contains(cause.getMessage());
        assertThat(exception.getCause()).isSameAs(cause);
    }

    private static MalformedURLException newMalformedURLException() {
        return new MalformedURLException("bad URL");
    }
}
