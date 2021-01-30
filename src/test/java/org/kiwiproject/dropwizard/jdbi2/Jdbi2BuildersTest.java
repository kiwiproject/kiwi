package org.kiwiproject.dropwizard.jdbi2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi.DBIHealthCheck;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Jdbi2Builders")
class Jdbi2BuildersTest {

    private static final String NULL_ENV_MSG = "Environment cannot be null";
    private static final String NULL_DATA_SOURCE_FACTORY_MSG = "PooledDataSourceFactory cannot be null";
    private static final String NULL_DATA_SOURCE_MSG = "ManagedDataSource cannot be null";
    private static final String DEFAULT_HEALTH_CHECK = "database";
    private static final String NAMED_HEALTH_CHECK = "aHealthCheck";

    private Environment environment;
    private MetricRegistry metricRegistry;
    private LifecycleEnvironment lifecycleEnvironment;
    private HealthCheckRegistry healthCheckRegistry;
    private PooledDataSourceFactory pooledDataSourceFactory;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);

        healthCheckRegistry = mock(HealthCheckRegistry.class);
        when(environment.healthChecks()).thenReturn(healthCheckRegistry);

        metricRegistry = mock(MetricRegistry.class);
        when(environment.metrics()).thenReturn(metricRegistry);

        lifecycleEnvironment = spy(new LifecycleEnvironment(metricRegistry));
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

        pooledDataSourceFactory = mock(PooledDataSourceFactory.class);
    }

    @Nested
    class WithEnvironmentAndPooledDataSourceFactory {

        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(null, pooledDataSourceFactory))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(environment, null))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            when(pooledDataSourceFactory.build(any(MetricRegistry.class), anyString())).thenReturn(managedDataSource);

            var jdbi = Jdbi2Builders.buildManagedJdbi(environment, pooledDataSourceFactory);
            assertThat(jdbi).isNotNull();

            verifyMocksWhenBuildingManagedDataSource(managedDataSource, DEFAULT_HEALTH_CHECK);
        }
    }

    private ManagedDataSource mockManagedDataSource() throws SQLException {
        var connection = mock(Connection.class);
        var managedDataSource = mock(ManagedDataSource.class);
        when(managedDataSource.getConnection()).thenReturn(connection);

        return managedDataSource;
    }

    private void verifyMocksWhenBuildingManagedDataSource(ManagedDataSource managedDataSource, String healthCheckName) {
        verify(environment, times(2)).metrics();
        verify(environment).lifecycle();
        verify(environment).healthChecks();
        verify(environment).getHealthCheckExecutorService();
        verify(lifecycleEnvironment).manage(managedDataSource);
        verify(healthCheckRegistry).register(eq(healthCheckName), any(DBIHealthCheck.class));
        verify(pooledDataSourceFactory).build(metricRegistry, healthCheckName);
        verify(pooledDataSourceFactory).getValidationQuery();
        verify(pooledDataSourceFactory).getValidationQueryTimeout();
        verify(pooledDataSourceFactory).isAutoCommentsEnabled();
        verify(pooledDataSourceFactory).getDriverClass();

        verifyNoMoreInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
    }

    @Nested
    class WithEnvironmentPooledDataSourceFactoryAndHealthCheckName {
        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(null, pooledDataSourceFactory, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(environment, null, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            when(pooledDataSourceFactory.build(any(MetricRegistry.class), anyString())).thenReturn(managedDataSource);

            var jdbi = Jdbi2Builders.buildManagedJdbi(environment, pooledDataSourceFactory, NAMED_HEALTH_CHECK);
            assertThat(jdbi).isNotNull();

            verifyMocksWhenBuildingManagedDataSource(managedDataSource, NAMED_HEALTH_CHECK);
        }
    }

    @Nested
    class WithEnvironmentPooledDataSourceFactoryAndManagedDataSource {
        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(null, pooledDataSourceFactory, managedDataSource))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(environment, null, managedDataSource))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenManagedDataSourceIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(environment, pooledDataSourceFactory, (ManagedDataSource) null))
                    .withMessage(NULL_DATA_SOURCE_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            when(pooledDataSourceFactory.build(any(MetricRegistry.class), anyString())).thenReturn(managedDataSource);

            var jdbi = Jdbi2Builders.buildManagedJdbi(environment, pooledDataSourceFactory, managedDataSource);
            assertThat(jdbi).isNotNull();

            verifyMocksWhenGivenAManagedDataSource(managedDataSource, DEFAULT_HEALTH_CHECK);
        }
    }

    private void verifyMocksWhenGivenAManagedDataSource(ManagedDataSource managedDataSource, String healthCheckName) {
        verify(environment).metrics();
        verify(environment).lifecycle();
        verify(environment).healthChecks();
        verify(environment).getHealthCheckExecutorService();
        verify(lifecycleEnvironment).manage(managedDataSource);
        verify(healthCheckRegistry).register(eq(healthCheckName), any(DBIHealthCheck.class));
        verify(pooledDataSourceFactory).getValidationQuery();
        verify(pooledDataSourceFactory).getValidationQueryTimeout();
        verify(pooledDataSourceFactory).isAutoCommentsEnabled();
        verify(pooledDataSourceFactory).getDriverClass();

        verifyNoMoreInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
    }

    @Nested
    class WithEnvironmentPooledDataSourceFactoryManagedDataSourceAndAHealthCheckName {
        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(null, pooledDataSourceFactory, managedDataSource, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(environment, null, managedDataSource, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenManagedDataSourceIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi2Builders.buildManagedJdbi(environment, pooledDataSourceFactory, null, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_DATA_SOURCE_MSG);

            verifyNoInteractions(environment, metricRegistry, lifecycleEnvironment, healthCheckRegistry, pooledDataSourceFactory);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() throws SQLException {
            var managedDataSource = mockManagedDataSource();
            when(pooledDataSourceFactory.build(any(MetricRegistry.class), anyString())).thenReturn(managedDataSource);

            var jdbi = Jdbi2Builders.buildManagedJdbi(environment, pooledDataSourceFactory, managedDataSource, NAMED_HEALTH_CHECK);
            assertThat(jdbi).isNotNull();

            verifyMocksWhenGivenAManagedDataSource(managedDataSource, NAMED_HEALTH_CHECK);
        }
    }
}
