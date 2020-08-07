package org.kiwiproject.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

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
                .hasCauseReference(cause)
                .hasMessage("oops");
    }

    @Test
    void shouldAcceptMessageAndCause() {
        assertThat(new UncheckedJAXBException("bad XML", cause))
                .hasMessage("bad XML")
                .hasCauseReference(cause);
    }

    @Test
    void shouldAcceptCause() {
        assertThat(new UncheckedJAXBException(cause))
                .hasMessageContaining(CAUSE_MESSAGE)
                .hasCauseReference(cause);
    }
}