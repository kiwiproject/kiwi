package org.kiwiproject.base.process;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
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
     * There are a few significant differences in command line argument flags between the much older {@code procps} UNIX
     * process library, which is found on Red Hat 6 (and older) systems, and the newer {@code procps-ng} library found on
     * Red Hat 7 (and above) systems. The main difference for the purposes of this class is the different flags on the
     * {@code pgrep} command. Specifically in the older {@code procps} library you use {@code -fl} to match against and
     * print out the full command line with {@code pgrep}. But with {@code procps-ng} you must use {@code -fa} to perform
     * the same full command line match and printing. (The {@code -a} short flag is equivalent to the {@code --list-full}
     * long flag, though we only use short flags here.)
     * <p>
     * <em>Note: we are assuming the {@code pgrep} command is available, and that the minimum version of it comes from
     * {@code procps}. If {@code procps-ng} is available then we change the command flags used to match and print the
     * full command line. Obviously if these assumptions are false, for example if {@code pgrep} command is not present,
     * then none of the pgrep methods in this class will work anyway.</em>
     */
    private static final boolean PROCPS_NEXT_GENERATION = isProcpsNextGeneration();

    /**
     * The command flags to use with the {@code pgrep} command for matching and printing the full command line.
     *
     * @see #PROCPS_NEXT_GENERATION
     */
    private static final String PGREP_FULL_COMMAND_MATCH_AND_PRINT_FLAGS = choosePgrepFlags();

    private static final String PGREP_COMMAND = "pgrep";

    /**
     * This method launches a sleep process, then attempts to use the {@code procps-ng} flag {@code -a} with a
     * {@code pgrep} command to determine if the {@code progps-ng} library is available, or whether we only have
     * the older {@code procps} library available.
     */
    private static boolean isProcpsNextGeneration() {
        Process sleeperProc = null;
        try {
            sleeperProc = launch("sleep", "123");
            Process pgrepCheckerProc = launch(PGREP_COMMAND, "-a", "sleep");

            List<String> stdOut = readLinesFromInputStreamOf(pgrepCheckerProc);
            LOG.trace("stdout from procps-ng check: {}", stdOut);

            List<String> stdErr = readLinesFromErrorStreamOf(pgrepCheckerProc);
            LOG.trace("stderr from procps-ng check: {}", stdErr);

            return stdErr.isEmpty();
        } catch (Exception e) {
            LOG.error("Error checking if procps-ng is available. Assuming yes. If assumption is wrong, pgrep calls may fail in unexpected ways!", e);
            return true;
        } finally {
            if (nonNull(sleeperProc)) {
                long processId = sleeperProc.pid();
                killSilently(processId);
            }
        }
    }

    private static void killSilently(long processId) {
        try {
            LOG.trace("Killing sleeper process ({}) used to check procps-ng", processId);
            kill(processId, KillSignal.SIGTERM, KillTimeoutAction.NO_OP);
        } catch (Exception e) {
            LOG.warn("Error killing sleeper process", e);
        }
    }

    private static String choosePgrepFlags() {
        String flags = Processes.PROCPS_NEXT_GENERATION ? "-fa" : "-fl";
        LOG.info("procps-ng present? {}. Using [{}] as flags for pgrep commands", Processes.PROCPS_NEXT_GENERATION, flags);

        return flags;
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
     * Waits up to {@link #DEFAULT_WAIT_FOR_EXIT_TIME_SECONDS} for the given process to exit. Returns an {@link Optional}
     * that will contain the exit code if the process exited before the timeout, or be empty if the process did not exit
     * before the timeout expired.
     */
    public static Optional<Integer> waitForExit(Process process) {
        return waitForExit(process, DEFAULT_WAIT_FOR_EXIT_TIME_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Waits up to the specified {@code timeout} for the given process to exit. Returns an {@link Optional}
     * that will contain the exit code if the process exited before the timeout, or be empty if the process did not exit
     * before the timeout expired.
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
        try {
            return launchInternal(command);
        } catch (IOException e) {
            throw new UncheckedIOException("Error launching command: " + command, e);
        }
    }

    /**
     * Launches a new process using the specified {@code command}.
     *
     * @see #launch(List)
     */
    public static Process launch(String... command) {
        return launch(Lists.newArrayList(command));
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @see #pgrep(String, String)
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
     */
    public static List<Long> pgrep(String user, String commandLine) {
        try {
            List<String> command = buildPgrepCommand(user, commandLine);
            Process process = launchInternal(command);

            return streamLinesFromInputStreamOf(process)
                    .map(Long::valueOf)
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
     * @see #pgrepList(String, String)
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
     */
    public static List<String> pgrepList(String user, String commandLine) {
        try {
            List<String> command = buildPgrepListCommand(user, commandLine);
            Process process = launchInternal(command);

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
     * @see #pgrepParsedList(String, String)
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
     */
    public static List<Pair<Long, String>> pgrepParsedList(String user, String commandLine) {
        List<String> lines = pgrepList(user, commandLine);

        return lines.stream().map(Processes::pairFromPgrepLine).collect(toList());
    }

    private static Pair<Long, String> pairFromPgrepLine(String line) {
        List<String> splat = splitToList(line, SPACE, 2);

        return Pair.of(Long.valueOf(first(splat)), second(splat));
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
            Process killProcess = launchInternal("kill", KillSignal.withLeadingDash(signal), String.valueOf(processId));

            return killInternal(processId, killProcess, timeout, unit, action);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Process launchInternal(String... commandLine) throws IOException {
        return launchInternal(Lists.newArrayList(commandLine));
    }

    private static Process launchInternal(List<String> command) throws IOException {
        return new ProcessBuilder(command).start();
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
