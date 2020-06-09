package org.kiwiproject.json;

import static java.util.stream.Collectors.toList;

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
     *
     * @param maskedFieldRegexps list containing regular expressions that define the properties to mask
     * @return a new {@link SimpleModule}
     */
    public static SimpleModule buildPropertyMaskingSafeSerializerModule(List<String> maskedFieldRegexps) {
        var modifier = new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc,
                                                             List<BeanPropertyWriter> beanProperties) {
                return beanProperties.stream()
                        .map(beanPropertyWriter ->
                                new PropertyMaskingSafePropertyWriter(beanPropertyWriter, maskedFieldRegexps))
                        .collect(toList());
            }
        };

        return new SimpleModule().setSerializerModifier(modifier);
    }
}
