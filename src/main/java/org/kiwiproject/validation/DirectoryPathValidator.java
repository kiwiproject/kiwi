package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static org.kiwiproject.logging.LazyLogParameterSupplier.lazy;
import static org.kiwiproject.validation.InternalKiwiValidators.containsNulCharacter;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.nio.file.Path;

/**
 * Validates that a string value is a valid path, exists, and is a directory.
 * <p>
 * Optionally, this validator will verify that the directory is readable and/or writable.
 * <p>
 * As mentioned in the documentation for {@link DirectoryPath}, this may also attempt to create the directory
 * if it does not already exist, which may be an unexpected side-effect.
 *
 * @implNote Please read the implementation note in {@link DirectoryPath} regarding the possibility of
 * <a href="https://owasp.org/www-community/attacks/Path_Traversal">Path Traversal</a> attacks.
 */
@Slf4j
public class DirectoryPathValidator implements ConstraintValidator<DirectoryPath, String> {

    private DirectoryPath directoryPath;

    @Override
    public void initialize(DirectoryPath constraintAnnotation) {
        this.directoryPath = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return directoryPath.allowNull();
        }

        try {
            var file = Path.of(value).toFile();
            LOG.trace("Validating access to directory: {}", lazy(file::getAbsolutePath));

            var exists = file.exists();
            var directoryCreated = createDirectoryIfNecessary(file, exists);
            var directoryExistsOrWasCreated = (exists || directoryCreated);

            return directoryExistsOrWasCreated &&
                    file.isDirectory() &&
                    isReadableOrIgnoresEnsureReadable(file) &&
                    isWritableOrIgnoresEnsureReadable(file);

        } catch (Exception e) {
            var nulCharacterMessage = containsNulCharacter(value) ? " Path contains Nul character!" : "";
            LOG.warn("Exception thrown validating path.{}", nulCharacterMessage, e);
            return false;
        }
    }

    private boolean createDirectoryIfNecessary(File file, boolean exists) {
        if (exists || !directoryPath.mkdirs()) {
            return false;
        }

        var absolutePath = file.getAbsolutePath();
        LOG.info("Directory does not exist and 'mkdirs' option is true. Creating directory: {}", absolutePath);
        var created = file.mkdirs();
        if (!created) {
            LOG.error("Unable to create directory: {}", absolutePath);
        }

        return created;
    }

    private boolean isReadableOrIgnoresEnsureReadable(File file) {
        return !directoryPath.ensureReadable() || file.canRead();
    }

    private boolean isWritableOrIgnoresEnsureReadable(File file) {
        return !directoryPath.ensureWritable() || file.canWrite();
    }
}
