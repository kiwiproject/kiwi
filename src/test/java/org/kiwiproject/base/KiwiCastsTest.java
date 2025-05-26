package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DisplayName("KiwiCasts")
class KiwiCastsTest {

    @Nested
    class UncheckedCast {

        @Test
        void shouldCastObjectToString() {
            Object obj = "test string";
            String result = KiwiCasts.uncheckedCast(obj);
            assertThat(result).isEqualTo("test string");
        }

        @Test
        void shouldCastObjectToInteger() {
            Object obj = 42;
            Integer result = KiwiCasts.uncheckedCast(obj);
            assertThat(result).isEqualTo(42);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldCastObjectToBoolean(Object obj) {
            Boolean result = KiwiCasts.uncheckedCast(obj);
            assertThat(result).isEqualTo(obj);
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        @Test
        void shouldCastObjectToCollection() {
            Collection<String> originalCollection = new ArrayList<>();
            originalCollection.add("item1");
            originalCollection.add("item2");

            Object obj = originalCollection;
            Collection<String> result = KiwiCasts.uncheckedCast(obj);

            assertThat(result).isEqualTo(originalCollection);
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        @Test
        void shouldCastObjectToList() {
            List<String> originalList = new ArrayList<>();
            originalList.add("item1");
            originalList.add("item2");

            Object obj = originalList;
            List<String> result = KiwiCasts.uncheckedCast(obj);
            
            assertThat(result).isEqualTo(originalList);
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        @Test
        void shouldCastObjectToSet() {
            Set<String> originalSet = new HashSet<>();
            originalSet.add("item1");
            originalSet.add("item2");

            Object obj = originalSet;
            Set<String> result = KiwiCasts.uncheckedCast(obj);

            assertThat(result).isEqualTo(originalSet);
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        @Test
        void shouldCastObjectToMap() {
            Map<String, Integer> originalMap = new HashMap<>();
            originalMap.put("key1", 1);
            originalMap.put("key2", 2);
            
            Object obj = originalMap;
            Map<String, Integer> result = KiwiCasts.uncheckedCast(obj);

            assertThat(result).isEqualTo(originalMap);
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldReturnNullWhenCastingNull() {
            Object obj = null;
            String result = KiwiCasts.uncheckedCast(obj);
            assertThat(result).isNull();
        }
    }
}
