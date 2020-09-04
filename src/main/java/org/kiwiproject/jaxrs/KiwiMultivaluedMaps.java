package org.kiwiproject.jaxrs;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

/**
 * Static utilities for working with {@link MultivaluedMap} instances.
 */
@UtilityClass
public class KiwiMultivaluedMaps {

    /**
     * Create a new {@link MultivaluedMap} from the given items, which are expected to be in alternating
     * key/value pairs. This allows for keys to be repeated, i.e. so that a single key can have multiple values.
     *
     * @param items the keys and values, e.g. key1, value1, key2, value2, ...
     * @return a new {@link MultivaluedMap}
     * @implNote the actual type returned is currently a {@link MultivaluedHashMap}
     */
    public static MultivaluedMap<String, String> newMultivaluedMap(String... items) {
        checkEvenItemCount(items);

        var map = new MultivaluedHashMap<String, String>();
        List<List<String>> kvPairs = partition(newArrayList(items), 2);
        for (List<String> kvPair : kvPairs) {
            var key = first(kvPair);
            var value = second(kvPair);
            map.add(key, value);
        }
        return map;
    }

    /**
     * Create a new {@link MultivaluedMap} from the given items, which are expected to be in alternating
     * key/value pairs.
     * <p>
     * <strong>NOTE:</strong> Unlike {@link #newMultivaluedMap(String...)}, this method assumes you want only
     * one value per key, even if {@code items} contains multiple values per key. In that case, the <em>last</em>
     * value in {@code items} associated with a given key is the one that "wins" and stays in the returned map.
     *
     * @param items the keys and values, e.g. key1, value1, key2, value2, ...
     * @return a new {@link MultivaluedMap}
     * @implNote the actual type returned is currently a {@link MultivaluedHashMap}
     */
    public static MultivaluedMap<String, String> newSingleValuedParameterMap(String... items) {
        checkEvenItemCount(items);

        var map = new MultivaluedHashMap<String, String>();
        List<List<String>> kvPairs = partition(newArrayList(items), 2);
        for (List<String> kvPair : kvPairs) {
            var key = first(kvPair);
            var value = second(kvPair);
            map.putSingle(key, value);
        }
        return map;
    }

    /**
     * Converts the given {@link MultivaluedMap} to a single-valued JDK {@link Map} by getting <em>only</em>
     * the <em>first</em> element for each key in the multi-valued map. This means that if there are keys
     * having multiple values, elements past the first one <em>are ignored</em>.
     *
     * @param original the original multi-valued map to convert
     * @return the <em>single</em>-valued JDK map
     */
    public static Map<String, String> toSingleValuedParameterMap(MultivaluedMap<String, String> original) {
        checkArgumentNotNull(original, "original map cannot be null");

        var singleValuedMap = Maps.<String, String>newHashMapWithExpectedSize(original.keySet().size());
        for (var key : original.keySet()) {
            singleValuedMap.put(key, original.getFirst(key));
        }
        return singleValuedMap;
    }

    /**
     * Convert the given {@link MultivaluedMap} into a Guava {@link Multimap}.
     *
     * @param original the original multi-valued map to convert
     * @return the Guava multimap
     */
    public static Multimap<String, String> toMultimap(MultivaluedMap<String, String> original) {
        checkArgumentNotNull(original, "original map cannot be null");

        var multimap = ArrayListMultimap.<String, String>create();
        original.forEach(multimap::putAll);
        return multimap;
    }
}
