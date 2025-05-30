package org.kiwiproject.concurrent;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.KiwiThrowables;
import org.kiwiproject.base.UUIDs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Static utilities that work with {@link java.util.concurrent.CompletableFuture} and can make testing easier by
 * permitting selective (e.g., in unit tests) forcing of synchronous behavior for things that would normally
 * execute asynchronously. This applies only to some methods, so read the method's documentation before assuming.
 * <p>
 * Use the {@code xxxAsync} methods when you need to control concurrent behavior and make things deterministic
 * during unit tests (e.g., blocking on futures). Note this does actually change the true behavior of the code under
 * test, since methods will execute synchronously, so use them with care, caution, and understanding.
 * <p>
 * In tests, you can call the {@link #setUnitTestAsyncMode(Mode)} method with {@link Mode#DISABLED} to force
 * synchronous behavior when calling {@link Async} methods. This may be desirable in certain situations to make
 * testing asynchronous code easier. Note that this is a global change, such that all calls to {@link Async} methods
 * will then behave as synchronous calls and block until the operation is complete. So, when using this capability
 * in tests, you need to be careful to reset the behavior after tests complete. This may also cause problems
 * if tests are executed in parallel, and some tests use this feature, but some do not.
 * <p>
 * The <a href="https://github.com/kiwiproject/kiwi-test">kiwi-test</a> library provides a JUnit extension,
 * <a href="https://javadoc.io/doc/org.kiwiproject/kiwi-test/latest/org/kiwiproject/test/junit/jupiter/AsyncModeDisablingExtension.html">AsyncModeDisablingExtension</a>,
 * that disables asynchronous behavior in {@link Async} before each test, then re-enables it after each test.
 * <p>
 * Alternatively, you can use {@link AsyncHelper}, which will let you control asynchronous behavior in each
 * instance or to use a mock instance in tests. Either of these avoids problems with multiple threads or
 * running tests in parallel.
 *
 * @see AsyncHelper
 * @implNote the "asyncMode" flag is a STATIC variable and should only ever be changed during testing using the
 * {@link #setUnitTestAsyncMode(Mode)} method. Generally, you should set this before tests and reset after
 * they have run. Note also this almost certainly will cause unexpected behavior if tests are run in parallel.
 */
@Slf4j
@UtilityClass
public class Async {

    /**
     * The {@link #ENABLED} mode enables asynchronous behavior, which is the default behavior. {@link #DISABLED}
     * disables all asynchronous behavior and is intended only for unit testing.
     */
    public enum Mode {

        /**
         * Asynchronous behavior (default).
         */
        ENABLED,

        /**
         * Disable asynchronous behavior (code is executed synchronously).
         */
        DISABLED
    }

    private static Mode asyncMode = Mode.ENABLED;

    /**
     * Use for testing purposes to force synchronous behavior.
     *
     * @param mode enable or disable asynchronous behavior
     */
    public static void setUnitTestAsyncMode(Mode mode) {
        checkArgumentNotNull(mode, "mode cannot be null");
        logWarningWhenAsyncDisabled(mode);

        Async.asyncMode = mode;
    }

    static void logWarningWhenAsyncDisabled(Mode mode) {
        if (mode == Mode.DISABLED) {
            LOG.warn("===================================================================");
            LOG.warn("------------ DISABLING ASYNC IS FOR TEST USE ONLY -----------------");
            LOG.warn("===================================================================");
        }
    }

    /**
     * Execute the given {@link Runnable} asynchronously. This uses the common fork join pool as the executor.
     * <p>
     * This is an alias method for {@link #doAsync(Runnable)} to provide a way to avoid ambiguity in certain
     * situations.
     *
     * @param func the code to run asynchronously
     * @return a {@link CompletableFuture} with no result
     * @see ForkJoinPool#commonPool()
     * @see #runAsync(Runnable, Executor)
     */
    public static CompletableFuture<Void> runAsync(Runnable func) {
        return doAsync(func);
    }

    /**
     * Execute the given {@link Runnable} asynchronously using the given {@link Executor}.
     * <p>
     * Essentially, wraps {@link CompletableFuture#runAsync(Runnable, Executor)} but allowing synchronous behavior
     * if mode is {@link Mode#DISABLED}.
     * <p>
     * This is an alias method for {@link #doAsync(Runnable, Executor)} to provide a way to avoid ambiguity in certain
     * situations.
     *
     * @param func     the code to run asynchronously
     * @param executor the {@link Executor} to use
     * @return a {@link CompletableFuture} with no result
     * @see CompletableFuture#runAsync(Runnable, Executor)
     */
    public static CompletableFuture<Void> runAsync(Runnable func, Executor executor) {
        return doAsync(func, executor);
    }

    /**
     * Execute the given {@link Runnable} asynchronously. This uses the common fork join pool as the executor.
     *
     * @param func the code to run asynchronously
     * @return a {@link CompletableFuture} with no result
     * @see ForkJoinPool#commonPool()
     * @see #doAsync(Runnable, Executor)
     */
    public static CompletableFuture<Void> doAsync(Runnable func) {
        return doAsync(func, ForkJoinPool.commonPool());
    }

    /**
     * Execute the given {@link Runnable} asynchronously using the given {@link Executor}.
     * <p>
     * Essentially, wraps {@link CompletableFuture#runAsync(Runnable, Executor)} but allowing synchronous behavior
     * if mode is {@link Mode#DISABLED}.
     *
     * @param func     the code to run asynchronously
     * @param executor the {@link Executor} to use
     * @return a {@link CompletableFuture} with no result
     * @see CompletableFuture#runAsync(Runnable, Executor)
     */
    public static CompletableFuture<Void> doAsync(Runnable func, Executor executor) {
        return waitIfAsyncDisabled(CompletableFuture.runAsync(func, executor));
    }

    /**
     * Execute the given {@link Supplier} asynchronously to return a result, using the common fork join pool
     * as the executor.
     * <p>
     * This is an alias method for {@link #doAsync(Supplier)} to provide a way to avoid ambiguity in certain
     * situations.
     *
     * @param supplier the code to run asynchronously
     * @param <T>      the type of object being supplied
     * @return the result returned by the supplier
     * @see ForkJoinPool#commonPool()
     * @see #doAsync(Supplier, Executor)
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return doAsync(supplier);
    }

    /**
     * Execute the given {@link Supplier} asynchronously to return a result, using the common fork join pool
     * as the executor.
     * <p>
     * Essentially, wraps {@link CompletableFuture#supplyAsync(Supplier, Executor)} but allowing synchronous behavior
     * if mode is {@link Mode#DISABLED}.
     * <p>
     * This is an alias method for {@link #doAsync(Supplier, Executor)} to provide a way to avoid ambiguity in certain
     * situations.
     *
     * @param supplier the code to run asynchronously
     * @param executor the {@link Executor} to use
     * @param <T>      the type of object being supplied
     * @return the result returned by the supplier
     * @see ForkJoinPool#commonPool()
     * @see CompletableFuture#supplyAsync(Supplier, Executor)
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        return doAsync(supplier, executor);
    }

    /**
     * Execute the given {@link Supplier} asynchronously to return a result, using the common fork join pool
     * as the executor.
     *
     * @param supplier the code to run asynchronously
     * @param <T>      the type of object being supplied
     * @return the result returned by the supplier
     * @see ForkJoinPool#commonPool()
     * @see #doAsync(Supplier, Executor)
     */
    public static <T> CompletableFuture<T> doAsync(Supplier<T> supplier) {
        return doAsync(supplier, ForkJoinPool.commonPool());
    }

    /**
     * Execute the given {@link Supplier} asynchronously to return a result, using the common fork join pool
     * as the executor.
     * <p>
     * Essentially, wraps {@link CompletableFuture#supplyAsync(Supplier, Executor)} but allowing synchronous behavior
     * if mode is {@link Mode#DISABLED}.
     *
     * @param supplier the code to run asynchronously
     * @param executor the {@link Executor} to use
     * @param <T>      the type of object being supplied
     * @return the result returned by the supplier
     * @see ForkJoinPool#commonPool()
     * @see CompletableFuture#supplyAsync(Supplier, Executor)
     */
    public static <T> CompletableFuture<T> doAsync(Supplier<T> supplier, Executor executor) {
        return waitIfAsyncDisabled(CompletableFuture.supplyAsync(supplier, executor));
    }

    @VisibleForTesting
    static <T> CompletableFuture<T> waitIfAsyncDisabled(CompletableFuture<T> future) {
        return waitIfAsyncDisabled(future, Async.asyncMode);
    }

    static <T> CompletableFuture<T> waitIfAsyncDisabled(CompletableFuture<T> future, Async.Mode mode) {
        if (mode == Mode.DISABLED) {
            LOG.warn("asyncMode = DISABLED; wait for the future!");
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.error("Interrupted while waiting for future: ", e);
            } catch (ExecutionException e) {
                LOG.error("The future completed exceptionally: ", e);
            }
        } else {
            future.whenComplete(Async::logExceptionIfPresent);
        }

        return future;
    }

    private static <T> void logExceptionIfPresent(T result, Throwable thrown) {
        if (nonNull(thrown)) {
            var errorID = UUIDs.randomUUIDString();
            var causeOptional = KiwiThrowables.nextCauseOf(thrown);
            var causeMessage = causeOptional.map(Throwable::getMessage).orElse(null);
            LOG.error("Encountered {} exception in async task (errorID: {})." +
                            " Message: {} ; Cause: {} ; Cause message: {} (enable DEBUG level to see stack traces and refer to the errorID)",
                    thrown.getClass().getName(),
                    errorID,
                    thrown.getMessage(),
                    causeOptional.orElse(null),
                    causeMessage
            );
            LOG.debug("Exception details (errorID: {}):", errorID, thrown);
        }
    }

    /**
     * Helper method that waits for a {@link CompletableFuture} up to a specified timeout.
     * <p>
     * <em>Note that {@link Mode} has no effect on this method.</em>
     *
     * @param future  the CompletableFuture to wait for
     * @param timeout the value of the timeout in the given unit
     * @param unit    the time unit to use
     * @param <T>     the result returned by the future
     * @throws AsyncException if any error occurs during asynchronous code execution
     */
    public static <T> void waitFor(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        waitForAll(List.of(future), timeout, unit);
    }

    /**
     * Helper method that waits for a collection of {@link CompletableFuture} of type {@code T} up to a
     * specified timeout.
     * <p>
     * <em>Note that {@link Mode} has no effect on this method.</em>
     *
     * @param futures the CompletableFuture instances to wait for
     * @param timeout the value of the timeout in the given unit
     * @param unit    the time unit to use
     * @param <T>     the result returned by the futures
     */
    @SuppressWarnings("DuplicatedCode")
    public static <T> void waitForAll(Collection<CompletableFuture<T>> futures, long timeout, TimeUnit unit) {
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get(timeout, unit);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw newAsyncException(timeout, unit, ex, null);
        } catch (ExecutionException | TimeoutException ex) {
            throw newAsyncException(timeout, unit, ex, null);
        }
    }

    /**
     * Helper method that waits for a collection of {@link CompletableFuture} with no explicit type up to a
     * specified timeout.
     * <p>
     * <em>Note that {@link Async.Mode} has no effect on this method.</em>
     *
     * @param futures the CompletableFuture instances to wait for
     * @param timeout the value of the timeout in the given unit
     * @param unit    the time unit to use
     * @throws AsyncException if any error occurs during asynchronous code execution
     * @implNote Suppressed the IntelliJ and Sonar warnings about raw types
     */
    @SuppressWarnings({"DuplicatedCode", "rawtypes", "java:S3740"})
    public static void waitForAllIgnoringType(Collection<CompletableFuture> futures, long timeout, TimeUnit unit) {
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get(timeout, unit);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw newAsyncException(timeout, unit, ex, null);
        } catch (ExecutionException | TimeoutException ex) {
            throw newAsyncException(timeout, unit, ex, null);
        }
    }

    /**
     * Wraps a {@link CompletableFuture} with a timeout so that it can proceed asynchronously, but still have
     * a maximum duration. Uses the common fork join pool as the {@link ExecutorService}.
     * <p>
     * <em>Note that {@link Async.Mode} has no effect on this method.</em>
     *
     * @param future  the CompletableFuture for which to apply the timeout
     * @param timeout the value of the timeout in the given unit
     * @param unit    the time unit to use
     * @param <T>     the result returned by the future
     * @return the original {@link CompletableFuture} wrapped by a new one that applies the given timeout
     * @throws AsyncException if any error occurs during asynchronous code execution, including timeout
     * @see #withMaxTimeout(CompletableFuture, long, TimeUnit, ExecutorService)
     */
    public static <T> CompletableFuture<T> withMaxTimeout(CompletableFuture<T> future,
                                                          long timeout,
                                                          TimeUnit unit) {
        return withMaxTimeout(future, timeout, unit, ForkJoinPool.commonPool());
    }

    /**
     * Wraps a {@link CompletableFuture} with a timeout so that it can proceed asynchronously, but still have
     * a maximum duration. Uses the given {@link ExecutorService}.
     * <p>
     * <em>Note that {@link Async.Mode} has no effect on this method.</em>
     *
     * @param future   the CompletableFuture for which to apply the timeout
     * @param timeout  the value of the timeout in the given unit
     * @param unit     the time unit to use
     * @param executor the {@link ExecutorService} to use
     * @param <T>      the result returned by the future
     * @return the original {@link CompletableFuture} wrapped by a new one that applies the given timeout
     * @throws AsyncException if any error occurs during asynchronous code execution, including timeout
     * @see CompletableFuture#supplyAsync(Supplier, Executor)
     */
    public static <T> CompletableFuture<T> withMaxTimeout(CompletableFuture<T> future,
                                                          long timeout,
                                                          TimeUnit unit,
                                                          ExecutorService executor) {
        Supplier<T> supplier = () -> {
            try {
                LOG.trace("Waiting up to {} {} for future {} to complete", timeout, unit, future);
                return future.get(timeout, unit);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                future.cancel(true);
                throw newAsyncException(timeout, unit, ex, future);
            } catch (ExecutionException | TimeoutException ex) {
                future.cancel(true);
                throw newAsyncException(timeout, unit, ex, future);
            }
        };

        return CompletableFuture.supplyAsync(supplier, executor);
    }

    /**
     * @implNote This method is not pure, since it has the side effect of logging the exception.
     * Since the side effect is benign, putting it here seems preferable to duplicating the same logic
     * everywhere this method is called.
     */
    private static AsyncException newAsyncException(long timeout, TimeUnit unit,
                                                    Exception ex,
                                                    CompletableFuture<?> future) {
        var msg = f("{} occurred (maximum wait was specified as {} {})",
                ex.getClass().getSimpleName(), timeout, unit);
        LOG.error(msg, ex);
        return new AsyncException(msg, ex, future);
    }
}
