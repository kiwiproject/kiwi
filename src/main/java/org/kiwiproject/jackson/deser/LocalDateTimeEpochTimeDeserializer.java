package org.kiwiproject.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.kiwiproject.jackson.ser.LocalDateTimeEpochTimeSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Jackson deserializer that converts milliseconds since the epoch into a
 * {@link LocalDateTime}, using UTC by default or a zone specified in the constructor.
 * Returns {@code null} if the JSON value is {@code null}.
 *
 * @see LocalDateTimeEpochTimeSerializer
 */
public class LocalDateTimeEpochTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private final ZoneId zoneId;

    /**
     * Create a new instance that deserializes in the UTC time zone.
     */
    public LocalDateTimeEpochTimeDeserializer() {
        this(UTC_ZONE);
    }

    /**
     * Create a new instance that deserializes in the specified time zone.
     *
     * @param zoneId the ID of the time zone
     */
    public LocalDateTimeEpochTimeDeserializer(ZoneId zoneId) {
        super(LocalDateTime.class);
        this.zoneId = zoneId;
    }

    /**
     * @throws com.fasterxml.jackson.databind.JsonMappingException if the JSON value is not numeric
     */
    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return Instant.ofEpochMilli(parser.getLongValue()).atZone(zoneId).toLocalDateTime();
    }

    @Override
    public LocalDateTime getNullValue(DeserializationContext context) {
        return null;
    }
}
