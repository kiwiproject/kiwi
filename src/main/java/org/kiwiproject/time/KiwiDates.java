package org.kiwiproject.time;

import static java.util.Objects.isNull;
import static org.kiwiproject.time.KiwiInstants.minusDays;
import static org.kiwiproject.time.KiwiInstants.minusHours;
import static org.kiwiproject.time.KiwiInstants.minusMinutes;
import static org.kiwiproject.time.KiwiInstants.minusMonths;
import static org.kiwiproject.time.KiwiInstants.minusYears;
import static org.kiwiproject.time.KiwiInstants.plusDays;
import static org.kiwiproject.time.KiwiInstants.plusHours;
import static org.kiwiproject.time.KiwiInstants.plusMinutes;
import static org.kiwiproject.time.KiwiInstants.plusMonths;
import static org.kiwiproject.time.KiwiInstants.plusYears;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.Date;

/**
 * The original JDK 1.0 {@link Date} class leaves, ahem, much to be desired, but there are still (unfortunately)
 * many classes, interfaces, and libraries that use it and probably won't be changed soon or ever. KiwiDate contains
 * some utilities to convert {@link Instant} objects to {@link Date} objects shifted by some amount of time, for
 * example if you need a Date that is 30 minutes from a given {@link Instant}.
 * <p>
 * If you have a choice, you should always prefer the Java 8 date/time APIs (e.g. {@link Instant},
 * {@link java.time.LocalDateTime}, {@link java.time.ZonedDateTime}) in the {@code java.time} package
 * over using the legacy {@link Date}. But if you need to convert instants to dates, then this will help.
 * <p>
 * Functionally this is just a Date wrapper for {@link KiwiInstants}.
 */
@UtilityClass
public class KiwiDates {

    /**
     * Return the {@link Date} converted from {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @return the converted {@link Date}
     * @apiNote This is just a simple wrapper around {@link Date#from(Instant)} and is mainly here for consistency
     * with the other utilities provided here
     */
    public static Date dateFrom(Instant instant) {
        return Date.from(instant);
    }

    /**
     * Return the {@link Date} converted from {@code instant}, or {@code null} if {@code instant} is {@code null}.
     * <p>
     * This is a null-safe wrapper around {@link Date#from(Instant)}, and is useful in situations when you might
     * have a {@code null} instant.
     *
     * @param instant the {@link Instant} to convert, may be null
     * @return the converted {@link Date} or {@code null}
     * @apiNote This method only exists because {@link Date#from(Instant)} throws {@link NullPointerException} if its
     * arguments is {@code null}.
     */
    @Nullable
    public static Date dateFromInstantOrNull(@Nullable Instant instant) {
        return isNull(instant) ? null : dateFrom(instant);
    }

    /**
     * Return the {@link Date} that is the given number of {@code minutes} before the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param minutes number of minutes to subtract from {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantMinusMinutes(Instant instant, long minutes) {
        return dateFrom(minusMinutes(instant, minutes));
    }

    /**
     * Return the {@link Date} that is the given number of {@code hours} before the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param hours   number of hours to subtract from {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantMinusHours(Instant instant, long hours) {
        return dateFrom(minusHours(instant, hours));
    }

    /**
     * Return the {@link Date} that is the given number of {@code days} before the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param days    number of days to subtract from {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantMinusDays(Instant instant, long days) {
        return dateFrom(minusDays(instant, days));
    }

    /**
     * Return the {@link Date} that is the given number of {@code months} before the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param months  number of months to subtract from {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantMinusMonths(Instant instant, long months) {
        return dateFrom(minusMonths(instant, months));
    }

    /**
     * Return the {@link Date} that is the given number of {@code years} before the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param years   number of years to subtract from {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantMinusYears(Instant instant, long years) {
        return dateFrom(minusYears(instant, years));
    }

    /**
     * Return the {@link Date} that is the given number of {@code minutes} after the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param minutes number of minutes to add to {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantPlusMinutes(Instant instant, long minutes) {
        return dateFrom(plusMinutes(instant, minutes));
    }

    /**
     * Return the {@link Date} that is the given number of {@code hours} after the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param hours   number of hours to add to {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantPlusHours(Instant instant, long hours) {
        return dateFrom(plusHours(instant, hours));
    }

    /**
     * Return the {@link Date} that is the given number of {@code days} after the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param days    number of days to add to {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantPlusDays(Instant instant, long days) {
        return dateFrom(plusDays(instant, days));
    }

    /**
     * Return the {@link Date} that is the given number of {@code months} after the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param months  number of months to add to {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantPlusMonths(Instant instant, long months) {
        return dateFrom(plusMonths(instant, months));
    }

    /**
     * Return the {@link Date} that is the given number of {@code years} after the given {@code instant}.
     *
     * @param instant the {@link Instant} to convert
     * @param years   number of years to add to {@code instant}
     * @return the converted {@link Date}
     */
    public static Date dateFromInstantPlusYears(Instant instant, long years) {
        return dateFrom(plusYears(instant, years));
    }
}
