package org.kiwiproject.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import jakarta.validation.Validator;
import lombok.experimental.UtilityClass;
import org.kiwiproject.internal.Fixtures;
import org.yaml.snakeyaml.Yaml;

@UtilityClass
public class YamlTestHelper {

    private static final Validator VALIDATOR = Validators.newValidator();
    private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();

    /**
     * Deserialize YAML using <a href="https://bitbucket.org/snakeyaml/snakeyaml/">SnakeYAML</a>.
     */
    public static <T> T loadFromYamlWithSnakeYaml(String filename, Class<T> clazz) {
        var yaml = new Yaml();
        var yamlConfig = Fixtures.fixture(filename);
        return yaml.loadAs(yamlConfig, clazz);
    }

    /**
     * Deserialize YAML using the same mechanism <a href="https://www.dropwizard.io/">Dropwizard</a>
     * uses when starting an application. Under the covers, Dropwizard uses
     * <a href="https://github.com/FasterXML/jackson-dataformats-text">Jackson</a>.
     */
    public static <T> T loadFromYamlWithDropwizard(String filename, Class<T> clazz) {
        var factory = new YamlConfigurationFactory<>(clazz, VALIDATOR, OBJECT_MAPPER, "dw");
        try {
            return factory.build(new ResourceConfigurationSourceProvider(), filename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize YAML using <a href="https://github.com/FasterXML/jackson-dataformats-text">Jackson</a>.
     */
    public static <T> T loadFromYamlWithJackson(String filename, Class<T> clazz) {
        var yamlFactory = new YAMLFactory();
        var objectMapper = new ObjectMapper(yamlFactory);
        var yamlConfig = Fixtures.fixture(filename);
        try {
            return objectMapper.readValue(yamlConfig, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
