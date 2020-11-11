package org.kiwiproject.jsch;

import static com.jcraft.jsch.Logger.DEBUG;
import static com.jcraft.jsch.Logger.ERROR;
import static com.jcraft.jsch.Logger.FATAL;
import static com.jcraft.jsch.Logger.INFO;
import static com.jcraft.jsch.Logger.WARN;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * <em>IMPORTANT:</em>
 * In order to setup SLF4J loggers at the appropriate level, the {@code src/test/resources/logback.xml} contains five
 * loggers for the five different SLF4J log levels (TRACE, DEBUG, INFO, WARN, ERROR).
 * <p>
 * The name format is: {@code jsch-logger-<level>} -- for example the DEBUG level logger is named
 * {@code jsch-logger-DEBUG}. These loggers MUST exist for this test to work properly.
 */
@DisplayName("JSchSlf4jLogger")
class JSchSlf4jLoggerTest {

    private Logger slf4jLogger;
    private JSchSlf4jLogger logger;
    private PrintStream originalSystemOut;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        nullifySlf4jLoggerToForcePerTestInitialization();
        redirectStandardOut();
    }

    private void nullifySlf4jLoggerToForcePerTestInitialization() {
        slf4jLogger = null;
    }

    private void redirectStandardOut() {
        originalSystemOut = System.out;

        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
    }

    @Nested
    class IsEnabled {

        @ParameterizedTest
        @ValueSource(ints = {-1, 5, 6, 100})
        void shouldReturnFalse_WhenGivenInvalidLevel() {
            setupJschLoggerWithSlf4jLoggerAtLevel("DEBUG");

            assertThat(logger.isEnabled(999)).isFalse();

            var logOutput = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(logOutput).contains("Was passed invalid level: 999");
        }

        @ParameterizedTest
        @ValueSource(ints = {WARN, ERROR, FATAL})
        void shouldReturnTrue_WhenEnabled(int level) {
            setupJschLoggerWithSlf4jLoggerAtLevel("WARN");

            assertThat(logger.isEnabled(level)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {DEBUG, INFO, WARN})
        void shouldReturnFalse_WhenDisabled(int level) {
            setupJschLoggerWithSlf4jLoggerAtLevel("ERROR");

            assertThat(logger.isEnabled(level)).isFalse();
        }
    }

    @Nested
    class Log {

        @Test
        void shouldRecordError_WhenGivenInvalidLevel() {
            setupJschLoggerWithSlf4jLoggerAtLevel("DEBUG");

            logger.log(-1, "test DEBUG message");

            var logOutput = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(logOutput)
                    .contains("Was passed invalid level: -1")
                    .contains(" (Message the caller wanted to log: test DEBUG message)");
        }

        @Test
        void shouldLogAtTRACE() {
            setupJschLoggerWithSlf4jLoggerAtLevel("TRACE");

            logAtAllLevels();

            var logOutput = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(logOutput).contains(
                    "test DEBUG message",
                    "test INFO message",
                    "test WARN message",
                    "test WARN message",
                    "test ERROR message",
                    "test FATAL message");
        }

        @Test
        void shouldLog_Jsch_DEBUG_Or_INFO_Level_At_SLF4J_DEBUG_Level() {
            setupJschLoggerWithSlf4jLoggerAtLevel("DEBUG");

            logAtAllLevels();

            var logOutput = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(logOutput).contains(
                    "test DEBUG message",
                    "test INFO message",
                    "test WARN message",
                    "test WARN message",
                    "test ERROR message",
                    "test FATAL message"
            );
        }

        @Test
        void shouldLog_Jsch_WARN_Level_At_SLF4J_WARN_Level() {
            setupJschLoggerWithSlf4jLoggerAtLevel("WARN");

            logAtAllLevels();

            var logOutput = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(logOutput).contains(
                    "test WARN message",
                    "test WARN message",
                    "test ERROR message",
                    "test FATAL message")
                    .doesNotContain("test DEBUG message")
                    .doesNotContain("test INFO message");
        }

        // See logback.xml in src/test which configures org.kiwiproject.jsch.JSchSlf4jLogger at WARN level
        @Test
        void shouldLog_AtLogbackConfiguredLevel_WhenConstructUsingDefaultConstructor() {
            logger = new JSchSlf4jLogger();

            logAtAllLevels();

            var logOutput = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(logOutput).contains(
                    "test WARN message",
                    "test WARN message",
                    "test ERROR message",
                    "test FATAL message")
                    .doesNotContain("test DEBUG message")
                    .doesNotContain("test INFO message");
        }

        @Test
        void shouldLog_Jsh_ERROR_Or_FATAL_Level_At_SLF4J_ERROR_Level() {
            setupJschLoggerWithSlf4jLoggerAtLevel("ERROR");

            logAtAllLevels();

            var logOutput = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(logOutput).contains(
                    "test ERROR message",
                    "test FATAL message")
                    .doesNotContain("test DEBUG message")
                    .doesNotContain("test INFO message")
                    .doesNotContain("test WARN message");
        }
    }

    private void logAtAllLevels() {
        logger.log(DEBUG, "test DEBUG message");
        logger.log(INFO, "test INFO message");
        logger.log(WARN, "test WARN message");
        logger.log(ERROR, "test ERROR message");
        logger.log(FATAL, "test FATAL message");
    }

    private void setupJschLoggerWithSlf4jLoggerAtLevel(String level) {
        slf4jLogger = LoggerFactory.getLogger("jsch-logger-" + level);
        logger = new JSchSlf4jLogger(slf4jLogger);
    }
}
