package org.kiwiproject.jar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;

/**
 * Utilities for working with Java JAR files.
 */
@Slf4j
@UtilityClass
public class KiwiJars {

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

    public static Optional<String> resolveSingleValueFromJarManifest(String manifestEntryName) {
        return resolveSingleValueFromJarManifest(KiwiJars.class.getClassLoader(), manifestEntryName);
    }

    public static Optional<String> resolveSingleValueFromJarManifest(ClassLoader classLoader, String manifestEntryName) {
        try {
            var url = classLoader.getResource("META-INF/MANIFEST.MF");

            LOG.trace("Using manifest URL: {}", url);

            if (isNull(url)) {
                return Optional.empty();
            }

            try (var in = url.openStream()) {
                var manifest = new Manifest(in);
                return readEntry(manifest, manifestEntryName);
            }
        } catch (Exception e) {
            LOG.warn("Unable to locate {} from JAR", manifestEntryName, e);
        }

        return Optional.empty();
    }

    public static Map<String, String> resolveValuesFromJarManifest(String... manifestEntryNames) {
        return resolveValuesFromJarManifest(KiwiJars.class.getClassLoader(), manifestEntryNames);
    }

    public static Map<String, String> resolveValuesFromJarManifest(ClassLoader classLoader, String... manifestEntryNames) {
        var entries = new HashMap<String, String>();

        try {
            var url = classLoader.getResource("META-INF/MANIFEST.MF");

            LOG.trace("Using manifest URL: {}", url);

            if (isNull(url)) {
                return entries;
            }

            try (var in = url.openStream()) {
                var manifest = new Manifest(in);

                Arrays.stream(manifestEntryNames).forEach(manifestEntryName -> {
                    var entry = readEntry(manifest, manifestEntryName);
                    entry.ifPresent(value -> entries.put(manifestEntryName, value));
                });
            }
        } catch (Exception e) {
            LOG.warn("Unable to locate values from JAR", e);
        }

        return entries;
    }

    private static Optional<String> readEntry(Manifest manifest, String manifestEntryName) {
        return Optional.ofNullable(manifest.getMainAttributes().getValue(manifestEntryName));
    }
}
