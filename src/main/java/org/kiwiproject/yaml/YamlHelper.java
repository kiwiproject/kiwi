package org.kiwiproject.yaml;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.jackson.KiwiTypeReferences.MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.util.List;
import java.util.Map;

/**
 * Some utilities to make it easy to work with YAML.
 *
 * @implNote This uses Jackson to perform YAML operations, which in turn uses SnakeYAML, so both of those must
 * be available at runtime.
 */
public class YamlHelper {

    private final ObjectMapper objectMapper;

    /**
     * Create a new instance using an {@link ObjectMapper} created with a {@link YAMLFactory} to support YAML.
     * In addition, the {@link YAMLFactory} <strong>disables</strong> the
     * {@link YAMLGenerator.Feature#SPLIT_LINES SPLIT_LINES} feature.
     */
    public YamlHelper() {
        this(new ObjectMapper(newYAMLFactory()));
    }

    private static YAMLFactory newYAMLFactory() {
        return new YAMLFactory().disable(YAMLGenerator.Feature.SPLIT_LINES);
    }

    /**
     * Create a new instance using the given {@link ObjectMapper}, which <strong>must</strong> support the YAML
     * format. Otherwise, it can be configured however you want.
     *
     * @param objectMapper the {@link ObjectMapper} to use
     * @throws IllegalArgumentException if the object mapper is null or does not support YAML
     * @see YAMLFactory#getFormatName()
     * @see YAMLFactory#FORMAT_NAME_YAML
     */
    public YamlHelper(ObjectMapper objectMapper) {
        checkArgumentNotNull(objectMapper, "objectMapper cannot be null");
        var supportedFormat = objectMapper.getFactory().getFormatName();
        checkArgument(YAMLFactory.FORMAT_NAME_YAML.equals(supportedFormat), "ObjectMapper does not support YAML");
        this.objectMapper = objectMapper;
    }

    /**
     * Convert the given object to YAML.
     *
     * @param object the object to convert
     * @return a YAML representation of the given object
     */
    public String toYaml(Object object) {
        return toYaml(object, null);
    }

    /**
     * Convert the given object to YAML using the given {@link JsonView}.
     *
     * @param object   the object to convert
     * @param yamlView the nullable {@link JsonView} class
     * @return a YAML representation of the given object
     */
    public String toYaml(Object object, Class<?> yamlView) {
        var writer = objectMapper.writer();

        if (nonNull(yamlView)) {
            writer = writer.withView(yamlView);
        }

        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeYamlException(e);
        }
    }

    /**
     * Convert the given YAML into the specified type.
     *
     * @param yaml        the YAML content
     * @param targetClass the type of obejct to convert into
     * @param <T>         the object type
     * @return a new instance of the given type
     * @throws IllegalArgumentException if the YAML is blank or null
     */
    public <T> T toObject(String yaml, Class<T> targetClass) {
        checkYamlNotBlank(yaml);
        try {
            return objectMapper.readValue(yaml, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeYamlException(e);
        }
    }

    /**
     * Convert the given YAML into an object of type {@code T} using the given {@link TypeReference}.
     *
     * @param yaml       the YAML content
     * @param targetType the {@link TypeReference} representing the target object type
     * @param <T>        the object type
     * @return a new instance of the given type reference
     * @throws IllegalArgumentException if the YAML is blank or null
     */
    public <T> T toObject(String yaml, TypeReference<T> targetType) {
        checkYamlNotBlank(yaml);
        try {
            return objectMapper.readValue(yaml, targetType);
        } catch (JsonProcessingException e) {
            throw new RuntimeYamlException(e);
        }
    }

    /**
     * Convert the given YAML into a List of objects of type {@code T}.
     *
     * @param yaml           the YAML content
     * @param targetListType the {@link TypeReference} representing the target object type
     * @param <T>            the object type
     * @return a list containing objects of the given type
     * @throws IllegalArgumentException if the YAML is blank or null
     */
    public <T> List<T> toObjectList(String yaml, TypeReference<List<T>> targetListType) {
        checkYamlNotBlank(yaml);
        return toObject(yaml, targetListType);
    }

    /**
     * Convert the given YAML into a map with String keys and Object values.
     *
     * @param yaml the YAML content
     * @return the parsed map
     * @throws IllegalArgumentException if the YAML is blank or null
     */
    public Map<String, Object> toMap(String yaml) {
        return toMap(yaml, MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);
    }

    /**
     * Convert the given YAML into a map with keys of type {@code K} and values of type {@code V}.
     *
     * @param yaml          the YAML content
     * @param targetMapType the {@link TypeReference} representing the target map type
     * @param <K>           the type of keys in the map
     * @param <V>           the type of values in the map
     * @return the parsed map
     * @throws IllegalArgumentException if the YAML is blank or null
     */
    public <K, V> Map<K, V> toMap(String yaml, TypeReference<Map<K, V>> targetMapType) {
        checkYamlNotBlank(yaml);
        try {
            return objectMapper.readValue(yaml, targetMapType);
        } catch (JsonProcessingException e) {
            throw new RuntimeYamlException(e);
        }
    }

    public void checkYamlNotBlank(String yaml) {
        checkArgumentNotBlank(yaml, "yaml cannot be blank");
    }
}
