package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.experimental.UtilityClass;
import org.kiwiproject.json.JsonHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

/**
 * Internal shared validation code.
 */
@UtilityClass
class InternalKiwiValidators {

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

    static final String TEMPLATE_REQUIRED = "{org.kiwiproject.validation.Required.message}";

    /**
     * Convert a string representation of a "compare value" (for example a string representing a minimum or maximum)
     * into an object of the same type as {@code value}.
     * <p>
     * If the "compare value" is blank (null, empty, or only whitespace) or {@code value} is {@code null}, then
     * {@code null} is returned.
     * <p>
     * As a concrete example, the {@code compareValue} might be the String from a min() or max() in a validation
     * annotation such as {@link Range}. In order to compare with {@code value}, we need to convert that String to the
     * same type as {@code value}. Suppose we are given a compare value of "10" and {@code value} is a {@link Long}.
     * In this case, "10" must is converted into a {@link Long}.
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
     *
     * @param compareValue a string representation of a value that should be converted into the same type as the
     *                     {@code value} argument
     * @param value        the value being validated, and which defines the conversion type
     * @implNote This is non-ideal with the massive if/else if/else, but since we have to check each type we
     * support, I cannot think of a "better" or "cleaner" way to do this without it becoming so abstract that
     * it becomes unreadable. Interestingly, neither IntelliJ not Sonar is complaining...maybe we don't have the
     * appropriate rules enabled. Suggestions for improvement welcome!
     */
    static Comparable<?> toComparableOrNull(String compareValue, Comparable<?> value) {
        if (isBlank(compareValue) || isNull(value)) {
            return null;
        }

        Comparable<?> typedValue;

        if (value instanceof Double) {
            typedValue = Double.valueOf(compareValue);
        } else if (value instanceof Float) {
            typedValue = Float.valueOf(compareValue);
        } else if (value instanceof Byte) {
            typedValue = Byte.valueOf(compareValue);
        } else if (value instanceof Short) {
            typedValue = Short.valueOf(compareValue);
        } else if (value instanceof Integer) {
            typedValue = Integer.valueOf(compareValue);
        } else if (value instanceof Long) {
            typedValue = Long.valueOf(compareValue);
        } else if (value instanceof BigDecimal) {
            typedValue = new BigDecimal(compareValue);
        } else if (value instanceof BigInteger) {
            typedValue = new BigInteger(compareValue);
        } else if (value instanceof Date) {
            typedValue = new Date(Long.parseLong(compareValue));
        } else if (value instanceof Instant) {
            typedValue = Instant.ofEpochMilli(Long.parseLong(compareValue));
        } else if (compareValue.stripLeading().startsWith("{")) {
            typedValue = JSON_HELPER.toObject(compareValue, value.getClass());
        } else {
            var message = "This validator does not support validating objects of type: " + value.getClass().getName();
            throw new IllegalArgumentException(message);
        }

        return typedValue;
    }
}
