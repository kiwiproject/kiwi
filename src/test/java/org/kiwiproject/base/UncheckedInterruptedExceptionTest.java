package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UncheckedInterruptedException")
class UncheckedInterruptedExceptionTest {

    private InterruptedException cause;

    @BeforeEach
    void setUp() {
        cause = new InterruptedException("Interrupted!");
    }

    @SuppressWarnings("ThrowableNotThrown")
    @Test
    void testConstructWithMessageButNoCause_ThrowsNPE() {
        assertThatThrownBy(() -> new UncheckedInterruptedException("message", null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("cause cannot be null");
    }

    @SuppressWarnings("ThrowableNotThrown")
    @Test
    void testConstructWithNoCause_ThrowsNPE() {
        assertThatThrownBy(() -> new UncheckedInterruptedException(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("cause cannot be null");
    }

    @Test
    void testConstruct_WithMessageAndCause() {
        String message = "Interrupted while resting...";
        UncheckedInterruptedException ex = new UncheckedInterruptedException(message, cause);
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void testConstruct_WithOnlyCause() {
        UncheckedInterruptedException ex = new UncheckedInterruptedException(cause);
        assertThat(ex.getMessage()).contains(cause.getMessage());
        assertThat(ex.getCause()).isEqualTo(cause);
    }

}