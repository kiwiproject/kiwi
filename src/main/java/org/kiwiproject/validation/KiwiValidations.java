package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiStrings.format;
import static org.kiwiproject.collect.KiwiSets.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiSets.isNullOrEmpty;

import com.google.common.base.Preconditions;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.reflect.KiwiReflection;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Static utilities related to Jakarta Bean Validation (formerly Java Bean Validation).
 * Relies on the Bean Validation API.
 * <p>
 * <strong>Dependency requirements:</strong>
 * <p>
 * The {@code jakarta.validation:jakarta.validation-api} dependency and some implementation such as Hibernate Validator
 * ({@code org.hibernate.validator:hibernate-validator} must be available at runtime.
 */
@UtilityClass
@Slf4j
public class KiwiValidations {

    /**
     * A re-usable {@link Validator} instance built using {@link #newValidator()}.
     */
    private static Validator validatorInstance = newValidator();

    /**
     * Creates a new, default {@link Validator} instance using the default validator factory provided by the
     * underlying bean validation implementation, for example, Hibernate Validator.
     *
     * @return a new {@link Validator} instance
     */
    public static Validator newValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Return the re-usable (singleton) Validator instance.
     *
     * @return singleton {@link Validator} instance
     */
    public static Validator getValidator() {
        return validatorInstance;
    }

    /**
     * Reset the singleton Validator instance.
     * <p>
     * <strong>This is intended primarily for use by unit tests, to permit resetting the Validator.
     * Use with caution and remember: with great power, come great responsibility.</strong>
     *
     * @param newValidator the new Validator to use as the singleton instance
     * @implNote This method is intentionally not synchronized. Since it is expected only to be used once at
     * application startup, and during unit tests, we can skip adding synchronization to this and all
     * other methods that use the singleton Validator instance. In other words, because we assume this will
     * not be called in real production code, except perhaps one time at application startup, we don't think
     * it's worth adding locking or synchronization on every method that uses the singleton Validator instance.
     */
    public static void setValidator(Validator newValidator) {
        LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOG.warn("               Overriding static Validator instance             !!!");
        LOG.warn("!!! You should only see this in tests or an application startup !!!");
        LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        validatorInstance = newValidator;
    }

    /**
     * Validate the given object using the singleton validator instance against the {@link Default} group.
     *
     * @param object the object to validate
     * @param <T>    the object type
     * @return validation results
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return validatorInstance.validate(object, Default.class);
    }

    /**
     * Validate the given object using the singleton validator instance against the specified validation groups.
     *
     * @param object       the object to validate
     * @param groupClasses zero or more validation group classes
     * @param <T>          the object type
     * @return validation results
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groupClasses) {
        return validatorInstance.validate(object, groupClasses);
    }

    /**
     * Validate the given object using the singleton validator instance against the {@link Default} group, and throw
     * a {@link ConstraintViolationException} if validation fails.
     *
     * @param object the object to validate
     * @param <T>    the object type
     */
    public static <T> void validateThrowing(T object) {
        var violations = KiwiValidations.validate(object);
        throwConstraintViolationExceptionIfNotEmpty(violations);
    }

    /**
     * Validate the given object using the singleton validator instance against the specified validation groups, and
     * throw a {@link ConstraintViolationException} if validation fails.
     *
     * @param object       the object to validate
     * @param groupClasses zero or more validation group classes
     * @param <T>          the object type
     */
    public static <T> void validateThrowing(T object, Class<?>... groupClasses) {
        var violations = KiwiValidations.validate(object, groupClasses);
        throwConstraintViolationExceptionIfNotEmpty(violations);
    }

    /**
     * If the set of constraint violations is not empty, throw a {@link ConstraintViolationException}.
     *
     * @param violations the constraint violations
     * @param <T>        the object type
     */
    public static <T> void throwConstraintViolationExceptionIfNotEmpty(Set<ConstraintViolation<T>> violations) {
        if (isNotNullOrEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }
    }

    /**
     * Validate the given object using the singleton validator instance against the {@link Default} group.
     * If the argument is not valid, throws an {@link IllegalArgumentException}. The exception message is
     * supplied by {@link #checkArgumentNoViolations(Set)}.
     *
     * @param object the object to validate
     * @param <T>    the object type
     * @see #checkArgumentNoViolations(Set)
     */
    public static <T> void checkArgumentValid(T object) {
        var violations = KiwiValidations.validate(object);
        checkArgumentNoViolations(violations);
    }

    /**
     * Validate the given object using the singleton validator instance against the {@link Default} group.
     * If the argument is not valid, throws an {@link IllegalArgumentException}.
     *
     * @param object       the object to validate
     * @param errorMessage the error message for the exception
     * @param <T>          the object type
     */
    public static <T> void checkArgumentValid(T object, String errorMessage) {
        var violations = KiwiValidations.validate(object);
        checkArgumentNoViolations(violations, errorMessage);
    }

    /**
     * Validate the given object using the singleton validator instance against the {@link Default} group.
     * If the argument is not valid, throws an {@link IllegalArgumentException}.
     *
     * @param object               the object to validate
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @param <T>                  the object type
     */
    public static <T> void checkArgumentValid(T object, String errorMessageTemplate, Object... errorMessageArgs) {
        var violations = KiwiValidations.validate(object);
        checkArgumentNoViolations(violations, errorMessageTemplate, errorMessageArgs);
    }

    /**
     * Validate the given object using the singleton validator instance against the {@link Default} group.
     * If the argument is not valid, throws an {@link IllegalArgumentException}.
     *
     * @param object              the object to validate
     * @param errorMessageCreator a Function that transforms constraint violations into an error message for the exception
     * @param <T>                 the object type
     */
    public static <T> void checkArgumentValid(T object,
                                              Function<Set<ConstraintViolation<T>>, String> errorMessageCreator) {
        var violations = KiwiValidations.validate(object);
        checkArgumentNoViolations(violations, errorMessageCreator);
    }

    /**
     * Validate the given object using the singleton validator instance against the specified validation groups.
     * If the argument is not valid, throws an {@link IllegalArgumentException}. The exception message is
     * supplied by {@link #checkArgumentNoViolations(Set)}.
     *
     * @param object       the object to validate
     * @param groupClasses zero or more validation group classes
     * @param <T>          the object type
     * @see #checkArgumentNoViolations(Set)
     */
    public static <T> void checkArgumentValid(T object, Class<?>... groupClasses) {
        var violations = KiwiValidations.validate(object, groupClasses);
        checkArgumentNoViolations(violations);
    }

    /**
     * Validate the given object using the singleton validator instance against the specified validation groups.
     * If the argument is not valid, throws an {@link IllegalArgumentException}.
     *
     * @param object       the object to validate
     * @param errorMessage the error message for the exception
     * @param groupClasses zero or more validation group classes
     * @param <T>          the object type
     */
    public static <T> void checkArgumentValid(T object, String errorMessage, Class<?>... groupClasses) {
        var violations = KiwiValidations.validate(object, groupClasses);
        checkArgumentNoViolations(violations, errorMessage);
    }

    /**
     * Validate the given object using the singleton validator instance against the specified validation groups.
     * If the argument is not valid, throws an {@link IllegalArgumentException}.
     *
     * @param object               the object to validate
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @param groupClasses         zero or more validation group classes
     * @param <T>                  the object type
     */
    public static <T> void checkArgumentValid(T object,
                                              String errorMessageTemplate,
                                              List<Object> errorMessageArgs,
                                              Class<?>... groupClasses) {
        var violations = KiwiValidations.validate(object, groupClasses);
        checkArgumentNoViolations(violations, errorMessageTemplate, errorMessageArgs.toArray());
    }

    /**
     * Validate the given object using the singleton validator instance against the specified validation groups.
     * If the argument is not valid, throws an {@link IllegalArgumentException}.
     *
     * @param object              the object to validate
     * @param errorMessageCreator a Function that transforms constraint violations into an error message for the exception
     * @param groupClasses        zero or more validation group classes
     * @param <T>                 the object type
     */
    public static <T> void checkArgumentValid(T object,
                                              Function<Set<ConstraintViolation<T>>, String> errorMessageCreator,
                                              Class<?>... groupClasses) {
        var violations = KiwiValidations.validate(object, groupClasses);
        checkArgumentNoViolations(violations, errorMessageCreator);
    }

    /**
     * Ensures the set of constraint violations is empty, throwing an {@link IllegalArgumentException} otherwise.
     * The exception message is supplied by {@link KiwiConstraintViolations#simpleCombinedErrorMessageOrNull(Set)}.
     *
     * @param violations the set of constraint violations to check
     * @param <T>        the object type
     */
    public static <T> void checkArgumentNoViolations(Set<ConstraintViolation<T>> violations) {
        checkArgumentNoViolations(violations, KiwiConstraintViolations::simpleCombinedErrorMessageOrNull);
    }

    /**
     * Ensures the set of constraint violations is empty, throwing an {@link IllegalArgumentException} otherwise.
     *
     * @param violations   the set of constraint violations to check
     * @param errorMessage the error message for the exception
     * @param <T>          the object type
     */
    public static <T> void checkArgumentNoViolations(Set<ConstraintViolation<T>> violations,
                                                     String errorMessage) {
        Preconditions.checkArgument(isNullOrEmpty(violations), errorMessage);
    }

    /**
     * Ensures the set of constraint violations is empty, throwing an {@link IllegalArgumentException} otherwise.
     *
     * @param violations           the set of constraint violations to check
     * @param errorMessageTemplate a template for the exception message should the check fail, according to how
     *                             {@link KiwiStrings#format(String, Object...)} handles placeholders
     * @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
     *                             are converted to Strings using {@link String#valueOf(Object)}.
     * @param <T>                  the object type
     */
    public static <T> void checkArgumentNoViolations(Set<ConstraintViolation<T>> violations,
                                                     String errorMessageTemplate,
                                                     Object... errorMessageArgs) {
        if (isNotNullOrEmpty(violations)) {
            var errorMessage = format(errorMessageTemplate, errorMessageArgs);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Ensures the set of constraint violations is empty, throwing an {@link IllegalArgumentException} otherwise.
     *
     * @param violations          the set of constraint violations to check
     * @param errorMessageCreator a Function that transforms constraint violations into an error message for the exception
     * @param <T>                 the object type
     */
    public static <T> void checkArgumentNoViolations(Set<ConstraintViolation<T>> violations,
                                                     Function<Set<ConstraintViolation<T>>, String> errorMessageCreator) {

        if (isNotNullOrEmpty(violations)) {
            var errorMessage = getErrorMessageOrFallback(violations, errorMessageCreator);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static <T> String getErrorMessageOrFallback(Set<ConstraintViolation<T>> violations,
                                                        Function<Set<ConstraintViolation<T>>, String> errorMessageCreator) {
        try {
            return errorMessageCreator.apply(violations);
        } catch (Exception e) {
            LOG.warn("errorMessageCreator threw exception creating message. Falling back to default message.", e);
        }

        return KiwiConstraintViolations
                .simpleCombinedErrorMessageOrEmpty(violations)
                .orElse("Argument contained one or more constraint violations");
    }

    /**
     * Adds an error to the {@link ConstraintValidatorContext} using the specified template, thereby overriding
     * the constraint's default message.
     * <p>
     * <strong>NOTE:</strong>
     * As of Hibernate Validator 6.2 and higher, expression language (EL) is <em>disabled</em> by default for
     * custom violations. This means custom validator error messages that use EL will show the uninterpolated
     * value in the message template, unless the custom validator has explicitly enabled EL. For example, given
     * a template: <code>"'${validatedValue}' is not a valid ACME, Inc. product code."</code>, the error message
     * will just be the template itself, so the user will see the literal <code>${validatedValue}</code> instead
     * of the value that was validated. See this <a href="https://github.com/kiwiproject/kiwi/discussions/690">discussion</a>
     * for additional information, and specifically see the References.
     *
     * @param context  the validator context
     * @param template the template to use
     * @see ConstraintValidatorContext#buildConstraintViolationWithTemplate(String)
     */
    public static void addError(ConstraintValidatorContext context, String template) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(template).addConstraintViolation();
    }

    /**
     * Adds an error to the {@link ConstraintValidatorContext} using the specified template and property name,
     * thereby overriding the constraint's default message.
     * <p>
     * This is intended to be used when a validation annotation applies at the type level, as opposed to a field
     * or method. The {@code propertyName} can be used to specify the property node that the violation should be
     * attached to. For example, if a validator validates multiple fields and the annotation is applied to the
     * type, then this can be used to specify which field a constraint violation applies to.
     * <p>
     * <strong>NOTE:</strong>
     * Please see the note in {@link #addError(ConstraintValidatorContext, String)} for important information
     * regarding expression language (EL) being disabled by default in Hibernate Validator 6.2 and higher for
     * custom violations.
     *
     * @param context      the validator context
     * @param template     the template to use
     * @param propertyName the property name to attach constrain violations to
     */
    public static void addError(ConstraintValidatorContext context, String template, String propertyName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(template)
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }

    /**
     * Finds the value of the specified property <em>by direct field access</em>.
     * <p>
     * This is provided for validators to obtain the value of a specific field, and will only be useful when
     * for validation annotations that are comparing multiple fields. For example, an annotation that
     * validates a range of values between two fields in an object.
     *
     * @param bean      the object
     * @param fieldName the property/field name
     * @return the value of the property in the object, <strong>or null</strong> if any problem occurs, {@code bean} is
     * null, or {@code fieldName} is blank
     * @implNote This uses {@link KiwiReflection#findField(Object, String)} to obtain the value.
     */
    @Nullable
    public static Object getPropertyValue(Object bean, String fieldName) {
        if (isNull(bean) || isBlank(fieldName)) {
            LOG.warn("bean is null or fieldName is blank; unable to continue so returning null");
            return null;
        }

        try {
            return KiwiReflection.findField(bean, fieldName).get(bean);
        } catch (Exception e) {
            LOG.warn("Unable to get value of property '{}' from object of type {}", fieldName, bean.getClass().getName(), e);
            return null;
        }
    }
}
