package org.kiwiproject.jackson.ser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;

@DisplayName("InstantEpochTimeSerializer")
class InstantEpochTimeSerializerTest {

    private InstantEpochTimeSerializer serializer;
    private JsonGenerator jsonGenerator;
    private StringWriter writer;
    private Instant now;

    @BeforeEach
    void setUp() throws IOException {
        serializer = new InstantEpochTimeSerializer();

        writer = new StringWriter();
        var jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(writer);

        now = Instant.now();
    }

    @Test
    void shouldSerializeToEpochMillis() throws IOException {
        serializer.serialize(now, jsonGenerator, null);
        jsonGenerator.close();

        var serializedValue = Long.parseLong(writer.toString());
        assertThat(serializedValue).isEqualTo(now.toEpochMilli());
    }

    @Test
    void shouldSerializeWhenRegisteredOnObjectMapper() throws JsonProcessingException {
        var entry = Entry.builder()
                .name("Diane")
                .text("Today we went sledding in the snow...")
                .createdAt(now)
                .build();

        var module = new SimpleModule().addSerializer(serializer);
        var objectMapper = new ObjectMapper().registerModule(module);
        var json = objectMapper.writeValueAsString(entry);

        assertSerializesCreatedAtToEpochMillis(json, objectMapper, now);
    }

    @Test
    void shouldSerializeWhenUsingJsonSerializeAnnotationOnClass() throws IOException {
        var post = Post.builder()
                .name("Carlos")
                .text("Yesterday we went fishing...")
                .createdAt(now)
                .build();

        var objectMapper = new ObjectMapper();
        var json = objectMapper.writeValueAsString(post);

        assertSerializesCreatedAtToEpochMillis(json, objectMapper, now);
    }

    private static void assertSerializesCreatedAtToEpochMillis(String json, ObjectMapper mapper, Instant expected)
            throws JsonProcessingException {

        var map = mapper.readValue(json, Map.class);
        var createdAt = (long) map.get("createdAt");
        assertThat(createdAt).isEqualTo(expected.toEpochMilli());
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

        @JsonSerialize(using = InstantEpochTimeSerializer.class)
        Instant createdAt;
    }
}