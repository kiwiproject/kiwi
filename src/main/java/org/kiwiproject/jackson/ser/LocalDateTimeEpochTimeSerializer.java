package org.kiwiproject.jackson.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Jackson serializer that converts a {@link java.time.LocalDateTime} into milliseconds since the epoch
 * in the UTC zone.
 */
public class LocalDateTimeEpochTimeSerializer extends StdSerializer<LocalDateTime> {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private final ZoneId zoneId;

    /**
     * Create a new instance that serializes in the UTC time zone.
     */
    public LocalDateTimeEpochTimeSerializer() {
        this(UTC_ZONE);
    }

    /**
     * Create a new instance that serializes in the specified time zone.
     *
     * @param zoneId the ID of the time zone
     */
    public LocalDateTimeEpochTimeSerializer(ZoneId zoneId) {
        super(LocalDateTime.class);
        this.zoneId = zoneId;
    }

    @Override
    public void serialize(LocalDateTime value,
                          JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException {

        jsonGenerator.writeNumber(value.atZone(zoneId).toInstant().toEpochMilli());
    }
}
