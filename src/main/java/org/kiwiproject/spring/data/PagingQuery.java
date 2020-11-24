package org.kiwiproject.spring.data;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A subclass of {@link Query} that adds pagination helpers.
 * <p>
 * Expects a {@link Pageable} to be set when performing query operations. Use {@link #with(Pageable)} to add or change
 * the pagination of this instance.
 */
@Slf4j
@SuppressWarnings("java:S2160")  // we don't need to override equals
public class PagingQuery extends Query {

    private final MongoTemplate mongoTemplate;
    private Pageable pageable;

    private static final ConcurrentMap<Class<?>, AggregateResult<?>> CLASS_WRAPPERS = new ConcurrentHashMap<>();

    /**
     * Construct an instance.
     *
     * @param mongoTemplate the {@link MongoTemplate} that this instance will use internally
     */
    public PagingQuery(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Add/change the pagination of {@code this} instance.
     *
     * @param pageable the {@link Pageable} that specifies this query's pagination
     * @return this instance
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public PagingQuery with(Pageable pageable) {
        this.pageable = pageable;
        super.with(pageable);
        return this;
    }

    /**
     * Finds a specific page for the given type of object, which is assumed to be mapped to a MongoDB collection.
     *
     * @param clazz the domain/model class mapped to a Mongo collection
     * @param <T>   the result type
     * @return a {@link Page} of results
     * @implNote Due to <a href="https://jira.spring.io/browse/DATAMONGO-1783">DATAMONGO-1783</a>, we have to create
     * a {@link Query} with the same criteria but which is not paginated; otherwise the count query is limited to the
     * limit specified on {@link Pageable} specified in the {@link #with(Pageable)} method. We don't quite understand
     * why you would ever want to limit a count query, especially in the context of pagination, since you basically
     * can get an incorrect result that is always limited to the specified count.
     */
    public <T> Page<T> findPage(Class<T> clazz) {
        checkArgumentNotNull(clazz);
        checkPageableNotNull();

        var unlimitedPageable = KiwiPaging.createPageable(0, Integer.MAX_VALUE);
        var unpagedQuery = Query.of(this).with(unlimitedPageable);
        long count = mongoTemplate.count(unpagedQuery, clazz);

        List<T> results = mongoTemplate.find(this, clazz);
        return new PageImpl<>(results, this.pageable, count);
    }

    /**
     * Aggregates a page of results for the given type of object,  which is assumed to be mapped to a MongoDB
     * collection. <strong>Please make sure to read the caveats and possible problems sections below.</strong>
     * <h3>Caveats and Possible Problems</h3>
     * Not all types of {@link AggregationOperation} will actually work here, which is why it is marked as beta.
     * For example, using an {@link Aggregation#project(String...)} will fail because the internal implementation is
     * assuming the "shape" of the result is an {@link AggregateResult} with the given type {@code T} and it performs
     * its own internal projection and faceting. Other types may work, for example {@link Aggregation#match(Criteria)}
     * works, as do lookup (i.e. "left join") aggregations.
     * <p>
     * Note also that a sort must be specified in the {@link Pageable} given to {@link #with(Pageable)}. Otherwise
     * an {@link org.springframework.data.mongodb.UncategorizedMongoDbException UncategorizedMongoDbException} will be
     * thrown with the error: "{@code $sort stage must have at least one sort key}".
     * <h3>Recommendations for New Code</h3>
     * Based on the above restrictions and potential usage problems, <strong>we strongly recommend avoiding this
     * method for new code</strong>, as its original purpose was very limited in scope, mainly to perform lookup (join)
     * operations in order to return an "aggregate" page that contained model objects as well as their associated
     * objects. Unfortunately, since we have various usages of this method sprinkled across a few dozen Dropwizard
     * services, we cannot remove it until we find and replace all those usages (with something else that we have
     * not implemented as of now).
     *
     * @param clazz          the domain/model class mapped to a Mongo collection
     * @param aggregationOps one or more aggregation operations
     * @param <T>            the result type
     * @return the aggregated {@link Page}
     */
    @Beta
    public <T> Page<T> aggregatePage(Class<T> clazz, AggregationOperation... aggregationOps) {
        checkArgumentNotNull(clazz);
        checkPageableNotNull();

        LOG.debug("Preparing aggregation query for class: {}", clazz);
        List<AggregationOperation> aggregations = buildNewAggregationWithOps(aggregationOps);
        aggregations.add(buildResultFacet());
        aggregations.add(buildResultProjection());

        Class<? extends AggregateResult<T>> wrappedClass = getClassWrapper(clazz);
        TypedAggregation<? extends AggregateResult<T>> typedAggregation = new TypedAggregation<>(wrappedClass, aggregations);

        LOG.debug("Executing aggregation query: {}", typedAggregation);
        try {
            AggregationResults<? extends AggregateResult<T>> aggregationResults =
                    executeAggregationQuery(clazz, wrappedClass, typedAggregation);

            return convertToPage(aggregationResults);
        } catch (Exception ex) {
            LOG.error("Encountered error performing aggregation query", ex);
            throw ex;
        }
    }

    private void checkPageableNotNull() {
        checkState(nonNull(this.pageable), "this.pageable cannot be null; call with(Pageable) first");
    }

    private List<AggregationOperation> buildNewAggregationWithOps(AggregationOperation[] aggregationOps) {
        var aggregations = new ArrayList<AggregationOperation>();
        if (isNotNullOrEmpty(this.getCriteria())) {
            //noinspection SuspiciousToArrayCall
            var matchOperation = Aggregation.match(
                    new Criteria().andOperator(this.getCriteria().toArray(Criteria[]::new)));
            aggregations.add(matchOperation);
        }

        if (nonNull(aggregationOps)) {
            var aggregationOpList = newArrayList(aggregationOps)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(toList());
            aggregations.addAll(aggregationOpList);
        }

        return aggregations;
    }

    private FacetOperation buildResultFacet() {
        return Aggregation.facet(buildPagingOperations())
                .as("results")
                .and(Aggregation.count().as("count"))
                .as("totalCount");
    }

    private AggregationOperation[] buildPagingOperations() {
        return new AggregationOperation[]{
                Aggregation.sort(pageable.getSort()),
                Aggregation.skip(getSkip()),
                Aggregation.limit(getLimit())
        };
    }

    private static ProjectionOperation buildResultProjection() {
        return Aggregation.project("results")
                .and("totalCount.count")
                .arrayElementAt(0)
                .as("totalCount");
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends AggregateResult<T>> getClassWrapper(Class<T> clazz) {
        var resultClass = CLASS_WRAPPERS.computeIfAbsent(clazz, AggregateResult::of);
        return (Class<? extends AggregateResult<T>>) resultClass.getClass();
    }

    @VisibleForTesting
    private <T> AggregationResults<? extends AggregateResult<T>> executeAggregationQuery(
            Class<T> clazz,
            Class<? extends AggregateResult<T>> wrappedClass,
            TypedAggregation<? extends AggregateResult<T>> typedAggregation) {

        return mongoTemplate.aggregate(typedAggregation, clazz, wrappedClass);
    }

    private <T> PageImpl<T> convertToPage(AggregationResults<? extends AggregateResult<T>> aggregateResults) {
        AggregateResult<T> result = aggregateResults.getUniqueMappedResult();
        if (isNull(result)) {
            LOG.warn("Received a NULL result from aggregation query");
            throw new IllegalStateException("Query failed to obtain an aggregate result");
        }
        return new PageImpl<>(result.getResults(), pageable, result.getTotalCount());
    }
}
