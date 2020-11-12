package org.kiwiproject.spring.data;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.bson.BsonUndefined;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * A few utilities related to Spring Data Mongo and custom {@link Converter}s.
 */
@UtilityClass
public class KiwiMongoConverters {

    /**
     * Create a new {@link BsonUndefinedToNullStringConverter}.
     *
     * @return new BsonUndefinedToNullStringConverter instance
     */
    public static Converter<BsonUndefined, String> newBsonUndefinedToNullObjectConverter() {
        return new BsonUndefinedToNullStringConverter();
    }

    /**
     * Adds one or more custom converters to a {@link MongoTemplate} instance.
     * <p>
     * The MongoTemplate is assumed to have a {@link Converter} of type {@link MappingMongoConverter}.
     *
     * @param template   the MongoTemplate
     * @param converters the converters to add to MongoTemplate's existing converter
     * @throws IllegalArgumentException if the given MongoTemplate's {@link Converter} is <em>not</em>
     *                                  a {@link MappingMongoConverter}
     * @see org.springframework.data.mongodb.core.convert.AbstractMongoConverter#setCustomConversions(CustomConversions)
     */
    public static MongoTemplate addCustomConverters(MongoTemplate template, Converter<?, ?>... converters) {
        checkArgument(template.getConverter() instanceof MappingMongoConverter,
                "Currently a MappingMongoConverter is required as the MongoTemplate's converter");

        var converter = (MappingMongoConverter) template.getConverter();
        converter.setCustomConversions(new MongoCustomConversions(Lists.newArrayList(converters)));
        converter.afterPropertiesSet();

        return template;
    }

    /**
     * A {@link Converter} that maps from the JS 'undefined' type to a 'null' value.
     */
    public static class BsonUndefinedToNullStringConverter implements Converter<BsonUndefined, String> {

        @SuppressWarnings("NullableProblems")
        @Override
        public String convert(BsonUndefined bsonUndefined) {
            return null;
        }
    }
}
