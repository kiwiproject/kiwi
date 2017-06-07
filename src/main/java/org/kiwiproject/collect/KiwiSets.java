package org.kiwiproject.collect;

import lombok.experimental.UtilityClass;

import java.util.Set;

import static java.util.Objects.nonNull;

@UtilityClass
public class KiwiSets {

    public static <T> boolean isNullOrEmpty(Set<T> items) {
        return items == null || items.isEmpty();
    }

    public static <T> boolean isNotNullOrEmpty(Set<T> items) {
        return !isNullOrEmpty(items);
    }

    public static <T> boolean hasOneElement(Set<T> items) {
        return nonNull(items) && items.size() == 1;
    }

}
