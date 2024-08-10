package org.kiwiproject.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated <em>type</em> must have two fields that define a valid range, i.e., in the simplest configuration
 * {@link #startField()} must come before {@link #endField()}. You can have multiple {@link FieldRange} annotations
 * on a type, either as standalone annotations or inside a {@link FieldRanges} annotation.
 * <p>
 * The main restriction imposed by this annotation is that {@link #startField()} and {@link #endField()} must be
 * {@link Comparable}. It is also assumed that they are defined to both have the same type, e.g., both are Integer
 * or {@link java.time.Instant}. No guarantees are made if they are different types, and most likely unpredictable
 * results and/or exceptions will occur.
 * <p>
 * Note also that <em>direct field access using reflection</em> is used to get the start and end values as
 * currently implemented.
 * <p>
 * By default, null values are not allowed, and the range check is exclusive, meaning start cannot equal end. You can
 * change the default behavior using the various options.
 * <p>
 * In addition to ensuring that the start and end fields define a valid range, you can also constrain them to minimum
 * and/or maximum values.
 * <p>
 * Use the {@link #minLabel()} and {@link #maxLabel()} to specify custom labels to use in place of the min and max
 * values. This is useful in cases where the min and max are large numbers or when validating date/time values where
 * the min and max are specified as milliseconds since the epoch. Note specifically that when both a minimum and a
 * maximum are supplied, and you want to use labels, then you should supply both the min and max labels.
 * <p>
 * This validator's type support depends on whether minimum and maximum values are specified as part of the
 * configuration. If minimum and/or maximum are specified, then the supported types are the same as those supported
 * by {@link Range} since the minimum/maximum values must be converted from strings into the type of object that
 * the start and end fields are. If there are no minimum or maximum values defined, then any type that implements
 * {@link Comparable} is supported.
 * <p>
 * Finally, assuming there are no minimum or maximum values specified and that the type being validated contains
 * only a <em>time</em> component, e.g. {@link java.time.LocalTime LocalTime}, then there is an edge case between
 * 12:00 AM (00:00) and 1:00 AM (01:00) that may result in unexpected validation failures. For example, if the first
 * time in the range is 11:56 PM (23:56), and the second is 12:15 AM (00:15), then without a date to indicate whether
 * the times cross a midnight boundary, this range will be considered as invalid. The reason is simply that within a
 * single day (again because there is no date component to indicate otherwise), 12:15 AM always comes before 23:56 PM.
 */
@Documented
@Constraint(validatedBy = {})
@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(FieldRanges.class)
public @interface FieldRange {

    String message() default "{org.kiwiproject.validation.FieldRange.unknownError.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return the name of the field that defines the range start
     */
    String startField();

    /**
     * @return the name of the field that defines the range end
     */
    String endField();

    /**
     * @return the label to use in error messages instead of the end field
     * @implNote There is no equivalent for start field since the errors are attached to the start field, such that
     * the message is defined relative to the start field. For example, the property path in a constraint violation
     * is startField and will have the associated message "must occur before [endField|endFieldLabel]".
     */
    String endFieldLabel() default "";

    /**
     * @return true if the start and end can be the same value; the default is false
     */
    boolean allowStartToEqualEnd() default false;

    /**
     * If true, the range only includes the end field. This is mainly useful when used with a max value.
     *
     * @return true to define a range that only considers the end field
     */
    boolean allowNullStart() default false;

    /**
     * If true, the range only includes the start field. This is mainly useful when used with a min value.
     *
     * @return true to define a range that only considers the start field
     */
    boolean allowNullEnd() default false;

    /**
     * @return the minimum value allowed for the start of the range
     */
    String min() default "";

    /**
     * @return the label to be used in error messages in place of the minimum value, e.g. "ten" instead of 10
     */
    String minLabel() default "";

    /**
     * @return the minimum value allowed for the end of the range
     */
    String max() default "";

    /**
     * @return the label to be used in error messages in place of the maximum value, e.g. "ten" instead of 10
     */
    String maxLabel() default "";
}
