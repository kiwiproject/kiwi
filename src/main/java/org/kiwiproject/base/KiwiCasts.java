package org.kiwiproject.base;

import lombok.experimental.UtilityClass;

/**
 * Utility class providing type casting operations.
 */
@UtilityClass
public class KiwiCasts {

    /**
     * Performs an unchecked cast of the given object to the specified type.
     * 
     * @param object the object to cast
     * @param <T> the type to cast to
     * @return the object cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object object) {
        return (T) object;
    }
}
