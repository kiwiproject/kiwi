package org.kiwiproject.base;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Utility methods relating to strings or similar.
 */
@UtilityClass
@SuppressWarnings("WeakerAccess")
public final class KiwiStrings {

    /**
     * A space character.
     */
    public static final char SPACE = ' ';

    /**
     * A tab character.
     */
    public static final char TAB = '\t';

    /**
     * A comma character.
     */
    public static final char COMMA = ',';

    /**
     * A newline character.
     */
    public static final char NEWLINE = '\n';

    private static final Splitter TRIMMING_AND_EMPTY_OMITTING_SPACE_SPLITTER =
            Splitter.on(SPACE).omitEmptyStrings().trimResults();

    private static final Splitter TRIMMING_AND_EMPTY_OMITTING_TAB_SPLITTER =
            Splitter.on(TAB).omitEmptyStrings().trimResults();

    private static final Splitter TRIMMING_AND_EMPTY_OMITTING_COMMA_SPLITTER =
            Splitter.on(COMMA).omitEmptyStrings().trimResults();

    private static final Splitter TRIMMING_AND_EMPTY_OMITTING_NEWLINE_SPLITTER =
            Splitter.on(NEWLINE).omitEmptyStrings().trimResults();

    /**
     * Splits the given {@link CharSequence}, using a {@link #SPACE} as the separator character, omitting any empty
     * strings and trimming leading and trailing whitespace.
     *
     * @param sequence the character sequence to be split
     * @return an Iterable over the split strings
     * @see #splitWithTrimAndOmitEmpty(CharSequence, char)
     */
    public static Iterable<String> splitWithTrimAndOmitEmpty(CharSequence sequence) {
        return splitWithTrimAndOmitEmpty(sequence, SPACE);
    }

    /**
     * Splits the given {@link CharSequence}, using the specified separator character, omitting any empty
     * strings and trimming leading and trailing whitespace.
     *
     * @param sequence  the character sequence to be split
     * @param separator the separator character to use
     * @return an Iterable over the split strings
     */
    public static Iterable<String> splitWithTrimAndOmitEmpty(CharSequence sequence, char separator) {
        switch (separator) {
            case COMMA:
                return TRIMMING_AND_EMPTY_OMITTING_COMMA_SPLITTER.split(sequence);
            case SPACE:
                return TRIMMING_AND_EMPTY_OMITTING_SPACE_SPLITTER.split(sequence);
            case TAB:
                return TRIMMING_AND_EMPTY_OMITTING_TAB_SPLITTER.split(sequence);
            case NEWLINE:
                return TRIMMING_AND_EMPTY_OMITTING_NEWLINE_SPLITTER.split(sequence);
            default:
                return Splitter.on(separator).omitEmptyStrings().trimResults().split(sequence);
        }
    }

    /**
     * Splits the given {@link CharSequence}, using the specified separator string, omitting any empty
     * strings and trimming leading and trailing whitespace.
     *
     * @param sequence  the character sequence to be split
     * @param separator the separator to use, e.g. {@code ", "}
     * @return an Iterable over the split strings
     */
    public static Iterable<String> splitWithTrimAndOmitEmpty(CharSequence sequence, String separator) {
        return Splitter.on(separator).omitEmptyStrings().trimResults().split(sequence);
    }

    /**
     * Splits the given {@link CharSequence}, using a {@link #SPACE} as the separator character, omitting any empty
     * strings and trimming leading and trailing whitespace. Returns an <i>immutable</i> list.
     *
     * @param sequence the character sequence to be split
     * @return an immutable list containing the split strings
     * @see #splitWithTrimAndOmitEmpty(CharSequence, char)
     */
    public static List<String> splitToList(CharSequence sequence) {
        return splitToList(sequence, SPACE);
    }

    /**
     * Splits the given {@link CharSequence}, using the specified separator character, omitting any empty
     * strings and trimming leading and trailing whitespace. Returns an <i>immutable</i> list.
     *
     * @param sequence  the character sequence to be split
     * @param separator the separator character to use
     * @return an immutable list containing the split strings
     * @see #splitWithTrimAndOmitEmpty(CharSequence, char)
     */
    public static List<String> splitToList(CharSequence sequence, char separator) {
        switch (separator) {
            case COMMA:
                return TRIMMING_AND_EMPTY_OMITTING_COMMA_SPLITTER.splitToList(sequence);
            case SPACE:
                return TRIMMING_AND_EMPTY_OMITTING_SPACE_SPLITTER.splitToList(sequence);
            case TAB:
                return TRIMMING_AND_EMPTY_OMITTING_TAB_SPLITTER.splitToList(sequence);
            case NEWLINE:
                return TRIMMING_AND_EMPTY_OMITTING_NEWLINE_SPLITTER.splitToList(sequence);
            default:
                return Splitter.on(separator).omitEmptyStrings().trimResults().splitToList(sequence);
        }
    }

    /**
     * Splits the given {@link CharSequence}, using the specified separator character, into the maximum number of groups
     * specified omitting any empty strings and trimming leading and trailing whitespace. Returns an
     * <i>immutable</i> list.
     *
     * @param sequence  the character sequence to be split
     * @param separator the separator character to use
     * @param maxGroups the maximum number of groups to separate into
     * @return an immutable list containing the split strings
     */
    public static List<String> splitToList(CharSequence sequence, char separator, int maxGroups) {
        return Splitter.on(separator).limit(maxGroups).omitEmptyStrings().trimResults().splitToList(sequence);
    }

    /**
     * Splits the given {@link CharSequence}, using the specified separator string, omitting any empty
     * strings and trimming leading and trailing whitespace. Returns an <i>immutable</i> list.
     *
     * @param sequence  the character sequence to be split
     * @param separator the separator string to use
     * @return an immutable list containing the split strings
     */
    public static List<String> splitToList(CharSequence sequence, String separator) {
        return Splitter.on(separator).omitEmptyStrings().trimResults().splitToList(sequence);
    }

    /**
     * Splits the given {@link CharSequence}, using the specified separator string, into the maximum number of groups
     * specified omitting any empty strings and trimming leading and trailing whitespace. Returns an
     * <i>immutable</i> list.
     *
     * @param sequence  the character sequence to be split
     * @param separator the separator string to use
     * @param maxGroups the maximum number of groups to separate into
     * @return an immutable list containing the split strings
     */
    public static List<String> splitToList(CharSequence sequence, String separator, int maxGroups) {
        return Splitter.on(separator).limit(maxGroups).omitEmptyStrings().trimResults().splitToList(sequence);
    }

    /**
     * Convenience method that splits the given comma-delimited {@link CharSequence}, omitting any empty strings and
     * trimming leading and trailing whitespace. Returns an <i>immutable</i> list.
     *
     * @param sequence the character sequence to be split
     * @return an immutable list containing the split strings
     * @see #splitWithTrimAndOmitEmpty(CharSequence, char)
     */
    public static List<String> splitOnCommas(CharSequence sequence) {
        return ImmutableList.copyOf(splitWithTrimAndOmitEmpty(sequence, COMMA));
    }

    /**
     * Convenience method that splits the given comma-delimited {@link CharSequence}, omitting any empty strings and
     * trimming leading and trailing whitespace. Returns an <i>immutable</i> list.
     *
     * @param sequence the character sequence to be split, may be null
     * @return an immutable list containing the split strings, or an empty list if {@code sequence} is blank
     * @see #splitWithTrimAndOmitEmpty(CharSequence, char)
     */
    public static List<String> nullSafeSplitOnCommas(CharSequence sequence) {
        if (isBlank(sequence)) {
            return List.of();
        }

        return splitOnCommas(sequence);
    }

    /**
     * Returns a null if the input string is all whitespace characters or null.
     *
     * @param sequence a possibly null, blank, or zero length String.
     * @return null if {@code sequence} is blank, otherwise return {@code sequence}
     */
    public static String blankToNull(String sequence) {
        return isBlank(sequence) ? null : sequence;
    }

    /**
     * Substitutes each {@code %s} or {@code {}} in {@code template} with an argument. These are matched by
     * position: the first {@code %s} (or {@code {}}) gets {@code args[0]}, etc. If there are more arguments than
     * placeholders, the unmatched arguments will be appended to the end of the formatted message in
     * square braces.
     * <p>
     * <em>This method currently accepts <strong>either</strong> {@code %s} or {@code {}} <strong>but not both at
     * the same time</strong></em>. It won't work if you mix and match them as that is confusing anyway. What will happen
     * is that only the {@code %s} placeholders will be resolved, and the {@code {}} will appear as a literal {@code {}}
     * and the resulting message will thus be very difficult to understand, as there will be more arguments than
     * {@code %s} placeholders.
     * <p>
     * Generally you should pick one style and be consistent throughout your entire application. Since originally this
     * method only supported the Guava {@code %s}, this support was retained for obvious backward-compatibility reasons,
     * and the SLF4J {@code {}} style as added because we kept coming across instances where people are used to SLF4J
     * replacement parameter style and used that, thus making the message not interpolate correctly (though thanks to
     * Guava's implementation, all the parameter values are still displayed after the message as extra parameters).
     * <p>
     * This method was originally copied directly from Guava 18.0's
     * {@code com.google.common.base.Preconditions#format(String, Object...)}
     * because it was not public in Guava, and it is useful and provides better performance than using the
     * {@link String#format(java.util.Locale, String, Object...)} method. A slight modification we made is to not
     * re-assign the {@code template} argument. Guava 25.1 moved this very useful functionality into the Guava
     * {@link com.google.common.base.Strings} class as
     * {{@link com.google.common.base.Strings#lenientFormat(String, Object...)}}. However, it only accepts {@code %s}
     * as the replacement placeholder. For performance comparisons of the JDK {@link String#format(String, Object...)}
     * method, see http://stackoverflow.com/questions/12786902/performance-javas-string-format
     *
     * @param template a non-null string containing 0 or more {@code %s} or {@code {}} placeholders.
     * @param args     the arguments to be substituted into the message template. Arguments are converted
     *                 to strings using {@link String#valueOf(Object)}. Arguments can be null.
     * @return the formatted string after making replacements
     */
    public static String format(String template, Object... args) {
        var nonNullTemplate = String.valueOf(template);  // null -> "null"

        if (nonNullTemplate.contains("%s")) {
            return formatGuavaStyle(template, args);
        }
        return formatSlf4jJStyle(nonNullTemplate, args);
    }

    /**
     * Alias for {@link #format(String, Object...)}.
     *
     * @param template a non-null string containing 0 or more {@code %s} or {@code {}} placeholders.
     * @param args     the arguments to be substituted into the message template. Arguments are converted
     *                 to strings using {@link String#valueOf(Object)}. Arguments can be null.
     * @return the formatted string after making replacements
     */
    public static String f(String template, Object... args) {
        return format(template, args);
    }

    /**
     * Same as {@link #format(String, Object...)} assuming Guava-style placeholders.
     *
     * @param template a non-null string containing 0 or more {@code %s} placeholders.
     * @param args     the arguments to be substituted into the message template. Arguments are converted
     *                 to strings using {@link String#valueOf(Object)}. Arguments can be null.
     * @return the formatted string after making replacements
     * @see #format(String, Object...)
     */
    public static String formatGuavaStyle(String template, Object... args) {
        return formatInternal(template, "%s", args);
    }

    /**
     * Same as {@link #format(String, Object...)} assuming SLF4J-style placeholders.
     *
     * @param template a non-null string containing 0 or more {@code {}} placeholders.
     * @param args     the arguments to be substituted into the message template. Arguments are converted
     *                 to strings using {@link String#valueOf(Object)}. Arguments can be null.
     * @return the formatted string after making replacements
     * @see #format(String, Object...)
     */
    public static String formatSlf4jJStyle(String template, Object... args) {
        return formatInternal(template, "{}", args);
    }

    /**
     * This is copied and modified from Guava 18 to accommodate {@code %s} or {@code {}} placeholders.
     */
    private static String formatInternal(String template, String placeholder, Object... args) {
        var nonNullTemplate = String.valueOf(template); // null -> "null"

        // start substituting the arguments into the placeholders
        var builder = new StringBuilder(nonNullTemplate.length() + 16 * args.length);
        var templateStart = 0;
        var i = 0;
        while (i < args.length) {
            var placeholderStart = nonNullTemplate.indexOf(placeholder, templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(nonNullTemplate, templateStart, placeholderStart);
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(nonNullTemplate, templateStart, nonNullTemplate.length());

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }
}
