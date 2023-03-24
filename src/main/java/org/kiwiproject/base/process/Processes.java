package org.kiwiproject.base.process;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.SPACE;
import static org.kiwiproject.base.KiwiStrings.format;
import static org.kiwiproject.base.KiwiStrings.splitToList;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.io.KiwiIO.readLinesFromErrorStreamOf;
import static org.kiwiproject.io.KiwiIO.readLinesFromInputStreamOf;
import static org.kiwiproject.io.KiwiIO.streamLinesFromInputStreamOf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kiwiproject.base.UncheckedInterruptedException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for working with operating system processes.
 * <p>
 * <em>Note that most of the methods are intended only for use on Unix/Linux operating systems.</em>
 *
 * @see ProcessHelper
 */
@UtilityClass
@Slf4j
public class Processes {

    /**
     * Exit code that indicates a {@link Process} terminated normally.
     *
     * @see Process#exitValue()
     */
    public static final int SUCCESS_EXIT_CODE = 0;

    /**
     * Default number of seconds to wait for termination of a process.
     *
     * @see #kill(long, KillSignal, KillTimeoutAction)
     * @see #kill(long, String, KillTimeoutAction)
     */
    public static final int DEFAULT_KILL_TIMEOUT_SECONDS = 5;

    /**
     * Default number of seconds to wait for a process to exit.
     *
     * @see #waitForExit(Process)
     */
    public static final long DEFAULT_WAIT_FOR_EXIT_TIME_SECONDS = 5;

    /**
     * The command flags to use with the {@code pgrep} command for matching and printing the full command line.
     * For example, to find the "sleep 25" process with pid 32332 we want pgrep to return results in the
     * format: "[pid] [full command]". For this example the expected result is: {@code 32332 sleep 25}.
     * <p>
     * However, there are differences in pgrep command line arguments between BSD-based systems (including macOS) and
     * Linux systems. There are also differences between older and newer Linux distributions such as
     * CentOS/Red Hat 6 and 7. Specifically, some systems require {@code -fl} to match against and print out the full
     * command line with {@code pgrep}. Other implementations require {@code -fa} (or {@code -f --list-full}).
     * <p>
     * See for example <a href="http://man7.org/linux/man-pages/man1/pgrep.1.html">Linux pgrep</a> and
     * <a href="https://www.freebsd.org/cgi/man.cgi?query=pgrep&sektion=1">BSD pgrep</a> for more information.
     */
    private static final String PGREP_FULL_COMMAND_MATCH_AND_PRINT_FLAGS;

    private static final boolean PGREP_CHECK_SUCCESSFUL;

    static {
        var result = choosePgrepFlags();
        PGREP_FULL_COMMAND_MATCH_AND_PRINT_FLAGS = result.getLeft();
        PGREP_CHECK_SUCCESSFUL = result.getRight();
    }

    private static final String PGREP_COMMAND = "pgrep";

    private static Pair<String, Boolean> choosePgrepFlags() {
        String flagsOrNull = findPgrepFlags().orElse(null);
        return choosePgrepFlags(flagsOrNull);
    }

    @VisibleForTesting
    static Pair<String, Boolean> choosePgrepFlags(@Nullable String flagsOrNull) {
        if (isNull(flagsOrNull)) {
            logPgrepFlagWarnings();
            return Pair.of("-fa", false);
        }

        return Pair.of(flagsOrNull, true);
    }

    private static Optional<String> findPgrepFlags() {
        return tryPgrepForSleepCommand("-fa", "123")
                .or(() -> tryPgrepForSleepCommand("-fl", "124"));
    }

    private static Optional<String> tryPgrepForSleepCommand(String flags, String sleepTime) {
        Process sleeperProc = null;
        try {
            sleeperProc = launch("sleep", sleepTime);
            var pid = String.valueOf(sleeperProc.pid());
            var pgrepCheckerProc = launch(PGREP_COMMAND, flags, "sleep");
            var stdOutLines = readLinesFromInputStreamOf(pgrepCheckerProc);
            var stdErrLines = readLinesFromErrorStreamOf(pgrepCheckerProc);
            var expectedCommand = "sleep " + sleepTime;
            logPgrepCheckInfo(flags, pid, stdOutLines, stdErrLines, expectedCommand);

            if (linesContainPidAndFullCommand(stdOutLines, pid, expectedCommand)) {
                LOG.info("Will use [{}] as flags for pgrep full-command listing", flags);
                return Optional.of(flags);
            }

            LOG.trace("Flags [{}] did not produce pgrep full-command listing", flags);
            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Error checking pgrep flags. pgrep calls may fail in unexpected ways!", e);
            return Optional.empty();
        } finally {
            if (nonNull(sleeperProc)) {
                killSilently(sleeperProc.pid());
            }
        }
    }

    @VisibleForTesting
    static void logPgrepCheckInfo(String flags,
                                  String pid,
                                  List<String> stdOutLines,
                                  List<String> stdErrLines,
                                  String expectedCommand) {
        LOG.trace("Checking pgrep flags [{}] for command [{}] with pid {}", flags, expectedCommand, pid);
        LOG.trace("pid {} stdOut: {}", pid, stdOutLines);
        if (stdErrLines.isEmpty()) {
            LOG.trace("pid {} stdErr: {}", pid, stdErrLines);
        } else {
            LOG.warn("stdErr checking pgrep flags for pid {} (command: {}): {}",
                    pid, expectedCommand, stdErrLines);
        }
    }

    private static boolean linesContainPidAndFullCommand(List<String> lines, String pid, String expectedCommand) {
        return lines.stream().anyMatch(line -> line.contains(pid) && line.contains(expectedCommand));
    }

    private static void killSilently(long processId) {
        try {
            LOG.trace("Killing sleeper process ({}) used to determine pgrep flags", processId);
            kill(processId, KillSignal.SIGTERM, KillTimeoutAction.NO_OP);
        } catch (Exception e) {
            LOG.warn("Error killing sleeper process ({}) used to determine pgrep flags", processId, e);
        }
    }

    @VisibleForTesting
    static void logPgrepFlagWarnings() {
        LOG.warn("Neither -fa nor -fl flags produced PID and full command line, so pgrep commands will behave (or fail) in unexpected ways!");
        LOG.warn("If you see this warning, DO NOT use any of the pgrep-related methods in Processes or ProcessHelper and submit a bug report.");
        LOG.warn("Turn on TRACE-level logging to see standard output and error for pgrep commands");
        LOG.warn("We will use -fa even though we know this will not work, instead of throwing an exception");
    }

    /**
     * Use this method to determine if calling any of the pgrep methods in this class will work as expected.
     *
     * @return true if the pgrep check to determine the flags to use for full command matching was successful; false
     * otherwise. If false you should NOT use any of the pgrep methods.
     * @see #getPgrepFlags()
     */
    public static boolean wasPgrepFlagsCheckSuccessful() {
        return PGREP_CHECK_SUCCESSFUL;
    }

    /**
     * Returns the pgrep flags that {@link Processes} will use in all pgrep methods.
     * Use {@link #wasPgrepFlagsCheckSuccessful()} to check if the flags were chosen successfully
     * to ensure that pgrep commands will work as expected on your OS.
     *
     * @return the flags that will be used in pgrep commands
     */
    public static String getPgrepFlags() {
        return PGREP_FULL_COMMAND_MATCH_AND_PRINT_FLAGS;
    }

    /**
     * Get a process id, or "pid".
     * <p>
     * <em>Earlier versions of Kiwi used to perform some nasty things (checking if the actual process class was
     * {@code java.lang.UNIXProcess}, then using reflection to get the value of the private {@code pid} field)
     * to obtain Process pids, but since JDK 9 offers the {@link Process#pid()} method, that is no longer necessary.
     * This method is now a simple delegation to {@link Process#pid()} and you should now prefer that method.</em>
     *
     * @param process the process to obtain the process id (pid) from
     * @return the process id of {@code process}
     * @throws UnsupportedOperationException if the Process implementation does not support getting the pid
     * @see Process#pid()
     */
    public static long processId(Process process) {
        checkArgumentNotNull(process);
        return process.pid();
    }

    /**
     * Get a process id, or "pid", if it is available from the {@link Process} implementation, wrapped inside
     * an OptionalLong. If the pid is not available for whatever reason, return an empty OptionalLong.
     *
     * @param process the process to obtain the process id (pid) from
     * @return an OptionalLong containing the process if of {@code process} or an empty OptionalLong if the
     * {@link Process} implementation does not support getting the pid for whatever reason.
     * @implNote the {@link Process#pid()} method says it can throw {@link UnsupportedOperationException} if the
     * "implementation does not support this operation" but does not specify under what circumstances that can
     * happen, and I have not been able to find this information using Google, Bing, or DuckDuckGo. This method
     * logs a warning along with the exception, so if this occurs check your logs for the possible reason.
     */
    public static OptionalLong processIdOrEmpty(Process process) {
        checkArgumentNotNull(process);
        try {
            return OptionalLong.of(process.pid());
        } catch (UnsupportedOperationException e) {
            LOG.warn("The JDK cannot get the PID of the given Process. Check stack trace for a possible reason", e);
            return OptionalLong.empty();
        }
    }

    /**
     * Check if the given {@link Process} has an exit code representing successful termination.
     *
     * @param process the Process, assumed to have exited
     * @return true if the process terminated successfully
     * @see #isSuccessfulExitCode(int)
     * @see #SUCCESS_EXIT_CODE
     * @see Process#exitValue()
     */
    public static boolean hasSuccessfulExitCode(Process process) {
        return isSuccessfulExitCode(process.exitValue());
    }

    /**
     * Check if the given exit code represents successful termination.
     *
     * @param exitCode the exit code to check
     * @return true if the exit code represents success
     * @see #SUCCESS_EXIT_CODE
     * @see Process#exitValue()
     */
    public static boolean isSuccessfulExitCode(int exitCode) {
        return exitCode == SUCCESS_EXIT_CODE;
    }

    /**
     * Check if the given exit code represents anything other than successful termination.
     * In other words, is the exit code nonzero?
     *
     * @param exitCode the exit code to check
     * @return true if the exit code is nonzero
     * @see Process#exitValue()
     * @implNote This method is specifically named to indicate that the exit code does not
     * represent success, leaving the possibility open that a nonzero exit code can indicate
     * some condition other than an error.
     */
    public static boolean isNonzeroExitCode(int exitCode) {
        return !isSuccessfulExitCode(exitCode);
    }

    /**
     * Waits up to {@link #DEFAULT_WAIT_FOR_EXIT_TIME_SECONDS} for the given process to exit.
     * <p>
     * Note that this method does <em>not</em> destroy the process if it times out waiting.
     *
     * @param process the process to wait for
     * @return an {@link Optional} that will contain the exit code if the process exited before the timeout, or
     * empty if the process did not exit before the timeout expired.
     */
    public static Optional<Integer> waitForExit(Process process) {
        return waitForExit(process, DEFAULT_WAIT_FOR_EXIT_TIME_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Waits up to the specified {@code timeout} for the given process to exit.
     * <p>
     * Note that this method does <em>not</em> destroy the process if it times out waiting.
     *
     * @param process the process to wait for
     * @param timeout the value of the time to wait
     * @param unit    the unit of time to wait
     * @return an {@link Optional} that will contain the exit code if the process exited before the timeout, or
     * empty if the process did not exit before the timeout expired.
     */
    public static Optional<Integer> waitForExit(Process process, long timeout, TimeUnit unit) {
        try {
            boolean exited = process.waitFor(timeout, unit);
            return exited ? Optional.of(process.exitValue()) : Optional.empty();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted waiting for process to exit", e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Launches a new process using the specified {@code command}. This is just a convenience wrapper around
     * creating a new {@link ProcessBuilder} and calling {@link ProcessBuilder#start()}.
     * <p>
     * This wrapper also converts any thrown {@link IOException} to an {@link UncheckedIOException}.
     * <p>
     * <em>If you need more flexibility than provided in this simple wrapper, use {@link ProcessBuilder} directly.</em>
     *
     * @param command the list containing the program and its arguments
     * @return the new {@link Process}
     * @see ProcessBuilder#ProcessBuilder(List)
     * @see ProcessBuilder#start()
     */
    public static Process launch(List<String> command) {
        return launch(null, command);
    }

    /**
     * Launches a new process using the specified {@code command} with the given working directory.
     * This is just a convenience wrapper around creating a new {@link ProcessBuilder}, setting the
     * {@link ProcessBuilder#directory(File) working directory}, and calling {@link ProcessBuilder#start()}.
     * <p>
     * This wrapper converts any thrown {@link IOException} to an {@link UncheckedIOException}.
     * <p>
     * <em>If you need more flexibility than provided in this simple wrapper, use {@link ProcessBuilder} directly.</em>
     *
     * @param workingDirectory the working directory to use
     * @param command          the list containing the program and its arguments
     * @return the new {@link Process}
     * @see ProcessBuilder#ProcessBuilder(List)
     * @see ProcessBuilder#directory(File)
     * @see ProcessBuilder#start()
     */
    public static Process launch(@Nullable File workingDirectory, List<String> command) {
        try {
            return launchProcessInternal(workingDirectory, command);
        } catch (IOException e) {
            throw new UncheckedIOException("Error launching command: " + command, e);
        }
    }

    /**
     * Launches a new process using the specified {@code command}.
     *
     * @param command a list containing the program and its arguments
     * @return the new {@link Process}
     * @see #launch(List)
     */
    public static Process launch(String... command) {
        return launch(Lists.newArrayList(command));
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param commandLine the full command to match
     * @return a list of matching process ids (pids)
     * @see #pgrep(String, String)
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static List<Long> pgrep(String commandLine) {
        return pgrep(null, commandLine);
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command to match
     * @return list of matching process ids (pids)
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static List<Long> pgrep(String user, String commandLine) {
        try {
            List<String> command = buildPgrepCommand(user, commandLine);
            var process = launchProcessInternal(command);

            return streamLinesFromInputStreamOf(process)
                    .map(Processes::getPidOrThrow)
                    .collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(
                    format("Error executing pgrep with user [%s] and command [%s]", user, commandLine), e);
        }
    }

    /**
     * Does a {@code pgrep} against the specified full command, expecting a single result, or no result.
     *
     * @param commandLine the full command line
     * @return an optional either containing a process id, or an empty optional
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static Optional<Long> pgrepWithSingleResult(String commandLine) {
        return pgrepWithSingleResult(null, commandLine);
    }

    /**
     * Does a {@code pgrep} against the specified full command, expecting a single result for a specific user, or no result.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command to match
     * @return an optional either containing a process id, or an empty optional
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static Optional<Long> pgrepWithSingleResult(String user, String commandLine) {
        List<Long> pids = pgrep(user, commandLine);
        if (pids.isEmpty()) {
            return Optional.empty();
        }

        checkState(pids.size() == 1, "Expecting exactly one result pid for command [%s], but received %s: %s",
                commandLine, pids.size(), pids);

        return Optional.of(first(pids));
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param commandLine the full command line to match
     * @return a list of pgrep output, with each line in format "{pid} {command}"
     * @see #pgrepList(String, String)
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static List<String> pgrepList(String commandLine) {
        return pgrepList(null, commandLine);
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command line to match
     * @return a list of pgrep output, with each line in format "{pid} {command}"
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static List<String> pgrepList(String user, String commandLine) {
        try {
            List<String> command = buildPgrepListCommand(user, commandLine);
            var process = launchProcessInternal(command);

            return readLinesFromInputStreamOf(process);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    format("Error executing pgrep with user [%s] and command [%s]", user, commandLine), e);
        }
    }

    /**
     * Does a {@code pgrep} for the specified full command, returning a list of pairs containing the
     * process id (pid) and the matched command line.
     *
     * @param commandLine the full command line to match
     * @return a list of {@link Pair} objects; each pair contains the pid as a Long and the associated full command
     * @see #pgrepParsedList(String, String)
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static List<Pair<Long, String>> pgrepParsedList(String commandLine) {
        return pgrepParsedList(null, commandLine);
    }

    /**
     * Does a {@code pgrep} for the specified full command, returning a list of pairs containing the
     * process id (pid) and the matched command line.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command line to match
     * @return a list of {@link Pair} objects; each pair contains the pid as a Long and the associated full command
     * @see #wasPgrepFlagsCheckSuccessful()
     * @see #getPgrepFlags()
     */
    public static List<Pair<Long, String>> pgrepParsedList(String user, String commandLine) {
        List<String> lines = pgrepList(user, commandLine);

        return lines.stream().map(Processes::pairFromPgrepLine).collect(toList());
    }

    private static Pair<Long, String> pairFromPgrepLine(String line) {
        List<String> splat = splitToList(line, SPACE, 2);

        var pid = getPidOrThrow(first(splat));
        var command = second(splat);
        return Pair.of(pid, command);
    }

    static Long getPidOrThrow(String pidString) {
        try {
            return Long.valueOf(pidString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("pid must be a number", e);
        }
    }

    private static List<String> buildPgrepCommand(String user, String commandLine) {
        return buildPgrepCommand(user, commandLine, "-f");
    }

    private static List<String> buildPgrepListCommand(String user, String commandLine) {
        return buildPgrepCommand(user, commandLine, PGREP_FULL_COMMAND_MATCH_AND_PRINT_FLAGS);
    }

    private static List<String> buildPgrepCommand(String user, String commandLine, String flags) {
        checkArgument(doesNotContainWhitespace(flags),
                "Currently only short flags specified together with no whitespace" +
                        " are supported, e.g. -fl and NOT -f -l. Offending flags: %s", flags);

        if (StringUtils.isBlank(user)) {
            return Lists.newArrayList(PGREP_COMMAND, flags, commandLine);
        }

        return Lists.newArrayList(PGREP_COMMAND, "-u", user, flags, commandLine);
    }

    private static boolean doesNotContainWhitespace(String string) {
        return !string.contains(" ");
    }

    /**
     * Kill a process, waiting up to {@link #DEFAULT_KILL_TIMEOUT_SECONDS} seconds for it to terminate.
     *
     * @param processId the pid of the process to kill
     * @param signal    the kill signal; this could be the signal number (e.g. "1") or name (e.g. "SIGHUP")
     * @param action    the {@link KillTimeoutAction} to take if the process doesn't terminate within the allotted time
     * @return the exit code from the {@code kill} command, or {@code -1} if {@code action} is
     * @see #kill(long, String, long, TimeUnit, KillTimeoutAction)
     */
    public static int kill(long processId, KillSignal signal, KillTimeoutAction action) {
        return kill(processId, signal.number(), action);
    }

    /**
     * Kill a process, waiting up to {@link #DEFAULT_KILL_TIMEOUT_SECONDS} seconds for it to terminate.
     *
     * @param processId the pid of the process to kill
     * @param signal    the kill signal; this could be the signal number (e.g. "1") or name (e.g. "SIGHUP")
     * @param action    the {@link KillTimeoutAction} to take if the process doesn't terminate within the allotted time
     * @return the exit code from the {@code kill} command, or {@code -1} if {@code action} is
     * {@link KillTimeoutAction#NO_OP} and the kill command times out
     * @throws UncheckedIOException if an I/O error occurs killing the process
     */
    public static int kill(long processId, String signal, KillTimeoutAction action) {
        return kill(processId, signal, DEFAULT_KILL_TIMEOUT_SECONDS, TimeUnit.SECONDS, action);
    }

    /**
     * Kill a process, waiting up to {@code timeout} in the specified {@link TimeUnit} for it to terminate.
     *
     * @param processId the pid of the process to kill
     * @param signal    the kill signal enum
     * @param timeout   the time to wait for the process to be killed
     * @param unit      the time unit associated with {@code timeout}
     * @param action    the {@link KillTimeoutAction} to take if the process doesn't terminate within the allotted time
     * @return the exit code from the {@code kill} command, or {@code -1} if {@code action} is
     * @see #kill(long, String, long, TimeUnit, KillTimeoutAction)
     */
    public static int kill(long processId, KillSignal signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return kill(processId, signal.number(), timeout, unit, action);
    }

    /**
     * Kill a process, waiting up to {@code timeout} in the specified {@link TimeUnit} for it to terminate.
     *
     * @param processId the pid of the process to kill
     * @param signal    the kill signal; this could be the signal number (e.g. "1") or name (e.g. "SIGHUP")
     * @param timeout   the time to wait for the process to be killed
     * @param unit      the time unit associated with {@code timeout}
     * @param action    the {@link KillTimeoutAction} to take if the process doesn't terminate within the allotted time
     * @return the exit code from the {@code kill} command, or {@code -1} if {@code action} is
     * {@link KillTimeoutAction#NO_OP} and the kill command times out
     * @throws UncheckedIOException if an I/O error occurs killing the process
     */
    public static int kill(long processId, String signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        try {
            var killProcess = launchProcessInternal("kill", KillSignal.withLeadingDash(signal), String.valueOf(processId));

            return killInternal(processId, killProcess, timeout, unit, action);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Process launchProcessInternal(String... commandLine) throws IOException {
        return launchProcessInternal(Lists.newArrayList(commandLine));
    }

    private static Process launchProcessInternal(List<String> command) throws IOException {
        return launchProcessInternal(null, command);
    }

    private static Process launchProcessInternal(@Nullable File workingDirectory,
                                                 List<String> command) throws IOException {
        return new ProcessBuilder(command)
                .directory(workingDirectory)
                .start();
    }

    /**
     * Equivalent to a {@code kill -9} (i.e. a {@code SIGKILL}).
     *
     * @param process the process to kill forcibly
     * @param timeout the time to wait for the process to be forcibly killed
     * @param unit    the time unit associated with the {@code timeout}
     * @return {@code true} if {@code process} was killed before the timeout period elapsed; {@code false} otherwise
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @see Process#destroyForcibly()
     * @see Process#waitFor(long, TimeUnit)
     */
    public static boolean killForcibly(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        return process.destroyForcibly().waitFor(timeout, unit);
    }

    @VisibleForTesting
    static int killInternal(long processId, Process killProcess, long timeout, TimeUnit unit, KillTimeoutAction action) {
        try {
            boolean exitedBeforeWaitTimeout = killProcess.waitFor(timeout, unit);

            if (exitedBeforeWaitTimeout) {
                return killProcess.exitValue();
            }

            return doTimeoutAction(action, killProcess, processId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new UncheckedInterruptedException(e);
        }
    }

    private static int doTimeoutAction(KillTimeoutAction action, Process killProcess, long processId)
            throws InterruptedException {

        switch (action) {
            case FORCE_KILL:
                boolean killedBeforeWaitTimeout = killForcibly(killProcess, 1L, TimeUnit.SECONDS);
                validateKilledBeforeTimeout(processId, killedBeforeWaitTimeout);
                return killProcess.exitValue();

            case THROW_EXCEPTION:
                throw new IllegalStateException(
                        format("Process %s did not end before timeout (and exception was requested)", processId));

            case NO_OP:
                LOG.warn("Process {} did not end before timeout and no-op action requested, so doing nothing", processId);
                return -1;

            default:
                throw new IllegalStateException("Unaccounted for action: " + action);
        }
    }

    private static void validateKilledBeforeTimeout(long processId, boolean killedBeforeWaitTimeout) {
        if (!killedBeforeWaitTimeout) {
            throw new IllegalStateException(
                    format("Process %s was not killed before 1 second timeout expired", processId));
        }
    }
}
