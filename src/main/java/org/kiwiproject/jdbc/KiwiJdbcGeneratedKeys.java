package org.kiwiproject.jdbc;

import lombok.experimental.UtilityClass;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utilities for extracting generated keys from a JDBC {@link Statement} after an insert operation.
 *
 * <p><strong>Requesting generated keys:</strong> The caller must request generated keys before
 * or at execution time, otherwise {@link Statement#getGeneratedKeys()} returns an empty
 * {@link java.sql.ResultSet} and these methods will throw. There are three ways to request them:
 *
 * <pre>{@code
 * // Option 1: PreparedStatement, flag-based (works with index-based retrieval methods)
 * var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 *
 * // Option 2: PreparedStatement, column-name-based (works with both index- and name-based retrieval)
 * var ps = connection.prepareStatement(sql, new String[]{"id"});
 *
 * // Option 3: plain Statement, flag-based at execute time
 * var stmt = connection.createStatement();
 * stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 * }</pre>
 *
 * <p>Since {@link PreparedStatement} and {@link java.sql.CallableStatement} both extend
 * {@link Statement}, all methods in this class accept those types as well.
 *
 * <p><strong>Driver compatibility for name-based methods:</strong> Index-based methods are the most
 * portable across JDBC drivers. Name-based methods have varying support. The following table
 * reflects documented driver behavior and general community experience rather than
 * test-verified guarantees across all listed databases:
 *
 * <table border="1">
 *   <caption>Name-based generated key access by database (based on documented driver behavior)</caption>
 *   <tr><th>Database</th><th>Name-based access</th></tr>
 *   <tr><td>PostgreSQL</td><td>Generally supported</td></tr>
 *   <tr><td>H2</td><td>Supported</td></tr>
 *   <tr><td>SQL Server</td><td>Generally supported</td></tr>
 *   <tr><td>Oracle</td><td>Generally supported (requires column names specified at prepare time)</td></tr>
 *   <tr><td>MySQL / MariaDB</td><td>Unreliable — column may be named {@code GENERATED_KEY}
 *       rather than the actual column name</td></tr>
 *   <tr><td>SQLite</td><td>Not supported — use index-based methods</td></tr>
 * </table>
 */
@UtilityClass
public class KiwiJdbcGeneratedKeys {

    private static final String NO_KEYS_MESSAGE =
            "No generated keys were returned; ensure keys were requested using" +
            " Statement.RETURN_GENERATED_KEYS, by specifying column names, or by specifying column indexes";

    /**
     * Extract the generated key at column index 1 as a {@link Long}.
     * <p>
     * This is the most portable option, as all major JDBC drivers support index-based
     * access on the generated keys {@link java.sql.ResultSet}.
     * <p>
     * The generated keys {@link java.sql.ResultSet} is closed before this method returns.
     *
     * @param statement the {@link Statement}, which must have been executed with generated
     *                  key retrieval enabled
     * @return the Long value of the generated key
     * @throws SQLException          if a database access error occurs
     * @throws IllegalStateException if no generated keys were returned
     * @see #generatedKey(Statement, int, Class)
     * @see Statement#getGeneratedKeys()
     */
    public static Long generatedId(Statement statement) throws SQLException {
        return generatedId(statement, 1);
    }

    /**
     * Extract the generated key at the given column index as a {@link Long}.
     * <p>
     * Index-based access is the most portable option across JDBC drivers.
     * <p>
     * The generated keys {@link java.sql.ResultSet} is closed before this method returns.
     *
     * @param statement   the {@link Statement}, which must have been executed with generated
     *                    key retrieval enabled
     * @param columnIndex the 1-based index of the generated key column
     * @return the Long value of the generated key
     * @throws SQLException          if a database access error occurs
     * @throws IllegalStateException if no generated keys were returned
     * @see #generatedKey(Statement, int, Class)
     * @see Statement#getGeneratedKeys()
     */
    public static Long generatedId(Statement statement, int columnIndex) throws SQLException {
        return generatedKey(statement, columnIndex, Long.class);
    }

    /**
     * Extract the generated key with the given column name as a {@link Long}.
     * <p>
     * Name-based access is not supported by all JDBC drivers. Prefer
     * {@link #generatedId(Statement, int)} for maximum portability. See the
     * class-level Javadoc for driver compatibility details.
     * <p>
     * The generated keys {@link java.sql.ResultSet} is closed before this method returns.
     *
     * @param statement  the {@link Statement}, which must have been executed with generated
     *                   key retrieval enabled
     * @param columnName the name of the generated key column
     * @return the Long value of the generated key
     * @throws SQLException          if a database access error occurs
     * @throws IllegalStateException if no generated keys were returned
     * @see #generatedKey(Statement, String, Class)
     * @see Statement#getGeneratedKeys()
     */
    public static Long generatedId(Statement statement, String columnName) throws SQLException {
        return generatedKey(statement, columnName, Long.class);
    }

    /**
     * Extract the generated key at the given column index as an instance of the given type.
     * <p>
     * Index-based access is the most portable option across JDBC drivers.
     * <p>
     * The generated keys {@link java.sql.ResultSet} is closed before this method returns.
     *
     * @param statement   the {@link Statement}, which must have been executed with generated
     *                    key retrieval enabled
     * @param columnIndex the 1-based index of the generated key column
     * @param keyType     the target type of the generated key
     * @param <T>         the key type
     * @return the value of the generated key
     * @throws SQLException          if a database access error occurs
     * @throws IllegalStateException if no generated keys were returned
     * @see Statement#getGeneratedKeys()
     */
    public static <T> T generatedKey(Statement statement, int columnIndex, Class<T> keyType)
            throws SQLException {
        try (var keys = statement.getGeneratedKeys()) {
            KiwiJdbc.nextOrThrow(keys, NO_KEYS_MESSAGE);
            return keys.getObject(columnIndex, keyType);
        }
    }

    /**
     * Extract the generated key with the given column name as an instance of the given type.
     * <p>
     * Name-based access is not supported by all JDBC drivers. Prefer
     * {@link #generatedKey(Statement, int, Class)} for maximum portability. See the
     * class-level Javadoc for driver compatibility details.
     * <p>
     * The generated keys {@link java.sql.ResultSet} is closed before this method returns.
     *
     * @param statement  the {@link Statement}, which must have been executed with generated
     *                   key retrieval enabled
     * @param columnName the name of the generated key column
     * @param keyType    the target type of the generated key
     * @param <T>        the key type
     * @return the value of the generated key
     * @throws SQLException          if a database access error occurs
     * @throws IllegalStateException if no generated keys were returned
     * @see Statement#getGeneratedKeys()
     */
    public static <T> T generatedKey(Statement statement, String columnName, Class<T> keyType)
            throws SQLException {
        try (var keys = statement.getGeneratedKeys()) {
            KiwiJdbc.nextOrThrow(keys, NO_KEYS_MESSAGE);
            return keys.getObject(columnName, keyType);
        }
    }
}
