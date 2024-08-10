package org.kiwiproject.spring.data;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kiwiproject.search.KiwiSearching.PageNumberingScheme;
import org.springframework.data.domain.Sort;

/**
 * Jakarta REST-based implementation of {@link PagingParams}.
 * <p>
 * Intended to be used in Jakarta REST resource classes with HTTP {@link jakarta.ws.rs.GET} endpoint methods having an
 * argument annotated with {@link jakarta.ws.rs.BeanParam}.
 * <p>
 * Example:
 * <pre>
 * {@literal @}GET
 *  public Response page(@BeanParam PagingRequest pagingRequest) {
 *     // ...
 *  }
 * </pre>
 *
 * @implNote Requires Spring Data Commons and the Jakarta REST API to be available at runtime.
 */
@Getter
@Setter
@ToString
public class PagingRequest implements PagingParams {

    public static final int DEFAULT_MAX_LIMIT = 100;

    /*
     IMPLEMENTATION NOTE:
     For the @DefaultValue annotations using enums (e.g., Sort.Direction), we must use a constant value, meaning
     we cannot use the enum and call name(). And unfortunately, you cannot get around it even if you define a
     private static final String in this or any other class. The compiler error is always:
     "element value must be a constant expression"
    */

    /**
     * The page number. Default is zero.
     * <p>
     * Note this does not preclude using either zero or one-based page numbering.
     */
    @QueryParam("page")
    @DefaultValue("0")
    private Integer page = 0;

    /**
     * The page numbering scheme used by this paging request.
     */
    @QueryParam("numbering")
    @DefaultValue("ZERO_BASED")
    private PageNumberingScheme numbering = PageNumberingScheme.ZERO_BASED;

    /**
     * The page size limit. Default is 100.
     */
    @QueryParam("limit")
    @DefaultValue("" + DEFAULT_MAX_LIMIT)
    private Integer limit = DEFAULT_MAX_LIMIT;

    /**
     * The primary sort property. Default is null.
     */
    @QueryParam("primarySort")
    private String primarySort;

    /**
     * The primary sort direction. Default is ascending (ASC).
     * <p>
     * This is only relevant if a primarySort has been specified.
     */
    @QueryParam("primaryDirection")
    @DefaultValue("ASC")
    private Sort.Direction primaryDirection = Sort.Direction.ASC;

    /**
     * The secondary sort property. Default is null.
     */
    @QueryParam("secondarySort")
    private String secondarySort;

    /**
     * The secondary sort direction. Default is ascending (ASC).
     * <p>
     * This is only relevant if a secondarySort has been specified.
     */
    @QueryParam("secondaryDirection")
    @DefaultValue("ASC")
    private Sort.Direction secondaryDirection = Sort.Direction.ASC;

    /**
     * Create a copy of the given {@link PagingRequest}.
     *
     * @param original the object to copy
     * @return a new instance containing the same values as {@code original}
     * @throws IllegalArgumentException if {@code original} is {@code null}
     */
    public static PagingRequest copyOf(PagingRequest original) {
        checkArgumentNotNull(original, "PagingRequest to copy must not be null");

        var copy = new PagingRequest();
        copy.setPage(original.getPage());
        copy.setNumbering(original.getNumbering());
        copy.setLimit(original.getLimit());
        copy.setPrimarySort(original.getPrimarySort());
        copy.setPrimaryDirection(original.getPrimaryDirection());
        copy.setSecondarySort(original.getSecondarySort());
        copy.setSecondaryDirection(original.getSecondaryDirection());

        return copy;
    }

    /**
     * Create a copy of this instance.
     *
     * @return a new instance containing the same values as {@code this}
     */
    public PagingRequest copyOf() {
        return copyOf(this);
    }

    /**
     * Ensure the given {@link PagingRequest} has pagination properties by providing default values for any
     * null or invalid properties.
     * <p>
     * Note specifically that this mutates the {@code request} argument in-place. You can use either the instance or
     * static {@code withPaginationProperties} method to create a new instance with valid pagination properties to
     * avoid mutation of the original {@link PagingRequest} instance.
     *
     * @param request the {@link PagingRequest} to check; <em>this argument may be mutated in-place</em>
     * @return true if {@code request} was mutated, otherwise false
     */
    public static boolean ensurePaginationProperties(PagingRequest request) {
        var changed = false;

        var limit = request.getLimit();
        if (isNull(limit) || limit < 1) {
            request.setLimit(DEFAULT_MAX_LIMIT);
            changed = true;
        }

        if (isNull(request.getNumbering())) {
            request.setNumbering(PageNumberingScheme.ZERO_BASED);
            changed = true;
        }

        var minPage = request.getNumbering().getMinimumPageNumber();
        var page = request.getPage();
        if (isNull(page) || page < minPage) {
            request.setPage(minPage);
            changed = true;
        }

        return changed;
    }

    /**
     * Ensure the given {@link PagingRequest} has pagination properties by providing default values for any
     * null or invalid properties, returning the same instance if it is valid, and otherwise returning a new
     * instance.
     *
     * @param request the request to check
     * @return {@code request} if it contains pagination properties, otherwise a new instance with valid values
     * for the missing properties
     */
    public static PagingRequest withPaginationProperties(PagingRequest request) {
        checkArgumentNotNull(request, "PagingRequest must not be null");

        if (request.hasPaginationProperties()) {
            return request;
        }

        var newRequest = request.copyOf();
        ensurePaginationProperties(newRequest);
        return newRequest;
    }

    /**
     * Ensure the given {@link PagingRequest} has pagination properties by providing default values for any
     * null or invalid properties.
     *
     * @return this instance if it contains pagination properties, otherwise a new instance with valid values
     * for the missing properties
     */
    public PagingRequest withPaginationProperties() {
        return withPaginationProperties(this);
    }
}
