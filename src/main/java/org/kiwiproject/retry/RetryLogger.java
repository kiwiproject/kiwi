package org.kiwiproject.retry;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Package-private helper class to log attempts at different SLF4J logging levels. SLF4J does not
 * provide any log methods that accept a {@link Level} object, so we unfortunately have to resort to this instead.
 *
 * @see Logger SLFJ Logger
 * @see Level SLF4J Level
 */
@UtilityClass
class RetryLogger {

    /**
     * Log an attempt for the current attempt number. First attempts are <em>always</em> logged at TRACE level.
     *
     * @param logger         the Logger to use
     * @param level          the Level to log at for retry attempts
     * @param currentAttempt the current attempt number (starting at 1)
     * @param message        the message or message template
     * @param args           the optional arguments if the message is a template
     */
    static void logAttempt(Logger logger,
                           Level level,
                           long currentAttempt,
                           String message,
                           Object... args) {

        checkArgument(currentAttempt > 0, "currentAttempt must be a positive integer");

        // If this is the first attempt, log the traffic at trace level regardless of the log level argument
        if (currentAttempt == 1) {
            logger.trace(message, args);
            return;
        }

        // Otherwise, log it at the appropriate level
        logAttempt(logger, level, message, args);
    }

    /**
     * Log an attempt at the specified {@link Level}.
     *
     * @param logger  the Logger to use
     * @param level   the Level to log at for retry attempts
     * @param message the message or message template
     * @param args    the optional arguments if the message is a template
     */
    static void logAttempt(Logger logger, Level level, String message, Object... args) {
        switch (level) {
            case DEBUG -> logger.debug(message, args);
            case INFO -> logger.info(message, args);
            case WARN -> logger.warn(message, args);
            case ERROR -> logger.error(message, args);
            case TRACE -> logger.trace(message, args);
        }
    }
}
