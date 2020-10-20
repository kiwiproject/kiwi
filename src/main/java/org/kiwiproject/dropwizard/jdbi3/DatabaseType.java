package org.kiwiproject.dropwizard.jdbi3;

import lombok.Getter;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.core.spi.JdbiPlugin;

import java.util.Optional;

/**
 * This enum allows you to instantiate a {@link JdbiPlugin} for certain databases based on the JDBC database URL.
 * <p>
 * Currently supports only Postgres and H2.
 */
enum DatabaseType {

    H2(H2DatabasePlugin.class.getName()),

    POSTGRES("org.jdbi.v3.postgres.PostgresPlugin");

    @Getter
    private final String pluginClassName;

    DatabaseType(String pluginClassName) {
        this.pluginClassName = pluginClassName;
    }

    /**
     * Given a JDBC database URL, attempt to find and instantiate a plugin.
     * <p>
     * Currently supports only H2 and Postgres.
     *
     * @param databaseUrl the JDBC database URL
     * @return an Optional with a plugin instance or an empty Optional
     */
    static Optional<JdbiPlugin> pluginFromDatabaseUrl(String databaseUrl) {
        return databaseTypeFromDatabaseUrl(databaseUrl)
                .flatMap(databaseType -> Jdbi3Helpers.getPluginInstance(databaseType.getPluginClassName()));
    }

    /**
     * Determine the database type from the given JDBC database URL.
     * <p>
     * Currently supports only H2 and Postgres.
     *
     * @param databaseUrl the JDBC database URL
     * @return an Optional containing the database type if found, otherwise an empty Optional
     */
    static Optional<DatabaseType> databaseTypeFromDatabaseUrl(String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:postgresql:")) {
            return Optional.of(POSTGRES);
        } else if (databaseUrl.startsWith("jdbc:h2:")) {
            return Optional.of(H2);
        }

        return Optional.empty();
    }
}
