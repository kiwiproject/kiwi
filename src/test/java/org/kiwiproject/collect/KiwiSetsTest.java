package org.kiwiproject.collect;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

@DisplayName("KiwiSets")
class KiwiSetsTest {

    @SuppressWarnings("ConstantValue")
    @Test
    void testIsNullOrEmpty_WhenNull() {
        assertThat(KiwiSets.isNullOrEmpty(null)).isTrue();
    }

    @Test
    void testIsNullOrEmpty_WhenEmpty() {
        assertThat(KiwiSets.isNullOrEmpty(new HashSet<>())).isTrue();
    }

    @Test
    void testIsNullOrEmpty_WhenContainsSomeElements() {
        Set<String> fruits = newSetOfFruits();
        assertThat(KiwiSets.isNullOrEmpty(fruits)).isFalse();
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void testIsNotNullOrEmpty_WhenNull() {
        assertThat(KiwiSets.isNotNullOrEmpty(null)).isFalse();
    }

    @Test
    void testIsNotNullOrEmpty_WhenEmpty() {
        assertThat(KiwiSets.isNotNullOrEmpty(new HashSet<>())).isFalse();
    }

    @Test
    void testIsNotNullOrEmpty_WhenContainsElements() {
        Set<String> fruits = newSetOfFruits();
        assertThat(KiwiSets.isNotNullOrEmpty(fruits)).isTrue();
    }

    @Test
    void testHasOneElement_WhenNull() {
        assertThat(KiwiSets.hasOneElement(null)).isFalse();
    }

    @Test
    void testHasOneElement_WhenEmpty() {
        assertThat(KiwiSets.hasOneElement(new HashSet<>())).isFalse();
    }

    @Test
    void testHasOneElement_WhenHasOnlyOneElement() {
        assertThat(KiwiSets.hasOneElement(newHashSet("kiwi"))).isTrue();
    }

    @Test
    void testHasOneElement_WhenHasMoreThanOneElement() {
        assertThat(KiwiSets.hasOneElement(newSetOfFruits())).isFalse();
    }

    private static Set<String> newSetOfFruits() {
        return newHashSet("orange", "apple", "kiwi", "banana", "guava");
    }
}
