package org.kiwiproject.jackson.deser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@DisplayName("LocalDateTimeEpochTimeDeserializer")
class LocalDateTimeEpochTimeDeserializerTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private LocalDateTimeEpochTimeDeserializer deserializer;
    private LocalDateTime nowUtc;

    @BeforeEach
    void setUp() {
        deserializer = new LocalDateTimeEpochTimeDeserializer();
        nowUtc = LocalDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS);
    }

    @Test
    void shouldDeserializeFromEpochMillis_WhenJsonContainsOnlyTimestamp() throws JsonProcessingException {
        var epochMillis = nowUtc.atZone(UTC).toInstant().toEpochMilli();
        var json = """
                {"createdAt":%d}""".formatted(epochMillis);

        var module = new SimpleModule().addDeserializer(LocalDateTime.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(nowUtc);
    }

    @Test
    void shouldDeserializeFromEpochMillis_WhenJsonContainsOtherFields() throws JsonProcessingException {
        var epochMillis = nowUtc.atZone(UTC).toInstant().toEpochMilli();
        var json = """
                {
                    "name":"Alice",
                    "text":"Today we went sledding...",
                    "createdAt":%d
                }""".formatted(epochMillis);

        var module = new SimpleModule().addDeserializer(LocalDateTime.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(nowUtc);
    }

    @Test
    void shouldDeserializeWhenUsingJsonDeserializeAnnotationOnClass() throws JsonProcessingException {
        var epochMillis = nowUtc.atZone(UTC).toInstant().toEpochMilli();
        var json = """
                {
                    "name":"Bob",
                    "text":"Yesterday we went fishing...",
                    "createdAt":%d
                }""".formatted(epochMillis);

        var objectMapper = new ObjectMapper();
        var post = objectMapper.readValue(json, Post.class);

        assertThat(post.getCreatedAt()).isEqualTo(nowUtc);
    }

    @Test
    void shouldDeserializeDirectly() throws IOException {
        var epochMillis = nowUtc.atZone(UTC).toInstant().toEpochMilli();
        var parser = new JsonFactory().createParser(String.valueOf(epochMillis));
        parser.nextToken();

        var localDateTime = deserializer.deserialize(parser, null);

        assertThat(localDateTime).isEqualTo(nowUtc);
    }

    @Test
    void shouldDeserializeInSpecifiedZone() throws JsonProcessingException {
        var newYorkZoneId = ZoneId.of("America/New_York");
        var zoneDeserializer = new LocalDateTimeEpochTimeDeserializer(newYorkZoneId);
        var epochMillis = nowUtc.atZone(UTC).toInstant().toEpochMilli();
        var json = """
                {"createdAt":%d}""".formatted(epochMillis);

        var module = new SimpleModule().addDeserializer(LocalDateTime.class, zoneDeserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        var expectedInNewYork = Instant.ofEpochMilli(epochMillis).atZone(newYorkZoneId).toLocalDateTime();
        assertThat(entry.getCreatedAt()).isEqualTo(expectedInNewYork);
    }

    @Test
    void shouldDeserializeEpochZero() throws JsonProcessingException {
        var json = """
                {"createdAt":0}""";

        var module = new SimpleModule().addDeserializer(LocalDateTime.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(Instant.EPOCH, UTC));
    }

    @Test
    void shouldDeserializeNegativeEpochMillis() throws JsonProcessingException {
        var beforeEpoch = Instant.parse("1960-01-01T00:00:00Z");
        var json = """
                {"createdAt":%d}""".formatted(beforeEpoch.toEpochMilli());

        var module = new SimpleModule().addDeserializer(LocalDateTime.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(beforeEpoch, UTC));
    }

    @Test
    void shouldReturnNull_WhenJsonValueIsNull() throws JsonProcessingException {
        var json = """
                {"createdAt":null}""";

        var module = new SimpleModule().addDeserializer(LocalDateTime.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isNull();
    }

    @Test
    void shouldThrow_WhenJsonValueIsNonNumeric() {
        var json = """
                {"createdAt":"not-a-number"}""";

        var module = new SimpleModule().addDeserializer(LocalDateTime.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);

        assertThatThrownBy(() -> objectMapper.readValue(json, Entry.class))
                .isInstanceOf(JsonMappingException.class);
    }

    @Value
    @Builder
    private static class Entry {
        String name;
        String text;
        LocalDateTime createdAt;
    }

    @Value
    @Builder
    private static class Post {
        String name;
        String text;

        @JsonDeserialize(using = LocalDateTimeEpochTimeDeserializer.class)
        LocalDateTime createdAt;
    }
}
