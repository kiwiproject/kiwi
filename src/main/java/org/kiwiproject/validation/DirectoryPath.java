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
 * The annotated element must point to an existing directory. Please read the implementation note regarding
 * intended usage of this annotation with respect to the potential for
 * <a href="https://owasp.org/www-community/attacks/Path_Traversal">Path Traversal</a> attacks.
 * <p>
 * By default does not permit null values. If the element being validated allows {@code null} values, you can
 * set {@link #allowNull()} to {@code true}.
 * <p>
 * You can also use {@link #ensureReadable()} and {@link #ensureWritable()} to verify that the directory is readable
 * or writable by the current process.
 * <p>
 * Finally, the {@link #mkdirs()} option provides a way to actually create the specified directory if it does not
 * exist. <em>Please read the documentation for this option.</em>
 * <p>
 * Examples:
 * <pre>
 * {@literal @}DirectoryPath
 *  private String tempDir;
 * </pre>
 * <pre>
 * {@literal @}DirectoryPath(allowNull = true)
 *  public String getTempDir() { return this.tempDir; }
 * </pre>
 * <pre>
 * {@literal @}DirectoryPath(ensureReadable = true, ensureWritable = true, mkdirs = true)
 *  private String tempDir;
 * </pre>
 *
 * @implNote This annotation is not intended to validate user input from client-side applications, because of the
 * possibility of <a href="https://owasp.org/www-community/attacks/Path_Traversal">Path Traversal</a> attacks. Instead,
 * the intended usage is to validate application configuration parameters, e.g. an application reads a local
 * configuration file at startup. Even this is not 100% safe, since the configuration could come from a remote
 * location such as a configuration service, so users should understand the usage risks and mitigate when possible.
 */
@Documented
@Constraint(validatedBy = {DirectoryPathValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface DirectoryPath {

    String message() default "{org.kiwiproject.validation.DirectoryPath.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to consider null as valid. The default is false.
     *
     * @return true to consider null as valid
     */
    boolean allowNull() default false;

    /**
     * Whether to verify that the specified directory can be read by the current process.
     * The default is false (no verification is performed).
     *
     * @return true to validate the directory is readable; if false does not verify
     */
    boolean ensureReadable() default false;

    /**
     * Whether to verify that the specified directory can be read by the current process.
     * The default is false (no verification is performed).
     *
     * @return true to validate the directory is writable; if false does not verify
     */
    boolean ensureWritable() default false;

    /**
     * Whether this validator will attempt to create the directory if it does not exist.
     * <p>
     * <strong>IMPORTANT: This is generally unexpected behavior (to have any side-effects during validation)</strong>.
     * <p>
     * However, based on the use cases we have encountered, this is a <em>pragmatic</em> way to ensure directories are
     * present, and we have found the alternatives to be clumsy at best, overly complicated at worst. One common
     * example is to ensure a temporary or working directory exists for an application to use.
     * <p>
     * <em>Regardless, please be sure you understand the side-effecting behavior if you set this to {@code true}.</em>
     *
     * @return true to create any missing directories; if false directories are not created
     */
    boolean mkdirs() default false;
}
