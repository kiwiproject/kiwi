package org.kiwiproject.collect;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

public class KiwiPropertiesTest {

    @Test
    public void testNewProperties_UsingVarArgs_WhenNoItems() {
        assertThat(KiwiProperties.newProperties()).isEmpty();
    }

    @Test
    public void testNewProperties_UsingVarArgs_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiProperties.newProperties("a", "A", "b"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNewProperties_UsingVarArgs_WhenEvenNumberOfItems() {
        Properties properties = KiwiProperties.newProperties("a", "A", "b", "B", "c", "C", "d", "D");
        assertThat(properties).containsOnly(
                entry("a", "A"),
                entry("b", "B"),
                entry("c", "C"),
                entry("d", "D")
        );
    }

    @Test
    public void testNewProperties_UsingList_WhenNoItems() {
        assertThat(KiwiProperties.newProperties(new ArrayList<>())).isEmpty();
    }

    @Test
    public void testNewProperties_UsingList_WhenOddNumberOfItems() {
        assertThatThrownBy(() -> KiwiProperties.newProperties(newArrayList("a", "A", "b")))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNewProperties_UsingList_WhenEvenNumberOfItems() {
        Properties properties = KiwiProperties.newProperties(newArrayList("a", "A", "b", "B", "c", "C", "d", "D"));
        assertThat(properties).containsOnly(
                entry("a", "A"),
                entry("b", "B"),
                entry("c", "C"),
                entry("d", "D")
        );
    }

    @Test
    public void testNewProperties_UsingMap_WhenNoItems() {
        assertThat(KiwiProperties.newProperties(new HashMap<>())).isEmpty();
    }

    @Test
    public void testNewProperties_UsingMap() {
        Properties properties = KiwiProperties.newProperties(KiwiMaps.newHashMap("a", "A", "b", "B", "c", "C", "d", "D"));
        assertThat(properties).containsOnly(
                entry("a", "A"),
                entry("b", "B"),
                entry("c", "C"),
                entry("d", "D")
        );
    }

    @Test
    public void testNewPropertiesFromStringPairs_WhenNoItems() {
        assertThat(KiwiProperties.newPropertiesFromStringPairs(newArrayList())).isEmpty();
    }

    @Test
    public void testNewPropertiesFromStringPairs_WhenBadArgument() {
        List<List<String>> items = newArrayList(
                newArrayList("a", "1"),
                newArrayList("b", "2"),
                newArrayList("BAD"),
                newArrayList("d", "4")
        );
        assertThatThrownBy(() -> KiwiProperties.newPropertiesFromStringPairs(items))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Each sublist must contain exactly 2 items");
    }

    @Test
    public void testNewPropertiesFromStringPairs() {
        List<List<String>> items = newArrayList(
                newArrayList("a", "1"),
                newArrayList("b", "2"),
                newArrayList("c", "3"),
                newArrayList("d", "4")
        );
        Properties properties = KiwiProperties.newPropertiesFromStringPairs(items);
        assertThat(properties).containsOnly(
                entry("a", "1"),
                entry("b", "2"),
                entry("c", "3"),
                entry("d", "4")
        );
    }

}