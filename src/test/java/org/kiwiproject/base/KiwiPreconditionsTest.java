package org.kiwiproject.base;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.kiwiproject.base.KiwiPreconditions.MAX_PORT_NUMBER;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNullElse;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNullElseGet;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.util.BlankStringArgumentsProvider;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

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

        //noinspection DataFlowIssue
        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeCheckedException.class)))
                .isExactlyInstanceOf(SomeCheckedException.class)
                .hasMessage(null);

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeRuntimeException.class)))
                .isNull();

        //noinspection DataFlowIssue
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

        //noinspection DataFlowIssue
        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeCheckedException.class, message)))
                .isExactlyInstanceOf(SomeCheckedException.class)
                .hasMessage(message);

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeRuntimeException.class, message)))
                .isNull();

        //noinspection DataFlowIssue
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

        //noinspection DataFlowIssue
        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(false, SomeCheckedException.class, template, args)))
                .isExactlyInstanceOf(SomeCheckedException.class)
                .hasMessage("something went wrong");

        softly.assertThat(catchThrowable(() ->
                KiwiPreconditions.checkArgument(true, SomeRuntimeException.class, template, args)))
                .isNull();

        //noinspection DataFlowIssue
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

    @Nested
    class CheckArgumentIsNull {

        @Nested
        class WithNoMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsNull() {
                assertThatCode(() -> KiwiPreconditions.checkArgumentIsNull(null))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotNull() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentIsNull(new Object()));
            }
        }

        @Nested
        class WithMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsNull() {
                assertThatCode(() -> KiwiPreconditions.checkArgumentIsNull(null, "the argument cannot be null"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotNull() {
                var errorMessage = "the argument cannot be null";

                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentIsNull("foo", errorMessage))
                        .withMessage(errorMessage);
            }
        }

        @Nested
        class WithTemplateMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsNull() {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentIsNull(null, "{} cannot be null", "foo"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotNull() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiPreconditions.checkArgumentIsNull(BigInteger.ONE, "{} cannot be null (code: {})", "bar", 84))
                        .withMessage("bar cannot be null (code: 84)");
            }
        }
    }

    @Nested
    class CheckArgumentIsBlank {

        @Nested
        class WithNoMessage {

            @ParameterizedTest
            @ArgumentsSource(BlankStringArgumentsProvider.class)
            void shouldNotThrow_WhenArgumentIsBlank(String string) {
                assertThatCode(() -> KiwiPreconditions.checkArgumentIsBlank(string))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotBlank() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentIsBlank("hello, world"));
            }
        }

        @Nested
        class WithMessage {

            @ParameterizedTest
            @ArgumentsSource(BlankStringArgumentsProvider.class)
            void shouldNotThrow_WhenArgumentIsBlank(String string) {
                assertThatCode(() -> KiwiPreconditions.checkArgumentIsBlank(string, "the argument cannot be blank"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotBlank() {
                var errorMessage = "the argument cannot be blank";

                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentIsBlank("bar", errorMessage))
                        .withMessage(errorMessage);
            }
        }

        @Nested
        class WithTemplateMessage {

            @ParameterizedTest
            @ArgumentsSource(BlankStringArgumentsProvider.class)
            void shouldNotThrow_WhenArgumentIsBlank(String string) {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentIsBlank(string, "{} cannot be blank", "foo"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotBlank() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiPreconditions.checkArgumentIsBlank("a non-blank value", "{} cannot be blank (code: {})", "bar", 84))
                        .withMessage("bar cannot be blank (code: 84)");
            }
        }
    }

    @Nested
    class CheckCollectionArgumentNotEmpty {

        @Nested
        class WithNoMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsNotEmpty() {
                assertThatCode(() -> KiwiPreconditions.checkArgumentNotEmpty(List.of(1)))
                        .doesNotThrowAnyException();

                assertThatCode(() -> KiwiPreconditions.checkArgumentNotEmpty(Set.of(42)))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenListArgument_IsNullOrEmpty(List<Integer> list) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentNotEmpty(list));
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenSetArgument_IsNullOrEmpty(Set<Integer> set) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentNotEmpty(set));
            }
        }

        @Nested
        class WithMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsNotEmpty() {
                assertThatCode(() -> KiwiPreconditions.checkArgumentNotEmpty(List.of(1), "invalid list"))
                        .doesNotThrowAnyException();

                assertThatCode(() -> KiwiPreconditions.checkArgumentNotEmpty(Set.of(42), "invalid set"))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenListArgument_IsNullOrEmpty(List<Long> list) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentNotEmpty(list, "invalid list"))
                        .withMessage("invalid list");
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenSetArgument_IsNullOrEmpty(Set<Long> set) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentNotEmpty(set, "invalid set"))
                        .withMessage("invalid set");
            }
        }

        @Nested
        class WithMessageTemplate {

            @Test
            void shouldNotThrow_WhenArgumentIsNotEmpty() {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentNotEmpty(List.of(1), "bad {} map", "foo"))
                        .doesNotThrowAnyException();

                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentNotEmpty(Set.of(42), "bad {} set", "bar"))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenListArgument_IsNullOrEmpty(List<String> list) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiPreconditions.checkArgumentNotEmpty(list, "bad {} list", "bar"))
                        .withMessage("bad bar list");
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenSetArgument_IsNullOrEmpty(Set<String> set) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiPreconditions.checkArgumentNotEmpty(set, "bad {} set", "foo"))
                        .withMessage("bad foo set");
            }
        }
    }

    @Nested
    class CheckMapArgumentNotEmpty {

        @Nested
        class WithNoMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsNotEmpty() {
                assertThatCode(() -> KiwiPreconditions.checkArgumentNotEmpty(Map.of("k1", "v1")))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenArgument_IsNullOrEmpty(Map<String, Integer> map) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentNotEmpty(map));
            }
        }

        @Nested
        class WithMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsNotEmpty() {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentNotEmpty(Map.of("k1", "v1"), "invalid map"))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenArgument_IsNullOrEmpty(Map<Integer, String> map) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentNotEmpty(map, "invalid map"));
            }
        }

        @Nested
        class WithMessageTemplate {

            @Test
            void shouldNotThrow_WhenArgumentIsNotEmpty() {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentNotEmpty(Map.of("k1", "v1"), "invalid {} {} map", "foo", "bar"))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldThrowWhenArgument_IsNullOrEmpty(Map<String, Object> map) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                KiwiPreconditions.checkArgumentNotEmpty(map, "invalid {} {} map", "foo", "bar"))
                        .withMessage("invalid foo bar map");
            }
        }
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

    @Nested
    class CheckArgumentInstanceOf {

        @Nested
        class WithNoMessage {

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#instanceOfArguments")
            void shouldNotThrow_WhenArgumentIsExpectedType(Object argument, Class<?> expectedType) {
                assertThatCode(() -> KiwiPreconditions.checkArgumentInstanceOf(argument, expectedType))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#notInstanceOfArguments")
            void shouldThrow_WhenArgumentIsNotExpectedType(Object argument, Class<?> expectedType) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentInstanceOf(argument, expectedType));
            }
        }

        @Nested
        class WithMessage {

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#instanceOfArguments")
            void shouldNotThrow_WhenArgumentIsExpectedType(Object argument, Class<?> expectedType) {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentInstanceOf(argument, expectedType, "not expected type"))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#notInstanceOfArguments")
            void shouldThrow_WhenArgumentIsNotExpectedType(Object argument, Class<?> expectedType) {
                assertThatIllegalArgumentException().isThrownBy(() ->
                                KiwiPreconditions.checkArgumentInstanceOf(argument, expectedType, "not expected type"))
                        .withMessage("not expected type");
            }
        }

        @Nested
        class WithMessageTemplateAndArguments {
            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#instanceOfArguments")
            void shouldNotThrow_WhenArgumentIsExpectedType(Object argument, Class<?> expectedType) {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentInstanceOf(argument, expectedType, "arg not expected type {}", expectedType))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#notInstanceOfArguments")
            void shouldThrow_WhenArgumentIsNotExpectedType(Object argument, Class<?> expectedType) {
                assertThatIllegalArgumentException().isThrownBy(() ->
                                KiwiPreconditions.checkArgumentInstanceOf(argument, expectedType, "arg not expected {}", expectedType))
                        .withMessage("arg not expected %s", expectedType);
            }
        }
    }

    @Nested
    class CheckArgumentNotInstanceOf {

        @Nested
        class WithNoMessage {

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#notInstanceOfArguments")
            void shouldNotThrow_WhenIsNotInstanceOfRestrictedType(Object argument, Class<?> restrictedType) {
                assertThatCode(() -> KiwiPreconditions.checkArgumentNotInstanceOf(argument, restrictedType))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#instanceOfArguments")
            void shouldThrow_WhenArgumentIsInstanceOfRestrictedType(Object argument, Class<?> restrictedType) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiPreconditions.checkArgumentNotInstanceOf(argument, restrictedType));
            }
        }

        @Nested
        class WithMessage {

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#notInstanceOfArguments")
            void shouldNotThrow_WhenIsNotInstanceOfRestrictedType(Object argument, Class<?> restrictedType) {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentNotInstanceOf(argument, restrictedType, "has restricted type"))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#instanceOfArguments")
            void shouldThrow_WhenArgumentIsInstanceOfRestrictedType(Object argument, Class<?> restrictedType) {
                assertThatIllegalArgumentException().isThrownBy(() ->
                                KiwiPreconditions.checkArgumentNotInstanceOf(argument, restrictedType, "has restricted type"))
                        .withMessage("has restricted type");
            }
        }

        @Nested
        class WithMessageTemplateAndArguments {

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#notInstanceOfArguments")
            void shouldNotThrow_WhenIsNotInstanceOfRestrictedType(Object argument, Class<?> restrictedType) {
                assertThatCode(() ->
                        KiwiPreconditions.checkArgumentNotInstanceOf(argument, restrictedType, "has restricted type {}", restrictedType))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @MethodSource("org.kiwiproject.base.KiwiPreconditionsTest#instanceOfArguments")
            void shouldThrow_WhenArgumentIsInstanceOfRestrictedType(Object argument, Class<?> restrictedType) {
                assertThatIllegalArgumentException().isThrownBy(() ->
                                KiwiPreconditions.checkArgumentNotInstanceOf(argument, restrictedType, "has restricted type {}", restrictedType))
                        .withMessage("has restricted type %s", restrictedType);
            }
        }
    }

    static Stream<Arguments> instanceOfArguments() {
        return Stream.of(
                Arguments.of(RandomStringUtils.random(5), Object.class),
                Arguments.of(RandomStringUtils.random(15), String.class),
                Arguments.of(RandomStringUtils.random(10), CharSequence.class),
                Arguments.of(random().nextLong(), Long.class),
                Arguments.of(random().nextInt(), Integer.class),
                Arguments.of(random().nextInt(), Number.class),
                Arguments.of(new java.sql.Timestamp(System.currentTimeMillis()), Date.class),
                Arguments.of(Instant.now(), Instant.class),
                Arguments.of(LocalDateTime.now(), LocalDateTime.class)
        );
    }

    static Stream<Arguments> notInstanceOfArguments() {
        return Stream.of(
                Arguments.of(RandomStringUtils.random(15), Long.class),
                Arguments.of(random().nextLong(), String.class),
                Arguments.of(random().nextInt(), Collection.class),
                Arguments.of(Instant.now(), Date.class),
                Arguments.of(LocalDateTime.now(), ZonedDateTime.class)
        );
    }

    private static ThreadLocalRandom random() {
        return ThreadLocalRandom.current();
    }
}
