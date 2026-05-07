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
     * Return the number of elements on this page.
     *
     * @return the number of elements on this page, which may be less than {@link #getPageSize()}
     *         when this is the last page or when this page has no content
     */
    default int getNumberOfElements() {
        return getContent().size();
    }
}
