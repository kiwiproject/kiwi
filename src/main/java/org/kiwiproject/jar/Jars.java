package org.kiwiproject.jar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Utilities for working with Java JAR files.
 */
@Slf4j
@UtilityClass
public class Jars {

    /**
     * Get the path components of the JAR file path that the given class lives in, or an empty list if the path
     * components could not be obtained.
     *
     * @param classInJar the class which resides in the JAR you want to find
     * @return a <em>mutable</em> list of path components if found, or an <em>immutable</em> empty list if not found
     * or an exception is thrown attempting to get the path
     */
    public static List<String> getPathComponents(Class<?> classInJar) {
        checkArgumentNotNull(classInJar, "classInJar cannot be null");

        try {
            var codeSource = classInJar.getProtectionDomain().getCodeSource();
            checkNotNull(codeSource, "Code source for %s is null", classInJar);

            var encodedJarPath = codeSource.getLocation().getPath();
            var decodedJarPath = URLDecoder.decode(encodedJarPath, StandardCharsets.UTF_8);

            return newArrayList(decodedJarPath.trim().split(File.separator));
        } catch (Exception e) {
            return logExceptionAndReturnEmptyList(e, classInJar);
        }
    }

    @VisibleForTesting
    static List<String> logExceptionAndReturnEmptyList(Exception ex, Class<?> classInJar) {
        LOG.error("Error getting Jar path components for {}! (turn on DEBUG logging to see stack traces)", classInJar);
        LOG.debug("Exception related to {}:", classInJar, ex);
        return emptyList();
    }

    /**
     * Get the path of the JAR file that the given class lives in, or an empty {@link Optional} if the path
     * could not be obtained for any reason.
     *
     * @param classInJar the class which resides in the JAR you want to find
     * @return optional containing the path if found, or an empty optional
     */
    public static Optional<String> getPath(Class<?> classInJar) {
        var pathComponents = getPathComponents(classInJar);
        return joined(pathComponents);
    }

    /**
     * Get the <em>directory</em> path of the JAR file that the given class lives in, or an empty {@link Optional}
     * if the path could not be obtained for any reason.
     *
     * @param classInJar the class which resides in the JAR you want to find
     * @return optional containing the directory path if found, or an empty optional
     */
    public static Optional<String> getDirectoryPath(Class<?> classInJar) {
        var pathComponents = getPathComponents(classInJar);
        checkState(isNotNullOrEmpty(pathComponents), "there must be at least one path component!");
        return joined(pathComponents.subList(0, pathComponents.size() - 1));
    }

    @VisibleForTesting
    static Optional<String> joined(List<String> parts) {
        if (isNullOrEmpty(parts)) {
            return Optional.empty();
        }

        return Optional.of(String.join(File.separator, parts));
    }
}
