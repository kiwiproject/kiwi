package org.kiwiproject.jaxws;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import javax.xml.namespace.QName;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * A JAX-WS {@link SOAPHandler} that logs SOAP messages for inbound and outbound messages.
 * <p>
 * The log level is configurable; the default is {@link Level#DEBUG}. To use a different level,
 * use the {@link #SoapLoggingHandler(Level)} constructor.
 * <p>
 * If an error occurs while attempting to log a SOAP message, it is caught and logged at
 * {@code WARN} level so that message processing is never interrupted.
 * <p>
 * This handler can be used with {@link SoapLoggingConfig} to allow applications
 * to make SOAP logging configurable via their configuration file.
 * 
 * @implNote Logging the SOAP message requires serializing it via
 *     {@link jakarta.xml.soap.SOAPMessage#writeTo(java.io.OutputStream)}, which forces a full
 *     marshalling pass through the JAXB stack. This can have unexpected side effects, since
 *     during normal message processing the message is never written to a stream. Adding or
 *     removing this handler may surface or resolve latent bugs in the JAXB implementation;
 *     if behavior changes after modifying the handler chain, the JAXB implementation and its
 *     version should be the first thing to investigate.
 *     <p>
 *     For example, the Eclipse Implementation of JAXB introduced a regression in
 *     {@code UTF8XmlOutput} in version 4.0.7 that affected message serialization (see the
 *     <a href="https://github.com/eclipse-ee4j/jaxb-ri/releases/tag/4.0.7-RI">release notes</a>).
 */
@Slf4j
public class SoapLoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private final Level logLevel;

    /**
     * Creates a new {@code SoapLoggingHandler} that logs at {@link Level#DEBUG}.
     */
    public SoapLoggingHandler() {
        this(Level.DEBUG);
    }

    /**
     * Creates a new {@code SoapLoggingHandler} that logs at the specified level.
     *
     * @param logLevel the SLF4J {@link Level} at which SOAP messages should be logged
     * @throws IllegalArgumentException if {@code logLevel} is null
     */
    public SoapLoggingHandler(Level logLevel) {
        this.logLevel = requireNotNull(logLevel, "logLevel must not be null");
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    private void logMessage(SOAPMessageContext context) {
        // Avoid serializing the SOAP message if the logger does not have the level enabled
        if (!LOG.isEnabledForLevel(logLevel)) {
            return;
        }

        try {
            var isOutbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            var direction = Boolean.TRUE.equals(isOutbound) ? "OUTBOUND" : "INBOUND";

            var message = context.getMessage();
            var outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);

            var soapMessage = outputStream.toString(StandardCharsets.UTF_8);

            LOG.atLevel(logLevel)
                    .log("{} SOAP message: {}", direction, soapMessage);

        } catch (Exception e) {
            LOG.warn("Failed to log SOAP message", e);
        }
    }

    @Override
    public Set<QName> getHeaders() {
        return Set.of();
    }

    @Override
    public void close(MessageContext context) {
        // no-op
    }
}
