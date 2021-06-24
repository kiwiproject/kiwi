package org.kiwiproject.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;
import java.util.Map;

@DisplayName("KiwiMultivaluedMaps")
class KiwiMultivaluedMapsTest {

    @Nested
    class NewMultivaluedMap {

        @Test
        void shouldNotAllowOddNumberOfItems() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMultivaluedMaps.newMultivaluedMap("color"));
        }

        @Test
        void shouldCreateEmptyMultivaluedMap_WhenNoItems() {
            var mvMap = KiwiMultivaluedMaps.newMultivaluedMap();
            assertThat(mvMap).isEmpty();
        }

        /**
         * @implNote As of AssertJ 3.20.0, the implementation of containsOnly was changed significantly in PR
         * https://github.com/assertj/assertj-core/pull/2167/ and as a result broke this test when it used containsOnly
         * to verify the map entries. Long story short, it is caused by the new AssertJ implementation cloning the
         * MultivaluedMap in a way that it flattens it to a single-valued Map, and the values (which are List) are
         * wrapped inside another List, so the comparison completely fails.
         */
        @Test
        void shouldCreateMultivaluedMap() {
            var mvMap = KiwiMultivaluedMaps.newMultivaluedMap(
                    "color", "red",
                    "food", "pizza",
                    "car", "tesla",
                    "food", "steak");

            assertThat(mvMap).hasSize(3)
                    .contains(entry("color", List.of("red")))
                    .contains(entry("food", List.of("pizza", "steak")))
                    .contains(entry("car", List.of("tesla")));
        }
    }

    @Nested
    class NewSingleValuedParameterMap {

        @Test
        void shouldNotAllowOddNumberOfItems() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMultivaluedMaps.newSingleValuedParameterMap("food"));
        }

        @Test
        void shouldCreateEmptyMultivaluedMap_WhenNoItems() {
            var mvMap = KiwiMultivaluedMaps.newSingleValuedParameterMap();
            assertThat(mvMap).isEmpty();
        }

        /**
         * @implNote As of AssertJ 3.20.0, the implementation of containsOnly was changed significantly in PR
         * https://github.com/assertj/assertj-core/pull/2167/ and as a result broke this test when it used containsOnly
         * to verify the map entries. Long story short, it is caused by the new AssertJ implementation cloning the
         * MultivaluedMap in a way that it flattens it to a single-valued Map, and the values (which are List) are
         * wrapped inside another List, so the comparison completely fails.
         */
        @Test
        void shouldCreateMultivaluedMap_WithOnlyOneValuePerKey() {
            var mvMap = KiwiMultivaluedMaps.newSingleValuedParameterMap(
                    "color", "yellow",
                    "food", "pizza",
                    "food", "steak",
                    "car", "tesla",
                    "food", "hamburgers",
                    "car", "ferrari");

            assertThat(mvMap).hasSize(3)
                    .contains(entry("color", List.of("yellow")))
                    .contains(entry("food", List.of("hamburgers")))
                    .contains(entry("car", List.of("ferrari")));
        }
    }

    @Nested
    class ToSingleValuedParameterMap {

        @Test
        void shouldNotAllowNullArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMultivaluedMaps.toSingleValuedParameterMap(null));
        }

        @Test
        void shouldConvertToEmptyMultivaluedMap_WhenEmptyMapArgument() {
            var mvMap = KiwiMultivaluedMaps.toSingleValuedParameterMap(new MultivaluedHashMap<>());
            assertThat(mvMap).isEmpty();
        }

        @Test
        void shouldConvertToMultivaluedMap_WithOnlyOneValuePerKey() {
            var mvMap = new MultivaluedHashMap<String, String>();
            mvMap.put("color", List.of("red", "green", "blue"));
            mvMap.put("food", List.of("pizza", "hamburgers", "hot dogs"));
            mvMap.putSingle("car", "tesla");

            var parameterMap = KiwiMultivaluedMaps.toSingleValuedParameterMap(mvMap);

            assertThat(parameterMap).containsOnly(
                    entry("color", "red"),
                    entry("food", "pizza"),
                    entry("car", "tesla")
            );
        }
    }

    @Nested
    class ToMultimap {

        @Test
        void shouldNotAllowNullArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMultivaluedMaps.toMultimap(null));
        }

        @Test
        void shouldConvertToEmptyMultimap_WhenEmptyMapArgument() {
            var multimap = KiwiMultivaluedMaps.toMultimap(new MultivaluedHashMap<>());
            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        void shouldConvertToMultimap_FromMultivaluedMap_ContainingOnlyOneValuePerKey() {
            var mvMap = new MultivaluedHashMap<>(Map.of(
                    "color", "red",
                    "food", "pizza",
                    "pet", "cat"
            ));

            var multimap = KiwiMultivaluedMaps.toMultimap(mvMap);

            assertThat(multimap.keySet()).containsOnly("color", "food", "pet");
            assertThat(multimap.entries()).containsOnly(
                    entry("color", "red"),
                    entry("food", "pizza"),
                    entry("pet", "cat")
            );
        }

        @Test
        void shouldConvertToMultimap_FromMultivaluedMap_ContainingMultipleValuesPerKey() {
            var mvMap = new MultivaluedHashMap<String, String>();
            mvMap.put("color", List.of("red", "green", "blue"));
            mvMap.put("food", List.of("pizza", "hamburgers", "hot dogs"));
            mvMap.putSingle("car", "tesla");
            mvMap.put("pet", List.of("cat", "guinea pig"));

            var multimap = KiwiMultivaluedMaps.toMultimap(mvMap);

            assertThat(multimap.keySet()).containsOnly("color", "food", "car", "pet");
            assertThat(multimap.entries()).containsOnly(
                    entry("color", "red"),
                    entry("color", "green"),
                    entry("color", "blue"),
                    entry("food", "pizza"),
                    entry("food", "hamburgers"),
                    entry("food", "hot dogs"),
                    entry("car", "tesla"),
                    entry("pet", "cat"),
                    entry("pet", "guinea pig")
            );
        }
    }
}
