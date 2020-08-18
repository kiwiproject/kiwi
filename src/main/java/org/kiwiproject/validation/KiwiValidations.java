package org.kiwiproject.validation;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.reflect.KiwiReflection;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * Static utilities related to Jakarta Bean Validation (formerly Java Bean Validation).
 * Relies on the Bean Validation API.
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
     * underlying bean validation implementation, for example Hibernate Valdiator.
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
     * @param <T> the object type
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
     * Adds an error to the {@link ConstraintValidatorContext} using the specified template, thereby overriding
     * the constraint's default message.
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
     * Finds the value of the specified property <em>by direct field access</em>.
     * <p>
     * This is provided for validators to obtain the value of a specific field, and will only be useful when
     * for validation annotations that are comparing multiple fields. For example, an annotation that
     * validates a range of values between two fields in an object.
     *
     * @param bean      the object
     * @param fieldName the property/field name
     * @return the value of the property in the object, <strong>or null</strong> if any problem occurs
     * @implNote This uses {@link KiwiReflection#findField(Object, String)} to obtain the value.
     */
    public static Object getPropertyValue(Object bean, String fieldName) {
        try {
            return KiwiReflection.findField(bean, fieldName).get(bean);
        } catch (Exception e) {
            LOG.warn("Unable to get property value {} from object {}", fieldName, bean, e);
            return null;
        }
    }
}
