package org.kiwiproject.hibernate.usertype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.hibernate.usertype.UserTypeTestHelpers.buildHibernateConfiguration;
import static org.kiwiproject.hibernate.usertype.UserTypeTestHelpers.preparedDbExtensionFor;

import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@DisplayName("ArrayUserTypes (Integration)")
class ArrayUserTypesIntegrationTest {

    @RegisterExtension
    static final PreparedDbExtension POSTGRES =
            preparedDbExtensionFor("hibernate/UserTypeTests/array-usertype-migration.xml");

    private static SessionFactory sessionFactory;

    private Session session;
    private Transaction transaction;

    @BeforeAll
    static void beforeAll() {
        var config = buildHibernateConfiguration(POSTGRES.getConnectionInfo(), SampleEntity.class);
        sessionFactory = config.buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
    }

    @AfterEach
    void tearDown() {
        transaction.rollback();
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

    private Object saveAndClearSession(SampleEntity entity) {
        return UserTypeTestHelpers.saveAndClearSession(session, entity);
    }

}
