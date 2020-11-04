package org.kiwiproject.hibernate.usertype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;

import io.zonky.test.db.postgres.embedded.LiquibasePreparer;
import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.postgresql.Driver;

import java.io.Serializable;

class ArrayUserTypesIntegrationTest {

    @RegisterExtension
    static final PreparedDbExtension POSTGRES = EmbeddedPostgresExtension.preparedDatabase(
            LiquibasePreparer.forClasspathLocation("hibernate/UserTypeTests/array-usertype-migration.xml"));

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeAll
    static void beforeAll() {
        var connectionInfo = POSTGRES.getConnectionInfo();
        var url = f("jdbc:postgresql://localhost:{}/{}", connectionInfo.getPort(), connectionInfo.getDbName());

        var config = new Configuration();
        config.setProperty("hibernate.connection.driver_class", Driver.class.getName());
        config.setProperty("hibernate.connection.url", url);
        config.setProperty("hibernate.connection.username", connectionInfo.getUser());
        config.setProperty("hibernate.connection.password", "");
        config.setProperty("hibernate.dialect", PostgreSQL95Dialect.class.getName());

        config.addAnnotatedClass(SampleEntity.class);

        sessionFactory = config.buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    @AfterAll
    static void afterAll() {
        sessionFactory.close();
    }

    @Nested
    class BigintArrays {

        @Test
        void shouldMapBigintArrays() {
            var entity = new SampleEntity();
            entity.setTextCol("some text");
            entity.setBigintArrayCol(new Long[]{1L, 2L, 3L, 4L, 5L});

            var id = saveAndClearSession(entity);

            var foundEntity = session.get(SampleEntity.class, id);
            assertThat(foundEntity.getBigintArrayCol()).containsExactly(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        void shouldBeNullWhenNotSet() {
            var entity = new SampleEntity();
            entity.setTextCol("some text");

            var id = saveAndClearSession(entity);

            var foundEntity = session.get(SampleEntity.class, id);
            assertThat(foundEntity.getBigintArrayCol()).isNull();
        }
    }

    @Nested
    class TextArrays {

        @Test
        void shouldMapTextArrays() {
            var entity = new SampleEntity();
            entity.setTextCol("some text");
            entity.setTextArrayCol(new String[]{"abc", "def", "hij", "klm"});

            var id = saveAndClearSession(entity);

            var foundEntity = session.get(SampleEntity.class, id);
            assertThat(foundEntity.getTextArrayCol()).containsExactly("abc", "def", "hij", "klm");
        }

        @Test
        void shouldMapVarcharArrays() {
            var entity = new SampleEntity();
            entity.setTextCol("some text");
            entity.setVarcharArrayCol(new String[]{"abc", "def", "hij", "klm"});

            var id = saveAndClearSession(entity);

            var foundEntity = session.get(SampleEntity.class, id);
            assertThat(foundEntity.getVarcharArrayCol()).containsExactly("abc", "def", "hij", "klm");
        }

        @Test
        void shouldBeNullWhenNotSet() {
            var entity = new SampleEntity();
            entity.setTextCol("some text");

            var id = saveAndClearSession(entity);

            var foundEntity = session.get(SampleEntity.class, id);
            assertThat(foundEntity.getTextArrayCol()).isNull();
        }
    }

    private Serializable saveAndClearSession(SampleEntity entity) {
        var id = session.save(entity);
        assertThat(id)
                .describedAs("Entity should have been flushed to database and now have an assigned ID")
                .isNotNull();

        session.clear();

        return id;
    }

}