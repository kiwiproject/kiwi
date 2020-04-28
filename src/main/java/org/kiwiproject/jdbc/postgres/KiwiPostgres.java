package org.kiwiproject.jdbc.postgres;

import lombok.experimental.UtilityClass;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

/**
 * Utility functions related to Postgres DBs.
 */
@UtilityClass
public class KiwiPostgres {

    /**
     * The Postgres "json" type.
     */
    public static final String JSON_TYPE = "json";

    /**
     * The Postgres "jsonb" type.
     */
    public static final String JSONB_TYPE = "jsonb";

    /**
     * Creates a new {@link PGobject} of type {@link #JSON_TYPE} with the given JSON value.
     */
    public static PGobject newJSONObject(String jsonValue) {
        return newPGobject(JSON_TYPE, jsonValue);
    }

    /**
     * Creates a new {@link PGobject} of type {@link #JSONB_TYPE} with teh given JSON value.
     */
    public static PGobject newJSONBObject(String jsonValue) {
        return newPGobject(JSONB_TYPE, jsonValue);
    }

    /**
     * Creates a new {@link PGobject} of the specified type and with the given value.
     *
     * @implNote We are catching the {@link SQLException} thrown by {@link PGobject#setType(String)} and wrapping
     * with an {@link IllegalStateException} since this should never happen, since that specific setter method simply
     * sets an instance field.
     */
    public static PGobject newPGobject(String type, String value) {
        var pg = new PGobject();
        pg.setType(type);
        try {
            pg.setValue(value);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "This should not have happened (we are setting value on the base PGobject)", e);
        }
        return pg;
    }
}
