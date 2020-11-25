package org.kiwiproject.spring.data;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.net.KiwiUrls.stripTrailingSlash;
import static org.kiwiproject.spring.data.OrderTestData.ORDER_COLLECTION;
import static org.kiwiproject.spring.data.OrderTestData.insertSampleOrders;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.search.KiwiSearching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.function.Predicate;

/**
 * The code in {@link PagingQuery#aggregatePage(Class, AggregationOperation...)} blows up with a NPE whenever executing
 * it using the in-memory {@link de.bwaldvogel.mongo.MongoServer}, which is the only reason this test even exists.
 * <p>
 * To use this test, you need a "real" MongoDB instance. For now, this test is only enabled when {@code realMongoDB}
 * and {@code realMongoDB.url} system properties are specified and match the expected values. This lets you run the
 * tests locally, even though they won't currently run in our CI environment. Assuming we even keep the aggregatePage
 * method, we'll want to look into using the Flapdoodle embedded Mongo which can be found on GitHub
 * <a href="https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo">here</a>.
 * <p>
 * One easy way is to run a Docker container:
 * {@code docker run -p 27017:27017 --name test-mongo-1 -d mongo:4.0.21}
 * <p>
 * Then set the above-mentioned system properties as follows:
 * {@code -DrealMongoDB=true  -DrealMongoDB.url=mongodb://localhost:27017}
 * <p>
 * And finally run the test.
 * <p>
 * I have manually tested against the following Mongo versions running in Docker containers:
 * <ul>
 *     <li>3.6.21</li>
 *     <li>4.0.21</li>
 *     <li>4.2.11</li>
 *     <li>4.4.2</li>
 * </ul>
 */
@DisplayName("PagingQuery (real MongoDB)")
@ExtendWith(SoftAssertionsExtension.class)
@Slf4j
@EnabledIfSystemProperty(
        named = "realMongoDB",
        matches = "true"
)
@EnabledIfSystemProperty(
        named = "realMongoDB.url",
        matches = "mongodb://[a-zA-Z0-9-.]+:\\d+/?"
)
class PagingQueryRealMongoTest {

    private static MongoTemplate mongoTemplate;

    private List<Order> storedOrders;
    private int storedOrderCount;

    @BeforeAll
    static void beforeAll() {
        var databaseName = "test_db_" + System.nanoTime();
        LOG.info("Using database name: {}", databaseName);

        var mongoUri = stripTrailingSlash(System.getProperty("realMongoDB.url"));
        var connectionString = f("{}/{}?connectTimeoutMS=500&socketTimeoutMS=500&serverSelectionTimeoutMS=500",
                mongoUri, databaseName);
        LOG.info("Mongo connection string: {}", connectionString);

        var dbFactory = new SimpleMongoClientDbFactory(connectionString);
        mongoTemplate = new MongoTemplate(dbFactory);
    }

    @BeforeEach
    void setUp() {
        storedOrders = insertSampleOrders(mongoTemplate);
        storedOrderCount = storedOrders.size();

        LOG.info("Inserted {} Orders", storedOrders.size());
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(ORDER_COLLECTION);
        LOG.info("Dropped {} collection", ORDER_COLLECTION);
    }

    @Test
    void shouldAggregatePage(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var pagingQuery = new PagingQuery(mongoTemplate).with(KiwiPaging.createPageable(pagingParams));
        var aggregatePage = pagingQuery.aggregatePage(Order.class);

        softly.assertThat(aggregatePage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(aggregatePage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(aggregatePage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(aggregatePage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(aggregatePage.getSize()).isEqualTo(limit);

        // There is no secondary sort, so exact order within a specific customer is non-deterministic
        var expectedCustomerIds = storedOrders.stream()
                .sorted(comparing(Order::getCustomerId))
                .map(Order::getCustomerId)
                .limit(limit)
                .collect(toList());

        var aggregatedCustomerIds = aggregatePage.getContent()
                .stream()
                .map(Order::getCustomerId)
                .collect(toList());

        softly.assertThat(aggregatedCustomerIds).isEqualTo(expectedCustomerIds);
    }

    @Test
    void shouldAggregate_andPaginate(SoftAssertions softly) {
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(0);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var pagingQuery = new PagingQuery(mongoTemplate).with(KiwiPaging.createPageable(pagingParams));
        var aggregatePage0 = pagingQuery.aggregatePage(Order.class);

        assertThat(aggregatePage0.getTotalPages()).isEqualTo(4);

        softly.assertThat(aggregatePage0.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(aggregatePage0.getNumber()).isZero();
        softly.assertThat(aggregatePage0.getNumberOfElements()).isEqualTo(limit);

        pagingParams.setPage(1);
        pagingQuery.with(KiwiPaging.createPageable(pagingParams));
        var aggregatePage1 = pagingQuery.aggregatePage(Order.class);
        softly.assertThat(aggregatePage1.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(aggregatePage1.getNumber()).isOne();
        softly.assertThat(aggregatePage1.getNumberOfElements()).isEqualTo(limit);

        pagingParams.setPage(2);
        pagingQuery.with(KiwiPaging.createPageable(pagingParams));
        var aggregatePage2 = pagingQuery.aggregatePage(Order.class);
        softly.assertThat(aggregatePage2.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(aggregatePage2.getNumber()).isEqualTo(2);
        softly.assertThat(aggregatePage2.getNumberOfElements()).isEqualTo(limit);

        pagingParams.setPage(3);
        pagingQuery.with(KiwiPaging.createPageable(pagingParams));
        var aggregatePage3 = pagingQuery.aggregatePage(Order.class);
        softly.assertThat(aggregatePage3.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(aggregatePage3.getNumber()).isEqualTo(3);
        softly.assertThat(aggregatePage3.getNumberOfElements()).isLessThanOrEqualTo(limit);
    }

    @Test
    void shouldAggregatePage_WithMatchOperation(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);
        pagingParams.setSecondarySort("amount");
        pagingParams.setSecondaryDirection(Sort.Direction.DESC);

        var pagingQuery = new PagingQuery(mongoTemplate).with(KiwiPaging.createPageable(pagingParams));
        var aggregatePage = pagingQuery.aggregatePage(Order.class,
                Aggregation.match(Criteria.where("status").is("A")));

        Predicate<Order> orderFilter = order -> order.getStatus().equals("A");
        var expectedTotalElements = storedOrders.stream()
                .filter(orderFilter)
                .count();

        softly.assertThat(aggregatePage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(aggregatePage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(aggregatePage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(aggregatePage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(aggregatePage.getSize()).isEqualTo(limit);

        var expectedOrders = storedOrders.stream()
                .filter(orderFilter)
                .sorted(comparing(Order::getCustomerId).thenComparing(comparingDouble(Order::getAmount).reversed()))
                .limit(limit)
                .collect(toList());

        softly.assertThat(aggregatePage.getContent()).isEqualTo(expectedOrders);
    }

}
