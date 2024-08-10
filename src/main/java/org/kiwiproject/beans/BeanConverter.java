package org.kiwiproject.beans;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Simple way to convert one bean to another.  This utility uses spring-beans to attempt the conversion at first.
 * If attempting to convert maps, it will attempt to do simple copies of the key-value pairs.
 * <p>
 * Exclusion lists can be provided to ignore specific fields.
 * <p>
 * Also, custom mappers can be provided per field for more control over how the fields are converted.
 * <p>
 * NOTE: This class requires spring-beans as a dependency.
 */
@Slf4j
public class BeanConverter<T> {

    /**
     * Custom mappers that map specific field names to a function that will accept the data and convert it to the new value.
     */
    @Getter
    private final Map<String, Function<T, ?>> mappers = new HashMap<>();

    /**
     * Set of property names to exclude from copying over.  Defaults to {@code ["class", "new"] }
     */
    @Getter
    @Setter
    private Set<String> exclusions = Sets.newHashSet("class", "new");

    /**
     * Allows for a failed conversion to throw an exception if {@code true}; otherwise just logs a failure if {@code false}
     */
    @Getter
    @Setter
    private boolean failOnError;

    /**
     * This conversion method takes a single parameter and modifies the object in place.
     *
     * @param input the object to perform the conversion on
     * @return the same object that was passed in
     */
    public T convert(T input) {
        return convert(input, input);
    }

    /**
     * This conversion method takes two parameters, and it copies properties from one object to another
     *
     * @param input  the object to copy the properties from
     * @param target the object to copy the properties too (destination)
     * @param <R>    the type of object being returned
     * @return the modified target object
     */
    public <R> R convert(T input, R target) {
        if (isNull(input)) {
            return null;
        }

        // perform standard mapping first
        var inputWrapper = new BeanWrapperImpl(input);
        var targetWrapper = new BeanWrapperImpl(target);

        var propertyNames = getPropertySet(input, inputWrapper);

        // This cannot be a foreach because if failOnError is true, the exceptions need to bubble.
        for (String propName : propertyNames) {
            if (hasPropertyMapper(propName)) {
                // custom mapper
                var func = getPropertyMapper(propName);
                func.apply(input);
            } else if (input != target) {
                // only need to map simple properties if it's a different object
                var inputValue = readBeanValue(input, inputWrapper, propName);
                writeBeanValue(target, targetWrapper, propName, inputValue);

            }
        }

        return target;
    }

    protected Set<String> getPropertySet(T input, BeanWrapper inputWrapper) {
        var propertyNames = getPropertyNamesAsSet(input, inputWrapper);

        // remove exclusions
        return propertyNames.stream()
                .filter(not(prop -> exclusions.contains(prop)))
                .collect(toUnmodifiableSet());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Set<String> getPropertyNamesAsSet(T input, BeanWrapper inputWrapper) {
        if (input instanceof Map inputMap) {
            return inputMap.keySet();
        }

        return Stream.of(inputWrapper.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .collect(toUnmodifiableSet());
    }

    /**
     * Checks to see if a property mapper exists for a given property name
     *
     * @param propertyName the property name
     * @return true if a property mapper was found for that property
     */
    public boolean hasPropertyMapper(String propertyName) {
        return mappers.containsKey(propertyName);
    }

    /**
     * Get the property mapper function for a specific property name.
     * <p>
     * It is assumed the caller knows the correct return type for the mapper, otherwise a
     * {@link ClassCastException} will be thrown at runtime.
     *
     * @param propertyName the property name
     * @param <R>          the result type of the mapper for the given property
     * @return the Function that will be triggered
     */
    @SuppressWarnings("unchecked")
    public <R> Function<T, R> getPropertyMapper(String propertyName) {
        return (Function<T, R>) mappers.get(propertyName);
    }

    @SuppressWarnings("unchecked")
    protected Object readBeanValue(T input, BeanWrapper inputWrapper, String propName) {
        try {
            return inputWrapper.getPropertyValue(propName);
        } catch (NotReadablePropertyException e) {
            // try direct access if map
            if (input instanceof Map) {
                return ((Map<String, ?>) input).get(propName);
            } else {
                // if no approach works
                logOrFail("Exception trying to read value", propName, e);
            }
        }

        return null;
    }

    protected void logOrFail(String msg, String propName, RuntimeException e) {
        if (failOnError) {
            throw e;
        } else {
            LOG.debug("{} - property: {}", msg, propName);
            LOG.trace("Exception: ", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <R> void writeBeanValue(R target, BeanWrapper targetWrapper, String propName, Object inputValue) {
        try {
            // default - grab value directly from input
            targetWrapper.setPropertyValue(propName, inputValue);
        } catch (NotWritablePropertyException e) {
            // try direct access if map
            if (target instanceof Map targetMap) {
                targetMap.put(propName, inputValue);
            } else {
                // if no approach works
                logOrFail("Exception trying to write value", propName, e);
            }
        }
    }

    /**
     * Adds a property mapper function for a specific property name
     *
     * @param propertyName the property name
     * @param function     the Function that will be triggered
     * @throws IllegalStateException if a mapper is already registered on the given property
     */
    public void addPropertyMapper(String propertyName, Function<T, ?> function) {
        checkState(!mappers.containsKey(propertyName),
                "Mapper already registered for property: %s", propertyName);

        mappers.put(propertyName, function);
    }
}
