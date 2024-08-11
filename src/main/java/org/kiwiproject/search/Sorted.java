package org.kiwiproject.search;

/**
 * Simple contract for search results that are sorted in a specific direction.
 */
public interface Sorted {

    /**
     * Defines a general sort direction, which will usually by something like "asc" or "desc" but it can be customized
     * as an application likes.
     *
     * @return the sort direction
     */
    String getSortDirection();
}
