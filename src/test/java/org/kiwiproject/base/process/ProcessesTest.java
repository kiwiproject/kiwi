package org.kiwiproject.base.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

@DisplayName("Processes")
class ProcessesTest {

    @BeforeEach
    void setUp() {
        assumeTrue(SystemUtils.IS_OS_UNIX, "This test should only run on UNIX or UNIX-like systems");
    }

    @Test
    void testGetPgrepFlags() {
        assertThat(Processes.getPgrepFlags()).isIn(List.of("-fa", "-fl"));
    }

    @Test
    void testWasPgrepCheckSuccessful() {
        assertThat(Processes.wasPgrepFlagsCheckSuccessful())
                .describedAs("We expect this to always be true...")
                .isTrue();
    }

    @Nested
    class ChoosePgrepFlags {

        @Test
        void shouldIndicateSuccess_WhenNonNullFlags() {
            var flags = "-fl";
            var result = Processes.choosePgrepFlags(flags);
            assertThat(result.getLeft()).isEqualTo(flags);
            assertThat(result.getRight()).isTrue();
        }

        @Test
        void shouldIndicateUnsuccessful_WhenNullFlags() {
            var result = Processes.choosePgrepFlags(null);
            assertThat(result.getLeft())
                    .describedAs("default should be -fa")
                    .isEqualTo("-fa");
            assertThat(result.getRight())
                    .describedAs("should indicate not successful")
                    .isFalse();
        }
    }

    @RepeatedTest(5)
    void testLogPgrepFlagWarnings() {
        assertThatCode(Processes::logPgrepFlagWarnings).doesNotThrowAnyException();
    }

    @Test
    void testLogPgrepCheckInfo() {
        assertThatCode(() -> Processes.logPgrepCheckInfo(
                "-fl",
                "12345",
                List.of(),
                List.of("PEBKAC (problem exists between keyboard and chair)", "User error"),
                "doit -foo -bar -baz")).doesNotThrowAnyException();
    }

    @Test
    void testProcessId() {
        var process = Processes.launch("sleep", "1");

        assertThat(Processes.processId(process)).isEqualTo(process.pid());
    }

    @Test
    void testKillInternal_WhenProcessIsKilledCleanlyBeforeTimeout() throws InterruptedException {
        var pid = 2970L;
        var process = mock(Process.class);
        var timeout = 5L;
        var unit = TimeUnit.SECONDS;

        when(process.waitFor(timeout, unit)).thenReturn(true);
        when(process.exitValue()).thenReturn(129);

        int exitCode = Processes.killInternal(pid, process, timeout, unit, KillTimeoutAction.FORCE_KILL);
        assertThat(exitCode).isEqualTo(129);
    }

    @Test
    void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndNoOpAction() throws InterruptedException {
        var pid = 2970L;
        var process = mock(Process.class);
        var timeout = 5L;
        var unit = TimeUnit.SECONDS;

        when(process.waitFor(timeout, unit)).thenReturn(false);

        int exitCode = Processes.killInternal(pid, process, timeout, unit, KillTimeoutAction.NO_OP);
        assertThat(exitCode).isEqualTo(-1);

        verify(process, never()).exitValue();
    }

    @Test
    void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndThrowExceptionAction() throws InterruptedException {
        var pid = 2970L;
        var process = mock(Process.class);
        var timeout = 5L;
        var unit = TimeUnit.SECONDS;

        when(process.waitFor(timeout, unit)).thenReturn(false);

        assertThatThrownBy(() ->
                Processes.killInternal(pid, process, timeout, unit, KillTimeoutAction.THROW_EXCEPTION))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Process 2970 did not end before timeout");

        verify(process, never()).exitValue();
    }

    @Test
    void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndForceKillActionCleanlyKills() throws InterruptedException {
        var pid = 2970L;
        var process = mock(Process.class);
        var timeout = 5L;
        var unit = TimeUnit.SECONDS;

        when(process.waitFor(timeout, unit)).thenReturn(false);
        when(process.destroyForcibly()).thenReturn(process);
        when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(true);
        when(process.exitValue()).thenReturn(137);

        int exitCode = Processes.killInternal(pid, process, timeout, unit, KillTimeoutAction.FORCE_KILL);
        assertThat(exitCode).isEqualTo(137);
    }

    @Test
    void testKillInternal_WhenProcessIsNotKilledBeforeTimeout_AndForceKillActionTimesOut() throws InterruptedException {
        var pid = 2970L;
        var process = mock(Process.class);
        var timeout = 5L;
        var unit = TimeUnit.SECONDS;

        when(process.waitFor(timeout, unit)).thenReturn(false);
        when(process.destroyForcibly()).thenReturn(process);
        when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() ->
                Processes.killInternal(pid, process, timeout, unit, KillTimeoutAction.FORCE_KILL))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Process 2970 was not killed before 1 second timeout expired");

        verify(process, never()).exitValue();
    }
}