package org.kiwiproject.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

class SqlOrderTest {

    @Nested
    class FromFactoryMethod {

        @ParameterizedTest
        @ValueSource(strings = {"asc", "ASC", "Asc", "aSC", "AsC", "  asc  ", "\n\tASC\n"})
        void shouldReturn_ASC_ForAscendingValues(String value) {
            assertThat(SqlOrder.from(value)).isEqualTo(SqlOrder.ASC);
        }

        @ParameterizedTest
        @ValueSource(strings = {"desc", "DESC", "Desc", "deSC", "DEsC", " desc  ", "\t\tDESC\n\t\t"})
        void shouldReturn_DESC_ForDescendingValues(String value) {
            assertThat(SqlOrder.from(value)).isEqualTo(SqlOrder.DESC);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowException_ForNullAndEmptyValues(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> SqlOrder.from(value))
                    .withMessage("Invalid SQL order: " + value);
        }

        @ParameterizedTest
        @ValueSource(strings = {"bad", "worse", "whatever", "ascending", "descending", " "})
        void shouldThrowException_ForInvalidValues(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> SqlOrder.from(value))
                    .withMessage("Invalid SQL order: " + value);
        }
    }

    @Nested
    class SearchWith {

        @Test
        void shouldSearchAscending() {
            var results = SqlOrder.ASC.searchWith(
                    () -> List.of("foo", "bar"),
                    ArrayList::new
            );
            assertThat(results).containsExactly("foo", "bar");
        }

        @Test
        void shouldSearchDescending() {
            var results = SqlOrder.DESC.searchWith(
                    ArrayList::new,
                    () -> List.of("baz", "qux")
            );
            assertThat(results).containsExactly("baz", "qux");
        }
    }

    @Nested
    class ToSql {

        @Test
        void shouldReturnAscending() {
            assertThat(SqlOrder.ASC.toSql()).isEqualTo("ASC");
        }

        @Test
        void shouldReturnDescending() {
            assertThat(SqlOrder.DESC.toSql()).isEqualTo("DESC");
        }
    }
}