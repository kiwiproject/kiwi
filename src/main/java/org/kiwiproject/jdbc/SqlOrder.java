package org.kiwiproject.jdbc;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Simple enum that represents the values for SQL {@code ORDER BY} clauses.
 * <p>
 * This is useful in building queries, for example, when using JDBI and the SQL objects API, you might
 * need to add dynamic ordering based on user input but want to ensure no SQL injection attack is possible.
 * So you would accept user input and then use {@link #from(String)} to get a {@link SqlOrder} instance.
 * Then, you could use it in a JDBI {@code SqlQuery} annotation as one example.
 */
public enum SqlOrder {

    ASC, DESC;

    /**
     * Given a string value, return a {@link SqlOrder}, ignoring case and leading/trailing whitespace.
     *
     * @param value the value to convert from
     * @return the SqlOrder enum corresponding to the given value
     * @throws IllegalArgumentException if an invalid value is provided but does not map to a SqlOrder enum constant
     */
    public static SqlOrder from(String value) {
        var trimmedValue = Optional.ofNullable(value).map(String::trim).orElse("");

        if ("ASC".equalsIgnoreCase(trimmedValue)) {
            return ASC;
        } else if ("DESC".equalsIgnoreCase(trimmedValue)) {
            return DESC;
        }

        throw new IllegalArgumentException("Invalid SQL order: " + value);
    }

    /**
     * Perform a search in either ascending or descending order using the given {@link Supplier} instances.
     * If this instance is {@link #ASC} then the ascending search is executed, otherwise if {@link #DESC} the
     * descending search is executed.
     *
     * @param ascSearch  the logic to perform an ascending search
     * @param descSearch the logic to perform a descending search
     * @param <T>        the result type
     * @return a list of results in either ascending or descending order based on this instance
     */
    public <T> List<T> searchWith(Supplier<List<T>> ascSearch, Supplier<List<T>> descSearch) {
        checkArgumentNotNull(ascSearch, "Ascending search must be specified");
        checkArgumentNotNull(descSearch, "Descending search must be specified");

        if (this == ASC) {
            return ascSearch.get();
        }

        return descSearch.get();
    }

    /**
     * Return a string that can be used directly in a SQL {@code ORDER BY} clause.
     *
     * @return a string that can be used in an SQL query
     */
    public String toSql() {
        return name();
    }
}
