package org.kiwiproject.dropwizard.jdbi3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi3.JdbiHealthCheck;
import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.h2.Driver;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

@DisplayName("Jdbi3Builders")
class Jdbi3BuildersTest {

    private static final String NULL_ENV_MSG = "Environment cannot be null";
    private static final String NULL_DATA_SOURCE_FACTORY_MSG = "PooledDataSourceFactory cannot be null";
    private static final String NULL_DATA_SOURCE_MSG = "ManagedDataSource cannot be null";
    private static final String DEFAULT_HEALTH_CHECK = "database";
    private static final String NAMED_HEALTH_CHECK = "aHealthCheck";

    private Environment environment;
    private LifecycleEnvironment lifecycleEnvironment;
    private HealthCheckRegistry healthCheckRegistry;
    private PooledDataSourceFactory pooledDataSourceFactory;
    private ManagedDataSource managedDataSource;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);

        healthCheckRegistry = new HealthCheckRegistry();
        when(environment.healthChecks()).thenReturn(healthCheckRegistry);

        when(environment.metrics()).thenReturn(new MetricRegistry());

        lifecycleEnvironment = new LifecycleEnvironment(new MetricRegistry());
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

        pooledDataSourceFactory = inMemoryH2DataSourceFactory();
        managedDataSource = pooledDataSourceFactory.build(new MetricRegistry(), "testDataSource");
    }

    private static DataSourceFactory inMemoryH2DataSourceFactory() {
        var factory = new DataSourceFactory();
        factory.setDriverClass(Driver.class.getName());
        factory.setUrl("jdbc:h2:mem:");
        factory.setUser("");
        factory.setPassword("");
        factory.setInitialSize(1);
        return factory;
    }

    @Nested
    class WithEnvironmentAndPooledDataSourceFactory {

        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(null, pooledDataSourceFactory))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(environment, null))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment, pooledDataSourceFactory);
            assertCanGetConnection(jdbi);

            verifyAndAssertWhenBuildingManagedDataSource(DEFAULT_HEALTH_CHECK);
        }

        @Test
        void whenUsingTheJdbiVarArgs() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment,
                    pooledDataSourceFactory,
                    new SqlObjectPlugin(),
                    new H2DatabasePlugin(),
                    new H2DatabasePlugin());

            assertCanGetConnection(jdbi);

            verifyAndAssertWhenBuildingManagedDataSource(DEFAULT_HEALTH_CHECK);
        }
    }

    private void verifyAndAssertWhenBuildingManagedDataSource(String healthCheckName) {
        verify(environment, times(2)).metrics();
        verify(environment).lifecycle();
        verify(environment).healthChecks();
        verify(environment).getHealthCheckExecutorService();

        var managedClasses = lifecycleEnvironment.getManagedObjects()
                .stream()
                .map(toJettyManaged())
                .map(JettyManaged::getManaged)
                .map(Managed::getClass)
                .toList();
        assertThat(managedClasses).hasSize(1);
        var firstManagedClass = first(managedClasses);
        assertThat(firstManagedClass.getInterfaces()).contains(ManagedDataSource.class);

        assertThat(healthCheckRegistry.getHealthCheck(healthCheckName)).isInstanceOf(JdbiHealthCheck.class);

        verifyNoMoreInteractions(environment);
    }

    @Nested
    class WithEnvironmentPooledDataSourceFactoryAndHealthCheckName {
        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(null, pooledDataSourceFactory, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(environment, null, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment, pooledDataSourceFactory, NAMED_HEALTH_CHECK);
            assertCanGetConnection(jdbi);

            verifyAndAssertWhenBuildingManagedDataSource(NAMED_HEALTH_CHECK);
        }

        @Test
        void whenUsingTheJdbiVarArgs() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment,
                    pooledDataSourceFactory,
                    NAMED_HEALTH_CHECK,
                    new SqlObjectPlugin(),
                    new H2DatabasePlugin(),
                    new H2DatabasePlugin());

            assertCanGetConnection(jdbi);


            verifyAndAssertWhenBuildingManagedDataSource(NAMED_HEALTH_CHECK);
        }
    }

    @Nested
    class WithEnvironmentPooledDataSourceFactoryAndManagedDataSource {
        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(null, pooledDataSourceFactory, managedDataSource))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(environment, null, managedDataSource))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenManagedDataSourceIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(environment, pooledDataSourceFactory, (ManagedDataSource) null))
                    .withMessage(NULL_DATA_SOURCE_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment, pooledDataSourceFactory, managedDataSource);
            assertCanGetConnection(jdbi);


            verifyAndAssertWhenGivenAManagedDataSource(managedDataSource, DEFAULT_HEALTH_CHECK);
        }

        @Test
        void whenUsingTheJdbiVarArgs() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment,
                    pooledDataSourceFactory,
                    managedDataSource,
                    new SqlObjectPlugin(),
                    new H2DatabasePlugin(),
                    new H2DatabasePlugin());

            assertCanGetConnection(jdbi);


            verifyAndAssertWhenGivenAManagedDataSource(managedDataSource, DEFAULT_HEALTH_CHECK);
        }
    }

    private void verifyAndAssertWhenGivenAManagedDataSource(ManagedDataSource managedDataSource, String healthCheckName) {
        verify(environment).metrics();
        verify(environment).lifecycle();
        verify(environment).healthChecks();
        verify(environment).getHealthCheckExecutorService();

        var managedObjects = lifecycleEnvironment.getManagedObjects()
                .stream()
                .map(toJettyManaged())
                .map(JettyManaged::getManaged)
                .toList();
        assertThat(managedObjects).containsExactly(managedDataSource);

        assertThat(healthCheckRegistry.getHealthCheck(healthCheckName)).isInstanceOf(JdbiHealthCheck.class);

        verifyNoMoreInteractions(environment);
    }

    private static Function<LifeCycle, JettyManaged> toJettyManaged() {
        return JettyManaged.class::cast;
    }

    @Nested
    class WithEnvironmentPooledDataSourceFactoryManagedDataSourceAndAHealthCheckName {
        @Test
        void whenEnvironmentIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(null, pooledDataSourceFactory, managedDataSource, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_ENV_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenPooledDataSourceFactoryIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(environment, null, managedDataSource, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_DATA_SOURCE_FACTORY_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenManagedDataSourceIsNullThrowsIllegalStateException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Jdbi3Builders.buildManagedJdbi(environment, pooledDataSourceFactory, null, NAMED_HEALTH_CHECK))
                    .withMessage(NULL_DATA_SOURCE_MSG);

            verifyNoInteractions(environment);
        }

        @Test
        void whenProperlyConfiguredReturnsJdbi() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment, pooledDataSourceFactory, managedDataSource, NAMED_HEALTH_CHECK);
            assertCanGetConnection(jdbi);


            verifyAndAssertWhenGivenAManagedDataSource(managedDataSource, NAMED_HEALTH_CHECK);
        }

        @Test
        void whenUsingTheJdbiVarArgs() {
            var jdbi = Jdbi3Builders.buildManagedJdbi(environment,
                    pooledDataSourceFactory,
                    managedDataSource,
                    NAMED_HEALTH_CHECK,
                    new SqlObjectPlugin(),
                    new H2DatabasePlugin(),
                    new H2DatabasePlugin());

            assertCanGetConnection(jdbi);


            verifyAndAssertWhenGivenAManagedDataSource(managedDataSource, NAMED_HEALTH_CHECK);
        }
    }

    private void assertCanGetConnection(Jdbi jdbi) {
        assertThat(jdbi).isNotNull();

        jdbi.useHandle(handle -> assertThat(handle.getConnection()).isNotNull());
    }
}
