package org.kiwiproject.base.system;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Factory for {@link ExecutionStrategy} instances.
 */
@UtilityClass
public class ExecutionStrategies {

    /**
     * Returns a strategy that does nothing (is a "no-op").
     *
     * @return a no-op ExecutionStrategy
     */
    public static NoOpExecutionStrategy noOp() {
        return new NoOpExecutionStrategy();
    }

    /**
     * Returns a strategy that "flags" when the {@code exit()} method is called, but does not actually
     * terminate the JVM.
     *
     * @return an exit-flagging ExecutionStrategy
     */
    public static ExitFlaggingExecutionStrategy exitFlagging() {
        return new ExitFlaggingExecutionStrategy();
    }

    /**
     * Returns a strategy that uses the {@link System} class to exit/terminate the JVM.
     *
     * @return a strategy that uses {@link System#exit(int)} that sets the exit code to 1 (one)
     */
    public static SystemExitExecutionStrategy systemExit() {
        return new SystemExitExecutionStrategy();
    }

    /**
     * Returns a strategy that uses the {@link System} class to exit/terminate the JVM.
     *
     * @param exitCode the exit code that will be supplied to {@link System#exit(int)}
     * @return a strategy that uses {@link System#exit(int)} that uses the given exit code
     */
    public static SystemExitExecutionStrategy systemExit(int exitCode) {
        return new SystemExitExecutionStrategy(exitCode);
    }

    /**
     * Implementation of {@link ExecutionStrategy} that uses {@link System#exit(int)}.
     */
    @Slf4j
    public static class SystemExitExecutionStrategy implements ExecutionStrategy {

        @Getter
        private final int exitCode;

        /**
         * Construct an instance that will set the exit code to 1 (one).
         */
        public SystemExitExecutionStrategy() {
            this(1);
        }

        /**
         * Construct an instance that will use the given exit code.
         *
         * @param exitCode the exit code for {@link System#exit(int)}
         */
        public SystemExitExecutionStrategy(int exitCode) {
            this.exitCode = exitCode;
        }

        /**
         * Terminates the currently running JVM.
         */
        @Override
        public void exit() {
            LOG.warn("Terminating the VM!");
            System.exit(exitCode);
        }
    }

    /**
     * Implementation of {@link ExecutionStrategy} that does nothing.
     */
    public static class NoOpExecutionStrategy implements ExecutionStrategy {

        /**
         * A no-op. Mainly useful in unit testing scenarios.
         */
        @Override
        public void exit() {
            // Intentionally empty
        }
    }

    /**
     * Implementation of {@link ExecutionStrategy} that "flags" a call to {@link #exit()} but does not actually
     * exit the JVM.
     */
    public static class ExitFlaggingExecutionStrategy implements ExecutionStrategy {

        private final AtomicBoolean didExit = new AtomicBoolean();

        @Override
        public void exit() {
            didExit.set(true);
        }

        /**
         * Was {@link #exit()} called?
         *
         * @return true if exit was called, otherwise false
         */
        public boolean didExit() {
            return didExit.get();
        }
    }
}
