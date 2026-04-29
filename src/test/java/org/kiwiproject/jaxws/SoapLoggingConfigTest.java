package org.kiwiproject.jaxws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.yaml.YamlHelper;
import org.slf4j.event.Level;

@DisplayName("SoapLoggingConfig")
public class SoapLoggingConfigTest {

    @Test
    void shouldHaveDefaultValues() {
        var config = new SoapLoggingConfig();

        assertAll(
            () -> assertThat(config.isEnabled()).isFalse(),
            () -> assertThat(config.getLogLevel()).isEqualTo(Level.DEBUG)
        );
    }

    @Test
    void shouldDeserializeFromYaml() {
        var yaml = """
                ---
                name: testApp
                soapLogging:
                  enabled: true
                  logLevel: INFO
                """;
        
        var yamlHelper = new YamlHelper();
        var appConfig = yamlHelper.toObject(yaml, AppConfig.class);
        var soapLoggingConfig = appConfig.getSoapLoggingConfig();

        assertAll(
            () -> assertThat(soapLoggingConfig.isEnabled()).isTrue(),
            () -> assertThat(soapLoggingConfig.getLogLevel()).isEqualTo(Level.INFO)
        );
    }

    @Test
    void shouldUseDefaultLogLevelWhenNotSpecifiedInYaml() {
        var yaml = """
                ---
                name: myTestApp
                soapLogging:
                    enabled: true
                """;

        var appConfig = new YamlHelper().toObject(yaml, AppConfig.class);
        var soapLoggingConfig = appConfig.getSoapLoggingConfig();

        assertAll(
            () -> assertThat(soapLoggingConfig.isEnabled()).isTrue(),
            () -> assertThat(soapLoggingConfig.getLogLevel()).isEqualTo(Level.DEBUG)
        );
    }

    @Getter
    @Setter
    static class AppConfig {
        private String name;

        @JsonProperty("soapLogging")
        private SoapLoggingConfig soapLoggingConfig = new SoapLoggingConfig();
    }
}
