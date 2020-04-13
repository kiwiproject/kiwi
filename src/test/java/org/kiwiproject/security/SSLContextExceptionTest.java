package org.kiwiproject.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.UnrecoverableKeyException;

@DisplayName("SSLContextException")
class SSLContextExceptionTest {

    @Nested
    class ShouldConstruct {

        @Test
        void whenNoArguments() {
            var ex = new SSLContextException();

            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        void whenMessageArgument() {
            var message = "keystore password is bogus";
            var ex = new SSLContextException(message);

            assertThat(ex.getMessage()).isEqualTo(message);
            assertThat(ex.getCause()).isNull();
        }

        @Test
        void whenMessageAndThrowable() {
            var message = "keystore password cannot be null";
            var cause = new UnrecoverableKeyException("blah blah");
            var ex = new SSLContextException(message, cause);

            assertThat(ex.getMessage()).isEqualTo(message);
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        void whenCauseArgument() {
            var cause = new UnrecoverableKeyException("blah blah");
            var ex = new SSLContextException(cause);

            assertThat(ex.getMessage()).contains("blah blah");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

}