package org.kiwiproject.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Ensures that a value is provided (i.e. not null). Null values are always considered invalid. You can refine the
 * behavior by allowing blank or empty Strings, collections, maps, or other types of object to be considered valid.
 * <p>
 * For {@link CharSequence} objects (e.g. regular String objects), use allowBlank to allow empty or whitespace-only
 * Strings. It does not make sense to set allowEmpty to true but leave allowBlank as false, so in general when
 * annotating {@link CharSequence} objects, setting allowBlank to true is preferred, though it is also correct to
 * set both allowBlank and allowEmpty to true.
 * <p>
 * For {@link java.util.Collection} and {@link java.util.Map} objects, allowBlank has no effect, but allowEmpty
 * will cause empty collections and maps to be considered valid.
 * <p>
 * For any other type of object, only allowEmpty has any effect. If you have a custom object that has a public method
 * named {@code isEmpty} that has no arguments and returns {@code boolean}, this validator will attempt to invoke
 * that method reflectively and use its return value to determine whether the object is empty or not.
 */
@Documented
@Constraint(validatedBy = {RequiredValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Required {

    String message() default InternalKiwiValidators.TEMPLATE_REQUIRED;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to allow an object that is a {@link CharSequence} to be blank, using
     * {@link org.apache.commons.lang3.StringUtils#isBlank(CharSequence)} to perform the check.
     * <p>
     * This only applies to {@link CharSequence} objects. All other (non-null) objects are always considered as
     * not blank.
     *
     * @return true to allow a {@link CharSequence} to be blank, false to consider blank {@link CharSequence} as invalid
     */
    boolean allowBlank() default false;

    /**
     * Whether to allow an object to be "empty". For String values, just checks the value itself. For
     * anything else, attempts to find and call an {@code isEmpty()} method which allow this annotation to
     * work on other types of objects, for example on {@link java.util.Collection} objects.
     *
     * @return true to allow an object to be empty, false otherwise
     */
    boolean allowEmpty() default false;
}
