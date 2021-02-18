package org.kiwiproject.jackson.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Jackson serializer that converts an {@link Instant} into milliseconds since the epoch.
 */
public class InstantEpochTimeSerializer extends StdSerializer<Instant> {

    /**
     * Create a new instance that serializes {@link Instant} to epoch millis.
     */
    public InstantEpochTimeSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value,
                          JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException {

        jsonGenerator.writeNumber(value.toEpochMilli());
    }
}
