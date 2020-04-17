package org.kiwiproject.jackson;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Stream;

@DisplayName("KiwiJacksonDataFormats")
class KiwiJacksonDataFormatsTest {

    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    private static final String JSON_DEFAULT_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample.json", DEFAULT_CHARSET);
    private static final String JSON_UTF8_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample.json", UTF_8);
    private static final String JSON_US_ASCII_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample.json", US_ASCII);

    private static final String XML_DEFAULT_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample.xml", DEFAULT_CHARSET);
    private static final String XML_UTF8_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample.xml", UTF_8);
    private static final String XML_US_ASCII_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample.xml", US_ASCII);

    // YAML files that DO contain the opening --- as specified in the YAML specifications
    private static final String YAML_FORMAL_DEFAULT_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample-formal.yml", DEFAULT_CHARSET);
    private static final String YAML_FORMAL_UTF8_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample-formal.yml", UTF_8);
    private static final String YAML_FORMAL_US_ASCII_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample-formal.yml", US_ASCII);

    // YAML files that do NOT contain the opening --- as specified in the YAML specifications
    private static final String YAML_INFORMAL_DEFAULT_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample-informal.yml", DEFAULT_CHARSET);
    private static final String YAML_INFORMAL_UTF8_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample-informal.yml", UTF_8);
    private static final String YAML_INFORMAL_US_ASCII_CHARSET =
            fixtureWithCharset("KiwiJacksonDataFormatsTest/sample-informal.yml", US_ASCII);

    private static final String RANDOM_TEXT = "Something wicked this way comes";

    @Nested
    class IsJsonDefaultCharset {

        @Test
        void shouldReturnTrue_WhenJsonContent() {
            assertThat(KiwiJacksonDataFormats.isJson(JSON_DEFAULT_CHARSET)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotJsonContent() {
            assertThat(KiwiJacksonDataFormats.isJson(XML_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJson(YAML_FORMAL_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJson(YAML_INFORMAL_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJson(RANDOM_TEXT)).isFalse();
        }
    }

    @Nested
    class IsJsonUtf8 {

        @Test
        void shouldReturnTrue_WhenJsonContent() {
            assertThat(KiwiJacksonDataFormats.isJsonUtf8(JSON_UTF8_CHARSET)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotJsonContent() {
            assertThat(KiwiJacksonDataFormats.isJsonUtf8(XML_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJsonUtf8(YAML_FORMAL_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJsonUtf8(YAML_INFORMAL_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJsonUtf8(RANDOM_TEXT)).isFalse();
        }
    }

    @Nested
    class IsJsonUsingSpecificCharset {

        @Test
        void shouldReturnTrue_WhenJsonContent() {
            assertThat(KiwiJacksonDataFormats.isJson(JSON_US_ASCII_CHARSET, US_ASCII)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotJsonContent() {
            assertThat(KiwiJacksonDataFormats.isJson(XML_US_ASCII_CHARSET, US_ASCII)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJson(YAML_FORMAL_US_ASCII_CHARSET, US_ASCII)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJson(YAML_INFORMAL_US_ASCII_CHARSET, US_ASCII)).isFalse();
            assertThat(KiwiJacksonDataFormats.isJson(RANDOM_TEXT, US_ASCII)).isFalse();
        }
    }

    @Nested
    class IsXmlDefaultCharset {

        @Test
        void shouldReturnTrue_WhenXmlContent() {
            assertThat(KiwiJacksonDataFormats.isXml(XML_DEFAULT_CHARSET)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotXmlContent() {
            assertThat(KiwiJacksonDataFormats.isXml(JSON_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXml(YAML_FORMAL_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXml(YAML_INFORMAL_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXml(RANDOM_TEXT)).isFalse();
        }
    }

    @Nested
    class IsXmlUtf8 {

        @Test
        void shouldReturnTrue_WhenXmlContent() {
            assertThat(KiwiJacksonDataFormats.isXmlUtf8(XML_UTF8_CHARSET)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotXmlContent() {
            assertThat(KiwiJacksonDataFormats.isXmlUtf8(JSON_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXmlUtf8(YAML_FORMAL_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXmlUtf8(YAML_INFORMAL_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXmlUtf8(RANDOM_TEXT)).isFalse();
        }
    }

    @Nested
    class IsXmlUsingSpecificCharset {

        @Test
        void shouldReturnTrue_WhenXmlContent() {
            assertThat(KiwiJacksonDataFormats.isXml(XML_US_ASCII_CHARSET, US_ASCII)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotXmlContent() {
            assertThat(KiwiJacksonDataFormats.isXml(JSON_US_ASCII_CHARSET, US_ASCII)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXml(YAML_FORMAL_US_ASCII_CHARSET, US_ASCII)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXml(YAML_INFORMAL_US_ASCII_CHARSET, US_ASCII)).isFalse();
            assertThat(KiwiJacksonDataFormats.isXml(RANDOM_TEXT, US_ASCII)).isFalse();
        }
    }

    @Nested
    class IsYamlDefaultCharset {

        @Test
        void shouldReturnTrue_WhenYamlContent() {
            assertThat(KiwiJacksonDataFormats.isYaml(YAML_FORMAL_DEFAULT_CHARSET)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotYamlContent() {
            assertThat(KiwiJacksonDataFormats.isYaml(JSON_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isYaml(XML_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isYaml(YAML_INFORMAL_DEFAULT_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isYaml(RANDOM_TEXT)).isFalse();
        }
    }

    @Nested
    class IsYamlUtf8 {

        @Test
        void shouldReturnTrue_WhenYamlContent() {
            assertThat(KiwiJacksonDataFormats.isYamlUtf8(YAML_FORMAL_DEFAULT_CHARSET)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotYamlContent() {
            assertThat(KiwiJacksonDataFormats.isYamlUtf8(JSON_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isYamlUtf8(XML_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isYamlUtf8(YAML_INFORMAL_UTF8_CHARSET)).isFalse();
            assertThat(KiwiJacksonDataFormats.isYamlUtf8(RANDOM_TEXT)).isFalse();
        }
    }

    @Nested
    class IsYamlUsingSpecificCharset {

        @Test
        void shouldReturnTrue_WhenYamlContent() {
            assertThat(KiwiJacksonDataFormats.isYaml(YAML_FORMAL_DEFAULT_CHARSET, US_ASCII)).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenNotYamlContent() {
//            assertThat(KiwiJacksonDataFormats.isYaml(JSON_US_ASCII_CHARSET, US_ASCII)).isFalse();
//            assertThat(KiwiJacksonDataFormats.isYaml(XML_US_ASCII_CHARSET, US_ASCII)).isFalse();
            assertThat(KiwiJacksonDataFormats.isYaml(YAML_INFORMAL_US_ASCII_CHARSET, US_ASCII)).isFalse();
//            assertThat(KiwiJacksonDataFormats.isYaml(RANDOM_TEXT, US_ASCII)).isFalse();
        }
    }

    @Nested
    class DetectFormatWithDefaultCharset {

        @Test
        void shouldDetectJson() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(JSON_DEFAULT_CHARSET);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.JSON);
        }

        @Test
        void shouldDetectXml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(XML_DEFAULT_CHARSET);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.XML);
        }

        @Test
        void shouldDetectYaml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(YAML_FORMAL_DEFAULT_CHARSET);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.YAML);
        }

        @Test
        void shouldNotDetectInformalYaml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(YAML_INFORMAL_DEFAULT_CHARSET);
            assertThat(optionalFormat).isEmpty();
        }
    }

    @Nested
    class DetectFormatWithUtf8 {

        @Test
        void shouldDetectJson() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormatUtf8(JSON_UTF8_CHARSET);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.JSON);
        }

        @Test
        void shouldDetectXml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormatUtf8(XML_UTF8_CHARSET);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.XML);
        }

        @Test
        void shouldDetectYaml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormatUtf8(YAML_FORMAL_UTF8_CHARSET);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.YAML);
        }

        @Test
        void shouldNotDetectInformalYaml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormatUtf8(YAML_INFORMAL_UTF8_CHARSET);
            assertThat(optionalFormat).isEmpty();
        }
    }

    @Nested
    class DetectFormatWithSpecificCharset {

        @Test
        void shouldDetectJson() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(JSON_US_ASCII_CHARSET, US_ASCII);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.JSON);
        }

        @Test
        void shouldDetectXml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(XML_US_ASCII_CHARSET, US_ASCII);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.XML);
        }

        @Test
        void shouldDetectYaml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(YAML_FORMAL_US_ASCII_CHARSET, US_ASCII);
            assertThat(optionalFormat).hasValue(JacksonDataFormat.YAML);
        }

        @Test
        void shouldNotDetectInformalYaml() {
            var optionalFormat = KiwiJacksonDataFormats.detectFormat(YAML_INFORMAL_US_ASCII_CHARSET, US_ASCII);
            assertThat(optionalFormat).isEmpty();
        }
    }

    @Nested
    class DetectFormatUsingCustomDetector {

        @Test
        void shouldReturnEmptyOptional_WhenIOExceptionIsThrown() throws IOException {
            var dataFormatDetector = mock(DataFormatDetector.class);
            when(dataFormatDetector.findFormat(any(byte[].class))).thenThrow(new IOException("Danger! Error detecting!"));

            var text = "some text";
            var optionalFormatName = KiwiJacksonDataFormats.detectFormat(text, UTF_8, dataFormatDetector);
            assertThat(optionalFormatName).isEmpty();

            verify(dataFormatDetector).findFormat(text.getBytes(UTF_8));
        }

        @Nested
        class CustomDetection {

            private DataFormatDetector csvOnlyFormatDetector;

            @BeforeEach
            void setUp() {
                csvOnlyFormatDetector = new DataFormatDetector(new CsvFactory());
            }

            @Test
            void shouldDetectFormats_SupportedBy_ProvidedDataFormatDetector() {
                var sampleCsv = fixtureWithCharset("KiwiJacksonDataFormatsTest/sample.csv", UTF_8);

                assertThat(KiwiJacksonDataFormats.detectFormat(sampleCsv, UTF_8, csvOnlyFormatDetector))
                        .hasValue(CsvFactory.FORMAT_NAME_CSV);
            }

            @ParameterizedTest
            @MethodSource("org.kiwiproject.jackson.KiwiJacksonDataFormatsTest#utf8ContentStream")
            void shouldNotDetectFormats_ThatAreNotSupportedBy_CustomDetector(String content) {
                assertThat(KiwiJacksonDataFormats.detectFormat(content, UTF_8, csvOnlyFormatDetector))
                        .isEmpty();
            }
        }
    }

    static Stream<String> utf8ContentStream() {
        return Stream.of(
                JSON_UTF8_CHARSET,
                XML_UTF8_CHARSET,
                YAML_FORMAL_UTF8_CHARSET
        );
    }

    /**
     * "Stolen" from {@link io.dropwizard.testing.FixtureHelpers} since they made it private there for whatever
     * reason! Renamed so as not to collide with the fixture method.
     */
    @SuppressWarnings("UnstableApiUsage")
    private static String fixtureWithCharset(String filename, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(filename), charset).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}