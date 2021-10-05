package org.kiwiproject.jackson.ser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.yaml.YamlHelper;

@DisplayName("ListToCsvStringDeserializer")
class ListToCsvStringDeserializerTest {

    @Test
    void shouldRequireNonNullCodecToReadObjectAsTree() {
        var deserializer = new ListToCsvStringDeserializer();

        var parser = mock(JsonParser.class);
        when(parser.getCodec()).thenReturn(null);

        assertThatIllegalStateException()
                .isThrownBy(() -> deserializer.deserialize(parser, null))
                .withMessage("There is no codec associated with the parser; a codec is required to read the content as a tree");
    }

    @Nested
    class FromYaml {

        @Test
        void shouldSetFromCsv() {
            var yaml = testFixture("sample-config-csv-urls.yml");
            parseYamlAndAssertRegistryUrls(yaml);
        }

        @Test
        void shouldSetFromList() {
            var yaml = testFixture("sample-config-list-of-urls.yml");
            parseYamlAndAssertRegistryUrls(yaml);
        }

        private void parseYamlAndAssertRegistryUrls(String yaml) {
            var sampleConfig = parseYaml(yaml);
            assertParsedRegistryUrls(sampleConfig);
        }

        @Test
        void shouldSetFromSingleElementList() {
            var yaml = testFixture("sample-config-list-of-one-url.yml");
            var sampleConfig = parseYaml(yaml);
            assertThat(sampleConfig.getRegistryConfig().getRegistryUrls())
                    .isEqualTo("https://eureka-1.acme.com:8761");
        }

        private SampleAppConfig parseYaml(String yaml) {
            return new YamlHelper().toObject(yaml, SampleAppConfig.class);
        }
    }

    @Nested
    class FromJson {

        @Test
        void shouldSetFromCsv() {
            var json = testFixture("sample-config-csv-urls.json");
            parseJsonAndAssertRegistryUrls(json);
        }

        @Test
        void shouldSetFromList() {
            var json = testFixture("sample-config-list-of-urls.json");
            parseJsonAndAssertRegistryUrls(json);
        }

        private void parseJsonAndAssertRegistryUrls(String json) {
            var sampleConfig = new JsonHelper().toObject(json, SampleAppConfig.class);
            assertParsedRegistryUrls(sampleConfig);
        }
    }

    private static String testFixture(String fileName) {
        return Fixtures.fixture("ListToCsvStringDeserializerTest/" + fileName);
    }

    private static void assertParsedRegistryUrls(SampleAppConfig sampleAppConfig) {
        assertThat(sampleAppConfig.getRegistryConfig().getRegistryUrls())
                .isEqualTo("https://eureka-1.acme.com:8761,https://eureka-2.acme.com:8761");
    }

    @Getter
    @Setter
    private static class SampleAppConfig {
        private RegistryConfig registryConfig;
    }

    @Getter
    @Setter
    private static class RegistryConfig {
        @JsonDeserialize(using = ListToCsvStringDeserializer.class)
        private String registryUrls;
    }

}