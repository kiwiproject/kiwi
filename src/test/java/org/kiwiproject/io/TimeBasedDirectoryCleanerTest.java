package org.kiwiproject.io;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kiwiproject.io.TimeBasedDirectoryCleaner.DeleteError;
import org.kiwiproject.io.TimeBasedDirectoryCleaner.FileDeleteResult;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

class TimeBasedDirectoryCleanerTest {

    @TempDir
    Path temporaryPath;

    private TimeBasedDirectoryCleanerTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TimeBasedDirectoryCleanerTestHelper(temporaryPath);
    }

    @Test
    void testBuildCleaner() {
        var pathString = temporaryPath.toString();

        var cleaner = TimeBasedDirectoryCleaner.builder()
                .directoryPath(pathString)
                .retentionThreshold(Duration.ofDays(14))
                .build();

        assertThat(cleaner.getDirectoryPath()).isEqualTo(pathString);
        assertThat(cleaner.getRetentionThreshold()).isEqualTo(Duration.ofHours(14 * 24));
        assertThat(cleaner.getRetentionThresholdDescription()).isEqualTo("14 days");
        assertThat(cleaner.getDeleteCount()).isZero();
        assertThat(cleaner.getDeleteErrorCount()).isZero();
        assertThat(TimeBasedDirectoryCleaner.capacityOfRecentDeleteErrors()).isEqualTo(500);
    }

    @Test
    void testCreateCleaner_WithNegativeDuration_ThrowsException() {
        assertThatThrownBy(() -> TimeBasedDirectoryCleaner.builder()
                .directoryPath(temporaryPath.toString())
                .retentionThreshold(Duration.ofMillis(-100))
                .build())
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("retentionThreshold cannot be negative");
    }

    @Test
    void testCreateCleaner_WithNoLogLevel_DefaultsToWarn() {
        var cleaner = TimeBasedDirectoryCleaner.builder()
                .directoryPath(temporaryPath.toString())
                .retentionThreshold(Duration.ofHours(1))
                .build();

        assertThat(cleaner.deleteErrorLogLevel).isEqualTo(Level.WARN);
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void testCreateCleaner_WithSpecificLogLevel(Level level) {
        var cleaner = TimeBasedDirectoryCleaner.builder()
                .directoryPath(temporaryPath.toString())
                .retentionThreshold(Duration.ofHours(1))
                .deleteErrorLogLevel(level.toString())
                .build();

        assertThat(cleaner.deleteErrorLogLevel).isEqualTo(level);
    }

    @Test
    void testCreateCleaner_WithInvalidLogLevel_DefaultsToWarn() {
        var cleaner = TimeBasedDirectoryCleaner.builder()
                .directoryPath(temporaryPath.toString())
                .retentionThreshold(Duration.ofHours(1))
                .deleteErrorLogLevel("FOO")
                .build();

        assertThat(cleaner.deleteErrorLogLevel).isEqualTo(Level.WARN);
    }

    /**
     * Ok, yes, this is a test hack to ensure we cover the logUnableToDelete method at all logging levels.
     * There is nothing to assert, and success is that no exceptions are thrown, I guess.
     */
    @ParameterizedTest
    @EnumSource(Level.class)
    void testLogUnableToDelete(Level level) {
        var levelString = level.toString();
        var cleaner = newCleanerWithLogLevel(levelString);

        var result = FileDeleteResult.attempted("/tmp/foo.txt", false);
        assertThatCode(() -> cleaner.logUnableToDelete(result)).doesNotThrowAnyException();
    }

    private static TimeBasedDirectoryCleaner newCleanerWithLogLevel(String level) {
        return TimeBasedDirectoryCleaner.builder()
                .directoryPath("/foo/bar")
                .retentionThreshold(Duration.ofHours(8))
                .deleteErrorLogLevel(level)
                .build();
    }

    @Test
    void testClean_WhenNoFilesOlderThanRetentionThreshold() throws IOException {
        createFilesWithSuffixesInRange(1, 10);

        var cleaner = newCleanerWithRetentionThreshold(Duration.ofMinutes(5));
        cleanAndAssertBeforeAndAfterCounts(cleaner, 10, 10);

        assertFilesExist("file", 1, 10);
    }

    private void createFilesWithSuffixesInRange(int start, int end) {
        IntStream.rangeClosed(start, end).forEach(value -> newFileInTempFolder("file" + value));
    }

    private void newFileInTempFolder(String fileName) {
        try {
            Files.createFile(temporaryPath.resolve(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TimeBasedDirectoryCleaner newCleanerWithRetentionThreshold(Duration retentionThreshold) {
        return TimeBasedDirectoryCleaner.builder()
                .directoryPath(temporaryPath.toString())
                .retentionThreshold(retentionThreshold)
                .build();
    }

    private void cleanAndAssertBeforeAndAfterCounts(TimeBasedDirectoryCleaner cleaner,
                                                    int expectedCountBeforeCleaning,
                                                    int expectedCountAfterCleaning) throws IOException {

        assertTempFolderContainsFileCountOf(expectedCountBeforeCleaning);
        cleaner.run();
        assertTempFolderContainsFileCountOf(expectedCountAfterCleaning);
    }

    private void assertTempFolderContainsFileCountOf(int expectedCount) throws IOException {
        assertThat(testHelper.filesInTempFolder()).hasSize(expectedCount);
    }

    private void assertFilesExist(String fileNamePrefix, int firstExpectedFileNumber, int lastExpectedFileNumber) throws IOException {
        var filesExpectedToExist = IntStream.rangeClosed(firstExpectedFileNumber, lastExpectedFileNumber)
                .mapToObj(value -> temporaryPath.resolve(fileNamePrefix + value).toFile())
                .toList();

        assertThat(testHelper.filesInTempFolder()).containsOnly(filesExpectedToExist.toArray(new File[]{}));
    }

    @Test
    void testClean_WhenAllFilesOlderThanRetentionThreshold() throws IOException {
        createFilesWithSuffixesInRange(1, 10);

        var retentionThreshold = Duration.ofMillis(100);
        waitUntilLastModifiedIsBeforeRetentionThreshold("file10", retentionThreshold);

        var cleaner = newCleanerWithRetentionThreshold(retentionThreshold);
        cleanAndAssertBeforeAndAfterCounts(cleaner, 10, 0);
    }

    private void waitUntilLastModifiedIsBeforeRetentionThreshold(String fileName, Duration retentionThreshold) {
        var lastModified = temporaryPath.resolve(fileName).toFile().lastModified();

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> lastModifiedIsBeforeRetentionThreshold(lastModified, retentionThreshold));
    }

    private static boolean lastModifiedIsBeforeRetentionThreshold(long lastModified, Duration retentionThreshold) {
        return System.currentTimeMillis() - lastModified > retentionThreshold.plusMillis(100).toMillis();
    }

    @Test
    void testClean_WhenSomeFilesOlderThanRetentionThreshold() throws IOException {
        createFilesWithSuffixesInRange(1, 5);

        var retentionThreshold = Duration.ofSeconds(1);
        waitUntilLastModifiedIsBeforeRetentionThreshold("file5", retentionThreshold);

        createFilesWithSuffixesInRange(6, 10);

        var cleaner = newCleanerWithRetentionThreshold(retentionThreshold);
        cleanAndAssertBeforeAndAfterCounts(cleaner, 10, 5);

        assertFilesExist("file", 6, 10);
    }

    @Test
    void testClean_WhenAllFilesAreDirectories_AndNoDirectoriesOlderThanRetentionThreshold() throws IOException {
        createDirectoriesWithFiles(1, 5);

        var cleaner = newCleanerWithRetentionThreshold(Duration.ofMinutes(5));
        cleanAndAssertBeforeAndAfterCounts(cleaner, 5, 5);

        assertFilesExist("folder", 1, 5);
    }

    private void createDirectoriesWithFiles(int start, int end) {
        testHelper.createDirectoriesWithFiles(start, end);
    }

    @Test
    void testClean_WhenAllFilesAreDirectories_AndAllDirectoriesOlderThanRetentionThreshold() throws IOException {
        createDirectoriesWithFiles(1, 5);

        var retentionThreshold = Duration.ofMillis(100);
        waitUntilLastModifiedIsBeforeRetentionThreshold("folder5", retentionThreshold);

        var cleaner = newCleanerWithRetentionThreshold(retentionThreshold);
        cleanAndAssertBeforeAndAfterCounts(cleaner, 5, 0);
    }

    @Test
    void testClean_WhenAllFilesAreDirectories_AndSomeDirectoriesOlderThanRetentionThreshold() throws IOException {
        createDirectoriesWithFiles(1, 10);

        var retentionThreshold = Duration.ofSeconds(1);
        waitUntilLastModifiedIsBeforeRetentionThreshold("folder10", retentionThreshold);

        createDirectoriesWithFiles(11, 20);

        var cleaner = newCleanerWithRetentionThreshold(retentionThreshold);
        cleanAndAssertBeforeAndAfterCounts(cleaner, 20, 10);

        assertFilesExist("folder", 11, 20);
    }

    @Test
    void testCleaner_NeverLetsExceptionEscape() {
        var cleaner = spy(newCleanerWithRetentionThreshold(Duration.ofDays(5)));

        doThrow(new RuntimeException("bad things")).when(cleaner).cleanDirectory();

        var thrown = catchThrowable(cleaner::run);
        assertThat(thrown).isNull();

        assertSoftly(softly -> {
            softly.assertThat(cleaner.getDeleteCount()).isZero();
            softly.assertThat(cleaner.getDeleteErrorCount()).isOne();
            softly.assertThat(cleaner.getNumberOfRecentDeleteErrors()).isOne();
            softly.assertThat(cleaner.getRecentDeleteErrors()).hasSize(1);
        });

        var firstError = first(cleaner.getRecentDeleteErrors());
        assertThat(firstError.isExceptionError()).isTrue();
        assertThat(firstError.isFileDeleteError()).isFalse();
        assertSoftly(softly -> {
            softly.assertThat(firstError.getExceptionType()).isEqualTo(RuntimeException.class.getName());
            softly.assertThat(firstError.getExceptionMessage()).isEqualTo("bad things");
        });
    }

    @Test
    void testUpdateFileDeletionMetadata_WhenNoDeleteErrors() {
        var cleaner = newCleanerWithRetentionThreshold(Duration.ofDays(14));

        cleaner.updateFileDeletionMetadata(10, emptyList());

        assertSoftly(softly -> {
            softly.assertThat(cleaner.getDeleteCount()).isEqualTo(10);
            softly.assertThat(cleaner.getDeleteErrorCount()).isZero();
            softly.assertThat(cleaner.getNumberOfRecentDeleteErrors()).isZero();
            softly.assertThat(cleaner.getRecentDeleteErrors()).isEmpty();
        });
    }

    @Test
    void testUpdateFileDeletionMetaData_WhenSomeDeleteErrors() {
        var cleaner = newCleanerWithRetentionThreshold(Duration.ofDays(7));

        var filesNotDeleted = List.of(
                newFailedFileDeleteResult(temporaryPath.resolve("file1.txt").toString()),
                newFailedFileDeleteResult(temporaryPath.resolve("file2.txt").toString()),
                newFailedFileDeleteResult(temporaryPath.resolve("file3.txt").toString())
        );

        cleaner.updateFileDeletionMetadata(25, filesNotDeleted);

        assertSoftly(softly -> {
            softly.assertThat(cleaner.getDeleteCount()).isEqualTo(22);
            softly.assertThat(cleaner.getDeleteErrorCount()).isEqualTo(3);
            softly.assertThat(cleaner.getNumberOfRecentDeleteErrors()).isEqualTo(3);

            softly.assertThat(cleaner.getRecentDeleteErrors().stream().map(DeleteError::getFileName))
                    .containsOnly(filesNotDeleted.stream().map(FileDeleteResult::getAbsolutePath).toList().toArray(new String[]{}));

            softly.assertThat(cleaner.getRecentDeleteErrors().stream().map(DeleteError::isFileDeleteError))
                    .containsOnly(true);

            softly.assertThat(cleaner.getRecentDeleteErrors().stream().map(DeleteError::isExceptionError))
                    .containsOnly(false);
        });

        cleaner.clearRecentDeleteErrors();
        assertThat(cleaner.getNumberOfRecentDeleteErrors()).isZero();
    }

    private static FileDeleteResult newFailedFileDeleteResult(String absolutePath) {
        return FileDeleteResult.attempted(absolutePath, false);
    }

    @Test
    void testUpdateFileDeletionMetadata_WhenExceedInMemoryDeleteErrorCapacity() {
        var cleaner = newCleanerWithRetentionThreshold(Duration.ofDays(7));

        var filesNotDeleted = IntStream.rangeClosed(1, 501)
                .mapToObj(value -> newFailedFileDeleteResult(temporaryPath.resolve("folder" + value).toString()))
                .toList();

        cleaner.updateFileDeletionMetadata(600, filesNotDeleted);

        assertSoftly(softly -> {
            softly.assertThat(cleaner.getDeleteCount()).isEqualTo(99);
            softly.assertThat(cleaner.getDeleteErrorCount()).isEqualTo(501);
            softly.assertThat(cleaner.getNumberOfRecentDeleteErrors()).isEqualTo(500);

            var last500 = filesNotDeleted.stream()
                    .map(FileDeleteResult::getAbsolutePath)
                    .toList()
                    .subList(1, filesNotDeleted.size());

            softly.assertThat(cleaner.getRecentDeleteErrors().stream().map(DeleteError::getFileName))
                    .containsOnly(last500.toArray(new String[]{}));
        });
    }

    @Nested
    class TryDeleteIfExists {

        @Test
        void shouldReturnSkippedWhenFileDoesNotExist() {
            var file = new File("/does/not/exist/file.txt");

            var deleteResult = TimeBasedDirectoryCleaner.tryDeleteIfExists(file);

            assertThat(deleteResult.isDeleteWasAttempted()).isFalse();
            assertThat(deleteResult.isDeleteWasSuccessful()).isFalse();
        }
    }

    @Nested
    class FileDeleteResults {

        @Test
        void shouldReturnTrue_WhenDeleteAttemptedAndFailed() {
            var deleteResult = FileDeleteResult.attempted("/path/to/some/file.txt", false);

            assertThat(deleteResult.isDeleteWasAttempted()).isTrue();
            assertThat(deleteResult.isDeleteWasSuccessful()).isFalse();
            assertThat(deleteResult.deleteAttemptedAndFailed()).isTrue();
        }
    }
}
