package org.kiwiproject.jackson.ser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@DisplayName("LocalDateTimeEpochTimeSerializer")
class LocalDateTimeEpochTimeSerializerTest {

    private LocalDateTimeEpochTimeSerializer serializer;
    private JsonGenerator jsonGenerator;
    private StringWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        writer = new StringWriter();

        var jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(writer);
    }

    @Test
    void shouldSerializeToEpochMillis_InUTC_ByDefault() throws IOException {
        var newYorkZoneId = ZoneId.of("America/New_York");
        var nowInNewYork = LocalDateTime.now(newYorkZoneId);
        var zoneOffset = newYorkZoneId.getRules().getOffset(nowInNewYork);
        var nowEpochMilliInNY = nowInNewYork.atZone(newYorkZoneId).toInstant().toEpochMilli();

        serializer = new LocalDateTimeEpochTimeSerializer();
        serializer.serialize(nowInNewYork, jsonGenerator, null);
        jsonGenerator.close();

        var nowEpochMillisInUTC = nowInNewYork.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        assertThat(writer).hasToString(String.valueOf(nowEpochMillisInUTC));

        // Sanity checks the offset...
        var offsetSeconds = zoneOffset.getTotalSeconds();
        assertThat(nowEpochMillisInUTC - nowEpochMilliInNY)
                .isEqualTo(Duration.ofSeconds(offsetSeconds).toMillis());
    }

    @Test
    void shouldSerializeToEpochMillis_InSpecifiedZone() throws IOException {
        var newYorkZoneId = ZoneId.of("America/New_York");
        var nowInNewYork = LocalDateTime.now(newYorkZoneId);
        var nowEpochMilliInNY = nowInNewYork.atZone(newYorkZoneId).toInstant().toEpochMilli();

        var chicagoZoneId = ZoneId.of("America/Chicago");
        serializer = new LocalDateTimeEpochTimeSerializer(chicagoZoneId);
        serializer.serialize(nowInNewYork, jsonGenerator, null);
        jsonGenerator.close();

        var nowEpochMillisInChicago = nowInNewYork.atZone(chicagoZoneId).toInstant().toEpochMilli();
        assertThat(writer).hasToString(String.valueOf(nowEpochMillisInChicago));

        // Sanity checks the offset...
        var expectedDiffInSeconds = diffInSecondsBetweenZones(nowInNewYork, newYorkZoneId, chicagoZoneId);
        assertThat(nowEpochMillisInChicago - nowEpochMilliInNY)
                .isEqualTo(Duration.ofSeconds(expectedDiffInSeconds).toMillis());
    }

    @Test
    void shouldSerializeWhenRegisteredOnObjectMapper() throws JsonProcessingException {
        var newYorkZoneId = ZoneId.of("America/New_York");
        var createdAtInNewYork = LocalDateTime.now(newYorkZoneId);
        var createdAtEpochMilliInNY = createdAtInNewYork.atZone(newYorkZoneId).toInstant().toEpochMilli();

        var chicagoZoneId = ZoneId.of("America/Chicago");
        serializer = new LocalDateTimeEpochTimeSerializer(chicagoZoneId);

        var objectMapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addSerializer(serializer);
        objectMapper.registerModule(module);

        var comment = sampleComment(createdAtInNewYork);
        var json = objectMapper.writeValueAsString(comment);

        var commentAsMap = objectMapper.readValue(json, Map.class);
        var createdAtEpochMilliInChicago = (long) commentAsMap.get("createdAt");

        // Sanity checks the offset...
        var expectedDiffInSeconds = diffInSecondsBetweenZones(createdAtInNewYork, newYorkZoneId, chicagoZoneId);
        assertThat(createdAtEpochMilliInChicago - createdAtEpochMilliInNY)
                .isEqualTo(Duration.ofSeconds(expectedDiffInSeconds).toMillis());
    }

    private static Comment sampleComment(LocalDateTime createdAt) {
        return Comment.builder()
                .name("Alice")
                .text("This is my first comment!")
                .createdAt(createdAt)
                .build();
    }

    private static int diffInSecondsBetweenZones(LocalDateTime dateTime, ZoneId zone1, ZoneId zone2) {
        var offset1 = zone1.getRules().getOffset(dateTime);
        var offset2 = zone2.getRules().getOffset(dateTime);
        return offset1.getTotalSeconds() - offset2.getTotalSeconds();
    }

    @Value
    @Builder
    private static class Comment {
        String name;
        String text;
        LocalDateTime createdAt;
    }
}
