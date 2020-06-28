package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.fourth;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;

import java.util.List;

@DisplayName("RetryResultLogger")
@ExtendWith(SoftAssertionsExtension.class)
class RetryResultLoggerTest {

    // Note this is a Logback Logger, NOT an SLF4J one, because we need to be able to get the MapAppender
    // The cast is necessary since LoggerFactory returns an SLF4J Logger.
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(RetryResultLoggerTest.class);

    private RetryResult<String> result;
    private InMemoryAppender appender;

    @BeforeEach
    void setUp() {
        appender = (InMemoryAppender) LOG.getAppender("MEMORY");
    }

    @AfterEach
    void tearDown() {
        appender.clearEvents();
    }

    @Nested
    class ShouldThrowIllegalArgumentException {

        @Test
        void whenGivenNullRetryResult(SoftAssertions softly) {
            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logSummaryIfFailed(null, LOG, () -> "description"));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logSummaryIfHasErrorsOrMultipleAttempts(null, LOG, () -> "description"));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logSummary(null, LOG, "description"));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logAllExceptions(null, LOG));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logLastException(null, LOG));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logExceptionTypesAndLast(null, LOG));
        }

        @Test
        void whenGivenNullLogger(SoftAssertions softly) {
            result = new RetryResult<>(3, 3, null, List.of());

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logSummaryIfFailed(result, null, () -> "description"));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logSummaryIfHasErrorsOrMultipleAttempts(result, null, () -> "description"));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logSummary(result, null, "description"));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logAllExceptions(result, null));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logLastException(result, null));

            assertIllegalArgumentExceptionThrownBy(softly,
                    () -> RetryResultLogger.logExceptionTypesAndLast(result, null));
        }

        private void assertIllegalArgumentExceptionThrownBy(SoftAssertions softly,
                                                            ThrowableAssert.ThrowingCallable shouldRaiseThrowable) {
            softly.assertThatThrownBy(shouldRaiseThrowable)
                    .isExactlyInstanceOf(IllegalArgumentException.class);
            softly.assertThat(appender.getOrderedEvents()).isEmpty();
        }
    }

    @Nested
    class LogSummaryMethods {

        private static final String ACTION = "Create Order 12345";

        @Test
        void shouldLogFailedResults_WhenHaveErrors() {
            result = new RetryResult<>(3, 3, null, List.of(
                    new ConstraintViolationException("'bar' is required"),
                    new ConstraintViolationException("'bar' is required"),
                    new ConstraintViolationException("'bar' is required")
            ));

            RetryResultLogger.logSummary(result, LOG, ACTION);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .startsWith(summaryMessage(result))
                    .endsWith("'bar' is required");
            assertThat(event.getThrowableProxy()).isNull();
        }

        @Test
        void shouldLogFailedResults_WhenHaveNoErrors() {
            result = new RetryResult<>(3, 3, null, List.of());

            RetryResultLogger.logSummary(result, LOG, ACTION);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .startsWith(summaryMessage(result))
                    .endsWith("[none]");
            assertThat(event.getThrowableProxy()).isNull();
        }

        @Test
        void shouldLogSuccessfulResults_WhenHaveErrors() {
            result = new RetryResult<>(3, 5, "Foo", List.of(
                    new ConstraintViolationException("'bar' is required"),
                    new ConstraintViolationException("'bar' is required")
            ));

            RetryResultLogger.logSummary(result, LOG, ACTION);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .startsWith(summaryMessage(result))
                    .endsWith("'bar' is required");
            assertThat(event.getThrowableProxy()).isNull();
        }

        @Test
        void shouldLogSuccessfulResults_WhenHaveNoErrors() {
            result = new RetryResult<>(3, 5, "Foo", List.of());

            RetryResultLogger.logSummary(result, LOG, ACTION);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .isEqualTo(successMessageForMultipleAttempts(result));
            assertThat(event.getThrowableProxy()).isNull();
        }

        @Test
        void shouldLogSummary_IfFailed() {
            result = new RetryResult<>(2, 2, null, List.of(
                    new ConstraintViolationException("'bar' is required"),
                    new ConstraintViolationException("'bar' is required")
            ));

            RetryResultLogger.logSummaryIfFailed(result, LOG, () -> ACTION);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .startsWith(summaryMessage(result))
                    .endsWith("'bar' is required");
            assertThat(event.getThrowableProxy()).isNull();
        }

        @Test
        void shouldLogSummary_IfHasErrors() {
            result = new RetryResult<>(3, 5, "Foo", List.of(
                    new ConstraintViolationException("'bar' is required"),
                    new ConstraintViolationException("'bar' is required")
            ));

            RetryResultLogger.logSummaryIfHasErrorsOrMultipleAttempts(result, LOG, () -> ACTION);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .startsWith(summaryMessage(result))
                    .endsWith("'bar' is required");
            assertThat(event.getThrowableProxy()).isNull();
        }

        private String summaryMessage(RetryResult<String> result) {
            return f("Result {}: [{}] {} after {} attempts with {} errors. Unique error types: {}. Last error type/message: ",
                    result.getResultUuid(),
                    ACTION,
                    result.succeeded() ? "SUCCEEDED" : "FAILED",
                    result.getNumAttemptsMade(),
                    result.getNumErrors(),
                    result.getUniqueErrorTypes());
        }

        @Test
        void shouldLogSummary_IfHasMultipleAttempts() {
            result = new RetryResult<>(2, 5, "Foo", List.of());

            RetryResultLogger.logSummaryIfHasErrorsOrMultipleAttempts(result, LOG, () -> ACTION);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .isEqualTo(successMessageForMultipleAttempts(result));
            assertThat(event.getThrowableProxy()).isNull();
        }

        private String successMessageForMultipleAttempts(RetryResult<String> result) {
            return f("Result {}: [{}] SUCCEEDED after {} attempts with no errors.",
                    result.getResultUuid(),
                    ACTION,
                    result.getNumAttemptsMade());
        }
    }

    @Nested
    class WhenFailedResultHasNoExceptions {

        @Test
        void shouldStillLogMessage() {
            result = new RetryResult<>(3, 3, null, List.of());

            RetryResultLogger.logAllExceptions(result, LOG);

            var events = assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage())
                    .startsWith(f("Result {}: attempts: {}, maxAttempts: {}, hasObject: {}, hasErrors: {}, numErrors: {} ({})",
                            result.getResultUuid(),
                            result.getNumAttemptsMade(),
                            result.getMaxAttempts(),
                            result.hasObject(),
                            result.hasAnyErrors(),
                            result.getNumErrors(),
                            "no errors to log"));
            assertThat(event.getThrowableProxy()).isNull();
        }
    }

    @Nested
    class WhenResultHasSameExceptions {

        @BeforeEach
        void setUp() {
            List<Exception> errors = List.of(
                    new DataException("Thing 42 update error 1", new ConstraintViolationException("foo cannot be null")),
                    new DataException("Thing 42 update error 2", new ConstraintViolationException("foo cannot be null")),
                    new DataException("Thing 42 update error 3", new ConstraintViolationException("foo cannot be null"))
            );

            result = new RetryResult<>(3, 3, null, errors);
        }

        @Test
        void shouldLogAllExceptions() {
            assertLogsAllThreeExceptionsWithTypes("DataException", "DataException", "DataException");
        }

        @Test
        void shouldLogLastException() {
            assertLogsLastExceptionWithType("DataException");
        }

        @Test
        void shouldLogExceptionTypesAndLast() {
            assertLogsExceptionTypesAndLastWithType("DataException");
        }
    }

    @Nested
    class WhenResultHasDifferentExceptions {

        @BeforeEach
        void setUp() {
            List<Exception> errors = List.of(
                    new GeneralDataException("Thing 42 update error 1", new ConstraintViolationException("foo cannot be null")),
                    new DataException("Thing 42 update error 2", new ConstraintViolationException("foo cannot be null")),
                    new DataUpdateException("Thing 42 update error 3", new ConstraintViolationException("foo cannot be null"))
            );

            result = new RetryResult<>(3, 3, null, errors);
        }

        @Test
        void shouldLogAllExceptions() {
            assertLogsAllThreeExceptionsWithTypes("GeneralDataException", "DataException", "DataUpdateException");
        }

        @Test
        void shouldLogLastException() {
            assertLogsLastExceptionWithType("DataUpdateException");
        }

        @Test
        void shouldLogExceptionTypesAndLast() {
            assertLogsExceptionTypesAndLastWithType("DataUpdateException");
        }
    }

    @SuppressWarnings("SameParameterValue")
    void assertLogsAllThreeExceptionsWithTypes(String type1, String type2, String type3) {
        RetryResultLogger.logAllExceptions(result, LOG);

        var events = assertNumberOfLoggingEventsAndGet(4);

        assertThat(first(events).getFormattedMessage())
                .startsWith(f("Result {}:", result.getResultUuid()))
                .endsWith("(all errors logged below)");
        assertThat(first(events).getThrowableProxy()).isNull();

        assertThat(second(events).getFormattedMessage())
                .startsWith(f("Result {}: error #1 of 3", result.getResultUuid()));
        assertThat(second(events).getThrowableProxy().getClassName()).endsWith(type1);

        assertThat(third(events).getFormattedMessage())
                .startsWith(f("Result {}: error #2 of 3", result.getResultUuid()));
        assertThat(third(events).getThrowableProxy().getClassName()).endsWith(type2);

        assertThat(fourth(events).getFormattedMessage())
                .startsWith(f("Result {}: error #3 of 3", result.getResultUuid()));
        assertThat(fourth(events).getThrowableProxy().getClassName()).endsWith(type3);
    }

    private void assertLogsLastExceptionWithType(String exceptionType) {
        RetryResultLogger.logLastException(result, LOG);

        var events = assertNumberOfLoggingEventsAndGet(2);

        assertThat(first(events).getFormattedMessage())
                .startsWith(f("Result {}:", result.getResultUuid()))
                .endsWith("(last error logged below)");
        assertThat(first(events).getThrowableProxy()).isNull();

        assertThat(second(events).getFormattedMessage())
                .startsWith(f("Result {}: last error (of 3 total errors)", result.getResultUuid()));
        assertThat(second(events).getThrowableProxy().getClassName()).endsWith(exceptionType);
    }

    private void assertLogsExceptionTypesAndLastWithType(String exceptionType) {
        RetryResultLogger.logExceptionTypesAndLast(result, LOG);

        var events = assertNumberOfLoggingEventsAndGet(3);

        assertThat(first(events).getFormattedMessage())
                .startsWith(f("Result {}:", result.getResultUuid()))
                .endsWith("(error types and last error logged below)");
        assertThat(first(events).getThrowableProxy()).isNull();

        assertThat(second(events).getFormattedMessage())
                .startsWith(f("Result {}: {} unique error types:",
                        result.getResultUuid(), result.getUniqueErrorTypes().size()));
        assertThat(second(events).getThrowableProxy()).isNull();

        assertThat(third(events).getFormattedMessage())
                .startsWith(f("Result {}: last error (of 3 total errors)", result.getResultUuid()));
        assertThat(third(events).getThrowableProxy().getClassName()).endsWith(exceptionType);
    }

    private List<ILoggingEvent> assertNumberOfLoggingEventsAndGet(int expectedEventCount) {
        var events = appender.getOrderedEvents();
        assertThat(events).hasSize(expectedEventCount);
        return events;
    }

    private static class GeneralDataException extends RuntimeException {
        GeneralDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class DataException extends GeneralDataException {
        DataException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class DataUpdateException extends DataException {
        DataUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class ConstraintViolationException extends RuntimeException {
        ConstraintViolationException(String message) {
            super(message);
        }
    }
}