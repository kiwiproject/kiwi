package org.kiwiproject.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import java.util.function.IntConsumer;

/**
 * Utility methods for iteration.
 */
@UtilityClass
public class KiwiIterations {

    /**
     * Execute the given action {@code n} times, passing the current iteration
     * index (from 0 up to {@code n - 1}).
     *
     * <p>This is a convenience wrapper similar to Ruby's {@code n.times { ... }},
     * and is especially useful for concise test code or repeated setup/teardown actions.
     *
     * @param n      the number of times to run the action; must be &gt;= 0
     * @param action the action to run with the current iteration index
     * @throws IllegalArgumentException if {@code n} is negative or {@code action} is null
     */
    public static void times(int n, IntConsumer action) {
        checkArgument(n >= 0, "n must be positive or zero, but was %s", n);
        checkArgumentNotNull(action, "action must not be null");
        for (int i = 0; i < n; i++) {
            action.accept(i);
        }
    }

    /**
     * Execute the given action {@code n} times.
     *
     * <p>Use this overload when you don't care about the current iteration index.
     *
     * @param n      the number of times to run the action; must be &gt;= 0
     * @param action the action to run
     * @throws IllegalArgumentException if {@code n} is negative or {@code action} is null
     */
    public static void times(int n, Runnable action) {
        checkArgumentNotNull(action, "action must not be null");
        times(n, ignored -> action.run());
    }
}
