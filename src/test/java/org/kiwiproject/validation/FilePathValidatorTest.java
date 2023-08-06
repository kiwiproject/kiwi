package org.kiwiproject.validation;

import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import jakarta.validation.Validator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.internal.Fixtures;

@DisplayName("FilePathValidator")
class FilePathValidatorTest {

    private static final String NOT_VALID_FILE_PATH_MESSAGE = "is not a valid file path";

    private Validator validator;
    private Config config;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
        config = new Config();
    }

    @Nested
    class WhenNullValue {

        @Test
        void shouldBeInvalid_ByDefault() {
            assertPropertyViolations(validator, config, "filePath1", NOT_VALID_FILE_PATH_MESSAGE);
        }

        @Test
        void shouldBeValid_WhenAllowingNulls() {
            assertNoPropertyViolations(validator, config, "filePath2");
        }

        @Test
        void shouldBeInvalid_WhenExplicitlyFalse() {
            assertPropertyViolations(validator, config, "filePath3", NOT_VALID_FILE_PATH_MESSAGE);
        }
    }

    @Nested
    class ShouldBeValid {

        @Test
        void whenValidPathToExistingFile() {
            var path = Fixtures.fixturePath("FilePathValidatorTest/test-file.txt").toString();
            config.setFilePath1(path);
            config.setFilePath2(path);
            config.setFilePath3(path);

            assertNoViolations(validator, config);
        }
    }

    @Nested
    class ShouldBeInvalid {

        @Test
        void whenPathIsNotValid() {
            var invalidPath = "this is not a valid path";
            config.setFilePath1(invalidPath);

            assertOnePropertyViolation(validator, config, "filePath1");
        }

        @Test
        void whenFileDoesNotExist() {
            config.setFilePath1("/this/does/not-exist/file.txt");
            config.setFilePath2("/neither/does/this.txt");

            assertOnePropertyViolation(validator, config, "filePath1");
            assertOnePropertyViolation(validator, config, "filePath2");
        }

        @Test
        void whenPathExists_ButIsDirectory() {
            var path = Fixtures.fixturePath("FilePathValidatorTest").toString();
            config.setFilePath1(path);

            assertOnePropertyViolation(validator, config, "filePath1");
        }

        @Test
        void whenInvalidPathExceptionThrown() {
            config.setFilePath1("\0");  // "Nul" character is not allowed in paths

            assertOnePropertyViolation(validator, config, "filePath1");
        }
    }

    @Getter
    @Setter
    private static class Config {

        @FilePath
        private String filePath1;

        @FilePath(allowNull = true)
        private String filePath2;

        @SuppressWarnings("DefaultAnnotationParam")
        @FilePath(allowNull = false)
        private String filePath3;
    }
}
