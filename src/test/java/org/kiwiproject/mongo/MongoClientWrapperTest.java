package org.kiwiproject.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.junit.jupiter.MongoServerExtension;
import org.kiwiproject.spring.util.MongoTestHelpers;

class MongoClientWrapperTest {

    @RegisterExtension
    static final MongoServerExtension MONGO_SERVER_EXTENSION = new MongoServerExtension();

    private MongoClientWrapper clientWrapper;
    private String databaseName;

    @BeforeEach
    void setUp() {
        databaseName = "test_database_" + System.currentTimeMillis();
        var mongoUri = MongoTestHelpers.mongoConnectionString(MONGO_SERVER_EXTENSION.getMongoServer());
        clientWrapper = new MongoClientWrapper(mongoUri, databaseName);
    }

    @AfterEach
    void tearDown() {
        clientWrapper.close();
    }

    @Test
    void shouldHaveMongoClient() {
        var mongoClient = clientWrapper.getMongoClient();
        assertThat(mongoClient).isNotNull();

        // Create collection to ensure the database exists
        clientWrapper.getMongoDatabase().createCollection("test_collection");
        assertThat(mongoClient.listDatabaseNames()).contains(databaseName);
    }

    @Test
    void shouldHaveMongoDatabase() {
        var mongoDatabase = clientWrapper.getMongoDatabase();
        assertThat(mongoDatabase).isNotNull();
        assertThat(mongoDatabase.getName()).isEqualTo(databaseName);
    }

    @Test
    void shouldHaveDB() {
        var db = clientWrapper.getDB();
        assertThat(db).isNotNull();
        assertThat(db.getName()).isEqualTo(databaseName);
    }

    @Test
    void shouldCloseMongoClient() {
        var mongoClient = clientWrapper.getMongoClient();

        mongoClient.close();

        var database = mongoClient.getDatabase(databaseName);

        assertThatIllegalStateException()
                .describedAs("Expecting IllegalStateException after closing the client")
                .isThrownBy(() -> database.createCollection("another_collection"));
    }
}
