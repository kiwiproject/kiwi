package org.kiwiproject.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities/constants for common types of Jackson {@link TypeReference} objects.
 */
@UtilityClass
public class KiwiTypeReferences {

    public static final TypeReference<Map<String, Object>> MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<List<Map<String, Object>>> LIST_OF_MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<List<String>> LIST_OF_STRING_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<List<Integer>> LIST_OF_INTEGER_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<List<Long>> LIST_OF_LONG_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<List<Double>> LIST_OF_DOUBLE_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<List<Float>> LIST_OF_FLOAT_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<List<Boolean>> LIST_OF_BOOLEAN_TYPE_REFERENCE =
            new TypeReference<>() {
            };

    public static final TypeReference<Set<String>> SET_OF_STRING_TYPE_REFERENCE =
            new TypeReference<>() {
            };
}
