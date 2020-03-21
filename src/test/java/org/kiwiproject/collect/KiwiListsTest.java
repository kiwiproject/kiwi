package org.kiwiproject.collect;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
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
        items = Arrays.asList("guava", "kiwi", "guarana", "limeberry", "chupa-chupa");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testIsNullOrEmpty_WhenNull() {
        assertThat(KiwiLists.isNullOrEmpty(null)).isTrue();
    }

    @Test
    void testIsNullOrEmpty_WhenEmpty() {
        assertThat(KiwiLists.isNullOrEmpty(newArrayList())).isTrue();
    }

    @Test
    void testIsNullOrEmpty_WhenContainsElements() {
        assertThat(KiwiLists.isNullOrEmpty(items)).isFalse();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testIsNotNullOrEmpty_WhenNull() {
        assertThat(KiwiLists.isNotNullOrEmpty(null)).isFalse();
    }

    @Test
    void testIsNotNullOrEmpty_WhenEmpty() {
        assertThat(KiwiLists.isNotNullOrEmpty(newArrayList())).isFalse();
    }

    @Test
    void testIsNotNullOrEmpty_WhenContainsElements() {
        assertThat(KiwiLists.isNotNullOrEmpty(items)).isTrue();
    }

    @Test
    void testHasOneElement_WhenNull() {
        assertThat(KiwiLists.hasOneElement(null)).isFalse();
    }

    @Test
    void testHasOneElement_WhenEmpty() {
        assertThat(KiwiLists.hasOneElement(new ArrayList<>())).isFalse();
    }

    @Test
    void testHasOneElement_WhenHasOnlyOneElement() {
        assertThat(KiwiLists.hasOneElement(newArrayList("kiwi"))).isTrue();
    }

    @Test
    void testHasOneElement_WhenHasMoreThanOneElement() {
        assertThat(KiwiLists.hasOneElement(items)).isFalse();
    }

    @Test
    void testSorted_WhenEmptyList() {
        assertThat(KiwiLists.sorted(newArrayList())).isEmpty();
    }

    @Test
    void testSorted_WhenNullList() {
        assertThatThrownBy(() -> KiwiLists.sorted(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("items cannot be null");
    }

    @Test
    void testSorted_WhenHasItems() {
        var someIntegers = IntStream.iterate(1, n -> n + 1).limit(100).boxed().collect(toList());

        var randomIntegers = newArrayList(someIntegers);
        Collections.shuffle(randomIntegers);

        assertThat(KiwiLists.sorted(randomIntegers))
                .hasSize(100)
                .containsExactlyElementsOf(someIntegers);
    }

    @Test
    void testSorted_WithComparator_WhenEmptyList() {
        List<Integer> items = newArrayList();
        Comparator<Integer> comparator = Comparator.reverseOrder();

        assertThat(KiwiLists.sorted(items, comparator)).isEmpty();
    }

    @Test
    void testSorted_WithComparator_WhenNullList() {
        assertThatThrownBy(() -> KiwiLists.sorted(null, Comparator.<Integer>reverseOrder()))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("items cannot be null");
    }

    @Test
    void testSorted_WithComparator_WhenNullComparator() {
        assertThatThrownBy(() -> KiwiLists.sorted(newArrayList(), null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("Comparator cannot be null");
    }

    @Test
    void testSorted_WithComparator_WhenHasItems() {
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

    @Test
    void testFirstIfPresent_WhenNullList() {
        assertThat(KiwiLists.firstIfPresent(null)).isEmpty();
    }

    @Test
    void testFirstIfPresent_WhenEmptyList() {
        assertThat(KiwiLists.firstIfPresent(new ArrayList<>())).isEmpty();
    }

    @Test
    void testFirstIfPresent_WhenSingleElementList() {
        assertThat(KiwiLists.firstIfPresent(newArrayList("kiwi")))
                .isPresent()
                .contains("kiwi");
    }

    @Test
    void testFirstIfPresent_WhenTwoElementList() {
        assertThat(KiwiLists.firstIfPresent(newArrayList("kiwi", "guava")))
                .isPresent()
                .contains("kiwi");
    }

    @Test
    void testFirst() {
        assertThat(KiwiLists.first(items)).isEqualTo(items.get(0));
    }

    @Test
    void testSecond() {
        assertThat(KiwiLists.second(items)).isEqualTo(items.get(1));
    }

    @Test
    void testThird() {
        assertThat(KiwiLists.third(items)).isEqualTo(items.get(2));
    }

    @Test
    void testFourth() {
        assertThat(KiwiLists.fourth(items)).isEqualTo(items.get(3));
    }

    @Test
    void testFifth() {
        assertThat(KiwiLists.fifth(items)).isEqualTo(items.get(4));
    }

    @Test
    void testSecondToLast(SoftAssertions softly) {
        var expected = items.get(items.size() - 2);
        softly.assertThat(KiwiLists.secondToLast(items)).isEqualTo(expected);
        softly.assertThat(KiwiLists.penultimate(items)).isEqualTo(expected);
    }

    @Test
    void testLast() {
        var expected = items.get(items.size() - 1);
        assertThat(KiwiLists.last(items)).isEqualTo(expected);
    }

    @Test
    void testLastIfPresent_WhenEmptyList() {
        assertThat(KiwiLists.lastIfPresent(new ArrayList<>())).isEmpty();
    }

    @Test
    void testLastIfPresent_WhenSingleElementList() {
        assertThat(KiwiLists.lastIfPresent(newArrayList("kiwi")))
                .isPresent()
                .contains("kiwi");
    }

    @Test
    void testLastIfPresent_WhenTwoElementList() {
        assertThat(KiwiLists.lastIfPresent(newArrayList("kiwi", "guava")))
                .isPresent()
                .contains("guava");
    }

    @Test
    void testNth_WhenNegativeNumber() {
        assertThatThrownBy(() -> KiwiLists.nth(items, -1))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("number must be positive");
    }

    @Test
    void testNth_WhenNullList() {
        assertThatThrownBy(() -> KiwiLists.nth(null, 42))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("items cannot be null");
    }

    @Test
    void testNth_WhenEmptyList() {
        assertThatThrownBy(() -> KiwiLists.nth(new ArrayList<>(), 8))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("expected at least 8 items (actual size: 0)");
    }

    @Test
    void testNth_WhenNotEnoughElementsForRequestedPosition() {
        int number = items.size() + 1;
        assertThatThrownBy(() -> KiwiLists.nth(items, number))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("expected at least %d items (actual size: %d)", number, items.size());
    }

    @Test
    void testNth_WhenJustEnoughElementsForRequestedPosition() {
        int number = items.size();
        assertThat(KiwiLists.nth(items, number)).isEqualTo(items.get(number - 1));
    }

    @Test
    void testCheckMinimumSize_WhenNullArgument() {
        assertThatThrownBy(() -> KiwiLists.checkMinimumSize(null, 10))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void testDistinct(SoftAssertions softly) {
        softly.assertThat(KiwiLists.distinct(newArrayList(1, 2, 3))).hasSize(3);
        softly.assertThat(KiwiLists.distinct(newArrayList(1, 1, 1))).hasSize(1);
        softly.assertThat(KiwiLists.distinct(newArrayList("a", "b", "b"))).hasSize(2);
    }

    @Test
    void testDistinct_NullThrowsException(SoftAssertions softly) {
        softly.assertThatThrownBy(() -> KiwiLists.distinct(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("collection can not be null");
    }

    @Test
    void testDistinctOrNull_NullIsAllowed(SoftAssertions softly) {
        softly.assertThat(KiwiLists.distinctOrNull(null)).isNull();
    }

    @Test
    void testCircularListOffset() {
        assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 0))
                .containsExactly("zero", "one", "two", "three");

        assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 1))
                .containsExactly("one", "two", "three", "zero");

        assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 3))
                .containsExactly("three", "zero", "one", "two");
    }

    @Test
    void testCircularListOffset_OffsetWraps() {
        assertThat(KiwiLists.newListStartingAtCircularOffset(newArrayList("zero", "one", "two", "three"), 4))
                .containsExactly("zero", "one", "two", "three");
    }
}