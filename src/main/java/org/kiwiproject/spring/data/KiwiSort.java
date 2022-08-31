package org.kiwiproject.spring.data;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Locale;

/**
 * Describes a sort on a specific property that is applied to a result list.
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class KiwiSort {

    /**
     * Sort direction.
     */
    public enum Direction {

        /**
         * An ascending sort.
         */
        ASC(true),

        /**
         * A descending sort.
         */
        DESC(false);

        /**
         * Accessible via a getter method as well as direct field access.
         */
        @Getter
        public final boolean ascending;

        Direction(boolean ascending) {
            this.ascending = ascending;
        }

        /**
         * Converts the given value to a {@link Direction} in a case-insensitive manner and ignoring leading and
         * trailing whitespace.
         * <p>
         * Uses the default {@link Locale} to convert the value to uppercase.
         *
         * @param value the value to convert
         * @return the {@link Direction} representing the given value
         * @throws IllegalArgumentException if there is no {@link Direction} that matches after converting to uppercase
         *                                  and stripping leading and trailing whitespace
         * @see Locale#getDefault()
         */
        public static Direction fromString(String value) {
            return Direction.fromString(value, Locale.getDefault());
        }

        /**
         * Converts the given value to a {@link Direction} in a case-insensitive manner and ignoring leading and
         * trailing whitespace.
         * <p>
         * Uses the given {@link Locale} to convert the value to uppercase.
         *
         * @param value  the value to convert
         * @param locale the Locale to use to uppercase the value
         * @return the {@link Direction} representing the given value
         * @throws IllegalArgumentException if there is no {@link Direction} that matches after converting to uppercase
         *                                  and stripping leading and trailing whitespace
         */
        public static Direction fromString(String value, Locale locale) {
            checkArgumentNotBlank(value, "direction value must not be blank");

            var upperCaseValue = value.strip().toUpperCase(locale);
            return Direction.valueOf(upperCaseValue);
        }
    }

    private String direction;  // this is intentionally a string, for JSON serialization purposes
    private String property;
    private boolean ignoreCase;
    private boolean ascending;

    /**
     * Create a new instance.
     * <p>
     * If you want to specify that the sort is not case-sensitive, you can immediately call the
     * {@link #ignoringCase()} in a fluent style.
     *
     * @param property  the property the sort is applied to
     * @param direction the sort direction
     * @return a new instance
     * @throws IllegalArgumentException if property is blank or direction is null
     */
    public static KiwiSort of(String property, KiwiSort.Direction direction) {
        checkArgumentNotBlank(property);
        checkArgumentNotNull(direction);

        var sort = new KiwiSort();
        sort.setProperty(property);
        sort.setDirection(direction.name());
        sort.setAscending(direction.isAscending());
        sort.setIgnoreCase(false);
        return sort;
    }

    /**
     * Specifies that the sort is <em>not</em> case sensitive, i.e. it ignores case.
     *
     * @return this instance, for method chaining
     */
    public KiwiSort ignoringCase() {
        setIgnoreCase(true);
        return this;
    }
}
