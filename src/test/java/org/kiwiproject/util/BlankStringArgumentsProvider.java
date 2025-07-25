package org.kiwiproject.util;

import static com.google.common.collect.Lists.newArrayList;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.List;
import java.util.stream.Stream;

/**
 * Creates an {@link ArgumentsProvider} that can feed a test method with multiple blank {@link String} objects for a
 * {@link org.junit.jupiter.params.ParameterizedTest}. This will generate a {@code null} String argument, an empty
 * String argument, a String with multiple spaces, and a String with whitespace characters.
 * <p>
 * Usage:
 * <pre>
 * {@literal @}ParameterizedTest
 * {@literal @}ArgumentsSource(BlankStringArgumentsProvider.class)
 * void testThatEachProvidedArgumentIsBlank(String blankString) {
 *     assertThat(blankString).isBlank();
 *     // or whatever else you need to test where you need a blank String
 * }
 * </pre>
 *
 * <p>
 * <strong>NOTE:</strong> This is a duplicate of the kiwi-test version. It is included here to prevent a circular dependency.
 */
public class BlankStringArgumentsProvider implements ArgumentsProvider {

    public static final List<String> BLANK_STRINGS = newArrayList(
            null,
            "",
            " ",
            "    ",

            // ASCII Characters
            "\t", // HORIZONTAL TABULATION (U+0009)
            "\n", // LINE FEED (U+000A)
            "\u000B", // VERTICAL TABULATION
            "\f", // FORM FEED (U+000C)
            "\r", // CARRIAGE RETURN (U+000D)
            "\u001C", // FILE SEPARATOR
            "\u001D", // GROUP SEPARATOR
            "\u001E",
            "\u001F",

            // SPACE_SEPARATOR characters (general category "Zs" of the Unicode Specification)
            " ", // SPACE (U+0020)
            // U+00A0 (NO-BREAK SPACE) is explicitly **NOT** considered whitespace per Character Javadocs
            "\u1680", // OGHAM SPACE MARK
            "\u2000", // EN QUAD
            "\u2001", // EM QUAD
            "\u2002", // EN SPACE
            "\u2003", // EM SPACE
            "\u2004", // THREE-PER-EM SPACE
            "\u2005", // FOUR-PER-EM SPACE
            "\u2006", // SIX-PER-EM SPACE
            // U+2007 (FIGURE SPACE) is **NOT** considered whitespace per Character Javadocs
            "\u2008", // PUNCTUATION SPACE
            "\u2009", // THIN SPACE
            "\u200A", // HAIR SPACE
            // U_202F (NARROW NO-BREAK SPACE) is **NOT** considered whitespace per Character Javadocs
            "\u205F", // MEDIUM MATHEMATICAL SPACE
            "\u3000", // IDEOGRAPHIC SPACE

            // LINE_SEPARATOR characters (general category "Zl" of the Unicode Specification)
            "\u2028", // LINE SEPARATOR

            // PARAGRAPH_SEPARATOR characters (general category "Zp" of the Unicode Specification)
            "\u2029", // PARAGRAPH SEPARATOR

            // Multiple character tests
            // ASCII chars
            "\t \n \u000B \f \r \u001C \u001D \u001E \u001F",
            // SPACE_SEPARATOR chars
            "\u1680 \u2000 \u2001 \u2002 \u2003 \u2004 \u2005 \u2006 \u2008 \u2009 \u200A \u205F \u3000",
            // LINE_SEPARATOR & PARAGRAPH_SEPARATOR chars
            "\u2028 \u2029"
    );

    /**
     * Provides a bunch of blank arguments for {@link org.junit.jupiter.params.ParameterizedTest} tests.
     *
     * @param context the extension context
     * @return a {@link Stream} of blank String {@link Arguments}
     * @implNote The stream must be instantiated for each test. If you try to extract this out to a constant,
     * any test class that uses this {@link ArgumentsProvider} more than once will fail due to the stream being
     * exhausted.
     */
    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
        return BLANK_STRINGS.stream()
                .map(Arguments::of);
    }
}
