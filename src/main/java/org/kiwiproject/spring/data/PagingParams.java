package org.kiwiproject.spring.data;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.data.domain.Sort;

/**
 * Defines the contract for query parameters for performing pagination and sorting.
 * <p>
 * Note this allows standard pagination properties like page and limit, but also provides for two levels of
 * sorting, a "primary" and a "secondary". For example, you might want to sort people's names by last name descending,
 * then by first name ascending.
 * <p>
 * Note also that if there is only one sort, only the primary sort and direction should be set. If the primary
 * sort is not defined, but the secondary sort is defined, then the behavior is up to the implementation. e.g. it
 * may choose to ignore the secondary sort entirely, or it might treat the secondary sort as the primary one.
 * <p>
 * A page number and page size limit should always be specified. Sorting parameters are optional, but as mentioned
 * above, the primary sort property and direction should be specified when only one level of sorting is required.
 * Both primary and secondary sort can be specified when two levels are required.
 *
 * @implNote Our specific needs have never required more than primary and second sorts, which is why there are only
 * these two levels.
 */
public interface PagingParams {

    /**
     * @return the page number
     */
    Integer getPage();

    /**
     * @param page the page number
     */
    void setPage(Integer page);

    /**
     * @return the page size limit
     */
    Integer getLimit();

    /**
     * @param limit the page size limit
     */
    void setLimit(Integer limit);

    /**
     * @return the primary sort property
     */
    String getPrimarySort();

    /**
     * @param primarySort the primary sort property
     */
    void setPrimarySort(String primarySort);

    /**
     * @return the primary sort direction
     * @apiNote Implementations should return a non-null value if {@link #hasPrimarySort()} returns true
     */
    Sort.Direction getPrimaryDirection();

    /**
     * @param primaryDirection the primary sort direction
     */
    void setPrimaryDirection(Sort.Direction primaryDirection);

    /**
     * @return the secondary sort property
     */
    String getSecondarySort();

    /**
     * @param secondarySort the secondary sort property
     */
    void setSecondarySort(String secondarySort);

    /**
     * @return the secondary sort direction
     * @apiNote Implementations should return a non-null value if {@link #hasSecondarySort()} returns true
     */
    Sort.Direction getSecondaryDirection();

    /**
     * @param secondaryDirection the secondary sort direction
     */
    void setSecondaryDirection(Sort.Direction secondaryDirection);

    /**
     * Check if there is a primary or secondary sort.
     *
     * @return true if there is a primary or secondary sort
     */
    default boolean hasSort() {
        return hasPrimarySort() || hasSecondarySort();
    }

    /**
     * Check if there is a primary sort.
     *
     * @return true if {@link #getPrimarySort()} is not blank
     */
    default boolean hasPrimarySort() {
        return isNotBlank(getPrimarySort());
    }

    /**
     * Check if there is a secondary sort.
     *
     * @return true if {@link #getSecondarySort()} is not blank
     */
    default boolean hasSecondarySort() {
        return isNotBlank(getSecondarySort());
    }
}
