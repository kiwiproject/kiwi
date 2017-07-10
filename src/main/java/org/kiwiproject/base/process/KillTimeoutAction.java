package org.kiwiproject.base.process;

import org.kiwiproject.base.UncheckedInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static org.kiwiproject.base.KiwiStrings.format;

public enum KillTimeoutAction {

    FORCE_KILL(KillTimeoutAction::forceKill),
    NO_OP(KillTimeoutAction::noOp),
    THROW_EXCEPTION(KillTimeoutAction::throwException);

    private static final Logger LOG = LoggerFactory.getLogger(KillTimeoutAction.class);
    private final BiFunction<Process, Integer, Integer> executionFunction;

    KillTimeoutAction(BiFunction<Process, Integer, Integer> executionFunction) {
        this.executionFunction = executionFunction;
    }

    public int executeOn(Process process, int processId) {
        return executionFunction.apply(process, processId);
    }

    private static int forceKill(Process process, int processId) {
        boolean killedBeforeTimeout = forceKill(process);
        if (!killedBeforeTimeout) {
            throw new IllegalStateException(
                    format("Process {} was not forcibly killed within 1 second", processId));
        }
        return process.exitValue();
    }

    private static boolean forceKill(Process process) {
        boolean killedBeforeTimeout;
        try {
            killedBeforeTimeout = Processes.killForcibly(process, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedInterruptedException(e);
        }
        return killedBeforeTimeout;
    }

    private static int noOp(Process process, int processId) {
        LOG.warn("Process {} ( {} ) did not end before timeout", processId, process);
        return 0;
    }

    private static int throwException(Process process, int processId) {
        throw new IllegalStateException(format("Process {} ( {} ) did not end before timeout", processId, process));
    }

}
