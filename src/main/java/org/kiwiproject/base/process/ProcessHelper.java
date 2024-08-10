package org.kiwiproject.base.process;

import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.io.KiwiIO.readLinesFromInputStreamOf;
import static org.kiwiproject.io.KiwiIO.streamLinesFromInputStreamOf;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Wrapper class around the static utility functions in {@link Processes} that requires an instance, adn thus by using
 * an instance of this class instead of {@link Processes} directly, it will make it much easier to test code that deals
 * with processes.
 * <p>
 * <em>Note that most of the methods are intended only for use on Unix/Linux operating systems.</em>
 */
public class ProcessHelper {

    /**
     * Waits up to {@link Processes#DEFAULT_WAIT_FOR_EXIT_TIME_SECONDS} for the given process to exit.
     *
     * @param process the process to wait for
     * @return an {@link Optional} that will contain the exit code if the process exited before the timeout, or
     * empty if the process did not exit before the timeout expired.
     * @see Processes#waitForExit(Process)
     */
    public Optional<Integer> waitForExit(Process process) {
        return Processes.waitForExit(process);
    }

    /**
     * Waits up to the specified {@code timeout} for the given process to exit.
     *
     * @param process the process to wait for
     * @param timeout the value of the time to wait
     * @param unit    the unit of time to wait
     * @return an {@link Optional} that will contain the exit code if the process exited before the timeout, or
     * empty if the process did not exit before the timeout expired.
     * @see Processes#waitForExit(Process, long, TimeUnit)
     */
    public Optional<Integer> waitForExit(Process process, long timeout, TimeUnit unit) {
        return Processes.waitForExit(process, timeout, unit);
    }

    /**
     * Launches a new process using the specified {@code command}.
     *
     * @param command the list containing the program and its arguments
     * @return the new {@link Process}
     * @see Processes#launch(List)
     */
    public Process launch(List<String> command) {
        return Processes.launch(command);
    }

    /**
     * Launches a new process using the specified {@code workingDirectory} and {@code command}.
     *
     * @param workingDirectory the working directory to use
     * @param command          the list containing the program and its arguments
     * @return the new {@link Process}
     * @see Processes#launch(File, List)
     */
    public Process launch(@Nullable File workingDirectory, List<String> command) {
        return Processes.launch(workingDirectory, command);
    }

    /**
     * Launches a new process using the specified {@code command}.
     *
     * @param command a list containing the program and its arguments
     * @return the new {@link Process}
     * @see Processes#launch(String...)
     */
    public Process launch(String... command) {
        return Processes.launch(command);
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param commandLine the full command to match
     * @return a list of matching process ids (pids)
     * @see Processes#pgrep(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Long> pgrep(String commandLine) {
        return Processes.pgrep(commandLine);
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command to match
     * @return list of matching process ids (pids)
     * @see Processes#pgrep(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Long> pgrep(String user, String commandLine) {
        return Processes.pgrep(user, commandLine);
    }

    /**
     * Does a {@code pgrep} against the specified full command, expecting a single result, or no result.
     *
     * @param commandLine the full command line
     * @return an optional either containing a process id, or an empty optional
     * @see Processes#pgrepWithSingleResult(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public Optional<Long> pgrepWithSingleResult(String commandLine) {
        return Processes.pgrepWithSingleResult(commandLine);
    }

    /**
     * Does a {@code pgrep} against the specified full command, expecting a single result for a specific user, or no result.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command to match
     * @return an optional either containing a process id, or an empty optional
     * @see Processes#pgrepWithSingleResult(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public Optional<Long> pgrepWithSingleResult(String user, String commandLine) {
        return Processes.pgrepWithSingleResult(user, commandLine);
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param commandLine the full command line to match
     * @return a list of pgrep output, with each line in format "{pid} {command}"
     * @see Processes#pgrepList(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<String> pgrepList(String commandLine) {
        return Processes.pgrepList(commandLine);
    }

    /**
     * Does a {@code pgrep} with the specified full command.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command line to match
     * @return a list of pgrep output, with each line in format "{pid} {command}"
     * @see Processes#pgrepList(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<String> pgrepList(String user, String commandLine) {
        return Processes.pgrepList(user, commandLine);
    }

    /**
     * Does a {@code pgrep} for the specified full command, returning a list of pairs containing the
     * process id (pid) and the matched command line.
     *
     * @param commandLine the full command line to match
     * @return a list of {@link Pair} objects; each pair contains the pid as a {@code Long} and the associated full command
     * @see Processes#pgrepParsedList(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Pair<Long, String>> pgrepParsedList(String commandLine) {
        return Processes.pgrepParsedList(commandLine);
    }

    /**
     * Does a {@code pgrep} for the specified full command, returning a list of pairs containing the
     * process id (pid) and the matched command line.
     *
     * @param user        the OS user (passed to the {@code -u} option)
     * @param commandLine the full command line to match
     * @return a list of {@link Pair} objects; each pair contains the pid as a {@code Long} and the associated full command
     * @see Processes#pgrepParsedList(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Pair<Long, String>> pgrepParsedList(String user, String commandLine) {
        return Processes.pgrepParsedList(user, commandLine);
    }

    /**
     * Kill a process, waiting up to {@link Processes#DEFAULT_KILL_TIMEOUT_SECONDS} seconds for it to terminate.
     *
     * @param processId the pid of the process to kill
     * @param signal    the kill signal; this could be the signal number (e.g. "1") or name (e.g. "SIGHUP")
     * @param action    the {@link KillTimeoutAction} to take if the process doesn't terminate within the allotted time
     * @return the exit code from the {@code kill} command, or {@code -1} if {@code action} is
     * @see Processes#kill(long, KillSignal, KillTimeoutAction)
     */
    public int kill(long processId, KillSignal signal, KillTimeoutAction action) {
        return Processes.kill(processId, signal, action);
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
     * @see Processes#kill(long, KillSignal, long, TimeUnit, KillTimeoutAction)
     */
    public int kill(long processId, KillSignal signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return Processes.kill(processId, signal, timeout, unit, action);
    }

    /**
     * Kill a process, waiting up to {@link Processes#DEFAULT_KILL_TIMEOUT_SECONDS} seconds for it to terminate.
     *
     * @param processId the pid of the process to kill
     * @param signal    the kill signal; this could be the signal number (e.g. "1") or name (e.g. "SIGHUP")
     * @param action    the {@link KillTimeoutAction} to take if the process doesn't terminate within the allotted time
     * @return the exit code from the {@code kill} command, or {@code -1} if {@code action} is
     * {@link KillTimeoutAction#NO_OP} and the kill command times out
     * @throws UncheckedIOException if an I/O error occurs while killing the process
     * @see Processes#kill(long, String, KillTimeoutAction)
     */
    public int kill(long processId, String signal, KillTimeoutAction action) {
        return Processes.kill(processId, signal, action);
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
     * @throws UncheckedIOException if an I/O error occurs while killing the process
     * @see Processes#kill(long, String, long, TimeUnit, KillTimeoutAction)
     */
    public int kill(long processId, String signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return Processes.kill(processId, signal, timeout, unit, action);
    }

    /**
     * Equivalent to a {@code kill -9} (i.e. a {@code SIGKILL}).
     *
     * @param process the process to kill forcibly
     * @param timeout the time to wait for the process to be forcibly killed
     * @param unit    the time unit associated with the {@code timeout}
     * @return {@code true} if {@code process} was killed before the timeout period elapsed; {@code false} otherwise
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @see Processes#killForcibly(Process, long, TimeUnit)
     */
    public boolean killForcibly(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        return Processes.killForcibly(process, timeout, unit);
    }

    /**
     * For a given parent process id, find <em>one and only one</em> child process. There may be no child processes,
     * in which case an empty {@link Optional} is returned.
     * <p>
     * <em>This method considers it an error if the process has more than one child process id.</em>
     *
     * @param parentProcessId the parent process id
     * @return optional containing the child process id if there is one; otherwise an empty optional
     * @throws IllegalStateException if there is more than one child process found
     */
    public Optional<Long> findChildProcessId(long parentProcessId) {
        return findChildProcessIdInternal(parentProcessId, this);
    }

    /**
     * This method is <em>entirely for testing</em>, specifically to be able to mock the Process object returned by
     * calling launch on {@code processHelper} to perform a {@code pgrep -P}.
     *
     * @see #launchPgrepWithParentPidFlag(long, ProcessHelper)
     */
    @VisibleForTesting
    Optional<Long> findChildProcessIdInternal(long parentProcessId, ProcessHelper processHelper) {
        var process = launchPgrepWithParentPidFlag(parentProcessId, processHelper);
        List<String> lines = readLinesFromInputStreamOf(process);

        if (lines.isEmpty()) {
            return Optional.empty();
        } else if (lines.size() == 1) {
            return Optional.of(Processes.getPidOrThrow(first(lines)));
        } else {
            throw new IllegalStateException("More than one child process found for process ID " + parentProcessId);
        }
    }

    /**
     * For a given parent process id, find any child processes. There may be no child processes, in which case an
     * empty collection is returned.
     *
     * @param parentProcessId the parent process id
     * @return a collection of child pids, or an empty collection if there are no child processes
     */
    public Collection<Long> findChildProcessIds(long parentProcessId) {
        return findChildProcessIdsInternal(parentProcessId, this);
    }

    /**
     * This method is <em>entirely for testing</em>, specifically to be able to mock the Process object returned by
     * calling launch on {@code processHelper} to perform a {@code pgrep -P}.
     *
     * @see #launchPgrepWithParentPidFlag(long, ProcessHelper)
     */
    @VisibleForTesting
    Collection<Long> findChildProcessIdsInternal(long parentProcessId, ProcessHelper processHelper) {
        var process = launchPgrepWithParentPidFlag(parentProcessId, processHelper);
        Stream<String> stream = streamLinesFromInputStreamOf(process);

        return stream.map(Processes::getPidOrThrow).toList();
    }

    /**
     * @implNote The "-P" flag causes pgrep to "Only match processes whose parent process ID is listed" per
     * the man page. Also of note, "-P" is equivalent to the {@code --parent} long flag
     */
    private Process launchPgrepWithParentPidFlag(long parentProcessId, ProcessHelper processHelper) {
        return processHelper.launch("pgrep", "-P", String.valueOf(parentProcessId));
    }

    /**
     * Locate a program in the user's path, returning the result as a {@link Path}.
     *
     * @param program the program to locate
     * @return an Optional containing the full {@link Path} to the program, or an empty Optional if not found
     * @implNote If there is more than program found, only the first one is returned
     * @see Processes#whichAsPath(String)
     */
    public Optional<Path> whichAsPath(String program) {
        return Processes.whichAsPath(program);
    }

    /**
     * Locate a program in the user's path.
     *
     * @param program the program to locate
     * @return an Optional containing the full path to the program, or an empty Optional if not found
     * @implNote If there is more than program found, only the first one is returned
     * @see Processes#which(String)
     */
    public Optional<String> which(String program) {
        return Processes.which(program);
    }
}
