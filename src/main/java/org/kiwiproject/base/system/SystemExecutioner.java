package org.kiwiproject.base.system;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.time.KiwiDurationFormatters.formatJavaDurationWords;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.DefaultEnvironment;

import java.time.Duration;

/**
 * Wrapper around {@link System#exit(int)} for situations in which the JVM must be exited. This class is mainly
 * intended for scenarios in which production code may call {@link System#exit(int)} in order to allow unit testing
 * of the exit behavior, but without actually exiting the JVM. For example, a Jetty server that cannot start due to
 * failure to obtain a port, or any other non-recoverable startup error that would otherwise result in a hung or
 * unresponsive server, application, resource, etc. In situations like this, you can test that the
 * {@link SystemExecutioner} would have exited when specific errors occur.
 * <p>
 * The no-args constructor uses the {@link ExecutionStrategies.SystemExitExecutionStrategy}, which uses
 * {@link System#exit(int)} to terminate the JVM. You can supply your own {@link ExecutionStrategy} as well, for
 * example {@link ExecutionStrategies.NoOpExecutionStrategy} is useful in unit tests (so it doesn't actually terminate
 * the JVM), and {@link ExecutionStrategies.ExitFlaggingExecutionStrategy} is useful in integration tests where you
 * want to verify the exit code without terminating the JVM.
 */
@Slf4j
public class SystemExecutioner {

    /**
     * The execution strategy to use when one of the {@code exit} methods is called.
     */
    @Getter
    private final ExecutionStrategy executionStrategy;

    /**
     * Creates a new {@link SystemExecutioner} using the default {@link ExecutionStrategy}, which is
     * {@link ExecutionStrategies.SystemExitExecutionStrategy}.
     */
    public SystemExecutioner() {
        this(ExecutionStrategies.systemExit());
    }

    /**
     * Creates a new {@link SystemExecutioner} using the given {@link ExecutionStrategy}.
     *
     * @param executionStrategy the strategy to use
     */
    public SystemExecutioner(ExecutionStrategy executionStrategy) {
        this.executionStrategy = requireNotNull(executionStrategy, "executionStrategy must not be null");
    }

    /**
     * Exits immediately with the given exit code.
     *
     * @param exitCode the exit code, following the same conventions as {@link System#exit(int)}
     */
    public void exit(int exitCode) {
        executionStrategy.exit(exitCode);
    }

    /**
     * Waits the given duration, then exits with the given exit code.
     *
     * @param exitCode the exit code, following the same conventions as {@link System#exit(int)}
     * @param waitTime the amount of time to wait before exiting
     */
    public void exit(int exitCode, Duration waitTime) {
        requireNotNull(waitTime, "waitTime must not be null");
        LOG.warn("Waiting {} before exiting", formatJavaDurationWords(waitTime));
        new DefaultEnvironment().sleepQuietly(waitTime);
        exit(exitCode);
    }
}
