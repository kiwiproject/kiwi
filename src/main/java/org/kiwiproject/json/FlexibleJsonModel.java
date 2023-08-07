package org.kiwiproject.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * This class can be used as a base class for situations where there may be additional/unknown properties in
 * a JSON string that don't have a corresponding property in the class being de-serialized into. It uses Jackson's
 * {@link JsonAnyGetter} and {@link JsonAnySetter} feature to get and set these "extra fields" in a property
 * named {@code extraFields}.
 * <p>
 * The general use case for this "extra fields" is when a service or application proxies data from a canonical source
 * where we don't want or need to know about every individual field, but where we still want to capture those fields,
 * so we can store, display, etc. them. For example, a remote service that has a large number of properties, and
 * we only care about a few of them, but we might want to store all of them for analytics, debugging, etc.
 * <p>
 * This implementation intentionally does not define {@code equals} or {@code hashCode} methods, which means the
 * extra fields will not be included in equality checks unless subclasses choose to include them. It does, however,
 * include a default {@code toString} method that will print out the extra fields. Whether to include extra fields
 * in equality checks depends heavily on the use case, so subclasses must decide for themselves. In addition, many
 * times simple data holder objects don't need to be compared so don't need {@code equals} or {@code hashCode}.
 */
@ToString
public abstract class FlexibleJsonModel {

    protected final Map<String, Object> extraFields = new HashMap<>();

    /**
     * Returns the "extra" fields that were not explicitly defined as properties when this object was de-serialized
     * from JSON.
     *
     * @return the extra fields
     */
    @JsonAnyGetter
    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    /**
     * Add an "extra" field when de-serializing from JSON.
     *
     * @param key   the extra field name
     * @param value the value of the extra field
     */
    @JsonAnySetter
    public void setExtraFields(String key, Object value) {
        extraFields.put(key, value);
    }
}
