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
 * The annotated element must have a value in the specified enum class.
 * <p>
 * For example, if you have a String property that you want to constrain to one of the values in an enum, you can
 * apply this annotation, and specify the enum class to validate against.
 * <p>
 * By default, does not permit null values. If the element being validated permits nulls, you can set
 * {@link #allowNull()} to {@code true}.
 * <p>
 * You can optionally perform case-insensitive validation using {@link #ignoreCase()} or specify a custom method
 * using {@link #valueMethod()} which provides the values to validate against.
 * <p>
 * Examples:
 * <pre>
 * {@literal @}InEnum(enumClass = Season.class)
 *  private String season;
 * </pre>
 * <pre>
 * {@literal @}InEnum(enumClass = Season.class, allowNull = true, ignoreCase = true)
 *  private String season;
 * </pre>
 * <pre>
 * {@literal @}InEnum(enumClass = Season.class, ignoreCase = true, valueMethod = "humanizedValue")
 *  private String season;
 * </pre>
 */
@Documented
@Constraint(validatedBy = {InEnumValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface InEnum {

    String message() default "{org.kiwiproject.validation.InEnum.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return the enum class containing the allowable values for the annotated element
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * Whether to consider null as valid. The default is false.
     *
     * @return true to consider null as valid
     */
    boolean allowNull() default false;

    /**
     * Whether to ignore case. By default, match is case sensitive.
     *
     * @return true to ignore case, false to perform a case-sensitive match.
     */
    boolean ignoreCase() default false;

    /**
     * By default, {@link InEnum} uses the enum constants as the values to validate against. If there is a specific
     * method in the enum which provides a String value that should be used for validation instead of the enum
     * constants, specify it here.
     * <p>
     * For example, if you have a {@code Season} enum with values like {@code FALL} and {@code SPRING}, but the
     * values specified by users are more human-friendly, such as "Fall" and "Spring", and are available via a method
     * named {@code humanizedValue}, then the annotation should be specified as follows:
     * <pre>
     * {@literal @}InEnum(enumClass = Season.class, valueMethod = "humanizedValue")
     *  private String season;
     * </pre>
     *
     * @return the name of the method that returns the String value to perform validation against. By default this is
     * an empty String, which means the enum constant will be used.
     */
    String valueMethod() default "";
}
