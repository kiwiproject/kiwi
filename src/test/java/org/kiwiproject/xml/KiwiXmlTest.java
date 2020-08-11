package org.kiwiproject.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kiwiproject.collect.KiwiMaps;
import org.kiwiproject.internal.Fixtures;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@DisplayName("KiwiXml")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiXmlTest {

    @Nested
    class CachedJAXBContext {

        @BeforeEach
        void setUp() {
            KiwiXml.toXml(new AnnotatedThing("42", "type-1"));
            KiwiXml.toXml(new DifferentAnnotatedThing("42", "type-1"));
            KiwiXml.toXml(new NotAnnotatedThing("42", "type-1"));
            KiwiXml.toXml(new OneMoreAnnotatedThing("42", "type-1"));
        }

        @Test
        void shouldGetCachedClasses() {
            assertThat(KiwiXml.getCachedJAXBContextClasses()).containsExactlyInAnyOrder(
                    AnnotatedThing.class,
                    DifferentAnnotatedThing.class,
                    NotAnnotatedThing.class,
                    OneMoreAnnotatedThing.class
            );
        }

        @Test
        void shouldClearCachedClasses() {
            KiwiXml.clearCachedJAXBContextClasses();

            assertThat(KiwiXml.getCachedJAXBContextClasses()).isEmpty();
        }
    }

    @Nested
    class ToXml {

        @Test
        void shouldNotAllowNullArguments(SoftAssertions softly) {
            softly.assertThatThrownBy(() -> KiwiXml.toXml(null))
                    .isExactlyInstanceOf(IllegalArgumentException.class);

            softly.assertThatThrownBy(() -> KiwiXml.toXml(null, AnnotatedThing.class))
                    .isExactlyInstanceOf(IllegalArgumentException.class);

            softly.assertThatThrownBy(() -> KiwiXml.toXml("...", null))
                    .isExactlyInstanceOf(IllegalArgumentException.class);

            softly.assertThatThrownBy(() -> KiwiXml.toXml(null, AnnotatedThing.class, Map.of()))
                    .isExactlyInstanceOf(IllegalArgumentException.class);

            softly.assertThatThrownBy(() -> KiwiXml.toXml("...", null, Map.of()))
                    .isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldConvertAnnotatedObjectWithNoClass() {
            var annotatedThing = new AnnotatedThing("some-id", "some-type");
            var xml = KiwiXml.toXml(annotatedThing);

            assertAnnotatedThingXml(xml);
        }

        @Test
        void shouldConvertAnnotatedObjectWithClass() {
            var annotatedThing = new AnnotatedThing("some-id", "some-type");
            var xml = KiwiXml.toXml(annotatedThing, AnnotatedThing.class);

            assertAnnotatedThingXml(xml);
        }

        @Test
        void shouldThrowWhenExceptionThrownMarshallingToXml() {
            var thing = new OneMoreAnnotatedThing("an-id", "BOOM");

            assertThatThrownBy(() -> KiwiXml.toXml(thing, OneMoreAnnotatedThing.class))
                    .isExactlyInstanceOf(XmlRuntimeException.class)
                    .hasMessage("Unable to convert to XML")
                    .hasRootCauseExactlyInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("Invalid type: BOOM");
        }

        @Test
        void shouldConvertAnnotatedObjectWithMarshallerProperties() {
            var annotatedThing = new AnnotatedThing("some-id", "some-type");
            var properties = Map.<String, Object>of(
                    Marshaller.JAXB_FORMATTED_OUTPUT, true,
                    Marshaller.JAXB_SCHEMA_LOCATION, "http://my-schema-location/"
            );
            var xml = KiwiXml.toXml(annotatedThing, AnnotatedThing.class, properties);

            assertAnnotatedThingXml(xml);
            assertThat(xml).contains("xsi:schemaLocation=\"http://my-schema-location/\"");
        }

        @Test
        void shouldThrowWhenInvalidMarshallerProperties() {
            var annotatedThing = new AnnotatedThing("some-id", "some-type");
            var properties = KiwiMaps.<String, Object>newHashMap(
                    Marshaller.JAXB_FORMATTED_OUTPUT, true,
                    Marshaller.JAXB_SCHEMA_LOCATION, null  /* invalid schema location */
            );

            assertThatThrownBy(() -> KiwiXml.toXml(annotatedThing, AnnotatedThing.class, properties))
                    .isExactlyInstanceOf(XmlRuntimeException.class)
                    .hasMessage("Unable to configure marshaller properties")
                    .hasCauseExactlyInstanceOf(PropertyException.class);
        }

        @Test
        void shouldAllowNullMarshallerProperties() {
            var annotatedThing = new AnnotatedThing("some-id", "some-type");
            var xml = KiwiXml.toXml(annotatedThing, AnnotatedThing.class, null);

            assertAnnotatedThingXml(xml);
        }

        private void assertAnnotatedThingXml(String xml) {
            assertThat(xml)
                    .containsPattern("<\\w+:AnnotatedThing")
                    .containsPattern("xsi:type=\"[\\w]+:AnnotatedThing\"")
                    .containsPattern("xmlns:[\\w]+=\"urn:org:kiwiproject:xml\"")
                    .contains("<id>some-id</id>", "<type>some-type</type>")
                    .containsPattern("</\\w+:AnnotatedThing>");
        }

        @Test
        void shouldRoundTripFromXmlToObjectAndBack() {
            var annotatedThing = new AnnotatedThing("some-id", "some-type");
            var xml = KiwiXml.toXml(annotatedThing, AnnotatedThing.class);

            var unmarshalled = KiwiXml.toObject(xml, AnnotatedThing.class);

            assertThat(unmarshalled).isEqualTo(annotatedThing);
        }

        @Test
        void shouldConvertNonAnnotatedObject() {
            var notAnnotatedThing = new NotAnnotatedThing("some-id", "some-type");
            var xml = KiwiXml.toXml(notAnnotatedThing);

            assertThat(xml)
                    .contains("<NotAnnotatedThing")
                    .contains("<id>some-id</id>", "<type>some-type</type>")
                    .contains("</NotAnnotatedThing>");
        }
    }

    @Nested
    class ToObject {

        @Test
        void shouldNotAllowNullArguments(SoftAssertions softly) {
            softly.assertThatThrownBy(() -> KiwiXml.toObject(null, AnnotatedThing.class))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("xml cannot be blank");

            softly.assertThatThrownBy(() -> KiwiXml.toObject("...", null))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("clazz cannot be null");
        }

        @Test
        void shouldNotAllowBlankXmlArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXml.toObject("", AnnotatedThing.class))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .withMessage("xml cannot be blank");
        }

        @Test
        void shouldConvertToAnnotatedClass() {
            var xml = Fixtures.fixture("KiwiXmlTest/annotatedThing.xml");

            var thing = KiwiXml.toObject(xml, AnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("some-id");
            assertThat(thing.getType()).isEqualTo("some-type");
        }

        @Test
        void shouldConvertToNotAnnotatedClass() {
            var xml = Fixtures.fixture("KiwiXmlTest/notAnnotatedThing.xml");

            var thing = KiwiXml.toObject(xml, NotAnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("some-id");
            assertThat(thing.getType()).isEqualTo("some-type");
        }

        @Test
        void shouldConvertToAnnotatedClass_WhenNoNamespaceInformation() {
            var xml = "<root><id>42</id><type>answer</type></root>";

            var thing = KiwiXml.toObject(xml, AnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("42");
            assertThat(thing.getType()).isEqualTo("answer");
        }

        @Test
        void shouldConvertToNotAnnotatedClass_WhenNoNamespaceInformation() {
            var xml = "<root><id>42</id><type>answer</type></root>";

            var thing = KiwiXml.toObject(xml, NotAnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("42");
            assertThat(thing.getType()).isEqualTo("answer");
        }

        @Test
        void shouldConvert_IgnoringComments() {
            var xml = "<!-- comment --><root><!-- another comment --><id>42</id><type>answer</type><!-- one more comment --></root>";

            var thing = KiwiXml.toObject(xml, NotAnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("42");
            assertThat(thing.getType()).isEqualTo("answer");
        }

        @Test
        void shouldThrow_WhenMismatchedNamespace() {
            var xml = "<root xmlns=\"unknown:namespace\"><id>42</id><type>answer</type></root>";

            assertThatThrownBy(() -> KiwiXml.toObject(xml, AnnotatedThing.class))
                    .isExactlyInstanceOf(XmlRuntimeException.class)
                    .hasCauseExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unable to unmarshal XML")
                    .hasRootCauseMessage("XML namespace does not match expected type");
        }

        @Test
        void shouldThrow_WhenGivenMalformedXml() {
            var xml = "<!-- a comment -->\n<root>\n<id>42</wrongEndTag>\n</root>";

            var thrown = catchThrowable(() -> KiwiXml.toObject(xml, NotAnnotatedThing.class));

            assertThat(thrown)
                    .isExactlyInstanceOf(XmlRuntimeException.class)
                    .hasMessage("Unable to unmarshal XML");

            var cause = thrown.getCause();
            assertThat(cause)
                    .isInstanceOf(XMLStreamException.class)
                    .hasMessageContaining("Unexpected close tag </wrongEndTag>");
        }
    }

    @Nested
    class ToObjectIgnoringNamespace {

        @Test
        void shouldConvertToAnnotatedClass_WhenInvalidNamespace() {
            var xml = "<root xmlns=\"unknown:namespace\"><id>42</id><type>answer</type></root>";

            var thing = KiwiXml.toObjectIgnoringNamespace(xml, AnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("42");
            assertThat(thing.getType()).isEqualTo("answer");
        }

        @Test
        void shouldConvertToNotAnnotatedClass_WhenInvalidNamespace() {
            var xml = "<root xmlns=\"unknown:namespace\"><id>42</id><type>answer</type></root>";

            var thing = KiwiXml.toObjectIgnoringNamespace(xml, NotAnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("42");
            assertThat(thing.getType()).isEqualTo("answer");
        }

        @Test
        void shouldConvertToAnnotatedClass() {
            var xml = Fixtures.fixture("KiwiXmlTest/annotatedThing.xml");

            var thing = KiwiXml.toObjectIgnoringNamespace(xml, AnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("some-id");
            assertThat(thing.getType()).isEqualTo("some-type");
        }

        @Test
        void shouldConvertToNotAnnotatedClass() {
            var xml = Fixtures.fixture("KiwiXmlTest/notAnnotatedThing.xml");

            var thing = KiwiXml.toObjectIgnoringNamespace(xml, NotAnnotatedThing.class);
            assertThat(thing.getId()).isEqualTo("some-id");
            assertThat(thing.getType()).isEqualTo("some-type");
        }
    }

    @Nested
    class ToObjectAssertingValid {

        @Test
        void shouldConvertToAnnotatedClass() {
            var xml = Fixtures.fixture("KiwiXmlTest/annotatedThing.xml");

            var thing = KiwiXml.toObjectAssertingValid(xml, AnnotatedThing.class, KiwiXml.NamespaceValidation.YES);
            assertThat(thing.getId()).isEqualTo("some-id");
            assertThat(thing.getType()).isEqualTo("some-type");
        }

        @Test
        void shouldConvertToNotAnnotatedClass() {
            var xml = Fixtures.fixture("KiwiXmlTest/notAnnotatedThing.xml");

            var thing = KiwiXml.toObjectAssertingValid(xml, NotAnnotatedThing.class, KiwiXml.NamespaceValidation.YES);
            assertThat(thing.getId()).isEqualTo("some-id");
            assertThat(thing.getType()).isEqualTo("some-type");
        }

        @Test
        void shouldConvertToAnnotatedClass_WhenNoNamespace_ButIgnoringNamespaceValidation() {
            var xml = "<root><id>42</id><type>answer</type></root>";

            var thing = KiwiXml.toObjectAssertingValid(xml, AnnotatedThing.class, KiwiXml.NamespaceValidation.NO);
            assertThat(thing.getId()).isEqualTo("42");
            assertThat(thing.getType()).isEqualTo("answer");
        }

        @Test
        void shouldConvertToNotAnnotatedClass_WhenNoNamespace_ButIgnoringNamespaceValidation() {
            var xml = "<root><id>42</id><type>answer</type></root>";

            var thing = KiwiXml.toObjectAssertingValid(xml, NotAnnotatedThing.class, KiwiXml.NamespaceValidation.NO);
            assertThat(thing.getId()).isEqualTo("42");
            assertThat(thing.getType()).isEqualTo("answer");
        }
    }

    @ParameterizedTest
    @MethodSource("validateXmlMatchesTypeArgs")
    void shouldValidateXmlMatchesType(String fixtureName, Class<?> clazz, boolean expectedResult) {
        var xml = Fixtures.fixture("KiwiXmlTest/" + fixtureName);
        assertThat(KiwiXml.validateXmlMatchesType(xml, clazz)).isEqualTo(expectedResult);
    }

    static Stream<Arguments> validateXmlMatchesTypeArgs() {
        return Stream.of(
                Arguments.of("annotatedThing.xml", AnnotatedThing.class, true),
                Arguments.of("annotatedThing.xml", NotAnnotatedThing.class, true),
                Arguments.of("notAnnotatedThing.xml", AnnotatedThing.class, true),
                Arguments.of("notAnnotatedThing.xml", NotAnnotatedThing.class, true),
                Arguments.of("annotatedThing.xml", DifferentAnnotatedThing.class, false),
                Arguments.of("differentAnnotatedThing.xml", AnnotatedThing.class, false)
        );
    }

    @ParameterizedTest
    @MethodSource("validateXmlMatchesTypeArgsIgnoringNamespaces")
    void shouldValidateXmlMatchesTypeIgnoringNamespaces(String fixtureName,
                                                        Class<?> clazz,
                                                        String ignoredNamespace,
                                                        boolean expectedResult) {

        var xml = Fixtures.fixture("KiwiXmlTest/" + fixtureName);
        var ignoredNamespaces = List.of(ignoredNamespace);
        assertThat(KiwiXml.validateXmlMatchesType(xml, clazz, ignoredNamespaces)).isEqualTo(expectedResult);
    }

    static Stream<Arguments> validateXmlMatchesTypeArgsIgnoringNamespaces() {
        return Stream.of(
                Arguments.of("annotatedThing.xml", DifferentAnnotatedThing.class, "urn:org:kiwiproject:xml", true),
                Arguments.of("annotatedThing.xml", DifferentAnnotatedThing.class, "urn:org:someotherproject:xml", true),
                Arguments.of("differentAnnotatedThing.xml", AnnotatedThing.class, "urn:org:kiwiproject:xml", true),
                Arguments.of("differentAnnotatedThing.xml", AnnotatedThing.class, "urn:org:someotherproject:xml", true),
                Arguments.of("annotatedThing.xml", DifferentAnnotatedThing.class, "##default", false),
                Arguments.of("differentAnnotatedThing.xml", AnnotatedThing.class, "##default", false)
        );
    }

    @Nested
    class GetNameAndNamespace {

        @Test
        void shouldNotAcceptNullArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXml.getNameAndNamespace(null));
        }

        @Test
        void shouldGetFromAnnotatedClass() {
            var nameAndNamespace = KiwiXml.getNameAndNamespace(AnnotatedThing.class);

            assertThat(nameAndNamespace).containsOnly(
                    entry("namespace", "urn:org:kiwiproject:xml"),
                    entry("name", "AnnotatedThing")
            );
        }

        @Test
        void shouldGetFromNonAnnotatedClass() {
            var nameAndNamespace = KiwiXml.getNameAndNamespace(NotAnnotatedThing.class);

            assertThat(nameAndNamespace).containsOnly(
                    entry("namespace", null),
                    entry("name", "NotAnnotatedThing")
            );
        }
    }

    @Nested
    class StripTags {

        @Test
        void shouldDoNothingWhenNoTagsToRemove() {
            var xml = "<foo><bar>keep-me</bar><baz>keep me too</baz></foo>";

            assertThat(KiwiXml.stripTags(xml)).isEqualTo(xml);
        }

        @Test
        void shouldStripOneTag() {
            var xml = "<foo><bar>remove me</bar><baz>keep me</baz></foo>";

            assertThat(KiwiXml.stripTags(xml, "bar"))
                    .isEqualTo("<foo><baz>keep me</baz></foo>");
        }

        @Test
        void shouldStripMultipleTags() {
            var xml = "<foo><bar>remove me</bar><baz>remove me too</baz></foo>";

            assertThat(KiwiXml.stripTags(xml, "bar", "baz"))
                    .isEqualTo("<foo></foo>");
        }

        @Test
        void shouldStripIgnoringNamespaces() {
            var xml = "<ns1:foo><ns2:bar>bar1</ns2:bar><ns1:baz>baz</ns1:baz></ns1:foo>";

            assertThat(KiwiXml.stripTags(xml, "bar"))
                    .isEqualTo("<ns1:foo><ns1:baz>baz</ns1:baz></ns1:foo>");

            assertThat(KiwiXml.stripTags(xml, "bar", "baz"))
                    .isEqualTo("<ns1:foo></ns1:foo>");
        }

        @Test
        void shouldStripSpecificNamespaces() {
            var xml = "<ns1:foo><ns1:bar>bar1</ns1:bar><ns2:bar>bar2</ns2:bar><ns1:baz>baz</ns1:baz></ns1:foo>";

            assertThat(KiwiXml.stripTagsConsideringNamespace(xml, "ns2:bar"))
                    .isEqualTo("<ns1:foo><ns1:bar>bar1</ns1:bar><ns1:baz>baz</ns1:baz></ns1:foo>");

            assertThat(KiwiXml.stripTagsConsideringNamespace(xml, "ns1:bar", "ns2:bar"))
                    .isEqualTo("<ns1:foo><ns1:baz>baz</ns1:baz></ns1:foo>");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "AnnotatedThing",
            namespace = "urn:org:kiwiproject:xml",
            propOrder = {"id", "type"}
    )
    public static class AnnotatedThing {
        private String id;
        private String type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "AnnotatedThing",
            namespace = "urn:org:someotherproject:xml",
            propOrder = {"id", "type"}
    )
    public static class DifferentAnnotatedThing {
        private String id;
        private String type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(
            name = "OneMoreAnnotatedThing",
            namespace = "urn:org:yetanotherproject:xml",
            propOrder = {"id", "type"}
    )
    public static class OneMoreAnnotatedThing {
        private String id;
        private String type;

        public String getType() {
            if ("BOOM".equals(type)) {
                throw new RuntimeException("Invalid type: " + type);
            }
            return type;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotAnnotatedThing {
        private String id;
        private String type;
    }
}
