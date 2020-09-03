package org.kiwiproject.jaxrs;

import lombok.experimental.UtilityClass;

import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Map;

/**
 * Utilities/constants for containing common, re-usable types of {@link GenericType} objects.
 */
@UtilityClass
public class KiwiGenericTypes {

    public static final GenericType<Map<String, Object>> MAP_OF_STRING_TO_OBJECT_GENERIC_TYPE =
            new GenericType<>() {
            };

    public static final GenericType<List<Map<String, Object>>> LIST_OF_MAP_OF_STRING_TO_OBJECT_GENERIC_TYPE =
            new GenericType<>() {
            };

    public static final GenericType<List<String>> LIST_OF_STRING_GENERIC_TYPE =
            new GenericType<>() {
            };

    public static final GenericType<List<Integer>> LIST_OF_INTEGER_GENERIC_TYPE =
            new GenericType<>() {
            };

    public static final GenericType<List<Long>> LIST_OF_LONG_GENERIC_TYPE =
            new GenericType<>() {
            };

    public static final GenericType<List<Double>> LIST_OF_DOUBLE_GENERIC_TYPE =
            new GenericType<>() {
            };

    public static final GenericType<List<Float>> LIST_OF_FLOAT_GENERIC_TYPE =
            new GenericType<>() {
            };

    public static final GenericType<List<Boolean>> LIST_OF_BOOLEAN_GENERIC_TYPE =
            new GenericType<>() {
            };
}
