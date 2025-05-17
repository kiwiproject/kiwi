package org.kiwiproject.collect;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
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
     * @return {@code true} if {@code map} is null or empty; {@code false} otherwise
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
     * @return {@code true} if {@code map} is neither null nor empty; {@code false} otherwise
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
        checkMapAndKeyArgsNotNull(map, key);

        if (map.containsKey(key)) {
            var v = map.get(key);
            if (isNull(v)) {
                throw new NoSuchElementException(f("value associated with key '{}' is null", key));
            }
            return v;
        }

        throw new NoSuchElementException(f("key '{}' does not exist in map", key));
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

        return getOrThrowChecked(map, key, exceptionSupplier);
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
        checkMapAndKeyArgsNotNull(map, key);
        checkArgumentNotNull(exceptionSupplier, "exceptionSupplier must not be null");

        if (map.containsKey(key)) {
            var v = map.get(key);
            if (isNull(v)) {
                throw exceptionSupplier.get();
            }
            return v;
        }

        throw exceptionSupplier.get();
    }

    /**
     * Returns the value associated with the specified key as a String, or null if the key doesn't exist,
     * or the value is null.
     * <p>
     * If the (non-null) value is not a String, it is converted using {@link Object#toString()}.
     *
     * @param map the map
     * @param key the key whose associated value is to be returned as a String
     * @return the value associated with the key as a String, or null if the key doesn't exist or the value is null
     */
    @Nullable
    public static String getAsStringOrNull(Map<?, ?> map, Object key) {
        return getAsString(map, key, null);
    }

    /**
     * Returns the value associated with the specified key as a String, or the provided default value
     * if the key doesn't exist or the value is null.
     * <p>
     * If the (non-null) value is not a String, it is converted using {@link Object#toString()}.
     *
     * @param map          the map
     * @param key          the key whose associated value is to be returned as a String
     * @param defaultValue the default value to return if the key doesn't exist or the value is null
     * @return the value associated with the key as a String, or the default value if the key doesn't exist or the value is null
     */
    @Nullable
    public static String getAsString(Map<?, ?> map, Object key, @Nullable String defaultValue) {
        return getConvertedValue(map, key, v -> isNull(v) ? defaultValue : v.toString());
    }

    /**
     * Returns the value associated with the specified key cast, to the specified type,
     * or null if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param valueType the class representing the type to cast the value to
     * @param <T>       the type of the value to be returned
     * @return the value associated with the key, cast to the specified type, or null if the key doesn't exist or the value is null
     * @throws MapTypeMismatchException if the value cannot be cast to the specified type
     */
    @Nullable
    public static <T> T getTypedValueOrNull(Map<?, ?> map, Object key, Class<T> valueType) {
        return getTypedValue(map, key, valueType, null);
    }

    /**
     * Returns the value associated with the specified key, cast to the specified type,
     * or the provided default value if the key doesn't exist or the value is null.
     *
     * @param map          the map
     * @param key          the key whose associated value is to be returned
     * @param valueType    the class representing the type to cast the value to
     * @param defaultValue the default value to return if the key doesn't exist or the value is null
     * @param <T>          the type of the value to be returned
     * @return the value associated with the key, cast to the specified type, or the default value if the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to the specified type
     */
    @Nullable
    public static <T> T getTypedValue(Map<?, ?> map, Object key, Class<T> valueType, @Nullable T defaultValue) {
        checkMapAndKeyArgsNotNull(map, key);
        checkValueTypeNotNull(valueType);

        try {
            return getConvertedValue(map, key, v -> isNull(v) ? defaultValue : valueType.cast(v));
        } catch (ClassCastException e) {
            throw MapTypeMismatchException.forTypeMismatch(key, valueType, e);
        }
    }

    /**
     * Returns the value associated with the specified key as a Collection of the specified type,
     * or null if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param valueType the class representing the type of elements in the collection
     * @param <T>       the type of elements in the collection
     * @return the value associated with the key as a Collection of the specified type, or null if
     * the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Collection
     * @implNote see implementation note in {@link #getTypedCollection(Map, Object, Class, Collection)}
     */
    @Nullable
    public static <T> Collection<T> getTypedCollectionOrNull(Map<?, ?> map, Object key, Class<T> valueType) {
        return getTypedCollection(map, key, valueType, null);
    }

    /**
     * Returns the value associated with the specified key as a Collection of the specified type,
     * or an empty Collection if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param valueType the class representing the type of elements in the collection
     * @param <T>       the type of elements in the collection
     * @return the value associated with the key as a Collection of the specified type, or an empty Collection
     * if the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Collection
     * @implNote see implementation note in {@link #getTypedCollection(Map, Object, Class, Collection)}
     */
    public static <T> Collection<T> getTypedCollectionOrEmpty(Map<?, ?> map, Object key, Class<T> valueType) {
        return getTypedCollection(map, key, valueType, List.of());
    }

    /**
     * Returns the value associated with the specified key as a Collection of the specified type,
     * or the provided default value if the key doesn't exist, or the value is null.
     *
     * @param map          the map
     * @param key          the key whose associated value is to be returned
     * @param valueType    the class representing the type of elements in the collection
     * @param defaultValue the default value to return if the key doesn't exist or the value is null
     * @param <T>          the type of elements in the collection
     * @return the value associated with the key as a Collection of the specified type, or the default value
     * if the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Collection
     * @implNote Internally this performs an unchecked cast of the value to {@code Collection<T>}. If the
     * values in the collection are not actually of type {@code T}, a {@code ClassCastException} will
     * be thrown when the collection is accessed, which may be downstream from the original call site
     * and therefore be challenging to diagnose the root cause.
     */
    @Nullable
    public static <T> Collection<T> getTypedCollection(Map<?, ?> map,
                                                       Object key,
                                                       Class<T> valueType,
                                                       @Nullable Collection<T> defaultValue) {
        checkMapAndKeyArgsNotNull(map, key);
        checkValueTypeNotNull(valueType);

        try {
            return getConvertedValue(map, key, v -> isNull(v) ? defaultValue : uncheckedCast(v));
        } catch (ClassCastException e) {
            throw MapTypeMismatchException.forTypeMismatch(key, Collection.class, e);
        }
    }

    /**
     * Returns the value associated with the specified key as a List of the specified type,
     * or null if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param valueType the class representing the type of elements in the list
     * @param <T>       the type of elements in the list
     * @return the value associated with the key as a List of the specified type, or null if
     * the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a List
     * @implNote see implementation note in {@link #getTypedList(Map, Object, Class, List)}
     */
    @Nullable
    public static <T> List<T> getTypedListOrNull(Map<?, ?> map, Object key, Class<T> valueType) {
        return getTypedList(map, key, valueType, null);
    }

    /**
     * Returns the value associated with the specified key as a List of the specified type,
     * or an empty List if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param valueType the class representing the type of elements in the list
     * @param <T>       the type of elements in the list
     * @return the value associated with the key as a List of the specified type, or an empty List
     * if the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a List
     * @implNote see implementation note in {@link #getTypedList(Map, Object, Class, List)}
     */
    public static <T> List<T> getTypedListOrEmpty(Map<?, ?> map, Object key, Class<T> valueType) {
        return getTypedList(map, key, valueType, List.of());
    }

    /**
     * Returns the value associated with the specified key as a List of the specified type,
     * or the provided default value if the key doesn't exist, or the value is null.
     *
     * @param map          the map
     * @param key          the key whose associated value is to be returned
     * @param valueType    the class representing the type of elements in the list
     * @param defaultValue the default value to return if the key doesn't exist or the value is null
     * @param <T>          the type of elements in the list
     * @return the value associated with the key as a List of the specified type, or the default value
     * if the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a List
     * @implNote Internally this performs an unchecked cast of the value to {@code List<T>}. If the
     * values in the list are not actually of type {@code T}, a {@code ClassCastException} will
     * be thrown when the list is accessed, which may be downstream from the original call site
     * and therefore be challenging to diagnose the root cause.
     */
    @Nullable
    public static <T> List<T> getTypedList(Map<?, ?> map,
                                           Object key,
                                           Class<T> valueType,
                                           @Nullable List<T> defaultValue) {
        checkMapAndKeyArgsNotNull(map, key);
        checkValueTypeNotNull(valueType);

        try {
            return getConvertedValue(map, key, v -> isNull(v) ? defaultValue : uncheckedCast(v));
        } catch (ClassCastException e) {
            throw MapTypeMismatchException.forTypeMismatch(key, List.class, e);
        }
    }

    /**
     * Returns the value associated with the specified key as a Set of the specified type,
     * or null if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param valueType the class representing the type of elements in the set
     * @param <T>       the type of elements in the set
     * @return the value associated with the key as a Set of the specified type, or null if
     * the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Set
     * @implNote see implementation note in {@link #getTypedSet(Map, Object, Class, Set)}
     */
    @Nullable
    public static <T> Set<T> getTypedSetOrNull(Map<?, ?> map, Object key, Class<T> valueType) {
        return getTypedSet(map, key, valueType, null);
    }

    /**
     * Returns the value associated with the specified key as a Set of the specified type,
     * or an empty Set if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param valueType the class representing the type of elements in the set
     * @param <T>       the type of elements in the set
     * @return the value associated with the key as a Set of the specified type, or an empty Set if
     * the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Set
     * @implNote see implementation note in {@link #getTypedSet(Map, Object, Class, Set)}
     */
    public static <T> Set<T> getTypedSetOrEmpty(Map<?, ?> map, Object key, Class<T> valueType) {
        return getTypedSet(map, key, valueType, Set.of());
    }

    /**
     * Returns the value associated with the specified key as a Set of the specified type,
     * or the provided default value if the key doesn't exist, or the value is null.
     *
     * @param map          the map
     * @param key          the key whose associated value is to be returned
     * @param valueType    the class representing the type of elements in the set
     * @param defaultValue the default value to return if the key doesn't exist or the value is null
     * @param <T>          the type of elements in the set
     * @return the value associated with the key as a Set of the specified type, or the default value if
     * the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Set
     * @implNote Internally this performs an unchecked cast of the value to {@code Set<T>}. If the
     * values in the list are not actually of type {@code T}, a {@code ClassCastException} will
     * be thrown when the set is accessed, which may be downstream from the original call site
     * and therefore be challenging to diagnose the root cause.
     */
    @Nullable
    public static <T> Set<T> getTypedSet(Map<?, ?> map,
                                         Object key,
                                         Class<T> valueType,
                                         @Nullable Set<T> defaultValue) {
        checkMapAndKeyArgsNotNull(map, key);
        checkValueTypeNotNull(valueType);

        try {
            return getConvertedValue(map, key, v -> isNull(v) ? defaultValue : uncheckedCast(v));
        } catch (ClassCastException e) {
            throw MapTypeMismatchException.forTypeMismatch(key, Set.class, e);
        }
    }

    /**
     * Returns the value associated with the specified key as a Map with keys and values of the specified types,
     * or null if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param keyType   the class representing the type of keys in the returned map
     * @param valueType the class representing the type of values in the returned map
     * @param <K>       the type of keys in the returned map
     * @param <V>       the type of values in the returned map
     * @return the value associated with the key as a Map with the specified key and value types, or null if
     * the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, keyType, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Map
     * @implNote see implementation note in {@link #getTypedMap(Map, Object, Class, Class, Map)}
     */
    @Nullable
    public static <K, V> Map<K, V> getTypedMapOrNull(Map<?, ?> map,
                                                     Object key,
                                                     Class<K> keyType,
                                                     Class<V> valueType) {
        return getTypedMap(map, key, keyType, valueType, null);
    }

    /**
     * Returns the value associated with the specified key as a Map with keys and values of the specified types,
     * or an empty Map if the key doesn't exist, or the value is null.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param keyType   the class representing the type of keys in the returned map
     * @param valueType the class representing the type of values in the returned map
     * @param <K>       the type of keys in the returned map
     * @param <V>       the type of values in the returned map
     * @return the value associated with the key as a Map with the specified key and value types, or an empty Map
     * if the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, keyType, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Map
     * @implNote see implementation note in {@link #getTypedMap(Map, Object, Class, Class, Map)}
     */
    public static <K, V> Map<K, V> getTypedMapOrEmpty(Map<?, ?> map,
                                                      Object key,
                                                      Class<K> keyType,
                                                      Class<V> valueType) {
        return getTypedMap(map, key, keyType, valueType, Map.of());
    }

    /**
     * Returns the value associated with the specified key as a Map with keys and values of the specified types,
     * or the provided default value if the key doesn't exist, or the value is null.
     *
     * @param map          the map
     * @param key          the key whose associated value is to be returned
     * @param keyType      the class representing the type of keys in the returned map
     * @param valueType    the class representing the type of values in the returned map
     * @param defaultValue the default value to return if the key doesn't exist or the value is null
     * @param <K>          the type of keys in the returned map
     * @param <V>          the type of values in the returned map
     * @return the value associated with the key as a Map with the specified key and value types, or the default
     * value if the key doesn't exist or the value is null
     * @throws IllegalArgumentException if the map, key, keyType, or valueType is null
     * @throws MapTypeMismatchException if the value cannot be cast to a Map
     * @implNote Internally this performs an unchecked cast of the value to {@code Map<K, V>}. If the
     * keys and values in the map are not actually of type {@code K} and {@code V}, respectively, a
     * {@code ClassCastException} will be thrown when the map is accessed, which may be downstream from
     * the original call site and therefore be challenging to diagnose the root cause.
     */
    @Nullable
    public static <K, V> Map<K, V> getTypedMap(Map<?, ?> map,
                                               Object key,
                                               Class<K> keyType,
                                               Class<V> valueType,
                                               @Nullable Map<K, V> defaultValue) {
        checkMapAndKeyArgsNotNull(map, key);
        checkArgumentNotNull(keyType, "keyType must not be null");
        checkValueTypeNotNull(valueType);

        try {
            return getConvertedValue(map, key, v -> isNull(v) ? defaultValue : uncheckedCast(v));
        } catch (ClassCastException e) {
            throw MapTypeMismatchException.forTypeMismatch(key, Map.class, e);
        }
    }

    private static <V> void checkValueTypeNotNull(Class<V> valueType) {
        checkArgumentNotNull(valueType, "valueType must not be null");
    }

    @SuppressWarnings("unchecked")
    private static <T> T uncheckedCast(Object object) {
        return (T) object;
    }

    /**
     * Returns the value associated with the specified key, converted using the provided converter function.
     * <p>
     * This method is marked as {@link Beta} and may change in future releases.
     *
     * @param map       the map
     * @param key       the key whose associated value is to be returned
     * @param converter a function that converts the value to the desired type
     * @param <T>       the type of the value to be returned
     * @return the value associated with the key, converted using the provided converter function
     * @throws IllegalArgumentException if the map, key, or converter is null
     */
    @Beta
    @Nullable
    public static <T> T getConvertedValue(Map<?, ?> map, Object key, Function<Object, T> converter) {
        checkMapAndKeyArgsNotNull(map, key);
        checkArgumentNotNull(converter, "converter function must not be null");

        var v = map.getOrDefault(key, null);
        return converter.apply(v);
    }

    /**
     * Returns the value associated with the specified key, converted using the provided converter function.
     * If the conversion fails with an exception, the fallback converter is used to provide an alternative value.
     * <p>
     * This method is marked as {@link Beta} and may change in future releases.
     *
     * @param map               the map
     * @param key               the key whose associated value is to be returned
     * @param converter         a function that converts the value to the desired type
     * @param fallbackConverter a function that provides a fallback value when the primary converter throws an exception
     * @param <T>               the type of the value to be returned
     * @return the value associated with the key, converted using the provided converter function,
     * or the result of the fallback converter if the primary conversion fails
     * @throws IllegalArgumentException if the map, key, converter, or fallbackConverter is null
     */
    @Beta
    @Nullable
    public static <T> T getConvertedValueWithFallback(Map<?, ?> map,
                                                      Object key,
                                                      Function<Object, T> converter,
                                                      BiFunction<Object, Exception, T> fallbackConverter) {

        checkMapAndKeyArgsNotNull(map, key);
        checkArgumentNotNull(converter, "converter function must not be null");
        checkArgumentNotNull(fallbackConverter, "fallbackConverter must not be null");

        var v = map.getOrDefault(key, null);
        try {
            return converter.apply(v);
        } catch (Exception e) {
            return fallbackConverter.apply(v, e);
        }
    }

    private static void checkMapAndKeyArgsNotNull(Map<?, ?> map, Object key) {
        checkArgumentNotNull(map, "map must not be null");
        checkArgumentNotNull(key, "key must not be null");
    }
}
