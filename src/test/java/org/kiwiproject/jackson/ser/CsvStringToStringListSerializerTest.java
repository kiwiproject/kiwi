package org.kiwiproject.jackson.ser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.stream.Stream;

@DisplayName("CsvStringToStringListSerializer")
class CsvStringToStringListSerializerTest {

    private CsvStringToStringListSerializer serializer;
    private JsonGenerator jsonGenerator;
    private StringWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        serializer = new CsvStringToStringListSerializer();
        writer = new StringWriter();
        jsonGenerator = new JsonFactory().createGenerator(writer);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("csvSerializationCases")
    void shouldSerializeToJsonArray(String description, String input, String expected) throws IOException {
        serializer.serialize(input, jsonGenerator, null);
        jsonGenerator.close();

        assertThat(writer).hasToString(expected);
    }

    static Stream<Arguments> csvSerializationCases() {
        return Stream.of(
                arguments("multiple values", "value1,value2,value3", "[\"value1\",\"value2\",\"value3\"]"),
                arguments("strips whitespace from values", " value1 , value2 , value3 ", "[\"value1\",\"value2\",\"value3\"]"),
                arguments("single value", "value1", "[\"value1\"]"),
                arguments("empty string", "", "[\"\"]")
        );
    }

    @Test
    void shouldSerializeWhenRegisteredOnObjectMapper() throws JsonProcessingException {
        var module = new SimpleModule().addSerializer(String.class, serializer);
        var objectMapper = new ObjectMapper().registerModule(module);

        var entry = Entry.builder().tags("alpha,beta,gamma").build();
        var json = objectMapper.writeValueAsString(entry);

        assertThat(json).isEqualTo("{\"tags\":[\"alpha\",\"beta\",\"gamma\"]}");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("csvAnnotationSerializationCases")
    void shouldSerializeWhenUsingJsonSerializeAnnotationOnField(String description, String input, String expected)
            throws JsonProcessingException {
        var objectMapper = new ObjectMapper();

        var post = Post.builder().tags(input).build();
        var json = objectMapper.writeValueAsString(post);

        assertThat(json).isEqualTo(expected);
    }

    static Stream<Arguments> csvAnnotationSerializationCases() {
        return Stream.of(
                arguments("multiple values", "alpha,beta,gamma", "{\"tags\":[\"alpha\",\"beta\",\"gamma\"]}"),
                arguments("strips whitespace from values", " alpha , beta , gamma ", "{\"tags\":[\"alpha\",\"beta\",\"gamma\"]}"),
                arguments("multi-word values", "hello world,foo bar", "{\"tags\":[\"hello world\",\"foo bar\"]}")
        );
    }

    @Value
    @Builder
    private static class Entry {
        String tags;
    }

    @Value
    @Builder
    private static class Post {
        @JsonSerialize(using = CsvStringToStringListSerializer.class)
        String tags;
    }
}
