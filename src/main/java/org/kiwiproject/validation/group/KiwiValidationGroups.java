package org.kiwiproject.validation.group;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import org.kiwiproject.validation.KiwiValidations;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.Set;

/**
 * An opinionated class that makes working with validation groups easier...if you are OK with the restrictions this
 * utility imposes. Specifically, it assumes you only need three validation groups:
 * <ol>
 *     <li>
 *         a group that should be included only when validating "new" objects (e.g. objects that have been
 *         instantiated but not yet persisted to some data store)
 *     </li>
 *     <li>
 *         a group that should be included only when validating "existing" objects (e.g. an object was retrieved
 *         from an external data source and has been changed with new information
 *     </li>
 *     <li>
 *         a group that should always be included (this is the {@link Default} validation group)
 *     </li>
 * </ol>
 * To use this utility, annotate your classes with Jakarta Beans Validation annotations. If you only want an
 * annotation to be included for "new" objects, set the {@code groups} property to {@link NewObject}. If instead
 * you only want an annotation to be included for "existing" objects, set the {@code groups} property to
 * {@link ExistingObject}. If you always want a validation annotation to be included during validation, don't set
 * the {@code groups} property. Here's an example:
 * <pre>
 * {@literal @}Null(groups = NewObject.class)
 * {@literal @}NotNull(groups = ExistingObject.class)
 *  private Long id;
 *
 * {@literal @}NotBlank
 *  private String content;
 * </pre>
 * In this example code, {@code id} must be null when the object is "new" but must be non-null when the object "exists".
 * The {@code content} is always required. When performing the logic to insert a "new" object you would pass
 * {@link #newObjectGroups()} as the groups in {@link Validator#validate(Object, Class[])}. Similarly, you would
 * pass {@link #existingObjectGroups()} as the groups in {@link Validator#validate(Object, Class[])} when performing
 * update logic on "existing" objects. For example, assuming a static import:
 * <pre>
 * // validate a new object
 * var violations = validator.validate(aNewObject, newObjectGroups());
 *
 * // validate an existing object
 * var violations = validator.validate(anExistingObject, existingObjectGroups());
 * </pre>
 * For even more convenience, you can just use the various {@code validateXxx} methods, assuming as mentioned earlier
 * that you have annotated your classes with Jakarta Beans Validator annotations. A typical example is using
 * annotations such as {@link javax.validation.constraints.NotNull} and {@link javax.validation.constraints.Null}
 * and setting {@link NewObject} and {@link ExistingObject} as the value of the {@code groups} property, as shown
 * in the above code example. To validate a new object, you would then simply do the following:
 * <pre>
 * var violations = KiwiValidationGroups.validateNewObject(aNewObject);
 * </pre>
 * And to validate an existing object, you would do:
 * <pre>
 * var violations = KiwiValidationGroups.validateExistingObject(anExistingObject);
 * </pre>
 * Using this utility ensures objects are validated against all groups, unlike {@link javax.validation.GroupSequence}
 * whose behavior is to stop validating any subsequent groups once a group fails validation. For example, if the
 * the sequence is {@code { Default.class, NewObject.class }} and a constraint fails for the {@code Default} group, then
 * the constraints for {@code NewObject} <em>will not be evaluated</em>. In general this is not the behavior we want or
 * expect.
 * <p>
 * To reiterate, this utility is opinionated and therfore limited in that it only knows about {@link NewObject} and
 * {@link ExistingObject}, and expects them to always be validated along with the {@link Default} group. We have
 * found this to be useful in enough situations to include it in kiwi. But if you need more flexibility, don't
 * use this and instead just pass specific validation groups manually to the validate method in {@link Validator}.
 */
@UtilityClass
public class KiwiValidationGroups {

    private static final Class<?>[] NEW_OBJECT_GROUPS = new Class[]{NewObject.class, Default.class};

    private static final Class<?>[] EXISTING_OBJECT_GROUPS = new Class[]{ExistingObject.class, Default.class};

    private static final Validator DEFAULT_VALIDATOR = KiwiValidations.newValidator();

    /**
     * Use this when validating a new (transient) object. The groups include {@link NewObject} and {@link Default}.
     *
     * @return a new array containing the new object group classes
     * @see NewObject
     * @see Default
     */
    public static Class<?>[] newObjectGroups() {
        return Arrays.copyOf(NEW_OBJECT_GROUPS, NEW_OBJECT_GROUPS.length);
    }

    /**
     * Use this when validating an existing (persistent) object. The groups include {@link ExistingObject}
     * and {@link Default}.
     *
     * @return a new array containing the existing object group classes
     * @see ExistingObject
     * @see Default
     */
    public static Class<?>[] existingObjectGroups() {
        return Arrays.copyOf(EXISTING_OBJECT_GROUPS, EXISTING_OBJECT_GROUPS.length);
    }

    /**
     * Validate a new (transient) object using a default {@link Validator} instance.
     *
     * @param object the object to validate
     * @param <T>    the type of object being validated
     * @return validation results
     */
    public static <T> Set<ConstraintViolation<T>> validateNewObject(T object) {
        return validateNewObject(DEFAULT_VALIDATOR, object);
    }

    /**
     * Validate a new (transient) object using the given {@link Validator} instance.
     *
     * @param validator the {@link Validator} instance to use
     * @param object    the object to validate
     * @param <T>       the type of object being validated
     * @return validation results
     */
    public static <T> Set<ConstraintViolation<T>> validateNewObject(Validator validator, T object) {
        checkArgumentNotNull(validator);
        return validator.validate(object, NEW_OBJECT_GROUPS);
    }

    /**
     * Validate an existing (persistent) object using a default {@link Validator} instance.
     *
     * @param object the object to validate
     * @param <T>    the type of object being validated
     * @return validation results
     */
    public static <T> Set<ConstraintViolation<T>> validateExistingObject(T object) {
        return validateExistingObject(DEFAULT_VALIDATOR, object);
    }

    /**
     * Validate an existing (persistent) object using the given {@link Validator} instance.
     *
     * @param validator the {@link Validator} instance to use
     * @param object    the object to validate
     * @param <T>       the type of object being validated
     * @return validation results
     */
    public static <T> Set<ConstraintViolation<T>> validateExistingObject(Validator validator, T object) {
        checkArgumentNotNull(validator);
        return validator.validate(object, EXISTING_OBJECT_GROUPS);
    }
}
