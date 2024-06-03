package org.kiwiproject.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.junit.jupiter.ClearBoxTest;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.IntStream;

@DisplayName("KiwiArrays")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiArraysTest {

    private String[] items;

    @BeforeEach
    void setUp() {
        items = new String[]{"guava", "kiwi", "guarana", "limeberry", "chupa-chupa"};
    }

    @Nested
    class IsNullOrEmpty {

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldBeTrue_WhenNullArg() {
            assertThat(KiwiArrays.isNullOrEmpty(null)).isTrue();
        }

        @Test
        void shouldBeTrue_WhenEmptyArray() {
            assertThat(KiwiArrays.isNullOrEmpty(new String[0])).isTrue();
        }

        @Test
        void shouldBeFalse_WhenContainsElements() {
            assertThat(KiwiArrays.isNullOrEmpty(items)).isFalse();
        }
    }

    @Nested
    class IsNotNullOrEmpty {

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldBeFalse_WhenNull() {
            assertThat(KiwiArrays.isNotNullOrEmpty(null)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenEmpty() {
            assertThat(KiwiArrays.isNotNullOrEmpty(new String[0])).isFalse();
        }

        @Test
        void shouldBeTrue_WhenContainsElements() {
            assertThat(KiwiArrays.isNotNullOrEmpty(items)).isTrue();
        }
    }

    @Nested
    class HasOneElement {

        @Test
        void shouldBeFalse_WhenNull() {
            assertThat(KiwiArrays.hasOneElement(null)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenEmpty() {
            assertThat(KiwiArrays.hasOneElement(new String[0])).isFalse();
        }

        @Test
        void shouldBeTrue_WhenHasOnlyOneElement() {
            assertThat(KiwiArrays.hasOneElement(new String[]{"kiwi"})).isTrue();
        }

        @Test
        void shouldBeFalse_WhenHasMoreThanOneElement() {
            assertThat(KiwiArrays.hasOneElement(items)).isFalse();
        }
    }

    @Nested
    class Sorted {

        @Test
        void shouldBeEmpty_WhenEmptyArray() {
            assertThat(KiwiArrays.sorted(new String[0], String.class)).isEmpty();
        }

        @Test
        void shouldThrow_WhenNullArray() {
            assertThatThrownBy(() -> KiwiArrays.sorted(null, String.class))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("items cannot be null");
        }

        @Test
        void shouldReturnSortedArray_WhenHasItems() {
            var someIntegers = IntStream.iterate(1, n -> n + 1).limit(100).boxed().toArray(Integer[]::new);

            var randomIntegers = newArrayList(someIntegers);
            Collections.shuffle(randomIntegers);

            assertThat(KiwiArrays.sorted(randomIntegers.toArray(new Integer[0]), Integer.class))
                    .hasSize(100)
                    .containsExactly(someIntegers);
        }
    }

    @Nested
    class SortedWithComparator {

        @Test
        void shouldBeEmpty_WhenEmptyArray() {
            Integer[] emptyItems = new Integer[0];
            Comparator<Integer> comparator = Comparator.reverseOrder();

            assertThat(KiwiArrays.sorted(emptyItems, comparator, Integer.class)).isEmpty();
        }

        @Test
        void shouldThrow_WhenNullArray() {
            assertThatThrownBy(() -> KiwiArrays.sorted(null, Comparator.reverseOrder(), Integer.class))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("items cannot be null");
        }

        @Test
        void shouldThrow_WhenNullComparator() {
            assertThatThrownBy(() -> KiwiArrays.sorted(new String[0], null, String.class))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("Comparator cannot be null");
        }

        @Test
        void shouldReturnSortedArray_WhenHasItems() {
            var someIntegers = IntStream.iterate(1, n -> n + 1).limit(100).boxed().toArray(Integer[]::new);
            var reverseIntegers = newArrayList(someIntegers);
            Collections.reverse(reverseIntegers);

            var randomIntegers = newArrayList(someIntegers);
            Collections.shuffle(randomIntegers);

            var comparator = Comparator.<Integer>reverseOrder();
            assertThat(KiwiArrays.sorted(randomIntegers.toArray(new Integer[0]), comparator, Integer.class))
                    .hasSize(100)
                    .containsExactlyElementsOf(reverseIntegers);
        }
    }

    @Nested
    class FirstIfPresent {

        @Test
        void shouldBeEmpty_WhenNullArray() {
            assertThat(KiwiArrays.firstIfPresent(null)).isEmpty();
        }

        @Test
        void shouldBeEmpty_WhenEmptyArray() {
            assertThat(KiwiArrays.firstIfPresent(new String[0])).isEmpty();
        }

        @Test
        void shouldContainFirst_WhenSingleElementArray() {
            assertThat(KiwiArrays.firstIfPresent(new String[]{"kiwi"}))
                    .isPresent()
                    .contains("kiwi");
        }

        @Test
        void shouldContainFirst_WhenTwoElementArray() {
            assertThat(KiwiArrays.firstIfPresent(new String[]{"kiwi", "guava"}))
                    .isPresent()
                    .contains("kiwi");
        }
    }

    @Nested
    class First {

        @Test
        void shouldReturnFirst() {
            assertThat(KiwiArrays.first(items)).isEqualTo(items[0]);
        }
    }

    @Nested
    class Second {

        @Test
        void shouldReturnSecond() {
            assertThat(KiwiArrays.second(items)).isEqualTo(items[1]);
        }
    }

    @Nested
    class Third {

        @Test
        void shouldReturnThird() {
            assertThat(KiwiArrays.third(items)).isEqualTo(items[2]);
        }
    }

    @Nested
    class Fourth {

        @Test
        void shouldReturnFourth() {
            assertThat(KiwiArrays.fourth(items)).isEqualTo(items[3]);
        }
    }

    @Nested
    class Fifth {

        @Test
        void shouldReturnFifth() {
            assertThat(KiwiArrays.fifth(items)).isEqualTo(items[4]);
        }
    }

    @Nested
    class SecondToLast {

        @Test
        void shouldReturnSecondToLast(SoftAssertions softly) {
            var expected = items[items.length - 2];
            softly.assertThat(KiwiArrays.secondToLast(items)).isEqualTo(expected);
            softly.assertThat(KiwiArrays.penultimate(items)).isEqualTo(expected);
        }
    }

    @Nested
    class Last {

        @Test
        void shouldReturnLast() {
            var expected = items[items.length - 1];
            assertThat(KiwiArrays.last(items)).isEqualTo(expected);
        }
    }

    @Nested
    class LastIfPresent {

        @Test
        void shouldBeEmpty_WhenEmptyArray() {
            assertThat(KiwiArrays.lastIfPresent(new String[0])).isEmpty();
        }

        @Test
        void shouldContainLast_WhenSingleElementArray() {
            assertThat(KiwiArrays.lastIfPresent(new String[]{"kiwi"}))
                    .isPresent()
                    .contains("kiwi");
        }

        @Test
        void shouldContainLast_WhenTwoElementArray() {
            assertThat(KiwiArrays.lastIfPresent(new String[]{"kiwi", "guava"}))
                    .isPresent()
                    .contains("guava");
        }
    }

    @Nested
    class Nth {

        @Test
        void shouldThrow_WhenNegativeNumber() {
            assertThatThrownBy(() -> KiwiArrays.nth(items, -1))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("number must be positive");
        }

        @Test
        void shouldThrow_WhenNullArray() {
            assertThatThrownBy(() -> KiwiArrays.nth(null, 42))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessage("items cannot be null");
        }

        @Test
        void shouldThrow_WhenEmptyArray() {
            assertThatThrownBy(() -> KiwiArrays.nth(new String[0], 8))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("expected at least 8 items (actual size: 0)");
        }

        @Test
        void shouldThrow_WhenNotEnoughElementsForRequestedPosition() {
            int number = items.length + 1;
            assertThatThrownBy(() -> KiwiArrays.nth(items, number))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("expected at least %d items (actual size: %d)", number, items.length);
        }

        @Test
        void shouldReturnNth_WhenJustEnoughElementsForRequestedPosition() {
            int number = items.length;
            assertThat(KiwiArrays.nth(items, number)).isEqualTo(items[number - 1]);
        }
    }

    @Nested
    class CheckMinimumSize {

        @ClearBoxTest
        void shouldThrow_WhenNullArgument() {
            assertThatThrownBy(() -> KiwiArrays.checkMinimumSize(null, 10))
                    .isExactlyInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Distinct {

        @Test
        void shouldReturnDistinct(SoftAssertions softly) {
            softly.assertThat(KiwiArrays.distinct(new Integer[]{1, 2, 3}, Integer.class)).hasSize(3);
            softly.assertThat(KiwiArrays.distinct(new Integer[]{1, 1, 1}, Integer.class)).hasSize(1);
            softly.assertThat(KiwiArrays.distinct(new String[]{"a", "b", "b"}, String.class)).hasSize(2);
        }

        @Test
        void shouldThrow_WhenNullArg() {
            assertThatThrownBy(() -> KiwiArrays.distinct(null, String.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("collection can not be null");
        }
    }

    @Nested
    class DistinctOrNull {

        @Test
        void shouldAllowNull() {
            assertThat(KiwiArrays.distinctOrNull(null, String.class)).isNull();
        }
    }

    @Nested
    class CircularArrayOffset {

        @Test
        void shouldReturnNewArrayAtCorrectStartingOffset() {
            assertThat(KiwiArrays.newArrayStartingAtCircularOffset(new String[]{"zero", "one", "two", "three"}, 0, String.class))
                    .containsExactly("zero", "one", "two", "three");

            assertThat(KiwiArrays.newArrayStartingAtCircularOffset(new String[]{"zero", "one", "two", "three"}, 1, String.class))
                    .containsExactly("one", "two", "three", "zero");

            assertThat(KiwiArrays.newArrayStartingAtCircularOffset(new String[]{"zero", "one", "two", "three"}, 3, String.class))
                    .containsExactly("three", "zero", "one", "two");
        }

        @Test
        void shouldWrapWhenOffsetIsBeyondEndOfArray() {
            assertThat(KiwiArrays.newArrayStartingAtCircularOffset(new String[]{"zero", "one", "two", "three"}, 4, String.class))
                    .containsExactly("zero", "one", "two", "three");
        }
    }

    @Nested
    class SubArrayExcludingFirst {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiArrays.subArrayExcludingFirst(null));
        }

        @Test
        void shouldReturnEmptyArray_WhenGivenEmptyArray() {
            assertThat(KiwiArrays.subArrayExcludingFirst(new String[0])).isEmpty();
        }

        @Test
        void shouldReturnEmptyArray_GivenOneElementArray() {
            assertThat(KiwiArrays.subArrayExcludingFirst(new Integer[]{42})).isEmpty();
        }

        @Test
        void shouldReturnElementsExceptFirst(SoftAssertions softly) {
            softly.assertThat(KiwiArrays.subArrayExcludingFirst(new Integer[]{1, 2})).isEqualTo(new Integer[]{2});
            softly.assertThat(KiwiArrays.subArrayExcludingFirst(new Integer[]{1, 2, 3})).isEqualTo(new Integer[]{2, 3});
            softly.assertThat(KiwiArrays.subArrayExcludingFirst(new Integer[]{1, 2, 3, 4, 5})).isEqualTo(new Integer[]{2, 3, 4, 5});
        }
    }

    @Nested
    class SubArrayExcludingLast {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiArrays.subArrayExcludingLast((String[]) null));
        }

        @Test
        void shouldReturnEmptyArray_WhenGivenEmptyArray() {
            assertThat(KiwiArrays.subArrayExcludingLast(new String[0])).isEmpty();
        }

        @Test
        void shouldReturnEmptyArray_GivenOneElementArray() {
            assertThat(KiwiArrays.subArrayExcludingLast(new Integer[]{84})).isEmpty();
        }

        @Test
        void shouldReturnElementsExceptLast(SoftAssertions softly) {
            softly.assertThat(KiwiArrays.subArrayExcludingLast(new Integer[]{1, 2})).isEqualTo(new Integer[]{1});
            softly.assertThat(KiwiArrays.subArrayExcludingLast(new Integer[]{1, 2, 3})).isEqualTo(new Integer[]{1, 2});
            softly.assertThat(KiwiArrays.subArrayExcludingLast(new Integer[]{1, 2, 3, 4, 5})).isEqualTo(new Integer[]{1, 2, 3, 4});
        }
    }

    @Nested
    class SubArrayFrom {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiArrays.subArrayFrom(null, 2));
        }

        @Test
        void shouldThrow_GivenEmptyArray() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiArrays.subArrayFrom(new Integer[0], 1));
        }

        @Test
        void shouldThrow_GivenInvalidNumber() {
            assertThatIllegalArgumentException()
                    .describedAs("zero is not a valid starting element number")
                    .isThrownBy(() -> KiwiArrays.subArrayFrom(new Integer[]{1, 2, 3}, 0));

            assertThatIllegalArgumentException()
                    .describedAs("4 is higher than the array size")
                    .isThrownBy(() -> KiwiArrays.subArrayFrom(new Integer[]{1, 2, 3}, 4));
        }

        @Test
        void shouldReturnSingleElementArray_WhenGivenSingleElementArray() {
            assertThat(KiwiArrays.subArrayFrom(new String[]{"a"}, 1)).isEqualTo(new String[]{"a"});
        }

        @Test
        void shouldReturnElementsIncludingLast(SoftAssertions softly) {
            var aToE = new String[]{"a", "b", "c", "d", "e"};
            softly.assertThat(KiwiArrays.subArrayFrom(aToE, 1)).isEqualTo(aToE);
            softly.assertThat(KiwiArrays.subArrayFrom(aToE, 2)).isEqualTo(new String[]{"b", "c", "d", "e"});
            softly.assertThat(KiwiArrays.subArrayFrom(aToE, 3)).isEqualTo(new String[]{"c", "d", "e"});
            softly.assertThat(KiwiArrays.subArrayFrom(aToE, 4)).isEqualTo(new String[]{"d", "e"});
            softly.assertThat(KiwiArrays.subArrayFrom(aToE, 5)).isEqualTo(new String[]{"e"});
        }
    }

    @Nested
    class SubArrayFromIndex {

        @Test
        void shouldThrow_WhenGivenNullArg() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiArrays.subArrayFromIndex(null, 1));
        }

        @Test
        void shouldThrow_GivenEmptyArray() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiArrays.subArrayFromIndex(new Integer[0], 0));
        }

        @Test
        void shouldThrow_GivenInvalidIndex() {
            assertThatIllegalArgumentException()
                    .describedAs("-1 is not a valid array index")
                    .isThrownBy(() -> KiwiArrays.subArrayFromIndex(new Integer[]{1, 2, 3}, -1));

            assertThatIllegalArgumentException()
                    .describedAs("3 is higher than the highest index in the array")
                    .isThrownBy(() -> KiwiArrays.subArrayFromIndex(new Integer[]{1, 2, 3}, 3));
        }

        @Test
        void shouldReturnSingleElementArray_WhenGivenSingleElementArray() {
            assertThat(KiwiArrays.subArrayFromIndex(new String[]{"a"}, 0)).isEqualTo(new String[]{"a"});
        }

        @Test
        void shouldReturnElementsIncludingLast(SoftAssertions softly) {
            var aToE = new String[]{"a", "b", "c", "d", "e"};
            softly.assertThat(KiwiArrays.subArrayFromIndex(aToE, 0)).isEqualTo(aToE);
            softly.assertThat(KiwiArrays.subArrayFromIndex(aToE, 1)).isEqualTo(new String[]{"b", "c", "d", "e"});
            softly.assertThat(KiwiArrays.subArrayFromIndex(aToE, 2)).isEqualTo(new String[]{"c", "d", "e"});
            softly.assertThat(KiwiArrays.subArrayFromIndex(aToE, 3)).isEqualTo(new String[]{"d", "e"});
            softly.assertThat(KiwiArrays.subArrayFromIndex(aToE, 4)).isEqualTo(new String[]{"e"});
        }
    }

    @Nested
    class FirstN {

        @Test
        void shouldThrow_WhenGivenNullArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiArrays.firstN(null, 3));
        }

        @ParameterizedTest
        @ValueSource(ints = {-100, -1, 0})
        void shouldThrow_WhenGivenInvalidNumberOfElements(int number) {
            assertThatIllegalArgumentException()
                    .describedAs("cannot get first zero elements")
                    .isThrownBy(() -> KiwiArrays.firstN(new Integer[]{1, 2, 3}, number));
        }

        @ParameterizedTest
        @ValueSource(ints = {4, 5, 10, 50})
        void shouldReturn_SameArray_WhenGivenNumberOfElements_IsLargerThanTheArraySize(int number) {
            var oneToThree = new Integer[]{1, 2, 3};
            assertThat(KiwiArrays.firstN(oneToThree, number)).isSameAs(oneToThree);
        }

        @Test
        void shouldReturn_WantedNumberOfElements_FromTheStartOfTheArray(SoftAssertions softly) {
            var aToE = new String[]{"a", "b", "c", "d", "e"};
            softly.assertThat(KiwiArrays.firstN(aToE, 1)).isEqualTo(new String[]{"a"});
            softly.assertThat(KiwiArrays.firstN(aToE, 2)).isEqualTo(new String[]{"a", "b"});
            softly.assertThat(KiwiArrays.firstN(aToE, 3)).isEqualTo(new String[]{"a", "b", "c"});
            softly.assertThat(KiwiArrays.firstN(aToE, 4)).isEqualTo(new String[]{"a", "b", "c", "d"});
            softly.assertThat(KiwiArrays.firstN(aToE, 5)).isEqualTo(new String[]{"a", "b", "c", "d", "e"});
        }
    }

    @Nested
    class LastN {

        @Test
        void shouldThrow_WhenGivenNullArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiArrays.lastN(null, 2));
        }

        @ParameterizedTest
        @ValueSource(ints = {-100, -1, 0})
        void shouldThrow_WhenGivenInvalidNumberOfElements(int number) {
            assertThatIllegalArgumentException()
                    .describedAs("cannot get last zero elements")
                    .isThrownBy(() -> KiwiArrays.lastN(new Integer[]{1, 2, 3}, number));
        }

        @ParameterizedTest
        @ValueSource(ints = {6, 7, 20, 90})
        void shouldReturn_SameArray_WhenGivenNumberOfElements_IsLargerThanTheArraySize(int number) {
            var oneToFive = new Integer[]{1, 2, 3, 4, 5};
            assertThat(KiwiArrays.lastN(oneToFive, number)).isSameAs(oneToFive);
        }

        @Test
        void shouldReturn_WantedNumberOfElements_FromTheEndOfTheArray(SoftAssertions softly) {
            var aToE = new String[]{"a", "b", "c", "d", "e"};
            softly.assertThat(KiwiArrays.lastN(aToE, 1)).isEqualTo(new String[]{"e"});
            softly.assertThat(KiwiArrays.lastN(aToE, 2)).isEqualTo(new String[]{"d", "e"});
            softly.assertThat(KiwiArrays.lastN(aToE, 3)).isEqualTo(new String[]{"c", "d", "e"});
            softly.assertThat(KiwiArrays.lastN(aToE, 4)).isEqualTo(new String[]{"b", "c", "d", "e"});
            softly.assertThat(KiwiArrays.lastN(aToE, 5)).isEqualTo(new String[]{"a", "b", "c", "d", "e"});
        }
    }
}
