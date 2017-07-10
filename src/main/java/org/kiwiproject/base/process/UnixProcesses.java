package org.kiwiproject.base.process;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.base.UncheckedInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Static utilities for working with processes on UNIX or UNIX-like systems.
 */
@UtilityClass
public class UnixProcesses {

    private static final Logger LOG = LoggerFactory.getLogger(UnixProcesses.class);

    public static final String UNIX_PROCESS_PID_FIELD_NAME = "pid";

    public static final String UNIX_PROCESS_CLASS_NAME = "java.lang.UNIXProcess";

    public static final int DEFAULT_KILL_TIMEOUT_SECONDS = 5;

    @SuppressWarnings("squid:S1872")  // SONAR: Cannot use instanceof on package-private class in java.lang
    public static boolean isUNIXProcess(Process process) {
        requireNonNull(process);
        return process.getClass().getName().equals(UNIX_PROCESS_CLASS_NAME);
    }

    public static boolean canGetProcessId(Process process) {
        requireNonNull(process);
        try {
            Field pidField = process.getClass().getDeclaredField(UNIX_PROCESS_PID_FIELD_NAME);
            pidField.setAccessible(true);
            return true;
        } catch (NoSuchFieldException e) {
            LOG.trace("Field [{}] does not exist in process {}", UNIX_PROCESS_PID_FIELD_NAME, process, e);
            return false;
        } catch (SecurityException e) {
            LOG.trace("Field [{}' cannot be set accessible on process {}", UNIX_PROCESS_PID_FIELD_NAME, process, e);
            return false;
        }
    }

    public static int processId(Process process) {
        requireNonNull(process);

        if (isUNIXProcess(process) && canGetProcessId(process)) {
            Object pidValue = getPidFieldValue(process);
            return tryParsePidToInt(pidValue, process);
        }

        String message = KiwiStrings.format(
                "Error getting pid of process {}. Is it a {} with a {} field that is accessible via reflection?",
                process, UNIX_PROCESS_CLASS_NAME, UNIX_PROCESS_PID_FIELD_NAME);
        throw new IllegalStateException(message);
    }

    private static Object getPidFieldValue(Process process) {
        try {
            Field pidField = process.getClass().getDeclaredField(UNIX_PROCESS_PID_FIELD_NAME);
            pidField.setAccessible(true);
            return pidField.get(process);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error getting pid field value", e);
        }
    }

    private static int tryParsePidToInt(Object pidValue, Process proc) {
        try {
            return Integer.parseInt(pidValue.toString());
        } catch (NumberFormatException e) {
            String message = KiwiStrings.format("Error parsing pid value [{}] of process {} as an int", pidValue, proc);
            throw new IllegalStateException(message, e);
        }
    }

    public static int kill(int processId, UnixKillSignal signal, KillTimeoutAction action) {
        return kill(processId, signal.number(), action);
    }

    public static int kill(int processId, UnixKillSignal signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return kill(processId, signal.number(), timeout, unit, action);
    }

    public static int kill(int processId, String signal, KillTimeoutAction action) {
        return kill(processId, signal, DEFAULT_KILL_TIMEOUT_SECONDS, TimeUnit.SECONDS, action);
    }

    public static int kill(int processId, String signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        Process killerProcess = Processes.launch("kill", UnixKillSignal.withLeadingDash(signal), String.valueOf(processId));
        return killInternal(processId, killerProcess, timeout, unit, action);
    }

    @VisibleForTesting
    static int killInternal(int processId,
                            Process killerProcess,
                            long timeout,
                            TimeUnit unit,
                            KillTimeoutAction action) {
        try {
            boolean exitedBeforeWaitTimeout = killerProcess.waitFor(timeout, unit);
            if (exitedBeforeWaitTimeout) {
                return killerProcess.exitValue();
            }
            return action.executeOn(killerProcess, processId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedInterruptedException(e);
        }
    }

}
