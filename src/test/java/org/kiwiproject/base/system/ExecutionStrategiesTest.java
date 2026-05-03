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

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
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
    void shouldBuildExitFlaggingStrategy() {
        assertThat(ExecutionStrategies.exitFlagging())
                .isExactlyInstanceOf(ExecutionStrategies.ExitFlaggingExecutionStrategy.class);
    }

    @Test
    void shouldBuildSystemExitStrategy() {
        assertThat(ExecutionStrategies.systemExit())
                .isExactlyInstanceOf(ExecutionStrategies.SystemExitExecutionStrategy.class);
    }

    @Test
    void shouldUseExitStrategyInMethodThatAcceptsExecutionStrategy() {
        ExecutionStrategy noOpStrategy = ExecutionStrategies.noOp();
        ExecutionStrategy exitFlaggingStrategy = ExecutionStrategies.exitFlagging();
        ExecutionStrategy systemExitStrategy = ExecutionStrategies.systemExit();

        assertAll(
            () -> assertThatCode(() -> acceptStrategy(noOpStrategy)).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(exitFlaggingStrategy)).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(systemExitStrategy)).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(ExecutionStrategies.noOp())).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(ExecutionStrategies.exitFlagging())).doesNotThrowAnyException(),
            () -> assertThatCode(() -> acceptStrategy(ExecutionStrategies.systemExit())).doesNotThrowAnyException()
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
            assertThatCode(() -> executionStrategy.exit(1)).doesNotThrowAnyException();
        }
    }

    @Nested
    class ExitFlaggingStrategy {

        @Test
        void shouldFlagCallsToExit() {
            var strategy = ExecutionStrategies.exitFlagging();
            assertThatCode(() -> strategy.exit(1)).doesNotThrowAnyException();

            assertThat(strategy.didExit()).isTrue();
            assertThat(strategy.exitCode()).hasValue(1);
        }

        @Test
        void shouldCaptureExitCode() {
            var strategy = ExecutionStrategies.exitFlagging();
            strategy.exit(42);

            assertThat(strategy.didExit()).isTrue();
            assertThat(strategy.exitCode()).hasValue(42);
        }

        @Test
        void shouldNotFlagWhenExitNotCalled() {
            var strategy = ExecutionStrategies.exitFlagging();

            assertThat(strategy.didExit()).isFalse();
            assertThat(strategy.exitCode()).isEmpty();
        }
    }

    @Nested
    class SystemExitStrategy {

        @ParameterizedTest
        @CsvSource({
                "1, 0",
                "143, 0",
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
            new SystemExecutioner().exit(exitCode, Duration.ofMillis(exitWaitDelayMillis));
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
