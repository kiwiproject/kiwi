package org.kiwiproject.jaxrs.exception;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.base.MoreObjects;
import jakarta.ws.rs.core.Response;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.collect.KiwiMaps;

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.Map;

/**
 * An error message that kiwi uses in Jakarta REST related utilities. This is effectively a replacement for the
 * Dropwizard class of the same name.
 * <p>
 * Each instance contains the HTTP status (error) code; the error message; an optional identifier to identify the
 * specific item causing the error (e.g., a primary key); and an optional field/property name for cases when a specific
 * field causes the error.
 */
@Getter
@EqualsAndHashCode
@Slf4j
public class ErrorMessage {

    public static final String KEY_CODE = "code";
    public static final String KEY_FIELD_NAME = "fieldName";
    public static final String KEY_ITEM_ID = "itemId";
    public static final String KEY_MESSAGE = "message";

    static final String DEFAULT_MSG = "Unknown error";
    static final int DEFAULT_CODE = 500;

    private final int code;
    private final String fieldName;
    private final String itemId;
    private final String message;

    /**
     * Create instance with the given message and the default status code (500).
     *
     * @param message the error message
     */
    public ErrorMessage(String message) {
        this(null, DEFAULT_CODE, message, null);
    }

    /**
     * Create instance with the given HTTP status and message.
     *
     * @param status  the HTTP response status
     * @param message the error message
     */
    public ErrorMessage(Response.Status status, String message) {
        this(null, status.getStatusCode(), message, null);
    }

    /**
     * Create instance with the given HTTP status code and message.
     *
     * @param code    the HTTP status code
     * @param message the error message
     */
    public ErrorMessage(int code, String message) {
        this(null, code, message, null);
    }

    /**
     * Create instance with the given HTTP status code, message, and field/property name.
     *
     * @param code      the HTTP status code
     * @param message   the error message
     * @param fieldName the field/property name that caused this error
     */
    public ErrorMessage(int code, String message, String fieldName) {
        this(null, code, message, fieldName);
    }

    /**
     * Create instance with the given item identifier, HTTP status code, message, and field/property name.
     * <p>
     * Note that only this constructor has been marked with {@link ConstructorProperties} due to an open (as
     * of 2020-09-02) issue in Jackson Databind.
     * See <a href="https://github.com/FasterXML/jackson-databind/issues/1514">jackson-databind issue #1514</a>.
     *
     * @param itemId    the unique ID of the item that caused this error
     * @param code      the HTTP status code
     * @param message   the error message
     * @param fieldName the field/property name that caused this error
     */
    @ConstructorProperties({"itemId", "code", "message", "fieldName"})
    public ErrorMessage(String itemId, int code, String message, String fieldName) {
        this.itemId = itemId;
        this.code = code <= 0 ? DEFAULT_CODE : code;
        this.message = isBlank(message) ? DEFAULT_MSG : message;
        this.fieldName = fieldName;
    }

    /**
     * Convert this instance to a map.
     *
     * @return an unmodifiable map
     * @see Collections#unmodifiableMap(Map)
     */
    public Map<String, Object> toMap() {
        // Map.of and Guava's ImmutableMap do not permit null values.
        // This is one way to make the map "immutable" while still allowing null values.
        return Collections.unmodifiableMap(
                KiwiMaps.newHashMap(
                        KEY_MESSAGE, message,
                        KEY_CODE, code,
                        KEY_FIELD_NAME, fieldName,
                        KEY_ITEM_ID, itemId
                )
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(KEY_CODE, code)
                .add(KEY_MESSAGE, message)
                .add(KEY_FIELD_NAME, fieldName)
                .add(KEY_ITEM_ID, itemId)
                .toString();
    }

    /**
     * Build an {@link ErrorMessage} from the given map of properties, whose keys must correspond to the property
     * names and whose values must have the expected type.
     *
     * @param props error message properties
     * @return a new ErrorMessage
     */
    public static ErrorMessage valueOf(Map<String, Object> props) {
        var messageOrDefault = (String) props.getOrDefault(KEY_MESSAGE, DEFAULT_MSG);
        var message = isBlank(messageOrDefault) ? DEFAULT_MSG : messageOrDefault;

        var itemId = (String) props.get(KEY_ITEM_ID);
        var fieldName = (String) props.get(KEY_FIELD_NAME);

        var code = DEFAULT_CODE;
        if (props.containsKey(KEY_CODE)) {
            try {
                code = (Integer) props.get(KEY_CODE);
            } catch (Exception e) {
                LOG.error("Invalid code in properties: {}", props, e);
            }
        }

        return new ErrorMessage(itemId, code, message, fieldName);
    }
}
