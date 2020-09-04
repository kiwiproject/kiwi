package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.util.MiscConstants.POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING;

import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@DisplayName("RetryLogger")
class RetryLoggerTest {

    // Note this is a Logback Logger, NOT an SLF4J one, because we need to be able to get the MapAppender
    // The cast is necessary since LoggerFactory returns an SLF4J Logger.
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(RetryLoggerTest.class);

    private InMemoryAppender appender;

    @BeforeEach
    void setUp() {
        appender = (InMemoryAppender) LOG.getAppender("MEMORY");
        assertThat(appender)
                .describedAs(POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING)
                .isNotNull();
    }

    @AfterEach
    void tearDown() {
        appender.clearEvents();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldNotAcceptZeroOrNegativeAttemptNumbers(int currentAttempt) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> RetryLogger.logAttempt(LOG, Level.INFO, currentAttempt, "the answer is always 42"));
    }

    @Nested
    class OnFirstAttempt {

        @Test
        void shouldAlwaysLogAtTraceLevel() {
            RetryLogger.logAttempt(LOG, Level.WARN, 1, "the answer is {}", 42);

            var events = appender.assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage()).isEqualTo("the answer is 42");
            assertThat(event.getLevel()).isEqualTo(ch.qos.logback.classic.Level.TRACE);
        }
    }

    @Nested
    class OnRetries {

        @ParameterizedTest
        @EnumSource(Level.class)
        void shouldLogAtSpecifiedLevel(Level slf4jLevel) {
            RetryLogger.logAttempt(LOG, slf4jLevel, 2, "the answer is {}", 84);

            var events = appender.assertNumberOfLoggingEventsAndGet(1);

            var event = first(events);
            assertThat(event.getFormattedMessage()).isEqualTo("the answer is 84");
            assertThat(event.getLevel())
                    .describedAs("Expected level to be: %s", slf4jLevel)
                    .isEqualTo(ch.qos.logback.classic.Level.valueOf(slf4jLevel.name()));
        }
    }
}