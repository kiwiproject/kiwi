package org.kiwiproject.base.process;

import java.util.concurrent.TimeUnit;

/**
 * Possible actions to take if a kill command times out.
 *
 * @see Processes#kill(long, KillSignal, long, TimeUnit, KillTimeoutAction)
 */
public enum KillTimeoutAction {

    /**
     * The process will be forced killed (e.g., like a {@code kill -9}) after timeout
     */
    FORCE_KILL,

    /**
     * No action is taken after timeout
     */
    NO_OP,

    /**
     * An exception ({@link IllegalStateException}) will be thrown after timeout
     */
    THROW_EXCEPTION
}
