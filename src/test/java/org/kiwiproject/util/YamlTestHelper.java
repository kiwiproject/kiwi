package org.kiwiproject.util;

import lombok.experimental.UtilityClass;
import org.kiwiproject.internal.Fixtures;
import org.yaml.snakeyaml.Yaml;

@UtilityClass
public class YamlTestHelper {

    public static <T> T loadFromYaml(String filename, Class<T> clazz) {
        var yaml = new Yaml();
        var yamlConfig = Fixtures.fixture(filename);
        return yaml.loadAs(yamlConfig, clazz);
    }
}
