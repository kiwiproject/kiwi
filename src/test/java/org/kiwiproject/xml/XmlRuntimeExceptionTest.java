package org.kiwiproject.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("XmlRuntimeException")
class XmlRuntimeExceptionTest {

    @Test
    void shouldAcceptMessage() {
        assertThat(new XmlRuntimeException("oops"))
                .hasNoCause()
                .hasMessage("oops");
    }

    @Test
    void shouldAcceptMessageAndCause() {
        var cause = new IOException("oops");
        assertThat(new XmlRuntimeException("bad Xml", cause))
                .hasMessage("bad Xml")
                .cause()
                .isSameAs(cause);
    }

    @Test
    void shouldAcceptCause() {
        var cause = new IOException("the cause");
        assertThat(new XmlRuntimeException(cause))
                .hasMessageContaining("the cause")
                .cause()
                .isSameAs(cause);
    }
}
