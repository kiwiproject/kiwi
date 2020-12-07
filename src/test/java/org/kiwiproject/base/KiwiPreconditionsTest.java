package org.kiwiproject.base;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.kiwiproject.base.KiwiPreconditions.MAX_PORT_NUMBER;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNullElse;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNullElseGet;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.util.BlankStringArgumentsProvider;

import java.util.ArrayList;

@DisplayName("KiwiPreconditions")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiPreconditionsTest {

    @Test
    void testCheckEvenItemCount_WithSupplier(SoftAssertions softly) {
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
    void testCheckEvenItemCount_WithVarArgs(SoftAssertions softly) {
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
    void testCheckEvenItemCount_WithCollection(SoftAssertions softly) {
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
    void testCheckArgument_WhenNoErrorMessage(SoftAssertions softly) {
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
    void testCheckArgument_WhenHasMessageConstant(SoftAssertions softly) {
        var message = "something went wrong";

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
    void testCheckArgument_WhenHasMessageTemplateWithArgs(SoftAssertions softly) {
        var template = "%s went %s";
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

    @Test
    void testCheckArgumentNotNull_NoMessage(SoftAssertions softly) {
        softly.assertThatThrownBy(() -> checkArgumentNotNull(null))
                .isExactlyInstanceOf(IllegalArgumentException.class);

        softly.assertThat(catchThrowable(() -> checkArgumentNotNull(new Object()))).isNull();
    }

    @Test
    void testCheckArgumentNotNull_StaticMessage(SoftAssertions softly) {
        var errorMessage = "the argument cannot be null";

        softly.assertThatThrownBy(() -> checkArgumentNotNull(null, errorMessage))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);

        softly.assertThat(catchThrowable(() -> checkArgumentNotNull(new Object(), errorMessage))).isNull();
    }

    @Test
    void testCheckArgumentNotNull_MessageWithTemplate(SoftAssertions softly) {
        var errorMessageTemplate = "{} cannot be null (code: {})";
        Object[] errorMessageArgs = { "foo", 42};

        softly.assertThatThrownBy(() -> checkArgumentNotNull(null, errorMessageTemplate, errorMessageArgs))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("foo cannot be null (code: 42)");

        softly.assertThat(catchThrowable(() -> checkArgumentNotNull(new Object(), errorMessageTemplate, errorMessageArgs))).isNull();
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

    @Test
    void testRequireNotNull_NoMessage(SoftAssertions softly) {
        softly.assertThatThrownBy(() -> requireNotNull(null))
                .isExactlyInstanceOf(IllegalArgumentException.class);

        softly.assertThat(catchThrowable(() -> requireNotNull(new Object()))).isNull();
    }

    @Test
    void testRequireNotNull_StaticMessage(SoftAssertions softly) {
        var errorMessage = "foo cannot be null";

        softly.assertThatThrownBy(() -> requireNotNull(null, errorMessage))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);

        softly.assertThat(catchThrowable(() -> requireNotNull(new Object(), errorMessage))).isNull();
    }

    @Test
    void testRequireNotNull_MessageWithTemplate(SoftAssertions softly) {
        var errorMessageTemplate = "{} cannot be null (code: {})";
        Object[] args = { "foo", 42 };

        softly.assertThatThrownBy(() -> requireNotNull(null, errorMessageTemplate, args))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("foo cannot be null (code: 42)");

        softly.assertThat(catchThrowable(() -> requireNotNull(new Object(), errorMessageTemplate, args))).isNull();
    }

    @Test
    void testRequireNotNullElse(SoftAssertions softly) {
        softly.assertThat(requireNotNullElse(null, "default value")).isEqualTo("default value");
        softly.assertThat(requireNotNullElse("a value", "default value")).isEqualTo("a value");
        softly.assertThatThrownBy(() -> requireNotNullElse(null, null))
                .isExactlyInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void testRequireNotNullElseGet(SoftAssertions softly) {
        softly.assertThat(requireNotNullElseGet(null, () -> "default value")).isEqualTo("default value");
        softly.assertThat(requireNotNullElseGet("a value", () -> "default value")).isEqualTo("a value");
        softly.assertThatThrownBy(() -> requireNotNullElseGet(null, () -> null))
                .isExactlyInstanceOf(IllegalArgumentException.class);

    }

    @ParameterizedTest
    @ArgumentsSource(BlankStringArgumentsProvider.class)
    void testRequireNotBlank_NoMessage(String value, SoftAssertions softly) {
        softly.assertThatThrownBy(() -> requireNotBlank(value))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ArgumentsSource(BlankStringArgumentsProvider.class)
    void testRequireNotBlank_StaticMessage(String value, SoftAssertions softly) {
        var errorMessage = "foo cannot be null";

        softly.assertThatThrownBy(() -> requireNotBlank(value, errorMessage))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);

    }

    @ParameterizedTest
    @ArgumentsSource(BlankStringArgumentsProvider.class)
    void testRequireNotBlank_MessageWithTemplate(String value, SoftAssertions softly) {
        var errorMessageTemplate = "{} cannot be null (code: {})";
        Object[] args = { "foo", 42 };

        softly.assertThatThrownBy(() -> requireNotBlank(value, errorMessageTemplate, args))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("foo cannot be null (code: 42)");
    }

    @Test
    void testRequireNotBlank_ReturnsNotBlankValue() {
        var value = "foo";
        assertThat(requireNotBlank(value)).isEqualTo(value);
    }

    @Test
    void testRequireNotBlank_ReturnsNotBlankValue_StaticMessage() {
        var value = "foo";
        assertThat(requireNotBlank(value, "foo cannot be null")).isEqualTo(value);
    }

    @Test
    void testRequireNotBlank_ReturnsNotBlankValue_MessageWithTemplate() {
        var value = "foo";
        assertThat(requireNotBlank(value, "{} cannot be null", "foo")).isEqualTo(value);
    }

    @Nested
    class CheckPositive {

        @Test
        void shouldThrowException_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldThrowException_WhenIntValue_IsZero() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(-1, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(-1, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldNotThrowException_WhenIntValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessage_WhenIntValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1, "custom error message")).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenIntValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1, "custom error message template %s", 42)).doesNotThrowAnyException();
        }

        @Test
        void shouldThrowException_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(-1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(-1L, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(-1L, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldThrowException_WhenLongValue_IsZero() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositive(0L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldNotThrowException_WhenLongValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1L)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessage_WhenLongValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1L, "custom error message")).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenLongValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1L, "custom error message template %s", 42)).doesNotThrowAnyException();
        }
    }

    @Nested
    class CheckPositiveOrZero {

        @Test
        void shouldThrowException_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositiveOrZero(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be positive or zero");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositiveOrZero(-1, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositiveOrZero(-1, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldNotThrowException_WhenIntValue_IsZero() {
            assertThatCode(() -> KiwiPreconditions.checkPositiveOrZero(0)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WhenIntValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositiveOrZero(1)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessage_WhenIntValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositiveOrZero(1, "custom error message")).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenIntValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositiveOrZero(1, "custom error message template %s", 42)).doesNotThrowAnyException();
        }

        @Test
        void shouldThrowException_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositiveOrZero(-1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be positive or zero");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositiveOrZero(-1L, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkPositiveOrZero(-1L, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldThrowException_WhenLongValue_IsZero() {
            assertThatCode(() -> KiwiPreconditions.checkPositiveOrZero(0L)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WhenLongValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositiveOrZero(1L)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessage_WhenLongValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1L, "custom error message")).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenLongValue_IsPositive() {
            assertThatCode(() -> KiwiPreconditions.checkPositive(1L, "custom error message template %s", 42)).doesNotThrowAnyException();
        }
    }

    @Nested
    class RequirePositive {

        @Test
        void shouldThrowException_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(-1, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(-1, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldThrowException_WhenIntValue_IsZero() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldReturnValue_WhenIntValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositive(1)).isEqualTo(1);
        }

        @Test
        void shouldReturnValue_WithCustomMessage_WhenIntValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositive(1, "custom error message")).isEqualTo(1);
        }

        @Test
        void shouldReturnValue_WithCustomMessageTemplate_WhenIntValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositive(1, "custom error message template %s", 42)).isEqualTo(1);
        }

        @Test
        void shouldThrowException_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(-1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(-1L, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(-1L, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldThrowException_WhenLongValue_IsZero() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositive(0L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be a positive number");
        }

        @Test
        void shouldReturnValue_WhenLongValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositive(1L)).isEqualTo(1L);
        }

        @Test
        void shouldReturnValue_WithCustomMessage_WhenLongValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositive(1L, "custom error message")).isEqualTo(1);
        }

        @Test
        void shouldReturnValue_WithCustomMessageTemplate_WhenLongValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositive(1L, "custom error message template %s", 42)).isEqualTo(1);
        }
    }

    @Nested
    class RequirePositiveOrZero {

        @Test
        void shouldThrowException_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositiveOrZero(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be positive or zero");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositiveOrZero(-1, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenIntValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositiveOrZero(-1, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldReturnValue_WhenIntValue_IsZero() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(0)).isZero();
        }

        @Test
        void shouldReturnValue_WhenIntValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(1)).isEqualTo(1);
        }

        @Test
        void shouldReturnValue_WithCustomMessage_WhenIntValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(1, "custom error message")).isEqualTo(1);
        }

        @Test
        void shouldReturnValue_WithCustomMessageTemplate_WhenIntValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(1, "custom error message template %s", 42)).isEqualTo(1);
        }

        @Test
        void shouldThrowException_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositiveOrZero(-1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("value must be positive or zero");
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositiveOrZero(-1L, "custom error message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenLongValue_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requirePositiveOrZero(-1L, "custom error message template %s", "42"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom error message template 42");
        }

        @Test
        void shouldReturnValue_WhenLongValue_IsZero() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(0L)).isZero();
        }

        @Test
        void shouldReturnValue_WhenLongValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(1L)).isEqualTo(1L);
        }

        @Test
        void shouldReturnValue_WithCustomMessage_WhenLongValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(1L, "custom error message")).isEqualTo(1);
        }

        @Test
        void shouldReturnValue_WithCustomMessageTemplate_WhenLongValue_IsPositive() {
            assertThat(KiwiPreconditions.requirePositiveOrZero(1L, "custom error message template %s", 42)).isEqualTo(1);
        }
    }

    @Nested
    class CheckValidPort {

        @Test
        void shouldThrowException_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidPort(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 0 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidPort(-1, "custom message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidPort(-1, "custom message %s", 42))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message 42");
        }

        @Test
        void shouldThrowException_WhenPort_IsTooHigh() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidPort(MAX_PORT_NUMBER + 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 0 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, MAX_PORT_NUMBER })
        void shouldNotThrowException_WhenPort_IsValid(int port) {
            assertThatCode(() -> KiwiPreconditions.checkValidPort(port)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessage_WhenPort_IsValid() {
            assertThatCode(() -> KiwiPreconditions.checkValidPort(0, "custom error message")).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenPort_IsValid() {
            assertThatCode(() -> KiwiPreconditions.checkValidPort(1, "custom error message template %s", 42)).doesNotThrowAnyException();
        }

    }

    @Nested
    class RequireValidPort {

        @Test
        void shouldThrowException_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidPort(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 0 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidPort(-1, "custom message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidPort(-1, "custom message %s", 42))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message 42");
        }

        @Test
        void shouldThrowException_WhenPort_IsTooHigh() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidPort(MAX_PORT_NUMBER + 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 0 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, MAX_PORT_NUMBER })
        void shouldReturnPort_WhenPort_IsValid(int port) {
            assertThat(KiwiPreconditions.requireValidPort(port)).isEqualTo(port);
        }

        @Test
        void shouldReturnPort_WithCustomMessage_WhenPort_IsValid() {
            assertThat(KiwiPreconditions.requireValidPort(0, "custom error message")).isZero();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenPort_IsValid() {
            assertThat(KiwiPreconditions.requireValidPort(0, "custom error message %s", 42)).isZero();
        }

    }

    @Nested
    class CheckValidNonZeroPort {

        @Test
        void shouldThrowException_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidNonZeroPort(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 1 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidNonZeroPort(-1, "custom message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidNonZeroPort(-1, "custom message %s", 42))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message 42");
        }

        @Test
        void shouldThrowException_WhenPort_IsZero() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidNonZeroPort(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 1 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @Test
        void shouldThrowException_WhenPort_IsTooHigh() {
            assertThatThrownBy(() -> KiwiPreconditions.checkValidNonZeroPort(MAX_PORT_NUMBER + 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 1 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, MAX_PORT_NUMBER })
        void shouldNotThrowException_WhenPort_IsValid(int port) {
            assertThatCode(() -> KiwiPreconditions.checkValidNonZeroPort(port)).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessage_WhenPort_IsValid() {
            assertThatCode(() -> KiwiPreconditions.checkValidNonZeroPort(1, "custom error message")).doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenPort_IsValid() {
            assertThatCode(() -> KiwiPreconditions.checkValidNonZeroPort(1, "custom error message template %s", 42)).doesNotThrowAnyException();
        }

    }

    @Nested
    class RequireValidNonZeroPort {

        @Test
        void shouldThrowException_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidNonZeroPort(-1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 1 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @Test
        void shouldThrowException_WithCustomMessage_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidNonZeroPort(-1, "custom message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message");
        }

        @Test
        void shouldThrowException_WithCustomMessageTemplate_WhenPort_IsNegative() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidNonZeroPort(-1, "custom message %s", 42))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("custom message 42");
        }

        @Test
        void shouldThrowException_WhenPort_IsZero() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidNonZeroPort(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 1 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @Test
        void shouldThrowException_WhenPort_IsTooHigh() {
            assertThatThrownBy(() -> KiwiPreconditions.requireValidNonZeroPort(MAX_PORT_NUMBER + 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("port must be between 1 and " + KiwiPreconditions.MAX_PORT_NUMBER);
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, MAX_PORT_NUMBER })
        void shouldReturnPort_WhenPort_IsValid(int port) {
            assertThat(KiwiPreconditions.requireValidNonZeroPort(port)).isEqualTo(port);
        }

        @Test
        void shouldReturnPort_WithCustomMessage_WhenPort_IsValid() {
            assertThat(KiwiPreconditions.requireValidNonZeroPort(1, "custom error message")).isOne();
        }

        @Test
        void shouldNotThrowException_WithCustomMessageTemplate_WhenPort_IsValid() {
            assertThat(KiwiPreconditions.requireValidNonZeroPort(1, "custom error message %s", 42)).isOne();
        }

    }
}
