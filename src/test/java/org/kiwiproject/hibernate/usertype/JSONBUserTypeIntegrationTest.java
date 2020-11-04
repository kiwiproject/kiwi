package org.kiwiproject.hibernate.usertype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.hibernate.usertype.UserTypeTestHelpers.buildHibernateConfiguration;
import static org.kiwiproject.hibernate.usertype.UserTypeTestHelpers.preparedDbExtensionFor;

import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.json.JsonHelper;

import java.io.Serializable;

@DisplayName("JSONBUserType (Integration)")
class JSONBUserTypeIntegrationTest {

    @RegisterExtension
    static final PreparedDbExtension POSTGRES =
            preparedDbExtensionFor("hibernate/UserTypeTests/jsonb-usertype-migration.xml");

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeAll
    static void beforeAll() {
        var config = buildHibernateConfiguration(POSTGRES.getConnectionInfo(), SampleJsonbEntity.class);
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

    @Test
    void shouldMapJsonbColumns() {
        var entity = new SampleJsonbEntity();
        entity.setTextCol("some text");
        var json = Fixtures.fixture("hibernate/UserTypeTests/sample.json");
        entity.setJsonbCol(json);

        var id = saveAndClearSession(entity);

        var foundEntity = session.get(SampleJsonbEntity.class, id);
        var foundJson = foundEntity.getJsonbCol();
        assertThat(foundJson).isNotBlank();
        assertThat(JsonHelper.newDropwizardJsonHelper().jsonEquals(foundJson, json))
                .describedAs("JSON should be equal (ignoring formatting differences)")
                .isTrue();
    }

    @Test
    void shouldPermitNullValues() {
        var entity = new SampleJsonbEntity();
        entity.setTextCol("some text");

        var id = saveAndClearSession(entity);

        var foundEntity = session.get(SampleJsonbEntity.class, id);
        assertThat(foundEntity.getJsonbCol()).isNull();
    }

    private Serializable saveAndClearSession(SampleJsonbEntity entity) {
        return UserTypeTestHelpers.saveAndClearSession(session, entity);
    }
}
