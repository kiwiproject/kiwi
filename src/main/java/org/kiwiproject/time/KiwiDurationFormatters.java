package org.kiwiproject.time;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.concurrent.TimeUnit;

/**
 * Utilities for formatting durations of various types.
 * <p>
 * See Apache Commons {@link DurationFormatUtils} for even more formatter methods.
 *
 * @implNote {@code dropwizard-util} must be available at runtime to use the
 * methods that accept Dropwizard's {@code io.dropwizard.util.Duration}.
 */
@UtilityClass
public class KiwiDurationFormatters {

    private static final boolean SUPPRESS_LEADING_ZERO_ELEMENTS = true;
    private static final boolean SUPPRESS_TRAILING_ZERO_ELEMENTS = true;
    private static final String LITERAL_NULL = "null";
    private static final String ZERO_SECONDS_MESSAGE = "0 seconds";
    private static final int NANOSECONDS_IN_ONE_MILLISECOND = 1_000_000;
    private static final int MILLISECONDS_IN_ONE_SECOND = 1_000;

    /**
     * Formats a Java {@link java.time.Duration Duration} using English words.
     * Converts the duration to nanos and then calls {@link #formatNanosecondDurationWords(long)}.
     * <p>
     * If the duration is null, the literal string {@code "null"} is returned.
     * If the duration is zero, {@code "0 seconds"} is returned.
     *
     * @param duration the Java duration to format
     * @return the duration in words (e.g., 2 hours 5 minutes)
     * @throws IllegalArgumentException if the duration is negative
     * @see #formatNanosecondDurationWords(long)
     */
    public static String formatJavaDurationWords(java.time.Duration duration) {
        if (isNull(duration)) {
            return LITERAL_NULL;
        }

        if (duration.isZero()) {
            return ZERO_SECONDS_MESSAGE;
        }

        return formatNanosecondDurationWords(duration.toNanos());
    }

    /**
     * Formats a Dropwizard {@link io.dropwizard.util.Duration Duration} using English words.
     * Converts the duration to nanos and then calls {@link #formatNanosecondDurationWords(long)}.
     * <p>
     * If the duration is null, the literal string {@code "null"} is returned.
     * If the duration is zero, {@code "0 seconds"} is returned.
     *
     * @param duration the Dropwizard duration to format
     * @return the duration in words (e.g., 1 minute 45 seconds)
     * @throws IllegalArgumentException if the duration is negative
     * @see #formatNanosecondDurationWords(long)
     * @implNote You will need the Dropwizard Duration class available at runtime to call this method!
     */
    public static String formatDropwizardDurationWords(io.dropwizard.util.Duration duration) {
        if (isNull(duration)) {
            return LITERAL_NULL;
        }

        if (duration.getQuantity() == 0) {
            return ZERO_SECONDS_MESSAGE;
        }

        return formatNanosecondDurationWords(duration.toNanoseconds());
    }

    /**
     * A thin wrapper around {@link DurationFormatUtils#formatDurationWords(long, boolean, boolean)} that always
     * suppresses leading and trailing "zero elements" because why would you want to see
     * "0 days 7 hours 25 minutes 0 seconds" instead of just "7 hours 25 minutes"? (We cannot think of a good reason...).
     * <p>
     * Unlike {@link DurationFormatUtils}, it also handles durations less than one second down to 1 nanosecond.
     * <p>
     * If the duration is zero, {@code "0 milliseconds"} is returned.
     * <p>
     * If you need to handle durations less than a millisecond, use {@link #formatNanosecondDurationWords(long)}
     *
     * @param durationMillis the duration in milliseconds to format
     * @return the duration in words (e.g., 10 minutes)
     * @throws IllegalArgumentException if the duration is negative
     * @implNote The main reasons for this method to exist are so that we don't constantly have to pass the two
     * boolean arguments to {@link DurationFormatUtils} and so that we can handle durations less than one
     * second. Plus, boolean arguments are evil because what exactly does "true, false" tell you without
     * requiring you to look at the parameter documentation?
     * @see DurationFormatUtils#formatDurationWords(long, boolean, boolean)
     * @see #formatNanosecondDurationWords(long)
     */
    public static String formatMillisecondDurationWords(long durationMillis) {
        checkArgument(durationMillis >= 0, "duration must not be negative");

        if (durationMillis == 0) {
            return "0 milliseconds";
        }

        if (durationMillis < MILLISECONDS_IN_ONE_SECOND) {
            return durationMillis + milliUnit(durationMillis);
        }

        return DurationFormatUtils.formatDurationWords(
                durationMillis,
                SUPPRESS_LEADING_ZERO_ELEMENTS,
                SUPPRESS_TRAILING_ZERO_ELEMENTS);
    }

    /**
     * A thin wrapper around {@link DurationFormatUtils#formatDurationWords(long, boolean, boolean)} that always
     * suppresses leading and trailing "zero elements" because why would you want to see
     * "0 days 7 hours 25 minutes 0 seconds" instead of just "7 hours 25 minutes"? (We cannot think of a good reason...)
     * <p>
     * If the duration is zero, {@code "0 nanosecond"} is returned.
     * <p>
     * Unlike {@link DurationFormatUtils}, it also handles durations less than one second down to 1 nanosecond.
     *
     * @param durationNanos the duration in nanoseconds to format
     * @return the duration in words (e.g., 10 minutes, 50 milliseconds)
     * @throws IllegalArgumentException if the duration is negative
     * @implNote The main reasons for this method to exist are so that we don't constantly have to pass the two
     * boolean arguments to {@link DurationFormatUtils} and so that we can handle durations less than one
     * second. Plus, boolean arguments are evil because what exactly does "true, false" tell you without
     * requiring you to look at the parameter documentation?
     * @see DurationFormatUtils#formatDurationWords(long, boolean, boolean)
     */
    public static String formatNanosecondDurationWords(long durationNanos) {
        checkArgument(durationNanos >= 0, "duration must not be negative");

        if (durationNanos == 0) {
            return "0 nanoseconds";
        }

        if (durationNanos < NANOSECONDS_IN_ONE_MILLISECOND) {
            return durationNanos + nanoUnit(durationNanos);
        }

        var durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        if (durationMillis < MILLISECONDS_IN_ONE_SECOND) {
            return durationMillis + milliUnit(durationMillis);
        }

        return DurationFormatUtils.formatDurationWords(
                durationMillis,
                SUPPRESS_LEADING_ZERO_ELEMENTS,
                SUPPRESS_TRAILING_ZERO_ELEMENTS);
    }

    private static String nanoUnit(long durationNanos) {
        return durationNanos == 1 ? " nanosecond" : " nanoseconds";
    }

    private static String milliUnit(long durationMillis) {
        return durationMillis == 1 ? " millisecond" : " milliseconds";
    }
}
