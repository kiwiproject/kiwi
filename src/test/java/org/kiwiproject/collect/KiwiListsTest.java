package org.kiwiproject.collect;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.junit.jupiter.WhiteBoxTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@DisplayName("KiwiLists")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiListsTest {

    private List<String> items;

    @BeforeEach
    void setUp() {
        items = List.of("guava", "kiwi", "guarana", "limeberry", "chupa-chupa");
    }

    @Nested
    class IsNullOrEmpty {

        @SuppressWarnings("ConstantConditions")
        @Test
        void shouldBeTrue_WhenNullArg() {
            assertThat(KiwiLists.isNullOrEmpty(null)).isTrue();
        }

        @Test
        void shouldBeTrue_WhenEmptyList() {
            assertThat(KiwiLists.isNullOrEmpty(newArrayList())).isTrue();
        }

        @Test
        void shouldBeFalse_WhenContainsElements() {
            assertThat(KiwiLists.isNullOrEmpty(items)).isFalse();
        }
    }

    @Nested
    class IsNotNullOrEmpty {

        @SuppressWarnings("ConstantConditions")
        @Test
        void shouldBeFalse_WhenNull() {
            assertThat(KiwiLists.isNotNullOrEmpty(null)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenEmpty() {
            assertThat(KiwiLists.isNotNullOrEmpty(newArrayList())).isFalse();
        }

        @Test
        void shouldBeTrue_WhenContainsElements() {
            assertThat(KiwiLists.isNotNullOrEmpty(items)).isTrue();
        }
    }

    @Nested
    class HasOneElement {

        @Test
        void shouldBeFalse_WhenNull() {
            assertThat(KiwiLists.hasOneElement(null)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenEmpty() {
            assertThat(KiwiLists.hasOneElement(new ArrayList<>())).isFalse();
        }

        @Test
        void shouldBeTrue_WhenHasOnlyOneElement() {
            assertThat(KiwiLists.hasOneElement(newArrayList("kiwi"))).isTrue();
        }

        @Test
        void shouldBeFalse_WhenHasMoreThanOneElement() {
            assertThat(KiwiLists.hasOneElement(items)).isFalse();
        }
    }

    @Nested
    class Sorted {

        @Test
        void shouldBeEmpty_WhenEmptyList() {
            assertThat(KiwiLists.sorted(newArrayList())).isEmpty();
        }

        @Test
        void shouldThrow_WhenNullList() {
            assertThatThrownBy(() -> KiwiLists.sorted(null))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("items cannot be null");
        }

        @Test
        void shouldReturnSortedList_WhenHasItems() {
            var someIntegers = IntStream.iterate(1, n -> n + 1).limit(100).boxed().collect(toList());

            var randomIntegers = newArrayList(someIntegers);
            Collections.shuffle(randomIntegers);

            assertThat(KiwiLists.sorted(randomIntegers))
                    .hasSize(100)
                    .containsExactlyElementsOf(someIntegers);
        }
    }

    @Nested
    class SortedWithComparator {

        @Test
        void shouldBeEmpty_WhenEmptyList() {
            List<Integer> items = newArrayList();
            Comparator<Integer> comparator = Comparator.reverseOrder();

            assertThat(KiwiLists.sorted(items, comparator)).isEmpty();
        }

        @Test
        void shouldThrow_WhenNullList() {
            assertThatThrownBy(() -> KiwiLists.sorted(null, Comparator.<Integer>reverseOrder()))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("items cannot be null");
        }

        @Test
        void shouldThrow_WhenNullComparator() {
            assertThatThrownBy(() -> KiwiLists.sorted(newArrayList(), null))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("Comparator cannot be null");
        }

        @Test
        void shouldReturnSortedList_WhenHasItems() {
            var someIntegers = IntStream.iterate(1, n -> n + 1).limit(100).boxed().collect(toList());
            var reverseIntegers = newArrayList(someIntegers);
            Collections.reverse(reverseIntegers);

            var randomIntegers = newArrayList(someIntegers);
            Collections.shuffle(randomIntegers);

            var comparator = Comparator.<Integer>reverseOrder();
            assertThat(KiwiLists.sorted(randomIntegers, comparator))
                    .hasSize(100)
                    .containsExactlyElementsOf(reverseIntegers);
        }
    }

    @Nested
    class FirstIfPresent {

        @Test
        void shouldBeEmpty_WhenNullList() {
            assertThat(KiwiLists.firstIfPresent(null)).isEmpty();
        }

        @Test
        void shouldBeEmpty_WhenEmptyList() {
            assertThat(KiwiLists.firstIfPresent(new ArrayList<>())).isEmpty();
        }

        @Test
        void shouldContainFirst_WhenSingleElementList() {
            assertThat(KiwiLists.firstIfPresent(newArrayList("kiwi")))
                    .isPresent()
                    .contains("kiwi");
        }

        @Test
        void shouldContainFirst_WhenTwoElementList() {
            assertThat(KiwiLists.firstIfPresent(newArrayList("kiwi", "guava")))
                    .isPresent()
                    .contains("kiwi");
        }
    }

    @Nested
    class First {

        @Test
        void shouldReturnFirst() {
            assertThat(KiwiLists.first(items)).isEqualTo(items.get(0));
        }
    }

    @Nested
    class Second {

        @Test
        void shouldReturnSecond() {
            assertThat(KiwiLists.second(items)).isEqualTo(items.get(1));
        }
    }

    @Nested
    class Third {

        @Test
        void shouldReturnThird() {
            assertThat(KiwiLists.third(items)).isEqualTo(items.get(2));
        }
    }

    @Nested
    class Fourth {

        @Test
        void shouldReturnFourth() {
            assertThat(KiwiLists.fourth(items)).isEqualTo(items.get(3));
        }
    }

    @Nested
    class Fifth {

        @Test
        void shouldReturnFifth() {
            assertThat(KiwiLists.fifth(items)).isEqualTo(items.get(4));
        }
    }

    @Nested
    class SecondToLast {

        @Test
        void shouldReturnSecondToLast(SoftAssertions softly) {
            var expected = items.get(items.size() - 2);
            softly.assertThat(KiwiLists.secondToLast(items)).isEqualTo(expected);
            softly.assertThat(KiwiLists.penultimate(items)).isEqualTo(expected);
        }
    }

    @Nested
    class Last {

        @Test
        void shouldReturnLast() {
            var expected = items.get(items.size() - 1);
            assertThat(KiwiLists.last(items)).isEqualTo(expected);
        }
    }

    @Nested
    class LastIfPresent {

        @Test
        void shouldBeEmpty_WhenEmptyList() {
            assertThat(KiwiLists.lastIfPresent(new ArrayList<>())).isEmpty();
        }

        @Test
        void shouldContainLast_WhenSingleElementList() {
            assertThat(KiwiLists.lastIfPresent(newArrayList("kiwi")))
                    .isPresent()
                    .contains("kiwi");
        }

        @Test
        void shouldContainLast_WhenTwoElementList() {
            assertThat(KiwiLists.lastIfPresent(newArrayList("kiwi", "guava")))
                    .isPresent()
                    .contains("guava");
        }
    }

    @Nested
    class Nth {

        @Test
        void shouldThrow_WhenNegativeNumber() {
            assertThatThrownBy(() -> KiwiLists.nth(items, -1))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("number must be positive");
        }

        @Test
        void shouldThrow_WhenNullList() {
            assertThatThrownBy(() -> KiwiLists.nth(null, 42))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("items cannot be null");
        }

        @Test
        void shouldThrow_WhenEmptyList() {
            assertThatThrownBy(() -> KiwiLists.nth(new ArrayList<>(), 8))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("expected at least 8 items (actual size: 0)");
        }

        @Test
        void shouldThrow_WhenNotEnoughElementsForRequestedPosition() {
            int number = items.size() + 1;
            assertThatThrownBy(() -> KiwiLists.nth(items, number))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("expected at least %d items (actual size: %d)", number, items.size());
        }

        @Test
        void shouldReturnNth_WhenJustEnoughElementsForRequestedPosition() {
            int number = items.size();
            assertThat(KiwiLists.nth(items, number)).isEqualTo(items.get(number - 1));
        }
    }

    @Nested
    class CheckMinimumSize {

        @WhiteBoxTest
        void shouldThrow_WhenNullArgument() {
            assertThatThrownBy(() -> KiwiLists.checkMinimumSize(null, 10))
                    .isExactlyInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Distinct {

        @Test
        void shouldReturnDistinct(SoftAssertions softly) {
            softly.assertThat(KiwiLists.distinct(newArrayList(1, 2, 3))).hasSize(3);
            softly.assertThat(KiwiLists.distinct(newArrayList(1, 1, 1))).hasSize(1);
            softly.assertThat(KiwiLists.distinct(newArrayList("a", "b", "b"))).hasSize(2);
        }

        @Test
        void shouldThrow_WhenNullArg() {
            assertThatThrownBy(() -> KiwiLists.distinct(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("collection can not be null");
        }
    }

    @Nested
    class DistinctOrNull {

        @Test
        void shouldAllowNull() {
            assertThat(KiwiLists.distinctOrNull(null)).isNull();
        }
    }

    @Nested
    class CircularListOffset {

        @Test
        void shouldReturnNewListAtCorrectStartingOffset() {
            assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 0))
                    .containsExactly("zero", "one", "two", "three");

            assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 1))
                    .containsExactly("one", "two", "three", "zero");

            assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 3))
                    .containsExactly("three", "zero", "one", "two");
        }

        @Test
        void shouldWrapWhenOffsetIsBeyonfEndOfList() {
            assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 4))
                    .containsExactly("zero", "one", "two", "three");
        }
    }
}