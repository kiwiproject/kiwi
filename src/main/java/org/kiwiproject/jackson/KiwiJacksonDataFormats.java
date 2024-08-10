package org.kiwiproject.jackson;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Static utilities for easily performing data format detection on String values. Currently, this class supports
 * detection of JSON, XML, and YAML assuming the YAML begins with the explicit document start marker ("---").
 * <p>
 * NOTE: This relies entirely on jackson's data format detection!!! You will also need to ensure that the
 * jackson-core, jackson-dataformat-xml, jackson-dataformat-yaml dependencies are present at runtime.
 * <p>
 * WARNING: Before using, please make sure you understand how Jackson actually does its detection. Hint: it's not
 * magic, and may not be as robust as you might like. For example, the YAML detector will only report YAML if the
 * content starts with the formal YAML preamble "---". Use the "@see" links below for links to the various Jackson
 * classes that perform data format detection. The main class is {@link DataFormatDetector}, to which you supply
 * one or more detectors, which are {@link JsonFactory} and its subclasses.
 *
 * @see DataFormatDetector
 * @see JsonFactory
 * @see XmlFactory
 * @see YAMLFactory
 * @see Charset#defaultCharset()
 */
@UtilityClass
@Slf4j
public class KiwiJacksonDataFormats {

    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * We currently support JSON, XML, and YAML.
     */
    private static final DataFormatDetector DEFAULT_FORMAT_DETECTOR =
            new DataFormatDetector(new JsonFactory(), new XmlFactory(), new YAMLFactory());

    /**
     * Is the given text JSON using the default {@link Charset}?
     *
     * @param text the String value to check
     * @return {@code true} if {@code text} is JSON; {@code false} otherwise
     */
    public static boolean isJson(String text) {
        return isJson(text, DEFAULT_CHARSET);
    }

    /**
     * Is the given text JSON using UTF-8 {@link Charset}?
     *
     * @param text the String value to check
     * @return {@code true} if {@code text} is JSON; {@code false} otherwise
     */
    public static boolean isJsonUtf8(String text) {
        return isJson(text, UTF_8);
    }

    /**
     * Is the given text JSON using the given {@link Charset}?
     *
     * @param text    the String value to check
     * @param charset the character set to use
     * @return {@code true} if {@code text} is JSON; {@code false} otherwise
     */
    public static boolean isJson(String text, Charset charset) {
        return isFormat(JacksonDataFormat.JSON, text, charset);
    }

    /**
     * Is the given text XML using the default {@link Charset}?
     *
     * @param text the String value to check
     * @return {@code true} if {@code text} is XML; {@code false} otherwise
     */
    public static boolean isXml(String text) {
        return isXml(text, DEFAULT_CHARSET);
    }

    /**
     * Is the given text XML using UTF-8 {@link Charset}?
     *
     * @param text the String value to check
     * @return {@code true} if {@code text} is XML; {@code false} otherwise
     */
    public static boolean isXmlUtf8(String text) {
        return isXml(text, UTF_8);
    }

    /**
     * Is the given text XML using the given {@link Charset}?
     *
     * @param text    the String value to check
     * @param charset the character set to use
     * @return {@code true} if {@code text} is XML; {@code false} otherwise
     */
    public static boolean isXml(String text, Charset charset) {
        return isFormat(JacksonDataFormat.XML, text, charset);
    }

    /**
     * Is the given text YAML using the default {@link Charset}?
     *
     * @param text the String value to check
     * @return {@code true} if {@code text} is YAML; {@code false} otherwise
     * @implNote Jackson does NOT consider the content YAML without "---" at the top of the file!
     */
    public static boolean isYaml(String text) {
        return isYaml(text, DEFAULT_CHARSET);
    }

    /**
     * Is the given text YAML using UTF-8 {@link Charset}?
     *
     * @param text the String value to check
     * @return {@code true} if {@code text} is YAML; {@code false} otherwise
     * @implNote Jackson does NOT consider the content YAML without "---" at the top of the file!
     */
    public static boolean isYamlUtf8(String text) {
        return isYaml(text, UTF_8);
    }

    /**
     * Is the given text YAML using the given {@link Charset}?
     *
     * @param text    the String value to check
     * @param charset the character set to use
     * @return {@code true} if {@code text} is YAML; {@code false} otherwise
     * @implNote Jackson does NOT consider the content YAML without "---" at the top of the file!
     */
    public static boolean isYaml(String text, Charset charset) {
        return isFormat(JacksonDataFormat.YAML, text, charset);
    }

    /**
     * Does the given text, using the given {@link Charset}, have data format {@code matchFormat}.
     *
     * @param matchFormat the format to match against
     * @param text        the String value to check
     * @param charset     the character set to use
     * @return {@code true} if {@code text} is equal to {@code matchFormat}; {@code false} otherwise
     */
    public static boolean isFormat(JacksonDataFormat matchFormat, String text, Charset charset) {
        return detectFormat(text, charset)
                .filter(dataFormat -> dataFormat == matchFormat)
                .isPresent();
    }

    /**
     * Detect the data format of given text using the default {@link Charset}.
     *
     * @param text the String value to check
     * @return Optional containing detected format, or empty Optional if format was not
     * detected <em>or not supported</em>
     */
    public static Optional<JacksonDataFormat> detectFormat(String text) {
        return detectFormat(text, DEFAULT_CHARSET);
    }

    /**
     * Detect the data format of given text using UTF-8 {@link Charset}.
     *
     * @param text the String value to check
     * @return Optional containing detected format, or empty Optional if format was not
     * detected <em>or not supported</em>
     */
    public static Optional<JacksonDataFormat> detectFormatUtf8(String text) {
        return detectFormat(text, UTF_8);
    }

    /**
     * Detect the data format of given text using the given {@link Charset}.
     *
     * @param text    the String value to check
     * @param charset the character set to use
     * @return Optional containing detected format, or empty Optional if format was not
     * detected <em>or not supported</em>
     */
    public static Optional<JacksonDataFormat> detectFormat(String text, Charset charset) {
        var optionalFormatName = detectFormat(text, charset, DEFAULT_FORMAT_DETECTOR);

        return optionalFormatName
                .map(JacksonDataFormat::from)
                .or(Optional::empty);
    }

    /**
     * Detect the data format of given text using given {@link Charset} and {@link DataFormatDetector}.
     * <p>
     * This method is an "escape hatch"; it allows detection of formats not supported in {@link JacksonDataFormat}
     * since it accepts a {@link DataFormatDetector} and it returns a String. Thus, since the caller supplies its own
     * custom {@link DataFormatDetector}, the formats that are supported are dictated entirely by that caller. And
     * because it returns a String, it is not limited to the formats defined in the {@link JacksonDataFormat} enum.
     *
     * @param text     the String value to check
     * @param charset  the character set to use
     * @param detector the format detector to use
     * @return Optional containing detected format name, or empty Optional if format was not
     * detected <em>or not supported</em> by the supplied {@link DataFormatDetector}
     */
    public static Optional<String> detectFormat(String text, Charset charset, DataFormatDetector detector) {
        try {
            var bytes = text.getBytes(charset);
            var formatMatcher = detector.findFormat(bytes);

            if (formatMatcher.hasMatch()) {
                return Optional.of(formatMatcher.getMatchedFormatName());
            }
        } catch (IOException e) {
            LOG.debug("Error finding data format", e);
        }

        return Optional.empty();
    }

}
