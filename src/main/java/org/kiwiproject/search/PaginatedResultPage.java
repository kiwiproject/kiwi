package org.kiwiproject.search;

import java.util.List;

/**
 * Interface that represents a page of results and contains pagination metadata.
 *
 * @param <T> the type of content on the page
 */
public interface PaginatedResultPage<T> extends PaginatedResult {

    /**
     * The content on this page.
     *
     * @return an unmodifiable list of items; must not be null
     */
    List<T> getContent();

    /**
     * The number of elements on this page. This may be less than {@link #getPageSize()} on the last page
     * when the total count is not evenly divisible by the page size.
     *
     * @return the number of elements on this page
     */
    default int getNumberOfElements() {
        return getContent().size();
    }
}
