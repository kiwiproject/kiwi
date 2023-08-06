package org.kiwiproject.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import jakarta.validation.Validator;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

@DisplayName("DirectoryPathValidator")
class DirectoryPathValidatorTest {

    private static final String CUSTOM_MESSAGE = "is not a directory, is not readable, or is not writable";
    private static final String DEFAULT_MESSAGE = "is not a valid directory path (and may not be readable or writable)";

    private Validator validator;
    private Config config;
    private String tempFolderPath;

    @TempDir
    Path folder;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
        config = new Config();
        tempFolderPath = folder.toString();
    }

    @Nested
    class ValidationMessage {

        @Test
        void shouldUseDefaultMessage() {
            assertPropertyViolations(validator, config, "directoryPath1", DEFAULT_MESSAGE);
        }

        @Test
        void shouldUseCustomMessage() {
            assertPropertyViolations(validator, config, "directoryPath2",
                    CUSTOM_MESSAGE);
        }
    }

    @Nested
    class WhenNullValue {

        @Test
        void shouldBeValid_WhenAllowingNulls() {
            assertNoPropertyViolations(validator, config, "directoryPath3");
        }

        @Test
        void shouldBeInvalid_ByDefault() {
            assertPropertyViolations(validator, config, "directoryPath4", DEFAULT_MESSAGE);
        }
    }

    @Nested
    class ShouldBeValid {

        @Test
        void whenDirectoryExists() {
            config.setDirectoryPath1(tempFolderPath);
            assertNoPropertyViolations(validator, config, "directoryPath1");
        }

        @Test
        void whenEnsuringReadable() {
            config.setDirectoryPath5(tempFolderPath);
            assertNoPropertyViolations(validator, config, "directoryPath5");
        }

        @Test
        void whenEnsuringWritable() {
            config.setDirectoryPath6(tempFolderPath);
            assertNoPropertyViolations(validator, config, "directoryPath6");
        }

        @Test
        void whenEnsuringReadableAndWritable() {
            config.setDirectoryPath7(tempFolderPath);
            assertNoPropertyViolations(validator, config, "directoryPath7");
        }
    }

    @Nested
    class WhenMkdirsTrue {

        @Test
        void shouldDoNothing_WhenDirectoryExists() {
            config.setDirectoryPath8(tempFolderPath);

            assertNoPropertyViolations(validator, config, "directoryPath8");
        }

        @Test
        void shouldCreateDirectory_WhenDirectoryDoesNotExist() {
            var newDirectory = new File(tempFolderPath, "newSubDir");
            assertThat(newDirectory)
                    .describedAs("precondition violated: directory should not exist")
                    .doesNotExist();

            config.setDirectoryPath8(newDirectory.getAbsolutePath());

            assertNoPropertyViolations(validator, config, "directoryPath8");

            assertThat(newDirectory)
                    .describedAs("should have created directory")
                    .exists();
        }

        @Test
        void shouldFailValidation_WhenUnableToCreateDirectory() {
            var notWritable = new File(tempFolderPath, "notWritable");
            assertThat(notWritable.mkdir()).isTrue();
            assertThat(notWritable.setWritable(false)).isTrue();

            var secondSubDir = new File(notWritable, "subDirOfNotWritable");
            config.setDirectoryPath8(secondSubDir.getAbsolutePath());

            assertOnePropertyViolation(validator, config, "directoryPath8");
        }
    }

    @Nested
    class ShouldBeInvalid {

        @Test
        void whenDirectoryDoesNotExist() {
            config.setDirectoryPath1(Path.of(tempFolderPath, "does-not-exist").toString());

            assertPropertyViolations(validator, config, "directoryPath1", DEFAULT_MESSAGE);
        }

        @Test
        void whenInvalidPathExceptionThrown() {
            var file = new File(tempFolderPath, "\0");
            config.setDirectoryPath1(file.getAbsolutePath());  // "Nul" character is not allowed in paths

            assertOnePropertyViolation(validator, config, "directoryPath1");
        }

        @Test
        void whenIsRegularFile() throws IOException {
            var file = Path.of(tempFolderPath, "a-file.txt");
            var newFile = Files.writeString(file, "tic tac toe");

            assertThat(newFile)
                    .describedAs("precondition violated: file should be a regular file")
                    .isRegularFile();

            config.setDirectoryPath2(newFile.toString());

            assertPropertyViolations(validator, config, "directoryPath2", CUSTOM_MESSAGE);
        }

        @Test
        void whenEnsuringReadable_ButDirectoryNotReadable() {
            var notReadable = new File(tempFolderPath, "notReadable");
            assertThat(notReadable.mkdir()).isTrue();
            setFilePermissions(notReadable, false, true);

            runEnsureTest(notReadable, config, (file, cfg) -> {
                cfg.setDirectoryPath5(file.getAbsolutePath());
                assertOnePropertyViolation(validator, cfg, "directoryPath5");
            });
        }

        @Test
        void whenEnsuringWritable_ButDirectoryNotWritable() {
            var notWritable = new File(tempFolderPath, "notWritable");
            assertThat(notWritable.mkdir()).isTrue();
            setFilePermissions(notWritable, true, false);

            runEnsureTest(notWritable, config, (file, cfg) -> {
                cfg.setDirectoryPath6(file.getAbsolutePath());
                assertOnePropertyViolation(validator, cfg, "directoryPath6");
            });
        }

        @Test
        void whenEnsuringReadWrite_ButDirectoryIsNeitherReadableNorWritable() {
            var notAccessible = new File(tempFolderPath, "notAccessible");
            assertThat(notAccessible.mkdir()).isTrue();
            setFilePermissions(notAccessible, false, false);

            runEnsureTest(notAccessible, config, (file, cfg) -> {
                cfg.setDirectoryPath7(file.getAbsolutePath());
                assertOnePropertyViolation(validator, cfg, "directoryPath7");
            });
        }

        private void runEnsureTest(File file, Config config, BiConsumer<File, Config> fileConsumer) {
            try {
                fileConsumer.accept(file, config);
            } finally {
                setFilePermissions(file, true, true);
            }
        }
    }

    private static void setFilePermissions(File file, boolean canRead, boolean canWrite) {
        assertThat(file.setReadable(canRead))
                .describedAs("failed to set readable on %s to %s", file, canRead)
                .isTrue();

        assertThat(file.setWritable(canWrite))
                .describedAs("failed to set writable on %s to %s", file, canWrite)
                .isTrue();
    }

    @Data
    static class Config {

        @DirectoryPath
        private String directoryPath1;

        @DirectoryPath(message = CUSTOM_MESSAGE)
        private String directoryPath2;

        @DirectoryPath(allowNull = true)
        private String directoryPath3;

        @SuppressWarnings("DefaultAnnotationParam")
        @DirectoryPath(allowNull = false)
        private String directoryPath4;

        @DirectoryPath(ensureReadable = true)
        private String directoryPath5;

        @DirectoryPath(ensureWritable = true)
        private String directoryPath6;

        @DirectoryPath(ensureReadable = true, ensureWritable = true)
        private String directoryPath7;

        @DirectoryPath(mkdirs = true, ensureReadable = true, ensureWritable = true)
        private String directoryPath8;
    }
}
