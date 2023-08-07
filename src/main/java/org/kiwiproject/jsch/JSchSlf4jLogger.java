package org.kiwiproject.jsch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom logger implementing Jsch's {@link com.jcraft.jsch.Logger} class.
 * <p>
 * Some implementation notes:
 * <ul>
 * <li>JSch's INFO level logging is very verbose, so we are treating it like DEBUG level</li>
 * <li>Jsch has both ERROR and FATAL levels, but SLF4J only has ERROR, so we treat both as ERROR in SLF4J</li>
 * </ul>
 */
public class JSchSlf4jLogger implements com.jcraft.jsch.Logger {

    @SuppressWarnings("NonConstantLogger")
    private final Logger slf4jLogger;

    /**
     * Construct an instance with a default logger, e.g. for {@code org.kiwiproject.jsch.JSchSlf4jLogger}.
     * <p>
     * Usually you should use {@link #JSchSlf4jLogger(Logger)} in order to specify the SLF4J {@link Logger} explicitly
     * to control the logging level and logger name.
     */
    public JSchSlf4jLogger() {
        this.slf4jLogger = LoggerFactory.getLogger(JSchSlf4jLogger.class);
    }

    /**
     * Construct an instance using the given SLF4J logger.
     *
     * @param slf4jLogger the SLF4J {@link Logger} that will be the destination for JSch logging output.
     */
    public JSchSlf4jLogger(Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
    }

    /**
     * Is the given level enabled?
     *
     * @param level one of the public constants in {@link com.jcraft.jsch.Logger}
     * @return true if the level is enabled in the underlying SLF4J logger.
     */
    @Override
    public boolean isEnabled(int level) {
        return switch (level) {
            case DEBUG, INFO -> slf4jLogger.isDebugEnabled();
            case WARN -> slf4jLogger.isWarnEnabled();
            case ERROR, FATAL -> slf4jLogger.isErrorEnabled();
            default -> {
                slf4jLogger.error("Was passed invalid level: {}", level);
                yield false;
            }
        };
    }

    /**
     * Log the given message at the given level.
     * <p>
     * If provided an invalid level, a message will be logged about the invalid level, along with the given message,
     * at the SLF4J ERROR level. This is intended to provide both information about the problem and provide
     * the original message, rather than throwing an exception or suppressing the message.
     *
     * @param level   one of the public constants in {@link com.jcraft.jsch.Logger}
     * @param message the message to log
     */
    @Override
    public void log(int level, String message) {
        // Probably don't need to check this again, but it is not clear since there is no documentation
        // on proper usage in JSch. So just being safe and checking again.
        if (isValidLevel(level) && isNotEnabled(level)) {
            return;
        }

        switch (level) {
            case DEBUG, INFO -> slf4jLogger.debug(message);
            case WARN -> slf4jLogger.warn(message);
            case ERROR, FATAL -> slf4jLogger.error(message);
            default ->
                    slf4jLogger.error("Was passed invalid level: {}. (Message the caller wanted to log: {})", level, message);
        }
    }

    private boolean isValidLevel(int level) {
        return switch (level) {
            case DEBUG, INFO, WARN, ERROR, FATAL -> true;
            default -> false;
        };
    }

    private boolean isNotEnabled(int level) {
        return !isEnabled(level);
    }
}
