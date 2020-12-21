package org.kiwiproject.collect;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.kiwiproject.util.BlankStringArgumentsProvider;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@DisplayName("KiwiMaps")
class KiwiMapsTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testIsNullOrEmpty_WhenNull() {
        assertThat(KiwiMaps.isNullOrEmpty(null)).isTrue();
    }

    @Test
    void testIsNullOrEmpty_WhenEmpty() {
        assertThat(KiwiMaps.isNullOrEmpty(new HashMap<>())).isTrue();
    }

    @Test
    void testIsNullOrEmpty_WhenContainsSomeMappings() {
        Map<String, Integer> wordsToNumbers = newWordNumberMap();
        assertThat(KiwiMaps.isNullOrEmpty(wordsToNumbers)).isFalse();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testIsNotNullOrEmpty_WhenNull() {
        assertThat(KiwiMaps.isNotNullOrEmpty(null)).isFalse();
    }

    @Test
    void testIsNotNullOrEmpty_WhenEmpty() {
        assertThat(KiwiMaps.isNotNullOrEmpty(new HashMap<>())).isFalse();
    }

    @Test
    void testIsNotNullOrEmpty_WhenContainsSomeMappings() {
        Map<String, Integer> wordsToNumbers = newWordNumberMap();
        assertThat(KiwiMaps.isNotNullOrEmpty(wordsToNumbers)).isTrue();
    }

    @Test
    void testNewHashMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> hashMap = KiwiMaps.newHashMap(items);
        assertThat(hashMap)
                .isExactlyInstanceOf(HashMap.class)
                .containsAllEntriesOf(newWordNumberMap());
    }

    @Test
    void testNewHashMap_WhenNoItems() {
        assertThat(KiwiMaps.newHashMap()).isEmpty();
    }

    @Test
    void testNewHashMap_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newHashMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must be an even number of items (received 3)");
    }

    @Test
    void testNewLinkedHashMap() {
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
    void testNewLinkedHashMap_WhenNoItems() {
        assertThat(KiwiMaps.newLinkedHashMap()).isEmpty();
    }

    @Test
    void testNewLinkedHashMap_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newLinkedHashMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must be an even number of items (received 3)");
    }

    @Test
    void testNewTreeMap() {
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
    void testNewTreeMap_WhenNoItems() {
        assertThat(KiwiMaps.<String, Object>newTreeMap()).isEmpty();
        assertThat(KiwiMaps.<Integer, Object>newTreeMap()).isEmpty();
        assertThat(KiwiMaps.<Path, Object>newTreeMap()).isEmpty();
        assertThat(KiwiMaps.<LocalDate, Object>newTreeMap()).isEmpty();
        assertThat(KiwiMaps.<ZonedDateTime, Object>newTreeMap()).isEmpty();
    }

    @Test
    void testNewTreeMap_StringKeys_To_ObjectValues() {
        SortedMap<String, Object> treeMap = KiwiMaps.newTreeMap("foo", 42, "bar", 84, "baz", 126);

        List<String> transformed = treeMap.entrySet()
                .stream()
                .map(entry -> entry.getKey() + entry.getValue())
                .collect(toList());

        assertThat(transformed).containsExactly("bar84", "baz126", "foo42");
    }

    @Test
    void testNewTreeMap_ZonedDateTimeKeys_To_ObjectValues() {
        var id = ZoneId.of("America/New_York");
        var dt1 = ZonedDateTime.now(id).minusMinutes(30);
        var dt2 = ZonedDateTime.now(id).minusMinutes(20);
        var dt3 = ZonedDateTime.now(id).minusMinutes(10);

        var sortedEvents = KiwiMaps.<ZonedDateTime, String>newTreeMap(dt2, "event2", dt3, "event3", dt1, "event1");

        assertThat(sortedEvents).containsExactly(
                entry(dt1, "event1"),
                entry(dt2, "event2"),
                entry(dt3, "event3")
        );
    }

    @Test
    void testNewTreeMap_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newTreeMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must be an even number of items (received 3)");
    }

    @Test
    void testNewConcurrentHashMap() {
        Object[] items = wordToNumberArray();
        ConcurrentMap<String, Integer> treeMap = KiwiMaps.newConcurrentHashMap(items);
        assertThat(treeMap)
                .isExactlyInstanceOf(ConcurrentHashMap.class)
                .containsAllEntriesOf(newWordNumberMap());
    }

    @Test
    void testNewConcurrentHash_WhenNoItems() {
        assertThat(KiwiMaps.newConcurrentHashMap()).isEmpty();
    }

    @Test
    void testNewConcurrentHash_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiMaps.newConcurrentHashMap("one", 1, "two"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("must be an even number of items (received 3)");
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

    @Nested
    class KeyExistsWithNullValue {

        @Nested
        class ShouldReturnTrue {

            @Test
            void whenKeyExistsAndHasNullValue() {
                var key = "aKey";
                var map = KiwiMaps.<String, Object>newHashMap(key, null);
                assertThat(KiwiMaps.keyExistsWithNullValue(map, key)).isTrue();
            }

            @Test
            void whenObjectKeyExistsAndHasNullValue() {
                var key = new Object();
                var map = KiwiMaps.newHashMap(key, null);
                assertThat(KiwiMaps.keyExistsWithNullValue(map, key)).isTrue();
            }
        }

        @Nested
        class ShouldReturnFalse {

            @Test
            void whenMapIsNull() {
                assertThat(KiwiMaps.keyExistsWithNullValue(null, "aKey")).isFalse();
            }

            @Test
            void whenMapIsEmpty() {
                assertThat(KiwiMaps.keyExistsWithNullValue(Map.of(), "aKey")).isFalse();
            }

            @Test
            void whenKeyIsNull() {
                assertThat(KiwiMaps.keyExistsWithNullValue(Map.of(), null)).isFalse();
            }

            @ParameterizedTest
            @ArgumentsSource(BlankStringArgumentsProvider.class)
            void whenKeyIsBlank(String value) {
                assertThat(KiwiMaps.keyExistsWithNullValue(Map.of(), value)).isFalse();
            }

            @Test
            void whenKeyDoesNotExist() {
                var map = Map.of("aKey", "aValue");
                assertThat(KiwiMaps.keyExistsWithNullValue(map, "anotherKey")).isFalse();
            }

            @Test
            void whenKeyExistsAndHasANonNullValue() {
                var key = "aKey";
                assertThat(KiwiMaps.keyExistsWithNullValue(Map.of(key, "aValue"), key)).isFalse();
            }

            @Test
            void whenObjectKeyExistsAndHasANonNullValue() {
                var key = new Object();
                assertThat(KiwiMaps.keyExistsWithNullValue(Map.of(key, "aValue"), key)).isFalse();
            }
        }
    }

}