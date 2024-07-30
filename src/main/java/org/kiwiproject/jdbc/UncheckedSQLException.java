package org.kiwiproject.jdbc;

import org.kiwiproject.base.KiwiPreconditions;

import java.sql.SQLException;

/**
 * Unchecked exception that wraps a {@link SQLException}.
 */
public class UncheckedSQLException extends RuntimeException {

    /**
     * Constructs an instance of this class.
     *
     * @param message the detail message
     * @param cause   the {@link SQLException} which is the cause
     */
    public UncheckedSQLException(String message, SQLException cause) {
        super(message, KiwiPreconditions.requireNotNull(cause));
    }

    /**
     * Constructs an instance of this class.
     *
     * @param cause the {@link SQLException} which is the cause
     */
    public UncheckedSQLException(SQLException cause) {
        super(KiwiPreconditions.requireNotNull(cause));
    }

    /**
     * Returns the cause of this exception.
     *
     * @return the {@link SQLException} which is the cause of this exception.
     */
    @Override
    public synchronized SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
