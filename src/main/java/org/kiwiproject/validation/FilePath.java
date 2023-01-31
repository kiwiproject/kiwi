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
 * The annotated element must point to an existing file. Please read the implementation note regarding
 * intended usage of this annotation with respect to the potential for
 * <a href="https://owasp.org/www-community/attacks/Path_Traversal">Path Traversal</a> attacks.
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
 *
 * @implNote This annotation is not intended to validate user input from client-side applications, because of the
 * possibility of <a href="https://owasp.org/www-community/attacks/Path_Traversal">Path Traversal</a> attacks. Instead,
 * the intended usage is to validate application configuration parameters, e.g. an application reads a local
 * configuration file at startup. Even this is not 100% safe, since the configuration could come from a remote
 * location such as a configuration service, so users should understand the usage risks and mitigate when possible.
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
