package org.kiwiproject.validation;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiSets.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiSets.isNullOrEmpty;
import static org.kiwiproject.stream.KiwiMultimapCollectors.toLinkedHashMultimap;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Static utilities for working with {@link jakarta.validation.ConstraintViolation} objects, generally
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
     * Convert the set of {@link ConstraintViolation} to a map keyed by the property path.
     * <p>
     * The map's values are the single {@link ConstraintViolation} associated with each property.
     * <p>
     * <strong>WARNING:</strong>
     * An {@link IllegalStateException} is thrown if there is more than one violation associated
     * with any key. Therefore, this method should only be used if you are sure there can only
     * be at most one violation per property. Otherwise, use either {@link #asMultiValuedMap(Set)}
     * or {@link #asSingleValuedMap(Set)}.
     *
     * @param violations set of non-null but possibly empty violations
     * @param <T>        the type of the root bean that was validated
     * @return a map whose keys are the property path of the violations, and values are the violations
     * @throws IllegalStateException if there is more than one violation associated with any key
     * @see #asSingleValuedMap(Set)
     * @see #asMultiValuedMap(Set)
     */
    public static <T> Map<String, ConstraintViolation<T>> asMap(Set<ConstraintViolation<T>> violations) {
        return asMap(violations, Path::toString);
    }

    /**
     * Convert the set of {@link ConstraintViolation} to a map keyed by the property path.
     * The property path is determined by the {@code pathTransformer}.
     * <p>
     * The map's values are the single {@link ConstraintViolation} associated with each property.
     * <p>
     * <strong>WARNING:</strong>
     * An {@link IllegalStateException} is thrown if there is more than one violation associated
     * with any key. Therefore, this method should only be used if you are sure there can only
     * be at most one violation per property. Otherwise, use either {@link #asMultiValuedMap(Set)}
     * or {@link #asSingleValuedMap(Set)}.
     *
     * @param violations      set of non-null but possibly empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             the type of the root bean that was validated
     * @return a map whose keys are the property path of the violations, and values are the violations
     * @throws IllegalStateException if there is more than one violation associated with any key
     * @see #asSingleValuedMap(Set)
     * @see #asMultiValuedMap(Set)
     */
    public static <T> Map<String, ConstraintViolation<T>> asMap(Set<ConstraintViolation<T>> violations,
                                                                Function<Path, String> pathTransformer) {
        return violations.stream().collect(toMap(
                violation -> pathTransformer.apply(violation.getPropertyPath()),
                violation -> violation));
    }

    /**
     * Convert the set of {@link ConstraintViolation} to a map keyed by the property path.
     * <p>
     * The map's values are the <em>last</em> {@link ConstraintViolation} associated with each property.
     * The definition of "last" depends on the iteration order of the provided set of violations, which
     * may be non-deterministic if the set does not have a well-defined traversal order.
     * <p>
     * <strong>WARNING:</strong>
     * If there is more than one violation associated with any key, the <em>last</em> violation, as
     * determined by the set traversal order, becomes they key. If you need to retain all violations
     * associated with each key, use {@link #asMultiValuedMap(Set)}.
     *
     * @param violations set of non-null but possibly empty violations
     * @param <T>        the type of the root bean that was validated
     * @return a map whose keys are the property path of the violations, and values are the violations
     * @see #asMultiValuedMap(Set)
     */
    public static <T> Map<String, ConstraintViolation<T>> asSingleValuedMap(Set<ConstraintViolation<T>> violations) {
        return asSingleValuedMap(violations, Path::toString);
    }

    /**
     * Convert the set of {@link ConstraintViolation} to a map keyed by the property path.
     * The property path is determined by the {@code pathTransformer}.
     * <p>
     * The map's values are the <em>last</em> {@link ConstraintViolation} associated with each property.
     * The definition of "last" depends on the iteration order of the provided set of violations, which
     * may be non-deterministic if the set does not have a well-defined traversal order.
     * <p>
     * <strong>WARNING:</strong>
     * If there is more than one violation associated with any key, the <em>last</em> violation, as
     * determined by the set traversal order, becomes they key. If you need to retain all violations
     * associated with each key, use {@link #asMultiValuedMap(Set)}.
     *
     * @param violations      set of non-null but possibly empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             the type of the root bean that was validated
     * @return a map whose keys are the property path of the violations, and values are the violations
     * @see #asMultiValuedMap(Set)
     */
    public static <T> Map<String, ConstraintViolation<T>> asSingleValuedMap(Set<ConstraintViolation<T>> violations,
                                                                            Function<Path, String> pathTransformer) {
        return violations.stream().collect(toMap(
                violation -> pathTransformer.apply(violation.getPropertyPath()),
                violation -> violation,
                (violation1, violation2) -> violation2));
    }

    /**
     * Convert the set of {@link ConstraintViolation} to a map keyed by the property path.
     * <p>
     * The map's values are the set of {@link ConstraintViolation} associated with each property.
     *
     * @param violations set of non-null but possibly empty violations
     * @param <T>        the type of the root bean that was validated
     * @return a map whose keys are the property path of the violations, and values are a Set containing
     * violations for the corresponding property
     */
    public static <T> Map<String, Set<ConstraintViolation<T>>> asMultiValuedMap(Set<ConstraintViolation<T>> violations) {
        return asMultiValuedMap(violations, Path::toString);
    }

    /**
     * Convert the set of {@link ConstraintViolation} to a map keyed by the property path.
     * The property path is determined by the {@code pathTransformer}.
     * <p>
     * The map's values are the set of {@link ConstraintViolation} associated with each property.
     *
     * @param violations      set of non-null but possibly empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             the type of the root bean that was validated
     * @return a map whose keys are the property path of the violations, and values are a Set containing
     * violations for the corresponding property
     */
    public static <T> Map<String, Set<ConstraintViolation<T>>> asMultiValuedMap(Set<ConstraintViolation<T>> violations,
                                                                                Function<Path, String> pathTransformer) {
        return violations.stream().collect(
                groupingBy(violation -> pathTransformer.apply(violation.getPropertyPath()), toSet()));
    }

    /**
     * Convert the set of {@link ConstraintViolation} to a {@link Multimap} keyed by the property path.
     *
     * @param violations set of non-null but possibly empty violations
     * @param <T>        the type of the root bean that was validated
     * @return a {@link Multimap} whose keys are the property path of the violations, and values contain
     * the violations for the corresponding property
     * @implNote The returned value is a {@link com.google.common.collect.LinkedHashMultimap}; the iteration
     * order of the values for each key is always the order in which the values were added, and there
     * cannot be duplicate values for a key.
     */
    public static <T> Multimap<String, ConstraintViolation<T>> asMultimap(Set<ConstraintViolation<T>> violations) {
        return asMultimap(violations, Path::toString);
    }

    /**
     * Convert the set of {@link ConstraintViolation} to a {@link Multimap} keyed by the property path.
     *
     * @param violations      set of non-null but possibly empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             the type of the root bean that was validated
     * @return a {@link Multimap} whose keys are the property path of the violations, and values contain
     * the violations for the corresponding property
     * @implNote The returned value is a {@link com.google.common.collect.LinkedHashMultimap}; the iteration
     * order of the values for each key is always the order in which the values were added, and there
     * cannot be duplicate values for a key.
     */
    public static <T> Multimap<String, ConstraintViolation<T>> asMultimap(Set<ConstraintViolation<T>> violations,
                                                                          Function<Path, String> pathTransformer) {
        return violations.stream()
                .map(violation -> Maps.immutableEntry(pathTransformer.apply(violation.getPropertyPath()), violation))
                .collect(toLinkedHashMultimap());
    }

    /**
     * Convenience method to get the property path of the {@link ConstraintViolation} as a String.
     * <p>
     * Please refer to the Implementation Note for details on the structure of the returned values
     * and <em>warnings</em> about that structure.
     *
     * @param violation the constraint violation
     * @param <T>       the type of the root bean that was validated
     * @return the property path of the violation, as a String
     * @implNote This uses {@link ConstraintViolation#getPropertyPath()} to obtain a {@link Path}
     * and then calls {@link Path#toString()} to get the final value. Therefore, the issues on
     * {@link Path#toString()} with regard to the structure of the return value apply here as well.
     * However, in many years of usage, the implementation (in Hibernate Validator anyway) has
     * always returned the same expected result, and is <em>generally</em> what you expect.
     * <p>
     * The main exception is iterable types, such as Set, that don't have a consistent traversal
     * order. For example, if you have a property named "nicknames" declared as
     * {@code Set<@NotBlank String> nicknames}, the property path for violation errors
     * look like {@code "nicknames[].<iterable element>"}.
     * <p>
     * Maps look similar to Sets. For example, in the Hibernate Validator reference
     * documentation, one example shows the property path of a constraint violation
     * on a Map as {@code "fuelConsumption[HIGHWAY].<map value>"}, and similarly on
     * a Map value as {@code "fuelConsumption<K>[].<map key>"}.
     * <p>
     * Indexed properties such as a List look more reasonable. For example, suppose a property
     * named "passwordHints" is declared as {@code List<@NotNull @Valid Hint> passwordHints},
     * and that {@code Hint} contains a String property named {@code text}. The property
     * path for violation errors includes the zero-based index as well as the path. For
     * example, if the second password hint is not valid, the property path is
     * {@code passwordHints[1].text}.
     */
    public static <T> String pathStringOf(ConstraintViolation<T> violation) {
        return violation.getPropertyPath().toString();
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a single string containing all violation messages
     * separated by commas. If the given set is empty (or null), then throw IllegalArgumentException.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return the combined error message
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> String simpleCombinedErrorMessage(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessage(violations, Objects::toString);
    }

    /**
     * Given a set of non-empty violations, produce a single string containing all violation messages separated
     * by commas. If the given set is empty (or null), then return null.
     *
     * @param violations set of violations
     * @param <T>        type of object being validated
     * @return the combined error message, or null
     */
    public static <T> String simpleCombinedErrorMessageOrNull(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessageOrNull(violations, Objects::toString);
    }

    /**
     * Given a set of non-empty violations, produce a single string containing all violation messages separated
     * by commas. If the given set is empty (or null), then return an empty Optional.
     *
     * @param violations set of violations
     * @param <T>        type of object being validated
     * @return the combined error message, or en empty Optional
     */
    public static <T> Optional<String> simpleCombinedErrorMessageOrEmpty(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessageOrEmpty(violations, Objects::toString);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a single string containing all violation messages
     * separated by commas. Each property name is "prettified" by converting {@code camelCase} to sentence case,
     * for example {@code firstName} becomes "First Name" in the resulting error message.
     * If the given set is empty (or null), then throw IllegalArgumentException.
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
     * Given a non-empty set of violations, produce a single string containing all violation messages
     * separated by commas. If the given set is empty (or null), then return null.
     * <p>
     * Each property name is "prettified" by converting {@code camelCase} to sentence case,
     * for example {@code firstName} becomes "First Name" in the resulting error message.
     *
     * @param violations set of violations
     * @param <T>        type of object being validated
     * @return the combined error message, or null
     */
    public static <T> String prettyCombinedErrorMessageOrNull(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessageOrNull(violations, KiwiConstraintViolations::humanize);
    }

    /**
     * Given a non-empty set of violations, produce a single string containing all violation messages
     * separated by commas. If the given set is empty (or null), then return an empty Optional.
     *
     * @param violations set of violations
     * @param <T>        type of object being validated
     * @return the combined error message, or an empty Optional
     */
    public static <T> Optional<String> prettyCombinedErrorMessageOrEmpty(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessageOrEmpty(violations, KiwiConstraintViolations::humanize);
    }

    /**
     * Given a <em>non-empty</em> set of violations, produce a single string containing all violation messages
     * separated by commas. Each property name is transformed using the specified {@code pathTransformer} function.
     * If the given set is empty (or null), then throw IllegalArgumentException.
     *
     * @param violations      set of non-empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return the combined error message
     * @throws IllegalArgumentException if violations is null or empty
     */
    public static <T> String combinedErrorMessage(Set<ConstraintViolation<T>> violations,
                                                  Function<Path, String> pathTransformer) {
        checkNotNullOrEmpty(violations);
        checkArgumentNotNull(pathTransformer);
        return combinedErrorMessageOrEmpty(violations, pathTransformer).orElseThrow();
    }

    /**
     * Given a non-empty set of violations, produce a single string containing all violation messages
     * separated by commas. If the given set is empty (or null), then return null.
     * <p>
     * Each property name is transformed using the specified {@code pathTransformer} function.
     *
     * @param violations      set of violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return the combined error message, or null
     */
    public static <T> String combinedErrorMessageOrNull(Set<ConstraintViolation<T>> violations,
                                                        Function<Path, String> pathTransformer) {
        return combinedErrorMessageOrEmpty(violations, pathTransformer).orElse(null);
    }

    /**
     * Given a non-empty set of violations, produce a single string containing all violation messages
     * separated by commas. If the given set is empty (or null), then return an empty Optional.
     * <p>
     * Each property name is transformed using the specified {@code pathTransformer} function.
     *
     * @param violations      set of violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return the combined error message, or an empty Optional
     */
    public static <T> Optional<String> combinedErrorMessageOrEmpty(Set<ConstraintViolation<T>> violations,
                                                                   Function<Path, String> pathTransformer) {

        checkArgumentNotNull(pathTransformer);
        if (isNullOrEmpty(violations)) {
            return Optional.empty();
        }

        var result = violations.stream()
                .map(violation -> KiwiConstraintViolations.propertyAndErrorMessage(violation, pathTransformer))
                .sorted()
                .collect(joining(", "));

        return Optional.of(result);
    }

    /**
     * Given a non-empty set of violations, produce a list of strings containing all violation messages.
     * Each message will contain the property followed by the error message, e.g. "firstName must not be blank".
     * If the given set is empty (or null), then return an empty list.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a list of the error messages
     */
    public static <T> List<String> simpleCombinedErrorMessages(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessages(violations, Objects::toString);
    }

    /**
     * Given a non-empty set of violations, produce a list of strings containing all violation messages.
     * Each message will contain the "prettified" property name followed by the error message, e.g., for
     * a violation on the {@code firstName} property, the message would look like "First Name must not be blank".
     * If the given set is empty (or null), then return an empty list.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a list of the error messages
     */
    public static <T> List<String> prettyCombinedErrorMessages(Set<ConstraintViolation<T>> violations) {
        return combinedErrorMessages(violations, KiwiConstraintViolations::humanize);
    }

    /**
     * Given a non-empty set of violations, produce a list of strings containing all violation messages.
     * Each message will contain the transformed property name followed by the error message, e.g.
     * "firstName must not be blank". Each property name is transformed using the specified {@code pathTransformer}
     * function. If the given set is empty (or null), then return an empty list.
     *
     * @param violations      set of non-empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return a list of the error messages
     */
    public static <T> List<String> combinedErrorMessages(Set<ConstraintViolation<T>> violations,
                                                         Function<Path, String> pathTransformer) {
        checkArgumentNotNull(pathTransformer);

        if (isNullOrEmpty(violations)) {
            return List.of();
        }

        return violations.stream()
                .map(violation -> KiwiConstraintViolations.propertyAndErrorMessage(violation, pathTransformer))
                .sorted()
                .toList();
    }

    /**
     * Given a non-empty set of violations, produce map whose keys are the properties and the corresponding
     * values are strings containing all violation messages. If the given set is empty (or null), then return an empty
     * map.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a map of error messages
     */
    public static <T> Map<String, String> simpleCombineErrorMessagesIntoMap(Set<ConstraintViolation<T>> violations) {
        return combineErrorMessagesIntoMap(violations, Objects::toString);
    }

    /**
     * Given a non-empty set of violations, produce map whose keys are the "prettified" properties and the
     * corresponding values are strings containing all violation messages. If the given set is empty (or null), then
     * return an empty map.
     *
     * @param violations set of non-empty violations
     * @param <T>        type of object being validated
     * @return a map of error messages
     */
    public static <T> Map<String, String> prettyCombineErrorMessagesIntoMap(Set<ConstraintViolation<T>> violations) {
        return combineErrorMessagesIntoMap(violations, KiwiConstraintViolations::humanize);
    }

    /**
     * Given a non-empty set of violations, produce map whose keys are the transformed properties and the
     * corresponding values are strings containing all violation messages. Each property name is transformed using
     * the specified {@code pathTransformer} function. If the given set is empty (or null), then return an empty map.
     *
     * @param violations      set of non-empty violations
     * @param pathTransformer function to convert a Path into a String
     * @param <T>             type of object being validated
     * @return a map of error messages
     */
    public static <T> Map<String, String> combineErrorMessagesIntoMap(Set<ConstraintViolation<T>> violations,
                                                                      Function<Path, String> pathTransformer) {
        checkArgumentNotNull(pathTransformer);

        if (isNullOrEmpty(violations)) {
            return Map.of();
        }

        return violations.stream()
                .collect(toUnmodifiableMap(
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
     * For example, contactInfo.email.address using ":" as the path separator would result in Contact Info:Email:Address.
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

    private static <T> void checkNotNullOrEmpty(Set<ConstraintViolation<T>> violations) {
        checkArgument(isNotNullOrEmpty(violations), "There are no violations to combine");
    }

    private static <T> String propertyAndErrorMessage(ConstraintViolation<T> violation,
                                                      Function<Path, String> pathTransformer) {
        return pathTransformer.apply(violation.getPropertyPath()) + " " + violation.getMessage();
    }
}
