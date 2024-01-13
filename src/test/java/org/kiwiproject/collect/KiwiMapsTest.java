package org.kiwiproject.collect;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.collect.KiwiMaps.newHashMap;

import lombok.experimental.StandardException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.kiwiproject.util.BlankStringSource;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@DisplayName("KiwiMaps")
class KiwiMapsTest {

    @SuppressWarnings("ConstantValue")
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

    @SuppressWarnings("ConstantValue")
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
        Map<String, Integer> hashMap = newHashMap(items);
        assertThat(hashMap)
                .isExactlyInstanceOf(HashMap.class)
                .containsAllEntriesOf(newWordNumberMap());
    }

    @Test
    void testNewHashMap_WhenNoItems() {
        assertThat(newHashMap()).isEmpty();
    }

    @Test
    void testNewHashMap_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> newHashMap("one", 1, "two"))
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
        ConcurrentMap<String, Integer> concurrentMap = KiwiMaps.newConcurrentHashMap(items);
        assertThat(concurrentMap)
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
                var map = newHashMap(key, null);
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
            @BlankStringSource
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

    @Test
    void shouldCreateUnmodifiableHashMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> unmodifiableHashMap = KiwiMaps.newUnmodifiableHashMap(items);
        assertThat(unmodifiableHashMap).containsAllEntriesOf(newWordNumberMap());

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> unmodifiableHashMap.put("six", 6))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldPermitNullsInUnmodifiableHashMap() {
        Map<String, Integer> map = KiwiMaps.newUnmodifiableHashMap(
                "null", null, "one", 1, "two", 2, null, 42);

        assertThat(map).contains(
                entry("null", null),
                entry("one", 1),
                entry("two", 2),
                entry(null, 42)
        );
    }

    @Test
    void shouldCreateUnmodifiableLinkedHashMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> unmodifiableLinkedHashMap = KiwiMaps.newUnmodifiableLinkedHashMap(items);
        assertThat(unmodifiableLinkedHashMap).containsAllEntriesOf(newWordNumberMap());
        List<String> expectedKeys = Arrays.stream(items)
                .filter(obj -> obj instanceof String)
                .map(String.class::cast)
                .collect(toList());
        assertThat(unmodifiableLinkedHashMap.keySet()).containsExactlyElementsOf(expectedKeys);

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> unmodifiableLinkedHashMap.put("seven", 7))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldPermitNullsInUnmodifiableLinkedHashMap() {
        Map<String, Integer> map = KiwiMaps.newUnmodifiableLinkedHashMap(
                "null", null, "one", 1, "two", 2, null, 42);

        assertThat(map).contains(
                entry("null", null),
                entry("one", 1),
                entry("two", 2),
                entry(null, 42)
        );
    }

    @Test
    void shouldCreateUnmodifiableTreeMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> unmodifiableTreeMap = KiwiMaps.newUnmodifiableTreeMap(items);
        assertThat(unmodifiableTreeMap).containsAllEntriesOf(newWordNumberMap());
        List<String> expectedKeys = Arrays.stream(items)
                .filter(obj -> obj instanceof String)
                .map(String.class::cast)
                .sorted()
                .collect(toList());
        assertThat(unmodifiableTreeMap.keySet()).containsExactlyElementsOf(expectedKeys);

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> unmodifiableTreeMap.put("eight", 8))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldCreateUnmodifiableConcurrentHashMap() {
        Object[] items = wordToNumberArray();
        Map<String, Integer> unmodifiableConcurrentHashMap = KiwiMaps.newUnmodifiableConcurrentHashMap(items);
        assertThat(unmodifiableConcurrentHashMap).containsAllEntriesOf(newWordNumberMap());

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> unmodifiableConcurrentHashMap.put("nine", 9))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Nested
    class KeyExistsWithNonNullValue {

        @Nested
        class ShouldReturnTrue {

            @Test
            void whenKeyExistsAndHasANonNullValue() {
                var key = "aKey";
                assertThat(KiwiMaps.keyExistsWithNonNullValue(Map.of(key, "aValue"), key)).isTrue();
            }

            @Test
            void whenObjectKeyExistsAndHasANonNullValue() {
                var key = new Object();
                assertThat(KiwiMaps.keyExistsWithNonNullValue(Map.of(key, "aValue"), key)).isTrue();
            }
        }

        @Nested
        class ShouldReturnFalse {

            @Test
            void whenMapIsNull() {
                assertThat(KiwiMaps.keyExistsWithNonNullValue(null, "aKey")).isFalse();
            }

            @Test
            void whenMapIsEmpty() {
                assertThat(KiwiMaps.keyExistsWithNonNullValue(Map.of(), "aKey")).isFalse();
            }

            @Test
            void whenKeyIsNull() {
                assertThat(KiwiMaps.keyExistsWithNonNullValue(Map.of(), null)).isFalse();
            }

            @ParameterizedTest
            @BlankStringSource
            void whenKeyIsBlank(String value) {
                assertThat(KiwiMaps.keyExistsWithNonNullValue(Map.of(), value)).isFalse();
            }

            @Test
            void whenKeyDoesNotExist() {
                var map = Map.of("aKey", "aValue");
                assertThat(KiwiMaps.keyExistsWithNonNullValue(map, "anotherKey")).isFalse();
            }

            @Test
            void whenKeyExistsAndHasNullValue() {
                var key = "aKey";
                var map = KiwiMaps.<String, Object>newUnmodifiableHashMap(key, null);
                assertThat(KiwiMaps.keyExistsWithNonNullValue(map, key)).isFalse();
            }

            @Test
            void whenObjectKeyExistsAndHasNullValue() {
                var key = new Object();
                var map = KiwiMaps.newUnmodifiableHashMap(key, null);
                assertThat(KiwiMaps.keyExistsWithNonNullValue(map, key)).isFalse();
            }
        }
    }

    @Nested
    class GetOrThrow {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrow(null, "aKey")),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrow(Map.of("count", 42), null))
            );
        }

        @Test
        void shouldReturnValue() {
            var fruitCounts = Map.of("orange", 12, "apple", 6);

            assertAll(
                    () -> assertThat(KiwiMaps.getOrThrow(fruitCounts, "orange"))
                            .isEqualTo(12),
                    () -> assertThat(KiwiMaps.getOrThrow(fruitCounts, "apple"))
                            .isEqualTo(6)
            );
        }

        @Test
        void shouldThrowNoSuchElementException_WhenKeyDoesNotExist() {
            var fruitCounts = newHashMap("orange", 12, "apple", 6);

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> KiwiMaps.getOrThrow(fruitCounts, "dragon fruit"))
                    .withMessage("key 'dragon fruit' does not exist in map");
        }

        @Test
        void shouldThrowNoSuchElementException_WhenValueIsNull() {
            var fruitCounts = newHashMap("orange", 12, "apple", 6, "papaya", null);

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> KiwiMaps.getOrThrow(fruitCounts, "papaya"))
                    .withMessage("value associated with key 'papaya' is null");
        }
    }

    @Nested
    class GetOrThrowCustomRuntimeException {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrow(null, "aKey", IllegalStateException::new)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrow(Map.of("count", 42), null, IllegalStateException::new)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrow(Map.of("count", 42), "count", null))
            );
        }

        @Test
        void shouldReturnValue() {
            var fruitCounts = Map.of("mango", 10, "kiwi", 7);

            assertAll(
                    () -> assertThat(KiwiMaps.getOrThrow(fruitCounts, "mango", RuntimeException::new))
                            .isEqualTo(10),
                    () -> assertThat(KiwiMaps.getOrThrow(fruitCounts, "kiwi", IllegalStateException::new))
                            .isEqualTo(7)
            );
        }

        @Test
        void shouldThrowCustomRuntimeException_WhenKeyDoesNotExist() {
            var fruitCounts = newHashMap("orange", 12, "apple", 6);

            Supplier<IllegalStateException> exceptionSupplier =
                    () -> new IllegalStateException("missing key or null value");
            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> KiwiMaps.getOrThrow(fruitCounts, "dragon fruit", exceptionSupplier))
                    .withMessage("missing key or null value");
        }

        @Test
        void shouldThrowCustomRuntimeException_WhenValueIsNull() {
            var fruitCounts = newHashMap("orange", 12, "apple", 6, "papaya", null);

            Supplier<MyRuntimeException> exceptionSupplier =
                    () -> new MyRuntimeException("missing key or null value");
            assertThatExceptionOfType(MyRuntimeException.class)
                    .isThrownBy(() -> KiwiMaps.getOrThrow(fruitCounts, "papaya", exceptionSupplier))
                    .withMessage("missing key or null value");
        }

        @StandardException
        static class MyRuntimeException extends RuntimeException {
        }
    }

    @Nested
    class GetOrThrowCustomCheckedException {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrowChecked(null, "aKey", Exception::new)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrowChecked(Map.of("count", 42), null, Exception::new)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getOrThrowChecked(Map.of("count", 42), "count", null))
            );
        }

        @Test
        void shouldReturnValue() {
            var fruitCounts = Map.of("papaya", 4, "guava", 3);

            assertAll(
                    () -> assertThat(KiwiMaps.getOrThrowChecked(fruitCounts, "papaya", Exception::new))
                            .isEqualTo(4),
                    () -> assertThat(KiwiMaps.getOrThrowChecked(fruitCounts, "guava", Exception::new))
                            .isEqualTo(3)
            );
        }

        @Test
        void shouldThrowCustomException_WhenKeyDoesNotExist() {
            var fruitCounts = newHashMap("orange", 12, "apple", 6);

            Supplier<Exception> exceptionSupplier =
                    () -> new Exception("missing key or null value");
            assertThatExceptionOfType(Exception.class)
                    .isThrownBy(() -> KiwiMaps.getOrThrowChecked(fruitCounts, "dragon fruit", exceptionSupplier))
                    .withMessage("missing key or null value");
        }

        @Test
        void shouldThrowCustomException_WhenValueIsNull() {
            var fruitCounts = newHashMap("orange", 12, "apple", 6, "papaya", null);

            Supplier<MyException> exceptionSupplier =
                    () -> new MyException("missing key or null value");
            assertThatExceptionOfType(MyException.class)
                    .isThrownBy(() -> KiwiMaps.getOrThrowChecked(fruitCounts, "papaya", exceptionSupplier))
                    .withMessage("missing key or null value");
        }

        @StandardException
        static class MyException extends Exception {
        }
    }
}
