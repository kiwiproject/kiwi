package org.kiwiproject.base.system;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
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
     * Returns a strategy that "flags" when the {@code exit(int)} method is called, but does not actually
     * terminate the JVM. Intended for use in integration tests where the JVM should not actually exit.
     *
     * @return an exit-flagging ExecutionStrategy
     */
    public static ExitFlaggingExecutionStrategy exitFlagging() {
        return new ExitFlaggingExecutionStrategy();
    }

    /**
     * Returns a strategy that uses the {@link System} class to exit/terminate the JVM.
     *
     * @return a strategy that uses {@link System#exit(int)}
     */
    public static SystemExitExecutionStrategy systemExit() {
        return new SystemExitExecutionStrategy();
    }

    /**
     * Implementation of {@link ExecutionStrategy} that uses {@link System#exit(int)}.
     */
    @Slf4j
    public static class SystemExitExecutionStrategy implements ExecutionStrategy {

        /**
         * Terminates the currently running JVM with the given exit code.
         *
         * @param exitCode the exit code, following the same conventions as {@link System#exit(int)}
         */
        @Override
        public void exit(int exitCode) {
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
        public void exit(int exitCode) {
            // Intentionally empty
        }
    }

    /**
     * Implementation of {@link ExecutionStrategy} that "flags" a call to {@link #exit(int)} but does not actually
     * exit the JVM. Intended for use in integration tests where the JVM should not actually exit, but code can
     * verify that exit would have been called and with what exit code.
     */
    public static class ExitFlaggingExecutionStrategy implements ExecutionStrategy {

        private final AtomicBoolean didExit = new AtomicBoolean();
        private final AtomicInteger exitCode = new AtomicInteger();

        @Override
        public void exit(int exitCode) {
            this.exitCode.set(exitCode);
            didExit.set(true);
        }

        /**
         * Was {@link #exit(int)} called?
         *
         * @return true if exit was called, otherwise false
         */
        public boolean didExit() {
            return didExit.get();
        }

        /**
         * Returns the exit code passed to {@link #exit(int)}, or an empty OptionalInt if exit was never called.
         *
         * @return an OptionalInt containing the exit code, or empty if exit was never called
         */
        public OptionalInt exitCode() {
            return didExit() ? OptionalInt.of(exitCode.get()) : OptionalInt.empty();
        }
    }
}
