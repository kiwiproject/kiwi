package org.kiwiproject.util.function;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

/**
 * Contains helper methods for {@link Consumer}.
 */
@UtilityClass
public class KiwiConsumers {

    /**
     * Provides a type-consistent, no-op {@link Consumer}.
     *
     * @param <T> type parameter of the Consumer
     * @return the no-op Consumer
     */
    public static <T> Consumer<T> noOp() {
        return argument -> {
            // no-op
        };
    }
}
