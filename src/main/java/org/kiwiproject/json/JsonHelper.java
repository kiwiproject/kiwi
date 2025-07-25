package org.kiwiproject.json;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;
import static org.kiwiproject.collect.KiwiMaps.newHashMap;
import static org.kiwiproject.jackson.KiwiTypeReferences.MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A bunch of utilities to make it easier to work with JSON.
 * <p>
 * One specific note on methods that accept paths. The syntax used to indicate array paths consists of the
 * array property name, followed by a period, followed by the array index in square brackets. For example,
 * to find the first value in an array property {@code luckyNumbers}, the path is {@code luckyNumbers.[0]}.
 * Similarly, to find the 13th lucky number the path is {@code luckyNumbers.[12]}.
 * <p>
 * Paths for nested JSON objects follow the syntax {@code objectName.propertyName}; for JSON that contains a
 * {@code homeAddress} object that contains a {@code zipCode}, the path is {@code homeAddress.zipCode}.
 *
 * @implNote This uses Jackson to perform JSON mapping to and from objects, so Jackson will need to be available
 * at runtime. In addition, if you use the no-args constructor, this relies on Dropwizard's {@link Jackson} class
 * which does a bunch of configuration on the default Jackson {@link ObjectMapper}. So, you would need
 * Dropwizard available at runtime as well, specifically {@code dropwizard-jackson}.
 */
@Slf4j
public class JsonHelper {

    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("\\[(\\d+)]");

    private final ObjectMapper objectMapper;
    private final DataFormatDetector jsonFormatDetector;

    /**
     * Represents an output format when serializing an object to JSON.
     */
    public enum OutputFormat {

        /**
         * JSON may or may not be formatted (but probably not).
         */
        DEFAULT,

        /**
         * JSON will be formatted nicely.
         */
        PRETTY;

        /**
         * Parse the given string as a boolean into an {@link OutputFormat}. Uses {@link Boolean#parseBoolean(String)}.
         *
         * @param pretty the boolean value as a string
         * @return the format
         */
        public static OutputFormat ofPrettyValue(String pretty) {
            return ofPrettyValue(Boolean.parseBoolean(pretty));
        }

        /**
         * Parse the (nullable) Boolean value into an {@link OutputFormat}. A null is treated as false.
         *
         * @param pretty the nullable value
         * @return the format
         */
        public static OutputFormat ofPrettyValue(@Nullable Boolean pretty) {
            return nonNull(pretty) ? ofPrettyValue(pretty.booleanValue()) : DEFAULT;
        }

        /**
         * Convert the given boolean value to the appropriate {@link OutputFormat}.
         *
         * @param pretty true or false
         * @return {@link OutputFormat#PRETTY} if the argument is true; otherwise {@link OutputFormat#DEFAULT}
         */
        public static OutputFormat ofPrettyValue(boolean pretty) {
            return pretty ? PRETTY : DEFAULT;
        }
    }

    /**
     * Create a new instance using an {@link ObjectMapper} created using {@link #newDropwizardObjectMapper()}.
     */
    public JsonHelper() {
        this(newDropwizardObjectMapper());
    }

    /**
     * Create a new instance using the given {@link ObjectMapper}.
     *
     * @param objectMapper the ObjectMapper to use
     */
    public JsonHelper(ObjectMapper objectMapper) {
        checkArgumentNotNull(objectMapper, "ObjectMapper cannot be null");
        this.objectMapper = objectMapper;
        this.jsonFormatDetector = new DataFormatDetector(objectMapper.getFactory());
    }

    /**
     * Create a new {@link JsonHelper} with an {@link ObjectMapper} supplied by {@link #newDropwizardObjectMapper()}.
     *
     * @return a new JsonHelper instance
     */
    public static JsonHelper newDropwizardJsonHelper() {
        var mapper = newDropwizardObjectMapper();
        return new JsonHelper(mapper);
    }

    /**
     * Creates a new {@link ObjectMapper} configured using the Dropwizard {@link Jackson#newObjectMapper()} factory
     * method. It also configures the returned mapper to read and write timestamps as milliseconds.
     *
     * @return a new ObjectMapper
     * @see DeserializationFeature#READ_DATE_TIMESTAMPS_AS_NANOSECONDS
     * @see SerializationFeature#WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
     * @see #configureForMillisecondDateTimestamps(ObjectMapper)
     */
    public static ObjectMapper newDropwizardObjectMapper() {
        var mapper = Jackson.newObjectMapper();
        return configureForMillisecondDateTimestamps(mapper);
    }

    /**
     * Configure the given {@link ObjectMapper} to read and write timestamps as milliseconds.
     *
     * @param mapper the {@link ObjectMapper} to change
     * @return the same instance, configured to write/read timestamps as milliseconds
     */
    public static ObjectMapper configureForMillisecondDateTimestamps(ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        return mapper;
    }

    /**
     * Provides direct access to the underlying object mapper. Care should be taken when accessing the
     * {@link ObjectMapper} directly, particularly if any changes are made to how objects are serialized/deserialized.
     *
     * @return the object mapper; any changes made to it will potentially change the behavior of this JsonHelper instance
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Use Jackson's data format detection to determine if the given content is JSON, assuming UTF-8 as the charset.
     *
     * @param content the content to check
     * @return true if detected as JSON, false otherwise (including if content is null or blank, or if an exception
     * is thrown detecting the format)
     */
    public boolean isJson(@Nullable String content) {
        return isJson(content, StandardCharsets.UTF_8);
    }

    /**
     * Use Jackson's data format detection to determine if the given content is JSON.
     *
     * @param content the content to check
     * @param charset the character set to use
     * @return true if detected as JSON, false otherwise (including if content is null or blank, or if an exception
     * is thrown detecting the format)
     * @see DataFormatDetector#findFormat(byte[])
     */
    public boolean isJson(@Nullable String content, Charset charset) {
        return detectJson(content, charset, jsonFormatDetector).isJson();
    }

    /**
     * Use Jackson's data format detection to determine if the given content is JSON, assuming UTF-8 as the charset.
     *
     * @param content the content to check
     * @return the detection result
     */
    public JsonDetectionResult detectJson(@Nullable String content) {
        return detectJson(content, StandardCharsets.UTF_8);
    }

    /**
     * Use Jackson's data format detection to determine if the given content is JSON.
     *
     * @param content the content to check
     * @param charset the character set to use
     * @return the detection result
     */
    public JsonDetectionResult detectJson(@Nullable String content, Charset charset) {
        return detectJson(content, charset, jsonFormatDetector);
    }

    @VisibleForTesting
    static JsonDetectionResult detectJson(@Nullable String content, Charset charset, DataFormatDetector formatDetector) {
        try {
            var result = isNotBlank(content) && formatDetector.findFormat(content.getBytes(charset)).hasMatch();
            return new JsonDetectionResult(result, null);
        } catch (IOException ex) {
            LOG.warn("Unable to determine content format. " +
                            "Enable TRACE logging to see exception details. Exception type: {}. Exception message: {}",
                    ex.getClass().getName(), ex.getMessage());
            LOG.trace("Exception details:", ex);
            return new JsonDetectionResult(null, ex);
        }
    }

    /**
     * Convert the given object to JSON using the {@link OutputFormat#DEFAULT} format.
     *
     * @param object the object to convert
     * @return a JSON representation of the given object, or {@code null} if the given object is {@code null}
     */
    public String toJson(@Nullable Object object) {
        return toJson(object, OutputFormat.DEFAULT);
    }

    /**
     * Convert the given object to JSON using the given format.
     *
     * @param object the object to convert
     * @param format the format to use
     * @return a JSON representation of the given object, or {@code null} if the given object is {@code null}
     */
    public String toJson(@Nullable Object object, OutputFormat format) {
        return toJson(object, format, null);
    }

    /**
     * Convert the given object to JSON using the given format and optionally a class representing the
     * {@link JsonView} to use.
     *
     * @param object   the object to convert
     * @param format   the format to use
     * @param jsonView the nullable {@link JsonView} class
     * @return a JSON representation of the given object, or {@code null} if the given object is {@code null}
     */
    public String toJson(@Nullable Object object, OutputFormat format, @Nullable Class<?> jsonView) {
        checkArgumentNotNull(format, "format is required");

        if (isNull(object)) {
            return null;
        }

        if (object instanceof String s && isJson(s)) {
            return s;
        }

        var writer = objectMapper.writer();

        if (nonNull(jsonView)) {
            writer = writer.withView(jsonView);
        }

        if (format == OutputFormat.PRETTY) {
            writer = writer.withDefaultPrettyPrinter();
        }

        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonException(e);
        }
    }

    /**
     * Consider the given arguments as key/value pairs, and convert those pairs to a JSON representation.
     *
     * @param kvPairs the objects to treat as key/value pairs
     * @return the JSON representation
     * @throws IllegalArgumentException if an odd number of arguments is supplied
     */
    public String toJsonFromKeyValuePairs(Object... kvPairs) {
        checkEvenItemCount(kvPairs, "must supply an even number of arguments");

        return toJson(newHashMap(kvPairs));
    }

    /**
     * Convert the given object to JSON, but ignoring (excluding) the given paths.
     * <p>
     * Note that if the input object is {@code null}, then the returned value is
     * the string literal {@code "null"}. The reason is that a {@code null} object
     * is represented as a {@link NullNode}, and its {@code toString} method returns
     * the literal {@code "null"}.
     *
     * @param object       the object to convert
     * @param ignoredPaths the paths to ignore/exclude
     * @return the JSON representation without the ignored paths
     */
    public String toJsonIgnoringPaths(@Nullable Object object, String... ignoredPaths) {
        var root = getRootNode(toJson(object));
        Stream.of(ignoredPaths).forEach(path -> removePathNode(root, path));

        return toJson(root);
    }

    /**
     * Convert the given JSON into the specified type.
     *
     * @param json        the JSON content
     * @param targetClass the type of object to convert into
     * @param <T>         the object type
     * @return a new instance of the given type, or {@code null} if the given input JSON is blank
     */
    @SuppressWarnings("unchecked")
    public <T> T toObject(@Nullable String json, Class<T> targetClass) {
        if (isBlank(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, targetClass);
        } catch (MismatchedInputException e) {
            if (nonNull(e.getTargetType())
                    && isNullOrEmpty(e.getPath())
                    && e.getTargetType().isAssignableFrom(String.class)) {
                return (T) json;
            } else {
                throw new RuntimeJsonException(e);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonException(e);
        }
    }

    /**
     * Convert the given JSON into the specified type, or return the given default value if input JSON is blank.
     *
     * @param json         the JSON content
     * @param clazz        the type of object to convert into
     * @param defaultValue the default value to use if necessary
     * @param <T>          the object type
     * @return a new instance of the given type, or return {@code defaultValue} if the given input JSON is blank
     */
    public <T> T toObjectOrDefault(@Nullable String json, Class<T> clazz, T defaultValue) {
        if (isBlank(json)) {
            return defaultValue;
        }

        return toObject(json, clazz);
    }

    /**
     * Convert the given JSON into the specified type, or return the supplied default value if input JSON is blank.
     *
     * @param json                 the JSON content
     * @param clazz                the type of object to convert into
     * @param defaultValueSupplier the default value {@link Supplier} to call if necessary
     * @param <T>                  the object type
     * @return a new instance of the given type, or return the value supplied by {@code defaultValueSupplier} if
     * the given input JSON is blank
     */
    public <T> T toObjectOrSupply(@Nullable String json, Class<T> clazz, Supplier<T> defaultValueSupplier) {
        if (isBlank(json)) {
            return defaultValueSupplier.get();
        }

        return toObject(json, clazz);
    }

    /**
     * Convert the given JSON into an object of type {@code T} using the given {@link TypeReference}.
     *
     * @param json       the JSON content
     * @param targetType the {@link TypeReference} representing the target object type
     * @param <T>        the object type
     * @return a new instance of the type encapsulated by the TypeReference, or {@code null} if the input JSON is blank
     */
    public <T> T toObject(@Nullable String json, TypeReference<T> targetType) {
        if (isBlank(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonException(e);
        }
    }

    /**
     * Return an Optional that will contain an object of the expected type {@code T}, or an empty Optional if the
     * input JSON is blank
     *
     * @param json        the JSON content
     * @param targetClass the type of object to convert into
     * @param <T>         the object type
     * @return an Optional that may contain a converted object
     */
    public <T> Optional<T> toObjectOptional(@Nullable String json, Class<T> targetClass) {
        if (isBlank(json)) {
            return Optional.empty();
        }

        return Optional.of(toObject(json, targetClass));
    }

    /**
     * Convert the given JSON into a List of objects of type {@code T}.
     *
     * @param json           the JSON content
     * @param targetListType the {@link TypeReference} representing the list target type
     * @param <T>            the object type
     * @return a list containing objects of the given type, or {@code null} if the input is blank
     */
    public <T> List<T> toObjectList(@Nullable String json, TypeReference<List<T>> targetListType) {
        return toObject(json, targetListType);
    }

    /**
     * Convert the given JSON into a map with String keys and Object values.
     *
     * @param json the JSON content
     * @return the parsed map, or {@code null} if the input JSON is blank
     */
    public Map<String, Object> toMap(@Nullable String json) {
        if (isBlank(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonException(e);
        }
    }

    /**
     * Convert the given JSON into a map with keys of type {@code K} and values of type {@code V}.
     *
     * @param json          the JSON content
     * @param targetMapType the {@link TypeReference} representing the target map type
     * @param <K>           the type of keys in the map
     * @param <V>           the type of values in the map
     * @return the parsed map, or {@code null} if the input JSON is blank
     */
    public <K, V> Map<K, V> toMap(@Nullable String json, TypeReference<Map<K, V>> targetMapType) {
        if (isBlank(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, targetMapType);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonException(e);
        }
    }

    /**
     * Parse the given object as JSON, then flatten all its properties to a map whose keys are the object property
     * names and whose values are converted to Strings.
     * <p>
     * For more details on the behavior, see {@link #toFlatMap(Object, Class)}.
     *
     * @param object the object to flatten
     * @return a map with string keys and values converted to strings, or {@code null} if the given
     * object is {@code null}
     */
    public Map<String, String> toFlatMap(@Nullable Object object) {
        return toFlatMap(object, String.class);
    }

    /**
     * Parse the given object as JSON, then flatten all its properties to a map whose keys are the object property
     * names and whose values are converted to the given {@code valueClass} type. In practice, this will often
     * just be {@code Object.class} but could be a more specific type, e.g., if you have a map containing student
     * names and grades then the values could all be of type {@code Double}.
     * <p>
     * This also flattens arrays/collections and maps. Flattened arrays use the following syntax:
     * {@code arrayPropertyName.[index]}. For example, {@code luckyNumbers.[0]} is the first element in a collection
     * named {@code luckyNumbers}. For maps, the syntax is: {@code mapPropertyName.key}. For example,
     * {@code emailAddresses.home} contains the value in the {@code emailAddresses} map under the key {@code home}.
     *
     * @param object     the object to flatten
     * @param valueClass the target class for the map's values
     * @param <T>        the generic type of the map values
     * @return a map with string keys and values converted to the specified type, or {@code null} if the given
     * object is {@code null}
     */
    public <T> Map<String, T> toFlatMap(@Nullable Object object, Class<T> valueClass) {
        if (isNull(object)) {
            return null;
        }

        var paths = listObjectPaths(object);
        return paths.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(path -> path, path -> getPath(object, path, valueClass)));
    }

    /**
     * Copies the given object by converting to JSON and then converting the JSON back
     * to an object of the same class as the copied object.
     *
     * @param object the object to copy
     * @param <T>    the type of object being copied
     * @return the copied object of the target class
     */
    @SuppressWarnings("unchecked")
    public <T> T copy(T object) {
        if (isNull(object)) {
            return null;
        }

        return copy(object, (Class<T>) object.getClass());
    }

    /**
     * Copies the given object by converting to JSON and then converting the JSON back
     * to an object of the given target class.
     *
     * @param object      the object to copy
     * @param targetClass the target class (it may be different from the original)
     * @param <T>         the type of object being copied
     * @param <R>         the type of object to copy into
     * @return the copied object of the target class
     */
    public <T, R> R copy(T object, Class<R> targetClass) {
        return copyIgnoringPaths(object, targetClass);
    }

    /**
     * Copies the given object by converting to JSON, ignoring the given paths, and then converting the JSON back
     * to an object of the given target class.
     *
     * @param object       the object to copy
     * @param targetClass  the target class (it may be different from the original)
     * @param ignoredPaths the paths to ignore during the copy
     * @param <T>          the type of object being copied
     * @param <R>          the type of object to copy into
     * @return the copied object of the target class
     */
    public <T, R> R copyIgnoringPaths(T object, Class<R> targetClass, String... ignoredPaths) {
        var json = toJsonIgnoringPaths(object, ignoredPaths);
        return toObject(json, targetClass);
    }

    /**
     * Converts the given object to an object of the target type.
     *
     * @param fromObject the object to convert
     * @param targetType the type of object to convert to
     * @param <T>        the target type
     * @return a new instance of the target type
     * @see ObjectMapper#convertValue(Object, Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(Object fromObject, Class<T> targetType) {
        if (isNull(fromObject)) {
            return null;
        }

        if (targetType.isAssignableFrom(String.class)) {
            return (T) toJson(fromObject);
        }

        return objectMapper.convertValue(fromObject, targetType);
    }

    /**
     * Converts the given object to an object of the target type described by the {@link TypeReference}.
     *
     * @param fromObject the object to convert
     * @param targetType a {@link TypeReference} that describes the target type
     * @param <T>        the target type
     * @return a new instance of the target type
     * @see ObjectMapper#convertValue(Object, TypeReference)
     */
    public <T> T convert(Object fromObject, TypeReference<T> targetType) {
        if (isNull(fromObject)) {
            return null;
        }

        return objectMapper.convertValue(fromObject, targetType);
    }

    /**
     * Converts the given object to a map with String keys and Object values.
     *
     * @param fromObject the object to convert
     * @return a new map instance
     */
    public Map<String, Object> convertToMap(Object fromObject) {
        return convertToMap(fromObject, MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);
    }

    /**
     * Converts the given object to a map using the given {@link TypeReference}.
     * <p>
     * Unless you have specialized requirements, usually {@link #convertToMap(Object)} will be what you want.
     *
     * @param fromObject    the object to convert
     * @param targetMapType the {@link TypeReference} describing the target map type
     * @param <K>           the type of keys in the map
     * @param <V>           the type of values in the map
     * @return a new map instance
     */
    public <K, V> Map<K, V> convertToMap(Object fromObject, TypeReference<Map<K, V>> targetMapType) {
        if (isNull(fromObject)) {
            return null;
        }

        return objectMapper.convertValue(fromObject, targetMapType);
    }

    /**
     * Get the value at the given path in the object, with the type as the given target class.
     *
     * @param object      the object to search
     * @param path        the path within the object (e.g. "homeAddress.zipCode")
     * @param targetClass the type associated with the given path
     * @param <T>         the return type
     * @return an instance of the specified target class
     */
    public <T> T getPath(Object object, String path, Class<T> targetClass) {
        var json = toJson(object);
        return getPath(json, path, targetClass);
    }

    /**
     * Get the value at the given path in the JSON, with the type as the given target class.
     *
     * @param json        the JSON to search
     * @param path        the path within the object (e.g. "homeAddress.zipCode")
     * @param targetClass the type associated with the given path
     * @param <T>         the return type
     * @return an instance of the specified target class
     */
    public <T> T getPath(String json, String path, Class<T> targetClass) {
        var pathNodeJson = getPathNode(json, path).toString();
        return toObject(pathNodeJson, targetClass);
    }

    /**
     * Get the value at the given path in the object, with the type as the given target class.
     *
     * @param object     the object to search
     * @param path       the path within the object (e.g. "homeAddress.zipCode")
     * @param targetType the type associated with the given path
     * @param <T>        the return type
     * @return an instance of the specified target class
     */
    public <T> T getPath(Object object, String path, TypeReference<T> targetType) {
        var json = toJson(object);
        return getPath(json, path, targetType);
    }

    /**
     * Get the value at the given path in the JSON, with the type as the given target class.
     *
     * @param json       the JSON to search
     * @param path       the path within the object (e.g. "homeAddress.zipCode")
     * @param targetType the type associated with the given path
     * @param <T>        the return type
     * @return an instance of the specified target class
     */
    public <T> T getPath(String json, String path, TypeReference<T> targetType) {
        var pathNodeJson = getPathNode(json, path).toString();
        return toObject(pathNodeJson, targetType);
    }

    private JsonNode getPathNode(String json, String path) {
        var rootNode = getRootNode(json);
        return getPathNode(rootNode, path).getRight();
    }

    /**
     * Remove the given path from the object.
     *
     * @param object the object from which to remove a path
     * @param path   the path to remove
     * @return a {@link JsonNode} with the given path removed
     */
    public JsonNode removePath(@Nullable Object object, String path) {
        var rootNode = getRootNode(toJson(object));
        return removePathNode(rootNode, path);
    }

    private static JsonNode removePathNode(JsonNode root, String path) {
        var parts = path.split("\\.");

        ContainerNode<?> parentNode = getPathNode(root, path).getLeft();
        if (nonNull(parentNode)) {
            var lastPart = parts[parts.length - 1];
            var matcher = ARRAY_INDEX_PATTERN.matcher(lastPart);
            if (matcher.matches() && parentNode instanceof ArrayNode) {
                asArrayNode(parentNode).remove(Integer.parseInt(matcher.group(1)));
            } else if (parentNode instanceof ObjectNode) {
                asObjectNode(parentNode).remove(lastPart);
            } else {
                throw new IllegalArgumentException(f("Unable to remove element: {} from node: {}", lastPart, root));
            }
        }

        return root;
    }

    /**
     * Update the given path in the object with the new value, converting to the target class.
     *
     * @param object      the original object
     * @param path        the path to update
     * @param value       the new value to use
     * @param targetClass the type of object to return
     * @param <T>         the type of the input object
     * @return a new instance of the given target type
     */
    public <T> T updatePath(@Nullable Object object, String path, Object value, Class<T> targetClass) {
        var rootNode = getRootNode(toJson(object));
        var jsonNode = convert(value, JsonNode.class);
        var updatedNode = updatePathNode(rootNode, path, jsonNode);

        return convert(updatedNode, targetClass);
    }

    private static JsonNode updatePathNode(JsonNode root, String path, JsonNode value) {
        var parts = path.split("\\.");

        var parentNode = getPathNode(root, path).getLeft();
        if (nonNull(parentNode)) {
            var lastPart = parts[parts.length - 1];
            var matcher = ARRAY_INDEX_PATTERN.matcher(lastPart);
            if (matcher.matches() && parentNode instanceof ArrayNode) {
                asArrayNode(parentNode).insert(Integer.parseInt(matcher.group(1)), value);
            } else if (parentNode instanceof ObjectNode) {
                asObjectNode(parentNode).replace(lastPart, value);
            } else {
                throw new IllegalArgumentException(f("Unable to set element: {} into parent root: {}", lastPart, root));
            }
        }

        return root;
    }

    private static Pair<ContainerNode<?>, JsonNode> getPathNode(JsonNode root, String path) {
        ContainerNode<?> parent = null;
        var parts = path.split("\\.");
        var node = root;
        for (var pathPart : parts) {
            parent = node instanceof ContainerNode ? (ContainerNode<?>) node : null;

            var matcher = ARRAY_INDEX_PATTERN.matcher(pathPart);
            if (matcher.matches()) {
                node = node.path(Integer.parseInt(matcher.group(1)));
            } else {
                node = node.path(pathPart);
            }
        }

        return Pair.of(parent, node);
    }

    /**
     * Compare two objects via their JSON differences, optionally ignoring one or more paths. The diff is from the
     * perspective of the first object.
     * <p>
     * The returned map of differences has keys that are the properties that are different. The map values are
     * the values for the corresponding key/property in the first and second objects, respectively.
     * <p>
     * NOTE: This is an expensive operation, so be careful of using it in production code in areas where performance
     * is critical.
     *
     * @param object1      the first object
     * @param object2      the second object
     * @param ignoredPaths the paths to ignore in the comparison
     * @return a map containing a list of differences
     */
    public Map<String, List<String>> jsonDiff(@Nullable Object object1, @Nullable Object object2, String... ignoredPaths) {
        return jsonDiff(Lists.newArrayList(object1, object2), ignoredPaths);
    }

    /**
     * Compare an object to an arbitrary number of other objects via their JSON differences. The diff is from the
     * perspective of the first object in the given list.
     * <p>
     * The returned map of differences has keys that are the properties that are different. The map values are
     * the values for the corresponding key/property in the first and later objects, respectively.
     * <p>
     * NOTE: This is an expensive operation, so be careful of using it in production code in areas where performance
     * is critical.
     *
     * @param objectList   the list of objects to compare; the first object is the reference object
     * @param ignoredPaths the paths to ignore in the comparison
     * @return a map containing a list of differences
     */
    public Map<String, List<String>> jsonDiff(@NonNull List<Object> objectList, String... ignoredPaths) {
        var jsonList = requireNotNull(objectList)
                .stream()
                .map(obj -> toJsonIgnoringPaths(obj, ignoredPaths))
                .toList();
        return jsonDiff(jsonList);
    }

    /**
     * Compare a JSON object to an arbitrary number of other objects via their JSON differences. The diff is from the
     * perspective of the first JSON object in the given list.
     * <p>
     * The returned map of differences has keys that are the properties that are different. The map values are
     * the values for the corresponding key/property in the first and later objects, respectively.
     * <p>
     * NOTE: This is an expensive operation, so be careful of using it in production code in areas where performance
     * is critical.
     *
     * @param listOfJson the list of JSON objects to compare
     * @return map containing a list of differences
     */
    public Map<String, List<String>> jsonDiff(@NonNull List<String> listOfJson) {
        var resultMap = new HashMap<String, List<String>>();

        requireNotNull(listOfJson)
                .stream()
                .map(this::listObjectPaths)
                .flatMap(Collection::stream)
                .forEach(path -> {
                    List<String> results = listOfJson.stream()
                            .map(json -> getPath(json, path, String.class))
                            .toList();
                    if (isNotNullOrEmpty(results)) {
                        var match = first(results);
                        if (!results.stream().allMatch(s -> Strings.CS.equals(s, match))) {
                            resultMap.put(path, new ArrayList<>(results));  // must use a mutable list to handle nulls
                        }
                    }
                });

        return resultMap;
    }

    /**
     * Compare the JSON representation of multiple objects.
     *
     * @param objects the objects to compare
     * @return true if all the given objects have equal JSON representations
     */
    public boolean jsonEquals(Object... objects) {
        var jsonNodeList = Stream.of(objects)
                .map(this::toJson)
                .map(this::getRootNode)
                .toList();

        return jsonNodeList.stream()
                .allMatch(jsonNode -> Objects.equals(jsonNode, first(jsonNodeList)));
    }

    /**
     * Compare the JSON representations of two objects, optionally ignoring paths.
     *
     * @param object1      the first object to compare
     * @param object2      the second object to compare
     * @param ignoredPaths the paths to ignore in the comparison
     * @return true if the objects have equal JSON representations, ignoring the given paths
     */
    public boolean jsonEqualsIgnoringPaths(Object object1, Object object2, String... ignoredPaths) {
        var json1 = toJsonIgnoringPaths(object1, ignoredPaths);
        var json2 = toJsonIgnoringPaths(object2, ignoredPaths);

        return jsonEquals(json1, json2);
    }

    /**
     * Compare the values at a given path in two objects.
     *
     * @param object1     the first object
     * @param object2     the second object
     * @param path        the path to compare
     * @param targetClass the type of object at the given path
     * @param <T>         the object type for the given path
     * @return true if the two objects have an equal value at the given path; the two values are compared using
     * {@link Objects#equals(Object, Object)}
     */
    public <T> boolean jsonPathsEqual(Object object1, Object object2, String path, Class<T> targetClass) {
        var value1 = getPath(object1, path, targetClass);
        var value2 = getPath(object2, path, targetClass);

        return Objects.equals(value1, value2);
    }

    /**
     * Describes how objects are to be merged.
     *
     * @see #mergeObjects(Object, Object, MergeOption...)
     * @see #mergeNodes(JsonNode, JsonNode, MergeOption...)
     */
    public enum MergeOption {

        /**
         * This option will cause arrays to be merged rather than replaced. The default is that arrays will be
         * replaced with a new value. Specify this option to merge them instead.
         */
        MERGE_ARRAYS,

        /**
         * This option will ignore null values in an update object. The default is to respect null values and
         * set the updated value to null. Specify this option to ignore nulls instead.
         */
        IGNORE_NULLS
    }

    /**
     * Merge values in {@code updateObject} into the original object, using the given merge options.
     * <p>
     * Note that the {@code originalObject} is not mutated in any way; it simply represents the original state
     * to be updated with values from {@code updateObject}. Both {@code originalObject} and {@code updateObject}
     * are converted to JSON before merging.
     *
     * @param originalObject the object into which updates will be merged (only used to read original state)
     * @param updateObject   the object containing updates
     * @param mergeOptions   zero or more {@link MergeOption}
     * @param <T>            the type of the merged object
     * @return a new instance of type T
     * @see MergeOption
     */
    @SuppressWarnings("unchecked")
    public <T> T mergeObjects(T originalObject, Object updateObject, MergeOption... mergeOptions) {
        var originalObjJson = toJson(originalObject);
        var updateObjJson = toJson(updateObject);

        var originalNode = getRootNode(originalObjJson);
        var updateNode = getRootNode(updateObjJson);

        JsonNode updatedNode = mergeNodes(originalNode, updateNode, mergeOptions);

        return (T) toObject(updatedNode.toString(), originalObject.getClass());
    }

    /**
     * Updates (mutates) {@code destinationNode} with values from {@code updateNode}.
     *
     * @param destinationNode the node that will be updated (mutated)
     * @param updateNode      the node containing updated values
     * @param mergeOptions    zero or more {@link MergeOption}
     * @return the mutated {@code destinationNode}
     * @see MergeOption
     */
    public JsonNode mergeNodes(JsonNode destinationNode, JsonNode updateNode, MergeOption... mergeOptions) {
        boolean mergeArrays = ArrayUtils.contains(mergeOptions, MergeOption.MERGE_ARRAYS);
        boolean ignoreNulls = ArrayUtils.contains(mergeOptions, MergeOption.IGNORE_NULLS);

        return mergeNodes(destinationNode, updateNode, mergeArrays, ignoreNulls);
    }

    private JsonNode mergeNodes(JsonNode destinationNode, JsonNode updateNode, boolean mergeArrays, boolean ignoreNulls) {
        updateNode.fieldNames().forEachRemaining(fieldName -> {
            var updateFieldNode = updateNode.get(fieldName);

            if (isNullNode(updateFieldNode)) {
                if (ignoreNulls) {
                    return;  // continues lambda with next fieldName
                } else if (isObjectNode(destinationNode)) {
                    asObjectNode(destinationNode).putNull(fieldName);
                }
            }

            var destFieldNode = destinationNode.get(fieldName);
            if (isContainerNode(destFieldNode)) {
                mergeContainerNode(fieldName, destinationNode, destFieldNode, updateFieldNode, mergeArrays, ignoreNulls);
            } else if (isObjectNode(destinationNode)) {
                asObjectNode(destinationNode).replace(fieldName, updateFieldNode);
            } else {
                LOG.warn("Unhandled node {}: {}", fieldName, destFieldNode);
            }
        });

        return destinationNode;
    }

    private static boolean isNullNode(JsonNode node) {
        return nonNull(node) && node.isNull();
    }

    private static boolean isObjectNode(JsonNode node) {
        return nonNull(node) && node.isObject();
    }

    private static boolean isContainerNode(JsonNode node) {
        return nonNull(node) && node.isContainerNode();
    }

    private static ArrayNode asArrayNode(JsonNode node) {
        return (ArrayNode) node;
    }

    private static ObjectNode asObjectNode(JsonNode node) {
        return (ObjectNode) node;
    }

    private void mergeContainerNode(String fieldName,
                                    JsonNode destinationNode,
                                    JsonNode destFieldNode,
                                    JsonNode updateFieldNode,
                                    boolean mergeArrays,
                                    boolean ignoreNulls) {

        if (destFieldNode.isObject()) {
            mergeNodes(destFieldNode, updateFieldNode, mergeArrays, ignoreNulls);  // recurse on contents

        } else if (destFieldNode.isArray()) {
            mergeOrReplaceArray(fieldName, destinationNode, (ArrayNode) destFieldNode, updateFieldNode, mergeArrays);
        }
    }

    private static void mergeOrReplaceArray(String fieldName,
                                            JsonNode destinationNode,
                                            ArrayNode destFieldNode,
                                            JsonNode updateFieldNode,
                                            boolean mergeArrays) {

        if (mergeArrays && updateFieldNode.isArray()) {  // merge array contents
            updateFieldNode.forEach(destFieldNode::add);
        } else {  // just replace array
            asObjectNode(destinationNode).replace(fieldName, updateFieldNode);
        }
    }

    /**
     * Parse the given object as JSON, and return a list containing the property paths in the object. The paths
     * include arrays, collections, and maps.
     * <p>
     * For details on the property path syntax, see {@link #toFlatMap(Object, Class)}.
     *
     * @param object the object to list paths for
     * @return a list of the property paths
     */
    public List<String> listObjectPaths(@Nullable Object object) {
        var rootNode = getRootNode(toJson(object));
        return listNodePaths(rootNode);
    }

    private JsonNode getRootNode(String json) {
        if (isBlank(json)) {
            return NullNode.getInstance();
        }

        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonException(e);
        }
    }

    private static List<String> listNodePaths(JsonNode node) {
        var paths = new ArrayList<String>();
        node.fieldNames().forEachRemaining(fieldName -> {
            var child = node.get(fieldName);

            if (nonNull(child)) {
                var parentPrefix = fieldName + ".";

                if (child.isObject()) {
                    appendChildPaths(paths, child, parentPrefix);
                } else if (child.isArray()) {
                    appendArrayNodePaths(paths, child, parentPrefix);
                } else {
                    // is a leaf node, so add the field name
                    paths.add(fieldName);
                }
            } else {
                LOG.warn("Unhandled node {}", fieldName);
            }
        });

        return paths;
    }

    private static void appendArrayNodePaths(List<String> paths, JsonNode child, String parentPrefix) {
        var index = new AtomicInteger();
        child.elements().forEachRemaining(arrayElement -> {
            var currentIndex = index.getAndIncrement();
            var currentPath = parentPrefix + f("[%s]", currentIndex);
            if (!arrayElement.isContainerNode()) {
                // not a container (e.g., object or array), so add the path
                paths.add(currentPath);
            }
            appendChildPaths(paths, arrayElement, currentPath + ".");
        });
    }

    private static void appendChildPaths(List<String> paths, JsonNode child, String parentPrefix) {
        var childPaths = listNodePaths(child);
        paths.addAll(
                childPaths.stream()
                        .map(path -> parentPrefix + path)
                        .toList()
        );
    }
}
