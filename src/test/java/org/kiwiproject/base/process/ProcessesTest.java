package org.kiwiproject.base.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Nested
    class ProcessIdOrEmpty {

        @Test
        void shouldReturnOptionalContainingPid_WhenPidIsAvailable() {
            var pid = 12345L;
            var process = mock(Process.class);
            when(process.pid()).thenReturn(pid);

            assertThat(Processes.processIdOrEmpty(process)).hasValue(pid);
        }

        @Test
        void shouldReturnEmptyOptionalContainingPid_WhenProcess_ThrowsUnsupportedOperationException() {
            var process = mock(Process.class);
            when(process.pid()).thenThrow(new UnsupportedOperationException("No pid for you!"));

            assertThat(Processes.processIdOrEmpty(process)).isEmpty();
        }
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

    @Nested
    class HasSuccessfulExitCode {

        @Test
        void shouldReturnTrueForZero() {
            var process = mock(Process.class);
            when(process.exitValue()).thenReturn(0);

            assertThat(Processes.hasSuccessfulExitCode(process)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 1, 2, 3, 127, 255})
        void shouldReturnFalseForNonZero(int code) {
            var process = mock(Process.class);
            when(process.exitValue()).thenReturn(code);

            assertThat(Processes.hasSuccessfulExitCode(process)).isFalse();
        }
    }

    @Nested
    class IsSuccessfulExitCode {

        @Test
        void shouldReturnTrueForZero() {
            assertThat(Processes.isSuccessfulExitCode(0)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 1, 2, 3, 127, 255})
        void shouldReturnFalseForNonzero(int code) {
            assertThat(Processes.isSuccessfulExitCode(code)).isFalse();
        }
    }

    @Nested
    class IsNonzeroExitCode {

        @Test
        void shouldReturnFalseForZero() {
            assertThat(Processes.isNonzeroExitCode(0)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 1, 2, 3, 127, 255})
        void shouldReturnTrueForNonzero(int code) {
            assertThat(Processes.isNonzeroExitCode(code)).isTrue();
        }
    }

    @Nested
    class GetPidOrThrow {

        @ParameterizedTest
        @ValueSource(strings = { "a", "", "foo", "12_000"})
        void shouldThrowIllegalArgument_WhenPidIsNotNumeric(String pidString) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Processes.getPidOrThrow(pidString));
        }
    }

    @Nested
    class Which {

        @ParameterizedTest
        @ValueSource(strings = {"cp", "ls", "mv"})
        void shouldFindProgramThatExists(String program) {
            assertThat(Processes.which(program)).hasValueSatisfying(value -> assertThat(value).endsWith(program));
        }

        @ParameterizedTest
        @ValueSource(strings = {"foobar", "abc-xyz", "clunkerate"})
        void shouldReturnEmptyOptional_WhenProgramDoesNotExistInPath(String program) {
            assertThat(Processes.which(program)).isEmpty();
        }
    }

}
