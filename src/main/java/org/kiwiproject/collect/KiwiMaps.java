package org.kiwiproject.collect;

import com.google.common.math.IntMath;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;

@UtilityClass
public class KiwiMaps {

    public static <K, V> boolean isNullOrEmpty(Map<K, V> items) {
        return items == null || items.isEmpty();
    }

    public static <K, V> boolean isNotNullOrEmpty(Map<K, V> items) {
        return !isNullOrEmpty(items);
    }

    public static <K, V> Map<K, V> newHashMap(Object... items) {
        checkEvenItemCount(items);
        Map<K, V> map = new HashMap<>(items.length);
        populate(map, items);
        return map;
    }

    public static <K, V> Map<K, V> newLinkedHashMap(Object... items) {
        checkEvenItemCount(items);
        Map<K, V> map = new LinkedHashMap<>(items.length);
        populate(map, items);
        return map;
    }

    public static <K extends Comparable, V> Map<K, V> newTreeMap(Object... items) {
        checkEvenItemCount(items);
        Map<K, V> map = new TreeMap<>();
        populate(map, items);
        return map;
    }

    public static <K, V> Map<K, V> newConcurrentHashMap(Object... items) {
        checkEvenItemCount(items);
        Map<K, V> map = new ConcurrentHashMap<>(items.length);
        populate(map, items);
        return map;
    }

    private static void checkEvenItemCount(Object... items) {
        checkArgument(IntMath.mod(items.length, 2) == 0,
                "must supply even number of items; received %s", items.length);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> void populate(Map<K, V> map, Object... items) {
        for (int i = 0; i < items.length; i += 2) {
            K key = (K) items[i];
            V value = (V) items[i + 1];
            map.put(key, value);
        }
    }

}
