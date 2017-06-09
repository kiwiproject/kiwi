package org.kiwiproject.collect;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KiwiMapsTest {

    @Test
    public void testIsNullOrEmpty_WhenNull() {
        assertThat(KiwiMaps.isNullOrEmpty(null)).isTrue();
    }

    @Test
    public void testIsNullOrEmpty_WhenEmpty() {
        assertThat(KiwiMaps.isNullOrEmpty(new HashMap<>())).isTrue();
    }

    @Test
    public void testIsNullOrEmpty_WhenContainsSomeMappings() {
        Map<String, Integer> wordsToNumbers = newWordNumberMap();
        assertThat(KiwiMaps.isNullOrEmpty(wordsToNumbers)).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenNull() {
        assertThat(KiwiMaps.isNotNullOrEmpty(null)).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenEmpty() {
        assertThat(KiwiMaps.isNotNullOrEmpty(new HashMap<>())).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenContainsSomeMappings() {
        Map<String, Integer> wordsToNumbers = newWordNumberMap();
        assertThat(KiwiMaps.isNotNullOrEmpty(wordsToNumbers)).isTrue();
    }

    @Test
    public void testNewHashMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> hashMap = KiwiMaps.newHashMap(items);
        assertThat(hashMap)
                .isExactlyInstanceOf(HashMap.class)
                .containsAllEntriesOf(newWordNumberMap());
    }

    @Test
    public void testNewHashMap_WhenNoItems() {
        assertThat(KiwiMaps.newHashMap()).isEmpty();
    }

    @Test
    public void testNewHashMap_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newHashMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must supply even number of items; received 3");
    }

    @Test
    public void testNewLinkedHashMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> linkedHashMap = KiwiMaps.newLinkedHashMap(items);
        assertThat(linkedHashMap)
                .isExactlyInstanceOf(LinkedHashMap.class)
                .containsAllEntriesOf(newWordNumberMap());
        List<String> expectedKeys = Arrays.stream(items)
                .filter(obj -> obj instanceof String)
                .map(String.class::cast)
                .collect(toList());
        assertThat(linkedHashMap.keySet()).containsExactlyElementsOf(expectedKeys);
    }

    @Test
    public void testNewLinkedHashMap_WhenNoItems() {
        assertThat(KiwiMaps.newLinkedHashMap()).isEmpty();
    }

    @Test
    public void testNewLinkedHashMap_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newLinkedHashMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must supply even number of items; received 3");
    }

    @Test
    public void testNewTreeMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> treeMap = KiwiMaps.newTreeMap(items);
        assertThat(treeMap)
                .isExactlyInstanceOf(TreeMap.class)
                .containsAllEntriesOf(newWordNumberMap());
        List<String> expectedKeys = Arrays.stream(items)
                .filter(obj -> obj instanceof String)
                .map(String.class::cast)
                .sorted()
                .collect(toList());
        assertThat(treeMap.keySet()).containsExactlyElementsOf(expectedKeys);
    }

    @Test
    public void testNewTreeMap_WhenNoItems() {
        assertThat(KiwiMaps.newTreeMap()).isEmpty();
    }

    @Test
    public void testNewTreeMap_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newTreeMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must supply even number of items; received 3");
    }

    @Test
    public void testNewConcurrentHashMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> treeMap = KiwiMaps.newConcurrentHashMap(items);
        assertThat(treeMap)
                .isExactlyInstanceOf(ConcurrentHashMap.class)
                .containsAllEntriesOf(newWordNumberMap());
    }

    @Test
    public void testNewConcurrentHash_WhenNoItems() {
        assertThat(KiwiMaps.newConcurrentHashMap()).isEmpty();
    }

    @Test
    public void testNewConcurrentHash_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newConcurrentHashMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must supply even number of items; received 3");
    }

    private Object[] wordToNumberArray() {
        return new Object[]{
                "one", 1,
                "two", 2,
                "three", 3,
                "four", 4,
                "five", 5
        };
    }

    private Map<String, Integer> newWordNumberMap() {
        Map<String, Integer> words = new HashMap<>();
        words.put("one", 1);
        words.put("two", 2);
        words.put("three", 3);
        words.put("four", 4);
        words.put("five", 5);
        return words;
    }

}