package org.kiwiproject.base.process;

import static java.util.Objects.requireNonNull;

public enum UnixKillSignal {

    SIGHUP(1),
    SIGINT(2),
    SIGQUIT(3),
    SIGKILL(9),
    SIGTERM(15);

    private static final char DASH = '-';

    public final int signalNumber;

    UnixKillSignal(int number) {
        this.signalNumber = number;
    }

    public String number() {
        return String.valueOf(signalNumber);
    }

    public static String withLeadingDash(String signal) {
        requireNonNull(signal);
        if (signal.charAt(0) == DASH) {
            return signal;
        }
        return DASH + signal;
    }

    public String withLeadingDash() {
        return withLeadingDash(this.number());
    }

}
