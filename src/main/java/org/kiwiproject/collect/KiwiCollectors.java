package org.kiwiproject.collect;

import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utility for custom {@link java.util.stream.Collector} implementations
 */
@UtilityClass
public class KiwiCollectors {

    /**
     * Return a {@link Collector} that collects into a Guava {@link ImmutableList.Builder}. You only then need to
     * call {@link ImmutableList.Builder#build()}.
     *
     * @param <T> the type in the list
     * @return ImmutableList of {@code T}
     */
    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList.Builder<T>> toImmutableListBuilder() {
        return Collector.of(
                ImmutableList::builder,
                ImmutableList.Builder::add,
                (builder1, builder2) -> {
                    var listFromBuilder2 = builder2.build();
                    return builder1.addAll(listFromBuilder2);
                }
        );
    }

    /**
     * Returns a {@link Collector} that collects into an {@link EnumSet}.
     *
     * @param type the type in the {@link EnumSet}
     * @param <E> the enum type subclass
     * @return the {@link Collector}
     */
    public static <E extends Enum<E>> Collector<E, EnumSet<E>, EnumSet<E>> toEnumSet(Class<E> type) {
        return Collector.of(
                () -> EnumSet.noneOf(type),
                Set::add,
                (enumSet1, enumSet2) -> {
                    enumSet1.addAll(enumSet2);
                    return enumSet1;
                }
        );
    }

    /**
     * Returns a {@link Collector} that collects into an {@link java.util.EnumMap}.
     * <p>
     * If the mapped keys contain duplicates (according to {@link Object#equals(Object)}, an
     * {@code IllegalStateException} is thrown when the collection operation is performed.
     *
     * @param enumClass   the key type for the returned map
     * @param keyMapper   a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param <T>         the type of the input elements
     * @param <K>         the output type of the key mapping function
     * @param <U>         the outptu type of the value mapping function
     * @return a {@link Collector} which collects elements into an {@link java.util.EnumMap} whose keys
     * are the result of applying key and value mapping functions.
     */
    public static <T, K extends Enum<K>, U> Collector<T, ?, Map<K, U>> toEnumMap(
            Class<K> enumClass,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (u1, u2) -> {
                    throw duplicateKeyException(u1, u2);
                },
                () -> new EnumMap<>(enumClass)
        );
    }

    /**
     * Returns a {@link Collector} that collects intoa {@link LinkedHashMap}.
     *
     * @param keyMapper    a mapping function to produce keys
     * @param valueMapper  a mapping function to produce values
     * @param <T>          the type of the input elements
     * @param <K>          the output type of the key mapping function
     * @param <U>          the output type of the value mapping function
     * @return a {@link Collector} which collects elements into a {@link java.util.LinkedHashMap} whose keys
     * are the result of applying key and value mapping functions.
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (u1, u2) -> {
                    throw duplicateKeyException(u1, u2);
                },
                LinkedHashMap::new
        );
    }


    /**
     * Used when a duplicate key occurs during a collection, returns a new {@link IllegalStateException}.
     *
     * @param u1  the first value supplied to the merge function
     * @param u2  the second value supplied to the merge function
     * @return a new {@link IllegalStateException} object
     * @implNote There is not an easy way to obtain the actual duplicate key here, since the merge function is a
     * {@link java.util.function.BinaryOperator} that accepts the two values needing to be merged. So unfortunately
     * the best we can easily do is report the values attempting to be merged, and let the person doing the
     * investigation hopefully backtrack to the duplicate key.
     */
    private static IllegalStateException duplicateKeyException(Object u1, Object u2) {
        return new IllegalStateException(f("Duplicate key. Attempted to merge values {} and {}", u1, u2));
    }
}
