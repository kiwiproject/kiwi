package org.kiwiproject.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

@DisplayName("UncheckedGeneralSecurityException")
class UncheckedGeneralSecurityExceptionTest {

    @Test
    void shouldConstructWithMessage() {
        var cause = newGeneralSecurityException();
        var exception = new UncheckedGeneralSecurityException("nope", cause);

        assertThat(exception.getMessage()).isEqualTo("nope");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void shouldConstructWithoutMessage() {
        var cause = newGeneralSecurityException();
        var exception = new UncheckedGeneralSecurityException(cause);

        assertThat(exception.getMessage()).contains(cause.getMessage());
        assertThat(exception.getCause()).isSameAs(cause);
    }

    private static GeneralSecurityException newGeneralSecurityException() {
        return new NoSuchAlgorithmException("bad algorithm: FooBar123");
    }
}
