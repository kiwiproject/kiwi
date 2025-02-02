package org.kiwiproject.concurrent;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * This helper class wraps the static utility functions in {@link Async}.
 * You need to create an instance of this class to use it. The main reason
 * to use this instead of {@link Async} directly is to have more control
 * over disabling asynchronous behavior, which should usually only be done
 * in test code.
 * <p>
 * By default, all operations are asynchronous like {@link Async}. You can call
 * {@link #setUnitTestAsyncMode(Async.Mode)} with {@link Async.Mode#DISABLED}
 * to force synchronous behavior when calling {@link AsyncHelper} methods. Also like
 * {@link Async}, some methods ignore {@link Async.Mode}, and is noted in their Javadocs.
 * <p>
 * Using an instance of this class instead of {@link Async} directly will make it much
 * easier to test asynchronous code. As mentioned above, you can set the {@link Async.Mode}
 * to {@link Async.Mode#DISABLED} before each test to execute the code synchronously in
 * tests. Unlike using {@link Async} directly in tests, there are no problems using
 * {@link AsyncHelper} with multiple threads or parallel test execution. You can also
 * choose to use a mock {@link AsyncHelper} in tests and specify the desired behavior,
 * which means you are bypassing the asynchronous calls entirely. This may be appropriate
 * in some situations, for example, when the asynchronous behavior has been tested
 * in other tests, and you want to simplify test code. As always, the choice depends on
 * the situation.
 *
 * @see Async
 */
@Slf4j
public class AsyncHelper {

    private Async.Mode asyncMode = Async.Mode.ENABLED;

    /**
     * Use for testing purposes to force synchronous behavior.
     *
     * @param mode enable or disable asynchronous behavior
     */
    public void setUnitTestAsyncMode(Async.Mode mode) {
        checkArgumentNotNull(mode, "mode cannot be null");
        Async.logWarningWhenAsyncDisabled(mode);

        this.asyncMode = mode;
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
    public CompletableFuture<Void> runAsync(Runnable func) {
        return doAsync(func);
    }

    /**
     * Execute the given {@link Runnable} asynchronously using the given {@link Executor}.
     * <p>
     * Essentially, wraps {@link CompletableFuture#runAsync(Runnable, Executor)} but allowing synchronous behavior
     * if mode is {@link Async.Mode#DISABLED}.
     * <p>
     * This is an alias method for {@link #doAsync(Runnable, Executor)} to provide a way to avoid ambiguity in certain
     * situations.
     *
     * @param func     the code to run asynchronously
     * @param executor the {@link Executor} to use
     * @return a {@link CompletableFuture} with no result
     * @see CompletableFuture#runAsync(Runnable, Executor)
     */
    public CompletableFuture<Void> runAsync(Runnable func, Executor executor) {
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
    public CompletableFuture<Void> doAsync(Runnable func) {
        return doAsync(func, ForkJoinPool.commonPool());
    }

    /**
     * Execute the given {@link Runnable} asynchronously using the given {@link Executor}.
     * <p>
     * Essentially, wraps {@link CompletableFuture#runAsync(Runnable, Executor)} but allowing synchronous behavior
     * if mode is {@link Async.Mode#DISABLED}.
     *
     * @param func     the code to run asynchronously
     * @param executor the {@link Executor} to use
     * @return a {@link CompletableFuture} with no result
     * @see CompletableFuture#runAsync(Runnable, Executor)
     */
    public CompletableFuture<Void> doAsync(Runnable func, Executor executor) {
        return Async.waitIfAsyncDisabled(CompletableFuture.runAsync(func, executor), asyncMode);
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
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return doAsync(supplier);
    }

    /**
     * Execute the given {@link Supplier} asynchronously to return a result, using the common fork join pool
     * as the executor.
     * <p>
     * Essentially, wraps {@link CompletableFuture#supplyAsync(Supplier, Executor)} but allowing synchronous behavior
     * if mode is {@link Async.Mode#DISABLED}.
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
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
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
    public <T> CompletableFuture<T> doAsync(Supplier<T> supplier) {
        return doAsync(supplier, ForkJoinPool.commonPool());
    }

    /**
     * Execute the given {@link Supplier} asynchronously to return a result, using the common fork join pool
     * as the executor.
     * <p>
     * Essentially, wraps {@link CompletableFuture#supplyAsync(Supplier, Executor)} but allowing synchronous behavior
     * if mode is {@link Async.Mode#DISABLED}.
     *
     * @param supplier the code to run asynchronously
     * @param executor the {@link Executor} to use
     * @param <T>      the type of object being supplied
     * @return the result returned by the supplier
     * @see ForkJoinPool#commonPool()
     * @see CompletableFuture#supplyAsync(Supplier, Executor)
     */
    public <T> CompletableFuture<T> doAsync(Supplier<T> supplier, Executor executor) {
        return Async.waitIfAsyncDisabled(CompletableFuture.supplyAsync(supplier, executor), asyncMode);
    }

    /**
     * Helper method that waits for a {@link CompletableFuture} up to a specified timeout.
     * <p>
     * <em>Note that {@link Async.Mode} has no effect on this method.</em>
     *
     * @param future  the CompletableFuture to wait for
     * @param timeout the value of the timeout in the given unit
     * @param unit    the time unit to use
     * @param <T>     the result returned by the future
     * @throws AsyncException if any error occurs during asynchronous code execution
     */
    public <T> void waitFor(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        Async.waitFor(future, timeout, unit);
    }

    /**
     * Helper method that waits for a collection of {@link CompletableFuture} of type {@code T} up to a
     * specified timeout.
     * <p>
     * <em>Note that {@link Async.Mode} has no effect on this method.</em>
     *
     * @param futures the CompletableFuture instances to wait for
     * @param timeout the value of the timeout in the given unit
     * @param unit    the time unit to use
     * @param <T>     the result returned by the futures
     */
    public <T> void waitForAll(Collection<CompletableFuture<T>> futures, long timeout, TimeUnit unit) {
        Async.waitForAll(futures, timeout, unit);
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
    @SuppressWarnings({ "rawtypes" })
    public void waitForAllIgnoringType(Collection<CompletableFuture> futures, long timeout, TimeUnit unit) {
        Async.waitForAllIgnoringType(futures, timeout, unit);
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
    public <T> CompletableFuture<T> withMaxTimeout(CompletableFuture<T> future,
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
    public <T> CompletableFuture<T> withMaxTimeout(CompletableFuture<T> future,
                                                   long timeout,
                                                   TimeUnit unit,
                                                   ExecutorService executor) {
        return Async.withMaxTimeout(future, timeout, unit, executor);
    }
}
