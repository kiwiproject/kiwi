package org.kiwiproject.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

@DisplayName("JaxbElementSerializer")
class JaxbElementSerializerTest {

    private JsonHelper jsonHelper;

    @BeforeEach
    void setUp() {
        var mapper = newObjectMapperWithJaxbElementSerializer();
        jsonHelper = new JsonHelper(mapper);
    }

    @Test
    void shouldSerializeNilObjectField() {
        var json = jsonHelper.toJson(newXmlTestObject());

        assertThat(json).contains("\"wrappedObject\":null");
    }

    @Test
    void shouldSerializeNonNilObjectField() {
        var testObject = newXmlTestObject();
        testObject.setWrappedObject(
                new JAXBElement<>(new QName(""),
                        XmlTestInnerObject.class,
                        new XmlTestInnerObject("foo")));

        var json = jsonHelper.toJson(testObject);

        assertThat(json).contains("\"wrappedObject\":{\"innerField\":\"foo\"}");
    }

    @Test
    void shouldSerializeNilStringField() {
        var json = jsonHelper.toJson(newXmlTestObject());

        assertThat(json).contains("\"wrappedString\":null");
    }

    @Test
    void shouldSerializeNonNilStringField() {
        var testObject = newXmlTestObject();
        testObject.setWrappedString(
                new JAXBElement<>(new QName(""),
                        String.class,
                        "foo"));

        var json = jsonHelper.toJson(testObject);

        assertThat(json).contains("\"wrappedString\":\"foo\"");
    }

    private static ObjectMapper newObjectMapperWithJaxbElementSerializer() {
        var mapper = JsonHelper.newDropwizardObjectMapper();
        var jaxbModule = new SimpleModule().addSerializer(JAXBElement.class, new JaxbElementSerializer());
        mapper.registerModule(jaxbModule);

        return mapper;
    }

    private static XmlTestObject newXmlTestObject() {
        var obj = new XmlTestObject();
        obj.setStringField("test");
        obj.setIntegerField(1);
        obj.setWrappedObject(new JAXBElement<>(new QName(""), XmlTestInnerObject.class, null));
        obj.setWrappedString(new JAXBElement<>(new QName(""), String.class, null));
        return obj;
    }

    @Getter
    @Setter
    @XmlRootElement
    private static class XmlTestObject {

        @XmlElement
        private String stringField;

        @XmlElement
        private Integer integerField;

        @XmlElementRef(required = false)
        private JAXBElement<String> wrappedString;

        @XmlElementRef(required = false)
        private JAXBElement<XmlTestInnerObject> wrappedObject;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement
    private static class XmlTestInnerObject {

        @XmlElement
        private String innerField;
    }
}