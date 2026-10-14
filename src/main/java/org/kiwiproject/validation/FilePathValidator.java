package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static org.kiwiproject.validation.InternalKiwiValidators.containsNulCharacter;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * Validates that a string value is a valid path, exists, and is a regular file (not a directory).
 * An empty or blank string is always invalid.
 *
 * @implNote Please read the implementation note in {@link FilePath} regarding the possibility of
 * <a href="https://owasp.org/www-community/attacks/Path_Traversal">Path Traversal</a> attacks.
 */
@Slf4j
public class FilePathValidator implements ConstraintValidator<FilePath, String> {

    private FilePath filePath;

    @Override
    public void initialize(FilePath constraintAnnotation) {
        this.filePath = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return filePath.allowNull();
        }

        // Empty and blank strings are never valid file paths. Be explicit rather than relying on java.io.File
        // behavior, which changed in JDK 25 (new File("") now means the current directory, JDK-8024695).
        if (value.isBlank()) {
            return false;
        }

        try {
            var file = Path.of(value).toFile();
            return file.exists() && file.isFile();
        } catch (Exception e) {
            var nulCharacterMessage = containsNulCharacter(value) ? " Path contains Nul character!" : "";
            LOG.warn("Exception thrown validating path.{}", nulCharacterMessage, e);
            return false;
        }
    }
}
