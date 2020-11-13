package org.kiwiproject.spring.data;

import org.springframework.data.domain.Sort;

/**
 * Defines the contract for query parameters for performing pagination and sorting.
 * <p>
 * Note this allows standard pagination properties like page and limit, but also provides for two levels of
 * sorting, a "primary" and a "secondary". For example, you might want to sort people's names by last name descending,
 * then by first name ascending.
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
     */
    Sort.Direction getSecondaryDirection();

    /**
     * @param secondaryDirection the secondary sort direction
     */
    void setSecondaryDirection(Sort.Direction secondaryDirection);

}
