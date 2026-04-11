package org.kiwiproject.jdbi;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

@SuppressWarnings("SqlNoDataSourceInspection")
@DisplayName("Jdbi3GeneratedKeys")
class Jdbi3GeneratedKeysTest {

    private Jdbi jdbi;

    @BeforeEach
    void setUp() {
        jdbi = Jdbi.create("jdbc:h2:mem:Jdbi3GeneratedKeysTest");
    }

    @Test
    void shouldExecuteAndGenerateIdWithImplicitFieldName() {
        jdbi.useHandle(handle -> {
            createUsersTable(handle);
            IntStream.range(1, 6).forEach(i -> assertExecuteAndGenerateIdWithImplicitFieldName(handle, i));
        });
    }

    private void assertExecuteAndGenerateIdWithImplicitFieldName(Handle handle, int i) {
        var update = createUserInsert(handle, i);
        var id = Jdbi3GeneratedKeys.executeAndGenerateId(update);
        assertThat(id).isEqualTo(i);
    }

    @Test
    void shouldExecuteAndGenerateIdWithExplicitFieldName() {
        jdbi.useHandle(handle -> {
            createUsersTable(handle);
            IntStream.range(1, 6).forEach(i -> assertExecuteAndGenerateIdWithExplicitFieldName(handle, i));
        });
    }

    private static void assertExecuteAndGenerateIdWithExplicitFieldName(Handle handle, int i) {
        var update = createUserInsert(handle, i);
        var id = Jdbi3GeneratedKeys.executeAndGenerateId(update, "id");
        assertThat(id).isEqualTo(i);
    }

    @Test
    void shouldExecuteAndGenerateKey() {
        jdbi.useHandle(handle -> {
            createUsersTable(handle);
            IntStream.range(1, 8).forEach(i -> assertExecuteAndGenerateKey(handle, i));
        });
    }

    private void assertExecuteAndGenerateKey(Handle handle, int i) {
        var update = createUserInsert(handle, i);
        var id = Jdbi3GeneratedKeys.executeAndGenerateKey(update, "id", Long.class);
        assertThat(id).isEqualTo(i);
    }

    private static void createUsersTable(Handle handle) {
        handle.execute("CREATE TABLE users (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR)");
    }

    @SuppressWarnings("SqlResolve")
    private static Update createUserInsert(Handle handle, int i) {
        return handle.createUpdate("INSERT INTO users (name) VALUES (:name)")
                .bind("name", "Adam" + i);
    }
}
