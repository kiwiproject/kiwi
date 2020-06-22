package org.skife.jdbi.v2;

import static org.mockito.Mockito.mock;

import lombok.Getter;
import org.skife.jdbi.v2.logging.NoOpLog;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.tweak.SQLLog;
import org.skife.jdbi.v2.tweak.StatementBuilder;
import org.skife.jdbi.v2.tweak.StatementCustomizer;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.skife.jdbi.v2.tweak.StatementRewriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A "real" mock of the JDBI2 {@link Query} class, specifically created for
 * {@code org.kiwiproject.jdbi2.sqlobject.JsonBinderFactoryTest}. It provides a no-args constructor
 * with default values, and overrides the {@link SQLStatement#bind(String, Argument)} in order to
 * capture the argument bound to the SQL statement. Use {@link #getBindArguments()} for verification of
 * bound arguments during unit tests.
 *
 * @implNote This is definitely more white box testing that I would like it to be, however it permits
 * us to validate that we are correctly binding Postgres JSONB arguments.
 */
public class MockQuery extends Query<Map<String, Object>> {

    @Getter
    private final List<BindArgument> bindArguments;

    public MockQuery() {
        this(
                new Binding(),
                new DefaultMapper(),
                new ClasspathStatementLocator(),
                new NoOpStatementRewriter(),
                mock(Handle.class),
                new DefaultStatementBuilder(),
                "select * from foo",
                new ConcreteStatementContext(),
                new NoOpLog(),
                TimingCollector.NOP_TIMING_COLLECTOR,
                List.of(),
                new MappingRegistry(),
                new Foreman(),
                new ContainerFactoryRegistry()
        );
    }

    private MockQuery(Binding params,
                      ResultSetMapper<Map<String, Object>> mapper,
                      StatementLocator locator,
                      StatementRewriter statementRewriter,
                      Handle handle,
                      StatementBuilder cache,
                      String sql,
                      ConcreteStatementContext ctx,
                      SQLLog log,
                      TimingCollector timingCollector,
                      Collection<StatementCustomizer> customizers,
                      MappingRegistry mappingRegistry,
                      Foreman foreman,
                      ContainerFactoryRegistry containerFactoryRegistry) {

        super(params, mapper, locator, statementRewriter, handle, cache, sql, ctx, log,
                timingCollector, customizers, mappingRegistry, foreman, containerFactoryRegistry);

        this.bindArguments = new ArrayList<>();
    }

    @Override
    public Query<Map<String, Object>> bind(String name, Argument argument) {
        this.bindArguments.add(new BindArgument(name, argument));

        return super.bind(name, argument);
    }
}
