package org.kiwiproject.base.process;

import com.google.common.io.LineReader;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assume.assumeTrue;

public class UnixProcessHelperTest {

    private UnixProcessHelper processes;

    @Before
    public void setUp() {
        assumeTrue("This test should only run on UNIX-like operating systems", SystemUtils.IS_OS_UNIX);
        processes = new UnixProcessHelper();
    }

    @Test
    public void testIsUNIXProcess_WhenShouldBeTrue() throws IOException {
        Process proc = new ProcessBuilder("echo", "hello processes").start();
        assertThat(processes.isUNIXProcess(proc)).isTrue();
        LineReader reader = new LineReader(new InputStreamReader(proc.getInputStream()));
        assertThat(reader.readLine()).isEqualTo("hello processes");
    }

    @Test
    public void testIsUNIXProcess_WhenShouldBeFalse() throws IOException {
        Process proc = new PidProcess(8765);
        assertThat(processes.isUNIXProcess(proc)).isFalse();
    }

    @Test
    public void testCanGetProcessId_WhenThereIsPidField_ShouldBeTrue() {
        Process proc = new PidProcess(42);
        assertThat(processes.canGetProcessId(proc)).isTrue();
    }

    @Test
    public void testCanGetProcessId_WhenThereIsNoPidField_ShouldBeFalse() {
        Process proc = new NoPidProcess();
        assertThat(processes.canGetProcessId(proc)).isFalse();
    }

    @Test
    public void testProcessId_WhenIsUNIXProcess() throws IOException {
        Process proc = new ProcessBuilder("pwd").start();
        int pid = processes.processId(proc);
        assertThat(pid).isGreaterThan(0);
    }

    @Test
    public void testProcessId_WhenCannotGetThePid() {
        Process proc = new NoPidProcess();
        assertThatThrownBy(() -> processes.processId(proc))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasNoCause()
                .hasMessageContaining("Error getting pid of process")
                .hasMessageContaining(proc.toString())
                .hasMessageContaining("Is it a java.lang.UNIXProcess with a pid field that is accessible via reflection?");
    }

    @Test
    public void testKill_WithDefaultTimeout() {
        Process process = createSleepProcessForTest();
        assertThat(process.isAlive()).isTrue();

        int processId = UnixProcesses.processId(process);
        int exitCode = processes.kill(processId, UnixKillSignal.SIGTERM, KillTimeoutAction.THROW_EXCEPTION);
        assertThat(exitCode).isZero();
    }

    @Test
    public void testKill_WithStringSignal_AndDefaultTimeout() {
        Process process = createSleepProcessForTest();
        assertThat(process.isAlive()).isTrue();

        int processId = UnixProcesses.processId(process);
        int exitCode = processes.kill(processId, UnixKillSignal.SIGTERM.number(), KillTimeoutAction.THROW_EXCEPTION);
        assertThat(exitCode).isZero();
    }

    @Test
    public void testKill_WithExplicitTimeout() {
        Process process = createSleepProcessForTest();
        assertThat(process.isAlive()).isTrue();

        int processId = UnixProcesses.processId(process);
        int exitCode = processes.kill(processId, UnixKillSignal.SIGTERM, 1, TimeUnit.SECONDS, KillTimeoutAction.THROW_EXCEPTION);
        assertThat(exitCode).isZero();
    }

    @Test
    public void testKill_WithExplicitTimeout_OfZero() {
        Process process = createSleepProcessForTest();
        assertThat(process.isAlive()).isTrue();

        int processId = UnixProcesses.processId(process);
        assertThatThrownBy(() -> processes.kill(processId, UnixKillSignal.SIGTERM, 0, TimeUnit.MILLISECONDS, KillTimeoutAction.THROW_EXCEPTION))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Process " + processId)
                .hasMessageContaining("did not end before timeout");
    }

    @Test
    public void testKill_WithStringSignal_AndExplicitTimeout() {
        Process process = createSleepProcessForTest();
        assertThat(process.isAlive()).isTrue();

        int processId = UnixProcesses.processId(process);
        int exitCode = processes.kill(processId, UnixKillSignal.SIGTERM.number(), 1, TimeUnit.SECONDS, KillTimeoutAction.THROW_EXCEPTION);
        assertThat(exitCode).isZero();
    }

    @Test
    public void testKill_WithStringSignal_AndExplicitTimeout_OfZero() {
        Process process = createSleepProcessForTest();
        assertThat(process.isAlive()).isTrue();

        int processId = UnixProcesses.processId(process);
        assertThatThrownBy(() -> processes.kill(processId, UnixKillSignal.SIGTERM.number(), 0, TimeUnit.SECONDS, KillTimeoutAction.THROW_EXCEPTION))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Process " + processId)
                .hasMessageContaining("did not end before timeout");

    }

    private Process createSleepProcessForTest() {
        return Processes.launch("sleep", "30");
    }

    abstract class AbstractProcess extends Process {

        @Override
        public OutputStream getOutputStream() {
            return null;
        }

        @Override
        public InputStream getInputStream() {
            return null;
        }

        @Override
        public InputStream getErrorStream() {
            return null;
        }

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }

    }

    class NoPidProcess extends AbstractProcess {
    }

    class PidProcess extends AbstractProcess {

        private final int pid;

        PidProcess(int pid) {
            this.pid = pid;
        }
    }

    class NonIntPidProcess extends AbstractProcess {

        private final String pid;

        NonIntPidProcess(String pid) {
            this.pid = pid;
        }
    }


}