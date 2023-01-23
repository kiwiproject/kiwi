package org.kiwiproject.spring.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisplayName("PagingQuery (Mongo 4)")
@Testcontainers(disabledWithoutDocker = true)
class PagingQueryMongo4IntegrationTest extends AbstractPagingQueryIntegrationTest {

    @Container
    static MongoDBContainer MONGODB = newMongoDBContainer("mongo:4");

    @BeforeAll
    static void beforeAll() throws Exception {
        mongoTemplate = newMongoTemplate(MONGODB);
    }
}
