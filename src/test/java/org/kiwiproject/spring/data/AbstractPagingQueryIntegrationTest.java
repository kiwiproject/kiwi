package org.kiwiproject.spring.data;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.spring.data.OrderTestData.ORDER_COLLECTION;
import static org.kiwiproject.spring.data.OrderTestData.insertSampleOrders;

import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.search.KiwiSearching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.function.Predicate;

/**
 * Base test class for PagingQuery tests. This permits subclasses to inherit all the actual tests; the only thing
 * they need to do is set up a {@link MongoDBContainer} and set the {@link MongoTemplate} in a {@code BeforeAll}
 * method. This class provides convenience methods for these requirements.
 * <p>
 * Background: the code in {@link PagingQuery#aggregatePage(Class, AggregationOperation...)} blows up with a NPE
 * whenever executing it using the in-memory {@link de.bwaldvogel.mongo.MongoServer}, which is the main reason for
 * using Testcontainers here. I have been unable to figure out exactly why it blows up, but it doesn't seem worth
 * pursuing for a test when Testcontainers is a perfectly good (and probably preferred) alternative.
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SoftAssertionsExtension.class)
@Slf4j
abstract class AbstractPagingQueryIntegrationTest {

    static int STANDARD_MONGODB_PORT = 27_017;

    /**
     * Subclasses must set this in a {@code BeforeAll}.
     */
    static MongoTemplate mongoTemplate;

    List<Order> storedOrders;
    int storedOrderCount;

    /**
     * Subclasses should use this to create the test container, supplying the Docker image name for a specific
     * version of Mongo.
     */
    @SuppressWarnings("resource")  // because Testcontainers closs it for us
    static MongoDBContainer newMongoDBContainer(String dockerImageName) {
        LOG.info("Create MongoDBcontainer for Docker image name: {}", dockerImageName);
        return new MongoDBContainer(DockerImageName.parse(dockerImageName)).waitingFor(new HostPortWaitStrategy());
    }

    /**
     * Subclassses should use this to create and set the static MongoTemplate field.
     */
    static MongoTemplate newMongoTemplate(MongoDBContainer container) {
        var mongoClient = MongoClients.create(connectionStringFor(container));
        return new MongoTemplate(mongoClient, "test");
    }

    private static String connectionStringFor(MongoDBContainer container) {
        var host = container.getHost();
        var port = container.getMappedPort(STANDARD_MONGODB_PORT);
        var connectionString = f("mongodb://{}:{}", host, port);
        LOG.info("Mongo connection string: {}", connectionString);
        return connectionString;
    }

    @BeforeEach
    void setUp() {
        storedOrders = insertSampleOrders(mongoTemplate);
        storedOrderCount = storedOrders.size();
        LOG.info("Inserted {} Orders into {} collection", storedOrders.size(), ORDER_COLLECTION);
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
