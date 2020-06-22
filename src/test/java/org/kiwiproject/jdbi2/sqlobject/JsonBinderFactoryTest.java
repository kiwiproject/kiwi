package org.kiwiproject.jdbi2.sqlobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.jdbc.postgres.KiwiPostgres;
import org.kiwiproject.json.JsonHelper;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.MockQuery;

/**
 * Tests that {@link BindJSONB.JsonBinderFactory} constructs the expected type of {@link PGobject} and that
 * it calls {@link org.skife.jdbi.v2.SQLStatement#bind(String, Object)} with the correct value. It does <em>not</em>
 * test against a real database, thus heavy usage of mocks (for better or worse) is used.
 *
 * @implNote Originally this test used an experimental(incubating) feature of Mockito that lets you mock final classes
 * and methods, because the JDBI {@link org.skife.jdbi.v2.SQLStatement} is abstract and contains final methods.
 * See item #39 in {@link org.mockito.Mockito} for details on the experimental feature, and also see
 * {@link org.mockito.internal.creation.bytebuddy.InlineByteBuddyMockMaker} for even more details. Originally, when
 * we updated the Jacoco Maven plugin from 0.8.3 to 0.8.4, we encountered a bunch of mocking problems that caused
 * a ton of tests to fail. Those failures all pointed back to item #39 in Mockito's Javadoc. We therefore removed
 * the inline mock configuration (in the {@code /mockito-extensions/org.mockito.plugins.MockMaker} file) and created
 * a "real mock" by creating the {@link MockQuery} class. This "real mock" gets around the whole set of problems
 * mocking abstract classes with final methods. Due to package-level visibility on the constructor, it has to be in
 * the JDBI v2 package (org.skife.jdbi.v2). It's certainly not perfect, but it works for now.
 */
@DisplayName("JsonBinderFactory")
class JsonBinderFactoryTest {

    @Test
    void shouldBindPostgresJSONBFieldToStatement() {
        var binder = new BindJSONB.JsonBinderFactory().build(null);
        var sqlStatement = new MockQuery();

        var bindJSONB = mock(BindJSONB.class);
        when(bindJSONB.value()).thenReturn("someField");

        var json = JsonHelper.newDropwizardJsonHelper().toJsonFromKeyValuePairs(
                "firstName", "Bob",
                "lastName", "Sacamano",
                "appearsIn", "Seinfeld"
        );

        //noinspection unchecked
        binder.bind(sqlStatement, bindJSONB, json);

        var bindArguments = sqlStatement.getBindArguments();

        assertThat(bindArguments).hasSize(1);

        var bindArg = first(bindArguments);
        assertThat(bindArg.getName()).isEqualTo("someField");

        var argValue = bindArg.getObjectArgumentValue();
        assertThat(argValue).isExactlyInstanceOf(PGobject.class);

        var pgObject = (PGobject) argValue;
        assertThat(pgObject.getType()).isEqualTo(KiwiPostgres.JSONB_TYPE);
        assertThat(pgObject.getValue()).isEqualTo(json);
    }
}
