package org.kiwiproject.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

@DisplayName("RuntimeSQLException")
class UncheckedSQLExceptionTest {

    @Test
    void shouldConstructWithMessageAndSQLException() {
        var sqlEx = new SQLException("Illegal syntax or something like that...");
        var runtimeSQLEx = new UncheckedSQLException(sqlEx);

        assertThat(runtimeSQLEx)
                .hasMessage("java.sql.SQLException: Illegal syntax or something like that...")
                .hasCauseReference(sqlEx);
    }

    @Test
    void shouldConstructWithSQLException() {
        var sqlEx = new SQLException("Unknown column 'foo'");
        var runtimeSQLEx = new UncheckedSQLException("Statement error", sqlEx);

        assertThat(runtimeSQLEx)
                .hasMessage("Statement error")
                .hasCauseReference(sqlEx);
    }
}
