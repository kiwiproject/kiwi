package org.kiwiproject.time;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * Provides utilities related to {@link Instant}.
 * <p>
 * First, {@link KiwiInstants} adds convenience methods for adding and subtracting common time increments to
 * {@link Instant} objects in the form of {@code plus<TemporalField>} and {@code minus<TemporalField>} methods such
 * as {@link #plusMinutes(Instant, long)} and {@link #minusDays(Instant, long)}.
 * <p>
 * The {@link Instant} class does not support all of the {@link ChronoUnit} values via the generic
 * {@link Instant#plus(long, TemporalUnit)} and {@link Instant#minus(long, TemporalUnit)} methods.
 * Specifically it only supports (as of this writing):
 * <ul>
 *     <li>{@link ChronoUnit#NANOS}</li>
 *     <li>{@link ChronoUnit#MICROS}</li>
 *     <li>{@link ChronoUnit#MILLIS}</li>
 *     <li>{@link ChronoUnit#SECONDS}</li>
 *     <li>{@link ChronoUnit#MINUTES}</li>
 *     <li>{@link ChronoUnit#HOURS}</li>
 *     <li>{@link ChronoUnit#HALF_DAYS}</li>
 *     <li>{@link ChronoUnit#DAYS}</li>
 * </ul>
 * This means {@link Instant} does not support any of the other units larger than {@link ChronoUnit#DAYS} such as weeks,
 * months, years, etc. This class supports adding and subtracting months and years directly, as well as other units via
 * the {@link #plusUsingZDT(Instant, long, TemporalUnit)} and {@link #minusUsingZDT(Instant, long, TemporalUnit)}
 * methods.
 * <p>
 * Second, {@link KiwiInstants} supports adding and subtracting months and years by first converting the {@link Instant}
 * to a {@link ZonedDateTime} at {@link ZoneOffset#UTC}. For example {@link #plusMonths(Instant, long)}. For other
 * {@link TemporalUnit} values you can use the {@link #plusUsingZDT(Instant, long, TemporalUnit)} and
 * {@link #minusUsingZDT(Instant, long, TemporalUnit)} methods, which will work as long as {@link ZonedDateTime}
 * supports the provided {@link TemporalUnit}.
 * <p>
 * Third, {@link KiwiInstants} provides utilities to retrieve individual standard parts of an {@link Instant} (second,
 * minute, hour, day, month, year) using the various {@code get<TemporalField>} methods, for example
 * {@link #getHour(Instant)}.
 */
@UtilityClass
public class KiwiInstants {

    /**
     * Return the value of the second from {@code instant}.
     *
     * @param instant the {@link Instant}
     * @return the value of the second
     */
    public static int getSecond(Instant instant) {
        return toZonedDateTime(instant).getSecond();
    }

    /**
     * Return the value of the minute from {@code instant}.
     *
     * @param instant the {@link Instant}
     * @return the value of the minute
     */
    public static int getMinute(Instant instant) {
        return toZonedDateTime(instant).getMinute();
    }

    /**
     * Return the value of the hour from {@code instant}.
     *
     * @param instant the {@link Instant}
     * @return the value of the hour
     */
    public static int getHour(Instant instant) {
        return toZonedDateTime(instant).getHour();
    }

    /**
     * Return the value of the day of the month from {@code instant}.
     *
     * @param instant the {@link Instant}
     * @return the value of the day of the month
     */
    public static int getDayOfMonth(Instant instant) {
        return toZonedDateTime(instant).getDayOfMonth();
    }

    /**
     * Return the {@link Month} from {@code instant}.
     *
     * @param instant the {@link Instant}
     * @return the {@link Month}
     */
    public static Month getMonth(Instant instant) {
        return toZonedDateTime(instant).getMonth();
    }

    /**
     * Return the value of the month (1-12) from {@code instant}.
     *
     * @param instant the {@link Instant}
     * @return the value the month, i.e. 1-12
     */
    public static int getMonthValue(Instant instant) {
        return getMonth(instant).getValue();
    }

    /**
     * Return the value of the year from {@code instant}.
     *
     * @param instant the {@link Instant}
     * @return the value of the year
     */
    public static int getYear(Instant instant) {
        return toZonedDateTime(instant).getYear();
    }

    /**
     * Return an {@link Instant} that is the given number of {@code minutes} before {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param minutes the amount of time to go backward in minutes
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant minusMinutes(Instant instant, long minutes) {
        return minus(instant, minutes, ChronoUnit.MINUTES);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code hours} before {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param hours   the amount of time to go backward in hours
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant minusHours(Instant instant, long hours) {
        return minus(instant, hours, ChronoUnit.HOURS);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code days} before {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param days    the amount of time to go backward in days
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant minusDays(Instant instant, long days) {
        return minus(instant, days, ChronoUnit.DAYS);
    }

    private static Instant minus(Instant instant, long value, TemporalUnit unit) {
        return instant.minus(value, unit);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code months} before {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param months  the amount of time to go backward in months
     * @return a new {@link Instant} appropriately adjusted
     * @implNote uses {@link ZonedDateTime} (at {@link ZoneOffset#UTC}) to traverse by month
     */
    public static Instant minusMonths(Instant instant, long months) {
        return minusUsingZDT(instant, months, ChronoUnit.MONTHS);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code years} before {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param years   the amount of time to go backward in years
     * @return a new {@link Instant} appropriately adjusted
     * @implNote uses {@link ZonedDateTime} (at {@link ZoneOffset#UTC}) to traverse by month
     */
    public static Instant minusYears(Instant instant, long years) {
        return minusUsingZDT(instant, years, ChronoUnit.YEARS);
    }

    /**
     * Subtract any given {@code amountToSubtract} of type {@link TemporalUnit} from the given instant.
     * This first converts the {@link Instant} into a {@link ZonedDateTime} at {@link ZoneOffset#UTC} before
     * applying the subtraction.
     *
     * @param instant          the {@link Instant}
     * @param amountToSubtract the amount of time to go backwards
     * @param unit             the unit for the amount of time to go backwards
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant minusUsingZDT(Instant instant, long amountToSubtract, TemporalUnit unit) {
        return toZonedDateTime(instant).minus(amountToSubtract, unit).toInstant();
    }

    /**
     * Return an {@link Instant} that is the given number of {@code minutes} after {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param minutes the amount of time to go forward in minutes
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant plusMinutes(Instant instant, long minutes) {
        return plus(instant, minutes, ChronoUnit.MINUTES);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code hours} after {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param hours   the amount of time to go forward in hours
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant plusHours(Instant instant, long hours) {
        return plus(instant, hours, ChronoUnit.HOURS);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code days} after {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param days    the amount of time to go forward in days
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant plusDays(Instant instant, long days) {
        return plus(instant, days, ChronoUnit.DAYS);
    }

    private static Instant plus(Instant instant, long value, TemporalUnit unit) {
        return instant.plus(value, unit);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code months} after {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param months  the amount of time to go forward in months
     * @return a new {@link Instant} appropriately adjusted
     * @implNote uses {@link ZonedDateTime} (at {@link ZoneOffset#UTC}) to traverse by month
     */
    public static Instant plusMonths(Instant instant, long months) {
        return plusUsingZDT(instant, months, ChronoUnit.MONTHS);
    }

    /**
     * Return an {@link Instant} that is the given number of {@code years} after {@code instant}.
     *
     * @param instant the {@link Instant}
     * @param years   the amount of time to go forward in years
     * @return a new {@link Instant} appropriately adjusted
     * @implNote uses {@link ZonedDateTime} (at {@link ZoneOffset#UTC}) to traverse by month
     */
    public static Instant plusYears(Instant instant, long years) {
        return plusUsingZDT(instant, years, ChronoUnit.YEARS);
    }

    /**
     * Add any given {@code amountToAdd} of type {@link TemporalUnit} from the given instant.
     * This first converts the {@link Instant} into a {@link ZonedDateTime} at {@link ZoneOffset#UTC} before
     * applying the addition.
     *
     * @param instant     the {@link Instant}
     * @param amountToAdd the amount of time to go forward
     * @param unit        the unit for the amount of time to go forward
     * @return a new {@link Instant} appropriately adjusted
     */
    public static Instant plusUsingZDT(Instant instant, long amountToAdd, TemporalUnit unit) {
        return toZonedDateTime(instant).plus(amountToAdd, unit).toInstant();
    }

    private static ZonedDateTime toZonedDateTime(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
