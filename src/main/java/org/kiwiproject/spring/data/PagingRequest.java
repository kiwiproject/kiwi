package org.kiwiproject.spring.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Sort;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

/**
 * JAX-RS based implementation of {@link PagingParams}.
 * <p>
 * Intended to be used in JAX-RS resource classes with HTTP {@link javax.ws.rs.GET} endpoint methods having an
 * argument annotated with {@link javax.ws.rs.BeanParam}.
 * <p>
 * Example:
 * <pre>
 * {@literal @}GET
 *  public Response page(@BeanParam PagingRequest pagingRequest) {
 *     // ...
 *  }
 * </pre>
 *
 * @implNote Requires Spring Data Commons and the JAX-RS API to be available at runtime.
 */
@Getter
@Setter
@ToString
public class PagingRequest implements PagingParams {

    public static final int DEFAULT_MAX_LIMIT = 100;

    // IMPLEMENTATION NOTE:
    // For the @DefaultValue annotations using the Sort.Direction enum, we must use a constant value, meaning
    // we cannot use the enum and call name(). And unfortunately, you cannot get around it even if you define a
    // private static final String in this or any other class. The compiler error is always:
    // "element value must be a constant expression"

    /**
     * The page number. Default is zero.
     * <p>
     * Note this does not preclude using either zero or one-based page numbering, but other classes may require
     * one or the other page numbering scheme.
     */
    @QueryParam("page")
    @DefaultValue("0")
    private Integer page = 0;

    /**
     * The page size limit. Default is 100.
     */
    @QueryParam("limit")
    @DefaultValue("" + DEFAULT_MAX_LIMIT)
    private Integer limit = DEFAULT_MAX_LIMIT;

    /**
     * The primary sort property. Default is null.
     * <p>
     * This is only relevant if a primarySort has been specified.
     */
    @QueryParam("primarySort")
    private String primarySort;

    /**
     * The primary sort direction. Default is ascending (ASC).
     */
    @QueryParam("primaryDirection")
    @DefaultValue("ASC")
    private Sort.Direction primaryDirection;

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
    private Sort.Direction secondaryDirection;
}
