package org.kiwiproject.search;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * Utilities related to searching and pagination. Supports both zero- and one-based page numbering, but the
 * default is one-based. Use the methods that accept {@link PageNumberingScheme} to work with either zero-
 * or one-based numbering.
 */
@UtilityClass
public class KiwiSearching {

    /**
     * Enum that represents either zero or one-based page numbering scheme.
     */
    public enum PageNumberingScheme {

        /**
         * Page numbers start at zero.
         */
        ZERO_BASED("pageNumber starts at 0", 0),

        /**
         * Page numbers start at one.
         */
        ONE_BASED("pageNumber starts at 1", 1);

        /**
         * @implNote Allow access through a traditional getter method or via a public field (which is perfectly
         * fine since this is an immutable String.)
         */
        @Getter
        public final String pageNumberError;

        /**
         * @implNote Allow access through traditional getter method or via public (immutable) field
         */
        @Getter
        public final int minimumPageNumber;

        PageNumberingScheme(String pageNumberError, int minimumPageNumber) {
            this.pageNumberError = pageNumberError;
            this.minimumPageNumber = minimumPageNumber;
        }

        /**
         * Check the given page number for this scheme's minimum page number.
         *
         * @param pageNumber the page number to check
         * @throws IllegalArgumentException if the given page number is not valid
         */
        public void checkPageNumber(int pageNumber) {
            checkArgument(pageNumber >= minimumPageNumber, pageNumberError);
        }
    }

    /**
     * A rather opinionated value for the default page size.
     */
    public static final int DEFAULT_PAGE_SIZE = 25;

    /**
     * The rather opinionated value for default page size as a String, to support web framework annotations
     * like Jakarta REST's {@code jakarta.ws.rs.DefaultValue} that require a String.
     *
     * @implNote This <em>must</em> be a constant not an expression, otherwise trying to use it in an annotation like
     * the Jakarta REST {@code DefaultValue} will result in a compilation error due to the vagaries of Java annotations.
     * For example, trying to do this:
     * {@code DEFAULT_PAGE_SIZE_AS_STRING = String.valueOf(DEFAULT_PAGE_SIZE)}
     * and then using in an annotation like this:
     * {@code @DefaultValue(DEFAULT_PAGE_SIZE_AS_STRING)}
     * will result in the following compiler error: "java: element value must be a constant expression"
     */
    public static final String DEFAULT_PAGE_SIZE_AS_STRING = "25";

    public static final String PAGE_SIZE_ERROR = "pageSize must be at least 1";

    /**
     * Validate that the given page size is greater than zero.
     *
     * @param pageSize the page size to check
     */
    public static void checkPageSize(int pageSize) {
        checkArgument(pageSize > 0, PAGE_SIZE_ERROR);
    }

    /**
     * Validate the given page number is greater than zero. <em>This uses one-based page numbering.</em>
     *
     * @param pageNumber the page number to check
     * @throws IllegalArgumentException if the page number is not greater than zero
     * @see #checkPageNumber(int, PageNumberingScheme)
     * @see PageNumberingScheme#ONE_BASED
     */
    public static void checkPageNumber(int pageNumber) {
        PageNumberingScheme.ONE_BASED.checkPageNumber(pageNumber);
    }

    /**
     * Validate the given page number is equal to or greater than the minimum for the given {@link PageNumberingScheme}.
     *
     * @param pageNumber      the page number to check
     * @param numberingScheme the page numbering scheme to use
     * @throws IllegalArgumentException if the page number is invalid, according to the numbering scheme
     * @see PageNumberingScheme
     */
    public static void checkPageNumber(int pageNumber, PageNumberingScheme numberingScheme) {
        checkArgumentNotNull(numberingScheme);
        numberingScheme.checkPageNumber(pageNumber);
    }

    /**
     * Calculate the <em>zero-based offset</em> for the given page number and size using page numbers starting
     * at one, after first validating the page number and size. Useful for any data access library that requires a
     * zero-based offset.
     * <p>
     * <em>This uses one-based page numbering.</em>
     *
     * @param pageNumber the page number (one-based)
     * @param pageSize   the page size
     * @return the zero-based offset, e.g., for use in SQL queries using OFFSET and LIMIT
     * @throws IllegalArgumentException if the page number or size is invalid
     * @see PageNumberingScheme#ONE_BASED
     * @see #zeroBasedOffset(int, PageNumberingScheme, int)
     */
    public static int zeroBasedOffset(int pageNumber, int pageSize) {
        return zeroBasedOffset(pageNumber, PageNumberingScheme.ONE_BASED, pageSize);
    }

    /**
     * Calculate the <em>zero-based offset</em> for the given page number and size using page numbers starting
     * at one, after first validating the page number and size. Useful for any data access library that requires a
     * zero-based offset.
     * <p>
     * This method is an alias for {@link #zeroBasedOffset(int, int)}.
     *
     * @param pageNumber the page number (one-based)
     * @param pageSize   the page size
     * @return the zero-based offset, e.g., for use in SQL queries using OFFSET and LIMIT
     * @see #zeroBasedOffset(int, int)
     */
    public static int zeroBasedOffsetForOneBasedPaging(int pageNumber, int pageSize) {
        return zeroBasedOffset(pageNumber, pageSize);
    }

    /**
     * Calculate the <em>zero-based offset</em> for the given page number and size using page numbers starting
     * at zero, after first validating the page number and size. Useful for any data access library that requires a
     * zero-based offset.
     *
     * @param pageNumber the page number (zero-based)
     * @param pageSize   the page size
     * @return the zero-based offset, e.g., for use in SQL queries using OFFSET and LIMIT
     */
    public static int zeroBasedOffsetForZeroBasedPaging(int pageNumber, int pageSize) {
        return zeroBasedOffset(pageNumber, PageNumberingScheme.ZERO_BASED, pageSize);
    }

    /**
     * Calculate the <em>zero-based offset</em> for the given page number and size using the given page numbering
     * scheme, after first validating the page number and size. Useful for any data access library that requires
     * a zero-based offset.
     *
     * @param pageNumber      the page number
     * @param numberingScheme the page numbering scheme to use
     * @param pageSize        the page size
     * @return the zero-based offset, e.g., for use in SQL queries using OFFSET and LIMIT
     * @throws IllegalArgumentException if the page number or size is invalid
     * @see PageNumberingScheme
     */
    public static int zeroBasedOffset(int pageNumber, PageNumberingScheme numberingScheme, int pageSize) {
        checkPageNumber(pageNumber, numberingScheme);
        checkPageSize(pageSize);
        return (pageNumber - numberingScheme.minimumPageNumber) * pageSize;
    }

    /**
     * Calculate the number of pages necessary to paginate the given number of results using the given page size.
     *
     * @param resultCount the total number of results to paginate
     * @param pageSize    the size of each page
     * @return the number of pages
     * @throws IllegalArgumentException if the page size is invalid
     */
    public static int numberOfPages(long resultCount, int pageSize) {
        checkPageSize(pageSize);
        return (int) (resultCount / pageSize) + (resultCount % pageSize > 0 ? 1 : 0);
    }

    /**
     * Calculate the number of results on the given page number for the given number of results and page size.
     * <em>This uses one-based page numbering.</em>
     *
     * @param resultCount the total number of results to paginate
     * @param pageSize    the size of each page
     * @param pageNumber  the page number
     * @return the number of results on the given page
     * @throws IllegalArgumentException if page number or size is invalid
     * @see PageNumberingScheme#ONE_BASED
     * @see #numberOnPage(long, int, int, PageNumberingScheme)
     */
    public static int numberOnPage(long resultCount, int pageSize, int pageNumber) {
        return numberOnPage(resultCount, pageSize, pageNumber, PageNumberingScheme.ONE_BASED);
    }

    /**
     * Calculate the number of results on the given page number for the given number of results and page size, and
     * using the given {@link PageNumberingScheme}.
     *
     * @param resultCount     the total number of results to paginate
     * @param pageSize        the size of each page
     * @param pageNumber      the page number
     * @param numberingScheme the page numbering scheme to use
     * @return the number of results on the given page
     * @throws IllegalArgumentException if page number or size is invalid
     * @see PageNumberingScheme
     */
    public static int numberOnPage(long resultCount,
                                   int pageSize,
                                   int pageNumber,
                                   PageNumberingScheme numberingScheme) {

        checkPageNumber(pageNumber, numberingScheme);
        checkPageSize(pageSize);

        if (resultCount == 0) {
            return 0;
        }

        var numPages = numberOfPages(resultCount, pageSize);

        var schemePageNumber = (numberingScheme == PageNumberingScheme.ONE_BASED) ? pageNumber : pageNumber + 1;

        if (schemePageNumber > numPages) {
            return 0;
        }

        if (schemePageNumber == numPages) {
            long remainder = resultCount % pageSize;
            return remainder == 0 ? pageSize : (int) remainder;
        }

        return pageSize;
    }
}
