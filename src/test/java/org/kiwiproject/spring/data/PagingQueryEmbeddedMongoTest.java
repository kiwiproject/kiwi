package org.kiwiproject.spring.data;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.spring.data.OrderTestData.ORDER_COLLECTION;
import static org.kiwiproject.spring.data.OrderTestData.insertSampleOrders;

import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.net.LocalPortChecker;
import org.kiwiproject.search.KiwiSearching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.function.Predicate;

/**
 * Uses Flapdoodle embedded Mongo which can be found on GitHub
 * <a href="https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo">here</a>.
 * <p>
 * This test is currently only testing against Mongo 4 using {@link Version.Main#V4_0}.
 * <p>
 * Note that we also have {@link PagingQueryRealMongoTest} which executes against a real Mongo instance. It
 * currently requires some manual setup, but allows you to execute the same tests against multiple Mongo
 * versions, e.g. in Docker containers.
 */
@DisplayName("PagingQuery (embedded Mongo)")
@ExtendWith(SoftAssertionsExtension.class)
@Slf4j
class PagingQueryEmbeddedMongoTest {

    // Currently only testing against Mongo 4.0
    private static final Version.Main MONGODB_VERSION = Version.Main.V4_0;

    private static MongodExecutable mongodExecutable;
    private static MongoTemplate mongoTemplate;

    private List<Order> storedOrders;
    private int storedOrderCount;

    @BeforeAll
    static void beforeAll() throws Exception {
        var ip = "localhost";

        // Attempt to get the default Mongo port (27017), or get first available port above it
        var port = new LocalPortChecker().findFirstOpenPortAbove(27016).orElseThrow();
        LOG.info("Using port {}", port);

        var mongoConfig = MongodConfig.builder()
                .version(MONGODB_VERSION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();

        var starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongoConfig);
        mongodExecutable.start();

        var connectionString = f("mongodb://{}:{}", ip, port);
        var mongoClient = MongoClients.create(connectionString);
        mongoTemplate = new MongoTemplate(mongoClient, "test");
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

    @AfterAll
    static void afterAll() {
        mongodExecutable.stop();
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
