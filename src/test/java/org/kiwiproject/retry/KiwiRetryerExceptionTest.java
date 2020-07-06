package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.github.rholder.retry.RetryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

@DisplayName("KiwiRetryerException")
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
        void shouldUnwrapExecutionExceptions() {
            var originalException = new RuntimeException("unknown and unexpected exception");
            var executionException = new ExecutionException(originalException);
            var retryerException = new KiwiRetryerException("oops", executionException);

            assertThat(retryerException.unwrap()).containsSame(originalException);
        }

        @Test
        void shouldUnwrapExecutionExceptions_HavingNullCause() {
            var executionException = new ExecutionException("unusual, but I have no cause", null);
            var retryerException = new KiwiRetryerException("error", executionException);

            assertThat(retryerException.unwrap()).isEmpty();
        }

        @Test
        void shouldReturnRetryExceptions() {
            var originalException = new RuntimeException("failed to get the object");
            var lastFailedAttempt = new ExceptionAttempt<String>(originalException, 5, 5_000);
            var retryException = new RetryException(3, lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrap()).containsSame(retryException);
        }

        @Test
        void shouldUnwrapRetryExceptions_HavingNullCause() {
            var lastFailedAttempt = new ExceptionAttempt<String>(null, 5, 5_000);
            var retryException = new RetryException(3, lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrap()).containsSame(retryException);
        }
    }

    @Nested
    class UnwrapKiwiRetryerExceptionFully {

        @Test
        void shouldUnwrapExecutionExceptions() {
            var originalException = new RuntimeException("unknown and unexpected exception");
            var executionException = new ExecutionException(originalException);
            var retryerException = new KiwiRetryerException("oops", executionException);

            assertThat(retryerException.unwrapFully()).containsSame(originalException);
        }

        @Test
        void shouldUnwrapExecutionExceptions_HavingNullCause() {
            var executionException = new ExecutionException("unusual, but I have no cause", null);
            var retryerException = new KiwiRetryerException("error", executionException);

            assertThat(retryerException.unwrapFully()).isEmpty();
        }

        @Test
        void shouldUnwrapRetryExceptions() {
            var originalException = new RuntimeException("failed to get the object");
            var lastFailedAttempt = new ExceptionAttempt<String>(originalException, 5, 2_500);
            var retryException = new RetryException(3, lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrapFully()).containsSame(originalException);
        }

        @Test
        void shouldUnwrapRetryExceptions_HavingNullCause() {
            var lastFailedAttempt = new ExceptionAttempt<String>(null, 3, 3_000);
            var retryException = new RetryException(3, lastFailedAttempt);
            var retryerException = new KiwiRetryerException("retry error", retryException);

            assertThat(retryerException.unwrapFully()).isEmpty();
        }
    }
}