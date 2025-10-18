package org.kiwiproject.search;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requirePositive;
import static org.kiwiproject.base.KiwiPreconditions.requirePositiveOrZero;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.kiwiproject.search.KiwiSearching.PageNumberingScheme;

import java.util.List;

/**
 * A basic implementation of {@link PaginatedResultPage}, suitable for use cases
 * where search results are presented one page at a time. It provides the pagination
 * metadata and page content.
 * <p>
 * Page numbers may be zero- or one-based, but only non-negative values are allowed.
 * The page size must be positive, and the total count must be zero or positive.
 * <ul>
 *     <li>If {@code totalCount} is zero, {@code content} must be empty.</li>
 *     <li>If {@code totalCount} &gt; 0, {@code content} must have between 0 and {@code pageSize} elements (inclusive).</li>
 *     <li>The {@code content} list is defensively copied and is unmodifiable.</li>
 * </ul>
 * <p>
 * If a page number is beyond the total number of available pages, the result may contain an
 * empty content list even when {@code totalCount} is greater than zero.
 * </p>
 * Servers and clients should agree on the page numbering scheme (zero- or one-based).
 *
 * @param <T> the type of content on the page
 */
@Getter
@ToString(exclude = "content")
public class SimplePaginatedResultPage<T> implements PaginatedResultPage<T> {

    private final long totalCount;
    private final int pageNumber;
    private final int pageSize;
    private final List<T> content;

    /**
     * Create a new instance.
     * <p>
     * Note that {@code pageNumber} may be zero- or one-based, and is not enforced here
     * except to ensure the page number is zero or positive. Servers and clients should
     * agree upon and use the same page numbering scheme.
     *
     * @param pageNumber the page number, which may be zero or one-based
     * @param pageSize   the maximum number of elements on a page
     * @param totalCount the total number of elements in the search results
     * @param content    the content for this page, which may be empty but not null
     */
    @JsonCreator
    @Builder
    public SimplePaginatedResultPage(@JsonProperty("pageNumber") int pageNumber,
                                     @JsonProperty("pageSize") int pageSize,
                                     @JsonProperty("totalCount") long totalCount,
                                     @JsonProperty("content") List<T> content) {
        this.pageNumber = requirePositiveOrZero(pageNumber, "pageNumber must be zero or positive");
        this.pageSize = requirePositive(pageSize, "pageSize must be positive");
        this.totalCount = requirePositiveOrZero(totalCount, "totalCount must be zero or positive");
        this.content = validateAndCopyContent(content, pageSize, totalCount);
    }

    private static <T> List<T> validateAndCopyContent(List<T> content,
                                                      @Positive int pageSize,
                                                      @NonNegative long totalCount) {
        checkArgumentNotNull(content, "content must not be null");
        if (totalCount == 0) {
            checkArgument(content.isEmpty(), "content must be empty when totalCount is zero");
        } else {
            checkArgument(content.size() <= pageSize, "content must not contain more elements than pageSize");
        }
        return List.copyOf(content);
    }

    /**
     * Convenience method that computes a zero-based offset (e.g., for use in SQL queries) for
     * a zero-based page numbering scheme.
     *
     * @return the zero-based offset
     */
    @JsonIgnore
    public long getZeroBasedOffsetForZeroBasedPaging() {
        return getZeroBasedOffset(PageNumberingScheme.ZERO_BASED);
    }

    /**
     * Convenience method that computes a zero-based offset (e.g., for use in SQL queries) for
     * a one-based page numbering scheme.
     *
     * @return the zero-based offset
     */
    @JsonIgnore
    public long getZeroBasedOffsetForOneBasedPaging() {
        return getZeroBasedOffset(PageNumberingScheme.ONE_BASED);
    }

    /**
     * Convenience method that computes a zero-based offset (e.g., for use in SQL queries) for
     * the given page numbering scheme.
     *
     * @param pageNumberingScheme the page numbering scheme
     * @return the zero-based offset
     */
    public long getZeroBasedOffset(PageNumberingScheme pageNumberingScheme) {
        return KiwiSearching.zeroBasedOffset(pageNumber, pageNumberingScheme, pageSize);
    }
}
