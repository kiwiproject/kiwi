package org.kiwiproject.search;

/**
 * Simple contract for search results that are sorted in a specific direction.
 */
public interface Sorted {

    /**
     * Defines a general sort direction, which will usually by something like "asc" or "desc" but can be customized
     * however an application likes.
     *
     * @return the sort direction
     */
    String getSortDirection();
}
