package org.kiwiproject.spring.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisplayName("PagingQuery (Mongo 6)")
@Testcontainers(disabledWithoutDocker = true)
class PagingQueryMongo6IntegrationTest extends AbstractPagingQueryIntegrationTest {

    @Container
    static final MongoDBContainer MONGODB = newMongoDBContainer("mongo:6");

    @BeforeAll
    static void beforeAll() {
        mongoTemplate = newMongoTemplate(MONGODB);
    }
}
