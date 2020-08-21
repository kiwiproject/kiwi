package org.kiwiproject.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated element must point to an existing file.
 * <p>
 * By default does not permit null values. If the element being validated allows {@code null} values, you can
 * set {@link #allowNull()} to {@code true}.
 * <p>
 * Examples:
 * <pre>
 * {@literal @}FilePath
 *  private String location;
 * </pre>
 * <pre>
 * {@literal @}FilePath(allowNull = true)
 *  public String getLocation() { return this.location; }
 * </pre>
 */
@Documented
@Constraint(validatedBy = {FilePathValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface FilePath {

    String message() default "{org.kiwiproject.validation.FilePath.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to consider null as valid. The default is false.
     *
     * @return true to consider null as valid
     */
    boolean allowNull() default false;
}
