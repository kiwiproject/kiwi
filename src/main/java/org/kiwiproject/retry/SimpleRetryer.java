package org.kiwiproject.retry;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.slf4j.event.Level;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A simple class to retry an operation up to a maximum number of attempts. Uses the same set of initial values for
 * maximum number of attempts, delay between attempts, etc. Consider using this rather than {@link SimpleRetries}
 * directly.
 * <p>
 * You can construct a {@link SimpleRetryer} using the builder obtained via {@code SimpleRetryer.builder()}.
 * <table>
 *     <caption>Available configuration options for SimpleRetryer:</caption>
 *      <tr>
 *         <th>Name</th>
 *         <th>Default</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *          <td>environment</td>
 *          <td>a {@link DefaultEnvironment} instance</td>
 *          <td>mainly useful for testing, e.g. to supply a mock</td>
 *     </tr>
 *     <tr>
 *         <td>maxAttempts</td>
 *         <td>{@link #DEFAULT_MAX_ATTEMPTS}</td>
 *         <td>the maximum number of attempts to make before giving up</td>
 *     </tr>
 *     <tr>
 *         <td>retryDelayTime</td>
 *         <td>{@link #DEFAULT_RETRY_DELAY_TIME}</td>
 *         <td>the time value to wait between attempts</td>
 *     </tr>
 *     <tr>
 *         <td>retryDelayUnit</td>
 *         <td>{@link #DEFAULT_RETRY_DELAY_UNIT}</td>
 *         <td>the time unit for {@code retryDelayTime}</td>
 *     </tr>
 *     <tr>
 *         <td>commonType</td>
 *         <td>{@link #DEFAULT_TYPE}</td>
 *         <td>use this to specify a common type/description that retryer will get (used in log messages)</td>
 *     </tr>
 *     <tr>
 *         <td>logLevelForSubsequentAttempts</td>
 *         <td>{@link #DEFAULT_RETRY_LOG_LEVEL}</td>
 *         <td>the log level at which retries will be logged (the first attempt is always logged at TRACE)</td>
 *     </tr>
 * </table>
 *
 * @implNote This is basically an instance wrapper around {@link SimpleRetries} to allow for specification of
 * common configuration, and thus avoid calling methods with many parameters. It also facilitates easy mocking in
 * tests as opposed to the static methods in {@link SimpleRetries}.
 */
@Builder
public class SimpleRetryer {

    /**
     * Default maximum attempts.
     */
    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * Default retry delay time. This is a static value, i.e., there is no fancy exponential or linear backoff.
     */
    public static final long DEFAULT_RETRY_DELAY_TIME = 50;

    /**
     * Default retry delay time unit.
     */
    public static final TimeUnit DEFAULT_RETRY_DELAY_UNIT = TimeUnit.MILLISECONDS;

    /**
     * Default value to include in attempt log messages.
     */
    public static final String DEFAULT_TYPE = "object";

    /**
     * Default value of the log level to use when logging retry attempts.
     */
    public static final Level DEFAULT_RETRY_LOG_LEVEL = Level.TRACE;

    /**
     * The {@link KiwiEnvironment} to use when sleeping between retry attempts.
     */
    @VisibleForTesting
    @Builder.Default
    final KiwiEnvironment environment = new DefaultEnvironment();

    /**
     * The maximum number of attempts before giving up.
     */
    @VisibleForTesting
    @Builder.Default
    final int maxAttempts = DEFAULT_MAX_ATTEMPTS;

    /**
     * The time to sleep between retry attempts.
     */
    @VisibleForTesting
    @Builder.Default
    final long retryDelayTime = DEFAULT_RETRY_DELAY_TIME;

    /**
     * The time unit for the time to sleep between retry attempts.
     */
    @VisibleForTesting
    @Builder.Default
    final TimeUnit retryDelayUnit = DEFAULT_RETRY_DELAY_UNIT;

    /**
     * The common type/description to include in log messages for each attempt.
     */
    @VisibleForTesting
    @Builder.Default
    final String commonType = DEFAULT_TYPE;

    /**
     * The SLF4J log level to use when logging retry attempts. The first attempt is always logged at TRACE level.
     */
    @VisibleForTesting
    @Builder.Default
    final Level logLevelForSubsequentAttempts = DEFAULT_RETRY_LOG_LEVEL;

    /**
     * Try to get an object.
     *
     * @param supplier on success return the object; return {@code null} or throw exception if the attempt failed
     * @param <T>      the type of object
     * @return an Optional which either contains a value or is empty if all attempts failed
     */
    public <T> Optional<T> tryGetObject(Supplier<T> supplier) {
        return tryGetObject(commonType, supplier);
    }

    /**
     * Try to get an object.
     *
     * @param type     the type of object to return, used only in logging messages
     * @param supplier on success return the object; return {@code null} or throw exception if the attempt failed
     * @param <T>      the type of object
     * @return an Optional which either contains a value or is empty if all attempts failed
     */
    public <T> Optional<T> tryGetObject(Class<T> type, Supplier<T> supplier) {
        return tryGetObject(type.getSimpleName(), supplier);
    }

    /**
     * Try to get an object.
     *
     * @param type     the type of object to return, used only in logging messages
     * @param supplier on success return the object; return {@code null} or throw exception if the attempt failed
     * @param <T>      the type of object
     * @return an Optional which either contains a value or is empty if all attempts failed
     */
    public <T> Optional<T> tryGetObject(String type, Supplier<T> supplier) {
        return SimpleRetries.tryGetObject(
                maxAttempts,
                retryDelayTime, retryDelayUnit,
                environment,
                type,
                logLevelForSubsequentAttempts,
                supplier);
    }

    /**
     * Try to get an object.
     *
     * @param supplier on success return the object; return {@code null} or throw exception if the attempt failed
     * @param <T>      the type of object
     * @return a {@link RetryResult}
     */
    public <T> RetryResult<T> tryGetObjectCollectingErrors(Supplier<T> supplier) {
        return tryGetObjectCollectingErrors(commonType, supplier);
    }

    /**
     * Try to get an object.
     *
     * @param type     the type of object to return, used only in logging messages
     * @param supplier on success return the object; return {@code null} or throw exception if the attempt failed
     * @param <T>      the type of object
     * @return a {@link RetryResult}
     */
    public <T> RetryResult<T> tryGetObjectCollectingErrors(Class<T> type, Supplier<T> supplier) {
        return tryGetObjectCollectingErrors(type.getSimpleName(), supplier);
    }

    /**
     * Try to get an object.
     *
     * @param type     the type of object to return, used only in logging messages
     * @param supplier on success return the object; return {@code null} or throw exception if the attempt failed
     * @param <T>      the type of object
     * @return a {@link RetryResult}
     */
    public <T> RetryResult<T> tryGetObjectCollectingErrors(String type, Supplier<T> supplier) {
        return SimpleRetries.tryGetObjectCollectingErrors(
                maxAttempts,
                retryDelayTime, retryDelayUnit,
                environment,
                type,
                supplier
        );
    }
}
