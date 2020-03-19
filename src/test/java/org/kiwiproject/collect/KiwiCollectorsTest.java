package org.kiwiproject.collect;

import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.collect.KiwiCollectors.toEnumMap;
import static org.kiwiproject.collect.KiwiCollectors.toEnumSet;
import static org.kiwiproject.collect.KiwiCollectors.toImmutableListBuilder;
import static org.kiwiproject.collect.KiwiCollectors.toLinkedMap;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.WordUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.stream.Stream;

@DisplayName("KiwiCollectors")
class KiwiCollectorsTest {

    @Nested
    class ToImmutableListBuilder {

        @Test
        void shouldReturnImmutableListBuilder() {
            var list = firstFiftyEvenIntegers()
                    .collect(toImmutableListBuilder())
                    .build();

            assertListContents(list);
        }

        @Test
        void shouldCombineFromParallelStream() {
            var list = firstFiftyEvenIntegers()
                    .parallel()
                    .collect(toImmutableListBuilder())
                    .build();

            assertListContents(list);
        }

        private void assertListContents(ImmutableList<Integer> list) {
            var expectedList = firstFiftyEvenIntegers().collect(toList());

            assertThat(list).isEqualTo(expectedList);
        }

        @SuppressWarnings("deprecation") // intentionally using deprecated add() method to demo exception generation
        @Test
        void shouldDefinitelyReturnImmutableList() {
            var list = firstFiftyEvenIntegers()
                    .collect(toImmutableListBuilder())
                    .build();

            assertThatThrownBy(() -> list.add(52)).isExactlyInstanceOf(UnsupportedOperationException.class);
        }

        private Stream<Integer> firstFiftyEvenIntegers() {
            return Stream.iterate(0, value -> value + 2).limit(50);
        }
    }

    enum Season {
        FALL, WINTER, SPRING, SUMMER
    }

    @Nested
    class ToEnumSet {

        @Test
        void shouldReturnEmptySet_WhenGiven_NoEnumValues() {
            var seasons = Arrays.stream(new Season[0])
                    .collect(toEnumSet(Season.class));

            assertThat(seasons).isEmpty();
        }

        @Test
        void shouldReturnFullSet_WhenGiven_AllEnumValues() {
            var seasons = Arrays.stream(Season.values())
                    .collect(toEnumSet(Season.class));

            assertThat(seasons)
                    .containsExactlyInAnyOrder(Season.FALL, Season.WINTER, Season.SPRING, Season.SUMMER);
        }

        @Test
        void shouldReturnPartialSet_WhenGiven_SomeEnumValues() {
            var seasons = Stream.of(Season.SPRING, Season.SUMMER)
                    .collect(toEnumSet(Season.class));

            assertThat(seasons)
                    .containsExactlyInAnyOrder(Season.SPRING, Season.SUMMER);
        }

        @Test
        void shouldHandleDuplicateEnumValues() {
            var seasons = Stream.of(Season.SPRING, Season.SUMMER, Season.SPRING)
                    .collect(toEnumSet(Season.class));

            assertThat(seasons)
                    .containsExactlyInAnyOrder(Season.SPRING, Season.SUMMER);
        }

        @Test
        void shouldCombineFromParallelStream() {
            var seasons = Arrays.stream(Season.values())
                    .parallel()
                    .collect(toEnumSet(Season.class));

            assertThat(seasons)
                    .containsExactlyInAnyOrder(Season.FALL, Season.WINTER, Season.SPRING, Season.SUMMER);
        }
    }

    @Nested
    class ToEnumMap {

        @Test
        void shouldNotPermitDuplicateKeys() {
            var stream = Stream.of(Season.SPRING, Season.SUMMER, Season.SPRING);

            assertThatIllegalStateException()
                    .isThrownBy(() -> {
                        //noinspection ResultOfMethodCallIgnored
                        stream.collect((toEnumMap(
                                Season.class,
                                season -> season,
                                season -> WordUtils.capitalize(season.name().toLowerCase(Locale.ENGLISH)))));

                    }).withMessage("Duplicate key. Attempted to merge values Spring and Spring");
        }

        @Test
        void shouldCollectToEnumMap() {
            var stream = Stream.of(
                    Pair.of(Season.SPRING, "pollen"),
                    Pair.of(Season.SUMMER, "hot"),
                    Pair.of(Season.FALL, "leaves"),
                    Pair.of(Season.WINTER, "snow"));

            var seasonWordAssociations = stream.collect(toEnumMap(
                    Season.class,
                    Pair::getLeft,
                    Pair::getRight
            ));

            // Note: EnumMap ordering is based on order in which the enum constants are declared (see EnumMap JavaDocs)
            assertThat(seasonWordAssociations)
                    .isExactlyInstanceOf(EnumMap.class)
                    .hasSize(4)
                    .containsExactly(
                            entry(Season.FALL, "leaves"),
                            entry(Season.WINTER, "snow"),
                            entry(Season.SPRING, "pollen"),
                            entry(Season.SUMMER, "hot")
                    );
        }
    }

    @Nested
    class ToLinkedMap {

        @Test
        void shouldNotPermitDuplicateKeys() {
            var stream = Stream.of(
                    Pair.of("red", 3),
                    Pair.of("blue", 4),
                    Pair.of("red", 5),
                    Pair.of("green", 5)
            );

            assertThatIllegalStateException()
                    .isThrownBy(() -> {
                        //noinspection ResultOfMethodCallIgnored
                        stream.collect(toLinkedMap(Pair::getLeft, Pair::getRight));
                    }).withMessage("Duplicate key. Attempted to merge values 3 and 5");
        }

        @Test
        void shouldCollectToLinkedHashMap() {
            var stream = Stream.of(
                    Pair.of("red", 3),
                    Pair.of("orange", 6),
                    Pair.of("yellow", 6),
                    Pair.of("green", 5),
                    Pair.of("blue", 4),
                    Pair.of("indigo", 6),
                    Pair.of("violet", 6)
            );

            var colorLengths = stream.collect(toLinkedMap(Pair::getLeft, Pair::getRight));

            assertThat(colorLengths)
                    .isExactlyInstanceOf(LinkedHashMap.class)
                    .hasSize(7)
                    .containsExactly(
                            entry("red", 3),
                            entry("orange", 6),
                            entry("yellow", 6),
                            entry("green", 5),
                            entry("blue", 4),
                            entry("indigo", 6),
                            entry("violet", 6)
                    );
        }
    }
}
