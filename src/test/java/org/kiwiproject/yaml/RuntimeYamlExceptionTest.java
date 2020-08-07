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
        assertThat(new RuntimeYamlException("bad YAML", new IOException("oops")))
                .hasMessage("bad YAML")
                .hasCauseExactlyInstanceOf(IOException.class)
                .hasRootCauseMessage("oops");
    }

    @Test
    void shouldAcceptCause() {
        assertThat(new RuntimeYamlException(new IOException("the cause")))
                .hasMessageContaining("the cause")
                .hasCauseExactlyInstanceOf(IOException.class)
                .hasRootCauseMessage("the cause");
    }
}