package org.kiwiproject.reflect;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.kiwiproject.base.KiwiStrings;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Some utilities related to reflection. Please read the <strong>WARNING</strong> below.
 * <p>
 * Many of the methods are simply wrappers around JDK methods in {@link Class}, {@link Field}, and {@link Method};
 * they catch the various exceptions that can be thrown by the JDK methods and wrap them with a single
 * {@link RuntimeReflectionException}.
 * <p>
 * <strong>WARNING:</strong> Note that some methods bypass the Java accessibility mechanism and/or change the
 * visibility of a method or field. This makes Sonar very unhappy. See Sonar rule
 * <a href="https://rules.sonarsource.com/java/RSPEC-3011">java:S3011</a> for more details, as well as rule
 * <a href="https://wiki.sei.cmu.edu/confluence/x/_jZGBQ">SEC05-J</a> from the SEI CERT Oracle Secure Coding Standard.
 * <p>
 * With the above warning in mind, there are good reasons to bypass Java's protections in some situations. And
 * since this is a class specifically intended to be used in such situations, we intentionally suppress Sonar's
 * warnings and assume you have good reasons to violate those rules.
 */
@SuppressWarnings({"unused", "WeakerAccess", "java:S3011"})
@UtilityClass
@Slf4j
public class KiwiReflection {

    private static final Object[] ONE_NULL_ARG_OBJECT_ARRAY = {null};
    private static final String UNDERSCORE = "_";

    /**
     * Defines the accessor method type.
     */
    public enum Accessor {

        /**
         * Setter method.
         */
        SET("set{}", "setter", 1),

        /**
         * Getter method for reference types and primitive types, except primitive {@code boolean} types.
         */
        GET("get{}", "getter", 0),

        /**
         * Getter method type for primitive {@code boolean}.
         */
        IS("is{}", "boolean getter", 0);

        private final String template;
        private final String description;
        private final int requiredArgs;

        Accessor(String template, String description, int requiredArgs) {
            this.template = template;
            this.description = description;
            this.requiredArgs = requiredArgs;
        }

        void validateParameterCount(Class<?>... parameterTypes) {
            checkArgumentNotNull(parameterTypes, "parameterTypes cannot be null");
            checkArgument(parameterTypes.length == requiredArgs,
                    "%s methods must have %s arguments", description, requiredArgs);
        }
    }

    /**
     * Find a field by name in the specified target object, whether it is public or not.
     * <p>
     * Note specifically that if the field is <em>not</em> public, this method uses {@link Field#setAccessible(boolean)}
     * to make it accessible and thus is subject to a {@link SecurityException} or
     * {@link java.lang.reflect.InaccessibleObjectException}.
     *
     * @param target    the target object
     * @param fieldName the field name to find
     * @return the Field object
     * @throws RuntimeReflectionException if any error occurs finding the field
     * @see Field#setAccessible(boolean)
     */
    public static Field findField(Object target, String fieldName) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            setAccessibleIfNotPublic(field);
            return field;
        } catch (Exception e) {
            throw new RuntimeReflectionException(f("Cannot get field [%s] in object [%s]", fieldName, target), e);
        }
    }

    private static void setAccessibleIfNotPublic(Field field) {
        if (isNotPublic(field)) {
            field.setAccessible(true);
        }
    }

    private static boolean isNotPublic(Field field) {
        return !Modifier.isPublic(field.getModifiers());
    }

    /**
     * Get the value of a specific field in an object, cast to the specified type.
     *
     * @param target    the target object
     * @param fieldName the field name
     * @param type      the type of object to return
     * @param <T>       the type parameter
     * @return the field value
     * @throws RuntimeReflectionException if any error occurs getting the field value (excluding type cast errors)
     * @throws ClassCastException         if the given return type is not correct
     */
    public static <T> T getTypedFieldValue(Object target, String fieldName, Class<T> type) {
        return type.cast(getFieldValue(target, fieldName));
    }

    /**
     * Get the value of a specific field in an object.
     * <p>
     * Note that if the field is not public, this method uses {@link Field#setAccessible(boolean)}
     * to make it accessible and thus is subject to a {@link SecurityException} or
     * {@link java.lang.reflect.InaccessibleObjectException}.
     *
     * @param target    the target object
     * @param fieldName the field name
     * @return the field value
     * @throws RuntimeReflectionException if any error occurs finding the field or getting its value
     */
    public static Object getFieldValue(Object target, String fieldName) {
        var field = findField(target, fieldName);
        return getFieldValue(target, field);
    }

    /**
     * Get the value of a specific field in an object, cast to the specified type.
     *
     * @param target the target object
     * @param field  the field object
     * @param type   the type of object to return
     * @param <T>    the type parameter
     * @return the field value
     * @throws RuntimeReflectionException if any error occurs getting the field value (excluding type
     *                                    cast errors)
     * @throws ClassCastException         if the given return type is not correct
     */
    public static <T> T getTypedFieldValue(Object target, Field field, Class<T> type) {
        return type.cast(getFieldValue(target, field));
    }

    /**
     * Get the value of a specific field in an object.
     *
     * @param target the target object
     * @param field  the field object
     * @return the field value
     * @throws RuntimeReflectionException if any error occurs getting the field
     */
    public static Object getFieldValue(Object target, Field field) {
        try {
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeReflectionException(
                    f("Cannot get value of field [%s] in object [%s]", field.getName(), target),
                    e);
        }
    }

    /**
     * Sets a value directly into the specified field in the target object.
     * <p>
     * Subject to the restrictions of and exceptions thrown by {@link Field#set(Object, Object)}.
     * <p>
     * Note that if the field is not public, this method uses {@link Field#setAccessible(boolean)}
     * to make it accessible and thus is subject to a {@link SecurityException} or
     * {@link java.lang.reflect.InaccessibleObjectException}.
     *
     * @param target    the target object in which the field resides
     * @param fieldName the field name
     * @param value     the new value
     * @throws RuntimeReflectionException if any error occurs while setting the field value
     * @see Field#set(Object, Object)
     */
    public static void setFieldValue(Object target, String fieldName, Object value) {
        var field = findField(target, fieldName);
        setFieldValue(target, field, value);
    }

    /**
     * Sets a value directly into the specified field in the target object.
     * <p>
     * Subject to the restrictions of and exceptions thrown by {@link Field#set(Object, Object)}.
     *
     * @param target the target object in which the field resides
     * @param field  the field to set
     * @param value  the new value
     * @throws RuntimeReflectionException if any error occurs while setting the field value
     * @see Field#set(Object, Object)
     */
    public static void setFieldValue(Object target, Field field, Object value) {
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeReflectionException(
                    f("Error setting field [%s] on target [%s] with value [%s]", field.getName(), target, value),
                    e
            );
        }
    }

    /**
     * Builds a list of all the <em>declared</em> non-static fields in an object including all parent non-static fields.
     *
     * @param type the class to extract fields from
     * @return the list of fields in the given class plus fields from all ancestor (parent) classes
     * @see Class#getDeclaredFields()
     */
    public static List<Field> nonStaticFieldsInHierarchy(Class<?> type) {
        var fields = new ArrayList<Field>();

        var currentType = type;
        while (nonNull(currentType)) {
            var nonStaticFields = Arrays.stream(currentType.getDeclaredFields())
                    .filter(not(KiwiReflection::isStatic))
                    .toList();

            fields.addAll(nonStaticFields);

            currentType = currentType.getSuperclass();
        }

        return fields;
    }

    private static boolean isStatic(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }

    /**
     * Finds all public accessor methods ({@code getXxx} / {@code isXxx} conforming to JavaBeans rules) in
     * the class of the given object (including superclasses).
     *
     * @param target the target object
     * @return list of public "getter" methods
     * @see Class#getMethods()
     */
    public static List<Method> findPublicAccessorMethods(Object target) {
        var clazz = target.getClass();
        return findPublicAccessorMethods(clazz);
    }

    /**
     * Finds all public accessor methods ({@code getXxx} / {@code isXxx} conforming to JavaBeans rules) in
     * the given class (including superclasses).
     *
     * @param clazz the target class
     * @return list of public "getter" methods
     * @see Class#getMethods()
     */
    public static List<Method> findPublicAccessorMethods(Class<?> clazz) {
        return findPublicMethods(clazz, KiwiReflection::isPublicAccessorMethod);
    }

    /**
     * Checks whether the given method is a public accessor method ({@code getXxx} / {@code isXxx} conforming to
     * JavaBeans rules).
     *
     * @param method the method to check
     * @return true if method is a public accessor, false otherwise
     */
    public static boolean isPublicAccessorMethod(Method method) {
        return isNotGetClassMethod(method) && isGetOrIsAccessorMethod(method);
    }

    private static boolean isGetOrIsAccessorMethod(Method method) {
        return isStrictlyGetAccessorMethod(method) || isStrictlyIsAccessorMethod(method);
    }

    /**
     * Checks whether the given method is a public accessor method (only {@code getXxx} conforming to
     * JavaBeans rules).
     * <p>
     * Note this explicitly excludes the {@link Object#getClass()} method.
     *
     * @param method the method to check
     * @return true if the method is a public "getXxx" method, false otherwise
     */
    public static boolean isStrictlyGetAccessorMethod(Method method) {
        return isPublicMethodWithParameterCount(method, 0) &&
                method.getName().startsWith("get") &&
                isNotGetClassMethod(method) &&
                !method.getReturnType().equals(Void.TYPE);
    }

    private static boolean isNotGetClassMethod(Method method) {
        return !"getClass".equals(method.getName());
    }

    /**
     * Checks whether the given method is a public accessor method (only {@code isXxx} conforming to
     * JavaBeans rules).
     *
     * @param method the method to check
     * @return true if the method is a public "isXxx" method, false otherwise
     */
    public static boolean isStrictlyIsAccessorMethod(Method method) {
        return isPublicMethodWithParameterCount(method, 0) &&
                method.getName().startsWith("is") &&
                method.getReturnType().equals(Boolean.TYPE);
    }

    /**
     * Finds all public mutator methods ({@code setXxx} conforming to JavaBeans rules) in the class of
     * the given object (including superclasses).
     *
     * @param target the target object
     * @return list of public "setter" methods
     * @see Class#getMethods()
     */
    public static List<Method> findPublicMutatorMethods(Object target) {
        var clazz = target.getClass();
        return findPublicMutatorMethods(clazz);
    }

    /**
     * Finds all public mutator methods ({@code setXxx} conforming to JavaBeans rules) in the given
     * class (including superclasses).
     *
     * @param clazz the target class
     * @return list of public "setter" methods
     * @see Class#getMethods()
     */
    public static List<Method> findPublicMutatorMethods(Class<?> clazz) {
        return findPublicMethods(clazz, KiwiReflection::isPublicMutatorMethod);
    }

    /**
     * Finds all public methods in the given class (including superclasses) that satisfy the given {@link Predicate}.
     *
     * @param clazz     the target class
     * @param predicate the predicate to satisfy
     * @return a list of methods
     * @see Class#getMethods()
     */
    public static List<Method> findPublicMethods(Class<?> clazz, Predicate<Method> predicate) {
        var methods = clazz.getMethods();
        return Arrays.stream(methods)
                .filter(predicate)
                .toList();
    }

    /**
     * Checks whether the given method is a public mutator method ({@code setXxx} conforming to JavaBeans rules).
     *
     * @param method the method to check
     * @return true if the method is a public "setXxx" method, false otherwise
     */
    public static boolean isPublicMutatorMethod(Method method) {
        return isPublicMethodWithParameterCount(method, 1) &&
                method.getReturnType().equals(Void.TYPE) &&
                method.getName().startsWith("set");
    }

    private static boolean isPublicMethodWithParameterCount(Method method, int desiredParameterCount) {
        return Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == desiredParameterCount;
    }

    /**
     * Finds public mutator methods for the given object, then for reference types invokes the mutator supplying
     * {@code null} as the argument.
     * <p>
     * The effect is thus to nullify the values of (mutable) properties in {@code target} having a
     * reference (non-primitive) type.
     *
     * @param target the object containing mutable properties exposed via public mutator methods
     * @see #findPublicMutatorMethods(Object)
     */
    public static void invokeMutatorMethodsWithNull(Object target) {
        invokeMutatorMethodsWithNullSatisfying(target, method -> true);
    }

    /**
     * Finds public mutator methods for the given object, then for reference types invokes the mutator supplying
     * {@code null} as the argument <em>except</em> for property names contained in {@code properties}.
     * <p>
     * The {@code properties} vararg specifies the names of the <em>properties</em> to <em>ignore</em>. These names
     * will be translated into the corresponding "setter" method name. For example, if "dateOfBirth" is the property
     * name to ignore, then the "setDateOfBirth" method is the corresponding setter method, and it will be
     * ignored (not called).
     * <p>
     * The effect is thus to nullify the values of (mutable) properties in {@code target} having a
     * reference (non-primitive) type, excluding the ones specified in {@code properties}.
     *
     * @param target     the object containing mutable properties exposed via public mutator methods
     * @param properties the property names to ignore, e.g. "firstName", "age", "zipCode"
     * @see #findPublicMutatorMethods(Object)
     */
    public static void invokeMutatorMethodsWithNullIgnoringProperties(Object target, String... properties) {
        var namesToIgnore = propertyNamesToSetterMethodNames(properties);
        invokeMutatorMethodsWithNullSatisfying(target, not(method -> namesToIgnore.contains(method.getName())));
    }

    /**
     * Finds public mutator methods for the given object, then for reference types invokes the mutator supplying
     * {@code null} as the argument <em>including only</em> the property names contained in {@code properties}.
     * <p>
     * The {@code properties} vararg specifies the names of the <em>properties</em> to <em>include</em>. These names
     * will be translated into the corresponding "setter" method name. For example, if "dateOfBirth" is the property
     * name to include, then the "setDateOfBirth" method is the corresponding setter method, and it will be called
     * with a null argument.
     * <p>
     * The effect is thus to nullify the values of (mutable) properties in {@code target} having a
     * reference (non-primitive) type, including only the ones specified in {@code properties}.
     *
     * @param target     the object containing mutable properties exposed via public mutator methods
     * @param properties the property names to include, e.g. "firstName", "age", "zipCode"
     * @see #findPublicMutatorMethods(Object)
     */
    public static void invokeMutatorMethodsWithNullIncludingOnlyProperties(Object target, String... properties) {
        var namesToInclude = propertyNamesToSetterMethodNames(properties);
        invokeMutatorMethodsWithNullSatisfying(target, method -> namesToInclude.contains(method.getName()));
    }

    private static Set<String> propertyNamesToSetterMethodNames(String[] names) {
        return Arrays.stream(names)
                .filter(not(StringUtils::isBlank))
                .map(name -> "set" + name.substring(0, 1).toUpperCase() + name.substring(1))
                .collect(toUnmodifiableSet());
    }

    /**
     * Finds public mutator methods for the given object, then for reference types invokes the mutator supplying
     * {@code null} as the argument <em>including only</em> methods that satisfy the given {@link Predicate}.
     * <p>
     * When {@code methodPredicate} returns {@code true}, the mutator method is called with a null argument. When
     * it returns {@code false} then the method is not called.
     * <p>
     * The effect is thus to nullify the values of (mutable) properties in {@code target} having a
     * reference (non-primitive) type, including only the ones for which {@code methodPredicate} returns {@code true}.
     *
     * @param target          the object containing mutable properties exposed via public mutator methods
     * @param methodPredicate a Predicate that tests whether to include a mutator method
     */
    public static void invokeMutatorMethodsWithNullSatisfying(Object target,
                                                              Predicate<Method> methodPredicate) {
        findPublicMutatorMethods(target)
                .stream()
                .filter(KiwiReflection::acceptsOneReferenceType)
                .filter(methodPredicate)
                .forEach(mutator -> invokeVoidReturn(mutator, target, ONE_NULL_ARG_OBJECT_ARRAY));
    }

    private static boolean acceptsOneReferenceType(Method method) {
        var parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 1 && !parameterTypes[0].isPrimitive();
    }

    /**
     * Finds a <em>public</em> method having the given name and parameter types in the given class.
     *
     * @param targetClass the class in which to look for the method
     * @param methodName  the name of the method to find
     * @param params      the parameter types of the method's argument list, if any
     * @return an {@link Optional} that will contain the {@link Method} if found. Otherwise, returns an
     * empty {@link Optional} in all other circumstances.
     * @see Class#getMethod(String, Class[])
     */
    public static Optional<Method> findMethodOptionally(Class<?> targetClass, String methodName, Class<?>... params) {
        return Optional.ofNullable(findMethodOrNull(targetClass, methodName, params));
    }

    /**
     * Finds an accessor method of the given {@code methodType} for the specified {@code fieldName} in {@code target}.
     *
     * @param methodType the type of method to find
     * @param fieldName  the field name
     * @param target     the target object
     * @param params     the method parameters
     * @return the found {@link Method}
     * @throws RuntimeReflectionException if any error occurs finding the accessor method
     */
    public static Method findAccessor(Accessor methodType,
                                      String fieldName,
                                      Object target,
                                      Class<?>... params) {

        return findAccessor(methodType, fieldName, requireNotNull(target).getClass(), params);
    }

    /**
     * Finds a <em>public</em> accessor method of the given {@code methodType} for the specified {@code fieldName} in
     * the given {@code targetClass}. For example, given "firstName" as the field name and {@link Accessor#SET}
     * as the method type, will attempt to find a public {@code setFirstName}
     * <p>
     * Handles primitive {@code boolean} vs. reference {@code Boolean} distinction transparently even if
     * {@link Accessor#GET} is passed as the method type for a primitive. In other words, this method will
     * return the {@code boolean isXxx()} method if provided {@link Accessor#GET} but the actual return type
     * is {@code primitive}.
     * <p>
     * Allows for having a naming convention of instance variables starting with an underscore, e.g.
     * {@code String _firstName}, by stripping underscores from the field name. The {@code fieldName} argument
     * can be supplied with or without a leading underscore. Note, however, that since this method removes all
     * underscores from the {@code fieldName}, oddly named fields may produce exceptions.
     *
     * @param methodType  the type of method to find
     * @param fieldName   the field name
     * @param targetClass the class in which the field resides
     * @param params      the method parameters (should be none for getters, and one for setters)
     * @return the found {@link Method}
     * @throws RuntimeReflectionException if any error occurs finding the accessor method
     */
    public static Method findAccessor(Accessor methodType,
                                      String fieldName,
                                      Class<?> targetClass,
                                      Class<?>... params) {

        checkArgumentNotNull(methodType, "methodType cannot be null");
        checkArgumentNotBlank(fieldName, "fieldName cannot be blank");
        checkArgumentNotNull(targetClass, "targetClass cannot be null");
        methodType.validateParameterCount(params);

        var strippedFieldName = Strings.CS.remove(fieldName, UNDERSCORE);
        var capitalizedFieldName = StringUtils.capitalize(strippedFieldName);
        var methodName = KiwiStrings.format(methodType.template, capitalizedFieldName);

        return switch (methodType) {
            case GET -> findGetOrIsMethod(targetClass, capitalizedFieldName, methodName);
            case IS -> findMethod(targetClass, methodName);
            case SET -> findSetMethod(targetClass, methodName, params[0]);
        };
    }

    private static Method findGetOrIsMethod(Class<?> targetClass, String capitalizedFieldName, String methodName) {
        var getMethod = findMethodOrNull(targetClass, methodName);
        if (nonNull(getMethod)) {
            return getMethod;
        }

        var alternateMethodName = KiwiStrings.format(Accessor.IS.template, capitalizedFieldName);
        LOG.trace("Falling back to see if {} exists (the field is a primitive boolean)", alternateMethodName);

        return findMethod(targetClass, alternateMethodName);
    }

    private static Method findSetMethod(Class<?> targetClass, String methodName, Class<?> param) {
        var setMethod = findMethodOrNull(targetClass, methodName, param);
        if (nonNull(setMethod)) {
            return setMethod;
        }

        LOG.trace("Falling back to see if {} exists for a primitive value", methodName);
        Class<?> primitiveParam = Primitives.unwrap(param);

        return findMethod(targetClass, methodName, primitiveParam);
    }

    @VisibleForTesting
    static Method findMethodOrNull(Class<?> targetClass, String methodName, Class<?>... params) {
        try {
            return targetClass.getMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            LOG.trace("Did not find method named {} in {} with parameter types {}",
                    methodName, targetClass, Arrays.toString(params), e);
            return null;
        } catch (Exception e) {
            LOG.warn("Error finding method named {} in {} with parameter types {}",
                    methodName, targetClass, Arrays.toString(params), e);
            return null;
        }
    }

    /**
     * Finds a <em>public</em> method having the given name and parameter types in the given class.
     * <p>
     * Use this when you expect the method to exist. If you are not sure, then use
     * {@link #findMethodOptionally(Class, String, Class[])}.
     *
     * @param targetClass the class in which to look for the method
     * @param methodName  the name of the method to find
     * @param params      the parameter types of the method's argument list, if any
     * @return the found {@link Method} object
     * @throws RuntimeReflectionException if any error occurs finding the method
     * @see Class#getMethod(String, Class[])
     */
    public static Method findMethod(Class<?> targetClass, String methodName, Class<?>... params) {
        try {
            return targetClass.getMethod(methodName, params);
        } catch (Exception e) {
            var error = f("Error finding method [%s] in [%s] with params %s",
                    methodName, targetClass, Arrays.toString(params));
            throw new RuntimeReflectionException(error, e);
        }
    }

    /**
     * Invokes a method on an object expecting a return value of a specific type.
     *
     * @param method     the method to invoke
     * @param target     the object on which to invoke the method
     * @param returnType the expected return type
     * @param args       optionally, the method arguments
     * @param <T>        the return type parameter
     * @return result of calling the method (which could be {@code null}), cast to type {@code T}
     * @throws RuntimeReflectionException if any error occurs while invoking the method (excluding type cast errors)
     * @throws ClassCastException         if the given return type is not correct
     */
    public static <T> T invokeExpectingReturn(Method method, Object target, Class<T> returnType, Object... args) {
        return returnType.cast(invokeExpectingReturn(method, target, args));
    }

    /**
     * Invokes a method on an object expecting a return value.
     *
     * @param method the method to invoke
     * @param target the object on which to invoke the method
     * @param args   optionally, the method arguments
     * @return result of calling the method (which could be {@code null})
     * @throws RuntimeReflectionException if any error occurs while invoking the method
     * @see Method#invoke(Object, Object...)
     */
    public static Object invokeExpectingReturn(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeReflectionException(
                    f("Error invoking method [%s] on target [%s] with args %s",
                            method.getName(), target, Arrays.toString(args)),
                    e);
        }
    }

    /**
     * Invokes a method on an object expecting no return value.
     *
     * @param method the method to invoke
     * @param target the object on which to invoke the method
     * @param args   optionally, the method arguments
     * @throws RuntimeReflectionException if any error occurs while invoking the method
     * @see Method#invoke(Object, Object...)
     */
    public static void invokeVoidReturn(Method method, Object target, Object... args) {
        try {
            method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeReflectionException(
                    f("Error invoking void method [%s] on target [%s] with args %s",
                            method.getName(), target, Arrays.toString(args)),
                    e);
        }
    }

    /**
     * Convenience method to create a new instance of the given type using its no-args constructor.
     *
     * @param <T> the type of object
     * @param type the {@link Class} representing the object type
     * @return a new instance
     * @throws RuntimeReflectionException if any error occurs while invoking the constructor
     * @throws IllegalArgumentException if a no-args constructor does not exist, is private, etc.
     * @see Class#getDeclaredConstructor(Class...)
     * @see java.lang.reflect.Constructor#newInstance(Object...)
     */
    @Beta
    public static <T> T newInstanceUsingNoArgsConstructor(Class<T> type) {
        checkArgumentNotNull(type);
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            var message = f("{} does not have a declared no-args constructor", type);
            throw new IllegalArgumentException(message, e);
        } catch (IllegalAccessException e) {
            var message = f("{} has a no-args constructor that is not accessible", type);
            throw new IllegalArgumentException(message, e);
        } catch (Exception e) {
            throw new RuntimeReflectionException(e);
        }
    }

    /**
     * Create a new instance of the given type using {@code arguments} to determine the constructor
     * argument types, using the first matching constructor based on the argument types and actual
     * constructor parameters. A constructor will match if the constructor parameter type is assignable
     * from the argument type. For example, if a one-argument constructor accepts a {@link CharSequence} and
     * the actual argument type is {@link String}, the constructor matches since String is assignable to
     * CharSequence.
     * <p>
     * <em>Note that this method cannot be used if any of the arguments are {@code null}.</em> The reason
     * is that the type cannot be inferred. If any argument might be null, or you don't know or aren't sure, use
     * {@link #newInstance(Class, List, List)} instead.
     *
     * @param <T> the type of object
     * @param type the {@link Class} representing the object type
     * @param arguments the constructor arguments
     * @return a new instance
     * @throws IllegalArgumentException if no matching constructor was found for the given arguments
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws RuntimeReflectionException if any error occurs while invoking the constructor
     * @see Class#getDeclaredConstructors()
     * @see java.lang.reflect.Constructor#newInstance(Object...)
     */
    @SuppressWarnings("unchecked")
    @Beta
    public static <T> T newInstanceInferringParamTypes(Class<T> type, Object... arguments) {
        checkArgumentNotNull(type);
        checkArgumentNotNull(arguments);

        if (arguments.length == 0) {
            return newInstanceUsingNoArgsConstructor(type);
        }

        var exactArgTypes = getTypesOrThrow(arguments);

        var constructor = Arrays.stream(type.getDeclaredConstructors())
                .filter(ctor -> ctor.getParameterCount() == arguments.length)
                .filter(ctor -> argumentsAreCompatible(ctor.getParameterTypes(), exactArgTypes))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(f("No declared constructor found for argument types: {}", exactArgTypes)));

        try {
            return (T) constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeReflectionException(e);
        }
    }

    private static List<Class<?>> getTypesOrThrow(Object... objects) {
        try {
            return Arrays.stream(objects).<Class<?>>map(Object::getClass).toList();
        } catch (NullPointerException npe) {
            throw new NullPointerException("Cannot infer types because one (or more) arguments is null");
        }
    }

    private static boolean argumentsAreCompatible(Class<?>[] parameterTypes, List<Class<?>> exactArgTypes) {
        for (var i = 0; i < parameterTypes.length; i++) {
            var paramType = parameterTypes[i];
            var exactArgType = exactArgTypes.get(i);

            if (!paramType.isAssignableFrom(exactArgType) && !matchesPrimitive(paramType, exactArgType)) {
                return false;
            }
        }

        return true;
    }

    @VisibleForTesting
    static boolean matchesPrimitive(Class<?> parameterType, Class<?> argType) {
        return parameterType.isPrimitive() &&
                ((parameterType.equals(byte.class) && argType.equals(Byte.class))
                        || (parameterType.equals(short.class) && argType.equals(Short.class))
                        || (parameterType.equals(int.class) && argType.equals(Integer.class))
                        || (parameterType.equals(long.class) && argType.equals(Long.class))
                        || (parameterType.equals(float.class) && argType.equals(Float.class))
                        || (parameterType.equals(double.class) && argType.equals(Double.class))
                        || (parameterType.equals(boolean.class) && argType.equals(Boolean.class))
                        || (parameterType.equals(char.class) && argType.equals(Character.class)));
    }

    /**
     * This method is an alias for {@link #newInstance(Class, List, List)}, with the arguments as varargs, which
     * may be more convenient in some situations. See that method's Javadoc for more details, including the types
     * of exceptions that can be thrown.
     *
     * @param <T> the type of object
     * @param type the {@link Class} representing the object type
     * @param parameterTypes the constructor parameter types
     * @param arguments the constructor arguments; individual arguments may be null (but note that a single literal
     * null argument is ambiguous, and must be cast to make the intent clear to the compiler)
     * @return a new instance
     * @see #newInstance(Class, List, List)
     * @apiNote This method is named differently than {@link #newInstance(Class, List, List)} to avoid amiguity
     * that happens with method overloads when one (or more) uses varargs and the others don't.
     */
    @Beta
    public static <T> T newInstanceExactParamTypes(Class<T> type, List<Class<?>> parameterTypes, Object... arguments) {
        checkArgumentNotNull(arguments);
        return newInstance(type, parameterTypes, Lists.newArrayList(arguments));
    }

    /**
     * Create a new instance of the given type using a constructor having the given parameter types, and supplying
     * the arguments to that constructor.
     * <p>
     * <em>Note that the parameter types must match exactly.</em>
     *
     * @param <T> the type of object
     * @param type the {@link Class} representing the object type
     * @param parameterTypes the constructor parameter types
     * @param arguments the constructor arguments; individual arguments may be null
     * @return a new instance
     * @throws IllegalArgumentException if any of the arguments is null, if the length of parameter types and
     * arguments is different, or if no constructor exists with the given parameter types
     * @throws RuntimeReflectionException if any error occurs while invoking the constructor
     * @see Class#getDeclaredConstructor(Class...)
     * @see java.lang.reflect.Constructor#newInstance(Object...)
     */
    public static <T> T newInstance(Class<T> type, List<Class<?>> parameterTypes, List<Object> arguments) {
        checkArgumentNotNull(type);
        checkArgumentNotNull(parameterTypes);
        checkArgumentNotNull(arguments);
        checkArgument(parameterTypes.size() == arguments.size(), "parameter types and arguments must have same size");

        try {
            var constructor = type.getDeclaredConstructor(parameterTypes.toArray(new Class[0]));
            return constructor.newInstance(arguments.toArray(new Object[0]));
        } catch (NoSuchMethodException e) {
            var message = f("No declared constructor exists in {} for parameter types: {}", type, parameterTypes);
            throw new IllegalArgumentException(message, e);
        } catch (IllegalAccessException e) {
            var message = f("{} has a constructor that is not accessible for parameter types: {}", type, parameterTypes);
            throw new IllegalArgumentException(message, e);
        } catch (Exception e) {
            throw new RuntimeReflectionException(e);
        }
    }
}
