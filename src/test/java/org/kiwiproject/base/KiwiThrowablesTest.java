package org.kiwiproject.base;

import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.core.util.Throwables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;

public class KiwiThrowablesTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private IOException cause;
    private String message;
    private UncheckedIOException error;

    @Before
    public void setUp() throws Exception {
        cause = new IOException("File not readable!");
        message = "Error reading file";
        error = new UncheckedIOException(message, cause);
    }

    @Test
    public void testCauseOf_WhenNullThrowable() {
        assertThat(KiwiThrowables.causeOf(null)).isEmpty();
    }

    @Test
    public void testCauseOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.causeOf(error)).containsSame(cause);
    }

    @Test
    public void testTypeOf_WhenNullThrowable() {
        assertThat(KiwiThrowables.typeOf(null)).isEmpty();
    }

    @Test
    public void testTypeOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.typeOf(error)).contains(error.getClass().getName());
    }

    @Test
    public void testMessageOf_WhenNullThrowable() {
        assertThat(KiwiThrowables.messageOf(null)).isEmpty();
    }

    @Test
    public void testMessageOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.messageOf(error)).contains(message);
    }

    @Test
    public void testStackTraceOf_WhenNullThrowable() {
        assertThat(KiwiThrowables.stackTraceOf(null)).isEmpty();
    }

    @Test
    public void testStackTraceOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.stackTraceOf(error))
                .contains(Throwables.getStackTrace(error));
    }

    @Test
    public void testThrowableInfo_OfNullThrowable() {
        KiwiThrowables.ThrowableInfo info = KiwiThrowables.ThrowableInfo.of(null);

        softly.assertThat(info.type).isNull();
        softly.assertThat(info.getType()).isEmpty();

        softly.assertThat(info.message).isNull();
        softly.assertThat(info.getMessage()).isEmpty();

        softly.assertThat(info.stackTrace).isNull();
        softly.assertThat(info.getStackTrace()).isEmpty();

        softly.assertThat(info.cause).isNull();
        softly.assertThat(info.getCause()).isEmpty();
    }

    @Test
    public void testThrowableInfo_OfNonNullThrowable_WithCause() {
        KiwiThrowables.ThrowableInfo info = KiwiThrowables.ThrowableInfo.of(error);

        softly.assertThat(info.type).isEqualTo(error.getClass().getName());
        softly.assertThat(info.getType()).isPresent().contains(error.getClass().getName());

        softly.assertThat(info.message).isEqualTo(message);
        softly.assertThat(info.getMessage()).isPresent().contains(message);

        softly.assertThat(info.stackTrace).isEqualTo(Throwables.getStackTrace(error));
        softly.assertThat(info.getStackTrace()).isPresent().contains(Throwables.getStackTrace(error));

        softly.assertThat(info.cause).isEqualTo(cause);
        softly.assertThat(info.getCause()).isPresent().contains(cause);
    }

    @Test
    public void testThrowableInfo_OfNonNullThrowable_WithNoCause() {
        IOException ex = new IOException("I/O error");
        KiwiThrowables.ThrowableInfo info = KiwiThrowables.ThrowableInfo.of(ex);

        softly.assertThat(info.type).isEqualTo(ex.getClass().getName());
        softly.assertThat(info.getType()).isPresent().contains(ex.getClass().getName());

        softly.assertThat(info.message).isEqualTo(ex.getMessage());
        softly.assertThat(info.getMessage()).isPresent().contains(ex.getMessage());

        softly.assertThat(info.stackTrace).isEqualTo(Throwables.getStackTrace(ex));
        softly.assertThat(info.getStackTrace()).isPresent().contains(Throwables.getStackTrace(ex));

        softly.assertThat(info.cause).isNull();
        softly.assertThat(info.getCause()).isEmpty();
    }

    @Test
    public void testThrowableInfo_OfNonNullThrowable_WithNoMessageOrCause() {
        IOException ex = new IOException();
        KiwiThrowables.ThrowableInfo info = KiwiThrowables.ThrowableInfo.of(ex);

        softly.assertThat(info.type).isEqualTo(ex.getClass().getName());
        softly.assertThat(info.getType()).isPresent().contains(ex.getClass().getName());

        softly.assertThat(info.message).isNull();
        softly.assertThat(info.getMessage()).isEmpty();

        softly.assertThat(info.stackTrace).isEqualTo(Throwables.getStackTrace(ex));
        softly.assertThat(info.getStackTrace()).isPresent().contains(Throwables.getStackTrace(ex));

        softly.assertThat(info.cause).isNull();
        softly.assertThat(info.getCause()).isEmpty();
    }

}