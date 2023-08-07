package org.kiwiproject.stream;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Stream;

@DisplayName("KiwiMultimapCollectors")
class KiwiMultimapCollectorsTest {

    @Nested
    class ShouldCollectSimpleMap {

        @Test
        void toArrayListMultimap() {
            var collector = KiwiMultimapCollectors.toArrayListMultimap();
            assertCollectorFromSimpleMap(collector);
        }

        @Test
        void toHashMultimap() {
            var collector = KiwiMultimapCollectors.toHashMultimap();
            assertCollectorFromSimpleMap(collector);
        }

        @Test
        void toLinkedHashMultimap() {
            var collector = KiwiMultimapCollectors.toLinkedHashMultimap();
            assertCollectorFromSimpleMap(collector);
        }
    }

    private <A extends Multimap<Object, Object>> void assertCollectorFromSimpleMap(
            Collector<Map.Entry<Object, Object>, A, A> collector) {

        var map = Map.<Object, Object>of(
                "fruits", "guava",
                "cars", "Tesla"
        );

        var multimap = map.entrySet().stream().collect(collector);

        assertThat(multimap.keySet()).containsExactlyInAnyOrder("fruits", "cars");
        assertThat(multimap.get("fruits")).containsExactly("guava");
        assertThat(multimap.get("cars")).containsExactly("Tesla");
    }

    @Nested
    class ShouldCollectParallelEntryStreamUsingCombiner {

        @Test
        void toArrayListMultimap() {
            var collector = KiwiMultimapCollectors.toArrayListMultimap();
            assertCollectorFromMapEntryParallelStream(collector);
        }

        @Test
        void toHashMultimap() {
            var collector = KiwiMultimapCollectors.toHashMultimap();
            assertCollectorFromMapEntryParallelStream(collector);
        }

        @Test
        void toLinkedHashMultimap() {
            var collector = KiwiMultimapCollectors.toLinkedHashMultimap();
            assertCollectorFromMapEntryParallelStream(collector);
        }
    }

    private <A extends Multimap<Object, Object>> void assertCollectorFromMapEntryParallelStream(
            Collector<Map.Entry<Object, Object>, A, A> collector) {

        var entryStream = Stream.<Map.Entry<Object, Object>>of(
                Pair.of("fruits", "guava"),
                Pair.of("cars", "Tesla"),
                Pair.of("fruits", "orange"),
                Pair.of("fruits", "apple"),
                Pair.of("foods", "pizza"),
                Pair.of("cars", "Ferrari"),
                Pair.of("foods", "hamburgers"),
                Pair.of("foods", "sushi"),
                Pair.of("cars", "Porsche")
        );

        var multimap = entryStream.parallel().collect(collector);

        assertThat(multimap.keySet()).containsExactlyInAnyOrder("cars", "foods", "fruits");
        assertThat(multimap.get("foods")).containsExactlyInAnyOrder("hamburgers", "pizza", "sushi");
        assertThat(multimap.get("fruits")).containsExactlyInAnyOrder("apple", "guava", "orange");
        assertThat(multimap.get("cars")).containsExactlyInAnyOrder("Ferrari", "Porsche", "Tesla");
    }

    @Nested
    class ShouldCollectMultivaluedMap {

        @Test
        void toArrayListMultimap() {
            var collector = KiwiMultimapCollectors.toArrayListMultimap();
            assertCollectorFromMultivaluedMap(collector);
        }

        @Test
        void toHashMultimap() {
            var collector = KiwiMultimapCollectors.toHashMultimap();
            assertCollectorFromMultivaluedMap(collector);
        }

        @Test
        void toLinkedHashMultimap() {
            var collector = KiwiMultimapCollectors.toLinkedHashMultimap();
            assertCollectorFromMultivaluedMap(collector);
        }
    }

    private <A extends Multimap<Object, Object>> void assertCollectorFromMultivaluedMap(
            Collector<Map.Entry<Object, Object>, A, A> collector) {

        var original = ArrayListMultimap.create();
        original.putAll("fruits", List.of("apple", "orange", "kiwi"));
        original.put("cars", "Tesla");

        var multimap = original.entries().stream().collect(collector);

        assertThat(multimap.keySet()).containsExactlyInAnyOrder("fruits", "cars");
        assertThat(multimap.get("fruits")).containsExactlyInAnyOrder("apple", "orange", "kiwi");
        assertThat(multimap.get("cars")).containsExactly("Tesla");
    }

    @Nested
    class ShouldCollectMultivaluedMapFiltering {

        @Test
        void toArrayListMultimap() {
            var collector = KiwiMultimapCollectors.toArrayListMultimap();
            assertCollectorFromMultivaluedMapWithFiltering(collector);
        }

        @Test
        void toHashMultimap() {
            var collector = KiwiMultimapCollectors.toHashMultimap();
            assertCollectorFromMultivaluedMapWithFiltering(collector);
        }

        @Test
        void toLinkedHashMultimap() {
            var collector = KiwiMultimapCollectors.toLinkedHashMultimap();
            assertCollectorFromMultivaluedMapWithFiltering(collector);
        }
    }

    private <A extends Multimap<Object, Object>> void assertCollectorFromMultivaluedMapWithFiltering(
            Collector<Map.Entry<Object, Object>, A, A> collector) {

        var original = ArrayListMultimap.create();
        original.putAll("fruits", List.of("apple", "orange", "kiwi"));
        original.putAll("foods", List.of("hamburgers", "pizza"));
        original.putAll("drinks", List.of("beer", "water"));
        original.put("cars", "Tesla");

        var multimap = original.entries()
                .stream()
                .filter(entry -> !Objects.equals(entry.getKey(), "fruits"))
                .collect(collector);

        assertThat(multimap.keySet()).containsExactlyInAnyOrder("foods", "drinks", "cars");
        assertThat(multimap.get("foods")).containsExactlyInAnyOrder("hamburgers", "pizza");
        assertThat(multimap.get("drinks")).containsExactlyInAnyOrder("beer", "water");
        assertThat(multimap.get("cars")).containsExactly("Tesla");
    }
}
