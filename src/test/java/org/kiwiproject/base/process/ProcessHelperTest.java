package org.kiwiproject.base.process;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.nth;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;
import static org.kiwiproject.io.KiwiIO.emptyByteArrayInputStream;
import static org.kiwiproject.io.KiwiIO.newByteArrayInputStreamOfLines;
import static org.kiwiproject.io.KiwiIO.readLinesFromInputStreamOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.StandardSystemProperty;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@DisplayName("ProcessHelper")
class ProcessHelperTest {

    private ProcessHelper processes;
    private String osUser;

    @BeforeEach
    void setUp() {
        osUser = System.getProperty(StandardSystemProperty.USER_NAME.key());
        processes = new ProcessHelper();
    }

    @Test
    void testProcessStarts_AndReturnsData() {
        var process = processes.launch("echo", "hello world");
        var lines = readLinesFromInputStreamOf(process);

        assertThat(process.pid()).isPositive();
        assertThat(lines).containsExactly("hello world");
    }

    @Test
    void testWaitForExit_WithDefaultTimeout() {
        var process = processes.launch("date");
        Optional<Integer> exitCode = processes.waitForExit(process);

        assertThat(exitCode).contains(0);
    }

    @Test
    void testWaitForExit_WithExplicitTimeout() {
        var process = processes.launch("sleep", "1");
        Optional<Integer> exitCode = processes.waitForExit(process, 25, TimeUnit.MILLISECONDS);

        assertThat(exitCode).isEmpty();
    }

    @Test
    void testWaitForExit_WhenInterruptedExceptionThrown() throws InterruptedException {
        var process = mock(Process.class);
        when(process.waitFor(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException("sorry"));

        var timeout = 2L;
        var timeUnit = TimeUnit.SECONDS;
        assertThat(processes.waitForExit(process, timeout, timeUnit)).isEmpty();

        verify(process).waitFor(timeout, timeUnit);
    }

    @Test
    void testLaunch_UsingVarargs() {
        var process = processes.launch("sleep", "11");

        assertProcessAlive(process);
    }

    @Test
    void testLaunch_UsingList() {
        var process = processes.launch(List.of("sleep", "11"));

        assertProcessAlive(process);
    }

    @Test
    void testLaunch_WithBadCommand() {
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> processes.launch("bad", "arguments"))
                .withCauseExactlyInstanceOf(IOException.class)
                .withMessageContaining("Error launching command")
                .withMessageContaining("bad")
                .withMessageContaining("arguments");
    }

    private void assertProcessAlive(Process process) {
        try {
            assertThat(process.isAlive()).isTrue();
        } finally {
            if (nonNull(process)) {
                long pid = process.pid();
                processes.kill(pid, KillSignal.SIGTERM, KillTimeoutAction.NO_OP);
            }
        }
    }

    @Test
    void testPgrep_ForAllUsers() {
        List<Long> pids = List.of(
                createSleepingProcess(),
                createSleepingProcess(),
                createSleepingProcess()
        );

        runTestWithPids(pids, pids1 -> {
            List<Long> foundPids = processes.pgrep("sleep 55");

            assertThat(foundPids).containsExactly(
                    first(pids),
                    second(pids),
                    third(pids)
            );
        });
    }

    @Test
    void testPgrep_ForSpecificUser() {
        List<Long> pids = List.of(
                createSleepingProcess(),
                createSleepingProcess(),
                createSleepingProcess(),
                createSleepingProcess()
        );

        runTestWithPids(pids, pids1 -> {
            List<Long> foundPids = processes.pgrep(osUser, "sleep 55");

            assertThat(foundPids).containsExactly(
                    first(pids),
                    second(pids),
                    third(pids),
                    nth(pids, 4)
            );
        });
    }

    @Test
    void testPgrepWithSingleResult_WhenOnlySingleResult() {
        List<Long> pids = List.of(createSleepingProcess());

        runTestWithPids(pids, pids1 -> {
            Optional<Long> foundPid = processes.pgrepWithSingleResult(osUser, "sleep 55");

            assertThat(foundPid).contains(first(pids));
        });
    }

    @Test
    void testPgrepWithSingleResult_WhenNoResultFound() {
        Optional<Long> foundPid = processes.pgrepWithSingleResult("foo bar baz");

        assertThat(foundPid).isEmpty();
    }

    @Test
    void testPgrepWithSingleResult_WhenMultipleResults() {
        List<Long> pids = List.of(
                createSleepingProcess(),
                createSleepingProcess()
        );

        runTestWithPids(pids, pids1 -> assertThatThrownBy(() -> processes.pgrepWithSingleResult("sleep 55"))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expecting exactly one result pid for command [sleep 55], but received 2")
                .hasMessageContaining(List.of(first(pids), second(pids)).toString()));
    }

    @Test
    void testPgrepList_ForAllUsers() {
        List<Long> pids = List.of(
                createSleepingProcess("33"),
                createSleepingProcess("44"),
                createSleepingProcess("55"),
                createSleepingProcess("66")
        );

        runTestWithPids(pids, pids1 -> {
            List<String> results = processes.pgrepList("sleep");

            assertThat(results).contains(
                    pgrepLine(first(pids), "sleep 33"),
                    pgrepLine(second(pids), "sleep 44"),
                    pgrepLine(third(pids), "sleep 55"),
                    pgrepLine(nth(pids, 4), "sleep 66")
            );
        });
    }

    @Test
    void testPgrepList_ForSpecificUser() {
        List<Long> pids = List.of(
                createSleepingProcess("33"),
                createSleepingProcess("44")
        );

        runTestWithPids(pids, pids1 -> {
            List<String> results = processes.pgrepList(osUser, "sleep");

            assertThat(results).contains(
                    pgrepLine(first(pids), "sleep 33"),
                    pgrepLine(second(pids), "sleep 44")
            );
        });
    }

    @Test
    void testPgrepParsedList_ForAllUsers() {
        List<Long> pids = List.of(
                createSleepingProcess("33"),
                createSleepingProcess("44"),
                createSleepingProcess("55")
        );

        runTestWithPids(pids, pids1 -> {
            List<Pair<Long, String>> results = processes.pgrepParsedList("sleep");

            assertThat(results).contains(
                    Pair.of(first(pids), "sleep 33"),
                    Pair.of(second(pids), "sleep 44"),
                    Pair.of(third(pids), "sleep 55")
            );
        });
    }

    @Test
    void testPgrepParsedList_ForSpecificUser() {
        List<Long> pids = List.of(
                createSleepingProcess("33"),
                createSleepingProcess("44")
        );

        runTestWithPids(pids, pids1 -> {
            List<Pair<Long, String>> results = processes.pgrepParsedList(osUser, "sleep");

            assertThat(results).contains(
                    Pair.of(first(pids), "sleep 33"),
                    Pair.of(second(pids), "sleep 44")
            );
        });
    }

    private void runTestWithPids(List<Long> pids, Consumer<List<Long>> consumer) {
        try {
            consumer.accept(pids);
        } finally {
            pids.forEach(pid -> processes.kill(pid, KillSignal.SIGTERM, KillTimeoutAction.NO_OP));
        }
    }

    private String pgrepLine(long pid, String commandLine) {
        return pid + " " + commandLine;
    }

    private long createSleepingProcess() {
        return createSleepingProcess("55");
    }

    private long createSleepingProcess(String seconds) {
        Process process;
        try {
            process = new ProcessBuilder("sleep", seconds).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return process.pid();
    }

    @Test
    void testKill_WithExplicitTimeout() throws IOException {
        var process = new ProcessBuilder("sleep", "55").start();
        assertThat(process.isAlive()).isTrue();

        int exitCode = processes.kill(
                process.pid(),
                KillSignal.SIGTERM,
                1, TimeUnit.SECONDS,
                KillTimeoutAction.THROW_EXCEPTION
        );

        assertThat(exitCode).isZero();
    }

    @Test
    void testKill_WithStringSignal_AndExplicitTimeout() throws IOException {
        var process = new ProcessBuilder("sleep", "55").start();
        assertThat(process.isAlive()).isTrue();

        int exitCode = processes.kill(
                process.pid(),
                "SIGTERM",
                1, TimeUnit.SECONDS,
                KillTimeoutAction.THROW_EXCEPTION
        );

        assertThat(exitCode).isZero();
    }

    @Test
    void testKill_WithDefaultTimeout() throws IOException {
        var process = new ProcessBuilder("sleep", "55").start();
        assertThat(process.isAlive()).isTrue();

        int exitCode = processes.kill(
                process.pid(),
                KillSignal.SIGTERM,
                KillTimeoutAction.THROW_EXCEPTION
        );

        assertThat(exitCode).isZero();
    }

    @Test
    void testKill_WithStringSignal_AndDefaultTimeout() throws IOException {
        var process = new ProcessBuilder("sleep", "55").start();
        assertThat(process.isAlive()).isTrue();

        int exitCode = processes.kill(
                process.pid(),
                "15",  // SIGTERM
                KillTimeoutAction.THROW_EXCEPTION
        );

        assertThat(exitCode).isZero();
    }

    @Test
    void testKillForcibly() throws IOException, InterruptedException {
        var process = new ProcessBuilder("sleep", "55").start();
        assertThat(process.isAlive()).isTrue();

        boolean killedBeforeTimeout = processes.killForcibly(process, 2500, TimeUnit.MILLISECONDS);

        assertThat(killedBeforeTimeout).isTrue();
    }

    @Test
    void testFindChildProcessId_WhenNoChildFound() {
        Optional<Long> childProcessId = processes.findChildProcessId(-1L);

        assertThat(childProcessId).isEmpty();
    }

    @Test
    void testFindChildProcessIdInternal_WhenNoProcessIdsFound() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(emptyByteArrayInputStream());

        assertThat(processes.findChildProcessIdInternal(parentProcessId, mockProcesses)).isEmpty();
    }

    @Test
    void testFindChildProcessIdInternal_WhenExactlyOneProcessIdFound() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(newByteArrayInputStreamOfLines("12345"));

        assertThat(processes.findChildProcessIdInternal(parentProcessId, mockProcesses)).contains(12345L);
    }

    @Test
    void testFindChildProcessIdInternal_WhenMoreThanOneProcessIdFound() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(newByteArrayInputStreamOfLines("12345", "67890"));

        assertThatIllegalStateException()
                .isThrownBy(() -> processes.findChildProcessIdInternal(parentProcessId, mockProcesses))
                .withMessage("More than one child process found for process ID %d", parentProcessId);
    }

    @Test
    void testFindChildProcessIdInternal_WhenIOExceptionThrown() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(new ThrowingInputStream("oops"));

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> processes.findChildProcessIdInternal(parentProcessId, mockProcesses))
                .withCauseExactlyInstanceOf(IOException.class)
                .withMessageContaining("oops");
    }

    @Test
    void testFindChildProcessIds_WhenNoneFound() {
        Collection<Long> childProcessIds = processes.findChildProcessIds(-1L);

        assertThat(childProcessIds).isEmpty();
    }

    @Test
    void testFindChildProcessIdsInternal_WhenNoChildProcessIdsFound() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(emptyByteArrayInputStream());

        assertThat(processes.findChildProcessIdsInternal(parentProcessId, mockProcesses)).isEmpty();
    }

    @Test
    void testFindChildProcessIdsInternal_WhenExactlyOneChildProcessIdFound() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(newByteArrayInputStreamOfLines("12345"));

        assertThat(processes.findChildProcessIdsInternal(parentProcessId, mockProcesses)).containsOnly(12345L);
    }

    @Test
    void testFindChildProcessIdsInternal_WhenMoreThanOneChildProcessIdFound() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(newByteArrayInputStreamOfLines("12345", "67890", "34567"));

        assertThat(processes.findChildProcessIdsInternal(parentProcessId, mockProcesses))
                .containsOnly(12345L, 67890L, 34567L);
    }

    @Test
    void testFindChildProcessIdsInternal_WhenIOExceptionThrown() {
        long parentProcessId = 42042L;
        var mockProcesses = mock(ProcessHelper.class);
        var mockProcess = mock(Process.class);

        when(mockProcesses.launch("pgrep", "-P", String.valueOf(parentProcessId))).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(new ThrowingInputStream("oopsy daisy"));

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> processes.findChildProcessIdsInternal(parentProcessId, mockProcesses))
                .withCauseExactlyInstanceOf(IOException.class)
                .withMessageContaining("oopsy daisy");
    }

    private static class ThrowingInputStream extends InputStream {

        private final String message;

        private ThrowingInputStream(String message) {
            this.message = message;
        }

        @Override
        public int read() throws IOException {
            throw new IOException(message);
        }
    }
}