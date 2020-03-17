package org.kiwiproject.base.process;

import static java.util.stream.Collectors.toList;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.io.KiwiIO.readLinesFromInputStreamOf;
import static org.kiwiproject.io.KiwiIO.streamLinesFromInputStreamOf;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.tuple.Pair;

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
     * @see Processes#waitForExit(Process)
     */
    public Optional<Integer> waitForExit(Process process) {
        return Processes.waitForExit(process);
    }

    /**
     * @see Processes#waitForExit(Process, long, TimeUnit)
     */
    public Optional<Integer> waitForExit(Process process, long timeout, TimeUnit unit) {
        return Processes.waitForExit(process, timeout, unit);
    }

    /**
     * @see Processes#launch(List)
     */
    public Process launch(List<String> command) {
        return Processes.launch(command);
    }

    /**
     * @see Processes#launch(String...)
     */
    public Process launch(String... command) {
        return Processes.launch(command);
    }

    /**
     * @see Processes#pgrep(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Long> pgrep(String commandLine) {
        return Processes.pgrep(commandLine);
    }

    /**
     * @see Processes#pgrep(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Long> pgrep(String user, String commandLine) {
        return Processes.pgrep(user, commandLine);
    }

    /**
     * @see Processes#pgrepWithSingleResult(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public Optional<Long> pgrepWithSingleResult(String commandLine) {
        return Processes.pgrepWithSingleResult(commandLine);
    }

    /**
     * @see Processes#pgrepWithSingleResult(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public Optional<Long> pgrepWithSingleResult(String user, String commandLine) {
        return Processes.pgrepWithSingleResult(user, commandLine);
    }

    /**
     * @see Processes#pgrepList(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<String> pgrepList(String commandLine) {
        return Processes.pgrepList(commandLine);
    }

    /**
     * @see Processes#pgrepList(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<String> pgrepList(String user, String commandLine) {
        return Processes.pgrepList(user, commandLine);
    }

    /**
     * @see Processes#pgrepParsedList(String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Pair<Long, String>> pgrepParsedList(String commandLine) {
        return Processes.pgrepParsedList(commandLine);
    }

    /**
     * @see Processes#pgrepParsedList(String, String)
     * @see Processes#wasPgrepFlagsCheckSuccessful()
     * @see Processes#getPgrepFlags()
     */
    public List<Pair<Long, String>> pgrepParsedList(String user, String commandLine) {
        return Processes.pgrepParsedList(user, commandLine);
    }

    /**
     * @see Processes#kill(long, KillSignal, KillTimeoutAction)
     */
    public int kill(long processId, KillSignal signal, KillTimeoutAction action) {
        return Processes.kill(processId, signal, action);
    }

    /**
     * @see Processes#kill(long, KillSignal, long, TimeUnit, KillTimeoutAction)
     */
    public int kill(long processId, KillSignal signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return Processes.kill(processId, signal, timeout, unit, action);
    }

    /**
     * @see Processes#kill(long, String, KillTimeoutAction)
     */
    public int kill(long processId, String signal, KillTimeoutAction action) {
        return Processes.kill(processId, signal, action);
    }

    /**
     * @see Processes#kill(long, String, long, TimeUnit, KillTimeoutAction)
     */
    public int kill(long processId, String signal, long timeout, TimeUnit unit, KillTimeoutAction action) {
        return Processes.kill(processId, signal, timeout, unit, action);
    }

    /**
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
        Process process = launchPgrepWithParentPidFlag(parentProcessId, processHelper);
        List<String> lines = readLinesFromInputStreamOf(process);

        if (lines.isEmpty()) {
            return Optional.empty();
        } else if (lines.size() == 1) {
            return Optional.of(Long.valueOf(first(lines)));
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
        Process process = launchPgrepWithParentPidFlag(parentProcessId, processHelper);
        Stream<String> stream = streamLinesFromInputStreamOf(process);

        return stream.map(Long::valueOf).collect(toList());
    }

    /**
     * @implNote The "-P" flag causes pgrep to "Only match processes whose parent process ID is listed" per
     * the man page. Also of note, "-P" is equivalent to the {@code --parent} long flag
     */
    private Process launchPgrepWithParentPidFlag(long parentProcessId, ProcessHelper processHelper) {
        return processHelper.launch("pgrep", "-P", String.valueOf(parentProcessId));
    }

}
