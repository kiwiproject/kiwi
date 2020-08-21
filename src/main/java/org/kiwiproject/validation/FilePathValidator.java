package org.kiwiproject.validation;

import static java.util.Objects.isNull;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;

/**
 * Validates that a string value is a valid path, exists, and is a regular file (not a directory).
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

        try {
            var file = Path.of(value).toFile();
            return file.exists() && file.isFile();
        } catch (Exception e) {
            LOG.trace("Exception thrown validating path [{}]", value, e);
            return false;
        }
    }
}
