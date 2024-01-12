package org.kiwiproject.collect;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Utility methods for working with {@link Map} instances
 */
@UtilityClass
public class KiwiMaps {

    /**
     * Checks whether the specified map is null or empty.
     *
     * @param map the map
     * @param <K> the type of the keys in the map
     * @param <V> the type of the values in the map
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
     * Creates an <em>unmodifiable</em> {@link java.util.HashMap} instance containing key/value pairs as parsed in
     * pairs from the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
     * <p>
     * Note that trying to cast the returned value to a {@link HashMap} will result in a {@link ClassCastException}
     * because the actual type returned by {@link Collections#unmodifiableMap(Map)} is a private class that
     * implements {@link Map}. Since it is generally it is best to use the interface type {@link Map} this should
     * not present a problem in most use cases.
     * <p>
     * Unlike the factory methods in {@link Map}, null keys and values <em>are</em> permitted.
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new <em>unmodifiable</em> HashMap with data from items
     * @implNote Wraps result of {@link #newHashMap(Object...)} with {@link Collections#unmodifiableMap(Map)}
     * @see #newHashMap(Object...)
     */
    public static <K, V> Map<K, V> newUnmodifiableHashMap(Object... items) {
        return Collections.unmodifiableMap(newHashMap(items));
    }

    /**
     * Creates a <em>mutable</em>, {@link java.util.HashMap} instance containing key/value pairs as parsed in pairs from
     * the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
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
     * Creates an <em>unmodifiable</em>, {@link java.util.LinkedHashMap} instance containing key/value pairs as parsed
     * in pairs from the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
     * <p>
     * Note that trying to cast the returned value to a {@link java.util.LinkedHashMap} will result in a {@link ClassCastException}
     * because the actual type returned by {@link Collections#unmodifiableMap(Map)} is a private class that
     * implements {@link Map}. Since it is generally it is best to use the interface type {@link Map} this should
     * not present a problem in most use cases.
     * <p>
     * Unlike the factory methods in {@link Map}, null keys and values <em>are</em> permitted.
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new <em>unmodifiable</em> LinkedHashMap with data from items
     * @implNote Wraps result of {@link #newLinkedHashMap(Object...)} with {@link Collections#unmodifiableMap(Map)}
     * @see #newLinkedHashMap(Object...)
     */
    public static <K, V> Map<K, V> newUnmodifiableLinkedHashMap(Object... items) {
        return Collections.unmodifiableMap(newLinkedHashMap(items));
    }

    /**
     * Creates a <em>mutable</em>, {@link java.util.LinkedHashMap} instance containing key/value pairs as parsed in
     * pairs from the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
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
     * Creates an <em>unmodifiable</em>, {@link java.util.TreeMap} instance containing key/value pairs as parsed in
     * pairs from the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
     * <p>
     * Note that trying to cast the returned value to a {@link TreeMap} will result in a {@link ClassCastException}
     * because the actual type returned by {@link Collections#unmodifiableMap(Map)} is a private class that
     * implements {@link Map}. Since it is generally it is best to use the interface type {@link Map} this should
     * not present a problem in most use cases.
     * <p>
     * Like the factory methods in {@link Map}, null keys and values are <em>not</em> permitted.
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new <em>unmodifiable</em> TreeMap with data from items
     * @implNote Wraps result of {@link #newTreeMap(Object...)} with {@link Collections#unmodifiableMap(Map)}
     * @see #newTreeMap(Object...)
     */
    public static <K extends Comparable<? super K>, V> SortedMap<K, V> newUnmodifiableTreeMap(Object... items) {
        SortedMap<K, V> sortedMap = newTreeMap(items);
        return Collections.unmodifiableSortedMap(sortedMap);
    }

    /**
     * Creates a <em>mutable</em>, {@link java.util.TreeMap} instance containing key/value pairs as parsed in pairs from
     * the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
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
     * Creates an <em>unmodifiable</em>, {@link ConcurrentHashMap} instance containing key/value pairs as parsed in
     * pairs from the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
     * <p>
     * Note that, unlike {@link #newConcurrentHashMap(Object...)}, this method has a return type of {@link Map}.
     * This is because {@link Collections#unmodifiableMap(Map)} returns {@link Map} and trying to cast the returned
     * instance to a {@link ConcurrentMap} results in a {@link ClassCastException}. Since it is generally it is best
     * to use the interface type {@link Map} this should not present a problem in most use cases.
     * <p>
     * Like the factory methods in {@link Map}, null keys and values are <em>not</em> permitted.
     *
     * @param items the items containing keys and values, in pairs
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new <em>unmodifiable</em> ConcurrentHashMap with data from items
     * @implNote Wraps result of {@link #newConcurrentHashMap(Object...)} with {@link Collections#unmodifiableMap(Map)}
     * @see #newConcurrentHashMap(Object...)
     */
    public static <K, V> Map<K, V> newUnmodifiableConcurrentHashMap(Object... items) {
        return Collections.unmodifiableMap(newConcurrentHashMap(items));
    }

    /**
     * Creates a <em>mutable</em>, {@link ConcurrentHashMap} instance containing key/value pairs as parsed in pairs from
     * the {@code items} argument. The items argument contains keys and values in the form:
     * <p>
     * <em>key-1, value-1, key-2, value-2, ... , key-N, value-N</em>
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
     * @param key the key to check
     * @param <K> the type of the keys in the map
     * @param <V> the type of the values in the map
     * @return {@code true} if and only if (1) {@code map} is not null or empty, (2) {@code map} contains the given
     * {@code key}, and (3) the value associated with the given key is {@code null}
     */
    public static <K, V> boolean keyExistsWithNullValue(Map<K, V> map, K key) {
        return keyExists(map, key) && isNull(map.get(key));
    }

    /**
     * Checks whether the given map contains a key whose value is not null.
     *
     * @param map the map
     * @param key the key to check
     * @param <K> the type of the keys in the map
     * @param <V> the type of the values in the map
     * @return {@code true} if and only if (1) {@code map} is not null or empty, (2) {@code map} contains the given
     * {@code key}, and (3) the value associated with the given key is not {@code null}
     */
    public static <K, V> boolean keyExistsWithNonNullValue(Map<K, V> map, K key) {
        return keyExists(map, key) && nonNull(map.get(key));
    }

    private static <K, V> boolean keyExists(Map<K, V> map, K key) {
        return isNotNullOrEmpty(map) && map.containsKey(key);
    }

    /**
     * Returns the value to which the specified key is mapped.
     * <p>
     * If the map is null or empty, or it does not contain the specified key, or the value
     * associated with the key is null, a {@link NoSuchElementException} is thrown.
     *
     * @param map the map
     * @param key the key whose associated value is to be returned
     * @param <K> the type of the keys in the map
     * @param <V> the type of the values in the map
     * @return the value to which the specified key is mapped
     * @throws IllegalArgumentException if either argument is null
     * @throws NoSuchElementException if the map is null or empty, does not contain the specified key,
     *                                or the value associated with the key is null
     */
    public static <K, V> V getOrThrow(Map<K, V> map, K key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the value to which the specified key is mapped.
     * <p>
     * If the map is null or empty, or it does not contain the specified key, or the value
     * associated with the key is null, the exception provided by the given Supplier is thrown.
     *
     * @param map               the map
     * @param key               the key whose associated value is to be returned
     * @param exceptionSupplier supplies a RuntimeException when there is no value to return
     * @param <K>               the type of the keys in the map
     * @param <V>               the type of the values in the map
     * @param <E>               the type of RuntimeException
     * @return the value to which the specified key is mapped
     * @throws IllegalArgumentException if any of the arguments is null
     * @throws E if the map is null or empty, does not contain the specified key,
     *           or the value associated with the key is null
     */
    public static <K, V, E extends RuntimeException> V getOrThrow(Map<K, V> map,
                                                                  K key,
                                                                  Supplier<E> exceptionSupplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the value to which the specified key is mapped.
     * <p>
     * If the map is null or empty, or it does not contain the specified key, or the value
     * associated with the key is null, the exception provided by the given Supplier is thrown.
     *
     * @param map               the map
     * @param key               the key whose associated value is to be returned
     * @param exceptionSupplier supplies an Exception when there is no value to return
     * @param <K>               the type of the keys in the map
     * @param <V>               the type of the values in the map
     * @param <E>               the type of Exception
     * @return the value to which the specified key is mapped
     * @throws IllegalArgumentException if any of the arguments is null
     * @throws E if the map is null or empty, does not contain the specified key,
     *           or the value associated with the key is null
     */
    public static <K, V, E extends Exception> V getOrThrowChecked(Map<K, V> map,
                                                                  K key,
                                                                  Supplier<E> exceptionSupplier) throws E {
        throw new UnsupportedOperationException();
    }
}
