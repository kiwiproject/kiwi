package org.kiwiproject.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.kiwiproject.jackson.ser.InstantEpochTimeSerializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Jackson deserializer that converts milliseconds since the epoch into an {@link Instant}.
 * Returns {@code null} if the JSON value is {@code null}.
 *
 * @see InstantEpochTimeSerializer
 */
public class InstantEpochTimeDeserializer extends StdDeserializer<Instant> {

    /**
     * Create a new instance that deserializes epoch millis to {@link Instant}.
     */
    public InstantEpochTimeDeserializer() {
        super(Instant.class);
    }

    /**
     * @throws com.fasterxml.jackson.databind.JsonMappingException if the JSON value is not numeric
     */
    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return Instant.ofEpochMilli(parser.getLongValue());
    }

    @Override
    public Instant getNullValue(DeserializationContext context) {
        return null;
    }
}
