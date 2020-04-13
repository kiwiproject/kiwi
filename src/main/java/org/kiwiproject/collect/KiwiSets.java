package org.kiwiproject.collect;

import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Utility methods for working with {@link Set} instances.
 */
@UtilityClass
public class KiwiSets {

    /**
     * Checks whether the specified set is null or empty.
     *
     * @param set the set
     * @param <T> the type of items in the set
     * @return {@code true} if set is null or empty; {@code false} otherwise
     */
    public static <T> boolean isNullOrEmpty(Set<T> set) {
        return set == null || set.isEmpty();
    }

    /**
     * Checks whether the specified is neither null nor empty.
     *
     * @param set the set
     * @param <T> the type of items in the set
     * @return {@code true} if set is neither null nor empty; {@code false} otherwise
     */
    public static <T> boolean isNotNullOrEmpty(Set<T> set) {
        return !isNullOrEmpty(set);
    }

    /**
     * Checks whether the specified list is non-null and has only one item.
     *
     * @param set the set
     * @param <T> the type of items in the set
     * @return {@code true} if list is non-null and has exactly one item; {@code false}
     */
    public static <T> boolean hasOneElement(Set<T> set) {
        return nonNull(set) && set.size() == 1;
    }

}
