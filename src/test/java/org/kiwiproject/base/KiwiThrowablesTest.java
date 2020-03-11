package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.base.KiwiThrowables.EMPTY_THROWABLE_INFO;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.assertj.core.util.Throwables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

@ExtendWith(SoftAssertionsExtension.class)
class KiwiThrowablesTest {

    private static final Throwable NULL_THROWABLE = null;

    private static final String ORIGINAL_ERROR_MESSAGE = "File not writable";
    private static final String WRAPPED_ERROR_MESSAGE = "Wrapping file write I/O error";
    private static final IOException CAUSE = new IOException(ORIGINAL_ERROR_MESSAGE);
    private static final UncheckedIOException ERROR = new UncheckedIOException(WRAPPED_ERROR_MESSAGE, CAUSE);
    private static final RuntimeException WRAPPED_ERROR = new RuntimeException(ERROR);

    @Test
    void testCauseOf_WhenNullThrowable(SoftAssertions softly) {
        softly.assertThatThrownBy(() -> KiwiThrowables.nextCauseOf(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot generate nextCauseOf from a null object");
    }

    @Test
    void testCauseOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.nextCauseOf(ERROR)).containsSame(CAUSE);
    }

    @Test
    void testCauseOfNullable_WhenNullThrowable() {
        assertThat(KiwiThrowables.nextCauseOfNullable(null)).isEmpty();
    }

    @Test
    void testCauseOfNullable_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.nextCauseOfNullable(ERROR)).containsSame(CAUSE);
    }

    @Test
    void testTypeOf_WhenNullThrowable() {
        assertThatThrownBy(() -> KiwiThrowables.typeOf(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot generate typeOf from a null object");
    }

    @Test
    void testTypeOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.typeOf(ERROR)).contains(ERROR.getClass().getName());
    }

    @Test
    void testTypeOfNullable_WhenNullThrowable() {
        assertThat(KiwiThrowables.typeOfNullable(null)).isEmpty();
    }

    @Test
    void testTypeOfNullable_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.typeOfNullable(ERROR)).contains(ERROR.getClass().getName());
    }

    @Test
    void testMessageOf_WhenNullThrowable() {
        assertThatThrownBy(() -> KiwiThrowables.messageOf(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot generate messageOf from a null object");
    }

    @Test
    void testMessageOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.messageOf(ERROR)).contains(WRAPPED_ERROR_MESSAGE);
    }

    @Test
    void testMessageOfNullable_WhenNullThrowable() {
        assertThat(KiwiThrowables.messageOfNullable(null)).isEmpty();
    }

    @Test
    void testMessageOfNullable_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.messageOfNullable(ERROR)).contains(WRAPPED_ERROR_MESSAGE);
    }

    @Test
    void testStackTraceOf_WhenNullThrowable() {
        assertThatThrownBy(() -> KiwiThrowables.stackTraceOf(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot generate stackTraceOf from a null object");
    }

    @Test
    void testStackTraceOf_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.stackTraceOf(ERROR)).contains(Throwables.getStackTrace(ERROR));
    }

    @Test
    void testStackTraceOfNullable_WhenNullThrowable() {
        assertThat(KiwiThrowables.stackTraceOfNullable(null)).isEmpty();
    }

    @Test
    void testStackTraceOfNullable_WhenNonNullThrowable() {
        assertThat(KiwiThrowables.stackTraceOfNullable(ERROR)).contains(Throwables.getStackTrace(ERROR));
    }

    @Test
    void testRootCauseOf_WhenNullThrowable() {
        assertThatThrownBy(() -> KiwiThrowables.rootCauseOf(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot generate rootCauseOf from a null object");
    }

    @Test
    void testRootCauseOf_WhenNonNullThrowable() {
        Optional<Throwable> rootCauseOptional = KiwiThrowables.rootCauseOf(WRAPPED_ERROR);

        assertThat(rootCauseOptional).isNotEmpty();
        rootCauseOptional.ifPresent(KiwiThrowablesTest::assertThrowableMatches);
    }

    @Test
    void testRootCauseOfNullable_WhenNullThrowable() {
        assertThat(KiwiThrowables.rootCauseOfNullable(null)).isEmpty();
    }

    @Test
    void testRootCauseOfNullable_WhenNonNullThrowable() {
        Optional<Throwable> rootCauseOptional = KiwiThrowables.rootCauseOfNullable(WRAPPED_ERROR);

        assertThat(rootCauseOptional).isNotEmpty();
        rootCauseOptional.ifPresent(KiwiThrowablesTest::assertThrowableMatches);
    }

    @Test
    void testEmptyThrowableInfo() {
        assertThat(EMPTY_THROWABLE_INFO.type).isNull();
        assertThat(EMPTY_THROWABLE_INFO.hasMessage()).isFalse();
        assertThat(EMPTY_THROWABLE_INFO.stackTrace).isNull();
        assertThat(EMPTY_THROWABLE_INFO.cause).isNull();
    }

    @Test
    void testThrowableInfo_OfNullThrowable(SoftAssertions softly) {
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
    void testThrowableInfo_OfNonNullThrowable_WithCause(SoftAssertions softly) {
        KiwiThrowables.ThrowableInfo info = KiwiThrowables.ThrowableInfo.of(ERROR);

        softly.assertThat(info.type).isEqualTo(ERROR.getClass().getName());
        softly.assertThat(info.getType()).isPresent().contains(ERROR.getClass().getName());

        softly.assertThat(info.message).isEqualTo(WRAPPED_ERROR_MESSAGE);
        softly.assertThat(info.getMessage()).isPresent().contains(WRAPPED_ERROR_MESSAGE);

        softly.assertThat(info.stackTrace).isEqualTo(Throwables.getStackTrace(ERROR));
        softly.assertThat(info.getStackTrace()).isPresent().contains(Throwables.getStackTrace(ERROR));

        softly.assertThat(info.cause).isEqualTo(CAUSE);
        softly.assertThat(info.getCause()).isPresent().contains(CAUSE);
    }

    @Test
    void testThrowableInfo_OfNonNullThrowable_WithNoCause(SoftAssertions softly) {
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
    void testThrowableInfo_OfNonNullThrowable_WithNoMessageOrCause(SoftAssertions softly) {
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

    @Test
    void testThrowableInfoOfNonNull_WithThrowable() {
        KiwiThrowables.ThrowableInfo throwableInfo = KiwiThrowables.throwableInfoOfNonNull(ERROR);
        assertThrowableInfoMatches(throwableInfo);
    }

    @Test
    void testThrowableInfoOfNonNull_WithNull() {
        assertThatThrownBy(() -> KiwiThrowables.throwableInfoOfNonNull(NULL_THROWABLE))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot generate throwableInfoOf from a null object");
    }

    @Test
    void testThrowableInfoOfNullable_WithThrowable() {
        Optional<KiwiThrowables.ThrowableInfo> throwableInfoOptional = KiwiThrowables.throwableInfoOfNullable(ERROR);

        assertThat(throwableInfoOptional).isNotEmpty();
        throwableInfoOptional.ifPresent(KiwiThrowablesTest::assertThrowableInfoMatches);
    }

    @Test
    void testThrowableInfoOfNullable_WithNull() {
        assertThat(KiwiThrowables.throwableInfoOfNullable(NULL_THROWABLE)).isEmpty();
    }

    private static void assertThrowableInfoMatches(KiwiThrowables.ThrowableInfo throwableInfo) {
        assertThat(throwableInfo.type).isEqualTo("java.io.UncheckedIOException");
        assertThat(throwableInfo.getMessage()).contains(WRAPPED_ERROR_MESSAGE);
        assertThat(throwableInfo.stackTrace).isEqualTo(ExceptionUtils.getStackTrace(ERROR));
    }

    private static void assertThrowableMatches(Throwable throwable) {
        assertThat(throwable.getClass().getName()).isEqualTo("java.io.IOException");
        assertThat(throwable.getMessage()).contains(ORIGINAL_ERROR_MESSAGE);
        assertThat(throwable.getStackTrace()).isEqualTo(CAUSE.getStackTrace());
    }

    @Nested
    class Unwrap {

        @Nested
        class ShouldThrowIllegalArgumentException {

            @Test
            void whenNullThrowable() {
                assertThatIllegalArgumentException().isThrownBy(() -> KiwiThrowables.unwrap(null, IOException.class));
            }

            @Test
            void whenNullWrapperClass() {
                assertThatIllegalArgumentException().isThrownBy(() -> KiwiThrowables.unwrap(new IOException(), null));
            }
        }

        @Test
        void shouldReturnOriginalThrowableWhenTypeDoesNotMatchWrapperClass() {
            UncheckedIOException original = new UncheckedIOException(new IOException());
            Throwable unwrapped = KiwiThrowables.unwrap(original, IOException.class);
            assertThat(unwrapped).isSameAs(original);
        }

        @Test
        void shouldReturnCauseOfThrowableWhenTypeMatchesWrapperClass() {
            FileNotFoundException cause = new FileNotFoundException("/foo.txt not found");
            IOException original = new IOException(cause);
            Throwable unwrapped = KiwiThrowables.unwrap(original, IOException.class);
            assertThat(unwrapped).isSameAs(cause);
        }
    }

}
