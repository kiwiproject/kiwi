package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.spring.util.MongoTestContainerHelpers.newMongoDBContainer;
import static org.kiwiproject.spring.util.MongoTestContainerHelpers.newMongoTemplate;

import org.bson.BsonUndefined;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisplayName("KiwiMongoConverters")
@Testcontainers(disabledWithoutDocker = true)
class KiwiMongoConvertersTest {

    @Container
    static final MongoDBContainer MONGODB = newMongoDBContainer();

    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate = newMongoTemplate(MONGODB);
    }

    @Nested
    class AddCustomConverters {

        @Test
        void shouldAddConverters() {
            var converter = KiwiMongoConverters.newBsonUndefinedToNullObjectConverter();
            var mongoTemplate1 = KiwiMongoConverters.addCustomConverters(mongoTemplate, converter);
            assertThat(mongoTemplate1).isSameAs(mongoTemplate);

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
