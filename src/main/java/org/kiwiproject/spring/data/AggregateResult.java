package org.kiwiproject.spring.data;

import com.google.common.annotations.Beta;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

/**
 * A generic aggregate result containing a list of results, and a total count.
 *
 * @param <T> the content type contained in this aggregate result
 * @implNote Marked as beta because it is used by {@link PagingQuery#aggregatePage(Class, AggregationOperation...)}.
 * Read the docs there for an explanation why that is beta.
 */
@Getter
@Setter
@Beta
public class AggregateResult<T> {

    private List<T> results;
    private long totalCount;

    /**
     * Factory to create {@link AggregateResult} instances of a given type.
     *
     * @param clazz the Class representing the result type
     * @param <T>   the result type
     * @return a new instance
     */
    @SuppressWarnings("unused")
    public static <T> AggregateResult<T> of(Class<T> clazz) {
        return new AggregateResult<>();
    }
}
