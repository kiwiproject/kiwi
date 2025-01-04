package org.kiwiproject.xml;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UncheckedJAXBException")
class UncheckedJAXBExceptionTest {

    private static final String CAUSE_MESSAGE = "XML parse error";

    private JAXBException cause;

    @BeforeEach
    void setUp() {
        cause = new JAXBException(CAUSE_MESSAGE);
    }

    @Test
    void shouldAcceptMessage() {
        assertThat(new UncheckedJAXBException("oops", cause))
                .hasMessage("oops")
                .cause()
                .isSameAs(cause);
    }

    @Test
    void shouldAcceptMessageAndCause() {
        assertThat(new UncheckedJAXBException("bad XML", cause))
                .hasMessage("bad XML")
                .cause()
                .isSameAs(cause);
    }

    @Test
    void shouldAcceptCause() {
        assertThat(new UncheckedJAXBException(cause))
                .hasMessageContaining(CAUSE_MESSAGE)
                .cause()
                .isSameAs(cause);
    }
}
