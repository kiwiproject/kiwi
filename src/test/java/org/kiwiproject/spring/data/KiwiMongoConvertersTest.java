package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.spring.util.MongoTestHelpers.newMongoTemplate;
import static org.kiwiproject.spring.util.MongoTestHelpers.startInMemoryMongoServer;

import de.bwaldvogel.mongo.MongoServer;
import org.bson.BsonUndefined;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

@DisplayName("KiwiMongoConverters")
class KiwiMongoConvertersTest {

    private static MongoServer mongoServer;

    private MongoTemplate mongoTemplate;

    @BeforeAll
    static void beforeAll() {
        mongoServer = startInMemoryMongoServer();
    }

    @AfterAll
    static void afterAll() {
        mongoServer.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        mongoTemplate = newMongoTemplate(mongoServer);
    }

    @Nested
    class AddCustomConverters {

        @Test
        void shouldAddConverters() {
            var converter = KiwiMongoConverters.newBsonUndefinedToNullObjectConverter();
            KiwiMongoConverters.addCustomConverters(mongoTemplate, converter);

            var bsonUndefined = new BsonUndefined();
            var result = mongoTemplate.getConverter().getConversionService().convert(bsonUndefined, String.class);
            assertThat(result).isNull();
        }
    }

    @Nested
    class BsonUndefinedToNullStringConverter {

        @Test
        void shouldConvertToNull() {
            var converter = KiwiMongoConverters.newBsonUndefinedToNullObjectConverter();
            assertThat(converter.convert(new BsonUndefined())).isNull();
        }
    }
}
