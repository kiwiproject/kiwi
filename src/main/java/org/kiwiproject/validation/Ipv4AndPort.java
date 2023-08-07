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
 * The annotated element must have the form {@code ipv4-address:port}. For example {@code 192.168.1.150:8888} is
 * valid while {@code 192.168.1.150} (without port) and {@code 1111.1111.1.150:8009} (too many digits in octets) are
 * not. Also, as the name indicates, this only supports IPv4 addresses.
 *
 * @implNote Currently the validation only verifies that the IP is in "dotted-quad" syntax followed by a colon and
 * a port. It only validates that each dotted-quad and port are numbers, and <em>does not</em> validate the values
 * are within valid ranges (i.e. it does not check each segment is in the range 0-255 nor does it validate the port
 * range).
 */
@Documented
@Constraint(validatedBy = {Ipv4AndPortValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Ipv4AndPort {

    String message() default "{org.kiwiproject.validation.Ipv4AndPort.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to consider null as valid. The default is false.
     *
     * @return true to consider null as valid
     */
    boolean allowNull() default false;

}
