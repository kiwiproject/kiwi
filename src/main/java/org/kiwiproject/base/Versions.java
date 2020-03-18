package org.kiwiproject.base;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * A few simple version comparison utilities.
 */
@UtilityClass
@Slf4j
public class Versions {

    /**
     * Performs a <=> comparison of numeric version numbers. When a section is determined to be non-numeric, a
     * case-insensitive string comparison is performed.
     *
     * @param left  the first version number (e.g. "1.2.3")
     * @param right the second version number (e.g. "1.2.4")
     * @return -1 if "left" is less than "right", 0 if the versions are equal, and 1 if "left" is higher than "right"
     * @implNote Current implementation works best when versions have the same number of segments, e.g. 2.1.0 vs 2.0.0.
     * It works also with different number of segments when the different segments are numeric,
     * e.g. 1.0.0 vs 1.0.0.42. It does NOT work so well right now when the last different segments are
     * non-numeric, e.g. 1.0.0 vs 1.0.0-SNAPSHOT or 1.0.0-alpha (1.0.0-SNAPSHOT and 1.0.0-alpha are considered
     * higher versions than 1.0.0 currently). See issue #45 which is intended to improve these comparisons.
     */
    public static int versionCompare(String left, String right) {
        checkArgumentNotBlank(left, "left version cannot be blank");
        checkArgumentNotBlank(right, "right version cannot be blank");

        if (left.equals(right)) {
            // if there's an exact string match, exit early since they are equal
            return 0;
        }

        // split versions on dot/period and dash
        String[] leftParts = left.split("[\\.-]");
        String[] rightParts = right.split("[\\.-]");

        // find the first non-equal ordinal (or last segment of shortest version string)
        int pos = 0;
        while (pos < leftParts.length && pos < rightParts.length && leftParts[pos].equals(rightParts[pos])) {
            pos++;
        }

        // compare first non-equal value
        if (pos < leftParts.length && pos < rightParts.length) {
            return contextuallyCompare(leftParts[pos], rightParts[pos]);
        }

        // the strings are so far equal or one is a substring of the other
        return Integer.signum(leftParts.length - rightParts.length);
    }

    private static int contextuallyCompare(String leftPart, String rightPart) {
        if (isNumeric(leftPart) && isNumeric(rightPart)) {
            return compareNumeric(leftPart, rightPart);
        }

        return compareString(leftPart, rightPart);
    }

    private static int compareNumeric(String leftPart, String rightPart) {
        int diff = Integer.valueOf(leftPart).compareTo(Integer.valueOf(rightPart));
        return Integer.signum(diff);
    }

    private static int compareString(String leftPart, String rightPart) {
        var result = StringUtils.compare(leftPart.toLowerCase(), rightPart.toLowerCase());
        return Integer.compare(result, 0);
    }
}
