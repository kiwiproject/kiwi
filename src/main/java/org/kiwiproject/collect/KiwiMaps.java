package org.kiwiproject.collect;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility methods for working with {@link Map} instances
 */
@UtilityClass
public class KiwiMaps {

    /**
     * Checks whether the specified map is null or empty.
     *
     * @param map  the map
     * @param <K>  the type of the keys in the map
     * @param <V>  the type of the values in the map
     * @return {@code true} if map is null or empty; {@code false} otherwise
     */
    public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks whether the specified map is neither null nor empty.
     *
     * @param map the map
     * @param <K> the type of the keys in the map
     * @param <V> the type of the values in the map
     * @return {@code true} if map is neither null nor empty; {@code false} otherwise
     */
    public static <K, V> boolean isNotNullOrEmpty(Map<K, V> map) {
        return !isNullOrEmpty(map);
    }

    /**
     * Creates a <i>mutable</i>, {@link java.util.HashMap} instance containing key/value pairs as parsed in pairs from
     * the items argument. The items argument contains keys and values in the form:
     * <p>
     * <i>key-1, value-1, key-2, value-2, ... , key-N, value-N</i>
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new HashMap with data from items
     */
    public static <K, V> Map<K, V> newHashMap(Object... items) {
        checkEvenItemCount(items);
        var map = new HashMap<K, V>(items.length);
        populate(map, items);
        return map;
    }

    /**
     * Creates a <i>mutable</i>, {@link java.util.LinkedHashMap} instance containing key/value pairs as parsed in pairs from
     * the items argument. The items argument contains keys and values in the form:
     * <p>
     * <i>key-1, value-1, key-2, value-2, ... , key-N, value-N</i>
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new LinkedHashMap with data from items
     */
    public static <K, V> Map<K, V> newLinkedHashMap(Object... items) {
        checkEvenItemCount(items);
        var map = new LinkedHashMap<K, V>(items.length);
        populate(map, items);
        return map;
    }

    /**
     * Creates a <i>mutable</i>, {@link java.util.TreeMap} instance containing key/value pairs as parsed in pairs from
     * the items argument. The items argument contains keys and values in the form:
     * <p>
     * <i>key-1, value-1, key-2, value-2, ... , key-N, value-N</i>
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new TreeMap with data from items
     */
    public static <K extends Comparable<? super K>, V> SortedMap<K, V> newTreeMap(Object... items) {
        checkEvenItemCount(items);
        var map = new TreeMap<K, V>();
        populate(map, items);
        return map;
    }

    /**
     * Creates a <i>mutable</i>, {@link ConcurrentHashMap} instance containing key/value pairs as parsed in pairs from
     * the items argument. The items argument contains keys and values in the form:
     * <p>
     * <i>key-1, value-1, key-2, value-2, ... , key-N, value-N</i>
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new ConcurrentHashMap with data from items
     */
    public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(Object... items) {
        checkEvenItemCount(items);
        var map = new ConcurrentHashMap<K, V>(items.length);
        populate(map, items);
        return map;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> void populate(Map<K, V> map, Object... items) {
        for (var i = 0; i < items.length; i += 2) {
            var key = (K) items[i];
            var value = (V) items[i + 1];
            map.put(key, value);
        }
    }

    /**
     * Returns {@code true} if and only if (1) {@code map} is not null or empty, (2) {@code map} contains the given
     * {@code key}, and (3) the value associated with the given key is {@code null}.
     *
     * @param map the map
     * @param key the key check
     * @param <K> the type of the keys in the map
     * @param <V> the type of the values in the map
     * @return {@code true} if and only if (1) {@code map} is not null or empty, (2) {@code map} contains the given
     * {@code key}, and (3) the value associated with the given key is {@code null}
     */
    public static <K, V> boolean keyExistsWithNullValue(Map<K, V> map, K key) {
        return isNotNullOrEmpty(map) && map.containsKey(key) && isNull(map.get(key));
    }

}
