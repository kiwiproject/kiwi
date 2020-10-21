package org.kiwiproject.dropwizard.jdbi3;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Jdbi3Helpers")
class Jdbi3HelpersTest {

    @Nested
    class GetPluginInstance {

        @Test
        void shouldReturnOptionalContainingPlugin_WhenClassIsAvailable() {
            assertThat(Jdbi3Helpers.getPluginInstance(PostgresPlugin.class.getName()))
                    .containsInstanceOf(PostgresPlugin.class);

            assertThat(Jdbi3Helpers.getPluginInstance(H2DatabasePlugin.class.getName()))
                    .containsInstanceOf(H2DatabasePlugin.class);
        }

        @Test
        void shouldReturnEmptyOptional_WhenClassIsNotAvailable() {
            assertThat(Jdbi3Helpers.getPluginInstance("org.jdbi.v3.mysql.MysqlPlugin"))
                    .isEmpty();
        }

        @Test
        void shouldReturnEmptyOptional_WhenErrorInstantiatingPluginClass() {
            assertThat(Jdbi3Helpers.getPluginInstance(MisbehavingDatabasePlugin.class.getName()))
                    .isEmpty();
        }
    }

    public static class MisbehavingDatabasePlugin extends JdbiPlugin.Singleton {
        public MisbehavingDatabasePlugin() {
            throw new IllegalStateException("I cannot be created");
        }
    }
}