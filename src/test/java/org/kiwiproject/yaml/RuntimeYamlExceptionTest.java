package org.kiwiproject.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("RuntimeYamlException")
class RuntimeYamlExceptionTest {

    @Test
    void shouldAcceptMessage() {
        assertThat(new RuntimeYamlException("oops"))
                .hasNoCause()
                .hasMessage("oops");
    }

    @Test
    void shouldAcceptMessageAndCause() {
        var cause = new IOException("oops");
        assertThat(new RuntimeYamlException("bad YAML", cause))
                .hasMessage("bad YAML")
                .cause()
                .isSameAs(cause);
    }

    @Test
    void shouldAcceptCause() {
        var cause = new IOException("the cause");
        assertThat(new RuntimeYamlException(cause))
                .hasMessageContaining("the cause")
                .cause()
                .isSameAs(cause);
    }
}
