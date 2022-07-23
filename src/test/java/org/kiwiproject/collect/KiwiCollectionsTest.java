package org.kiwiproject.collect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
}
