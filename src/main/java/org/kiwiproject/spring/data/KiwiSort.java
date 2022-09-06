package org.kiwiproject.spring.data;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.EnumUtils;

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
         *
         * @param value the value to convert
         * @return the {@link Direction} representing the given value
         * @throws IllegalArgumentException if there is no {@link Direction} that matches ignoring case
         *                                  and stripping leading and trailing whitespace
         */
        public static Direction fromString(String value) {
            checkArgumentNotBlank(value, "direction value must not be blank");

            var direction = EnumUtils.getEnumIgnoreCase(Direction.class, value.strip());
            checkArgumentNotNull(direction, "no matching enum constant found in Direction");

            return direction;
        }

        /**
         * Convenience method to allow checking if this {@link Direction} is descending.
         *
         * @return true if this sort is descending, false if ascending
         */
        public boolean isDescending() {
            return !ascending;
        }
    }

    @JsonIgnore
    private Direction directionObject;
    private String direction;  // this is intentionally a string, for JSON serialization purposes
    private String property;
    private boolean ignoreCase;
    private boolean ascending;

    /**
     * Create a new instance with ascending sort direction.
     * <p>
     * If you want to specify that the sort is not case-sensitive, you can immediately call the
     * {@link #ignoringCase()} in a fluent style.
     *
     * @param property the property the sort is applied to
     * @return a new instance
     */
    public static KiwiSort ofAscending(String property) {
        return of(property, Direction.ASC);
    }

    /**
     * Create a new instance with descending sort direction.
     * <p>
     * If you want to specify that the sort is not case-sensitive, you can immediately call the
     * {@link #ignoringCase()} in a fluent style.
     *
     * @param property the property the sort is applied to
     * @return a new instance
     */
    public static KiwiSort ofDescending(String property) {
        return of(property, Direction.DESC);
    }

    /**
     * Create a new instance.
     * <p>
     * If you want to specify that the sort is not case-sensitive, you can immediately call the
     * {@link #ignoringCase()} in a fluent style.
     *
     * @param property  the property the sort is applied to
     * @param direction the sort direction as a String, which must resolve via {@link Direction#fromString(String)}
     * @return a new instance
     * @throws IllegalArgumentException if property is blank or direction is blank or invalid
     */
    public static KiwiSort of(String property, String direction) {
        checkArgumentNotBlank(property);
        checkArgumentNotBlank(direction);

        return of(property, Direction.fromString(direction));
    }

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
        sort.setDirectionObject(direction);
        sort.setDirection(direction.name());
        sort.setAscending(direction.isAscending());
        sort.setIgnoreCase(false);
        return sort;
    }

    /**
     * Specifies that the sort is <em>not</em> case-sensitive, i.e. it ignores case.
     *
     * @return this instance, for method chaining
     */
    public KiwiSort ignoringCase() {
        setIgnoreCase(true);
        return this;
    }

    /**
     * Convenience method to allow checking if this {@link KiwiSort} is descending.
     *
     * @return true if this sort is descending, false if ascending
     */
    public boolean isDescending() {
        return !ascending;
    }
}
