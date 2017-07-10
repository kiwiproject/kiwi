package org.kiwiproject.base.process;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnixProcessesTest {

    private int processId;
    private Process process;
    private long timeout;
    private TimeUnit unit;

    @Before
    public void setUp() {
        processId = 4242;
        process = mock(Process.class);
        timeout = 2L;
        unit = TimeUnit.SECONDS;
    }

    @Test
    public void testKillInternal_WhenProcessIsKilledBeforeTimeout() throws InterruptedException {
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(process.exitValue()).thenReturn(127);

        int exitCode = UnixProcesses.killInternal(processId, process, timeout, unit, KillTimeoutAction.FORCE_KILL);
        assertThat(exitCode).isEqualTo(127);

        verify(process).waitFor(timeout, TimeUnit.SECONDS);
    }

    @Test
    public void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndActionIsForceKill_AndForceKillWorksAsExpected()
            throws InterruptedException {

        when(process.waitFor(timeout, unit)).thenReturn(false);
        when(process.destroyForcibly()).thenReturn(process);
        when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(true);
        when(process.exitValue()).thenReturn(137);

        int exitCode = UnixProcesses.killInternal(processId, process, timeout, unit, KillTimeoutAction.FORCE_KILL);
        assertThat(exitCode).isEqualTo(137);

        verify(process).waitFor(timeout, TimeUnit.SECONDS);
        verify(process).waitFor(1, TimeUnit.SECONDS);
    }

    @Test
    public void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndActionIsForceKill_AndForceKillTimesOut()
            throws InterruptedException {

        when(process.waitFor(timeout, unit)).thenReturn(false);
        when(process.destroyForcibly()).thenReturn(process);
        when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() -> UnixProcesses.killInternal(processId, process, timeout, unit, KillTimeoutAction.FORCE_KILL))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Process %d was not forcibly killed within 1 second", processId);

        verify(process, never()).exitValue();
    }

    @Test
    public void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndActionIsNoOp() throws InterruptedException {
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false);

        int exitCode = UnixProcesses.killInternal(processId, process, timeout, unit, KillTimeoutAction.NO_OP);
        assertThat(exitCode).isEqualTo(0);

        verify(process).waitFor(timeout, TimeUnit.SECONDS);
        verify(process, never()).exitValue();
    }

    @Test
    public void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndActionIsThrowException() throws InterruptedException {
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThatThrownBy(() -> UnixProcesses.killInternal(processId, process, timeout, unit, KillTimeoutAction.THROW_EXCEPTION))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Process " + processId)
                .hasMessageContaining("did not end before timeout");

        verify(process).waitFor(timeout, TimeUnit.SECONDS);
        verify(process, never()).exitValue();
    }

}