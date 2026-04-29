package org.kiwiproject.jaxws;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.event.Level;

/**
 * Configuration class intended to be used by applications that want to make SOAP logging configurable.
 */
@Getter
@Setter
public class SoapLoggingConfig {

    /**
     * Should SOAP envelopes be logged?
     */
    private boolean enabled;

    /**
     * If SOAP logging is enabled, what log level should be used?
     */
    private Level logLevel = Level.DEBUG;
}
