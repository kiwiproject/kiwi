package org.kiwiproject.collect;

import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class KiwiLists {

    public static <T> boolean isNullOrEmpty(List<T> items) {
        return items == null || items.isEmpty();
    }

    public static <T> boolean isNotNullOrEmpty(List<T> items) {
        return !isNullOrEmpty(items);
    }

    public static <T> boolean hasOneElement(List<T> items) {
        return nonNull(items) && items.size() == 1;
    }

    public static <T> List<T> sorted(List<T> items) {
        checkNonNull(items);
        return items.stream().sorted().collect(toList());
    }

    public static <T> List<T> sorted(List<T> items, Comparator<T> comparator) {
        checkNonNull(items);
        return items.stream().sorted(comparator).collect(toList());
    }

    public static <T> T first(List<T> items) {
        return nth(items, 1);
    }

    public static <T> Optional<T> firstIfPresent(List<T> items) {
        checkNonNull(items);
        return items.isEmpty() ? Optional.empty() : Optional.of(first(items));
    }

    public static <T> T second(List<T> items) {
        return nth(items, 2);
    }

    public static <T> T third(List<T> items) {
        return nth(items, 3);
    }

    public static <T> T fourth(List<T> items) {
        return nth(items, 4);
    }

    public static <T> T fifth(List<T> items) {
        return nth(items, 5);
    }

    public static <T> T penultimate(List<T> items) {
        return secondToLast(items);
    }

    public static <T> T secondToLast(List<T> items) {
        checkNonNull(items);
        return nth(items, items.size() - 1);
    }

    public static <T> T last(List<T> items) {
        checkNonNull(items);
        return nth(items, items.size());
    }

    public static <T> Optional<T> lastIfPresent(List<T> items) {
        checkNonNull(items);
        return items.isEmpty() ? Optional.empty() : Optional.of(last(items));
    }

    public static <T> T nth(List<T> items, int number) {
        checkArgument(number > 0, "number must be positive");
        checkMinimumSize(items, number);
        return items.get(number - 1);
    }

    private static <T> void checkMinimumSize(List<T> items, int minSize) {
        checkNonNull(items);
        checkArgument(items.size() >= minSize,
                "expected at least %s items (actual size: %s)",
                minSize, items.size());
    }

    private static <T> void checkNonNull(List<T> items) {
        requireNonNull(items, "items cannot be null");
    }

}
