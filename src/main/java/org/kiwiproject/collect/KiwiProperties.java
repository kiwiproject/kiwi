package org.kiwiproject.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;
import static java.util.Objects.requireNonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utility methods for making it easier to create and work with {@link java.util.Properties} instances.
 */
@UtilityClass
public class KiwiProperties {

    /**
     * Crates a <i>mutable</i> {@link Properties} instance by parsing the items argument in pairs.
     * The items argument contains keys and values in the form:
     * <p>
     * <i>key-1, value-1, key-2, value-2, ... , key-N, value-N</i>
     *
     * @param items the items containing keys and values, in pairs
     * @return a new Properties instance with data from items
     * @throws NullPointerException if any of the items is null
     * @implNote Please note that even though this method accepts objects, you should really only put strings in here,
     * due to the way {@link Properties} is designed to only have string keys and values. The values are always converted
     * to strings before putting them in the properties.
     */
    public static Properties newProperties(Object... items) {
        checkEvenItemCount(items);
        var strings = newArrayList(items).stream().map(Object::toString).toList();
        var kvPairs = partition(strings, 2);
        return newPropertiesFromStringPairs(kvPairs);
    }

    /**
     * Creates a <i>mutable</i> {@link Properties} instance by parsing the items argument in pairs.
     * The items argument contains keys and values in the form:
     * <p>
     * <i>key-1, value-1, key-2, value-2, ... , key-N, value-N</i>
     * </p>
     *
     * @param items the items containing keys and values, in pairs
     * @return a new Properties instance with data from items
     * @throws NullPointerException if any of the items is null
     */
    public static Properties newProperties(String... items) {
        checkEvenItemCount(items);
        var kvPairs = partition(newArrayList(items), 2);
        return newPropertiesFromStringPairs(kvPairs);
    }

    /**
     * Creates a <i>mutable</i> {@link Properties} instance by parsing the items argument in pairs from the list.
     *
     * @param items the items containing keys and values, in pairs
     * @return a new Properties instance with data from items
     * @throws NullPointerException if any of the items is null
     */
    public static Properties newProperties(List<String> items) {
        checkEvenItemCount(items);
        var kvPairs = partition(items, 2);
        return newPropertiesFromStringPairs(kvPairs);
    }

    /**
     * Makes creating a {@link Properties} instance from a {@link Map} a one-liner. Also, restricts the map to string
     * keys and values. Creates a <i>mutable</i> {@link Properties} instance.
     *
     * @param map the source map
     * @return a new Properties instance with data from the map
     * @throws NullPointerException if any of the items is null
     * @implNote The reason this method restricts the map keys and values to strings is because Properties is derived
     * from {@link java.util.Hashtable} and the JavaDoc states:
     * "Because {@code Properties} inherits from {@code Hashtable}, the {@code put} and {@code putAll} methods can be
     * applied to a {@code Properties} object. Their use is strongly discouraged as they allow the caller to insert
     * entries whose keys or values are not {@code Strings}. The {@code setProperty} method should be used instead."
     * This of course is simply poor design (they should have used composition and hidden the internal storage details
     * instead of extending Hashtable).
     */
    public static Properties newProperties(Map<String, String> map) {
        requireNonNull(map);
        var properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    /**
     * Creates a <i>mutable</i> {@link Properties} instance from each key/value pair list inside the outer list.
     * The items argument must contain keys and values in teh form:
     * <p>
     * <i>[ [key-1, value-1], [key-2, value-2], ... , [key-N, value-N]</i>
     * </p>
     *
     * @param items the items list containing a series of two-items key/value pair lists
     * @return a new Properties instance with data from items
     * @throws NullPointerException if any of the items is null
     * @throws IllegalArgumentException if any of the key/value pair lists do not have at least two elements
     * @implNote only the first and second elements of the key/value pair sub-lists are used when creating the
     * Properties instance. Thus while it does not make much sense, this method will not throw an exception if the
     * sub-lists contain more than two elements.
     */
    public static Properties newPropertiesFromStringPairs(List<List<String>> items) {
        requireNonNull(items);
        return items.stream().collect(
                Properties::new,
                KiwiProperties::checkPairAndAccumulate,
                Properties::putAll);
    }

    private static void checkPairAndAccumulate(Properties accumulator, List<String> pair) {
        checkArgument(pair.size() >= 2, "Each sublist must contain at least 2 items (additional elements are ignored but won't cause an error)");
        accumulator.setProperty(first(pair), second(pair));
    }

}
