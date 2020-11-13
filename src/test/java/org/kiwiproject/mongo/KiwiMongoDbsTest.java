package org.kiwiproject.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.base.KiwiStrings.f;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

@DisplayName("KiwiMongoDbs")
class KiwiMongoDbsTest {

    private static final String AUTHORITY = "test1.server:27017,test2.server:27107";
    private static final String DATABASE_NAME = "a-mongo-unit-test-database";
    private static final String TEST_MONGO_URI = f("mongodb://{}/{}?replicaSet=rs0", AUTHORITY, DATABASE_NAME);

    @Nested
    class ExtractDbName {

        @Test
        void shouldExtractFromString() {
            assertThat(KiwiMongoDbs.extractDbName(TEST_MONGO_URI))
                    .isEqualTo(DATABASE_NAME);
        }

        @Test
        void shouldExtractFromURI() {
            assertThat(KiwiMongoDbs.extractDbName(URI.create(TEST_MONGO_URI)))
                    .isEqualTo(DATABASE_NAME);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "/"})
        void shouldThrowIllegalArgumentException_WhenDoesNotContainDatabaseName(String pathComponent) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMongoDbs.extractDbName("mongodb://localhost:27017" + pathComponent))
                    .withMessage("Mongo connection URI does not contain a database name");
        }
    }

    @Nested
    class ExtractHostInformation {

        @ParameterizedTest
        @CsvSource({
                "mongodb://localhost/test_database , mongodb://localhost/",
                "mongodb://mongo-db-1.test:27019/test_database , mongodb://mongo-db-1.test:27019/",
                "'mongodb://test1.server:27017,test2.server:27107/test_database' , 'mongodb://test1.server:27017,test2.server:27107/'",
                "'mongodb://test1.server:27017,test2.server:27107/test_database?replicaSet=rs0' , 'mongodb://test1.server:27017,test2.server:27107/'",
        })
        void shouldExtractFromString(String uri, String expectedHostInfo) {
            assertThat(KiwiMongoDbs.extractHostInformation(uri)).isEqualTo(expectedHostInfo);
        }

        @Test
        void shouldExtractFromURI() {
            assertThat(KiwiMongoDbs.extractHostInformation(URI.create(TEST_MONGO_URI)))
                    .isEqualTo("mongodb://test1.server:27017,test2.server:27107/");
        }
    }
}
