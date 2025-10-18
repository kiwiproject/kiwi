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
}
