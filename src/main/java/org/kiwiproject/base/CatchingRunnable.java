package org.kiwiproject.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link Runnable} that never lets exceptions escape. Useful for things like scheduled executions
 * using {@link java.util.concurrent.ScheduledExecutorService} where an intermittent error should not cause the
 * executor to suppress future executions (which is the default behavior).
 */
@FunctionalInterface
public interface CatchingRunnable extends Runnable {

    /**
     * Wraps {@link #runSafely()} in a try/catch. Logs exceptions and will call {@link #handleExceptionSafely(Throwable)}
     * to permit handling of any thrown exceptions.
     */
    @Override
    @SuppressWarnings("java:S1181")
    default void run() {
        try {
            runSafely();
        } catch (Throwable e) {
            getLogger().error("Error occurred calling runSafely", e);

            try {
                handleExceptionSafely(e);
            } catch (Throwable ex) {
                getLogger().error("Error occurred calling handleExceptionSafely", ex);
            }
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(CatchingRunnable.class);
    }

    /**
     * Handle an exception thrown by {@link #runSafely()}.
     */
    default void handleExceptionSafely(Throwable throwable) {
        // no-op by default; override if desired
    }

    /**
     * The logic that could throw a {@link RuntimeException}.
     */
    void runSafely();
}
