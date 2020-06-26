package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.UUIDs;

import java.util.ArrayList;
import java.util.List;

@DisplayName("RetryResult")
class RetryResultTest {

    private RetryResult<Integer> result;

    @Nested
    class Constructor {

        private List<Exception> mutableErrorsList;

        @BeforeEach
        void setUp() {
            mutableErrorsList = new ArrayList<>();
            result = new RetryResult<>(1, 5, 42, mutableErrorsList);
        }

        @Test
        void shouldAssignUuid() {
            assertThat(result.getResultUuid()).isNotBlank();
            assertThat(UUIDs.isValidUUID(result.getResultUuid())).isTrue();
        }

        @Test
        void shouldMakeUnmodifiableCopyOfErrorsList() {
            var newError = new IllegalStateException();
            var errors = result.getErrors();

            assertThat(errors).isNotSameAs(mutableErrorsList);

            assertThatThrownBy(() -> errors.add(newError))
                    .describedAs("Errors should not be modifiable after construction")
                    .isExactlyInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void shouldAcceptNullErrorsList() {
            result = new RetryResult<>(1, 5, 84, null);

            assertThat(result.getErrors()).isEmpty();
        }

        @Nested
        class ShouldThrowIllegalArgumentException {

            @Test
            void whenNumAttemptsMoreThanMaxAttempts() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> new RetryResult<Integer>(6, 5, null, List.of()))
                        .withMessage("numAttemptsMade (6) is not less or equal to maxAttempts (5)");
            }
        }
    }

    @Nested
    class WhenHasNoErrors {

        @BeforeEach
        void setUp() {
            result = new RetryResult<>(1, 5, 42, new ArrayList<>());
        }

        @Test
        void shouldHaveAttemptsMade() {
            assertThat(result.getNumAttemptsMade()).isOne();
        }

        @Test
        void shouldNotHaveMoreThanOneAttempt() {
            assertThat(result.hasMoreThanOneAttempt()).isFalse();
        }

        @Test
        void shouldHaveMaxAttempts() {
            assertThat(result.getMaxAttempts()).isEqualTo(5);
        }

        @Test
        void shouldHaveNoErrors() {
            assertThat(result.hasAnyErrors()).isFalse();
            assertThat(result.getNumErrors()).isZero();
        }

        @Test
        void shouldNotHaveLastError() {
            assertThat(result.getLastErrorIfPresent()).isEmpty();
        }

        @Test
        void shouldThrowWhenAskedForLastError() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> result.getLastError())
                    .withMessage("No errors exist in this result");
        }
    }

    @Nested
    class WhenHasNoResult {

        private List<Exception> errors;

        @BeforeEach
        void setUp() {
            errors = List.of(
                    new IllegalStateException("attempt 1 error"),
                    new IllegalStateException("attempt 2 error"),
                    new IllegalStateException("attempt 3 error")
            );
            result = new RetryResult<>(4, 5, null, errors);
        }

        @Test
        void shouldHaveMoreThanOneAttempt() {
            assertThat(result.hasMoreThanOneAttempt()).isTrue();
        }

        @Test
        void shouldNotHaveAnObject() {
            assertThat(result.getObjectIfPresent()).isEmpty();
            assertThat(result.hasObject()).isFalse();
        }

        @Test
        void shouldThrowWhenAskedForAnObject() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> result.getObject())
                    .withMessage("No object is present in this result");
        }

        @Test
        void shouldReportFailure() {
            assertThat(result.succeeded()).isFalse();
            assertThat(result.failed()).isTrue();
        }

        @Test
        void shouldHaveErrors() {
            assertThat(result.hasAnyErrors()).isTrue();
            assertThat(result.getNumErrors()).isEqualTo(3);
        }

        @Test
        void shouldHaveLastError() {
            var lastError = result.getLastError();

            assertThat(lastError).isSameAs(third(errors));
            assertThat(result.getLastErrorIfPresent()).contains(lastError);
        }
    }

    @Nested
    class WhenHasResult {

        private List<Exception> errors;

        @BeforeEach
        void setUp() {
            errors = List.of(
                    new IllegalStateException("attempt 1 error"),
                    new IllegalStateException("attempt 2 error")
            );
            result = new RetryResult<>(3, 5, 42, errors);
        }

        @Test
        void shouldHaveObject() {
            assertThat(result.hasObject()).isTrue();
            assertThat(result.getObject()).isEqualTo(42);
            assertThat(result.getObjectIfPresent()).hasValue(42);
        }

        @Test
        void shouldReportSuccess() {
            assertThat(result.succeeded()).isTrue();
            assertThat(result.failed()).isFalse();
        }

        @Test
        void shouldHaveSomeErrors() {
            assertThat(result.hasAnyErrors()).isTrue();
            assertThat(result.getNumErrors()).isEqualTo(2);
        }

        @Test
        void shouldHaveLastError() {
            var lastError = result.getLastError();

            assertThat(lastError).isSameAs(second(errors));
            assertThat(result.getLastErrorIfPresent()).contains(lastError);
        }
    }

    @Nested
    class GetUniqueErrorTypes {

        @Test
        void shouldReturnEmptySet_WhenNoErrorsExist() {
            result = new RetryResult<>(1, 5, 84, List.of());
            assertThat(result.getUniqueErrorTypes()).isEmpty();
        }

        @Test
        void shouldReturnSingleElementSet_WhenAllErrorsHaveSameType() {
            result = new RetryResult<>(5, 5, null, List.of(
                    new IllegalStateException("attempt 1 error"),
                    new IllegalStateException("attempt 2 error"),
                    new IllegalStateException("attempt 3 error"),
                    new IllegalStateException("attempt 4 error"),
                    new IllegalStateException("attempt 5 error")
            ));
            assertThat(result.getUniqueErrorTypes())
                    .containsExactly(IllegalStateException.class.getName());
        }

        @Test
        void shouldReturnUniqueTypes_WhenErrorsHaveDifferentTypes() {
            result = new RetryResult<Integer>(5, 5, null, List.of(
                    new IllegalArgumentException("attempt 1 error"),
                    new IllegalStateException("attempt 2 error"),
                    new IllegalAccessException("attempt 3 error"),
                    new IllegalStateException("attempt 4 error"),
                    new IllegalArgumentException("attempt 5 error")
            ));
            assertThat(result.getUniqueErrorTypes()).containsExactlyInAnyOrder(
                    IllegalAccessException.class.getName(),
                    IllegalArgumentException.class.getName(),
                    IllegalStateException.class.getName()
            );
        }
    }
}
