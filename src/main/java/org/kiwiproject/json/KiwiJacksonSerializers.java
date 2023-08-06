package org.kiwiproject.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Custom Jackson serializers.
 * <p>
 * Jackson databind must be available at runtime.
 */
@UtilityClass
public class KiwiJacksonSerializers {

    /**
     * Build a new {@link SimpleModule} that will replace the values of specific fields with a "masked" value
     * and will replace any exceptions with a message indicating the field could not be serialized.
     * <p>
     * Uses the default replacement text provided by {@link PropertyMaskingOptions}.
     *
     * @param maskedFieldRegexps list containing regular expressions that define the properties to mask
     * @return a new {@link SimpleModule}
     */
    public static SimpleModule buildPropertyMaskingSafeSerializerModule(List<String> maskedFieldRegexps) {
        var options = PropertyMaskingOptions.builder()
                .maskedFieldRegexps(maskedFieldRegexps)
                .build();

        return buildPropertyMaskingSafeSerializerModule(options);
    }

    /**
     * Build a new {@link SimpleModule} that will replace the values of specific fields with a "masked" value
     * and will replace any exceptions with a message indicating the field could not be serialized.
     *
     * @param options the specific masking and serialization error options to use
     * @return a new {@link SimpleModule}
     * @implNote Per the docs for {@link BeanSerializerModifier#changeProperties(SerializationConfig, BeanDescription, List)}
     * the returned list is mutable.
     */
    public static SimpleModule buildPropertyMaskingSafeSerializerModule(PropertyMaskingOptions options) {
        var modifier = new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc,
                                                             List<BeanPropertyWriter> beanProperties) {
                return beanProperties
                        .stream()
                        .map(beanPropertyWriter ->
                                new PropertyMaskingSafePropertyWriter(beanPropertyWriter, options))
                        .map(BeanPropertyWriter.class::cast)
                        .toList();
            }
        };

        return new SimpleModule().setSerializerModifier(modifier);
    }
}
