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
     * Wraps {@link #runSafely()} in a try/catch. Logs exceptions and will call {@link #handleExceptionSafely(Exception)}
     * to permit handling of any thrown exceptions.
     */
    @Override
    default void run() {
        try {
            runSafely();
        } catch (Exception e) {
            getLogger().error("Error occurred calling runSafely", e);

            try {
                handleExceptionSafely(e);
            } catch (Exception ex) {
                getLogger().error("Error occurred calling handleExceptionSafely", ex);
            }
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(CatchingRunnable.class);
    }

    /**
     * Handle an exception thrown by {@link #runSafely()}.
     *
     * @param exception the {@link Exception} to handle
     */
    default void handleExceptionSafely(Exception exception) {
        // no-op by default; override if desired
    }

    /**
     * The logic that could throw a {@link RuntimeException}.
     */
    void runSafely();
}
