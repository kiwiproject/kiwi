package org.kiwiproject.retry;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiLists.nth;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Utility class for logging information about errors in {@link RetryResult} objects.
 */
@UtilityClass
public class RetryResultLogger {

    private static final String NO_ERRORS_TO_LOG = "no errors to log";

    /**
     * Logs a summary of the given result only if it {@link RetryResult#failed()}.
     *
     * @param result                    the result
     * @param logger                    the SLF4J logger to use
     * @param actionDescriptionSupplier a {@link Supplier} that provides a description of the action that was attempted
     *                                  for example "Create new order #12345" or "Update order #456"
     * @param <T>                       the type held in the result
     * @see #logSummary(RetryResult, Logger, String)
     */
    public static <T> void logSummaryIfFailed(RetryResult<T> result,
                                              Logger logger,
                                              Supplier<String> actionDescriptionSupplier) {

        checkArgumentsNotNull(result, logger);

        if (result.failed()) {
            logSummaryWithDescriptionSupplier(result, logger, actionDescriptionSupplier);
        }
    }

    /**
     * Logs a summary of the given result only if it has any errors or more than one attempt was made.
     *
     * @param result                    the result
     * @param logger                    the SLF4J logger to use
     * @param actionDescriptionSupplier a {@link Supplier} that provides a description of the action that was attempted
     *                                  for example "Create new order #12345" or "Update order #456"
     * @param <T>                       the type held in the result
     */
    public static <T> void logSummaryIfHasErrorsOrMultipleAttempts(RetryResult<T> result,
                                                                   Logger logger,
                                                                   Supplier<String> actionDescriptionSupplier) {

        checkArgumentsNotNull(result, logger);

        if (result.hasAnyErrors() || result.hasMoreThanOneAttempt()) {
            logSummaryWithDescriptionSupplier(result, logger, actionDescriptionSupplier);
        }
    }

    private static <T> void logSummaryWithDescriptionSupplier(RetryResult<T> result,
                                                              Logger logger,
                                                              Supplier<String> supplier) {

        var actionDescription = isNull(supplier) ? "" : supplier.get();
        logSummary(result, logger, actionDescription);
    }

    /**
     * Logs a high-level summary of the result. The log level is dependent on whether the result was successful
     * or not, and whether there were any errors (e.g. a result was successful but took more than one attempt).
     * <p>
     * The log levels are:
     * <p>
     * Result failed: ERROR
     * <p>
     * Result succeeded with errors: WARN
     * <p>
     * Result succeeded with no errors: DEBUG
     *
     * @param result            the result
     * @param logger            the SLF4J logger to use
     * @param actionDescription a {@link Supplier} that provides a description of the action that was attempted
     *                          for example "Create new order #12345" or "Update order #456"
     * @param <T>               the type held in the result
     */
    public static <T> void logSummary(RetryResult<T> result, Logger logger, String actionDescription) {
        checkArgumentsNotNull(result, logger);

        if (result.failed()) {
            logFailureSummary(result, logger, actionDescription);
        } else {
            logSuccessSummary(result, logger, actionDescription);
        }
    }

    private static <T> void logFailureSummary(RetryResult<T> result, Logger logger, String actionDescription) {
        checkArgument(result.failed(), "you must only pass a failed result to this method");

        var newActionDescription = buildActionDescription(actionDescription);
        var lastErrorInfo = buildLastErrorDescription(result);

        logger.error("Result {}: {} FAILED after {} attempts with {} errors. Unique error types: {}. Last error type/message: {}",
                result.getResultUuid(),
                newActionDescription,
                result.getNumAttemptsMade(),
                result.getNumErrors(),
                result.getUniqueErrorTypes(),
                lastErrorInfo);
    }

    private static <T> void logSuccessSummary(RetryResult<T> result, Logger logger, String actionDescription) {
        checkArgument(result.succeeded(), "you must only pass a successful result to this method");

        var newActionDescription = buildActionDescription(actionDescription);

        if (result.hasAnyErrors()) {
            var lastErrorInfo = buildLastErrorDescription(result);

            logger.warn("Result {}: {} SUCCEEDED after {} attempts with {} errors. Unique error types: {}. Last error type/message: {}",
                    result.getResultUuid(),
                    newActionDescription,
                    result.getNumAttemptsMade(),
                    result.getNumErrors(),
                    result.getUniqueErrorTypes(),
                    lastErrorInfo);
        } else {
            logger.debug("Result {}: {} SUCCEEDED after {} attempts with no errors.",
                    result.getResultUuid(),
                    newActionDescription,
                    result.getNumAttemptsMade());
        }
    }

    private static String buildActionDescription(String summaryText) {
        return StringUtils.isBlank(summaryText) ? "" : ("[" + summaryText + "]");
    }

    private static <T> String buildLastErrorDescription(RetryResult<T> result) {
        var lastError = result.getLastErrorIfPresent().orElse(null);
        return isNull(lastError) ? "[none]" : (lastError.getClass().getName() + " / " + lastError.getMessage());
    }

    /**
     * Log all exceptions contained in the result using the given logger.
     *
     * @param result the result
     * @param logger the SLF4J logger to use
     * @param <T>    the type held in the result
     */
    public static <T> void logAllExceptions(RetryResult<T> result, Logger logger) {
        logCommonResultInfo(result, logger, "all errors logged below");

        if (!result.hasAnyErrors()) {
            return;
        }

        var uuid = result.getResultUuid();
        var numErrors = result.getNumErrors();
        var errors = result.getErrors();

        IntStream.rangeClosed(1, numErrors).forEachOrdered(i -> {
            var error = nth(errors, i);
            logger.error("Result {}: error #{} of {}:", uuid, i, numErrors, error);
        });
    }

    /**
     * Log only the last exception contained in the result using the given logger.
     *
     * @param result the result
     * @param logger the SLF4J logger to use
     * @param <T>    the type held in the result
     */
    public static <T> void logLastException(RetryResult<T> result, Logger logger) {
        logCommonResultInfo(result, logger, "last error logged below");

        logLastErrorIfPresent(result, logger);
    }

    /**
     * Log the unique error types and the last exception contained in the result using the given logger.
     *
     * @param result the result
     * @param logger the SLF4J logger to use
     * @param <T>    the type held in the result
     */
    public static <T> void logExceptionTypesAndLast(RetryResult<T> result, Logger logger) {
        logCommonResultInfo(result, logger, "error types and last error logged below");

        var uniqueTypes = result.getUniqueErrorTypes();
        logger.error("Result {}: {} unique error types: {}", result.getResultUuid(), uniqueTypes.size(), uniqueTypes);

        logLastErrorIfPresent(result, logger);
    }

    private static <T> void logCommonResultInfo(RetryResult<T> result, Logger logger, String customErrorTypeMessage) {
        checkArgumentsNotNull(result, logger);

        var displayMessage = result.hasAnyErrors() ? customErrorTypeMessage : NO_ERRORS_TO_LOG;

        logger.error("Result {}: attempts: {}, maxAttempts: {}, hasObject: {}, hasErrors: {}, numErrors: {} ({})",
                result.getResultUuid(),
                result.getNumAttemptsMade(),
                result.getMaxAttempts(),
                result.hasObject(),
                result.hasAnyErrors(),
                result.getNumErrors(),
                displayMessage);
    }

    private static <T> void checkArgumentsNotNull(RetryResult<T> result, Logger logger) {
        checkArgumentNotNull(result, "result cannot be null");
        checkArgumentNotNull(logger, "logger cannot be null");
    }

    private static <T> void logLastErrorIfPresent(RetryResult<T> result, Logger logger) {
        result.getLastErrorIfPresent().ifPresent(lastError ->
                logger.error("Result {}: last error (of {} total errors):", result.getResultUuid(), result.getNumErrors(),
                        lastError));
    }
}
