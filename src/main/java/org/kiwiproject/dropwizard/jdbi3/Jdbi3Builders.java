package org.kiwiproject.dropwizard.jdbi3;

import static java.util.function.Predicate.not;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.Arrays;

/**
 * Utilities for building Dropwizard-managed {@link Jdbi} version 3 instances.
 */
@Slf4j
@UtilityClass
public class Jdbi3Builders {

    public static final String DEFAULT_HEALTH_CHECK_NAME = "database";

    private static final String NULL_ENVIRONMENT_MESSAGE = "Environment cannot be null";
    private static final String NULL_DATA_SOURCE_FACTORY_NAME = "PooledDataSourceFactory cannot be null";
    private static final String NULL_DATA_SOURCE_MESSAGE = "ManagedDataSource cannot be null";

    /**
     * Build a Dropwizard-managed {@link Jdbi} instance with metrics and health check given an {@link Environment} and
     * {@link PooledDataSourceFactory}. This will also register {@link SqlObjectPlugin} with JDBI before returning it.
     *
     * @param environment       the {@link Environment}
     * @param dataSourceFactory the {@link PooledDataSourceFactory}
     * @param jdbiPlugins       any other {@link JdbiPlugin} objects desired
     * @return the {@link Jdbi} instance
     * @implNote With the exception of {@link SqlObjectPlugin}, this method will allow you to install multiple of the
     * same plugin. User beware.
     */
    public static Jdbi buildManagedJdbi(Environment environment,
                                        PooledDataSourceFactory dataSourceFactory,
                                        JdbiPlugin... jdbiPlugins) {
        return buildManagedJdbi(environment, dataSourceFactory, DEFAULT_HEALTH_CHECK_NAME, jdbiPlugins);
    }

    /**
     * Build a Dropwizard-managed {@link Jdbi} instance with metrics and health check given an {@link Environment}, a
     * {@link PooledDataSourceFactory}, and a name to give the health check.
     *
     * @param environment       the {@link Environment}
     * @param dataSourceFactory the {@link PooledDataSourceFactory}
     * @param healthCheckName   the health check's name
     * @param jdbiPlugins       any other {@link JdbiPlugin} objects desired
     * @return the {@link Jdbi} instance
     * @implNote With the exception of {@link SqlObjectPlugin}, this method will allow you to install multiple of the
     * same plugin. User beware.
     */
    public static Jdbi buildManagedJdbi(Environment environment,
                                        PooledDataSourceFactory dataSourceFactory,
                                        String healthCheckName,
                                        JdbiPlugin... jdbiPlugins) {
       checkArgumentNotNull(environment, NULL_ENVIRONMENT_MESSAGE);
       checkArgumentNotNull(dataSourceFactory, NULL_DATA_SOURCE_FACTORY_NAME);

       var dataSource = dataSourceFactory.build(environment.metrics(), healthCheckName);

       return buildManagedJdbi(environment, dataSourceFactory, dataSource, healthCheckName, jdbiPlugins);
    }

    /**
     * Build a Dropwizard-managed {@link Jdbi} instance with metrics and health check given an {@link Environment}, a
     * {@link PooledDataSourceFactory}, and a {@link ManagedDataSource}. This will also register {@link SqlObjectPlugin}
     * with JDBI before returning it.
     *
     * @param environment       the {@link Environment}
     * @param dataSourceFactory the {@link PooledDataSourceFactory}
     * @param dataSource        the {@link ManagedDataSource}
     * @param jdbiPlugins       any other {@link JdbiPlugin} objects desired
     * @return the {@link Jdbi} instance
     * @implNote With the exception of {@link SqlObjectPlugin}, this method will allow you to install multiple of the
     * same plugin. User beware.
     */
    public static Jdbi buildManagedJdbi(Environment environment,
                                        PooledDataSourceFactory dataSourceFactory,
                                        ManagedDataSource dataSource,
                                        JdbiPlugin... jdbiPlugins) {
        return buildManagedJdbi(environment, dataSourceFactory, dataSource, DEFAULT_HEALTH_CHECK_NAME, jdbiPlugins);
    }

    /**
     * Build a Dropwizard-managed {@link Jdbi} instance with metrics and health check given an {@link Environment}, a
     * {@link PooledDataSourceFactory}, a {@link ManagedDataSource}, and a name to give the health check. This will also
     * register {@link SqlObjectPlugin} with JDBI before returning it.
     *
     * @param environment       the {@link Environment}
     * @param dataSourceFactory the {@link PooledDataSourceFactory}
     * @param dataSource        the {@link ManagedDataSource}
     * @param healthCheckName   the health check's name
     * @param jdbiPlugins       any other {@link JdbiPlugin} objects desired
     * @return the {@link Jdbi} instance
     * @implNote With the exception of {@link SqlObjectPlugin}, this method will allow you to install multiple of the
     * same plugin. User beware.
     */
    public static Jdbi buildManagedJdbi(Environment environment,
                                        PooledDataSourceFactory dataSourceFactory,
                                        ManagedDataSource dataSource,
                                        String healthCheckName,
                                        JdbiPlugin... jdbiPlugins) {
        checkArgumentNotNull(environment, NULL_ENVIRONMENT_MESSAGE);
        checkArgumentNotNull(dataSourceFactory, NULL_DATA_SOURCE_FACTORY_NAME);
        checkArgumentNotNull(dataSource, NULL_DATA_SOURCE_MESSAGE);

        var factory = new JdbiFactory();
        var jdbi = factory.build(environment, dataSourceFactory, dataSource, healthCheckName);

        installPlugins(jdbi, jdbiPlugins);

        LOG.debug("Created JDBI v3 instance: {}", jdbi);

        return jdbi;
    }

    private static void installPlugins(Jdbi jdbi, JdbiPlugin... jdbiPlugins) {
        jdbi.installPlugin(new SqlObjectPlugin());

        Arrays.stream(jdbiPlugins)
                .filter(not(Jdbi3Builders::isSqlObjectPlugin))
                .forEach(jdbi::installPlugin);
    }

    private static boolean isSqlObjectPlugin(JdbiPlugin jdbiPlugin) {
        return jdbiPlugin instanceof SqlObjectPlugin;
    }
}
