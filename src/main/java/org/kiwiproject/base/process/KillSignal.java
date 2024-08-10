package org.kiwiproject.base.process;

import static java.util.Objects.requireNonNull;

/**
 * A few common (Unix/Linux/etc) kill signals.
 */
@SuppressWarnings("unused")
public enum KillSignal {

    /**
     * Hang up signal, e.g. {@code kill -1}
     */
    SIGHUP(1),

    /**
     * Interrupt signal, e.g. {@code kill -2}
     */
    SIGINT(2),

    /**
     * Quit signal, e.g. {@code kill -3}
     */
    SIGQUIT(3),

    /**
     * Non-catchable, non-ignorable kill signal, e.g. {@code kill -9}
     */
    SIGKILL(9),

    /**
     * Software termination signal, e.g. {@code kill -15}
     */
    SIGTERM(15);

    private static final char DASH = '-';

    public final int signalNumber;

    KillSignal(int number) {
        this.signalNumber = number;
    }

    /**
     * @return the number associated with this signal.
     */
    public String number() {
        return String.valueOf(signalNumber);
    }

    /**
     * Given a signal, prepend a leading dash if necessary, e.g., change "9" into "-9".
     *
     * @param signal the signal to modify
     * @return the possibly modified signal with a leading dash
     */
    public static String withLeadingDash(String signal) {
        requireNonNull(signal);
        if (signal.charAt(0) == DASH) {
            return signal;
        }
        return DASH + signal;
    }

    /**
     * Return this signal's number with a leading dash, e.g. "-3".
     *
     * @return this instance of signal's number with a leading dash
     */
    public String withLeadingDash() {
        return withLeadingDash(this.number());
    }

}
