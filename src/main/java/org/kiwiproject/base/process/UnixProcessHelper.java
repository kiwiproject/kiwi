package org.kiwiproject.base.process;

import java.util.concurrent.TimeUnit;

/**
 * Helper class for working with processes on UNIX or UNIX-like systems. Use this instead of {@link UnixProcesses} if
 * you need to be able to easily mock UNIX process related things.
 */
public class UnixProcessHelper {

    public boolean isUNIXProcess(Process process) {
        return UnixProcesses.isUNIXProcess(process);
    }

    public boolean canGetProcessId(Process process) {
        return UnixProcesses.canGetProcessId(process);
    }

    public int processId(Process process) {
        return UnixProcesses.processId(process);
    }

    public int kill(int processId, UnixKillSignal signal, KillTimeoutAction action) {
        return UnixProcesses.kill(processId, signal, action);
    }

    public int kill(int processId, UnixKillSignal signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return UnixProcesses.kill(processId, signal, timeout, unit, action);
    }

    public int kill(int processId, String signal, KillTimeoutAction action) {
        return UnixProcesses.kill(processId, signal, action);
    }

    public int kill(int processId, String signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return UnixProcesses.kill(processId, signal, timeout, unit, action);
    }

}
