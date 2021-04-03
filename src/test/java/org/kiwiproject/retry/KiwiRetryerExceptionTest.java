package org.kiwiproject.retry;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.base.KiwiStrings.f;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

@DisplayName("KiwiRetryerException")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiRetryerExceptionTest {

    @SuppressWarnings("ThrowableNotThrown")
    @Test
    void shouldNotAcceptNullCauses() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new KiwiRetryerException("oops", null))
                .withMessage("cause is required");
    }

    @Nested
    class UnwrapKiwiRetryerException {

        @Test
        void shouldReturnRetryExceptions() {
            var originalException = new RuntimeException("failed to get the object");
            var lastFailedAttempt = Attempt.newExceptionAttempt(originalException, 5, 5_000);
            var retryException = new RetryException(lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrap()).containsSame(retryException);
        }

        @Test
        void shouldUnwrapRetryExceptions_HavingNullResult() {
            var lastFailedAttempt = Attempt.newResultAttempt(null, 5, 5_000);
            var retryException = new RetryException(lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrap()).containsSame(retryException);
        }

        @Test
        void shouldReturnInterruptedExceptions() {
            var interruptedException = new InterruptedException("Excuse me!");
            var retryerException = new KiwiRetryerException("interrupt error", interruptedException);

            assertThat(retryerException.unwrap()).containsSame(interruptedException);
        }
    }

    @Nested
    class UnwrapKiwiRetryerExceptionFully {

        @Test
        void shouldUnwrapRetryExceptions() {
            var originalException = new RuntimeException("failed to get the object");
            var lastFailedAttempt = Attempt.newExceptionAttempt(originalException, 5, 2_500);
            var retryException = new RetryException(lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrapFully()).containsSame(originalException);
        }

        @Test
        void shouldUnwrapRetryExceptions_HavingNullResult() {
            var lastFailedAttempt = Attempt.newResultAttempt(null, 3, 3_000);
            var retryException = new RetryException(lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrapFully()).isEmpty();
        }

        @Test
        void shouldUnwrapInterruptedExceptions() {
            var interruptedException = new InterruptedException("Excuse me!");
            var retryerException = new KiwiRetryerException("interrupt error", interruptedException);

            assertThat(retryerException.unwrapFully()).containsSame(interruptedException);
        }
    }

    @ParameterizedTest
    @MethodSource("invalidCauses")
    void shouldNotPermitInvalidCauses(Exception cause) {
        //noinspection ThrowableNotThrown
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new KiwiRetryerException("bad cause", cause))
                .withMessage(expectedMessageFor(cause));
    }

    private static String expectedMessageFor(Exception cause) {
        if (isNull(cause)) {
            return "cause is required";
        }

        return f("cause must be a RetryException or an InterruptedException but was a %s", cause.getClass().getName());
    }

    private static Stream<Exception> invalidCauses() {
        return Stream.of(
                null,
                new RuntimeException("oops"),
                new IOException("I/O problem")
        );
    }

    @ParameterizedTest(name = "[{index}] {0} ; expecting causeIsRetryException: {2} , causeIsInterruptedException: {3}")
    @MethodSource("causeArguments")
    void shouldCheckCauses(String description,
                           KiwiRetryerException retryerException,
                           boolean causeIsRetryException,
                           boolean causeIsInterruptedException,
                           SoftAssertions softly) {

        softly.assertThat(retryerException.isCauseRetryException())
                .describedAs(description)
                .isEqualTo(causeIsRetryException);

        softly.assertThat(retryerException.isCauseInterruptedException())
                .describedAs(description)
                .isEqualTo(causeIsInterruptedException);
    }

    private static Stream<Arguments> causeArguments() {
        return Stream.of(

                Arguments.of(
                        "RetryException: lastFailedAttempt has result",
                        new KiwiRetryerException("1",
                                new RetryException(Attempt.newResultAttempt("the result", 3, 3_000))),
                        true,
                        false
                ),

                Arguments.of(
                        "RetryException: lastFailedAttempt has exception",
                        new KiwiRetryerException("2",
                                new RetryException(Attempt.newExceptionAttempt(new IOException("I/O error"), 5, 10_000))),
                        true,
                        false
                ),

                Arguments.of(
                        "InterruptedException",
                        new KiwiRetryerException("3", new InterruptedException("Pardon")),
                        false,
                        true
                )
        );
    }

    @Nested
    class GetLastAttempt {

        @Test
        void shouldReturnEmptyOptional_WhenNotRetryException() {
            var retryerException = new KiwiRetryerException("Interrupted", new InterruptedException("Excuse me"));
            assertThat(retryerException.getLastAttempt()).isEmpty();
        }

        @Test
        void shouldReturnLastAttempt() {
            var lastFailedAttempt = Attempt.newExceptionAttempt
                    (new RuntimeException("failed to get the object"), 5, 2_500);
            var retryException = new RetryException(lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);
            assertThat(retryerException.getLastAttempt()).containsSame(lastFailedAttempt);
        }
    }

    @Nested
    class GetNumberOfFailedAttempts {

        @Test
        void shouldReturnEmptyOptionalInt_WhenNotRetryException() {
            var retryerException = new KiwiRetryerException("Interrupted", new InterruptedException("Pardon me"));
            assertThat(retryerException.getNumberOfFailedAttempts()).isEmpty();
        }

        @Test
        void shouldReturnNumberOfFailedAttempts() {
            var numFailedAttempts = 3;
            var lastFailedAttempt = Attempt.newExceptionAttempt(
                    new RuntimeException("failed to get the object"), numFailedAttempts, 2_500);
            var retryException = new RetryException(lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);
            assertThat(retryerException.getNumberOfFailedAttempts()).hasValue(numFailedAttempts);
        }
    }

    @Nested
    class UnwrapAsRetryException {

        @Test
        void shouldReturnEmptyOptional_WhenNotRetryException() {
            var retryerException = new KiwiRetryerException("Interrupted", new InterruptedException("Excuse me"));
            assertThat(retryerException.unwrapAsRetryException()).isEmpty();
        }

        @Test
        void shouldReturnRetryException() {
            var retryException = new RetryException(Attempt.newResultAttempt("a result", 3, 6_000));
            var retryerException = new KiwiRetryerException("bad results", retryException);
            assertThat(retryerException.unwrapAsRetryException()).containsSame(retryException);
        }
    }
}
