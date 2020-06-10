package org.kiwiproject.json;

import static java.util.stream.Collectors.toUnmodifiableList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Writes properties "safely" and masks sensitive properties such as passwords. This property writer will attempt
 * to write out the value, but if an exception is thrown, it will instead write a pre-configured value in place
 * of the actual value.
 * <p>
 * This writer also allows masking (hiding the true value) of certain fields based on their name by writing out
 * asterisks instead of the true value.
 * <p>
 * NOTE: Generally masking should be used only on String fields, because otherwise the type in the resulting JSON will
 * be different than the source type. For example, if a class has a "secretNumber" of type "int" and it is masked, the
 * resulting JSON contains a String instead of an int, which will likely cause problems if a downstream system reads
 * the JSON expecting an int. For such cases, consider using {@link com.fasterxml.jackson.annotation.JsonView} instead.
 * <p>
 * Note that jackson-core and jackson-databind must be available at runtime.
 */
@Slf4j
public class PropertyMaskingSafePropertyWriter extends BeanPropertyWriter {

    private final List<Pattern> hiddenFieldPatterns;
    private final String maskedFieldReplacementText;
    private final String serializationErrorReplacementText;

    /**
     * Construct new instance wrapping the given {@link BeanPropertyWriter} using the given list of (String)
     * regular expressions that define the properties which should be masked.
     * <p>
     * Default values for masked fields and serialization errors are used. The default values are
     * defined by {@link PropertyMaskingOptions}.
     *
     * @param base               the base or delegate {@link BeanPropertyWriter} to use
     * @param maskedFieldRegexps list containing regular expressions that define the properties to mask
     */
    public PropertyMaskingSafePropertyWriter(BeanPropertyWriter base, List<String> maskedFieldRegexps) {
        this(base, PropertyMaskingOptions.builder()
                .maskedFieldRegexps(maskedFieldRegexps)
                .build());
    }

    /**
     * Construct new instance wrapping the given {@link BeanPropertyWriter} using the given
     * {@link PropertyMaskingOptions} to define properties to be masked, as well as replacement text for
     * masked fields and serialization errors.
     *
     * @param base    the base or delegate {@link BeanPropertyWriter} to use
     * @param options the options to use
     * @see PropertyMaskingOptions
     */
    public PropertyMaskingSafePropertyWriter(BeanPropertyWriter base, PropertyMaskingOptions options) {
        super(base);
        this.hiddenFieldPatterns = convertToPatterns(options.getMaskedFieldRegexps());
        this.maskedFieldReplacementText = options.getMaskedFieldReplacementText();
        this.serializationErrorReplacementText = options.getSerializationErrorReplacementText();
    }

    private static List<Pattern> convertToPatterns(List<String> maskedFieldRegexps) {
        return maskedFieldRegexps.stream()
                .filter(Objects::nonNull)
                .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE))
                .collect(toUnmodifiableList());
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) {
        var propertyName = getName();
        try {
            LOG.trace("Using custom serializer for field: {}", propertyName);
            if (matchesExclusionPatterns(propertyName)) {
                writeReplacementText(gen, propertyName, maskedFieldReplacementText);
            } else {
                super.serializeAsField(bean, gen, prov);
            }
        } catch (Exception e) {
            LOG.debug("Unable to serialize: {}, of {} instance, exception {}: {}",
                    propertyName, bean.getClass().getName(), e.getClass().getName(), e.getMessage());
            LOG.trace("Exception serializing field: {}", propertyName, e);
            writeReplacementText(gen, propertyName, serializationErrorReplacementText);
        }
    }

    private boolean matchesExclusionPatterns(String name) {
        return hiddenFieldPatterns.stream().anyMatch(pattern -> pattern.matcher(name).find());
    }

    @VisibleForTesting
    static void writeReplacementText(JsonGenerator gen, String name, String text) {
        LOG.trace("Setting field '{}' to: {}", name, text);
        try {
            gen.writeFieldName(name);
            gen.writeString(text);
        } catch (Exception e) {
            LOG.error("Failed to serialize replacement value for field: {}", name, e);
        }
    }

}
