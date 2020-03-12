package org.kiwiproject.base.process;

import static java.util.Objects.requireNonNull;

/**
 * A few common (Unix/Linux/etc) kill signals.
 */
@SuppressWarnings("unused")
public enum KillSignal {

    SIGHUP(1),
    SIGINT(2),
    SIGQUIT(3),
    SIGKILL(9),
    SIGTERM(15);

    private static final char DASH = '-';

    public final int signalNumber;

    KillSignal(int number) {
        this.signalNumber = number;
    }

    /**
     * Return the number associated with this signal.
     */
    public String number() {
        return String.valueOf(signalNumber);
    }

    /**
     * Given a signal, prepend a leading dash if necessary, e.g. change "9" into "-9".
     */
    public static String withLeadingDash(String signal) {
        requireNonNull(signal);
        if (signal.charAt(0) == DASH) {
            return signal;
        }
        return DASH + signal;
    }

    /**
     * Return this signal 's number with a leading dash, e.g. "-3".
     */
    public String withLeadingDash() {
        return withLeadingDash(this.number());
    }

}
