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
 * Validates that a {@link CharSequence} represents a valid URL.
 * <p>
 * This annotation can be used to validate that a string field, parameter, etc. contains a valid URL.
 * By default, only HTTP and HTTPS schemes are allowed, but this can be configured using the
 * {@link #allowAllSchemes()} and {@link #allowSchemes()} properties.
 *
 * @implNote This annotation requires the commons-validator dependency
 * when using it, since the {@link ValidURLValidator} uses Apache Commons Validator's
 * {@link org.apache.commons.validator.routines.UrlValidator} internally.
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ValidURL {

    /**
     * The error message to use when validation fails.
     *
     * @return the error message
     */
    String message() default "{org.kiwiproject.validation.ValidURL.message}";

    /**
     * The validation groups to which this constraint belongs.
     * <p>
     * This is a standard property from the Bean Validation API.
     *
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * The payload associated with the constraint.
     * <p>
     * This is a standard property from the Bean Validation API.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to allow null values.
     * <p>
     * If set to true, null values will be considered valid.
     * If set to false (the default), null values will be considered invalid.
     *
     * @return true if null values are allowed, false otherwise
     */
    boolean allowNull() default false;

    /**
     * Whether to allow all URL schemes.
     * <p>
     * If set to true, all URL schemes will be allowed.
     * If set to false (the default), only the schemes specified in {@link #allowSchemes()} will be allowed.
     *
     * @return true if all schemes are allowed, false otherwise
     */
    boolean allowAllSchemes() default false;

    /**
     * The allowed URL schemes.
     * <p>
     * This property is only used if {@link #allowAllSchemes()} is set to false.
     * By default, only "http" and "https" schemes are allowed.
     *
     * @return the allowed URL schemes
     */
    String[] allowSchemes() default { "http", "https" };

    /**
     * Whether to allow local URLs (e.g., {@code localhost}, {@code localdomain}).
     * <p>
     * If set to true (the default), local URLs like {@code http://localhost} will be considered valid.
     * If set to false, local URLs will be considered invalid.
     *
     * @return true if local URLs are allowed, false otherwise
     */
    boolean allowLocalUrls() default true;

    /**
     * Whether to allow URLs with two consecutive slashes.
     * <p>
     * If set to true, URLs with two consecutive slashes (other than after the scheme) will be considered valid.
     * If set to false (the default), such URLs will be considered invalid.
     *
     * @return true if URLs with two consecutive slashes are allowed, false otherwise
     */
    boolean allowTwoSlashes() default false;

    /**
     * Whether to allow URLs with fragments.
     * <p>
     * If set to true (the default), URLs with fragments (the part after #) will be considered valid.
     * If set to false, URLs with fragments will be considered invalid.
     *
     * @return true if URLs with fragments are allowed, false otherwise
     */
    boolean allowFragments() default true;
}
