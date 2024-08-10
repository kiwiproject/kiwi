package org.kiwiproject.search;

/**
 * Simple interface defining basic pagination for any kind of search, e.g., a database query, SOLR search, etc.
 */
public interface PaginatedResult {

    /**
     * The total number of results, though a {@link PaginatedResult} instance will contain only a subset of the total.
     *
     * @return total number of results
     */
    long getTotalCount();

    /**
     * The page number of this result. Can be used in both zero- and one-based page numbering schemes as long
     * as it is used consistently, e.g., the server and clients are both using one-based page numbering.
     *
     * @return the page number
     */
    int getPageNumber();

    /**
     * The page size being used to paginate the search results.
     *
     * @return the page size
     */
    int getPageSize();
}
