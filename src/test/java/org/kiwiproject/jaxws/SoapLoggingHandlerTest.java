package org.kiwiproject.jaxws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;

@DisplayName("SoapLoggingHandler")
class SoapLoggingHandlerTest {

    private SoapLoggingHandler handler;
    private SOAPMessageContext context;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        handler = new SoapLoggingHandler();
        context = mock(SOAPMessageContext.class);
        logAppender = new ListAppender<>();
        logAppender.start();

        var logger = getLogbackLogger();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        var logger = getLogbackLogger();
        logger.detachAppender(logAppender);
    }

    private static Logger getLogbackLogger() {
        return (Logger) LoggerFactory.getLogger(SoapLoggingHandler.class);
    }

    @Nested
    class Constructor {

        @ParameterizedTest
        @EnumSource(org.slf4j.event.Level.class)
        void shouldUseSpecifiedLogLevel(org.slf4j.event.Level level) {
            var levelName = level.name();
            var logbackLevel = Level.valueOf(levelName);
            getLogbackLogger().setLevel(logbackLevel);

            var levelHandler = new SoapLoggingHandler(level);
            mockContextInteractionsWithOutboundProperty(true);

            levelHandler.handleMessage(context);

            assertThat(logAppender.list)
                    .hasSize(1)
                    .first()
                    .satisfies(event ->
                        assertThat(event.getLevel()).hasToString(levelName));
        }

        @Test
        void shouldRejectNullLogLevel() {
            assertThatThrownBy(() -> new SoapLoggingHandler(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class HandleMessage {

        @Test
        void shouldReturnTrue() {
            mockContextInteractionsWithOutboundProperty(false);

            assertThat(handler.handleMessage(context)).isTrue();

            verifyContextInteractions();
        }

        @Test
        void shouldNotSerializeMessageWhenLogLevelIsDisabled() {
            getLogbackLogger().setLevel(Level.INFO);

            assertThat(handler.handleMessage(context)).isTrue();

            // handler logs at DEBUG, logger is set to INFO, so the level check should short-circuit and not log
            assertThat(logAppender.list).isEmpty();
            
            verifyNoInteractions(context);
        }

        @Test
        void shouldLogOutbound() {
            mockContextInteractionsWithOutboundProperty(true);

            handler.handleMessage(context);

            assertThat(logAppender.list)
                    .hasSize(1)
                    .first()
                    .satisfies(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                        assertThat(event.getFormattedMessage()).contains("OUTBOUND");
                    });
            
            verifyContextInteractions();
        }

        @Test
        void shouldLogInbound() {
            mockContextInteractionsWithOutboundProperty(false);

            handler.handleMessage(context);

            assertThat(logAppender.list)
                    .hasSize(1)
                    .first()
                    .satisfies(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                        assertThat(event.getFormattedMessage()).contains("INBOUND");
                    });

            verifyContextInteractions();
        }

        @Test
        void shouldLogSoapMessageContent() {
            mockContextInteractionsWithOutboundProperty(true);

            handler.handleMessage(context);

            assertThat(logAppender.list)
                    .hasSize(1)
                    .first()
                    .satisfies(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                        assertThat(event.getFormattedMessage())
                                .contains("Envelope")
                                .contains("Header")
                                .contains("Body");
                    });

            verifyContextInteractions();
        }

        @Test
        void shouldNotLetExceptionsEscape() throws SOAPException, IOException {
            when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);

            var message = mock(SOAPMessage.class);
            when(context.getMessage()).thenReturn(message);

            doThrow(new IOException("oops"))
                    .when(message)
                    .writeTo(any(OutputStream.class));

            assertThat(handler.handleMessage(context)).isTrue();

            assertThat(logAppender.list)
                    .hasSize(1)
                    .first()
                    .satisfies(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.WARN);
                        assertThat(event.getFormattedMessage()).isEqualTo("Failed to log SOAP message");
                        assertThat(event.getThrowableProxy()).isNotNull();
                        assertThat(event.getThrowableProxy().getMessage()).isEqualTo("oops");
                    });

            verifyContextInteractions();
        }
    }

    @Nested
    class HandleFault {

        @Test
        void shouldReturnTrue() {
            mockContextInteractionsWithOutboundProperty(false);

            assertThat(handler.handleFault(context)).isTrue();

            verifyContextInteractions();
        }

        @Test
        void shouldLogFault() {
            mockContextInteractionsWithOutboundProperty(false);

            handler.handleFault(context);

            assertThat(logAppender.list)
                    .hasSize(1)
                    .first()
                    .satisfies(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                        assertThat(event.getFormattedMessage())
                                .contains("INBOUND")
                                .contains("Envelope")
                                .contains("Body");
                    });

            verifyContextInteractions();
        }
    }

    private void mockContextInteractionsWithOutboundProperty(boolean isOutbound) {
        when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(isOutbound);
        when(context.getMessage()).thenReturn(newSoapMessage());
    }

    private void verifyContextInteractions() {
        verify(context).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        verify(context).getMessage();
        verifyNoMoreInteractions(context);
    }

    @Nested
    class GetHeaders {

        @Test
        void shouldReturnEmptySet() {
            assertThat(handler.getHeaders()).isEmpty();
        }
    }

    @Nested
    class Close {

        @Test
        void shouldBeNoOp() {
            assertThatCode(() -> handler.close(context)).doesNotThrowAnyException();

            verifyNoInteractions(context);
        }
    }

    @SneakyThrows
    private static SOAPMessage newSoapMessage() {
        return MessageFactory.newInstance().createMessage(); 
    }
}
