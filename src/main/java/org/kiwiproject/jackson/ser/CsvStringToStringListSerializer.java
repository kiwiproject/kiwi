package org.kiwiproject.jackson.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Arrays;

/**
 * Jackson serializer that converts a CSV {@link String} into a JSON array of strings,
 * splitting on commas and stripping whitespace from each value.
 *
 * @see ListToCsvStringDeserializer
 */
public class CsvStringToStringListSerializer extends StdSerializer<String> {

    /**
     * Create a new instance that serializes CSV strings to JSON arrays.
     */
    public CsvStringToStringListSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        var items = Arrays.stream(value.split(","))
                .map(String::strip)
                .toList();

        jsonGenerator.writeStartArray();
        for (var item : items) {
            jsonGenerator.writeString(item);
        }
        jsonGenerator.writeEndArray();
    }
}
