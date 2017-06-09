package org.kiwiproject.base;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Sets.newLinkedHashSet;

public class KiwiPreconditionsTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testCheckEvenItemCount_WithSupplier() {
        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkEvenItemCount(() -> 0)))
                .isNull();

        softly.assertThatThrownBy(() -> KiwiPreconditions.checkEvenItemCount(() -> 1))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must be an even number of items (received 1)");

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkEvenItemCount(() -> 2)))
                .isNull();

        softly.assertThatThrownBy(() -> KiwiPreconditions.checkEvenItemCount(() -> 7))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must be an even number of items (received 7)");

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkEvenItemCount(() -> 42)))
                .isNull();
    }

    @Test
    public void testCheckEvenItemCount_WithVarArgs() {
        softly.assertThat(catchThrowable(KiwiPreconditions::checkEvenItemCount)).isNull();

        softly.assertThatThrownBy(() -> KiwiPreconditions.checkEvenItemCount("one"))
                .isExactlyInstanceOf(IllegalArgumentException.class);

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkEvenItemCount("one", 1)))
                .isNull();

        softly.assertThatThrownBy(() -> KiwiPreconditions.checkEvenItemCount("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class);

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkEvenItemCount("a", 1, "b", 2, "c", 3)))
                .isNull();
    }

    @Test
    public void testCheckEvenItemCount_WithCollection() {
        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkEvenItemCount(new ArrayList<>())))
                .isNull();

        softly.assertThatThrownBy(() -> KiwiPreconditions.checkEvenItemCount(newArrayList("one")))
                .isExactlyInstanceOf(IllegalArgumentException.class);

        softly.assertThat(catchThrowable(() -> KiwiPreconditions.checkEvenItemCount(newArrayList("one", 1))))
                .isNull();

        softly.assertThatThrownBy(() -> KiwiPreconditions.checkEvenItemCount(newArrayList("one", 1, "two")))
                .isExactlyInstanceOf(IllegalArgumentException.class);

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkEvenItemCount(newLinkedHashSet("a", 1, "b", 2, "c", 3))))
                .isNull();
    }

    @Test
    public void testCheckArgument_WhenNoErrorMessage() {
        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeCheckedException.class)))
                .isNull();

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeCheckedException.class)))
                .isExactlyInstanceOf(SomeCheckedException.class)
                .hasMessage(null);

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeRuntimeException.class)))
                .isNull();

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeRuntimeException.class)))
                .isExactlyInstanceOf(SomeRuntimeException.class)
                .hasMessage(null);
    }

    @Test
    public void testCheckArgument_WhenHasMessageConstant() {
        String message = "something went wrong";

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeCheckedException.class, message)))
                .isNull();

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeCheckedException.class, message)))
                .isExactlyInstanceOf(SomeCheckedException.class)
                .hasMessage(message);

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeRuntimeException.class, message)))
                .isNull();

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeRuntimeException.class, message)))
                .isExactlyInstanceOf(SomeRuntimeException.class)
                .hasMessage(message);
    }

    @Test
    public void testCheckArgument_WhenHasMessageTemplateWithArgs() {
        String template = "%s went %s";
        Object[] args = {"something", "wrong"};

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeCheckedException.class, template, args)))
                .isNull();

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeCheckedException.class, template, args)))
                .isExactlyInstanceOf(SomeCheckedException.class)
                .hasMessage("something went wrong");

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeRuntimeException.class, template, args)))
                .isNull();

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeRuntimeException.class, template, args)))
                .isExactlyInstanceOf(SomeRuntimeException.class)
                .hasMessage("something went wrong");
    }

    @SuppressWarnings("unused")
    static class SomeCheckedException extends Exception {

        public SomeCheckedException() {
            super();
        }

        public SomeCheckedException(String message) {
            super(message);
        }

        public SomeCheckedException(String message, Throwable cause) {
            super(message, cause);
        }

        public SomeCheckedException(Throwable cause) {
            super(cause);
        }
    }

    @SuppressWarnings("unused")
    static class SomeRuntimeException extends RuntimeException {

        public SomeRuntimeException() {
            super();
        }

        public SomeRuntimeException(String message) {
            super(message);
        }

        public SomeRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        public SomeRuntimeException(Throwable cause) {
            super(cause);
        }
    }


}