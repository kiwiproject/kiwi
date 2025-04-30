package org.kiwiproject.collect;

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
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
                .filter(isString())
                .map(String.class::cast)
                .toList();
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
                .filter(isString())
                .map(String.class::cast)
                .sorted()
                .toList();
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
                .toList();

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
                .filter(isString())
                .map(String.class::cast)
                .toList();
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
                .filter(isString())
                .map(String.class::cast)
                .sorted()
                .toList();
        assertThat(unmodifiableTreeMap.keySet()).containsExactlyElementsOf(expectedKeys);

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> unmodifiableTreeMap.put("eight", 8))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Predicate<Object> isString() {
        return String.class::isInstance;
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

    @Nested
    class GetAsStringOrNull {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getAsStringOrNull(null, "aKey")),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getAsStringOrNull(Map.of("count", 42), null))
            );
        }

        @Test
        void shouldReturnNull_WhenMapDoesNotContainKey() {
            var map = Map.of("a", 1, "b", 2);
            assertThat(KiwiMaps.getAsStringOrNull(map, "c")).isNull();
        }

        @Test
        void shouldReturnNull_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", 12);
            assertThat(KiwiMaps.getAsStringOrNull(map, "a")).isNull();
        }

        @Test
        void shouldReturnValue_WhenItIsString() {
            var map = Map.of(1, "a", 2, "b", 3, "c");
            assertThat(KiwiMaps.getAsStringOrNull(map, 2)).isEqualTo("b");
        }

        @Test
        void shouldReturnValueAsString_WhenItIsNotString() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            assertThat(KiwiMaps.getAsStringOrNull(map, "c")).isEqualTo("3");
        }
    }

    @Nested
    class GetAsString {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getAsString(null, "aKey", "theDefault")),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getAsString(Map.of("count", 42), null, "theDefault"))
            );
        }

        @Test
        void shouldReturnDefaultValue_WhenMapDoesNotContainKey() {
            var map = Map.of("a", 1, "b", 2);
            var defaultValue = "42";
            assertThat(KiwiMaps.getAsString(map, "c", defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnDefaultValue_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", 12);
            var defaultValue = "84";
            assertThat(KiwiMaps.getAsString(map, "a", defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnValue_WhenItIsString() {
            var map = Map.of(1, "a", 2, "b", 3, "c");
            var defaultValue = "z";
            assertThat(KiwiMaps.getAsString(map, 3, defaultValue)).isEqualTo("c");
        }

        @Test
        void shouldReturnValueAsString_WhenItIsNotString() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            var defaultValue = "z";
            assertThat(KiwiMaps.getAsString(map, "b", defaultValue)).isEqualTo("2");
        }
    }

    @Nested
    class GetTypedValueOrNull {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedValueOrNull(null, "aKey", Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedValueOrNull(Map.of("count", 42L), null, Long.class))
            );
        }

        @Test
        void shouldReturnNull_WhenMapDoesNotContainKey() {
            var map = Map.of("a", 1, "b", 2);
            assertThat(KiwiMaps.getTypedValueOrNull(map, "c", Integer.class)).isNull();
        }

        @Test
        void shouldReturnNull_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", 12L);
            assertThat(KiwiMaps.getTypedValueOrNull(map, "a", Long.class)).isNull();
        }

        @Test
        void shouldReturnValue_WhenItExists() {
            var map = Map.of(1, "a", 2, "b", 3, "c");
            assertThat(KiwiMaps.getTypedValueOrNull(map, 2, String.class)).isEqualTo("b");
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not an integer", "b", "also not an integer");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedValueOrNull(map, "a", Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.lang.Integer")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedValue {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedValue(null, "aKey", Integer.class, 84)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedValue(Map.of("count", 42L), null, Long.class, 126L))
            );
        }

        @Test
        void shouldReturnDefaultValue_WhenMapDoesNotContainKey() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            var defaultValue = 42;
            assertThat(KiwiMaps.getTypedValue(map, "d", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnDefaultValue_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", 12);
            var defaultValue = 84;
            assertThat(KiwiMaps.getTypedValue(map, "a", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            var defaultValue = 84;
            assertThat(KiwiMaps.getTypedValue(map, "c", Integer.class, defaultValue)).isEqualTo(3);
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not an integer", "b", "also not an integer");
            var defaultValue = 42;

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedValue(map, "a", Integer.class, defaultValue))
                    .withMessage("Cannot cast value for key 'a' to type java.lang.Integer")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedCollection {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollection(null, "aKey", Integer.class, List.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollection(Map.of("items", List.of(1, 2, 3)), null, Integer.class, List.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollection(Map.of("items", List.of(1, 2, 3)), "items", null, List.of()))
            );
        }

        @Test
        void shouldReturnDefaultValue_WhenMapDoesNotContainKey() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            var defaultValue = List.of(7, 8, 9);
            assertThat(KiwiMaps.getTypedCollection(map, "c", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnDefaultValue_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", List.of(1, 2, 3));
            var defaultValue = List.of(4, 5, 6);
            assertThat(KiwiMaps.getTypedCollection(map, "a", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            var defaultValue = List.of(7, 8, 9);
            assertThat(KiwiMaps.getTypedCollection(map, "a", Integer.class, defaultValue)).isEqualTo(List.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a collection", "b", "also not a collection");
            var defaultValue = List.of(1, 2, 3);

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedCollection(map, "a", Integer.class, defaultValue))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Collection")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedCollectionOrNull {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollectionOrNull(null, "aKey", Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollectionOrNull(Map.of("items", List.of(1, 2, 3)), null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollectionOrNull(Map.of("items", List.of(1, 2, 3)), "items", null))
            );
        }

        @Test
        void shouldReturnNull_WhenMapDoesNotContainKey() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedCollectionOrNull(map, "c", Integer.class)).isNull();
        }

        @Test
        void shouldReturnNull_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", List.of(1, 2, 3));
            assertThat(KiwiMaps.getTypedCollectionOrNull(map, "a", Integer.class)).isNull();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedCollectionOrNull(map, "a", Integer.class)).isEqualTo(List.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a collection", "b", "also not a collection");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedCollectionOrNull(map, "a", Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Collection")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedCollectionOrEmpty {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollectionOrEmpty(null, "aKey", Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollectionOrEmpty(Map.of("items", List.of(1, 2, 3)), null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedCollectionOrEmpty(Map.of("items", List.of(1, 2, 3)), "items", null))
            );
        }

        @Test
        void shouldReturnEmptyCollection_WhenMapDoesNotContainKey() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedCollectionOrEmpty(map, "c", Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnEmptyCollection_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", List.of(1, 2, 3));
            assertThat(KiwiMaps.getTypedCollectionOrEmpty(map, "a", Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedCollectionOrEmpty(map, "a", Integer.class)).isEqualTo(List.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a collection", "b", "also not a collection");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedCollectionOrEmpty(map, "a", Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Collection")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedList {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedList(null, "aKey", Integer.class, List.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedList(Map.of("items", List.of(1, 2, 3)), null, Integer.class, List.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedList(Map.of("items", List.of(1, 2, 3)), "items", null, List.of()))
            );
        }

        @Test
        void shouldReturnDefaultValue_WhenMapDoesNotContainKey() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            var defaultValue = List.of(7, 8, 9);
            assertThat(KiwiMaps.getTypedList(map, "c", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnDefaultValue_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", List.of(1, 2, 3));
            var defaultValue = List.of(4, 5, 6);
            assertThat(KiwiMaps.getTypedList(map, "a", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            var defaultValue = List.of(7, 8, 9);
            assertThat(KiwiMaps.getTypedList(map, "a", Integer.class, defaultValue)).isEqualTo(List.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a list", "b", "also not a list");
            var defaultValue = List.of(1, 2, 3);

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedList(map, "a", Integer.class, defaultValue))
                    .withMessage("Cannot cast value for key 'a' to type java.util.List")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedListOrNull {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedListOrNull(null, "aKey", Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedListOrNull(Map.of("items", List.of(1, 2, 3)), null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedListOrNull(Map.of("items", List.of(1, 2, 3)), "items", null))
            );
        }

        @Test
        void shouldReturnNull_WhenMapDoesNotContainKey() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedListOrNull(map, "c", Integer.class)).isNull();
        }

        @Test
        void shouldReturnNull_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", List.of(1, 2, 3));
            assertThat(KiwiMaps.getTypedListOrNull(map, "a", Integer.class)).isNull();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedListOrNull(map, "a", Integer.class)).isEqualTo(List.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a list", "b", "also not a list");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedListOrNull(map, "a", Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.List")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedListOrEmpty {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedListOrEmpty(null, "aKey", Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedListOrEmpty(Map.of("items", List.of(1, 2, 3)), null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedListOrEmpty(Map.of("items", List.of(1, 2, 3)), "items", null))
            );
        }

        @Test
        void shouldReturnEmptyList_WhenMapDoesNotContainKey() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedListOrEmpty(map, "c", Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnEmptyList_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", List.of(1, 2, 3));
            assertThat(KiwiMaps.getTypedListOrEmpty(map, "a", Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", List.of(1, 2, 3), "b", List.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedListOrEmpty(map, "a", Integer.class)).isEqualTo(List.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a list", "b", "also not a list");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedListOrEmpty(map, "a", Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.List")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedSet {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSet(null, "aKey", Integer.class, Set.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSet(Map.of("items", Set.of(1, 2, 3)), null, Integer.class, Set.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSet(Map.of("items", Set.of(1, 2, 3)), "items", null, Set.of()))
            );
        }

        @Test
        void shouldReturnDefaultValue_WhenMapDoesNotContainKey() {
            var map = Map.of("a", Set.of(1, 2, 3), "b", Set.of(4, 5, 6));
            var defaultValue = Set.of(7, 8, 9);
            assertThat(KiwiMaps.getTypedSet(map, "c", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnDefaultValue_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", Set.of(1, 2, 3));
            var defaultValue = Set.of(4, 5, 6);
            assertThat(KiwiMaps.getTypedSet(map, "a", Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", Set.of(1, 2, 3), "b", Set.of(4, 5, 6));
            var defaultValue = Set.of(7, 8, 9);
            assertThat(KiwiMaps.getTypedSet(map, "a", Integer.class, defaultValue)).isEqualTo(Set.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a set", "b", "also not a set");
            var defaultValue = Set.of(1, 2, 3);

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedSet(map, "a", Integer.class, defaultValue))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Set")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedSetOrNull {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSetOrNull(null, "aKey", Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSetOrNull(Map.of("items", Set.of(1, 2, 3)), null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSetOrNull(Map.of("items", Set.of(1, 2, 3)), "items", null))
            );
        }

        @Test
        void shouldReturnNull_WhenMapDoesNotContainKey() {
            var map = Map.of("a", Set.of(1, 2, 3), "b", Set.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedSetOrNull(map, "c", Integer.class)).isNull();
        }

        @Test
        void shouldReturnNull_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", Set.of(1, 2, 3));
            assertThat(KiwiMaps.getTypedSetOrNull(map, "a", Integer.class)).isNull();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", Set.of(1, 2, 3), "b", Set.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedSetOrNull(map, "a", Integer.class)).isEqualTo(Set.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a set", "b", "also not a set");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedSetOrNull(map, "a", Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Set")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedSetOrEmpty {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSetOrEmpty(null, "aKey", Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSetOrEmpty(Map.of("items", Set.of(1, 2, 3)), null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedSetOrEmpty(Map.of("items", Set.of(1, 2, 3)), "items", null))
            );
        }

        @Test
        void shouldReturnEmptySet_WhenMapDoesNotContainKey() {
            var map = Map.of("a", Set.of(1, 2, 3), "b", Set.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedSetOrEmpty(map, "c", Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnEmptySet_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", Set.of(1, 2, 3));
            assertThat(KiwiMaps.getTypedSetOrEmpty(map, "a", Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var map = Map.of("a", Set.of(1, 2, 3), "b", Set.of(4, 5, 6));
            assertThat(KiwiMaps.getTypedSetOrEmpty(map, "a", Integer.class)).isEqualTo(Set.of(1, 2, 3));
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a set", "b", "also not a set");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedSetOrEmpty(map, "a", Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Set")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedMap {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMap(null, "aKey", String.class, Integer.class, Map.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMap(Map.of("items", Map.of("a", 1, "b", 2)), null, String.class, Integer.class, Map.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMap(Map.of("items", Map.of("a", 1, "b", 2)), "items", null, Integer.class, Map.of())),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMap(Map.of("items", Map.of("a", 1, "b", 2)), "items", String.class, null, Map.of()))
            );
        }

        @Test
        void shouldReturnDefaultValue_WhenMapDoesNotContainKey() {
            var map = Map.of("a", Map.of("x", 1, "y", 2), "b", Map.of("p", 3, "q", 4));
            var defaultValue = Map.of("default", 42);
            assertThat(KiwiMaps.getTypedMap(map, "c", String.class, Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnDefaultValue_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", Map.of("p", 3, "q", 4));
            var defaultValue = Map.of("default", 42);
            assertThat(KiwiMaps.getTypedMap(map, "a", String.class, Integer.class, defaultValue)).isEqualTo(defaultValue);
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var nestedMap = Map.of("x", 1, "y", 2);
            var map = Map.of("a", nestedMap, "b", Map.of("p", 3, "q", 4));
            var defaultValue = Map.of("default", 42);
            assertThat(KiwiMaps.getTypedMap(map, "a", String.class, Integer.class, defaultValue)).isEqualTo(nestedMap);
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a map", "b", "also not a map");
            var defaultValue = Map.of("default", 42);

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedMap(map, "a", String.class, Integer.class, defaultValue))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Map")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedMapOrNull {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrNull(null, "aKey", String.class, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrNull(Map.of("items", Map.of("a", 1, "b", 2)), null, String.class, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrNull(Map.of("items", Map.of("a", 1, "b", 2)), "items", null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrNull(Map.of("items", Map.of("a", 1, "b", 2)), "items", String.class, null))
            );
        }

        @Test
        void shouldReturnNull_WhenMapDoesNotContainKey() {
            var map = Map.of("a", Map.of("x", 1, "y", 2), "b", Map.of("p", 3, "q", 4));
            assertThat(KiwiMaps.getTypedMapOrNull(map, "c", String.class, Integer.class)).isNull();
        }

        @Test
        void shouldReturnNull_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", Map.of("p", 3, "q", 4));
            assertThat(KiwiMaps.getTypedMapOrNull(map, "a", String.class, Integer.class)).isNull();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var nestedMap = Map.of("x", 1, "y", 2);
            var map = Map.of("a", nestedMap, "b", Map.of("p", 3, "q", 4));
            assertThat(KiwiMaps.getTypedMapOrNull(map, "a", String.class, Integer.class)).isEqualTo(nestedMap);
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a map", "b", "also not a map");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedMapOrNull(map, "a", String.class, Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Map")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetTypedMapOrEmpty {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrEmpty(null, "aKey", String.class, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrEmpty(Map.of("items", Map.of("a", 1, "b", 2)), null, String.class, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrEmpty(Map.of("items", Map.of("a", 1, "b", 2)), "items", null, Integer.class)),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getTypedMapOrEmpty(Map.of("items", Map.of("a", 1, "b", 2)), "items", String.class, null))
            );
        }

        @Test
        void shouldReturnEmptyMap_WhenMapDoesNotContainKey() {
            var map = Map.of("a", Map.of("x", 1, "y", 2), "b", Map.of("p", 3, "q", 4));
            assertThat(KiwiMaps.getTypedMapOrEmpty(map, "c", String.class, Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnEmptyMap_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", Map.of("p", 3, "q", 4));
            assertThat(KiwiMaps.getTypedMapOrEmpty(map, "a", String.class, Integer.class)).isEmpty();
        }

        @Test
        void shouldReturnValue_WhenMapContainsKeyOfExpectedType() {
            var nestedMap = Map.of("x", 1, "y", 2);
            var map = Map.of("a", nestedMap, "b", Map.of("p", 3, "q", 4));
            assertThat(KiwiMaps.getTypedMapOrEmpty(map, "a", String.class, Integer.class)).isEqualTo(nestedMap);
        }

        @Test
        void shouldThrowMapTypeMismatchException_WhenCastFails() {
            var map = Map.of("a", "not a map", "b", "also not a map");

            assertThatExceptionOfType(MapTypeMismatchException.class)
                    .isThrownBy(() -> KiwiMaps.getTypedMapOrEmpty(map, "a", String.class, Integer.class))
                    .withMessage("Cannot cast value for key 'a' to type java.util.Map")
                    .withCauseExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetConvertedValue {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getConvertedValue(null, "aKey", Object::toString))
                            .withMessage("map must not be null"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getConvertedValue(Map.of("count", 42L), null, Object::toString))
                            .withMessage("key must not be null"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getConvertedValue(Map.of("count", 42L), "count", null))
                            .withMessage("converter function must not be null")
            );
        }

        @Test
        void shouldApplyConverter_WhenMapContainsKey() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            var result = KiwiMaps.getConvertedValue(map, "b", value -> "Number: " + value);
            assertThat(result).isEqualTo("Number: 2");
        }

        @Test
        void shouldApplyConverter_WhenMapDoesNotContainKey() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            var result = KiwiMaps.getConvertedValue(map, "d", value -> value == null ? "Key not found" : value.toString());
            assertThat(result).isEqualTo("Key not found");
        }

        @Test
        void shouldApplyConverter_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", 12);
            var result = KiwiMaps.getConvertedValue(map, "a", value -> value == null ? "NULL" : value.toString());
            assertThat(result).isEqualTo("NULL");
        }

        @Test
        void shouldPropagateException_WhenConverterThrows() {
            var map = Map.of("a", "not a number", "b", "also not a number");

            assertThatThrownBy(() -> KiwiMaps.getConvertedValue(map, "a", value -> Integer.parseInt(value.toString())))
                    .isInstanceOf(NumberFormatException.class)
                    .hasMessageContaining("For input string: \"not a number\"");
        }
    }

    @Nested
    class GetConvertedValueWithFallback {

        @Test
        void shouldRequireArguments() {
            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getConvertedValueWithFallback(null, "aKey", Object::toString, (v, e) -> "fallback"))
                            .withMessage("map must not be null"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getConvertedValueWithFallback(Map.of("count", 42L), null, Object::toString, (v, e) -> "fallback"))
                            .withMessage("key must not be null"),
                    () -> {
                        Function<Object, String> nullConverter = null;
                        //noinspection ConstantValue
                        assertThatIllegalArgumentException()
                                .isThrownBy(() -> KiwiMaps.getConvertedValueWithFallback(Map.of("count", 42L), "count", nullConverter, (v, e) -> "fallback"))
                                .withMessage("converter function must not be null");
                    },
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiMaps.getConvertedValueWithFallback(Map.of("count", 42L), "count", Object::toString, null))
                            .withMessage("fallbackConverter must not be null")
            );
        }

        @Test
        void shouldApplyConverter_WhenMapContainsKey() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            var result = KiwiMaps.getConvertedValueWithFallback(map, "b",
                    value -> "Number: " + value,
                    (value, ex) -> "Fallback: " + value);
            assertThat(result).isEqualTo("Number: 2");
        }

        @Test
        void shouldApplyConverter_WhenMapDoesNotContainKey() {
            var map = Map.of("a", 1, "b", 2, "c", 3);
            var result = KiwiMaps.getConvertedValueWithFallback(map, "d",
                    value -> value == null ? "Key not found" : value.toString(),
                    (value, ex) -> "Fallback: " + value);
            assertThat(result).isEqualTo("Key not found");
        }

        @Test
        void shouldApplyConverter_WhenMapContainsNullValueForKey() {
            var map = KiwiMaps.newHashMap("a", null, "b", 12);
            var result = KiwiMaps.getConvertedValueWithFallback(map, "a",
                    value -> value == null ? "NULL" : value.toString(),
                    (value, ex) -> "Fallback: " + value);
            assertThat(result).isEqualTo("NULL");
        }

        @Test
        void shouldApplyFallbackConverter_WhenConverterThrows() {
            var map = Map.of("a", "not a number", "b", "also not a number");

            var result = KiwiMaps.getConvertedValueWithFallback(map, "a",
                    value -> Integer.parseInt(value.toString()),
                    (value, ex) -> 42);

            assertThat(result).isEqualTo(42);
        }

        @Test
        void shouldIncludeExceptionInFallback() {
            var map = Map.of("a", "not a number", "b", "also not a number");

            var result = KiwiMaps.getConvertedValueWithFallback(map, "a",
                    value -> Integer.parseInt(value.toString()),
                    (value, ex) -> ex instanceof NumberFormatException ? 84 : 42);

            assertThat(result).isEqualTo(84);
        }

        @Test
        void shouldHandleComplexConversionScenario() {
            var map = KiwiMaps.newHashMap(
                    "validInt", "42",
                    "invalidInt", "not a number",
                    "nullValue", null
            );

            assertAll(
                    () -> {
                        var validResult = KiwiMaps.getConvertedValueWithFallback(map, "validInt",
                                value -> Integer.parseInt(value.toString()),
                                (value, ex) -> -1);
                        assertThat(validResult)
                                .describedAs("valid values should be converted")
                                .isEqualTo(42);
                    },

                    () -> {
                        var invalidResult = KiwiMaps.getConvertedValueWithFallback(map, "invalidInt",
                                value -> Integer.parseInt(value.toString()),
                                (value, ex) -> -1);
                        assertThat(invalidResult)
                                .describedAs("invalid values should be converted using fallback")
                                .isEqualTo(-1);
                    },

                    () -> {
                        var nullResult = KiwiMaps.getConvertedValueWithFallback(map, "nullValue",
                                value -> value == null ? 0 : Integer.parseInt(value.toString()),
                                (value, ex) -> -1);
                        assertThat(nullResult)
                                .describedAs("null values should be handled by converter")
                                .isZero();
                    },

                    () -> {
                        var missingResult = KiwiMaps.getConvertedValueWithFallback(map, "nonExistent",
                                value -> value == null ? 0 : Integer.parseInt(value.toString()),
                                (value, ex) -> -1);
                        assertThat(missingResult)
                                .describedAs("missing keys should be handled by fallback")
                                .isZero();
                    }
            );
        }
    }
}
