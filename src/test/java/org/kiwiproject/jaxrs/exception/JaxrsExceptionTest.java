package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@DisplayName("JaxrsException")
@ExtendWith(SoftAssertionsExtension.class)
class JaxrsExceptionTest {

    @Nested
    class ErrorMessagesConstructor {

        @Test
        void shouldUseMessageFromFirstErrorMessage() {
            var errorMessage1 = new ErrorMessage(401, "Not Authorized");
            var errorMessage2 = new ErrorMessage(403, "Forbidden");
            var ex = new JaxrsException(List.of(errorMessage1, errorMessage2), null);

            assertThat(ex).hasMessage("Not Authorized");
            assertThat(ex.getErrors()).containsExactly(errorMessage1, errorMessage2);
            assertThat(ex.getOtherData()).isEmpty();
        }

        @Test
        void shouldAcceptEmptyErrorMessageList() {
            var ex = new JaxrsException(List.of(), null);

            assertThat(ex).hasMessage(ErrorMessage.DEFAULT_MSG);
            assertThat(ex.getStatusCode()).isEqualTo(ErrorMessage.DEFAULT_CODE);
        }
    }

    @Nested
    class JaxrsExceptionsConstructor {

        @Test
        void shouldAggregateListOfJaxrsExceptions() {
            var errorMessage1 = new ErrorMessage(401, "Not Authorized");
            var errorMessage2 = new ErrorMessage(403, "Forbidden");
            var ex1 = new JaxrsException(List.of(errorMessage1, errorMessage2), null);
            ex1.setOtherData(Map.of("detail1", 1));

            var errorMessage3 = new ErrorMessage(504, "Gateway Timeout");
            var ex2 = new JaxrsException(errorMessage3);

            var errorMessage4 = new ErrorMessage(404, "Not Found");
            var errorMessage5 = new ErrorMessage(429, "Too Many Requests");
            var ex3 = new JaxrsException(List.of(errorMessage4, errorMessage5), null);
            ex3.setOtherData(Map.of("detail2", 2));

            var aggregateException = new JaxrsException(List.of(ex1, ex2, ex3));

            assertThat(aggregateException).hasMessage("Rollup of 3 exceptions.");
            assertThat(aggregateException.getStatusCode())
                    .describedAs("Rollup status should be highest error mod 100")
                    .isEqualTo(500);
            assertThat(aggregateException.getErrors()).containsExactly(
                    errorMessage1,
                    errorMessage2,
                    errorMessage3,
                    errorMessage4,
                    errorMessage5
            );
            assertThat(aggregateException.getOtherData()).containsOnly(
                    entry("detail1", 1),
                    entry("detail2", 2)
            );
        }

        @Test
        void shouldAcceptEmptyJaxrsExceptionList() {
            var ex = new JaxrsException(List.of());

            assertThat(ex).hasMessage(ErrorMessage.DEFAULT_MSG);
            assertThat(ex.getStatusCode()).isEqualTo(ErrorMessage.DEFAULT_CODE);
        }
    }

    @Nested
    class BuildJaxrsException {

        @Test
        void shouldReturnSameInstance_WhenIsAJaxrsException() {
            var exception = new JaxrsException("401 Unauthorized", 401);
            var jaxrsException = JaxrsException.buildJaxrsException(exception);
            assertThat(jaxrsException).isSameAs(exception);
        }

        @Test
        void shouldWrapInJaxrsException_WhenIsWebApplicationException() {
            var exception = new NotSupportedException();
            var jaxrsException = JaxrsException.buildJaxrsException(exception);
            assertThat(jaxrsException.getStatusCode()).isEqualTo(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
        }

        @Test
        void shouldWrapInJaxrsException_WhenIsAnyOtherNonJaxrsException() {
            var exception = new IllegalArgumentException("No soup for you!");
            var jaxrsException = JaxrsException.buildJaxrsException(exception);
            assertThat(jaxrsException.getStatusCode()).isEqualTo(JaxrsException.getErrorCode(exception));
        }
    }

    @Nested
    class StatusCodes {

        @Test
        void shouldEqualCodeForThrowable_WhenGivenThrowable() {
            var cause = new IllegalArgumentException();
            var ex = new JaxrsException(cause);
            assertThat(ex.getStatusCode()).isEqualTo(JaxrsException.getErrorCode(cause));
        }

        @Test
        void shouldEqualDefaultErrorMessageCode_WhenGivenOnlyMessage() {
            var ex = new JaxrsException("An error occurred");
            assertThat(ex.getStatusCode()).isEqualTo(ErrorMessage.DEFAULT_CODE);
        }

        @Test
        void shouldEqualStatusCode_WhenGivenMessageAndStatusCode() {
            var ex = new JaxrsException("Length Required", 411);
            assertThat(ex.getStatusCode()).isEqualTo(411);
        }

        @Test
        void shouldEqualCodeForThrowable_WhenGivenMessageAndThrowable() {
            var cause = new IllegalStateException();
            var ex = new JaxrsException("I feel the conflict within you...", cause);
            assertThat(ex.getStatusCode()).isEqualTo(JaxrsException.getErrorCode(cause));
        }

        @Test
        void shouldEqualStatusCode_WhenGivenMessageAndCauseAndStatus() {
            var cause = new RuntimeException("unavailable");
            var ex = new JaxrsException("unavailable error", cause, 503);
            assertThat(ex.getStatusCode()).isEqualTo(503);
        }

        @Test
        void shouldEqualErrorMessageStatus_WhenGivenErrorMessage() {
            var errorMessage = new ErrorMessage(502, "Bad Gateway");
            var ex = new JaxrsException(errorMessage);
            assertThat(ex.getStatusCode()).isEqualTo(502);
        }

        @Test
        void shouldEqualStatusCode_WhenGivenThrowableAndStatusCode() {
            var cause = new RuntimeException("Not acceptable");
            var ex = new JaxrsException(cause, 406);
            assertThat(ex.getStatusCode()).isEqualTo(406);
        }

        @Test
        void shouldEqualErrorMessageStatus_WhenGivenErrorMessageAndThrowable() {
            var errorMessage = new ErrorMessage(405, "Not Allowed");
            var cause = new RuntimeException("Nope");
            var ex = new JaxrsException(errorMessage, cause);
            assertThat(ex.getStatusCode()).isEqualTo(405);
        }

        @Test
        void shouldEqualExplicitStatusCode_WhenGivenErrorMessageListAndStatusCode() {
            var errorMessage1 = new ErrorMessage(422, "The thing is not valid");
            var errorMessage2 = new ErrorMessage(401, "Not Authorized");

            // we'll say the overall status is a 409 Conflict
            var ex = new JaxrsException(List.of(errorMessage1, errorMessage2), 409);
            assertThat(ex.getStatusCode()).isEqualTo(409);
        }

        @Test
        void shouldEqualErrorMessageStatus_WhenGivenSingletonErrorMessageListAndNullStatusCode() {
            var errorMessage = new ErrorMessage(422, "The thing is not valid");

            // // we won't give an overall status; the rollup will equal the only status code given
            var ex = new JaxrsException(List.of(errorMessage), null);
            assertThat(ex.getStatusCode()).isEqualTo(422);
        }

        @Test
        void shouldEqualRolledUpStatus_WhenGivenErrorMessageListAndNullStatusCode() {
            var errorMessage1 = new ErrorMessage(422, "The thing is not valid");
            var errorMessage2 = new ErrorMessage(401, "Not Authorized");

            // we won't give an overall status; the rollup will be 400
            var ex = new JaxrsException(List.of(errorMessage1, errorMessage2), null);
            assertThat(ex.getStatusCode()).isEqualTo(400);
        }

        @Test
        void shouldEqualRolledUpStatus_WhenGivenJaxrsExceptionList() {
            var ex1 = new JaxrsException("Error 1", 405);
            var ex2 = new JaxrsException("Error 2", 503);
            var ex3 = new JaxrsException("Error 3", 415);
            var aggregateException = new JaxrsException(List.of(ex1, ex2, ex3));
            assertThat(aggregateException).hasMessage("Rollup of 3 exceptions.");
            assertThat(aggregateException.getStatusCode()).isEqualTo(500);
        }

        @Test
        void shouldEqualStatusOfErrors_WhenAllErrorsHaveSameStatus() {
            var ex1 = new JaxrsException("Error 1", 503);
            var ex2 = new JaxrsException("Error 2", 503);
            var ex3 = new JaxrsException("Error 3", 503);
            var aggregateException = new JaxrsException(List.of(ex1, ex2, ex3));
            assertThat(aggregateException).hasMessage("Rollup of 3 exceptions.");
            assertThat(aggregateException.getStatusCode()).isEqualTo(503);
        }

        @Test
        void shouldEqualDefaultStatus_WhenGivenEmptyJaxrsExceptionList() {
            var aggregateException = new JaxrsException(List.of());
            assertThat(aggregateException.getStatusCode()).isEqualTo(500);
        }
    }

    @Nested
    class ErrorMessages {

        @Test
        void shouldBeUnmodifiable() {
            var errorMessage1 = new ErrorMessage(422, "The thing is not valid");
            var errorMessage2 = new ErrorMessage(401, "Not Authorized");

            var ex = new JaxrsException(List.of(errorMessage1, errorMessage2), null);
            var errorMessages = ex.getErrors();
            assertThat(errorMessages).containsExactly(errorMessage1, errorMessage2);

            var anotherError = new ErrorMessage("another error");
            assertThatThrownBy(() -> errorMessages.add(anotherError))
                    .isExactlyInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void shouldReplaceExistingErrors() {
            var errorMessage1 = new ErrorMessage(422, "The thing is not valid");
            var errorMessage2 = new ErrorMessage(401, "Not Authorized");
            var ex = new JaxrsException(List.of(errorMessage1, errorMessage2), null);

            var differentError = new ErrorMessage("a different error");
            ex.setErrors(List.of(differentError));

            assertThat(ex.getErrors()).containsOnly(differentError);
        }

        @Test
        void shouldNotAllowEmptyErrorsToBeSet() {
            var errorMessage1 = new ErrorMessage(422, "The thing is not valid");
            var errorMessage2 = new ErrorMessage(401, "Not Authorized");
            var ex = new JaxrsException(List.of(errorMessage1, errorMessage2), null);

            ex.setErrors(List.of());

            assertThat(ex.getErrors()).containsExactlyInAnyOrder(errorMessage1, errorMessage2);
        }
    }

    @Nested
    class OtherData {

        @Test
        void shouldAppend() {
            var ex = new JaxrsException("An error occurred");
            var otherData = Map.<String, Object>of("detail1", "value1", "detail2", 42);
            ex.setOtherData(otherData);

            var moreData = Map.<String, Object>of("detail3", "boo");
            ex.setOtherData(moreData);

            assertThat(ex.getOtherData())
                    .describedAs("should have appended new data (even though this is odd)")
                    .containsOnly(
                            entry("detail1", "value1"),
                            entry("detail2", 42),
                            entry("detail3", "boo")
                    );
        }

        @Test
        void shouldAllowClearingExistingData() {
            var ex = new JaxrsException("An error occurred");
            var otherData = Map.<String, Object>of("detail1", "value1", "detail2", 42);
            ex.setOtherData(otherData);
            assertThat(ex.getOtherData()).hasSize(2);

            ex.setOtherData(null);

            assertThat(ex.getOtherData())
                    .describedAs("should be empty but not null")
                    .isEmpty();
        }
    }

    @Nested
    class GetErrorCode {

        @Test
        void shouldBe_500_WhenNullArgument() {
            assertThat(JaxrsException.getErrorCode(null)).isEqualTo(500);
        }

        @Test
        void shouldBe_500_WhenNullPointerException() {
            assertThat(JaxrsException.getErrorCode(new NullPointerException())).isEqualTo(500);
        }

        @Test
        void shouldBe_400_WhenIllegalArgumentException() {
            assertThat(JaxrsException.getErrorCode(new IllegalArgumentException())).isEqualTo(400);
        }

        @Test
        void shouldBe_409_WhenIllegalStateException() {
            assertThat(JaxrsException.getErrorCode(new IllegalStateException())).isEqualTo(409);
        }

        @Test
        void shouldBe_StatusOfJaxrsException() {
            assertThat(JaxrsException.getErrorCode(new JaxrsException("Forbidden", 403))).isEqualTo(403);
        }

        @Test
        void shouldBe_StatusOfWebApplicationException() {
            assertThat(JaxrsException.getErrorCode(new NotFoundException())).isEqualTo(404);
        }

        @Test
        void shouldBe_409_WhenClassName_IsContainedInSetOfConflictExceptions() {
            assertThat(JaxrsException.getErrorCode(new IllegalMonitorStateException())).isEqualTo(409);
        }
    }
}
