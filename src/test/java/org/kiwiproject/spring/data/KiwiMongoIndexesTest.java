package org.kiwiproject.spring.data;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.spring.util.MongoTestHelpers.newMongoTemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.junit.jupiter.MongoServerExtension;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

@DisplayName("KiwiMongoIndexes")
class KiwiMongoIndexesTest {

    @RegisterExtension
    static final MongoServerExtension MONGO_SERVER_EXTENSION = new MongoServerExtension();

    private static final String PEOPLE_COLLECTION_NAME = "people";

    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate = newMongoTemplate(MONGO_SERVER_EXTENSION.getMongoServer());

        mongoTemplate.createCollection(PEOPLE_COLLECTION_NAME);
    }

    @Nested
    class EnsureIndices {

        private String[] properties;

        @BeforeEach
        void setUp() {
            properties = new String[]{"lastName", "zipCode"};
        }

        @Test
        void shouldAcceptDocumentClass() {
            KiwiMongoIndexes.ensureIndices(mongoTemplate, Person.class, Sort.Direction.ASC, properties);

            assertIndices(Sort.Direction.ASC);
        }

        @Test
        void shouldAcceptCollectionName() {
            KiwiMongoIndexes.ensureIndices(mongoTemplate, PEOPLE_COLLECTION_NAME, Sort.Direction.DESC, properties);

            assertIndices(Sort.Direction.DESC);
        }

        private void assertIndices(Sort.Direction direction) {
            var indexInfos = mongoTemplate.indexOps(PEOPLE_COLLECTION_NAME).getIndexInfo();

            var indexFields = indexInfos.stream()
                    .map(IndexInfo::getIndexFields)
                    .flatMap(Collection::stream)
                    .collect(toList());

            assertThat(indexFields)
                    .extracting("key", "direction")
                    .contains(
                            tuple("lastName", direction),
                            tuple("zipCode", direction)
                    );
        }
    }

    @Document(collection = PEOPLE_COLLECTION_NAME)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Person {

        @Id
        private String id;

        private String firstName;
        private String lastName;

        private String street1;
        private String street2;
        private String city;
        private String state;
        private String zipCode;
    }

}
