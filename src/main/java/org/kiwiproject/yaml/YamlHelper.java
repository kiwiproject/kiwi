package org.kiwiproject.yaml;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.yaml.YamlHelper.YamlFormat.DEFAULT;
import static org.kiwiproject.yaml.YamlHelper.YamlFormat.PRETTY;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * Some utilities to make it easy to work with YAML.
 *
 * @implNote This uses Jackson to perform YAML operations, which in turn uses SnakeYAML, so both of those must
 * be available at runtime.
 */
public class YamlHelper {

    private final ObjectMapper objectMapper;

    /**
     * Represents an output format when serializing an object to YAML.
     *
     * @implNote At present Jackson seems to format the same whether using a pretty printer or not
     * for YAML content. Leaving this here in case Jackson changes its behavior in the future.
     */
    public enum YamlFormat {

        /**
         * YAML may or may not be formatted.
         */
        DEFAULT,

        /**
         * YAML will be formatted nicely. Using this will tell the ObjectMapper to use the default pretty printer.
         */
        PRETTY
    }

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
     * format. Otherwise it can be configured however you want.
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
     * Convert the given object to YAML using the {@link YamlFormat#DEFAULT DEFAULT} format.
     *
     * @param object the object to convert
     * @return a YAML representation of the given object
     */
    public String toYaml(Object object) {
        return toYaml(object, DEFAULT, null);
    }

    /**
     * Convert the given object to YAML using the given format.
     *
     * @param object the object to convert
     * @param format the format to use
     * @return a YAML representation of the given object
     */
    public String toYaml(Object object, YamlFormat format) {
        return toYaml(object, format, null);
    }

    /**
     * Convert the given object to YAML using the given {@link JsonView}.
     *
     * @param object   the object to convert
     * @param yamlView the nullable {@link JsonView} class
     * @return a YAML representation of the given object
     */
    public String toYaml(Object object, Class<?> yamlView) {
        return toYaml(object, DEFAULT, yamlView);
    }

    /**
     * Convert the given object to YAML using the given format and {@link JsonView}.
     *
     * @param object   the object to convert
     * @param format   the format to use
     * @param yamlView the nullable {@link JsonView} class
     * @return a YAML representation of the given object
     */
    public String toYaml(Object object, YamlFormat format, Class<?> yamlView) {
        var writer = objectMapper.writer();

        if (nonNull(yamlView)) {
            writer = writer.withView(yamlView);
        }

        if (PRETTY == format) {
            writer = writer.withDefaultPrettyPrinter();
        }

        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeYamlException(e);
        }
    }
}
