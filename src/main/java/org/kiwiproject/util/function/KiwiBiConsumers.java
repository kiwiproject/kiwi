package org.kiwiproject.util.function;

import lombok.experimental.UtilityClass;

import java.util.function.BiConsumer;

/**
 * Contains helper methods for {@link BiConsumer}.
 */
@UtilityClass
public class KiwiBiConsumers {

    /**
     * Provides a type-consistent, no-op {@link BiConsumer}.
     *
     * @param <T> first type parameter of the BiConsumer
     * @param <U> second type parameter of the BiConsumer
     * @return the no-op BiConsumer
     */
    public static <T, U> BiConsumer<T, U> noOp() {
        return (t, u) -> {
            // no-op
        };
    }
}
