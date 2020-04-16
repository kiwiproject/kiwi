package org.kiwiproject.util;

import io.dropwizard.testing.FixtureHelpers;
import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;

@UtilityClass
public class YamlTestHelper {

    public static <T> T loadFromYaml(String filename, Class<T> clazz) {
        var yaml = new Yaml();
        var yamlConfig = FixtureHelpers.fixture(filename);
        return yaml.loadAs(yamlConfig, clazz);
    }
}
