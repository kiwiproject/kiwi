package org.kiwiproject.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

@DisplayName("KiwiCollections")
class KiwiCollectionsTest {

    @Nested
    class IsNullOrEmpty {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldBeTrueWhenNullOrEmptyList(List<String> values) {
            assertThat(KiwiCollections.isNullOrEmpty(values)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldBeTrueWhenNullOrEmptySet(Set<String> values) {
            assertThat(KiwiCollections.isNullOrEmpty(values)).isTrue();
        }

        @Test
        void shouldBeFalseWhenNotEmptyList() {
            assertThat(KiwiCollections.isNullOrEmpty(List.of(1))).isFalse();
        }

        @Test
        void shouldBeFalseWhenNotEmptySet() {
            assertThat(KiwiCollections.isNullOrEmpty(Set.of(1))).isFalse();
        }
    }

    @Nested
    class IsNotNullOrEmpty {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldBeFalseWhenNullOrEmptyList(List<String> values) {
            assertThat(KiwiCollections.isNotNullOrEmpty(values)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldBeFalseWhenNullOrEmptySet(Set<String> values) {
            assertThat(KiwiCollections.isNotNullOrEmpty(values)).isFalse();
        }

        @Test
        void shouldBeTrueWhenNotEmptyList() {
            assertThat(KiwiCollections.isNotNullOrEmpty(List.of(1))).isTrue();
        }

        @Test
        void shouldBeTrueWhenNotEmptySet() {
            assertThat(KiwiCollections.isNotNullOrEmpty(Set.of(1))).isTrue();
        }
    }

    @Nested
    class HasOneElement {

        @Test
        void shouldBeTrue_WhenListHasExactlyOneElement() {
            assertThat(KiwiCollections.hasOneElement(List.of(42))).isTrue();
        }

        @Test
        void shouldBeTrue_WhenSetHasExactlyOneElement() {
            assertThat(KiwiCollections.hasOneElement(Set.of(42))).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldBeFalse_WhenNullOrEmptyList(List<Instant> values) {
            assertThat(KiwiCollections.hasOneElement(values)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldBeFalse_WhenNullOrEmptySet(Set<LocalDateTime> values) {
            assertThat(KiwiCollections.hasOneElement(values)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenListHasMoreThanOneElement() {
            assertThat(KiwiCollections.hasOneElement(List.of(4, 2))).isFalse();
        }

        @Test
        void shouldBeFalse_WhenSetHasMoreThanOneElement() {
            assertThat(KiwiCollections.hasOneElement(Set.of(2, 4))).isFalse();
        }
    }

    @Nested
    class FirstIfPresent {

        @Test
        void shouldThrowIllegalArgumentException_WhenCollectionIsNotSequenced() {
            var set = Set.of(1, 2, 3);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections.firstIfPresent(set))
                    .withMessage("collection of type %s is not supported as a 'sequenced' collection", set.getClass().getName());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_WhenCollectionIsNullOrEmpty(List<String> list) {
            assertThat(KiwiCollections.firstIfPresent(list)).isEmpty();
        }

        @Test
        void shouldReturnOptionalContainingFirstElement_WhenCollectionIsNotEmpty() {
            assertThat(KiwiCollections.firstIfPresent(List.of(1, 2, 3, 4, 5))).contains(1);
        }
    }

    @Nested
    class First {

        @Test
        void shouldThrowIllegalArgumentException_WhenCollectionIsNotSequenced() {
            var set = Set.of(4, 5);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections.first(set))
                    .withMessage("collection of type %s is not supported as a 'sequenced' collection", set.getClass().getName());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowIllegalArgumentException_WhenCollectionIsNullOrEmpty(List<Long> list) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections.first(list))
                    .withMessage("collection must contain at least one element");
        }

        @Test
        void shouldReturnFirstElementOfSortedSet() {
            var sortedSet = new TreeSet<>(List.of(5, 3, 4, 1, 2));
            assertThat(KiwiCollections.first(sortedSet)).isEqualTo(1);
        }

        @Test
        void shouldReturnFirstElementOfLinkedHashSet() {
            var values = List.of(4, 3, 1, 2, 5);
            var linkedHashSet = new LinkedHashSet<>(values);
            assertThat(KiwiCollections.first(linkedHashSet)).isEqualTo(4);
        }

        @Test
        void shouldReturnFirstElementOfList() {
            assertThat(KiwiCollections.first(List.of(3, 5, 4, 1, 2))).isEqualTo(3);
        }

        @Test
        void shouldReturnFirstElementOfDeque() {
            var values = List.of(42, 56, 31, 78, 99);
            var deque = new ArrayDeque<>(values);
            assertThat(KiwiCollections.first(deque)).isEqualTo(42);
            assertThat(deque)
                    .describedAs("no element should have been removed")
                    .hasSameSizeAs(values);
        }
    }

    @Nested
    class LastIfPresent {

        @Test
        void shouldThrowIllegalArgumentException_WhenCollectionIsNotSequenced() {
            var set = Set.of(42, 84, 24);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections.lastIfPresent(set))
                    .withMessage("collection of type %s is not supported as a 'sequenced' collection", set.getClass().getName());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_WhenCollectionIsNullOrEmpty(List<String> list) {
            assertThat(KiwiCollections.lastIfPresent(list)).isEmpty();
        }

        @Test
        void shouldReturnOptionalContainingLastElement_WhenCollectionIsNotEmpty() {
            assertThat(KiwiCollections.lastIfPresent(List.of(1, 2, 3, 4, 5))).contains(5);
        }
    }

    @Nested
    class Last {

        @Test
        void shouldThrowIllegalArgumentException_WhenCollectionIsNotSequenced() {
            var set = Set.of(12, 24, 36, 48);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections.last(set))
                    .withMessage("collection of type %s is not supported as a 'sequenced' collection", set.getClass().getName());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowIllegalArgumentException_WhenCollectionIsNullOrEmpty(List<Long> list) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections.last(list))
                    .withMessage("collection must contain at least one element");
        }

        @Test
        void shouldReturnLastElementOfSortedSet() {
            var sortedSet = new TreeSet<>(List.of(5, 3, 4, 1, 2));
            assertThat(KiwiCollections.last(sortedSet)).isEqualTo(5);
        }

        @Test
        void shouldReturnLastElementOfLinkedHashSet() {
            var values = List.of(4, 3, 1, 2, 5);
            var linkedHashSet = new LinkedHashSet<>(values);
            assertThat(KiwiCollections.last(linkedHashSet)).isEqualTo(5);
        }

        @Test
        void shouldReturnLastElementOfList() {
            assertThat(KiwiCollections.last(List.of(3, 5, 4, 1, 2))).isEqualTo(2);
        }

        @Test
        void shouldReturnLastElementOfDeque() {
            var values = List.of(42, 56, 31, 78, 99);
            var deque = new ArrayDeque<>(values);
            assertThat(KiwiCollections.last(deque)).isEqualTo(99);
            assertThat(deque)
                    .describedAs("no element should have been removed")
                    .hasSameSizeAs(values);
        }
    }

    @Nested
    class CheckNotEmptyCollection {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowIllegalArgumentException_WhenGivenNullOrEmptyCollection(Set<Integer> collection) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections.checkNotEmptyCollection(collection))
                    .withMessage("collection must contain at least one element");
        }

        @Test
        void shouldNotThrow_WhenGivenNotEmptyCollection() {
            assertThatCode(() -> KiwiCollections.checkNotEmptyCollection(Set.of(42)))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class CheckNonNullCollection {

        @SuppressWarnings("ConstantConditions")
        @Test
        void shouldThrowIllegalArgumentException_WhenGivenNullCollection() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiCollections.checkNonNullCollection(null))
                    .withMessage("collection must not be null");
        }

        @Test
        void shouldNotThrow_WhenGivenNotNullCollection() {
            assertThatCode(() -> KiwiCollections.checkNonNullCollection(Set.of()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class IsSequenced {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.collect.KiwiCollectionsTest#supportedSequencedCollections")
        void shouldBeTrue_ForSupportedCollectionTypes(Collection<Integer> collection) {
            assertThat(KiwiCollections.isSequenced(collection)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.collect.KiwiCollectionsTest#someNonSequencedCollections")
        void shouldBeFalse_ForUnsupportedCollectionTypes(Collection<Long> collection) {
            assertThat(KiwiCollections.isSequenced(collection)).isFalse();
        }
    }

    static Stream<Collection<Integer>> supportedSequencedCollections() {
        return Stream.of(
                new TreeSet<>(),
                new LinkedHashSet<>(),
                new ArrayList<>(),
                new ArrayDeque<>()
        );
    }

    static Stream<Collection<Long>> someNonSequencedCollections() {
        return Stream.of(
                new HashSet<>(),
                new CopyOnWriteArraySet<>(),
                ImmutableSet.of(),
                new ArrayBlockingQueue<>(5),
                HashMultiset.create()
        );
    }
}
