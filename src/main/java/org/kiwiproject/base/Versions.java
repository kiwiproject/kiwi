package org.kiwiproject.base;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * A few simple version comparison utilities.
 */
@UtilityClass
public class Versions {

    /**
     * Given two versions, return the higher version
     *
     * @param left  the first version to compare
     * @param right the second version to compare
     * @return the higher of the given versions
     */
    public static String higherVersion(String left, String right) {
        return versionCompare(left, right) >= 0 ? left : right;
    }

    /**
     * Returns true if the "left" version is strictly higher than the "right" version.
     *
     * @param left  the first version to compare
     * @param right the second version to compare
     * @return true if {@code left} is greater than {@code right}
     */
    public static boolean isStrictlyHigherVersion(String left, String right) {
        return versionCompare(left, right) > 0;
    }

    /**
     * Returns true if the "left" version is higher than or equal to the "right" version.
     *
     * @param left  the first version to compare
     * @param right the second version to compare
     * @return true if {@code left} is greater than or equal to {@code right}
     */
    public static boolean isHigherOrSameVersion(String left, String right) {
        return versionCompare(left, right) >= 0;
    }

    /**
     * Returns true if the "left" version is strictly lower than the "right" version.
     *
     * @param left  the first version to compare
     * @param right the second version to compare
     * @return true if {@code left} is lower than {@code right}
     */
    public static boolean isStrictlyLowerVersion(String left, String right) {
        return versionCompare(left, right) < 0;
    }

    /**
     * Returns true if the "left" version is lower than or equal to the "right" version.
     *
     * @param left  the first version to compare
     * @param right the second version to compare
     * @return true if {@code left} is less than or equal to {@code right}
     */
    public static boolean isLowerOrSameVersion(String left, String right) {
        return versionCompare(left, right) <= 0;
    }

    /**
     * Returns true if the "left" version exactly equals the "right" version.
     *
     * @param left  the first version to compare
     * @param right the second version to compare
     * @return true if {@code left} equals {@code right}
     */
    public static boolean isSameVersion(String left, String right) {
        return versionCompare(left, right) == 0;
    }

    /**
     * Performs a case-insensitive, segment by segment comparison of numeric and alphanumeric version numbers. Versions
     * are split on periods and dashes. For example, the segments of "2.5.0" are "2", "5", and "0" while the segments of
     * "1.0.0-alpha.3" are "1", "0", "0", "alpha", and "3". Returns -1, 0, or 1 as the "left" version is less than,
     * equal to, or greater than the "right" version. (These return values correspond to the values returned by the
     * {@link Integer#signum(int)} function.)
     * <p>
     * When a segment is determined to be non-numeric, a case-insensitive string comparison is performed. When the
     * number of segments in the version are different, then the general logic is that the <em>shorter</em> segment is
     * the higher version. This covers commons situations such as 1.0.0-SNAPSHOT, 1.0.0-alpha, and 1.0.0-beta.2, which
     * should all be <em>lower</em> versions than 1.0.0.
     *
     * @param left  the first version number (e.g. "1.2.3" or "2.0.0-alpha1")
     * @param right the second version number (e.g. "1.2.4" or "2.0.0-alpha2")
     * @return -1 if "left" is less than "right", 0 if the versions are equal, and 1 if "left" is higher than "right"
     * @implNote The current implementation works best when versions have the same number of segments, e.g. comparing
     * 2.1.0 vs 2.0.0. It also works fine with different number of segments when those different segments are numeric,
     * such as 1.0.0 vs 1.0.0.42 (the latter is higher). It also handles most normal cases when the last segments are
     * different and are non-numeric, e.g. 1.0.0 should be considered a higher version than 1.0.0-SNAPSHOT or
     * 1.0.0-alpha. There are various edge cases that might report results that might not be what you expect; for
     * example, should 2.0.0-beta.1 be a higher or lower version than 2.0.0-beta? Currently 2.0.0-beta is reported as
     * the higher version due to the simple implementation. However, note that 2.0.0-beta1 would be reported as higher
     * than 2.0.0-beta (because the String "beta" is "greater than" the String "beta" using (Java) string comparison.
     * @see Integer#signum(int)
     */
    public static int versionCompare(String left, String right) {
        checkArgumentNotBlank(left, "left version cannot be blank");
        checkArgumentNotBlank(right, "right version cannot be blank");

        if (left.equals(right)) {
            // if there's an exact string match, exit early since they are equal
            return 0;
        }

        // 1. normalize versions to lowercase so all alphanumeric comparisons are case-insensitive
        // 2. split versions on dot/period and dash
        String[] leftParts = lowerCaseAndSplit(left);
        String[] rightParts = lowerCaseAndSplit(right);

        // find the first non-equal segment index (or the index of the last segment of the shorter version)
        int pos = indexOfFirstUnequalOrLastCommonSegment(leftParts, rightParts);

        // compare first non-equal value if we found an unequal segment before the end
        if (pos < leftParts.length && pos < rightParts.length) {
            return contextuallyCompare(leftParts[pos], rightParts[pos]);
        }

        // one of the given version arguments is a substring of the other. e.g. 1.0.0-alpha1 contains 1.0.0

        // if all segments are numeric, then whichever is longer is the higher version,
        // e.g. 3.0.1 > 3.0 and 2.5.10.1 > 2.5.10
        if (allAreNumericIn(leftParts) && allAreNumericIn(rightParts)) {
            return Integer.signum(leftParts.length - rightParts.length);
        }

        // Not all segments are numeric. These segments are assumed to contain things like alpha, beta, SNAPSHOT, etc.
        // Handle special cases such as alpha[.n], beta[.n], Mn (i.e. milestone, like M1, M2) in a generic manner such
        // that whichever part is longer is considered the *lower* version. This simple logic means that 1.0.0.alpha1
        // is a lower version than 1.0.0, 2.5.0-SNAPSHOT is a lower version than 2.5.0, and so on.
        return Integer.signum(rightParts.length - leftParts.length);
    }

    private static int indexOfFirstUnequalOrLastCommonSegment(String[] leftParts, String[] rightParts) {
        var pos = 0;
        while (pos < leftParts.length && pos < rightParts.length && leftParts[pos].equals(rightParts[pos])) {
            pos++;
        }
        return pos;
    }

    private static String[] lowerCaseAndSplit(String version) {
        return version.toLowerCase().split("[.-]");
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
        // Both args should be lowercase, so can compare exactly
        var result = StringUtils.compare(leftPart, rightPart);
        return Integer.compare(result, 0);
    }

    private static boolean allAreNumericIn(String[] elements) {
        return Arrays.stream(elements).allMatch(StringUtils::isNumeric);
    }
}
