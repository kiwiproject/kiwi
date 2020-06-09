package org.kiwiproject.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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