package org.kiwiproject.dropwizard.jdbi2;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;

/**
 * Utilities for building Dropwizard-managed {@link DBI} instances.
 * <p>
 * NOTE: This class is for JDBI version 2, if you would like to upgrade to JDBI version
 * 3, there is an equivalent to this class in {@code org.kiwiproject.dropwizard.jdbi3}.
 */
@UtilityClass
@Slf4j
public class Jdbi2Builders {

    public static final String DEFAULT_HEALTH_CHECK_NAME = "database";

    private static final String NULL_ENVIRONMENT_MESSAGE = "Environment cannot be null";
    private static final String NULL_DATA_SOURCE_FACTORY_NAME = "PooledDataSourceFactory cannot be null";
    private static final String NULL_DATA_SOURCE_MESSAGE = "ManagedDataSource cannot be null";

    /**
     * Build a Dropwizard-managed {@link DBI} instance with metrics and health check given an {@link Environment} and
     * {@link PooledDataSourceFactory}.
     *
     * @param environment       the Dropwizard environment
     * @param dataSourceFactory the data source factory
     * @return new DBI instance
     */
    public static DBI buildManagedJdbi(Environment environment,
                                       PooledDataSourceFactory dataSourceFactory) {
        return buildManagedJdbi(environment, dataSourceFactory, DEFAULT_HEALTH_CHECK_NAME);
    }

    /**
     * Build a Dropwizard-managed {@link DBI} instance with metrics and health check given an {@link Environment},
     * {@link PooledDataSourceFactory}, and name to give to the health check.
     *
     * @param environment       the Dropwizard environment
     * @param dataSourceFactory the data source factory
     * @param healthCheckName   the name to give the health check
     * @return new DBI instance
     */
    public static DBI buildManagedJdbi(Environment environment,
                                       PooledDataSourceFactory dataSourceFactory,
                                       String healthCheckName) {
        checkArgumentNotNull(environment, NULL_ENVIRONMENT_MESSAGE);
        checkArgumentNotNull(dataSourceFactory, NULL_DATA_SOURCE_FACTORY_NAME);

        var dbiFactory = new DBIFactory();
        var jdbi = dbiFactory.build(environment, dataSourceFactory, healthCheckName);

        LOG.debug("Created DBI instance: {}", jdbi);
        return jdbi;
    }

    /**
     * Build a Dropwizard-managed {@link DBI} instance with metrics and health check given an {@link Environment},
     * {@link PooledDataSourceFactory}, and {@link ManagedDataSource}.
     *
     * @param environment       the Dropwizard environment
     * @param dataSourceFactory the data source factory
     * @param dataSource        the data source
     * @return new DBI instance
     */
    public static DBI buildManagedJdbi(Environment environment,
                                       PooledDataSourceFactory dataSourceFactory,
                                       ManagedDataSource dataSource) {
        return buildManagedJdbi(environment, dataSourceFactory, dataSource, DEFAULT_HEALTH_CHECK_NAME);
    }

    /**
     * Build a Dropwizard-managed {@link DBI} instance with metrics and health check given an {@link Environment},
     * {@link PooledDataSourceFactory}, {@link ManagedDataSource}, and name to give to the health check.
     *
     * @param environment       the Dropwizard environment
     * @param dataSourceFactory the data source factory
     * @param dataSource        the data source
     * @param healthCheckName   the name to give the health check
     * @return new DBI instance
     */
    public static DBI buildManagedJdbi(Environment environment,
                                       PooledDataSourceFactory dataSourceFactory,
                                       ManagedDataSource dataSource,
                                       String healthCheckName) {
        checkArgumentNotNull(environment, NULL_ENVIRONMENT_MESSAGE);
        checkArgumentNotNull(dataSourceFactory, NULL_DATA_SOURCE_FACTORY_NAME);
        checkArgumentNotNull(dataSource, NULL_DATA_SOURCE_MESSAGE);

        var dbiFactory = new DBIFactory();
        var jdbi = dbiFactory.build(environment, dataSourceFactory, dataSource, healthCheckName);

        LOG.debug("Created DBI instance: {}", jdbi);
        return jdbi;
    }
}
