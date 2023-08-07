package org.kiwiproject.retry;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.slf4j.event.Level.TRACE;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Static utilities for retrying an operation. The {@link Supplier} passed to each method must indicate success or
 * failure. Success is indicated by returning a non-null value. Failure is indicated by returning {@code null} or
 * by throwing an exception. Each time a failure occurs, the code sleeps for the specified delay time, and then
 * another attempt will be made unless the maximum number of attempts has been reached.
 * <p>
 * While you can use this directly, consider using {@link SimpleRetryer}, which is more flexible because (1) you
 * can easily mock it in tests, and (2) it accepts common configuration options and makes the method calls
 * simpler because there are many fewer arguments.
 */
@SuppressWarnings("WeakerAccess")
@UtilityClass
@Slf4j
public class SimpleRetries {

    private static final KiwiEnvironment DEFAULT_KIWI_ENV = new DefaultEnvironment();

    private static final String ATTEMPT_MSG_TEMPLATE = "Attempt {} of {} to obtain a(n) {} from supplier";

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE level.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return an Optional which either contains a value, or is empty if all attempts failed
     */
    public static <T> Optional<T> tryGetObject(int maxAttempts,
                                               long retryDelay, TimeUnit retryDelayUnit,
                                               Supplier<T> supplier) {
        return tryGetObject(maxAttempts, retryDelay, retryDelayUnit, DEFAULT_KIWI_ENV, supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE level
     * using "object" as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param environment    the {@link KiwiEnvironment} to use when sleeping between attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return an Optional which either contains a value, or is empty if all attempts failed
     */
    public static <T> Optional<T> tryGetObject(int maxAttempts,
                                               long retryDelay, TimeUnit retryDelayUnit,
                                               KiwiEnvironment environment,
                                               Supplier<T> supplier) {
        return tryGetObject(maxAttempts, retryDelay, retryDelayUnit, environment, "object", TRACE, supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE level
     * using the given {@code type} as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param type           the type of object we are attempting to return, used when logging attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return an Optional which either contains a value, or is empty if all attempts failed
     */
    public static <T> Optional<T> tryGetObject(int maxAttempts,
                                               long retryDelay, TimeUnit retryDelayUnit,
                                               Class<T> type,
                                               Supplier<T> supplier) {
        return tryGetObject(maxAttempts, retryDelay, retryDelayUnit, DEFAULT_KIWI_ENV, type, supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE
     * level, using the given {@code type} as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param environment    the {@link KiwiEnvironment} to use when sleeping between attempts
     * @param type           the type of object we are attempting to return, used when logging attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return an Optional which either contains a value, or is empty if all attempts failed
     */
    public static <T> Optional<T> tryGetObject(int maxAttempts,
                                               long retryDelay, TimeUnit retryDelayUnit,
                                               KiwiEnvironment environment,
                                               Class<T> type,
                                               Supplier<T> supplier) {
        return tryGetObject(maxAttempts, retryDelay, retryDelayUnit, environment, type.getSimpleName(), TRACE, supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt at TRACE level, and logs
     * retries at the given {@code level}, always using {@code type} as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param environment    the {@link KiwiEnvironment} to use when sleeping between attempts
     * @param type           the type of object we are attempting to return, used when logging attempts
     * @param level          the SLF4J log {@link Level} at which to log retries
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return an Optional which either contains a value, or is empty if all attempts failed
     */
    public static <T> Optional<T> tryGetObject(int maxAttempts,
                                               long retryDelay, TimeUnit retryDelayUnit,
                                               KiwiEnvironment environment,
                                               String type,
                                               Level level,
                                               Supplier<T> supplier) {

        return IntStream.rangeClosed(1, maxAttempts)
                .mapToObj(objectOrNull(maxAttempts, retryDelay, retryDelayUnit, environment, type, level, supplier))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private <T> IntFunction<T> objectOrNull(int maxAttempts,
                                            long retryDelay, TimeUnit retryDelayUnit,
                                            KiwiEnvironment environment,
                                            String type,
                                            Level level,
                                            Supplier<T> supplier) {

        return currentAttempt -> {
            RetryLogger.logAttempt(LOG, level, currentAttempt, ATTEMPT_MSG_TEMPLATE, currentAttempt, maxAttempts, type);

            var object = safeGetOrNull(currentAttempt, maxAttempts, type, level, supplier);

            if (isNull(object) && currentAttempt < maxAttempts) {
                environment.sleepQuietly(retryDelay, retryDelayUnit);
            } else if (nonNull(object)) {
                traceLogResultReceived(currentAttempt, maxAttempts, type);
            }

            return object;
        };
    }

    private static <T> T safeGetOrNull(int currentAttempt,
                                       int maxAttempts,
                                       String type,
                                       Level level,
                                       Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            RetryLogger.logAttempt(LOG, level, "Error occurred on attempt {} of {} getting {} from supplier",
                    currentAttempt, maxAttempts, type, e);
            return null;
        }
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE level
     * using "object" as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return a {@link RetryResult}
     */
    public static <T> RetryResult<T> tryGetObjectCollectingErrors(int maxAttempts,
                                                                  long retryDelay, TimeUnit retryDelayUnit,
                                                                  Supplier<T> supplier) {
        return tryGetObjectCollectingErrors(maxAttempts, retryDelay, retryDelayUnit, DEFAULT_KIWI_ENV, "object", supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE level
     * using the given {@code type} as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param type           the type of object we are attempting to return, used when logging attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return a {@link RetryResult}
     */
    public static <T> RetryResult<T> tryGetObjectCollectingErrors(int maxAttempts,
                                                                  long retryDelay, TimeUnit retryDelayUnit,
                                                                  Class<T> type,
                                                                  Supplier<T> supplier) {
        return tryGetObjectCollectingErrors(maxAttempts, retryDelay, retryDelayUnit, DEFAULT_KIWI_ENV, type, supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE level
     * using the given {@code type} as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param environment    the {@link KiwiEnvironment} to use when sleeping between attempts
     * @param type           the type of object we are attempting to return, used when logging attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return a {@link RetryResult}
     */
    public static <T> RetryResult<T> tryGetObjectCollectingErrors(int maxAttempts,
                                                                  long retryDelay, TimeUnit retryDelayUnit,
                                                                  KiwiEnvironment environment,
                                                                  Class<T> type,
                                                                  Supplier<T> supplier) {
        return tryGetObjectCollectingErrors(maxAttempts, retryDelay, retryDelayUnit, environment, type.getSimpleName(), supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt and retries at TRACE level
     * using the given {@code type} as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param environment    the {@link KiwiEnvironment} to use when sleeping between attempts
     * @param type           the type of object we are attempting to return, used when logging attempts
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return a {@link RetryResult}
     */
    public static <T> RetryResult<T> tryGetObjectCollectingErrors(int maxAttempts,
                                                                  long retryDelay, TimeUnit retryDelayUnit,
                                                                  KiwiEnvironment environment,
                                                                  String type,
                                                                  Supplier<T> supplier) {
        return tryGetObjectCollectingErrors(maxAttempts, retryDelay, retryDelayUnit, environment, type, TRACE, supplier);
    }

    /**
     * Try to get an object, making up to {@code maxAttempts} attempts. Logs first attempt at TRACE level and logs
     * retries at the given {@code level}, always using {@code type} as the description.
     *
     * @param maxAttempts    the maximum number of attempts to make before giving up
     * @param retryDelay     constant delay time between attempts
     * @param retryDelayUnit delay time unit between attempts
     * @param environment    the {@link KiwiEnvironment} to use when sleeping between attempts
     * @param type           the type of object we are attempting to return, used when logging attempts
     * @param level          the SLF4J log {@link Level} at which to log retries
     * @param supplier       on success return the object; return {@code null} or throw exception if attempt failed
     * @param <T>            the type of object
     * @return a {@link RetryResult}
     */
    public static <T> RetryResult<T> tryGetObjectCollectingErrors(int maxAttempts,
                                                                  long retryDelay, TimeUnit retryDelayUnit,
                                                                  KiwiEnvironment environment,
                                                                  String type,
                                                                  Level level,
                                                                  Supplier<T> supplier) {

        List<Pair<T, Exception>> results =
                collectResults(maxAttempts, retryDelay, retryDelayUnit, environment, type, level, supplier);

        var numAttemptsMade = results.size();

        var object = results.stream()
                .filter(pair -> nonNull(pair.getLeft()))
                .map(Pair::getLeft)
                .findFirst()
                .orElse(null);

        var errors = results.stream()
                .filter(pair -> nonNull(pair.getRight()))
                .map(Pair::getRight)
                .toList();

        return new RetryResult<>(numAttemptsMade, maxAttempts, object, errors);
    }

    /**
     * This method is specifically using imperative style because:
     * <p>
     * (1) JDK 9 has a takeWhile(Predicate) method, but it only includes the stream items that match the
     * predicate, which means we would lose the first non-matching item (the one with the actual result we need).
     * <p>
     * (2) Even though the lovely StreamEx library has a takeWhileInclusive(Predicate) that takes items while
     * the predicate is matched, plus the first non-matching item, I don't want to have to include a hard dependency
     * for just one function.
     * <p>
     * (3) Copying the code from StreamEx or trying to implement it here almost certainly is not worth it when
     * considering benefits vs. costs. If we ever start needing StreamEx in a bunch of other places, then this decision
     * can be revisited.
     * <p>
     * The following is what the code would look like using StreamEx and takeWhileInclusive:
     * <pre>
     * Stream&lt;Pair&lt;T, Exception>> resultStream = IntStream.rangeClosed(1, maxAttempts)
     *     .mapToObj(currentAttempt ->
     *         resultOrErrorPair(currentAttempt, maxAttempts, retryDelay, retryDelayUnit, environment, type, level, supplier));
     *
     * return StreamEx.of(resultStream)
     *     .takeWhileInclusive(result -> isNull(result.getLeft())
     *     .collect(toList());
     * </pre>
     */
    private static <T> List<Pair<T, Exception>> collectResults(int maxAttempts,
                                                               long retryDelay, TimeUnit retryDelayUnit,
                                                               KiwiEnvironment environment,
                                                               String type,
                                                               Level level,
                                                               Supplier<T> supplier) {

        List<Pair<T, Exception>> results = new ArrayList<>();

        for (var currentAttempt = 1; currentAttempt <= maxAttempts; currentAttempt++) {
            Pair<T, Exception> resultOrError =
                    resultOrErrorPair(currentAttempt, maxAttempts, retryDelay, retryDelayUnit, environment, type, level, supplier);

            results.add(resultOrError);

            if (nonNull(resultOrError.getLeft())) {
                traceLogResultReceived(currentAttempt, maxAttempts, type);
                break;
            }
        }

        return results;
    }

    // Suppress Sonar "Methods should not have too many parameters" as this is a private method and there isn't
    // a clearly superior alternative.
    @SuppressWarnings("java:S107")
    private static <T> Pair<T, Exception> resultOrErrorPair(int currentAttempt,
                                                            int maxAttempts,
                                                            long retryDelay, TimeUnit retryDelayUnit,
                                                            KiwiEnvironment environment,
                                                            String type,
                                                            Level level,
                                                            Supplier<T> supplier) {

        RetryLogger.logAttempt(LOG, level, currentAttempt, ATTEMPT_MSG_TEMPLATE, currentAttempt, maxAttempts, type);

        T object = null;
        Exception error = null;
        try {
            object = supplier.get();
        } catch (Exception e) {
            error = e;
        }

        if (isNull(object) && currentAttempt < maxAttempts) {
            environment.sleepQuietly(retryDelay, retryDelayUnit);
        }

        return Pair.of(object, error);
    }

    private static void traceLogResultReceived(int currentAttempt, int maxAttempts, String type) {
        LOG.trace("Received a result on attempt {} of {} to get {}; no more attempts are needed",
                currentAttempt, maxAttempts, type);
    }
}
