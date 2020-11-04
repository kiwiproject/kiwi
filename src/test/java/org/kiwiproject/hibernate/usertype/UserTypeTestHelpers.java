package org.kiwiproject.hibernate.usertype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;

import io.zonky.test.db.postgres.embedded.ConnectionInfo;
import io.zonky.test.db.postgres.embedded.LiquibasePreparer;
import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.postgresql.Driver;

import java.io.Serializable;
import java.util.Arrays;

@UtilityClass
class UserTypeTestHelpers {

    static PreparedDbExtension preparedDbExtensionFor(String classpathLocation) {
        var liquibasePreparer = LiquibasePreparer.forClasspathLocation(classpathLocation);
        return EmbeddedPostgresExtension.preparedDatabase(liquibasePreparer);
    }

    static Configuration buildHibernateConfiguration(ConnectionInfo connectionInfo, Class<?>... annotatedClasses) {
        var url = f("jdbc:postgresql://localhost:{}/{}", connectionInfo.getPort(), connectionInfo.getDbName());

        var config = new Configuration();
        config.setProperty("hibernate.connection.driver_class", Driver.class.getName());
        config.setProperty("hibernate.connection.url", url);
        config.setProperty("hibernate.connection.username", connectionInfo.getUser());
        config.setProperty("hibernate.connection.password", "");
        config.setProperty("hibernate.dialect", PostgreSQL95Dialect.class.getName());

        Arrays.stream(annotatedClasses).forEach(config::addAnnotatedClass);

        return config;
    }

    static Serializable saveAndClearSession(Session session, Object entity) {
        var id = session.save(entity);
        assertThat(id)
                .describedAs("Entity should have been flushed to database and now have an assigned ID")
                .isNotNull();

        session.clear();

        return id;
    }
}
