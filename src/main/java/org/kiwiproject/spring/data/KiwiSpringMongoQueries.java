package org.kiwiproject.spring.data;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.util.function.KiwiBiConsumers;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collection;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Static utilities for performing MongoDB queries using Spring Data.
 */
@UtilityClass
@Slf4j
public class KiwiSpringMongoQueries {

    private static final String ANY_STRING = ".*";
    private static final String CASE_INSENSITIVE_OPTION = "i";

    /**
     * Paginate objects of the given class, which are assumed to be mapped to a Mongo collection, using the given
     * paging parameters.
     *
     * @param mongoTemplate the {@link MongoTemplate} that is used to perform the MongoDB operations
     * @param pagingParams  the parameters describing the desired pagination
     * @param clazz         the domain/model class mapped to a Mongo collection
     * @param <T>           the result type
     * @param <P>           the pagination parameter type
     * @return a {@link Page} containing the paginated results
     */
    public static <T, P extends PagingParams> Page<T> paginate(MongoTemplate mongoTemplate,
                                                               P pagingParams,
                                                               Class<T> clazz) {

        return paginate(mongoTemplate, pagingParams, clazz, KiwiBiConsumers.noOp());
    }

    /**
     * Paginate objects of the given class, which are assumed to be mapped to a Mongo collection, using the given
     * paging parameters.
     * <p>
     * The {@code criteriaBuilder} is a {@link BiConsumer} that can be used to specify restriction criteria and/or
     * to access or change the pagination parameters.
     *
     * @param mongoTemplate   the {@link MongoTemplate} that is used to perform the MongoDB operations
     * @param pagingParams    the parameters describing the desired pagination
     * @param clazz           the domain/model class mapped to a Mongo collection
     * @param criteriaBuilder a {@link BiConsumer} that can be used to add additional pagination and query criteria
     * @param <T>             the result type
     * @param <P>             the pagination parameter type
     * @return a {@link Page} containing the paginated results, optionally filtered by criteria
     */
    public static <T, P extends PagingParams> Page<T> paginate(MongoTemplate mongoTemplate,
                                                               P pagingParams,
                                                               Class<T> clazz,
                                                               BiConsumer<PagingQuery, P> criteriaBuilder) {

        checkArgumentNotNull(mongoTemplate);
        checkArgumentNotNull(pagingParams);
        checkArgumentNotNull(clazz);
        checkArgumentNotNull(criteriaBuilder);

        if (isNull(pagingParams.getLimit()) || pagingParams.getLimit() < 1) {
            LOG.warn("No limit was supplied; setting it to 1. Supply a limit to avoid this warning");
            pagingParams.setLimit(1);
        }

        if (isNull(pagingParams.getPage()) || pagingParams.getPage() < 0) {
            LOG.warn("No page number was supplied; setting it to 0. Supply a page number to avoid this warning");
            pagingParams.setPage(0);
        }

        LOG.debug("Performing search using params: {}", pagingParams);
        var pageable = KiwiPaging.createPageable(pagingParams);
        var query = new PagingQuery(mongoTemplate).with(pageable);
        criteriaBuilder.accept(query, pagingParams);

        LOG.debug("Executing query: {}", query);
        return query.findPage(clazz);
    }

    /**
     * Add date restrictions to the given property.
     * <p>
     * Specify both start and end milliseconds (since the epoch) to create a closed range, or specify only start or
     * end milliseconds to create an open-range. E.g., if only start milliseconds is specified, then the criteria
     * includes only dates that are equal to or after the given value, with no upper bound.
     * <p>
     * If both start and end milliseconds are null, the call is a no-op.
     *
     * @param query                    the MongoDB query on which to add the criteria
     * @param propertyName             the property name, which is expected to be of type {@link Date}
     * @param startDateInclusiveMillis the start date, inclusive; it may be null.
     * @param endDateInclusiveMillis   the end date, inclusive; it may be null.
     */
    public static void addDateBounds(Query query,
                                     String propertyName,
                                     @Nullable Long startDateInclusiveMillis,
                                     @Nullable Long endDateInclusiveMillis) {

        checkArgumentNotNull(query);
        checkArgumentNotNull(propertyName);

        if (isNull(startDateInclusiveMillis) && isNull(endDateInclusiveMillis)) {
            LOG.debug("start and end are both null; ignoring");
            return;
        }

        checkArgumentNotNull(propertyName, "property must not be null");

        var datePropertyCriteria = Criteria.where(propertyName);

        // lower date bound
        if (nonNull(startDateInclusiveMillis)) {
            datePropertyCriteria.gte(new Date(startDateInclusiveMillis));
        }

        // upper date bound
        if (nonNull(endDateInclusiveMillis)) {
            datePropertyCriteria.lte(new Date(endDateInclusiveMillis));
        }

        query.addCriteria(datePropertyCriteria);
    }

    /**
     * Defines whether to require a partial or exact match.
     */
    public enum PartialMatchType {

        /**
         * Permits regex matching in a case-insensitive manner.
         *
         * @see Criteria#regex(String)
         */
        PARTIAL_MATCH,

        /**
         * Requires an equal match, and is case-sensitive.
         *
         * @see Criteria#is(Object)
         */
        EQUAL_MATCH;

        /**
         * Convert the given string into a {@link PartialMatchType}, where "truthy" values are considered to
         * represent {@link #PARTIAL_MATCH}, and "falsy" values are considered to mean {@link #EQUAL_MATCH}.
         * <p>
         * Accepts various values, such as "true", "false", "yes", "no", etc. and null is treated as false.
         *
         * @param value the value to convert
         * @return the {@link PartialMatchType}
         * @implNote Uses {@link BooleanUtils#toString()} to perform the conversion
         */
        public static PartialMatchType fromBooleanString(String value) {
            var allowPartialMatch = BooleanUtils.toBoolean(value);
            return from(allowPartialMatch);
        }

        /**
         * Convert the given boolean into a {@link PartialMatchType}. True is converted to {@link #PARTIAL_MATCH}
         * and false is converted to {@link #EQUAL_MATCH}.
         *
         * @param value the value to convert
         * @return the {@link PartialMatchType}
         */
        public static PartialMatchType from(boolean value) {
            if (value) {
                return PARTIAL_MATCH;
            }

            return EQUAL_MATCH;
        }
    }

    /**
     * Add a partial or equal match criteria for the given property and match string.
     *
     * @param query        the MongoDB query on which to add the criteria
     * @param matchString  the string to match
     * @param propertyName the property name
     * @param matchType    the desired match type
     */
    public static void addPartialOrEqualMatchCriteria(Query query,
                                                      String matchString,
                                                      String propertyName,
                                                      PartialMatchType matchType) {

        checkArgumentNotNull(query);
        checkArgumentNotNull(propertyName);
        checkArgumentNotNull(matchType);

        if (isBlank(matchString)) {
            LOG.debug("matchString is blank; ignoring");
            return;
        }

        Criteria matchCriteria;

        if (matchType == PartialMatchType.PARTIAL_MATCH) {
            matchCriteria = Criteria.where(propertyName).regex(ANY_STRING + matchString + ANY_STRING, CASE_INSENSITIVE_OPTION);
        } else {
            matchCriteria = Criteria.where(propertyName).is(matchString);
        }

        query.addCriteria(matchCriteria);
    }

    /**
     * Add a partial or equal match criteria for the given property and match strings. Any of the match strings
     * are considered to be a match, i.e., this effectively performs an <em>OR</em> operation.
     *
     * @param query        the MongoDB query on which to add the criteria
     * @param matchStrings the strings to match, using an <em>OR</em> operation
     * @param propertyName the property name
     * @param matchType    the desired match type
     */
    public static void addMultiplePartialOrEqualMatchCriteria(Query query,
                                                              Collection<String> matchStrings,
                                                              String propertyName,
                                                              PartialMatchType matchType) {

        checkArgumentNotNull(query);
        checkArgumentNotNull(propertyName);
        checkArgumentNotNull(matchType);

        if (isNull(matchStrings) || matchStrings.isEmpty()) {
            LOG.debug("matchStrings is null or empty; ignoring");
            return;
        }

        Criteria matchCriteria;

        if (matchType == PartialMatchType.PARTIAL_MATCH) {
            var termCriteria = matchStrings
                    .stream()
                    .map(term -> Criteria.where(propertyName).regex(ANY_STRING + term + ANY_STRING, CASE_INSENSITIVE_OPTION))
                    .toArray(Criteria[]::new);
            matchCriteria = new Criteria().orOperator(termCriteria);
        } else {
            matchCriteria = Criteria.where(propertyName).in(matchStrings);
        }

        query.addCriteria(matchCriteria);
    }

    /**
     * Adds a {@link Criteria#in(Object...)} for the given property using values separated by commas in {@code csv}.
     *
     * @param query        the MongoDB query on which to add the criteria
     * @param csv          a comma-separated list of acceptable values
     * @param propertyName the property name
     */
    public static void addInCriteriaFromCsv(Query query, String csv, String propertyName) {
        addInCriteriaFromCsv(query, csv, propertyName, Function.identity());
    }

    /**
     * Adds a {@link Criteria#in(Object...)} for the given property using by first separating the values by comma in
     * {@code csv}, and then applying the given function to each value.
     *
     * @param query        the MongoDB query on which to add the criteria
     * @param csv          a comma-separated list of acceptable values
     * @param propertyName the property name
     * @param converter    a function to convert the separated strings into a different type
     * @param <T>          the result type
     */
    public static <T> void addInCriteriaFromCsv(Query query,
                                                String csv,
                                                String propertyName,
                                                Function<String, T> converter) {

        checkArgumentNotNull(query);
        checkArgumentNotNull(propertyName);
        checkArgumentNotNull(converter);

        if (isBlank(csv)) {
            LOG.debug("csv is blank; ignoring");
            return;
        }

        var values = KiwiStrings.nullSafeSplitOnCommas(csv);
        var convertedValues = values.stream().map(converter).toList();
        query.addCriteria(Criteria.where(propertyName).in(convertedValues));
    }

}
