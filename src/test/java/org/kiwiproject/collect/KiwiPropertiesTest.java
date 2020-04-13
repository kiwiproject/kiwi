package org.kiwiproject.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

class KiwiPropertiesTest {

    @Test
    void testNewProperties_UsingVarArgs_WhenNoItems() {
        assertThat(KiwiProperties.newProperties()).isEmpty();
    }

    @Test
    void testNewProperties_UsingVarArgs_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiProperties.newProperties("a", "A", "b"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testNewProperties_UsingVarArgs_WhenEvenNumberOfItems() {
        Properties properties = KiwiProperties.newProperties("a", "A", "b", "B", "c", "C", "d", "D");
        assertThat(properties).containsOnly(
                entry("a", "A"),
                entry("b", "B"),
                entry("c", "C"),
                entry("d", "D")
        );
    }

    @Test
    void testNewProperties_UsingList_WhenNoItems() {
        assertThat(KiwiProperties.newProperties(new ArrayList<>())).isEmpty();
    }

    @Test
    void testNewProperties_UsingList_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiProperties.newProperties(List.of("a", "A", "b")))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testNewProperties_UsingList_WhenEvenNumberOfItems() {
        Properties properties = KiwiProperties.newProperties(List.of("a", "A", "b", "B", "c", "C", "d", "D"));
        assertThat(properties).containsOnly(
                entry("a", "A"),
                entry("b", "B"),
                entry("c", "C"),
                entry("d", "D")
        );
    }

    @Test
    void testNewProperties_UsingMap_WhenNoItems() {
        assertThat(KiwiProperties.newProperties(new HashMap<>())).isEmpty();
    }

    @Test
    void testNewProperties_UsingMap() {
        Properties properties = KiwiProperties.newProperties(KiwiMaps.newHashMap("a", "A", "b", "B", "c", "C", "d", "D"));
        assertThat(properties).containsOnly(
                entry("a", "A"),
                entry("b", "B"),
                entry("c", "C"),
                entry("d", "D")
        );
    }

    @Test
    void testNewPropertiesFromStringPairs_WhenNoItems() {
        assertThat(KiwiProperties.newPropertiesFromStringPairs(List.of())).isEmpty();
    }

    @Test
    void testNewPropertiesFromStringPairs_WhenBadArgument() {
        List<List<String>> items = List.of(
                List.of("a", "1"),
                List.of("b", "2"),
                List.of("BAD"),
                List.of("d", "4")
        );
        assertThatThrownBy(() -> KiwiProperties.newPropertiesFromStringPairs(items))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Each sublist must contain at least 2 items (additional elements are ignored but won't cause an error)");
    }

    @Test
    void testNewPropertiesFromStringPairs() {
        List<List<String>> items = List.of(
                List.of("a", "1"),
                List.of("b", "2"),
                List.of("c", "3"),
                List.of("d", "4")
        );
        Properties properties = KiwiProperties.newPropertiesFromStringPairs(items);
        assertThat(properties).containsOnly(
                entry("a", "1"),
                entry("b", "2"),
                entry("c", "3"),
                entry("d", "4")
        );
    }

    @Test
    void testNewPropertiesFromStringPairs_WhenExtraValuesInPairs() {
        List<List<String>> items = List.of(
                List.of("a", "1", "extra"),
                List.of("b", "2"),
                List.of("c", "3", "anotherExtra", "another Value"),
                List.of("d", "4")
        );
        Properties properties = KiwiProperties.newPropertiesFromStringPairs(items);
        assertThat(properties).containsOnly(
                entry("a", "1"),
                entry("b", "2"),
                entry("c", "3"),
                entry("d", "4")
        );
    }

    @Test
    void testNewProperties_WithEmptyObjectArray() {
        var objArr = new Object[0];
        assertThat(KiwiProperties.newProperties(objArr)).isEmpty();
    }

    @Test
    void testNewProperties_UsingObjectArray_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiProperties.newProperties("a", 42, "b"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testNewProperties_WithObjectArray_WhenEvenNumberOfItems() {
        var props = KiwiProperties.newProperties("k1", 42, "k2", "a string", "k3", true);
        assertThat(props).hasSize(3);
        assertThat(props.getProperty("k1")).isEqualTo("42");
        assertThat(props.getProperty("k2")).isEqualTo("a string");
        assertThat(props.getProperty("k3")).isEqualTo("true");
    }

}