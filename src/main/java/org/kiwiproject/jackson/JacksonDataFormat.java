package org.kiwiproject.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;

/**
 * Represents the detected type of a {@link String}.
 * <p>
 * Currently only supports JSON, XML, and YAML formats.
 *
 * @implNote Uses Jackson under the covers. You will need to ensure that jackson-core, jackson-dataformat-xml and
 * jackson-dataformat-yaml dependencies are present at runtime.
 * @see com.fasterxml.jackson.core.format.DataFormatDetector
 */
public enum JacksonDataFormat {

    JSON(JsonFactory.FORMAT_NAME_JSON),
    XML(XmlFactory.FORMAT_NAME_XML),
    YAML(YAMLFactory.FORMAT_NAME_YAML),
    UNKNOWN("UNKNOWN");

    @Getter
    private final String formatName;

    JacksonDataFormat(String formatName) {
        this.formatName = formatName;
    }

    /**
     * Returns one of the enum constants for the given {@code formatName}, or {@link #UNKNOWN} if the format name is
     * not found.
     * <p>
     * The {@code formatName} <em>must match exactly</em> (i.e. is case-sensitive).
     *
     * @param formatName the format name as a String
     * @return the JacksonDataFormat corresponding to the given format name (exact match)
     * @implNote Format names are the values of the public constants in the appropriate Jackson {@link JsonFactory}
     * and its subclasses.
     * @apiNote See the getFormatName() method which returns the name of the returned format
     */
    public static JacksonDataFormat from(String formatName) {
        if (JSON.formatName.equals(formatName)) {
            return JSON;
        } else if (XML.formatName.equals(formatName)) {
            return XML;
        } else if (YAML.formatName.equals(formatName)) {
            return YAML;
        }

        return UNKNOWN;
    }
}
