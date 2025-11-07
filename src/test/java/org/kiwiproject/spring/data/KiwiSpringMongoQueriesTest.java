package org.kiwiproject.spring.data;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.reverseOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.spring.data.KiwiSpringMongoQueries.addDateBounds;
import static org.kiwiproject.spring.data.KiwiSpringMongoQueries.addInCriteriaFromCsv;
import static org.kiwiproject.spring.data.KiwiSpringMongoQueries.addMultiplePartialOrEqualMatchCriteria;
import static org.kiwiproject.spring.data.KiwiSpringMongoQueries.addPartialOrEqualMatchCriteria;
import static org.kiwiproject.spring.data.OrderTestData.insertSampleOrders;
import static org.kiwiproject.spring.util.MongoTestContainerHelpers.newMongoDBContainer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.base.KiwiDoubles;
import org.kiwiproject.search.KiwiSearching;
import org.kiwiproject.spring.data.KiwiSpringMongoQueries.PartialMatchType;
import org.kiwiproject.time.KiwiInstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;

@DisplayName("KiwiSpringMongoQueries")
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SoftAssertionsExtension.class)
@Slf4j
class KiwiSpringMongoQueriesTest {

    @Container
    static final MongoDBContainer MONGODB = newMongoDBContainer();

    private static MongoTemplate mongoTemplate;

    private List<Order> storedOrders;
    private int storedOrderCount;

    @BeforeAll
    static void beforeAll() {
        var connectionString = MONGODB.getConnectionString() + "/test";
        var mongoClientDbFactory = new SimpleMongoClientDatabaseFactory(connectionString);
        mongoTemplate = new MongoTemplate(mongoClientDbFactory);
    }

    @BeforeEach
    void setUp() {
        storedOrders = insertSampleOrders(mongoTemplate);
        storedOrderCount = storedOrders.size();

        LOG.info("Inserted {} Orders", storedOrders.size());
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(OrderTestData.ORDER_COLLECTION);
        LOG.info("Dropped {} collection", OrderTestData.ORDER_COLLECTION);
    }

    @Test
    void shouldPaginate_WithPageAndLimit(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class);

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.unsorted());
        softly.assertThat(orderPage.getContent()).isEqualTo(storedOrders.subList(0, limit));
    }

    @Test
    void shouldPaginate_WithAscendingSort(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class);

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "customerId"));

        var expectedCustomerIds = storedOrders.stream()
                .map(Order::getCustomerId)
                .sorted()
                .limit(limit)
                .toList();

        softly.assertThat(customerIds(orderPage)).isEqualTo(expectedCustomerIds);
    }

    @Test
    void shouldPaginate_WithPrimaryAndSecondarySort(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);
        pagingParams.setSecondarySort("amount");
        pagingParams.setSecondaryDirection(Sort.Direction.DESC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class);

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(
                Sort.by(new Sort.Order(Sort.Direction.ASC, "customerId"), new Sort.Order(Sort.Direction.DESC, "amount")));

        var expectedOrders = storedOrders.stream()
                .sorted(comparing(Order::getCustomerId).thenComparing(comparingDouble(Order::getAmount).reversed()))
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @Test
    void shouldPaginate_WithDescendingSort(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 3;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.DESC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class);

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "customerId"));

        var expectedCustomerIds = storedOrders.stream()
                .map(Order::getCustomerId)
                .sorted(reverseOrder())
                .limit(limit)
                .toList();

        softly.assertThat(customerIds(orderPage)).isEqualTo(expectedCustomerIds);
    }

    @Test
    void shouldPaginate_GettingSecondPage(SoftAssertions softly) {
        var pageNumber = 1;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);
        pagingParams.setSecondarySort("amount");
        pagingParams.setSecondaryDirection(Sort.Direction.DESC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class);

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(
                Sort.by(new Sort.Order(Sort.Direction.ASC, "customerId"),
                        new Sort.Order(Sort.Direction.DESC, "amount"))
        );

        var expectedOrders = storedOrders.stream()
                .sorted(comparing(Order::getCustomerId)
                        .thenComparing(comparing(Order::getAmount).reversed()))
                .skip(pageNumber * limit)
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @ParameterizedTest
    @CsvSource({
            " 0 , ",
            " 0, 0 ",
            " 0, -1 ",
    })
    void shouldPaginate_AndSetLimitToOne_WhenInvalidLimitIsGiven(Integer pageNumber, Integer limit, SoftAssertions softly) {
        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class);

        var expectedPageSize = 1;
        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, expectedPageSize));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(expectedPageSize);
        softly.assertThat(orderPage.getSize())
                .describedAs("page size should have been set to %d", expectedPageSize)
                .isEqualTo(expectedPageSize);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.unsorted());
        softly.assertThat(orderPage.getContent()).isEqualTo(List.of(first(storedOrders)));
    }

    @ParameterizedTest
    @CsvSource({
            "  , 5 ",
            " -1 , 5 ",
            " -5 , 5 ",
    })
    void shouldPaginate_AndSetPageToZero_WhenInvalidPageIsGiven(Integer pageNumber, Integer limit, SoftAssertions softly) {
        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class);

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(orderPage.getNumber())
                .describedAs("page number should have been set to zero")
                .isEqualTo(0);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.unsorted());
        softly.assertThat(orderPage.getContent()).isEqualTo(storedOrders.subList(0, limit));
    }

    @Test
    void shouldAllowAddingCriteria(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("amount");
        pagingParams.setPrimaryDirection(Sort.Direction.DESC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) -> pagingQuery.addCriteria(Criteria.where("customerId").is("A123")));

        var expectedOrders = storedOrders.stream()
                .filter(order -> order.getCustomerId().equals("A123"))
                .sorted(comparing(Order::getAmount).reversed())
                .limit(limit)
                .toList();

        var expectedTotalElements = expectedOrders.size();

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "amount"));
        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @Test
    void shouldAddDateBounds(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);
        pagingParams.setSecondarySort("dateReceived");
        pagingParams.setSecondaryDirection(Sort.Direction.DESC);

        var now = Instant.now();
        var startDate = KiwiInstants.minusDays(now, 30);
        var endDate = KiwiInstants.minusDays(now, 20);
        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addDateBounds(pagingQuery, "dateReceived", startDate.toEpochMilli(), endDate.toEpochMilli()));

        softly.assertThat(orderPage.getTotalElements()).isOne();
        softly.assertThat(orderPage.getTotalPages()).isOne();
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isOne();
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(
                Sort.by(new Sort.Order(Sort.Direction.ASC, "customerId"), new Sort.Order(Sort.Direction.DESC, "dateReceived")));

        var expectedOrders = storedOrders.stream()
                .filter(order ->
                        order.getDateReceived().toInstant().isAfter(startDate) &&
                                order.getDateReceived().toInstant().isBefore(endDate))
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @Test
    void shouldIgnoreAddDateBounds_WhenGivenNullStartAndEnd(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addDateBounds(pagingQuery, "dateReceived", null, null));

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(storedOrderCount, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.unsorted());
        softly.assertThat(orderPage.getContent()).isEqualTo(storedOrders.subList(0, limit));
    }

    @Test
    void shouldIgnoreNullStartOrEnd_WhenAddingDateBounds(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 25;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);

        var now = Instant.now();

        // only lower bound
        var startMillis = KiwiInstants.minusDays(now, 50).toEpochMilli();
        var lowerBoundOrderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addDateBounds(pagingQuery, "dateReceived", startMillis, null));

        softly.assertThat(lowerBoundOrderPage.getTotalElements()).isEqualTo(storedOrderCount);

        // only upper bound
        var endMillis = KiwiInstants.plusDays(now, 30).toEpochMilli();
        var upperBoundOrderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addDateBounds(pagingQuery, "dateReceived", null, endMillis));

        softly.assertThat(upperBoundOrderPage.getTotalElements()).isEqualTo(storedOrderCount);
    }

    @ParameterizedTest
    @CsvSource({
            " true , PARTIAL_MATCH ",
            " TRUE , PARTIAL_MATCH ",
            " True , PARTIAL_MATCH ",
            " t , PARTIAL_MATCH ",
            " T , PARTIAL_MATCH ",
            " yes , PARTIAL_MATCH ",
            " YES , PARTIAL_MATCH ",
            " Yes , PARTIAL_MATCH ",
            " y , PARTIAL_MATCH ",
            " Y , PARTIAL_MATCH ",
            "  , EQUAL_MATCH ",
            " false , EQUAL_MATCH ",
            " FALSE , EQUAL_MATCH ",
            " False , EQUAL_MATCH ",
            " f , EQUAL_MATCH ",
            " no , EQUAL_MATCH ",
            " NO , EQUAL_MATCH ",
            " No , EQUAL_MATCH ",
            " F , EQUAL_MATCH ",
            " '' , EQUAL_MATCH ",
            " ' ' , EQUAL_MATCH ",
    })
    void shouldConstructPartialMatchType_FromBooleanStrings(String value, PartialMatchType expectedMatchType) {
        assertThat(PartialMatchType.fromBooleanString(value)).isEqualTo(expectedMatchType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, PARTIAL_MATCH",
            "false, EQUAL_MATCH"
    })
    void shouldConstructPartialMatchType_FromBooleans(boolean value, PartialMatchType expectedMatchType) {
        assertThat(PartialMatchType.from(value)).isEqualTo(expectedMatchType);
    }

    @Test
    void shouldAddPartialOrEqualMatchCriteria_WithPartialMatch(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        // partial match is case-insensitive
                        addPartialOrEqualMatchCriteria(pagingQuery, "a12", "customerId", PartialMatchType.PARTIAL_MATCH));

        Predicate<Order> customerFilter = order -> order.getCustomerId().contains("A12");
        var expectedTotalElements = storedOrders.stream()
                .filter(customerFilter)
                .count();

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "customerId"));

        var expectedCustomerIds = storedOrders.stream()
                .filter(customerFilter)
                .map(Order::getCustomerId)
                .sorted()
                .limit(limit)
                .toList();

        softly.assertThat(customerIds(orderPage)).isEqualTo(expectedCustomerIds);
    }

    private static List<String> customerIds(Page<Order> orderPage) {
        return orderPage.getContent()
                .stream()
                .map(Order::getCustomerId)
                .toList();
    }

    @Test
    void shouldAddPartialOrEqualMatchCriteria_WithEqualMatch(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        // equal match is case-sensitive
                        addPartialOrEqualMatchCriteria(pagingQuery, "C789", "customerId", PartialMatchType.EQUAL_MATCH));

        Predicate<Order> customerFilter = order -> order.getCustomerId().equals("C789");
        var expectedTotalElements = storedOrders.stream()
                .filter(customerFilter)
                .count();

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "customerId"));

        var expectedOrders = storedOrders.stream()
                .filter(customerFilter)
                .sorted(comparing(Order::getCustomerId))
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldIgnoreAddPartialOrEqualMatchCriteria_WhenMatchStringIsBlank(String matchString) {
        var pagingParams = new PagingRequest();

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addPartialOrEqualMatchCriteria(pagingQuery, matchString, "customerId", PartialMatchType.EQUAL_MATCH));

        assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
    }

    @Test
    void shouldAddMultiplePartialOrEqualMatchCriteria_WithPartialMatch(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 25;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        // partial match is case-insensitive
                        KiwiSpringMongoQueries.addMultiplePartialOrEqualMatchCriteria(
                                pagingQuery, List.of("a12", "b45"), "customerId", PartialMatchType.PARTIAL_MATCH));

        // Note the predicate here uses capital A and B
        Predicate<Order> customerFilter = order -> Strings.CS.containsAny(order.getCustomerId(), "A12", "B45");
        var expectedTotalElements = storedOrders.stream()
                .filter(customerFilter)
                .count();

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "customerId"));

        var expectedOrders = storedOrders.stream()
                .filter(customerFilter)
                .sorted(comparing(Order::getCustomerId))
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @Test
    void shouldAddMultiplePartialOrEqualMatchCriteria_WithEqualMatch(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 25;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        // equal match is case-sensitive
                        KiwiSpringMongoQueries.addMultiplePartialOrEqualMatchCriteria(
                                pagingQuery, List.of("A123", "A129", "D012"), "customerId", PartialMatchType.EQUAL_MATCH));

        Predicate<Order> customerFilter = order -> Strings.CS.containsAny(order.getCustomerId(), "A123", "A129", "D012");
        var expectedTotalElements = storedOrders.stream()
                .filter(customerFilter)
                .count();

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "customerId"));

        var expectedOrders = storedOrders.stream()
                .filter(customerFilter)
                .sorted(comparing(Order::getCustomerId))
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldIgnoreAddMultiplePartialOrEqualMatchCriteria_WhenMatchStringIsBlank(List<String> matchStrings) {
        var pagingParams = new PagingRequest();

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addMultiplePartialOrEqualMatchCriteria(pagingQuery, matchStrings, "customerId", PartialMatchType.EQUAL_MATCH));

        assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
    }

    @Test
    void shouldAddInCriteriaFromCsv(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 25;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        // A124 does not exist
                        addInCriteriaFromCsv(pagingQuery, "A124, A129, B456", "customerId"));

        Predicate<Order> customerFilter = order -> Strings.CS.containsAny(order.getCustomerId(), "A129", "B456");
        var expectedTotalElements = storedOrders.stream()
                .filter(customerFilter)
                .count();

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "customerId"));

        var expectedOrders = storedOrders.stream()
                .filter(customerFilter)
                .sorted(comparing(Order::getCustomerId))
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @Test
    void shouldAddInCriteriaFromCsv_WithTypeConverter(SoftAssertions softly) {
        var pageNumber = 0;
        var limit = 5;

        var pagingParams = new PagingRequest();
        pagingParams.setPage(pageNumber);
        pagingParams.setLimit(limit);
        pagingParams.setPrimarySort("customerId");
        pagingParams.setPrimaryDirection(Sort.Direction.ASC);
        pagingParams.setSecondarySort("amount");
        pagingParams.setSecondaryDirection(Sort.Direction.DESC);

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addInCriteriaFromCsv(pagingQuery, "50, 100, 250", "amount", Double::valueOf));

        Predicate<Order> amountFilter = order ->
                DoubleStream.of(50.0, 100.0, 250.0).anyMatch(value -> KiwiDoubles.areEqual(order.getAmount(), value));
        var expectedTotalElements = storedOrders.stream()
                .filter(amountFilter)
                .count();

        softly.assertThat(orderPage.getTotalElements()).isEqualTo(expectedTotalElements);
        softly.assertThat(orderPage.getTotalPages()).isEqualTo(KiwiSearching.numberOfPages(expectedTotalElements, limit));
        softly.assertThat(orderPage.getNumber()).isEqualTo(pageNumber);
        softly.assertThat(orderPage.getNumberOfElements()).isEqualTo(limit);
        softly.assertThat(orderPage.getSize()).isEqualTo(limit);
        softly.assertThat(orderPage.getSort()).isEqualTo(
                Sort.by(new Sort.Order(Sort.Direction.ASC, "customerId"), new Sort.Order(Sort.Direction.DESC, "amount")));

        var expectedOrders = storedOrders.stream()
                .filter(amountFilter)
                .sorted(comparing(Order::getCustomerId).thenComparing(comparingDouble(Order::getAmount).reversed()))
                .limit(limit)
                .toList();

        softly.assertThat(orderPage.getContent()).isEqualTo(expectedOrders);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldIgnoreAddInCriteriaFromCsv_WhenMatchStringIsBlank(String csv) {
        var pagingParams = new PagingRequest();

        var orderPage = KiwiSpringMongoQueries.paginate(mongoTemplate, pagingParams, Order.class,
                (pagingQuery, pagingRequest) ->
                        addInCriteriaFromCsv(pagingQuery, csv, "customerId"));

        assertThat(orderPage.getTotalElements()).isEqualTo(storedOrderCount);
    }
}
