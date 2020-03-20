package org.kiwiproject.concurrent;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
 * permitting selective (e.g. in unit tests) forcing of synchronous behavior for things that would normally
 * execute asynchronously. This applies only to some methods, so read the method's documentation before assuming.
 * <p>
 * Use the {@code doAsync} methods when you need to control concurrent behavior and make things deterministic
 * during unit tests (e.g. blocking on futures). Note this does actually change the true behavior of the code under
 * test, since methods will execute synchronously, so use with care, caution, and understanding.
 *
 * @implNote the "asyncMode" flag is a STATIC variable and should only ever be changed during testing using the
 * {@link #setUnitTestAsyncMode(Mode)} method. Generally, you should set this before tests and reset after
 * they have run. Note also this could cause unexpected behavior if tests are run in parallel.
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
        if (mode == Mode.DISABLED) {
            LOG.warn("===================================================================");
            LOG.warn("------------ DISABLING ASYNC IS FOR TEST USE ONLY -----------------");
            LOG.warn("===================================================================");
        }

        Async.asyncMode = mode;
    }

    /**
     * Execute the given {@link Runnable} asynchronously, returning a {@link CompletableFuture} with no result. This
     * uses the common fork join pool as the executor.
     *
     * @see ForkJoinPool#commonPool()
     * @see #doAsync(Runnable, Executor)
     */
    public static CompletableFuture<Void> doAsync(Runnable func) {
        return doAsync(func, ForkJoinPool.commonPool());
    }

    /**
     * Execute the given {@link Runnable} asynchronously using the given {@link Executor}, returning a
     * {@link CompletableFuture} with no result.
     * <p>
     * Essentially, wraps {@link CompletableFuture#runAsync(Runnable, Executor)} but allowing synchronous behavior
     * if mode is {@link Mode#DISABLED}.
     *
     * @see CompletableFuture#runAsync(Runnable, Executor)
     */
    public static CompletableFuture<Void> doAsync(Runnable func, Executor executor) {
        return waitIfAsyncDisabled(CompletableFuture.runAsync(func, executor));
    }

    /**
     * Execute the given {@link Supplier} asynchronously to return a result, using the common fork join pool
     * as the executor.
     *
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
     * * if mode is {@link Mode#DISABLED}.
     *
     * @see ForkJoinPool#commonPool()
     * @see CompletableFuture#supplyAsync(Supplier, Executor)
     */
    public static <T> CompletableFuture<T> doAsync(Supplier<T> supplier, Executor executor) {
        return waitIfAsyncDisabled(CompletableFuture.supplyAsync(supplier, executor));
    }

    @VisibleForTesting
    static <T> CompletableFuture<T> waitIfAsyncDisabled(CompletableFuture<T> future) {
        if (asyncMode == Mode.DISABLED) {
            LOG.trace("asyncMode = DISABLED; wait for the future!");
            try {
                future.get();
            } catch (Exception e) {
                LOG.error("Encountered error waiting for future: ", e);
            }
        }

        future.whenComplete(Async::logException);
        return future;
    }

    private static <T> void logException(T result, Throwable thrown) {
        if (nonNull(thrown)) {
            LOG.error("Encountered exception in async task: {}", thrown.getMessage());
            LOG.debug("Exception details", thrown);
        }
    }

    /**
     * Helper method that waits for a {@link CompletableFuture} up to a specified timeout.
     * <p>
     * Note that {@link Mode} has no effect on this method.
     *
     * @throws AsyncException if any error occurs during asynchronous code execution
     */
    public static <T> void waitFor(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        waitForAll(List.of(future), timeout, unit);
    }

    /**
     * Helper method that waits for a collection of {@link CompletableFuture} of type {@code T} up to a
     * specified timeout.
     * <p>
     * Note that {@link Mode} has no effect on this method.
     *
     * @throws AsyncException if any error occurs during asynchronous code execution
     */
    @SuppressWarnings("DuplicatedCode")
    public static <T> void waitForAll(Collection<CompletableFuture<T>> futures, long timeout, TimeUnit unit) {
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get(timeout, unit);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logAndThrowAsyncException(timeout, unit, ex, null);
        } catch (ExecutionException | TimeoutException ex) {
            logAndThrowAsyncException(timeout, unit, ex, null);
        }
    }

    /**
     * Helper method that waits for a collection of {@link CompletableFuture} with no explicit type up to a
     * specified timeout.
     * <p>
     * Note that {@link Mode} has no effect on this method.
     *
     * @throws AsyncException if any error occurs during asynchronous code execution
     */
    @SuppressWarnings({"DuplicatedCode", "rawtypes"})
    public static void waitForAllIgnoringType(Collection<CompletableFuture> futures, long timeout, TimeUnit unit) {
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get(timeout, unit);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logAndThrowAsyncException(timeout, unit, ex, null);
        } catch (ExecutionException | TimeoutException ex) {
            logAndThrowAsyncException(timeout, unit, ex, null);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void logAndThrowAsyncException(long timeout,
                                                  TimeUnit unit,
                                                  Exception ex,
                                                  CompletableFuture future) {

        var msg = f("Timeout occurred: maximum wait specified as {} {}", timeout, unit);
        LOG.error(msg, ex);
        throw new AsyncException(msg, ex, future);
    }

    /**
     * Wraps a {@link CompletableFuture} with a timeout so that it can proceed asynchronously, but still have
     * a maximum duration. Uses the common fork join pool as the {@link ExecutorService}.
     * <p>
     * Note that {@link Mode} has no effect on this method.
     *
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
     * Note that {@link Mode} has no effect on this method.
     *
     * @throws AsyncException if any error occurs during asynchronous code execution, including timeout
     * @see CompletableFuture#supplyAsync(Supplier, Executor)
     */
    public static <T> CompletableFuture<T> withMaxTimeout(CompletableFuture<T> future,
                                                          long timeout,
                                                          TimeUnit unit,
                                                          ExecutorService executor) {
        Supplier<T> supplier = () -> {
            try {
                return future.get(timeout, unit);
            } catch (Exception ex) {
                future.cancel(true);
                logAndThrowAsyncException(timeout, unit, ex, future);

                return null;  // should not reach here but compiler insists (it can't infer the exception thrown)
            }
        };

        return CompletableFuture.supplyAsync(supplier, executor);
    }
}
