package org.kiwiproject.jdbi;

import lombok.experimental.UtilityClass;
import org.jdbi.v3.core.statement.Update;

/**
 * Utilities for executing statements and extracting generated keys from JDBI 3 {@link Update} objects.
 */
@UtilityClass
public class Jdbi3GeneratedKeys {

    /**
     * Execute the given {@link Update} and extract the value of the generated key named "id" as a {@code Long}.
     *
     * @param update the {@link Update} object
     * @return the Long value of the generated key
     * @see #executeAndGenerateKey(Update, String, Class)
     */
    public static Long executeAndGenerateId(Update update) {
        return executeAndGenerateId(update, "id");
    }

    /**
     * Execute the given {@link Update} and extract the value of the generated key with the given
     * {@code fieldName} as a {@code Long}.
     *
     * @param update    the {@link Update} object
     * @param fieldName the name of the generated key field
     * @return the Long value of the generated key
     * @see #executeAndGenerateKey(Update, String, Class)
     */
    public static Long executeAndGenerateId(Update update, String fieldName) {
        return executeAndGenerateKey(update, fieldName, Long.class);
    }

    /**
     * Execute the given {@link Update} and extract the value of the generated key with the given
     * {@code fieldName} as an instance of the given class.
     *
     * @param update    the {@link Update} object
     * @param fieldName the name of the generated key field
     * @param clazz     the target class of the generated key
     * @param <T>       the key type
     * @return the value of the generated key
     */
    public static <T> T executeAndGenerateKey(Update update, String fieldName, Class<T> clazz) {
        return update.executeAndReturnGeneratedKeys(fieldName)
                .mapTo(clazz)
                .one();
    }
}
