package org.kiwiproject.collect;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.reverseOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

public class KiwiListsTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testIsNullOrEmpty_WhenNull() {
        assertThat(KiwiLists.isNullOrEmpty(null)).isTrue();
    }

    @Test
    public void testIsNullOrEmpty_WhenEmpty() {
        assertThat(KiwiLists.isNullOrEmpty(newArrayList())).isTrue();
    }

    @Test
    public void testIsNullOrEmpty_WhenContainsElements() {
        List<String> fruits = newListOfFruits();
        assertThat(KiwiLists.isNullOrEmpty(fruits)).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenNull() {
        assertThat(KiwiLists.isNotNullOrEmpty(null)).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenEmpty() {
        assertThat(KiwiLists.isNotNullOrEmpty(newArrayList())).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenContainsElements() {
        List<String> fruits = newListOfFruits();
        assertThat(KiwiLists.isNotNullOrEmpty(fruits)).isTrue();
    }

    @Test
    public void testHasOneElement_WhenNull() {
        assertThat(KiwiLists.hasOneElement(null)).isFalse();
    }

    @Test
    public void testHasOneElement_WhenEmpty() {
        assertThat(KiwiLists.hasOneElement(new ArrayList<>())).isFalse();
    }

    @Test
    public void testHasOneElement_WhenHasOnlyOneElement() {
        assertThat(KiwiLists.hasOneElement(newArrayList("kiwi"))).isTrue();
    }

    @Test
    public void testHasOneElement_WhenHasMoreThanOneElement() {
        assertThat(KiwiLists.hasOneElement(newListOfFruits())).isFalse();
    }

    @Test
    public void testSorted_WithNaturalOrdering() {
        List<String> fruits = newListOfFruits();
        List<String> sortedFruits = KiwiLists.sorted(fruits);
        softly.assertThat(sortedFruits).isNotSameAs(fruits);
        softly.assertThat(sortedFruits)
                .containsExactlyElementsOf(newSortedListOfFruits());
    }

    @Test
    public void testSorted_UsingCustomComparator() {
        List<String> fruits = newListOfFruits();
        List<String> sortedFruits = KiwiLists.sorted(fruits, reverseOrder());
        softly.assertThat(sortedFruits).isNotSameAs(fruits);
        softly.assertThat(sortedFruits)
                .containsExactly("orange", "kiwi", "guava", "banana", "apple");
    }

    @Test
    public void testFirstIfPresent_WhenNullList() {
        assertThatThrownBy(() -> KiwiLists.firstIfPresent(null))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void testFirstIfPresent_WhenEmptyList() {
        assertThat(KiwiLists.firstIfPresent(new ArrayList<>())).isEmpty();
    }

    @Test
    public void testFirstIfPresent_WhenSingleElementList() {
        assertThat(KiwiLists.firstIfPresent(newArrayList("kiwi")))
                .isPresent()
                .contains("kiwi");
    }

    @Test
    public void testFirstIfPresent_WhenTwoElementList() {
        assertThat(KiwiLists.firstIfPresent(newArrayList("kiwi", "guava")))
                .isPresent()
                .contains("kiwi");
    }

    @Test
    public void testFirst() {
        List<String> items = newListOfFruits();
        assertThat(KiwiLists.first(items)).isEqualTo(items.get(0));
    }

    @Test
    public void testSecond() {
        List<String> items = newListOfFruits();
        assertThat(KiwiLists.second(items)).isEqualTo(items.get(1));
    }

    @Test
    public void testThird() {
        List<String> items = newListOfFruits();
        assertThat(KiwiLists.third(items)).isEqualTo(items.get(2));
    }

    @Test
    public void testFourth() {
        List<String> items = newListOfFruits();
        assertThat(KiwiLists.fourth(items)).isEqualTo(items.get(3));
    }

    @Test
    public void testFifth() {
        List<String> items = newListOfFruits();
        assertThat(KiwiLists.fifth(items)).isEqualTo(items.get(4));
    }

    @Test
    public void testSecondToLast() {
        List<String> items = newListOfFruits();
        String expected = items.get(items.size() - 2);
        softly.assertThat(KiwiLists.secondToLast(items)).isEqualTo(expected);
        softly.assertThat(KiwiLists.penultimate(items)).isEqualTo(expected);
    }

    @Test
    public void testLast() {
        List<String> items = newListOfFruits();
        String expected = items.get(items.size() - 1);
        assertThat(KiwiLists.last(items)).isEqualTo(expected);
    }

    @Test
    public void testLastIfPresent_WhenEmptyList() {
        assertThat(KiwiLists.lastIfPresent(new ArrayList<>())).isEmpty();
    }

    @Test
    public void testLastIfPresent_WhenSingleElementList() {
        assertThat(KiwiLists.lastIfPresent(newArrayList("kiwi")))
                .isPresent()
                .contains("kiwi");
    }

    @Test
    public void testLastIfPresent_WhenTwoElementList() {
        assertThat(KiwiLists.lastIfPresent(newArrayList("kiwi", "guava")))
                .isPresent()
                .contains("guava");
    }

    @Test
    public void testNth_WhenNegativeNumber() {
        assertThatThrownBy(() -> KiwiLists.nth(newListOfFruits(), -1))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("number must be positive");
    }

    @Test
    public void testNth_WhenNullList() {
        assertThatThrownBy(() -> KiwiLists.nth(null, 42))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("items cannot be null");
    }

    @Test
    public void testNth_WhenEmptyList() {
        assertThatThrownBy(() -> KiwiLists.nth(new ArrayList<>(), 8))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("expected at least 8 items (actual size: 0)");
    }

    @Test
    public void testNth_WhenNotEnoughElementsForRequestedPosition() {
        List<String> fruits = newListOfFruits();
        int number = fruits.size() + 1;
        assertThatThrownBy(() -> KiwiLists.nth(fruits, number))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("expected at least %d items (actual size: %d)", number, fruits.size());
    }

    @Test
    public void testNth_WhenJustEnoughElementsForRequestedPosition() {
        List<String> fruits = newListOfFruits();
        int number = fruits.size();
        assertThat(KiwiLists.nth(fruits, number)).isEqualTo(fruits.get(number - 1));
    }

    private static List<String> newListOfFruits() {
        return newArrayList("orange", "apple", "kiwi", "banana", "guava");
    }

    private List<String> newSortedListOfFruits() {
        return newArrayList("apple", "banana", "guava", "kiwi", "orange");
    }

}