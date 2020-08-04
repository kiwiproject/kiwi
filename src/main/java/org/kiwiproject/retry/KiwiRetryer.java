package org.kiwiproject.retry;

import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;
import static org.kiwiproject.retry.KiwiRetryerPredicates.CONNECTION_ERROR;
import static org.kiwiproject.retry.KiwiRetryerPredicates.NO_ROUTE_TO_HOST;
import static org.kiwiproject.retry.KiwiRetryerPredicates.SOCKET_TIMEOUT;
import static org.kiwiproject.retry.KiwiRetryerPredicates.UNKNOWN_HOST;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;
import com.google.common.base.Predicate;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.base.KiwiThrowables;
import org.kiwiproject.base.UUIDs;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * This is a wrapper class for {@link Retryer}; it wraps methods so that the {@link RetryException} and
 * {@link ExecutionException} that are generated from the {@link Retryer#call(Callable)} method are converted to
 * {@link KiwiRetryerException}.
 * <p>
 * It also provides some kiwi-flavored default values, an identifier that can be used to distinguish between
 * retryer instances in logs, logging of retry attempts, and some factories for creating retryer instances
 * with common behavior.
 * <p>
 * You can construct a {@link KiwiRetryer} using the builder obtained via {@code KiwiRetryer.builder()}.
 * <table>
 *     <caption>Available configuration options for KiwiRetryer:</caption>
 *      <tr>
 *         <th>Name</th>
 *         <th>Default</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *          <td>retryerId</td>
 *          <td>UUID generated by {@link UUIDs#randomUUIDString()}</td>
 *          <td>The identifier for the retryer</td>
 *     </tr>
 *     <tr>
 *          <td>initialSleepTimeAmount</td>
 *          <td>100</td>
 *          <td>
 *              The initial sleep amount for the default incrementing wait strategy.
 *              This value will be ignored if an explicit {@link WaitStrategy} is defined.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>initialSleepTimeUnit</td>
 *          <td>{@link TimeUnit#MILLISECONDS}</td>
 *          <td>
 *              The initial sleep {@link TimeUnit} for the default incrementing wait strategy.
 *              This value will be ignored if an explicit {@link WaitStrategy} is defined.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>retryIncrementTimeAmount</td>
 *          <td>200</td>
 *          <td>
 *              The subsequent retry increment amount for the default incrementing wait strategy.
 *              This value will be ignored if an explicit {@link WaitStrategy} is defined.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>retryIncrementTimeUnit</td>
 *          <td>{@link TimeUnit#MILLISECONDS}</td>
 *          <td>
 *              The subsequent retry increment {@link TimeUnit} for the default incrementing wait strategy.
 *              This value will be ignored if an explicit {@link WaitStrategy} is defined.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>maxAttempts</td>
 *          <td>5</td>
 *          <td>
 *              The maximum number of attempts to use for the default stop strategy.
 *              This value will be ignored if an explicit {@link StopStrategy} is defined.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>processingLogLevel</td>
 *          <td>{@link Level#DEBUG}</td>
 *          <td>This log level controls the "happy path" messages that are logged by this retryer.</td>
 *     </tr>
 *     <tr>
 *          <td>exceptionLogLevel</td>
 *          <td>{@link Level#WARN}</td>
 *          <td>This log level controls the "sad path" messages (i.e. exceptions) that are logged by this retryer.</td>
 *     </tr>
 *     <tr>
 *          <td>retryOnAllExceptions</td>
 *          <td>false</td>
 *          <td>
 *              Tells the retryer to retry on any exception. NOTE: This supersedes any exceptions added to the
 *              {@code exceptionPredicates} list or if {@code retryOnAllRuntimeExceptions} is set to {@code true}
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>retryOnAllRuntimeExceptions</td>
 *          <td>false</td>
 *          <td>
 *              Tells the retryer to retry on all {@link RuntimeException}s. NOTE: This supersedes any exceptions
 *              added to the {@code exceptionPredicates} list.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>exceptionPredicates</td>
 *          <td>empty list</td>
 *          <td>
 *              Defines the {@link Throwable}s that should cause KiwiRetryer to retry its specified {@link Callable} if
 *              encountered during processing. Note these are <em>Guava</em> {@link Predicate} objects <em>not</em>
 *              JDK {@link java.util.function.Predicate} objects; the reason is that the underlying guava-retrying
 *              library uses Guava's predicate class.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>resultPredicates</td>
 *          <td>empty list</td>
 *          <td>
 *              Defines the {@code T} objects that should cause KiwiRetryer to retry its specified {@link Callable} if
 *              returned by the {@link Callable} during processing. Note these are <em>Guava</em> {@link Predicate}
 *              objects <em>not</em> JDK {@link java.util.function.Predicate} objects; the reason is that the underlying
 *              guava-retrying library uses Guava's predicate class.
 *          </td>
 *     </tr>
 *     <tr>
 *          <td>stopStrategy</td>
 *          <td>null</td>
 *          <td>An explicit {@code stopStrategy} which will override the default stop after attempts stop strategy.</td>
 *     </tr>
 *     <tr>
 *          <td>waitStrategy</td>
 *          <td>null</td>
 *          <td>An explicit {@code waitStrategy} which will override the default incrementing wait strategy.</td>
 *     </tr>
 * </table>
 * <p>
 * NOTE: The guava-retrying library must be available at runtime.
 */
@Slf4j
@Builder
@SuppressWarnings("java:S1170")
public class KiwiRetryer<T> {

    private static final long DEFAULT_INITIAL_SLEEP_TIME_MILLISECONDS = 100;
    private static final long DEFAULT_RETRY_INCREMENT_TIME_MILLISECONDS = 200;
    private static final int DEFAULT_MAXIMUM_ATTEMPTS = 5;

    @Builder.Default
    private final String retryerId = UUIDs.randomUUIDString();

    @Builder.Default
    private final long initialSleepTimeAmount = DEFAULT_INITIAL_SLEEP_TIME_MILLISECONDS;

    @Builder.Default
    private final TimeUnit initialSleepTimeUnit = TimeUnit.MILLISECONDS;

    @Builder.Default
    private final long retryIncrementTimeAmount = DEFAULT_RETRY_INCREMENT_TIME_MILLISECONDS;

    @Builder.Default
    private final TimeUnit retryIncrementTimeUnit = TimeUnit.MILLISECONDS;

    @Builder.Default
    private final int maxAttempts = DEFAULT_MAXIMUM_ATTEMPTS;

    @Builder.Default
    private final Level processingLogLevel = Level.DEBUG;

    @Builder.Default
    private final Level exceptionLogLevel = Level.WARN;

    private final boolean retryOnAllExceptions;

    private final boolean retryOnAllRuntimeExceptions;

    @Singular
    private final List<Predicate<Throwable>> exceptionPredicates;

    @Singular
    private final List<Predicate<T>> resultPredicates;

    private final StopStrategy stopStrategy;

    private final WaitStrategy waitStrategy;

    /**
     * Create a new instance with only the default values.
     *
     * @param <T> type of object the retryer returns
     * @return a KiwiRetryer for type {@code T}
     */
    public static <T> KiwiRetryer<T> newRetryerWithDefaults() {
        return KiwiRetryer.<T>builder().build();
    }

    /**
     * Create a new instance with several common network-related exception predicates.
     *
     * @param retryerId the retryer ID
     * @param <T>       type of object the retryer returns
     * @return a KiwiRetryer for type {@code T}
     * @see KiwiRetryerPredicates#CONNECTION_ERROR
     * @see KiwiRetryerPredicates#NO_ROUTE_TO_HOST
     * @see KiwiRetryerPredicates#SOCKET_TIMEOUT
     * @see KiwiRetryerPredicates#UNKNOWN_HOST
     */
    public static <T> KiwiRetryer<T> newRetryerWithDefaultExceptions(String retryerId) {
        return KiwiRetryer.<T>builder()
                .retryerId(retryerId)
                .exceptionPredicate(CONNECTION_ERROR)
                .exceptionPredicate(NO_ROUTE_TO_HOST)
                .exceptionPredicate(SOCKET_TIMEOUT)
                .exceptionPredicate(UNKNOWN_HOST)
                .build();
    }

    /**
     * Create a new instance that will retry on <em>all</em> exceptions.
     *
     * @param retryerId the retryer ID
     * @param <T>       type of object the retryer returns
     * @return a KiwiRetryer for type {@code T}
     */
    public static <T> KiwiRetryer<T> newRetryerRetryingAllExceptions(String retryerId) {
        return KiwiRetryer.<T>builder()
                .retryerId(retryerId)
                .retryOnAllExceptions(true)
                .build();
    }

    /**
     * Create a new instance that will retry on <em>all</em> runtime exceptions.
     *
     * @param retryerId the retryer ID
     * @param <T>       type of object the retryer returns
     * @return a KiwiRetryer for type {@code T}
     */
    public static <T> KiwiRetryer<T> newRetryerRetryingAllRuntimeExceptions(String retryerId) {
        return KiwiRetryer.<T>builder()
                .retryerId(retryerId)
                .retryOnAllRuntimeExceptions(true)
                .build();
    }

    /**
     * Invoke the retryer with the given {@link Callable}.
     *
     * @param callable the code that attempts to produce a result
     * @return the result of the {@link Callable}
     * @throws KiwiRetryerException if there was an unhandled exception during processing, or if the maximum
     *                              number of attempts was reached without success. For further information about the cause, you can
     *                              unwrap the exception.
     * @see KiwiRetryerException#unwrapKiwiRetryerException(KiwiRetryerException)
     * @see KiwiRetryerException#unwrapKiwiRetryerExceptionFully(KiwiRetryerException)
     */
    public T call(Callable<T> callable) {
        return call(retryerId, callable);
    }

    /**
     * Invoke the retryer with the given ID and {@link Callable}.
     * <p>
     * This method allows you to use different IDs with the same {@link KiwiRetryer} instance, for example if
     * the same retryer is called in separate threads it will be useful to be able to distinguish between them
     * in logs.
     *
     * @param retryerId the ID for this retryer call (overrides the {@code retryerId} of this instance)
     * @param callable  the code that attempts to produce a result
     * @return the result of the {@link Callable}
     * @throws KiwiRetryerException if there was an unhandled exception during processing, or if the maximum
     *                              number of attempts was reached without success. For further information about the cause, you can
     *                              unwrap the exception.
     * @see KiwiRetryerException#unwrapKiwiRetryerException(KiwiRetryerException)
     * @see KiwiRetryerException#unwrapKiwiRetryerExceptionFully(KiwiRetryerException)
     */
    public T call(String retryerId, Callable<T> callable) {
        try {
            var retryer = buildRetryer(retryerId);
            LOG.debug("Calling retryer with id: {}", retryerId);
            return retryer.call(callable);
        } catch (RetryException e) {
            var message = f("KiwiRetryer {} failed all {} attempts. Error: {}",
                    retryerId, e.getNumberOfFailedAttempts(), e.getMessage());
            throw new KiwiRetryerException(message, e);
        } catch (ExecutionException e) {
            var message = f("KiwiRetryer {} failed making call. Wrapped exception: {}",
                    retryerId, e.getCause());
            throw new KiwiRetryerException(message, e);
        }
    }

    private Retryer<T> buildRetryer(String retryerId) {
        var theWaitStrategy = determineWaitStrategy();
        var theStopStrategy = determineStopStrategy();
        var theLogListener = new LoggingRetryListener(retryerId, processingLogLevel, exceptionLogLevel);

        // suppress unstable API warning because RetryListener is marked with @Beta and we're ignoring that fact
        @SuppressWarnings("UnstableApiUsage")
        var retryerBuilder = RetryerBuilder.<T>newBuilder()
                .withWaitStrategy(theWaitStrategy)
                .withStopStrategy(theStopStrategy)
                .withRetryListener(theLogListener);

        if (retryOnAllExceptions) {
            logIfRetryOnRuntimeExceptionsIsSet();
            logIfExceptionPredicatesIsNotEmpty();

            retryerBuilder.retryIfException();
        } else if (retryOnAllRuntimeExceptions) {
            logIfExceptionPredicatesIsNotEmpty();

            retryerBuilder.retryIfRuntimeException();
        } else {
            exceptionPredicates.forEach(retryerBuilder::retryIfException);
        }

        resultPredicates.forEach(retryerBuilder::retryIfResult);

        return retryerBuilder.build();
    }

    private void logIfRetryOnRuntimeExceptionsIsSet() {
        if (retryOnAllRuntimeExceptions) {
            LOG.warn("Both retryOnAllExceptions and retryOnAllRuntimeExceptions are set;" +
                    " retryOnAllExceptions takes precedence");
        }
    }

    private void logIfExceptionPredicatesIsNotEmpty() {
        if (isNotNullOrEmpty(exceptionPredicates)) {
            var field = retryOnAllExceptions ? "retryOnAllExceptions" : "retryOnAllRuntimeExceptions";
            LOG.warn("{} is set while exceptionPredicates is populated: {} takes precedence", field, field);
        }
    }

    private WaitStrategy determineWaitStrategy() {
        return Optional.ofNullable(waitStrategy).orElseGet(this::newIncrementingWaitStrategy);
    }

    private WaitStrategy newIncrementingWaitStrategy() {
        return WaitStrategies.incrementingWait(
                initialSleepTimeAmount, initialSleepTimeUnit,
                retryIncrementTimeAmount, retryIncrementTimeUnit
        );
    }

    private StopStrategy determineStopStrategy() {
        return Optional.ofNullable(stopStrategy)
                .orElseGet(() -> StopStrategies.stopAfterAttempt(maxAttempts));
    }

    @SuppressWarnings("UnstableApiUsage")  // because RetryListener is marked with @Beta and we're ignoring that fact
    static class LoggingRetryListener implements RetryListener {

        private static final String RETRY_ATTEMPT_MSG = "Retryer [{}], attempt #{} [delay since first attempt: {} ms]";
        private static final String RESULT_MSG = "Result for retryer [{}]: {}";
        private static final String EXCEPTION_MESSAGE = "Exception occurred for retryer [{}]: {}: {}";

        private final String retryId;
        private final Level processingLogLevel;
        private final Level exceptionLogLevel;

        LoggingRetryListener(String id, Level processingLogLevel, Level exceptionLogLevel) {
            this.retryId = StringUtils.isBlank(id) ? UUIDs.randomUUIDString() : id;
            this.processingLogLevel = processingLogLevel;
            this.exceptionLogLevel = exceptionLogLevel;
        }

        /**
         * @implNote The {@link Attempt} should have either a result or an exception, but we are being safe here
         * and explicitly checking both of those cases instead of making any assumptions.
         */
        @Override
        public <V> void onRetry(Attempt<V> attempt) {
            var attemptNumber = attempt.getAttemptNumber();

            RetryLogger.logAttempt(LOG, processingLogLevel, attemptNumber,
                    RETRY_ATTEMPT_MSG, retryId, attemptNumber, attempt.getDelaySinceFirstAttempt());

            if (attempt.hasResult()) {
                logResultAttempt(attempt, attemptNumber);
            } else if (attempt.hasException()) {
                logExceptionAttempt(attempt);
            }
        }

        <V> void logResultAttempt(Attempt<V> attempt, long attemptNumber) {
            RetryLogger.logAttempt(LOG, processingLogLevel, attemptNumber, RESULT_MSG, retryId, attempt.getResult());
        }

        /**
         * Log all exceptions at exceptionLogLevel (only type and message, not stack trace).
         *
         * @implNote the throwable in attempt should never be null, but we guard against just in case
         */
        <V> void logExceptionAttempt(Attempt<V> attempt) {
            var throwable = attempt.getExceptionCause();

            var type = KiwiThrowables.typeOfNullable(throwable).orElse(null);
            var message = KiwiThrowables.messageOfNullable(throwable).orElse(null);
            RetryLogger.logAttempt(LOG, exceptionLogLevel, EXCEPTION_MESSAGE, retryId, type, message);
        }
    }
}