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
import org.kiwiproject.time.KiwiInstants;

import java.io.IOException;
import java.time.Instant;

@DisplayName("InstantEpochTimeDeserializer")
class InstantEpochTimeDeserializerTest {

    private InstantEpochTimeDeserializer deserializer;
    private Instant now;

    @BeforeEach
    void setUp() {
        deserializer = new InstantEpochTimeDeserializer();
        now = KiwiInstants.truncatedToMillis(Instant.now());
    }

    @Test
    void shouldDeserializeFromEpochMillis_WhenJsonContainsOnlyTimestamp() throws JsonProcessingException {
        var epochMillis = now.toEpochMilli();
        var json = """
                {"createdAt":%d}""".formatted(epochMillis);

        var module = new SimpleModule().addDeserializer(Instant.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldDeserializeFromEpochMillis_WhenJsonContainsOtherFields() throws JsonProcessingException {
        var epochMillis = now.toEpochMilli();
        var json = """
                {
                    "name":"Diane",
                    "text":"Today we went sledding...",
                    "createdAt":%d
                }""".formatted(epochMillis);

        var module = new SimpleModule().addDeserializer(Instant.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldDeserializeWhenUsingJsonDeserializeAnnotationOnClass() throws JsonProcessingException {
        var epochMillis = now.toEpochMilli();
        var json = """
                {
                    "name":"Carlos",
                    "text":"Yesterday we went fishing...",
                    "createdAt":%d
                }""".formatted(epochMillis);

        var objectMapper = new ObjectMapper();
        var post = objectMapper.readValue(json, Post.class);

        assertThat(post.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldDeserializeDirectly() throws IOException {
        var epochMillis = now.toEpochMilli();
        var parser = new JsonFactory().createParser(String.valueOf(epochMillis));
        parser.nextToken();

        var instant = deserializer.deserialize(parser, null);

        assertThat(instant).isEqualTo(now);
    }

    @Test
    void shouldDeserializeEpochZero() throws JsonProcessingException {
        var json = """
                {"createdAt":0}""";

        var module = new SimpleModule().addDeserializer(Instant.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(Instant.EPOCH);
    }

    @Test
    void shouldDeserializeNegativeEpochMillis() throws JsonProcessingException {
        var beforeEpoch = Instant.parse("1960-01-01T00:00:00Z");
        var json = """
                {"createdAt":%d}""".formatted(beforeEpoch.toEpochMilli());

        var module = new SimpleModule().addDeserializer(Instant.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isEqualTo(beforeEpoch);
    }

    @Test
    void shouldReturnNull_WhenJsonValueIsNull() throws JsonProcessingException {
        var json = """
                {"createdAt":null}""";

        var module = new SimpleModule().addDeserializer(Instant.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var entry = objectMapper.readValue(json, Entry.class);

        assertThat(entry.getCreatedAt()).isNull();
    }

    @Test
    void shouldThrow_WhenJsonValueIsNonNumeric() {
        var json = """
                {"createdAt":"not-a-number"}""";

        var module = new SimpleModule().addDeserializer(Instant.class, deserializer);
        var objectMapper = new ObjectMapper().registerModule(module);

        assertThatThrownBy(() -> objectMapper.readValue(json, Entry.class))
                .isInstanceOf(JsonMappingException.class);
    }

    @Value
    @Builder
    private static class Entry {
        String name;
        String text;
        Instant createdAt;
    }

    @Value
    @Builder
    private static class Post {
        String name;
        String text;

        @JsonDeserialize(using = InstantEpochTimeDeserializer.class)
        Instant createdAt;
    }
}
