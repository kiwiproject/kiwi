package org.kiwiproject.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Queues;
import com.google.errorprone.annotations.Immutable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.kiwiproject.collect.KiwiEvictingQueues;
import org.slf4j.event.Level;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Runnable} that cleans a given directory of files and/or directories that are older than a specified
 * retention threshold.
 * <p>
 * NOTE: This class relies on classes in commons-io, so you will need to add it as a dependency to your project!
 *
 * @implNote This is intended to be run in a single thread, e.g., using a {@link java.util.concurrent.ScheduledExecutorService}
 * with one thread. Results are undefined (and probably bad) if multiple threads execute the same instance of this
 * class concurrently. Note also that accessing the delete error count, recent delete errors, etc. is thread-safe.
 */
@Slf4j
public class TimeBasedDirectoryCleaner implements Runnable {

    private static final int MAX_RECENT_DELETE_ERRORS = 500;
    private static final boolean SUPPRESS_LEADING_ZERO_ELEMENTS = true;
    private static final boolean SUPPRESS_TRAILING_ZERO_ELEMENTS = true;
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    private final Queue<DeleteError> recentDeleteErrors = KiwiEvictingQueues.synchronizedEvictingQueue(MAX_RECENT_DELETE_ERRORS);
    private final AtomicLong deleteCount = new AtomicLong();
    private final AtomicInteger deleteErrorCount = new AtomicInteger();

    private final File directory;
    private final long retentionThresholdInMillis;

    @VisibleForTesting final Level deleteErrorLogLevel;

    @Getter
    private final String retentionThresholdDescription;

    /**
     * Value class representing a file delete error.
     */
    @Immutable
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeleteError {

        @With
        private final long timestamp;
        private final String fileName;
        private final String exceptionType;
        private final String exceptionMessage;

        public static DeleteError of(String fileName) {
            checkArgumentNotNull(fileName, "fileName is required");
            return new DeleteError(System.currentTimeMillis(), fileName, null, null);
        }

        public static DeleteError of(FileDeleteResult deleteResult) {
            checkArgumentNotNull(deleteResult, "deleteResult is required");
            checkState(deleteResult.deleteAttemptedAndFailed(), "must be an attempted delete that failed");
            return new DeleteError(System.currentTimeMillis(), deleteResult.absolutePath, null, null);
        }

        public static DeleteError of(Exception ex) {
            checkArgumentNotNull(ex, "exception is required");
            return new DeleteError(System.currentTimeMillis(), null, ex.getClass().getName(), ex.getMessage());
        }

        public boolean isExceptionError() {
            return nonNull(exceptionType);
        }

        public boolean isFileDeleteError() {
            return nonNull(fileName);
        }
    }

    /**
     * Create a new TimeBasedDirectoryCleaner instance.
     *
     * @param directoryPath       the directory to be cleaned
     * @param retentionThreshold  how long the directory contents should be retained before deletion
     * @param deleteErrorLogLevel the log level; should be a string corresponding to an SLF4J {@link Level}
     * @implNote No validation on whether the directoryPath points to a valid directory at this point; we assume it
     * will exist at some point, e.g., if some other component creates it the first time it is written to, we don't want
     * to throw exceptions from here.
     */
    @Builder
    public TimeBasedDirectoryCleaner(String directoryPath, Duration retentionThreshold, String deleteErrorLogLevel) {
        checkArgumentNotNull(directoryPath, "directoryPath is required");
        checkArgumentNotNull(retentionThreshold, "retentionThreshold is required");

        directory = new File(directoryPath);

        retentionThresholdInMillis = retentionThreshold.toMillis();
        checkArgument(retentionThresholdInMillis > 0, "retentionThreshold cannot be negative");

        retentionThresholdDescription = durationDescription(retentionThresholdInMillis);

        this.deleteErrorLogLevel = isNull(deleteErrorLogLevel) ? Level.WARN : resolveLevelOrDefaultToWarn(deleteErrorLogLevel);
    }

    private static Level resolveLevelOrDefaultToWarn(String deleteErrorLogLevel) {
        try {
            return Level.valueOf(deleteErrorLogLevel);
        } catch (IllegalArgumentException e) {
            LOG.warn("Level {} is not a valid SLF4J level, defaulting to WARN. Valid levels: {}",
                    deleteErrorLogLevel, Arrays.toString(Level.values()));
            LOG.trace("Actual exception:", e);

            return Level.WARN;
        }
    }

    private static String durationDescription(long milliseconds) {
        if (milliseconds < 1_000) {
            return milliseconds + " milliseconds";
        }

        return DurationFormatUtils.formatDurationWords(milliseconds,
                SUPPRESS_LEADING_ZERO_ELEMENTS,
                SUPPRESS_TRAILING_ZERO_ELEMENTS);
    }

    /**
     * Returns the absolute path of the directory being cleaned.
     *
     * @return directory path
     */
    public String getDirectoryPath() {
        return directory.getAbsolutePath();
    }

    /**
     * Returns the retention threshold as a {@link Duration}
     *
     * @return retention threshold
     */
    public Duration getRetentionThreshold() {
        return Duration.ofMillis(retentionThresholdInMillis);
    }

    /**
     * Returns the total number of deletes this instance has counted.
     *
     * @return the total delete count
     * @implNote A directory that is deleted is counted as ONE deletion, regardless of how many files were inside.
     */
    public long getDeleteCount() {
        return deleteCount.get();
    }

    /**
     * Returns the total number of delete errors this instance has counted.
     *
     * @return the total delete error count
     */
    public int getDeleteErrorCount() {
        return deleteErrorCount.get();
    }

    /**
     * Returns the number of recent delete errors currently stored in memory.
     *
     * @return total number of recent delete errors
     */
    public int getNumberOfRecentDeleteErrors() {
        return recentDeleteErrors.size();
    }

    /**
     * Returns the maximum number of delete failures that can be stored in memory.
     *
     * @return number of delete errors that will be stored in memory
     */
    public static int capacityOfRecentDeleteErrors() {
        return MAX_RECENT_DELETE_ERRORS;
    }

    /**
     * Clears all delete errors currently stored in memory.
     */
    public void clearRecentDeleteErrors() {
        recentDeleteErrors.clear();
    }

    /**
     * Returns all the recent delete failures stored in memory.
     *
     * @return a list of recent DeleteError objects
     * @implNote Per the docs for {@link Queues#synchronizedQueue(Queue)}, we MUST synchronize when iterating, and
     * creating a new ArrayList has to iterate the constructor argument's contents in some fashion.
     */
    public List<DeleteError> getRecentDeleteErrors() {
        synchronized (recentDeleteErrors) {
            return new ArrayList<>(recentDeleteErrors);
        }
    }

    @Override
    public void run() {
        try {
            cleanDirectory();
        } catch (Exception e) {
            deleteErrorCount.incrementAndGet();
            recentDeleteErrors.add(DeleteError.of(e));
            LOG.error("Error cleaning directory [{}] with retention threshold {}",
                    directory.getAbsolutePath(),
                    retentionThresholdDescription,
                    e);
        }
    }

    @VisibleForTesting
    @SuppressWarnings("java:S3864")
    void cleanDirectory() {
        LOG.debug("Cleaning directory [{}] with retention threshold {}",
                directory.getAbsolutePath(), retentionThresholdDescription);
        final long now = System.currentTimeMillis();
        LOG.trace("Reference current time for directory cleanup: {}", now);

        File[] filesToClean = Optional.ofNullable(
                directory.listFiles(file -> olderThanRetentionThreshold(file, now)))
                .orElse(EMPTY_FILE_ARRAY);
        LOG.debug("Found {} files to clean (that are older than retention threshold)", filesToClean.length);

        var attemptedDeletes = Arrays.stream(filesToClean)
                .map(TimeBasedDirectoryCleaner::tryDeleteIfExists)
                .filter(result -> result.deleteWasAttempted)
                .toList();

        var numExpectedDeletes = attemptedDeletes.size();

        var failedDeletes = attemptedDeletes
                .stream()
                .filter(FileDeleteResult::deleteAttemptedAndFailed)
                .peek(this::logUnableToDelete)
                .toList();

        updateFileDeletionMetadata(numExpectedDeletes, failedDeletes);
    }

    /**
     * Attempt to delete the file if it exists, which might not be the case if multiple cleaners in different JVMs are
     * executing concurrently against a shared directory.
     *
     * @implNote We have only tested this on a Centos/RedHat based system. Some issues have been noticed with deleting files
     * concurrently from multiple threads in the same directories on macOS. Use with caution.
     */
    @VisibleForTesting
    static FileDeleteResult tryDeleteIfExists(File file) {
        var absolutePath = file.getAbsolutePath();
        LOG.trace("Attempting to delete file {}", absolutePath);

        if (file.exists()) {
            LOG.trace("File {} exists", absolutePath);
            var wasDeleted = FileUtils.deleteQuietly(file);
            LOG.trace("Attempt to delete existing file {} was successful? {}", absolutePath, wasDeleted);

            return FileDeleteResult.attempted(absolutePath, wasDeleted);
        }

        LOG.trace("Skipped delete attempt as file did not exist: {}", absolutePath);
        return FileDeleteResult.skipped(absolutePath);
    }

    @AllArgsConstructor
    @Getter
    public static class FileDeleteResult {

        private final String absolutePath;
        private final boolean deleteWasAttempted;
        private final boolean deleteWasSuccessful;

        static FileDeleteResult attempted(String absolutePath, boolean deleteWasSuccessful) {
            return new FileDeleteResult(requireNonNull(absolutePath), true, deleteWasSuccessful);
        }

        static FileDeleteResult skipped(String absolutePath) {
            return new FileDeleteResult(requireNonNull(absolutePath), false, false);
        }

        boolean deleteAttemptedAndFailed() {
            return deleteWasAttempted && !deleteWasSuccessful;
        }
    }

    /**
     * Log that we could not delete a specific file at the logging level this cleaner instance has been configured at.
     */
    @VisibleForTesting
    void logUnableToDelete(FileDeleteResult deleteResult) {
        logDeleteError("Unable to delete " + deleteResult.absolutePath);
    }

    @VisibleForTesting
    void updateFileDeletionMetadata(int expectedDeleteCount, List<FileDeleteResult> failedDeleteResults) {
        if (!failedDeleteResults.isEmpty()) {
            var newDeleteErrorCount = deleteErrorCount.addAndGet(failedDeleteResults.size());
            logDeleteError(f("There are now {} total file delete errors", newDeleteErrorCount));

            failedDeleteResults.stream()
                    .map(DeleteError::of)
                    .forEach(recentDeleteErrors::add);
        }

        var actualDeleteCount = expectedDeleteCount - failedDeleteResults.size();
        var newCumulativeDeleteCount = deleteCount.addAndGet(actualDeleteCount);
        LOG.debug("Deleted {} files; new cumulative delete count: {}", actualDeleteCount, newCumulativeDeleteCount);
    }

    /**
     * Log some kind of delete error/warning at the logging level this cleaner instance has been configured at.
     *
     * @implNote Quite annoyingly, the SLF4J API does not define a general "log" method that accepts a {@link Level}
     * and only defines methods named after the level. As a result, we have to use conditional logic shenanigans
     * because we're definitely not going to use reflection for this. There is a JIRA ticket that has been around for a
     * long time, SLF4J-124, which was created in 2009, resolved as "Won't Fix", and then was re-opened at
     * some point (cannot tell when, unfortunately). As of 5/2019, it looks like they are targeting a 2.0 release to
     * add this feature.
     */
    private void logDeleteError(String message) {
        switch (deleteErrorLogLevel) {
            case TRACE -> LOG.trace(message);
            case DEBUG -> LOG.debug(message);
            case INFO -> LOG.info(message);
            case WARN, ERROR -> LOG.error(message);
            default -> LOG.warn(message);
        }
    }

    private boolean olderThanRetentionThreshold(File file, long now) {
        var ageInMillis = now - file.lastModified();
        var shouldDelete = ageInMillis > retentionThresholdInMillis;

        LOG.trace("Age of file {}: {} ms (retention threshold: {} ms); should delete? {}",
                file.getAbsolutePath(),
                ageInMillis,
                retentionThresholdInMillis,
                shouldDelete);
        return shouldDelete;
    }
}
