package org.kiwiproject.time;

import static java.util.Objects.isNull;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;

/**
 * Utilities for formatting durations of various types.
 * <p>
 * See Apache Commons {@link DurationFormatUtils} for even more formatter methods.
 */
@UtilityClass
public class KiwiDurationFormatters {

    private static final boolean SUPPRESS_LEADING_ZERO_ELEMENTS = true;
    private static final boolean SUPPRESS_TRAILING_ZERO_ELEMENTS = true;
    private static final String LITERAL_NULL = "null";

    /**
     * Formats a Java {@link Duration} using English words. Converts the duration to millis and then calls
     * {@link #formatDurationWords(long)}.
     *
     * @param duration the Java duration to format
     * @return the duration in words (e.g. 2 hours 5 minutes)
     */
    public static String formatDurationWords(Duration duration) {
        if (isNull(duration)) {
            return LITERAL_NULL;
        }

        return formatDurationWords(duration.toMillis());
    }

    /**
     * Formats a Dropwizard {@link io.dropwizard.util.Duration} using English words. Converts the duration to millis
     * and then calls {@link #formatDurationWords(long)}.
     *
     * @param duration the Dropwizard duration to format
     * @return the duration in words (e.g. 1 minute 45 seconds)
     * @implNote You will need the Dropwizard Duration class available at runtime to call this method!
     */
    public static String formatDurationWords(io.dropwizard.util.Duration duration) {
        if (isNull(duration)) {
            return LITERAL_NULL;
        }

        return formatDurationWords(duration.toMilliseconds());
    }

    /**
     * A thin wrapper around {@link DurationFormatUtils#formatDurationWords(long, boolean, boolean)} that always
     * suppresses leading and trailing "zero elements" because why would you want to see
     * "0 days 7 hours 25 minutes 0 seconds" instead of just "7 hours 25 minutes"? (We cannot think of a good reason...)
     *
     * @param durationMillis the duration in milliseconds to format
     * @return the duration in words (e.g. 10 minutes)
     * @implNote The only real reason for this method to exist is so we don't constantly have to pass the two
     * boolean arguments. Plus, boolean arguments are evil because what exactly does "true, false" tell you without
     * requiring you to look at the parameter documentation?
     * @see DurationFormatUtils#formatDurationWords(long, boolean, boolean)
     */
    public static String formatDurationWords(long durationMillis) {
        return DurationFormatUtils.formatDurationWords(
                durationMillis,
                SUPPRESS_LEADING_ZERO_ELEMENTS,
                SUPPRESS_TRAILING_ZERO_ELEMENTS);
    }
}
