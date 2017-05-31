package org.kiwiproject.base;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UncheckedInterruptedExceptionTest {

    private InterruptedException cause = new InterruptedException("Interrupted!");

    @Test
    public void testConstruct_WithMessageAndCause() {
        String message = "Interrupted while resting...";
        UncheckedInterruptedException ex = new UncheckedInterruptedException(message, cause);
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    public void testConstruct_WithOnlyCause() {
        UncheckedInterruptedException ex = new UncheckedInterruptedException(cause);
        assertThat(ex.getMessage()).contains(cause.getMessage());
        assertThat(ex.getCause()).isEqualTo(cause);
    }

}