package org.kiwiproject.spring.data;

import lombok.Getter;
import lombok.Setter;
import org.kiwiproject.base.KiwiDeprecated;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

/**
 * A generic aggregate result containing a list of results, and a total count.
 *
 * @param <T> the content type contained in this aggregate result
 * @implNote Used by the deprecated {@link PagingQuery#aggregatePage(Class, AggregationOperation...)};
 * see that method's documentation for details.
 * @deprecated This class supports the deprecated
 * {@link PagingQuery#aggregatePage(Class, AggregationOperation...)} and will be removed with it.
 * Will be removed without replacement.
 */
@Getter
@Setter
@Deprecated(since = "5.3.0", forRemoval = true)
@KiwiDeprecated(
        replacedBy = "will be removed without replacement",
        reference = "#1400",
        usageSeverity = KiwiDeprecated.Severity.SEVERE
)
@SuppressWarnings({"java:S1133", "DeprecatedIsStillUsed"})
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
