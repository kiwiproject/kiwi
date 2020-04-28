package org.kiwiproject.jdbc.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PGobject;

import java.util.Map;

@DisplayName("KiwiPostgres")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiPostgresTest {

    private static final JsonMapper MAPPER = new JsonMapper();

    @Test
    void shouldCreateNewJSONObject(SoftAssertions softly) {
        var json = sampleJson();
        var pgObject = KiwiPostgres.newJSONObject(json);
        assertJsonType(softly, pgObject, KiwiPostgres.JSON_TYPE, json);
    }

    @Test
    void shouldCreateNewJSONBObject(SoftAssertions softly) {
        var json = sampleJson();
        var pgObject = KiwiPostgres.newJSONBObject(json);
        assertJsonType(softly, pgObject, KiwiPostgres.JSONB_TYPE, json);
    }

    private String sampleJson() {
        try {
            return MAPPER.writeValueAsString(Map.of(
                    "firstName", "Bob",
                    "lastName", "Sackamano",
                    "appearsIn", "Seinfeld"
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertJsonType(SoftAssertions softly, PGobject pgObject, String expectedType, String expectedValue) {
        softly.assertThat(pgObject.getType()).isEqualTo(expectedType);
        softly.assertThat(pgObject.getValue()).isEqualTo(expectedValue);
    }
}
