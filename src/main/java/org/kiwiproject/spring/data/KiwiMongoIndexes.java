package org.kiwiproject.spring.data;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;

import java.util.stream.Stream;

/**
 * Utilities related to Mongo indexes.
 */
@UtilityClass
public class KiwiMongoIndexes {

    /**
     * Ensure indexes exist for the given collection (defined by {@code clazz}), sort direction, and properties.
     *
     * @param mongoTemplate the MongoTemplate instance
     * @param clazz         the entity/document class representing the mapped MongoDB collection
     * @param direction     the sort direction for all specified properties
     * @param properties    the properties for which to add an index
     * @see org.springframework.data.mongodb.core.index.IndexOperations#createIndex(IndexDefinition)
     */
    public static void ensureIndices(MongoTemplate mongoTemplate,
                                     Class<?> clazz,
                                     Sort.Direction direction,
                                     String... properties) {

        Stream.of(properties).forEach(prop ->
                mongoTemplate.indexOps(clazz).createIndex(new Index(prop, direction)));
    }

    /**
     * Ensure indexes exist for the given collection, sort direction, and properties.
     *
     * @param mongoTemplate  the MongoTemplate instance
     * @param collectionName the MongoDB collection name
     * @param direction      the sort direction for all specified properties
     * @param properties     the properties for which to add an index
     * @see org.springframework.data.mongodb.core.index.IndexOperations#createIndex(IndexDefinition)
     */
    public static void ensureIndices(MongoTemplate mongoTemplate,
                                     String collectionName,
                                     Sort.Direction direction,
                                     String... properties) {

        Stream.of(properties).forEach(prop ->
                mongoTemplate.indexOps(collectionName).createIndex(new Index(prop, direction)));
    }
}
