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
 * The annotated element must be in the specified range, which can include both a minimum and maximum, only a minimum,
 * or only a maximum. When only minimum or maximum is specified, the range is open-ended on the non-specified side.
 * For example, if only a minimum is specified then there is no maximum applied in the constraint validation.
 * <p>
 * Null values are considered valid by default, but you can change this behavior using the {@link #allowNull()}
 * property.
 * <p>
 * Use {@link #min()} and {@link #max()} to specify the minimum and maximum values allowed in the range. These
 * are <em>inclusive</em> values, e.g. for a range with a minimum of 5 and maximum of 10, the values 5 and 10 are
 * considered as part of the range.
 * <p>
 * Use the {@link #minLabel()} and {@link #maxLabel()} to specify custom labels to use in place of the min and max
 * values. This is useful in cases where the min and max are large numbers or when validating date/time values where
 * the min and max are specified as milliseconds since the epoch. Note specifically that when both a minimum and a
 * maximum are supplied, and you want to use labels, then you should supply both the min and max labels.
 * <p>
 * The supported types are:
 * <ul>
 *     <li>{@code byte}, {@code short}, {@code int}, {@code long}, and their respective wrapper types</li>
 *     <li>{@code float}, {@code double}, and their respective wrapper types</li>
 *     <li>{@code BigDecimal}</li>
 *     <li>{@code BigInteger}</li>
 *     <li>Date, using epoch millis for the min and max values</li>
 *     <li>Instant, using epoch millis for the min and max values</li>
 *     <li>JSON, using JSON to define the min and max values</li>
 * </ul>
 * While {@code float} and {@code double} are supported, be aware of the possibility for rounding errors when values
 * are near the range bounds. The comparisons use {@link Float#compareTo(Float)} and {@link Double#compareTo(Double)}.
 */
@Documented
@Constraint(validatedBy = {RangeValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Range {

    String message() default "{org.kiwiproject.validation.Range.between.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to consider null as valid. The default is true.
     *
     * @return true to consider null as valid
     */
    boolean allowNull() default true;

    /**
     * @return the minimum allowed value for this range
     */
    String min() default "";

    /**
     * @return the label to be used in error messages in place of the minimum value, e.g. "ten" instead of 10
     */
    String minLabel() default "";

    /**
     * @return the maximum allowed value for this range
     */
    String max() default "";

    /**
     * @return the label to be used in error messages in place of the maximum value, e.g. "ten" instead of 10
     */
    String maxLabel() default "";
}
