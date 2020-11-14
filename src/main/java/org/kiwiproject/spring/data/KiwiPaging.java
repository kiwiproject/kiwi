package org.kiwiproject.spring.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;
import static org.kiwiproject.collect.KiwiArrays.isNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.subListExcludingFirst;
import static org.kiwiproject.search.KiwiSearching.checkPageSize;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

/**
 * Static utilities to allow simple construction of Spring Data {@link Sort} and {@link Pageable} objects.
 */
@UtilityClass
public class KiwiPaging {

    /**
     * Constructs a {@link Pageable} object using a {@link PagingParams} as input.
     * <p>
     * Since Spring's {@link Pageable} assumes zero-based page numbering, the {@link PagingParams} should use that
     * same convention or else the page numbering will be wrong.
     */
    public static Pageable createPageable(PagingParams pagedRequest) {
        checkArgumentNotNull(pagedRequest, "pagedRequest cannot be null");
        checkArgumentNotNull(pagedRequest.getPage(), "page cannot be null");
        checkArgumentNotNull(pagedRequest.getLimit(), "limit cannot be null");

        return createPageable(pagedRequest.getPage(), pagedRequest.getLimit(),
                pagedRequest.getPrimaryDirection(),
                pagedRequest.getPrimarySort(),
                pagedRequest.getSecondaryDirection(),
                pagedRequest.getSecondarySort());
    }

    /**
     * Constructs a {@link Pageable} object using a page number and size, as well as an arbitrary
     * varargs of Sort direction and Field name pairs.
     * <p>
     * Since Spring's {@link Pageable} assumes zero-based page numbering, the {@link PagingParams} should use that
     * same convention or else the page numbering will be wrong.
     *
     * @param pageNumber                     current page number, <em>numbered from zero</em>
     * @param sizePerPage                    number of elements per page
     * @param sortDirectionAndFieldNamePairs sort direction and field pairs (must be an even number of arguments)
     * @return a Pageable instance that defines the current page attributes as well as the combined Sort characteristics
     * @implNote Any pairs containing a null direction and/or property are ignored, so it is safe to call this
     * method with varargs such as {@code { ASC, "lastName", ASC, null }} or {@code { ASC, null, ASC, null}} or
     * even {@code { null, null, null, null}}
     */
    public static Pageable createPageable(int pageNumber, int sizePerPage, Object... sortDirectionAndFieldNamePairs) {
        // TODO Use KiwiPreconditions checkPositive once get version having message
        checkArgument(pageNumber >= 0, "pageNumber cannot be negative");
        checkPageSize(sizePerPage);

        var sort = constructSortChainFromPairs(sortDirectionAndFieldNamePairs);
        if (nonNull(sort)) {
            return PageRequest.of(pageNumber, sizePerPage, sort);
        }

        return PageRequest.of(pageNumber, sizePerPage);
    }

    /**
     * Creates a {@link Sort} chain from a varargs list of sort direction and field pairs. Automatically chains the
     * Sort objects in the order that they were provided.
     *
     * @param directionAndFieldPairs sort direction and field pairs (must be an even number of arguments)
     * @return a new Sort instance, or {@code null} if the given varargs is empty
     */
    public static Sort constructSortChainFromPairs(Object... directionAndFieldPairs) {
        List<Sort> sortList = constructSortListFromPairs(directionAndFieldPairs);
        return constructSortChain(sortList);
    }

    /**
     * Constructs a list of Sort properties from a varargs of sort direction and field pairs.
     *
     * @param directionAndFieldPairs sort direction and field pairs (must be an even number of arguments)
     * @return a List of Sort properties, never {@code null}
     */
    public static List<Sort> constructSortListFromPairs(Object... directionAndFieldPairs) {
        checkEvenItemCount(directionAndFieldPairs);

        // NOTE: We cannot use List.of since there can be nulls in the pairs. That is why this uses newArrayList.
        List<List<Object>> directionFieldPairs = partition(newArrayList(directionAndFieldPairs), 2);

        return directionFieldPairs.stream()
                .filter(KiwiPaging::containsDirectionAndProperty)
                .map(KiwiPaging::sortFromDirectionAndProperty)
                .collect(toList());
    }

    private static boolean containsDirectionAndProperty(List<Object> directionFieldPair) {
        return first(directionFieldPair) instanceof Sort.Direction && second(directionFieldPair) instanceof String;
    }

    private static Sort sortFromDirectionAndProperty(List<Object> directionFieldPair) {
        var direction = (Sort.Direction) first(directionFieldPair);
        var property = (String) second(directionFieldPair);
        return Sort.by(direction, property);
    }

    /**
     * Accepts a varargs of Sort properties and chains them together in order.
     *
     * @param sorts the list of Sort properties
     * @return a new Sort instance, or {@code null} if the given varargs is empty
     */
    public static Sort constructSortChain(Sort... sorts) {
        if (isNullOrEmpty(sorts)) {
            return null;
        }

        // NOTE: We must use newArrayList here instead of List.of because there can be null sorts
        return constructSortChain(newArrayList(sorts));
    }

    /**
     * Accepts a list of Sort properties and chains them together in order.
     *
     * @param sorts the list of Sort properties
     * @return a new Sort instance, or {@code null} if the given list is empty
     */
    public static Sort constructSortChain(List<Sort> sorts) {
        if (isNullOrEmpty(sorts)) {
            return null;
        }

        var filteredSorts = sorts.stream()
                .filter(Objects::nonNull)
                .collect(toList());

        var firstSort = first(filteredSorts);
        var restOfSorts = subListExcludingFirst(filteredSorts);
        return restOfSorts.stream().reduce(firstSort, Sort::and);
    }
}
