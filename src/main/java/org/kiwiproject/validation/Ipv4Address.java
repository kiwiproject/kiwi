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
 * The annotated element must have a value that is a valid IPv4 address.
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/IPv4">IPv4</a> on Wikipedia.
 */
@Documented
@Constraint(validatedBy = {Ipv4AddressValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Ipv4Address {

    String message() default "{org.kiwiproject.validation.Ipv4Address.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to consider null as valid. The default is false.
     *
     * @return true to consider null as valid
     */
    boolean allowNull() default false;

}
