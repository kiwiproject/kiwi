package org.kiwiproject.util.regex;

import java.util.regex.Pattern;

/**
 * Exception class to indicate a regular expression match did not match.
 */
public class NoMatchesFoundException extends RuntimeException {

    /**
     * Create a new instance with the given message.
     *
     * @param message the exception message
     */
    public NoMatchesFoundException(String message) {
        super(message);
    }

    /**
     * Factory method to create a new instance with a standardized message for the given {@link Pattern}.
     *
     * @param pattern the Pattern object
     * @return a new instance
     */
    public static NoMatchesFoundException forPattern(Pattern pattern) {
        return new NoMatchesFoundException("No match found for pattern: " + pattern);
    }
}
