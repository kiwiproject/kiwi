package org.kiwiproject.base.system;

/**
 * Defines a strategy used in {@link SystemExecutioner} to terminate the JVM.
 */
public interface ExecutionStrategy {

    /**
     * Performs the exit operation using the given exit code.
     *
     * @param exitCode the exit code, following the same conventions as {@link System#exit(int)}
     */
    void exit(int exitCode);
}
