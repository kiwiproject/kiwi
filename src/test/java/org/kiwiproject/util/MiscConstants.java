package org.kiwiproject.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MiscConstants {

    public static final String POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING =
            "If these tests are failing, there is a good chance something (e.g. a Dropwizard ResourceExtension)" +
                    " has modified the loggers so that the level is not effectively DEBUG (e.g. an appender filter)." +
                    " We know ResourceExtension (and other Dropwizard JUnit extensions too) reset logging which" +
                    " is not only annoying, but it can cause problems. Check any/all Dropwizard extensions, and make" +
                    " sure to call bootstrapLogging(false) on any ResourceExtension instances. If any tests are using" +
                    " DropwizardAppExtension, the Configuration class for the Application being tested should override" +
                    " getLoggingFactory() and return an ExternalLoggingFactory, which will prevent Dropwizard from" +
                    " doing anything with the logging configuration.";
}
