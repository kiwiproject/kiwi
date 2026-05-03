package org.kiwiproject.base.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kiwiproject.base.process.Processes;
import org.kiwiproject.base.system.ExecutionStrategies.SystemExitExecutionStrategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DisplayName("ExecutionStrategies")
@Slf4j
class ExecutionStrategiesTest {

    @Test
    void shouldBuildNoOpStrategy() {
        assertThat(ExecutionStrategies.noOp())
                .isExactlyInstanceOf(ExecutionStrategies.NoOpExecutionStrategy.class);
    }

    @Test
    void shouldBuildSystemExitStrategy() {
        var strategy = ExecutionStrategies.systemExit();
        assertThat(strategy).isExactlyInstanceOf(ExecutionStrategies.SystemExitExecutionStrategy.class);
        assertThat(strategy.getExitCode()).isEqualTo(1);
    }

    @Test
    void shouldBuildSystemExitStrategyWithExitCode() {
        var strategy = ExecutionStrategies.systemExit(42);
        assertThat(strategy).isExactlyInstanceOf(ExecutionStrategies.SystemExitExecutionStrategy.class);
        assertThat(strategy.getExitCode()).isEqualTo(42);
    }

    // This is silly, and is here to simply show that the return type change
    // doesn't affect existing methods that accepts ExecutionStrategy or code
    // that declares a field or variable as ExecutionStrategy, as 'var' using
    // LTVI, or explicitly as the exact type. Yes, this is "just Java" and
    // isn't really necessary, but being overly cautious anyway.
    @Test
    void shouldUseExitStrategyInMethodThatAcceptsExecutionStrategy() {
        ExecutionStrategy noOpStrategy = ExecutionStrategies.noOp();
        var exitFlaggingStrategy = ExecutionStrategies.exitFlagging();
        SystemExitExecutionStrategy systemExitStrategy = ExecutionStrategies.systemExit(130);

        assertAll(
            () -> assertThatCode(() -> acceptStrategy(noOpStrategy)).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(exitFlaggingStrategy)).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(systemExitStrategy)).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(ExecutionStrategies.noOp())).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(ExecutionStrategies.exitFlagging())).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(ExecutionStrategies.systemExit())).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(ExecutionStrategies.systemExit(127))).doesNotThrowAnyException()
        );
    }

    private static void acceptStrategy(ExecutionStrategy strategy) {
        LOG.info("Accepted strategy: {}", strategy);
    }

    @Nested
    class NoOpStrategy {

        @Test
        void shouldDoNothing() {
            var executionStrategy = ExecutionStrategies.noOp();
            assertThatCode(executionStrategy::exit).doesNotThrowAnyException();
        }
    }

    @Nested
    class ExitFlaggingStrategy {

        @Test
        void shouldFlagCallsToExit() {
            var executionStrategy = ExecutionStrategies.exitFlagging();
            assertThatCode(executionStrategy::exit).doesNotThrowAnyException();

            assertThat(executionStrategy).isExactlyInstanceOf(ExecutionStrategies.ExitFlaggingExecutionStrategy.class);
            assertThat(executionStrategy.didExit()).isTrue();
        }

        @Test
        void shouldNotFlagWhenExitNotCalled() {
            var exitFlaggingStrategy = ExecutionStrategies.exitFlagging();
            assertThat(exitFlaggingStrategy.didExit()).isFalse();
        }
    }

    @Nested
    class SystemExitStrategy {

        @ParameterizedTest
        @CsvSource({
                "1, 0",
                "143 , 0",
                "127, 25",
                "143, 50"
        })
        void shouldExitTheJVM(int exitCode, long exitWaitDelayMillis) throws IOException {
            var result = execTestApplication(exitCode, exitWaitDelayMillis);

            assertThat(result.pid).isPositive();
            assertThat(result.exitValue)
                    .describedAs("Expecting non-null exit code (null means timeout waiting for process to exit)")
                    .isEqualTo(exitCode);
        }
    }

    @Slf4j
    public static class TestApplication {
        public static void main(String[] args) {
            int exitCode = Integer.parseInt(args[0]);
            long exitWaitDelayMillis = Long.parseLong(args[1]);
            LOG.debug("Using exitCode: {} ; exitWaitDelayMillis: {}", exitCode, exitWaitDelayMillis);

            // pretend some unrecoverable error occurred...
            var executionStrategy = ExecutionStrategies.systemExit(exitCode);
            new SystemExecutioner(executionStrategy).exit(exitWaitDelayMillis, TimeUnit.MILLISECONDS);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static ExecResult execTestApplication(int exitCode, long exitWaitDelayMillis) throws IOException {
        var javaHome = System.getProperty("java.home");
        var javaBin = Path.of(javaHome, "bin", "java").toString();
        var classpath = System.getProperty("java.class.path");
        var className = TestApplication.class.getName();
        var command = List.of(javaBin,
                "-cp", classpath,
                className,
                String.valueOf(exitCode),
                String.valueOf(exitWaitDelayMillis));

        LOG.debug("Executing TestApplication using {} with exitCode {} and exitWaitDelayMillis {}",
                javaBin, exitCode, exitWaitDelayMillis);
        long start = System.nanoTime();
        var process = new ProcessBuilder(command).start();
        var exitValueOrNull = Processes.waitForExit(process, 5, TimeUnit.SECONDS).orElse(null);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        LOG.debug("After {} ms, received exit value {} for process {}", elapsedMillis, exitValueOrNull, process.pid());

        return new ExecResult(process.pid(), exitValueOrNull);
    }

    private record ExecResult(long pid, Integer exitValue) {
    }
}
