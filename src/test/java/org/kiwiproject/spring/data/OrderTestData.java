package org.kiwiproject.spring.data;

import static java.util.stream.Collectors.toList;

import lombok.AllArgsConstructor;
import org.kiwiproject.time.KiwiInstants;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Utils for Spring Data Mongo DB tests.
 */
@AllArgsConstructor
class OrderTestData {

    /**
     * Name of the order collection in the test Mongo database.
     */
    static final String ORDER_COLLECTION = "order";

    /**
     * NOTE: Multiple tests uses this data, so if you want to alter it in any way, there will be a bunch of tests
     * to fix...
     */
    public static List<Order> insertSampleOrders(MongoTemplate mongoTemplate) {
        var now = Instant.now();
        var orders = List.of(
                new Order("A123", 500.0, "A", Date.from(KiwiInstants.minusDays(now, 15))),
                new Order("A123", 250.0, "A", Date.from(KiwiInstants.minusDays(now, 12))),
                new Order("B456", 200.0, "A", Date.from(KiwiInstants.minusDays(now, 25))),
                new Order("A123", 300.0, "D", Date.from(KiwiInstants.minusDays(now, 15))),
                new Order("C789", 100.0, "D", Date.from(KiwiInstants.minusDays(now, 8))),
                new Order("B456", 50.0, "A", Date.from(KiwiInstants.minusDays(now, 10))),
                new Order("A129", 80.0, "A", Date.from(KiwiInstants.minusDays(now, 9))),
                new Order("C789", 75.0, "A", Date.from(KiwiInstants.minusDays(now, 7))),
                new Order("C789", 150.0, "D", Date.from(KiwiInstants.minusDays(now, 5))),
                new Order("B451", 40.0, "A", Date.from(KiwiInstants.minusDays(now, 8))),
                new Order("A123", 90.0, "A", Date.from(KiwiInstants.minusDays(now, 4))),
                new Order("D012", 250.0, "A", Date.from(KiwiInstants.minusDays(now, 13))),
                new Order("B482", 70.0, "A", Date.from(KiwiInstants.minusDays(now, 18))),
                new Order("A129", 160.0, "A", Date.from(KiwiInstants.minusDays(now, 1))),
                new Order("B456", 250.0, "A", Date.from(KiwiInstants.minusDays(now, 7))),
                new Order("D012", 100.0, "D", Date.from(KiwiInstants.minusDays(now, 3)))
        );

        return orders.stream()
                .map(mongoTemplate::insert)
                .collect(toList());
    }
}
