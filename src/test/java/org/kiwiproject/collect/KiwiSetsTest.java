package org.kiwiproject.collect;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class KiwiSetsTest {

    @Test
    public void testIsNullOrEmpty_WhenNull() {
        assertThat(KiwiSets.isNullOrEmpty(null)).isTrue();
    }

    @Test
    public void testIsNullOrEmpty_WhenEmpty() {
        assertThat(KiwiSets.isNullOrEmpty(new HashSet<>())).isTrue();
    }

    @Test
    public void testIsNullOrEmpty_WhenContainsSomeElements() {
        Set<String> fruits = newSetOfFruits();
        assertThat(KiwiSets.isNullOrEmpty(fruits)).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenNull() {
        assertThat(KiwiSets.isNotNullOrEmpty(null)).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenEmpty() {
        assertThat(KiwiSets.isNotNullOrEmpty(new HashSet<>())).isFalse();
    }

    @Test
    public void testIsNotNullOrEmpty_WhenContainsElements() {
        Set<String> fruits = newSetOfFruits();
        assertThat(KiwiSets.isNotNullOrEmpty(fruits)).isTrue();
    }

    @Test
    public void testHasOneElement_WhenNull() {
        assertThat(KiwiSets.hasOneElement(null)).isFalse();
    }

    @Test
    public void testHasOneElement_WhenEmpty() {
        assertThat(KiwiSets.hasOneElement(new HashSet<>())).isFalse();
    }

    @Test
    public void testHasOneElement_WhenHasOnlyOneElement() {
        assertThat(KiwiSets.hasOneElement(newHashSet("kiwi"))).isTrue();
    }

    @Test
    public void testHasOneElement_WhenHasMoreThanOneElement() {
        assertThat(KiwiSets.hasOneElement(newSetOfFruits())).isFalse();
    }

    private static Set<String> newSetOfFruits() {
        return newHashSet("orange", "apple", "kiwi", "banana", "guava");
    }
}