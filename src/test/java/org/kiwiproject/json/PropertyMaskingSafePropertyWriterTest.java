package org.kiwiproject.json;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.junit.jupiter.ClearBoxTest;

import java.io.IOException;
import java.util.List;

@DisplayName("PropertyMaskingSafePropertyWriter")
class PropertyMaskingSafePropertyWriterTest {

    @Test
    void shouldMaskConfiguredPropertyValues() {
        var jsonHelper = JsonHelper.newDropwizardJsonHelper();

        jsonHelper.getObjectMapper()
                .registerModule(KiwiJacksonSerializers.buildPropertyMaskingSafeSerializerModule(List.of(".*password.*")));

        var properties = jsonHelper.convertToMap(new SampleUserObject());

        assertThat(properties).containsOnly(
                entry("email", "bob@example.com"),
                entry("username", "bob"),
                entry("password", "********"),
                entry("passwordConfirmation", "********"),
                entry("confirmationPassword", "********")
        );
    }

    @Test
    void shouldUseCustomMaskReplacementText() {
        var jsonHelper = JsonHelper.newDropwizardJsonHelper();

        var maskText = "xxxxxxxxxx";

        var options = PropertyMaskingOptions.builder()
                .maskedFieldRegexps(List.of(".*password.*"))
                .maskedFieldReplacementText(maskText)
                .build();

        jsonHelper.getObjectMapper()
                .registerModule(KiwiJacksonSerializers.buildPropertyMaskingSafeSerializerModule(options));

        var properties = jsonHelper.convertToMap(new SampleUserObject());

        assertThat(properties).containsOnly(
                entry("email", "bob@example.com"),
                entry("username", "bob"),
                entry("password", maskText),
                entry("passwordConfirmation", maskText),
                entry("confirmationPassword", maskText)
        );
    }

    @Test
    void shouldWriteWarningMessageInsteadOfThrowingExceptions() {
        var jsonHelper = JsonHelper.newDropwizardJsonHelper();

        jsonHelper.getObjectMapper()
                .registerModule(KiwiJacksonSerializers.buildPropertyMaskingSafeSerializerModule(List.of()));

        var properties = jsonHelper.convertToMap(new SampleExceptionThrowingObject());

        assertThat(properties).containsOnly(
                entry("normalProperty", "string-value"),
                entry("badProperty", "(unable to serialize field)"),
                entry("anotherProperty", 1)
        );
    }

    @Test
    void shouldUseCustomSerializationErrorText() {
        var jsonHelper = JsonHelper.newDropwizardJsonHelper();

        var errorText = "serializationError";

        var options = PropertyMaskingOptions.builder()
                .serializationErrorReplacementText(errorText)
                .build();

        jsonHelper.getObjectMapper()
                .registerModule(KiwiJacksonSerializers.buildPropertyMaskingSafeSerializerModule(options));

        var properties = jsonHelper.convertToMap(new SampleExceptionThrowingObject());

        assertThat(properties).containsOnly(
                entry("normalProperty", "string-value"),
                entry("badProperty", errorText),
                entry("anotherProperty", 1)
        );
    }

    @Test
    void shouldAllowNullsForReplacementText() {
        var jsonHelper = JsonHelper.newDropwizardJsonHelper();

        var options = PropertyMaskingOptions.builder()
                .maskedFieldRegexps(List.of("anotherProperty"))
                .maskedFieldReplacementText(null)
                .serializationErrorReplacementText(null)
                .build();

        jsonHelper.getObjectMapper()
                .registerModule(KiwiJacksonSerializers.buildPropertyMaskingSafeSerializerModule(options));

        var properties = jsonHelper.convertToMap(new SampleExceptionThrowingObject());

        System.out.println(jsonHelper.toJson(new SampleExceptionThrowingObject()));

        assertThat(properties).containsOnly(
                entry("normalProperty", "string-value"),
                entry("badProperty", null),
                entry("anotherProperty", null)
        );
    }

    /**
     * This test illustrates that masking writes strings in places of the actual values, which could be a different
     * type than the source object. In the source object, SampleSecretAgent, secretNumber is an int and secretIdentities
     * is a List of String. But in the serialized JSON, these are masked and have type String. So be careful when
     * using masking on non-String fields.
     */
    @Test
    void shouldWriteStringsToJsonInsteadOfSourceTypeWhenMasking() {
        var jsonHelper = JsonHelper.newDropwizardJsonHelper();

        jsonHelper.getObjectMapper()
                .registerModule(KiwiJacksonSerializers.buildPropertyMaskingSafeSerializerModule(List.of(".*secret.*")));

        var properties = jsonHelper.convertToMap(new SampleSecretAgent());

        assertThat(properties).containsOnly(
                entry("codeName", "007"),
                entry("secretNumber", "********"),
                entry("secretIdentities", "********")
        );
    }

    @Test
    void shouldUseDefaultTextReplacementOptions_WhenConstructedUsingOnlyList() {
        var maskedFields = List.of("secretNumber", "secretIdentities");
        var modifier = newBeanSerializerModifier(maskedFields);

        var module = new SimpleModule().setSerializerModifier(modifier);
        var mapper = JsonHelper.newDropwizardObjectMapper().registerModule(module);
        var jsonHelper = new JsonHelper(mapper);
        var properties = jsonHelper.convertToMap(new SampleSecretAgent());

        var defaultOptions = PropertyMaskingOptions.builder().build();
        var maskText = defaultOptions.getMaskedFieldReplacementText();
        assertThat(properties).containsOnly(
                entry("codeName", "007"),
                entry("secretNumber", maskText),
                entry("secretIdentities", maskText)
        );
    }

    private static BeanSerializerModifier newBeanSerializerModifier(List<String> maskedFields) {
        return new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc,
                                                             List<BeanPropertyWriter> beanProperties) {
                return beanProperties.stream()
                        .map(beanPropertyWriter ->
                                new PropertyMaskingSafePropertyWriter(beanPropertyWriter, maskedFields))
                        .collect(toList());
            }
        };
    }

    @ClearBoxTest
    void shouldCatchExceptionsThrownWritingReplacementText() throws IOException {
        var jsonGenerator = mock(JsonGenerator.class);
        doThrow(new IOException("oops"))
                .when(jsonGenerator)
                .writeString(anyString());

        assertThatCode(() ->
                PropertyMaskingSafePropertyWriter.writeReplacementText(jsonGenerator, "someProperty", "****"))
                .doesNotThrowAnyException();
    }

    @SuppressWarnings("unused")
    static class SampleUserObject {

        public String getEmail() {
            return "bob@example.com";
        }

        public String getUsername() {
            return "bob";
        }

        public String getPassword() {
            return "secret";
        }

        public String getPasswordConfirmation() {
            return "secret";
        }

        public String getConfirmationPassword() {
            return "secret";
        }
    }

    @SuppressWarnings("unused")
    static class SampleExceptionThrowingObject {

        public String getNormalProperty() {
            return "string-value";
        }

        public Double getBadProperty() {
            throw new IllegalStateException("bad object!");
        }

        public int getAnotherProperty() {
            return 1;
        }
    }

    @SuppressWarnings("unused")
    static class SampleSecretAgent {

        public String getCodeName() {
            return "007";
        }

        public int getSecretNumber() {
            return 42;
        }

        public List<String> getSecretIdentities() {
            return List.of("Bond", "James Bond");
        }
    }
}
