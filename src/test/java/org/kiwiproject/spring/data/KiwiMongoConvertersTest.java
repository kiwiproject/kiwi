package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.spring.util.MongoTestHelpers.newMongoTemplate;

import org.bson.BsonUndefined;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.junit.jupiter.MongoServerExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@DisplayName("KiwiMongoConverters")
class KiwiMongoConvertersTest {

    @RegisterExtension
    static final MongoServerExtension MONGO_SERVER_EXTENSION = new MongoServerExtension();

    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate = newMongoTemplate(MONGO_SERVER_EXTENSION.getMongoServer());
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
