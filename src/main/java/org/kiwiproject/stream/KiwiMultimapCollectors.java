package org.kiwiproject.stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A {@link Collector} that can collect into a Guava {@link Multimap}.
 */
@UtilityClass
public class KiwiMultimapCollectors {

    /**
     * Given a stream of {@link Map.Entry} objects, collects to the {@link Multimap} specified in the
     * {@code supplier} argument. This lets you create any type of Multimap you want to collect into. Usually you
     * will use a method reference as the supplier, e.g. {@code ArrayListMultimap:create}. You can easily get a
     * stream of Map.Entry objects by getting the map's {@link Map#entrySet()} and then calling stream on it, e.g.
     * {@code someMap.entrySet().stream()}.
     *
     * @param supplier the Multimap to collect into
     * @param <K>      the key type of the Map.Entry objects
     * @param <V>      the value type of the Map.Entry objects
     * @param <A>      the accumulator type, i.e. some kind of Multimap
     * @return the collected Multimap
     */
    public static <K, V, A extends Multimap<K, V>> Collector<Map.Entry<K, V>, A, A> toMultimap(Supplier<A> supplier) {
        return Collector.of(
                supplier,
                (accumulator, entry) -> accumulator.put(entry.getKey(), entry.getValue()),
                (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                });
    }

    /**
     * Collects into an {@link ArrayListMultimap}.
     *
     * @param <K> the key type of the Map.Entry objects
     * @param <V> the value type of the Map.Entry objects
     * @return the collected ArrayListMultimap
     */
    public static <K, V> Collector<Map.Entry<K, V>, Multimap<K, V>, Multimap<K, V>> toArrayListMultimap() {
        return toMultimap(ArrayListMultimap::create);
    }

    /**
     * Collects into an {@link HashMultimap}.
     *
     * @param <K> the key type of the Map.Entry objects
     * @param <V> the value type of the Map.Entry objects
     * @return the collected HashMultimap
     */
    public static <K, V> Collector<Map.Entry<K, V>, Multimap<K, V>, Multimap<K, V>> toHashMultimap() {
        return toMultimap(HashMultimap::create);
    }

    /**
     * Collects into an {@link LinkedHashMultimap}.
     *
     * @param <K> the key type of the Map.Entry objects
     * @param <V> the value type of the Map.Entry objects
     * @return the collected LinkedHashMultimap
     */
    public static <K, V> Collector<Map.Entry<K, V>, Multimap<K, V>, Multimap<K, V>> toLinkedHashMultimap() {
        return toMultimap(LinkedHashMultimap::create);
    }
}
