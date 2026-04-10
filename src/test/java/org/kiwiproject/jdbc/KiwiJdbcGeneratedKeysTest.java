package org.kiwiproject.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("SqlNoDataSourceInspection")
@DisplayName("KiwiJdbcGeneratedKeys")
class KiwiJdbcGeneratedKeysTest {

    private static Connection connection;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:KiwiJdbcGeneratedKeysTest;DB_CLOSE_DELAY=-1");
        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE test_items (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        connection.close();
    }

    @Nested
    class GeneratedId_DefaultColumnIndex {

        @Test
        void shouldReturnGeneratedIdAtColumnIndex1() throws SQLException {
            try (var ps = connection.prepareStatement(
                    "INSERT INTO test_items (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "item");
                ps.executeUpdate();
                var id = KiwiJdbcGeneratedKeys.generatedId(ps);
                assertThat(id).isPositive();
            }
        }

        @Test
        void shouldThrowWhenNoGeneratedKeysRequested() throws SQLException {
            try (var ps = connection.prepareStatement("INSERT INTO test_items (name) VALUES (?)")) {
                ps.setString(1, "item");
                ps.executeUpdate();
                assertThatIllegalStateException()
                        .isThrownBy(() -> KiwiJdbcGeneratedKeys.generatedId(ps));
            }
        }
    }

    @Nested
    class GeneratedId_ByColumnIndex {

        @Test
        void shouldReturnGeneratedIdAtGivenColumnIndex() throws SQLException {
            try (var ps = connection.prepareStatement(
                    "INSERT INTO test_items (name) VALUES (?)", new int[]{1})) {
                ps.setString(1, "item");
                ps.executeUpdate();
                var id = KiwiJdbcGeneratedKeys.generatedId(ps, 1);
                assertThat(id).isPositive();
            }
        }
    }

    @Nested
    class GeneratedId_ByColumnName {

        @Test
        void shouldReturnGeneratedIdByColumnName() throws SQLException {
            try (var ps = connection.prepareStatement(
                    "INSERT INTO test_items (name) VALUES (?)", new String[]{"id"})) {
                ps.setString(1, "item");
                ps.executeUpdate();
                var id = KiwiJdbcGeneratedKeys.generatedId(ps, "id");
                assertThat(id).isPositive();
            }
        }
    }

    @Nested
    class GeneratedKey_ByColumnIndex {

        @Test
        void shouldReturnGeneratedKeyAsLong() throws SQLException {
            try (var ps = connection.prepareStatement(
                    "INSERT INTO test_items (name) VALUES (?)", new int[]{1})) {
                ps.setString(1, "item");
                ps.executeUpdate();
                var id = KiwiJdbcGeneratedKeys.generatedKey(ps, 1, Long.class);
                assertThat(id).isPositive();
            }
        }

        @Test
        void shouldThrowWhenNoGeneratedKeysRequested() throws SQLException {
            try (var ps = connection.prepareStatement("INSERT INTO test_items (name) VALUES (?)")) {
                ps.setString(1, "item");
                ps.executeUpdate();
                assertThatIllegalStateException()
                        .isThrownBy(() -> KiwiJdbcGeneratedKeys.generatedKey(ps, 1, Long.class));
            }
        }
    }

    @Nested
    class GeneratedKey_ByColumnName {

        @Test
        void shouldReturnGeneratedKeyByColumnName() throws SQLException {
            try (var ps = connection.prepareStatement(
                    "INSERT INTO test_items (name) VALUES (?)", new String[]{"id"})) {
                ps.setString(1, "item");
                ps.executeUpdate();
                var id = KiwiJdbcGeneratedKeys.generatedKey(ps, "id", Long.class);
                assertThat(id).isPositive();
            }
        }

        @Test
        void shouldThrowWhenNoGeneratedKeysRequested() throws SQLException {
            try (var ps = connection.prepareStatement("INSERT INTO test_items (name) VALUES (?)")) {
                ps.setString(1, "item");
                ps.executeUpdate();
                assertThatIllegalStateException()
                        .isThrownBy(() -> KiwiJdbcGeneratedKeys.generatedKey(ps, "id", Long.class));
            }
        }
    }
}
