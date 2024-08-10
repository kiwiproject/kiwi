package org.kiwiproject.spring.data;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.checkPositive;
import static org.kiwiproject.base.KiwiPreconditions.checkPositiveOrZero;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Represents one page from a list of results.
 * <p>
 * By default, pagination assumes a start page index of 0 (i.e., the page offset). You can change this
 * by calling {@code setPagingStartsWith(int)} or {@code usingOneAsFirstPage()}.
 * <p>
 * You can also indicate whether a sort has been applied to the data by setting the {@link KiwiSort} via
 * the setter method or via {@link #addKiwiSort(KiwiSort)}.
 *
 * @param <T> the type of content this page contains
 */
@Getter
@Setter
@ToString(exclude = "content")
@JsonIgnoreProperties(ignoreUnknown = true)
public class KiwiPage<T> {

    /**
     * The content on this specific page.
     */
    private List<T> content;

    /**
     * The size limit of the pagination, for example, each page can have up to 25 items. The last page will often
     * contain fewer items than this limit unless the total number of items is such that there is no remainder
     * when dividing the total by the page size. For example, if the total number of items is 100 and the page size is
     * 20, then each of the five pages has exactly 20 items (the page size).
     */
    private long size;

    /**
     * The number of this page, e.g. page X of Y.
     */
    private long number;

    /**
     * The number of items/elements on this page. Only on the last page can this be different
     */
    private long numberOfElements;

    /**
     * The total number of pages, calculated from the page size and total number of elements.
     */
    private long totalPages;

    /**
     * The total number of items/elements in the overall result list.
     */
    private long totalElements;

    /**
     * Describes any sort that is active for the pagination. The default value is null.
     */
    private KiwiSort sort;

    /**
     * Allows adjustment for instances where pagination starts with one instead of zero.
     */
    private int pagingStartsWith = 0;

    /**
     * Create a new instance.
     * <p>
     * If you need to add a sort or change {@code pagingStartsWith}, you can chain the {@link #addKiwiSort(KiwiSort)}
     * and {@link #usingOneAsFirstPage()} in a fluent style.
     *
     * @param pageNum     the number of this page; it can be 0 or 1-based (0 is the default)
     * @param limit       the page size limit
     * @param total       the total number of elements in the overall result list
     * @param contentList the content on this page
     * @param <T>         the type of elements on this page
     * @return a new instance
     * @throws IllegalStateException    if any of the numeric arguments are negative or the limit is zero
     * @throws IllegalArgumentException if contentList is null
     */
    public static <T> KiwiPage<T> of(long pageNum, long limit, long total, List<T> contentList) {
        checkPositiveOrZero(pageNum);
        checkPositive(limit);
        checkPositiveOrZero(total);
        checkArgumentNotNull(contentList);

        var page = new KiwiPage<T>();
        page.setContent(contentList);
        page.setSize(limit);  // this page might have fewer elements in contentList than the page size limit
        page.setNumber(pageNum);
        page.setNumberOfElements(contentList.size());
        page.setTotalElements(total);
        page.setTotalPages((long) Math.ceil((double) total / limit));
        return page;
    }

    /**
     * Adds the given sort, returning this instance for method chaining.
     *
     * @param sort the sort to add
     * @return this instance
     */
    public KiwiPage<T> addKiwiSort(KiwiSort sort) {
        setSort(sort);
        return this;
    }

    /**
     * Sets {@code pagingStartsWith} to zero, so that pagination assumes zero-based page numbering.
     * <p>
     * This can also be done via the setter method, but it does not permit method chaining.
     *
     * @return this instance
     */
    public KiwiPage<T> usingZeroAsFirstPage() {
        setPagingStartsWith(0);
        return this;
    }

    /**
     * Sets {@code pagingStartsWith} to zero, so that pagination assumes one-based page numbering.
     * <p>
     * This can also be done via the setter method, but it does not permit method chaining.
     *
     * @return this instance
     */
    public KiwiPage<T> usingOneAsFirstPage() {
        setPagingStartsWith(1);
        return this;
    }

    /**
     * Determines if this is the first page when paginating a result list.
     *
     * @return true if this is the first page
     */
    public boolean isFirst() {
        return number == pagingStartsWith;
    }

    /**
     * Determines if this is the last page when paginating a result list.
     *
     * @return true if this is the last page
     */
    public boolean isLast() {
        var offset = 1 - pagingStartsWith;
        return number == (totalPages - offset);
    }

    /**
     * Does this page have a sort applied?
     *
     * @return true if this page has a sort applied
     */
    public boolean isSorted() {
        return nonNull(sort);
    }
}
