package org.kiwiproject.spring.data;

import static org.kiwiproject.spring.util.MongoTestContainerHelpers.newMongoDBContainer;
import static org.kiwiproject.spring.util.MongoTestContainerHelpers.newMongoTemplate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisplayName("PagingQuery (Mongo Latest)")
@Testcontainers(disabledWithoutDocker = true)
class PagingQueryMongoLatestIntegrationTest extends AbstractPagingQueryIntegrationTest {

    @Container
    static final MongoDBContainer MONGODB = newMongoDBContainer("mongo:latest");

    @BeforeAll
    static void beforeAll() {
        mongoTemplate = newMongoTemplate(MONGODB);
    }
}
