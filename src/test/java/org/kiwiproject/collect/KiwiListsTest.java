package org.kiwiproject.collect;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.junit.jupiter.WhiteBoxTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
            softly.assertThat(KiwiLists.distinct(newArrayList())).isEmpty();
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
    class DistinctOrEmpty {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyList_GivenNullOrEmptyCollection(Set<String> strings) {
            assertThat(KiwiLists.distinctOrEmpty(strings)).isEmpty();
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

    @Nested
    class SubListExcludingFirst {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiLists.subListExcludingFirst(null));
        }

        @Test
        void shouldReturnEmptyList_WhenGivenEmptyList() {
            assertThat(KiwiLists.subListExcludingFirst(List.of())).isEmpty();
        }

        @Test
        void shouldReturnEmptyList_GivenOneElementList() {
            assertThat(KiwiLists.subListExcludingFirst(List.of(42))).isEmpty();
        }

        @Test
        void shouldReturnElementsExceptFirst(SoftAssertions softly) {
            softly.assertThat(KiwiLists.subListExcludingFirst(List.of(1, 2))).isEqualTo(List.of(2));
            softly.assertThat(KiwiLists.subListExcludingFirst(List.of(1, 2, 3))).isEqualTo(List.of(2, 3));
            softly.assertThat(KiwiLists.subListExcludingFirst(List.of(1, 2, 3, 4, 5))).isEqualTo(List.of(2, 3, 4, 5));
        }
    }

    @Nested
    class SubListExcludingLast {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiLists.subListExcludingLast(null));
        }

        @Test
        void shouldReturnEmptyList_WhenGivenEmptyList() {
            assertThat(KiwiLists.subListExcludingLast(List.of())).isEmpty();
        }

        @Test
        void shouldReturnEmptyList_GivenOneElementList() {
            assertThat(KiwiLists.subListExcludingLast(List.of(84))).isEmpty();
        }

        @Test
        void shouldReturnElementsExceptLast(SoftAssertions softly) {
            softly.assertThat(KiwiLists.subListExcludingLast(List.of(1, 2))).isEqualTo(List.of(1));
            softly.assertThat(KiwiLists.subListExcludingLast(List.of(1, 2, 3))).isEqualTo(List.of(1, 2));
            softly.assertThat(KiwiLists.subListExcludingLast(List.of(1, 2, 3, 4, 5))).isEqualTo(List.of(1, 2, 3, 4));
        }
    }

    @Nested
    class SubListFrom {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiLists.subListFrom(null, 2));
        }

        @Test
        void shouldThrow_GivenEmptyList() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiLists.subListFrom(List.of(), 1));
        }

        @Test
        void shouldThrow_GivenInvalidNumber() {
            assertThatIllegalArgumentException()
                    .describedAs("zero is not a valid starting element number")
                    .isThrownBy(() -> KiwiLists.subListFrom(List.of(1, 2, 3), 0));

            assertThatIllegalArgumentException()
                    .describedAs("4 is higher than the list size")
                    .isThrownBy(() -> KiwiLists.subListFrom(List.of(1, 2, 3), 4));
        }

        @Test
        void shouldReturnSingleElementList_WhenGivenSingleElementList() {
            assertThat(KiwiLists.subListFrom(List.of("a"), 1)).isEqualTo(List.of("a"));
        }

        @Test
        void shouldReturnElementsIncludingLast(SoftAssertions softly) {
            var aToE = List.of("a", "b", "c", "d", "e");
            softly.assertThat(KiwiLists.subListFrom(aToE, 1)).isEqualTo(aToE);
            softly.assertThat(KiwiLists.subListFrom(aToE, 2)).isEqualTo(List.of("b", "c", "d", "e"));
            softly.assertThat(KiwiLists.subListFrom(aToE, 3)).isEqualTo(List.of("c", "d", "e"));
            softly.assertThat(KiwiLists.subListFrom(aToE, 4)).isEqualTo(List.of("d", "e"));
            softly.assertThat(KiwiLists.subListFrom(aToE, 5)).isEqualTo(List.of("e"));
        }
    }

    @Nested
    class SubListFromIndex {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiLists.subListFromIndex(null, 1));
        }

        @Test
        void shouldThrow_GivenEmptyList() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiLists.subListFromIndex(List.of(), 0));
        }

        @Test
        void shouldThrow_GivenInvalidIndex() {
            assertThatIllegalArgumentException()
                    .describedAs("-1 is not a valid list index")
                    .isThrownBy(() -> KiwiLists.subListFromIndex(List.of(1, 2, 3), -1));

            assertThatIllegalArgumentException()
                    .describedAs("3 is higher than the highest index in the list")
                    .isThrownBy(() -> KiwiLists.subListFromIndex(List.of(1, 2, 3), 3));
        }

        @Test
        void shouldReturnSingleElementList_WhenGivenSingleElementList() {
            assertThat(KiwiLists.subListFromIndex(List.of("a"), 0)).isEqualTo(List.of("a"));
        }

        @Test
        void shouldReturnElementsIncludingLast(SoftAssertions softly) {
            var aToE = List.of("a", "b", "c", "d", "e");
            softly.assertThat(KiwiLists.subListFromIndex(aToE, 0)).isEqualTo(aToE);
            softly.assertThat(KiwiLists.subListFromIndex(aToE, 1)).isEqualTo(List.of("b", "c", "d", "e"));
            softly.assertThat(KiwiLists.subListFromIndex(aToE, 2)).isEqualTo(List.of("c", "d", "e"));
            softly.assertThat(KiwiLists.subListFromIndex(aToE, 3)).isEqualTo(List.of("d", "e"));
            softly.assertThat(KiwiLists.subListFromIndex(aToE, 4)).isEqualTo(List.of("e"));
        }
    }

    @Nested
    class FirstN {

        @Test
        void shouldThrow_WhenGivenNullList() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiLists.firstN(null, 3));
        }

        @ParameterizedTest
        @ValueSource(ints = {-100, -1, 0})
        void shouldThrow_WhenGivenInvalidNumberOfElements(int number) {
            assertThatIllegalArgumentException()
                    .describedAs("cannot get first zero elements")
                    .isThrownBy(() -> KiwiLists.firstN(List.of(1, 2, 3), number));
        }

        @ParameterizedTest
        @ValueSource(ints = {4, 5, 10, 50})
        void shouldReturn_SameList_WhenGivenNumberOfElements_IsLargerThanTheListSize(int number) {
            var oneToThree = List.of(1, 2, 3);
            assertThat(KiwiLists.firstN(oneToThree, number)).isSameAs(oneToThree);
        }

        @Test
        void shouldReturn_WantedNumberOfElements_FromTheStartOfTheList(SoftAssertions softly) {
            var aToE = List.of("a", "b", "c", "d", "e");
            softly.assertThat(KiwiLists.firstN(aToE, 1)).isEqualTo(List.of("a"));
            softly.assertThat(KiwiLists.firstN(aToE, 2)).isEqualTo(List.of("a", "b"));
            softly.assertThat(KiwiLists.firstN(aToE, 3)).isEqualTo(List.of("a", "b", "c"));
            softly.assertThat(KiwiLists.firstN(aToE, 4)).isEqualTo(List.of("a", "b", "c", "d"));
            softly.assertThat(KiwiLists.firstN(aToE, 5)).isEqualTo(List.of("a", "b", "c", "d", "e"));
        }
    }

    @Nested
    class LastN {

        @Test
        void shouldThrow_WhenGivenNullList() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiLists.lastN(null, 2));
        }

        @ParameterizedTest
        @ValueSource(ints = {-100, -1, 0})
        void shouldThrow_WhenGivenInvalidNumberOfElements(int number) {
            assertThatIllegalArgumentException()
                    .describedAs("cannot get last zero elements")
                    .isThrownBy(() -> KiwiLists.lastN(List.of(1, 2, 3), number));
        }

        @ParameterizedTest
        @ValueSource(ints = {6, 7, 20, 90})
        void shouldReturn_SameList_WhenGivenNumberOfElements_IsLargerThanTheListSize(int number) {
            var oneToFive = List.of(1, 2, 3, 4, 5);
            assertThat(KiwiLists.lastN(oneToFive, number)).isSameAs(oneToFive);
        }

        @Test
        void shouldReturn_WantedNumberOfElements_FromTheEndOfTheList(SoftAssertions softly) {
            var aToE = List.of("a", "b", "c", "d", "e");
            softly.assertThat(KiwiLists.lastN(aToE, 1)).isEqualTo(List.of("e"));
            softly.assertThat(KiwiLists.lastN(aToE, 2)).isEqualTo(List.of("d", "e"));
            softly.assertThat(KiwiLists.lastN(aToE, 3)).isEqualTo(List.of("c", "d", "e"));
            softly.assertThat(KiwiLists.lastN(aToE, 4)).isEqualTo(List.of("b", "c", "d", "e"));
            softly.assertThat(KiwiLists.lastN(aToE, 5)).isEqualTo(List.of("a", "b", "c", "d", "e"));
        }
    }

    @Nested
    class ShuffledListOf {

        @Test
        void shouldCreateEmptyList() {
            assertThat(KiwiLists.shuffledListOf()).isEmpty();
        }

        @Test
        void shouldCreateSingleItemList() {
            assertThat(KiwiLists.shuffledListOf("foo")).containsExactly("foo");
        }

        @RepeatedTest(5)
        void shouldShuffleItems() {
            var letters = alphabet();

            var shuffledLetters = KiwiLists.shuffledListOf(letters);

            assertThat(shuffledLetters)
                    .describedAs("Should contain all the letters but not be in the original order")
                    .containsExactlyInAnyOrder(letters)
                    .isNotEqualTo(List.of(letters));
        }

        @Test
        void shouldCreateUnmodifiableList() {
            var letters = KiwiLists.shuffledListOf("a", "b", "c", "d");

            //noinspection ConstantConditions
            assertThatThrownBy(() -> letters.add("e"))
                    .isExactlyInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class ShuffledArrayListOf {

        @Test
        void shouldCreateArrayList() {
            assertThat(KiwiLists.shuffledArrayListOf(1, 2, 3))
                    .isExactlyInstanceOf(ArrayList.class);
        }

        @Test
        void shouldCreateEmptyList() {
            assertThat(KiwiLists.shuffledArrayListOf()).isEmpty();
        }

        @Test
        void shouldCreateSingleItemList() {
            assertThat(KiwiLists.shuffledArrayListOf("foo")).containsExactly("foo");
        }

        @RepeatedTest(5)
        void shouldShuffleItems() {
            var letters = alphabet();

            var shuffledLetters = KiwiLists.shuffledArrayListOf(letters);

            assertThat(shuffledLetters)
                    .describedAs("Should contain all the letters but not be in the original order")
                    .containsExactlyInAnyOrder(letters)
                    .isNotEqualTo(List.of(letters));
        }

        @Test
        void shouldCreateModifiableList() {
            var letters = KiwiLists.shuffledArrayListOf("a", "b", "c", "d");

            assertThatCode(() -> letters.add("e")).doesNotThrowAnyException();
            assertThat(letters).contains("e");
        }
    }

    private String[] alphabet() {
        return Stream.iterate('a', character -> (char) (character + 1))
                .map(String::valueOf)
                .limit(26)
                .toArray(String[]::new);
    }

}
