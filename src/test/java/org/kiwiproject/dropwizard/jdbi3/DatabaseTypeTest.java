package org.kiwiproject.dropwizard.jdbi3;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DatabaseType")
class DatabaseTypeTest {

    @Nested
    class DatabaseTypeEnum {

        @Test
        void shouldGetH2PluginInstance() {
            var pluginOptional = DatabaseType.pluginFromDatabaseUrl("jdbc:h2:/tmp/h2-test-db-12345/testdb");
            assertThat(pluginOptional).containsInstanceOf(H2DatabasePlugin.class);
        }

        @Test
        void shouldGetPostgresPluginInstance() {
            var pluginOptional = DatabaseType.pluginFromDatabaseUrl("jdbc:postgresql://localhost:5432/testdb");
            assertThat(pluginOptional).containsInstanceOf(PostgresPlugin.class);
        }

        @Test
        void shouldReturnEmptyOptionalForUnsupportedDatabase() {
            assertThat(DatabaseType.pluginFromDatabaseUrl("jdbc:mysql://localhost:33060/sakila")).isEmpty();
        }
    }
}