package org.kiwiproject.base;

import com.google.common.base.Splitter;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Utility methods relating to strings or similar.
 */
@UtilityClass
public class KiwiStrings {

    public static final char SPACE = ' ';

    public static final char TAB = '\t';

    public static final char COMMA = ',';

    public static final char NEWLINE = '\n';

    private static final Splitter TRIM_OMIT_EMPTY_SPACE_SPLITTER =
            Splitter.on(SPACE).omitEmptyStrings().trimResults();

    private static final Splitter TRIM_OMIT_EMPTY_TAB_SPLITTER =
            Splitter.on(TAB).omitEmptyStrings().trimResults();

    private static final Splitter TRIM_OMIT_EMPTY_COMMA_SPLITTER =
            Splitter.on(COMMA).omitEmptyStrings().trimResults();

    private static final Splitter TRIM_OMIT_EMPTY_NEWLINE_SPLITTER =
            Splitter.on(NEWLINE).omitEmptyStrings().trimResults();

    public static Iterable<String> splitOnSpaces(CharSequence sequence) {
        return split(sequence, SPACE);
    }

    public static Iterable<String> splitOnTabs(CharSequence sequence) {
        return split(sequence, TAB);
    }

    public static Iterable<String> splitOnCommas(CharSequence sequence) {
        return split(sequence, COMMA);
    }

    public static Iterable<String> splitOnNewlines(CharSequence sequence) {
        return split(sequence, NEWLINE);
    }

    public static Iterable<String> split(CharSequence sequence, char separator) {
        checkSequenceArgument(sequence);
        switch (separator) {
            case SPACE:
                return TRIM_OMIT_EMPTY_SPACE_SPLITTER.split(sequence);

            case TAB:
                return TRIM_OMIT_EMPTY_TAB_SPLITTER.split(sequence);

            case COMMA:
                return TRIM_OMIT_EMPTY_COMMA_SPLITTER.split(sequence);

            case NEWLINE:
                return TRIM_OMIT_EMPTY_NEWLINE_SPLITTER.split(sequence);

            default:
                return Splitter.on(separator).omitEmptyStrings().trimResults().split(sequence);
        }
    }

    public static Iterable<String> split(CharSequence sequence, String separator) {
        checkSequenceArgument(sequence);
        checkStringSeparatorArgument(separator);
        return Splitter.on(separator).omitEmptyStrings().trimResults().split(sequence);
    }

    public static List<String> splitToListOnSpaces(CharSequence sequence) {
        return splitToList(sequence, SPACE);
    }

    public static List<String> splitToListOnTabs(CharSequence sequence) {
        return splitToList(sequence, TAB);
    }

    public static List<String> splitToListOnCommas(CharSequence sequence) {
        return splitToList(sequence, COMMA);
    }

    public static List<String> splitToListOnNewlines(CharSequence sequence) {
        return splitToList(sequence, NEWLINE);
    }

    public static List<String> splitToList(CharSequence sequence, char separator) {
        checkSequenceArgument(sequence);
        switch (separator) {
            case SPACE:
                return TRIM_OMIT_EMPTY_SPACE_SPLITTER.splitToList(sequence);

            case TAB:
                return TRIM_OMIT_EMPTY_TAB_SPLITTER.splitToList(sequence);

            case COMMA:
                return TRIM_OMIT_EMPTY_COMMA_SPLITTER.splitToList(sequence);

            case NEWLINE:
                return TRIM_OMIT_EMPTY_NEWLINE_SPLITTER.splitToList(sequence);

            default:
                return Splitter.on(separator).omitEmptyStrings().trimResults().splitToList(sequence);
        }
    }

    public static List<String> splitToList(CharSequence sequence, String separator) {
        checkSequenceArgument(sequence);
        checkStringSeparatorArgument(separator);
        return Splitter.on(separator).omitEmptyStrings().trimResults().splitToList(sequence);
    }

    private static void checkSequenceArgument(CharSequence sequence) {
        checkArgument(nonNull(sequence), "sequence cannot be null");
    }

    private static void checkStringSeparatorArgument(String separator) {
        checkArgument(isNotBlank(separator), "separator cannot be blank");
    }

    /**
     * Performs string interpolation using either Guava- or SLF4J-style replacement placeholders. Following is copied
     * and ever so slightly modified from Guava:
     * <p>
     * Substitutes each placeholder - either {@code %s} or {@code {}} - in {@code template} with an argument. These are
     * matched by position: the first placeholder gets {@code args[0]}, etc. If there are more arguments than
     * placeholders, the unmatched arguments will be appended to the end of the formatted message in
     * square braces.
     * <p>
     * Note that if the template contains a {@code %s}, then Guava style is assumed. Otherwise, SLF4J style is assumed
     * but is not verified, i.e. if you don't use either of these placeholders then no replacements are performed, and
     * all values are simply appended to the end in square brackets.
     *
     * @param template a string containing 0 or more placeholders
     * @param args     the arguments to be substituted into the message template. Arguments are converted
     *                 to strings using {@link String#valueOf(Object)}. Arguments can be null.
     * @return the string resulting from substituting the replacements into the template
     * @implNote This is copied and slightly modified from Guava's
     * {@link com.google.common.base.Preconditions#format(String, Object...)} for several reasons. First, because
     * that method is not public in Guava, yet it is useful outside precondition checks. Second, this method
     * allows either Guava-style {@code %s} or SLF4J-style {@code {}} placeholders.
     */
    public static String format(String template, Object... args) {
        String nonNullTemplate = String.valueOf(template);  // null -> "null"
        if (nonNullTemplate.contains("%s")) {
            return formatGuavaStyle(template, args);
        }
        return formatSlf4jJStyle(nonNullTemplate, args);
    }

    /**
     * Alias for {@link #format(String, Object...)}.
     */
    public static String f(String template, Object... args) {
        return format(template, args);
    }

    /**
     * Same as {@link #format(String, Object...)} assuming Guava-style placeholders.
     *
     * @see #format(String, Object...)
     */
    public static String formatGuavaStyle(String template, Object... args) {
        return formatInternal(template, "%s", args);
    }

    /**
     * Same as {@link #format(String, Object...)} assuming SLF4J-style placeholders.
     *
     * @see #format(String, Object...)
     */
    public static String formatSlf4jJStyle(String template, Object... args) {
        return formatInternal(template, "{}", args);
    }

    /**
     * This is copied and modified from Guava to accommodate {@code %s} or {@code {}} placeholders.
     */
    private static String formatInternal(String template, String placeholder, Object... args) {
        String nonNullTemplate = String.valueOf(template); // null -> "null"

        // start substituting the arguments into the placeholders
        StringBuilder builder = new StringBuilder(nonNullTemplate.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = nonNullTemplate.indexOf(placeholder, templateStart);
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
