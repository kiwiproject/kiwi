package org.kiwiproject.validation;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiSets.isNotNullOrEmpty;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Static utilities for working with {@link javax.validation.ConstraintViolation} objects, generally
 * {@link java.util.Set}s of them.
 * <p>
 * <strong>Dependency requirements:</strong>
 * <p>
 * The {@code jakarta.validation:jakarta.validation-api} dependency and some implementation such as Hibernate Validator
 * ({@code org.hibernate.validator:hibernate-validator} must be available at runtime.
 * <p>
 * In addition, currently the "pretty" methods use the {@code #humanize} methods, which rely on {@link WordUtils} from
 * commons-text. So if you use any of these, you will need to ensure {@code org.apache.commons:commons-text} is
 * available at runtime.
 */
@UtilityClass
public class KiwiConstraintViolations {

    /**
     * Given a <em>non-empty</em> set of violations, produce a single string containing all violation messages
     * separated by commas.
     *
     * @param violations set of non-empty violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return the combined error message
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> String simpleCombinedErrorMessage(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessage(violations, Objects::toString);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a single string containing all violation messages
     * separated by commas. Each property name is "prettified" by converting {@code camelCase} to sentence case,
     * for example {@code firstName} becomes "First Name" in the resulting error message.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return the combined error message
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> String prettyCombinedErrorMessage(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessage(violations, KiwiConstraintViolations::humanize);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a single string containing all violation messages
     * separated by commas. Each property name is transformed using the specified {@code pathTransformer} function.
     *
     * @param violations      set of non-empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return the combined error message
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> String combinedErrorMessage(Set<ConstraintViolation<T>> violations,
                                                  Function<Path, String> pathTransformer) {
        checkArgumentsForCombining(violations);
        checkArgumentNotNull(pathTransformer);
        return violations.stream()
                .map(violation -> KiwiConstraintViolations.propertyAndErrorMessage(violation, pathTransformer))
                .sorted()
                .collect(joining(", "));
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a list of strings containing all violation messages.
     * Each message will contain the property followed by the error message, e.g. "firstName must not be blank".
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a list of the error messages
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> List<String> simpleCombinedErrorMessages(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessages(violations, Objects::toString);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a list of strings containing all violation messages.
     * Each message will contain the "prettified" property name followed by the error message, e.g. for
     * a violation on the {@code firstName} property, the message would look like "First Name must not be blank".
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a list of the error messages
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> List<String> prettyCombinedErrorMessages(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessages(violations, KiwiConstraintViolations::humanize);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a list of strings containing all violation messages.
     * Each message will contain the transformed property name followed by the error message, e.g.
     * "firstName must not be blank". Each property name is transformed using the specified {@code pathTransformer}
     * function.
     *
     * @param violations      set of non-empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return a list of the error messages
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> List<String> combinedErrorMessages(Set<ConstraintViolation<T>> violations,
                                                         Function<Path, String> pathTransformer) {
        checkArgumentsForCombining(violations);
        checkArgumentNotNull(pathTransformer);
        return violations.stream()
                .map(violation -> KiwiConstraintViolations.propertyAndErrorMessage(violation, pathTransformer))
                .sorted()
                .collect(toList());
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce map whose keys are the properties and the corresponding
     * values are strings containing all violation messages.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a map of error messages
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> Map<String, String> simpleCombineErrorMessagesIntoMap(Set<ConstraintViolation<T>> violations) {
        return combineErrorMessagesIntoMap(violations, Objects::toString);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce map whose keys are the "prettified" properties and the
     * corresponding values are strings containing all violation messages.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a map of error messages
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> Map<String, String> prettyCombineErrorMessagesIntoMap(Set<ConstraintViolation<T>> violations) {
        return combineErrorMessagesIntoMap(violations, KiwiConstraintViolations::humanize);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce map whose keys are the transformed properties and the
     * corresponding values are strings containing all violation messages. Each property name is transformed using
     * the specified {@code pathTransformer} function.
     *
     * @param violations      set of non-empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return a map of error messages
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> Map<String, String> combineErrorMessagesIntoMap(Set<ConstraintViolation<T>> violations,
                                                                      Function<Path, String> pathTransformer) {
        checkArgumentsForCombining(violations);
        checkArgumentNotNull(pathTransformer);
        return violations.stream()
                .collect(toMap(
                        violation -> pathTransformer.apply(violation.getPropertyPath()),
                        ConstraintViolation::getMessage,
                        (accumulatedMessage, newErrorMessage) -> accumulatedMessage + ", " + newErrorMessage));
    }

    /**
     * Transforms the given property path into a human-readable version. Nested paths are separated by
     * a slash character. Examples:
     * <ul>
     *     <li>age becomes Age</li>
     *     <li>firstName becomes First Name</li>
     *     <li>contactInfo.email.address becomes Contact Info / Email / Address</li>
     * </ul>
     *
     * @param propertyPath the property path from a {@link ConstraintViolation}
     * @return a human-readable path
     * @throws IllegalArgumentException if either argument is null
     */
    public static String humanize(Path propertyPath) {
        return humanize(propertyPath, "/");
    }

    /**
     * Transforms the give property path into a human-readable version. Nested paths are separated by
     * the given {@code pathSeparator}.
     * <p>
     * For example contactInfo.email.address using ":" as the path separator would result in Contact Info:Email:Address.
     *
     * @param propertyPath  the property path from a {@link ConstraintViolation}
     * @param pathSeparator the separator to use between path elements
     * @return a human-readable path
     * @throws IllegalArgumentException if either argument is null
     */
    public static String humanize(Path propertyPath, String pathSeparator) {
        checkArgumentNotNull(propertyPath, "propertyPath must not be null");
        checkArgumentNotNull(pathSeparator, "pathSeparator must not be null");
        var splat = StringUtils.splitByCharacterTypeCamelCase(propertyPath.toString());
        var joined = Arrays.stream(splat)
                .map(str -> ".".equals(str) ? pathSeparator : str)
                .collect(joining(" "));
        return WordUtils.capitalize(joined);
    }

    private static <T> void checkArgumentsForCombining(Set<ConstraintViolation<T>> violations) {
        checkArgument(isNotNullOrEmpty(violations), "There are no violations to combine");
    }

    private static <T> String propertyAndErrorMessage(ConstraintViolation<T> violation,
                                                      Function<Path, String> pathTransformer) {
        return pathTransformer.apply(violation.getPropertyPath()) + " " + violation.getMessage();
    }
}
