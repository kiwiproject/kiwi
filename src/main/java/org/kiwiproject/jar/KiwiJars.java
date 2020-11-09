package org.kiwiproject.jar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.Manifest;
import java.util.stream.StreamSupport;

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

    /**
     * Resolves a given entry name from the manifest file (if found) in the current class loader.
     *
     * @param manifestEntryName The name of the property to resolve
     * @return an {@link Optional} containing the resolved value or {@code Optional.empty()} if not
     */
    public static Optional<String> resolveSingleValueFromJarManifest(String manifestEntryName) {
        return resolveSingleValueFromJarManifest(KiwiJars.class.getClassLoader(), manifestEntryName, null);
    }

    /**
     * Resolves a given entry name from the manifest file (if found) from the given class loader.
     *
     * @param classLoader       The class loader to find the manifest file to search
     * @param manifestEntryName The name of the property to resolve
     * @param manifestFilter    An optional filter that can be used to filter down manifest files if there are more than one.
     * @return an {@link Optional} containing the resolved value or {@code Optional.empty()} if not
     * @implNote If this code is called from a "fat-jar" with single manifest file, then the filter predicate is not needed. The predicate filter is
     * really only needed if there are multiple jars loaded in the classpath all containing manifest files.
     */
    @SuppressWarnings("java:S2259")
    public static Optional<String> resolveSingleValueFromJarManifest(ClassLoader classLoader, String manifestEntryName, Predicate<URL> manifestFilter) {
        try {

            List<URL> urls;

            if (isNull(manifestFilter)) {
                var manifestUrl = Optional.ofNullable(classLoader.getResource("META-INF/MANIFEST.MF"));
                urls = manifestUrl.map(List::of).orElse(null);
            } else {
                var urlIterator = classLoader.getResources("META-INF/MANIFEST.MF").asIterator();
                Iterable<URL> urlIterable = () -> urlIterator;

                urls = StreamSupport
                        .stream((urlIterable).spliterator(), false)
                        .filter(manifestFilter)
                        .collect(toUnmodifiableList());
            }

            LOG.trace("Using manifest URL(s): {}", urls);

            if (isNullOrEmpty(urls)) {
                return Optional.empty();
            }

            return urls.stream().map(url -> readEntry(url, manifestEntryName))
                    .flatMap(Optional::stream)
                    .findFirst();

        } catch (Exception e) {
            LOG.warn("Unable to locate {} from JAR", manifestEntryName, e);
        }

        return Optional.empty();
    }

    private Optional<String> readEntry(URL url, String manifestEntryName) {
        try (var in = url.openStream()) {
            var manifest = new Manifest(in);
            return readEntry(manifest, manifestEntryName);
        } catch (Exception e) {
            LOG.warn("Unable to read manifest", e);
        }

        return Optional.empty();
    }

    private static Optional<String> readEntry(Manifest manifest, String manifestEntryName) {
        return Optional.ofNullable(manifest.getMainAttributes().getValue(manifestEntryName));
    }

    /**
     * Resolves all of the given entry names from the manifest (if found) from the current class loader.
     *
     * @param manifestEntryNames an array of names to resolve from the manifest
     * @return a {@code Map<String,String>} of resolved entries
     */
    public static Map<String, String> resolveValuesFromJarManifest(String... manifestEntryNames) {
        return resolveValuesFromJarManifest(KiwiJars.class.getClassLoader(), null, manifestEntryNames);
    }

    /**
     * Resolves all of the given entry names from the manifest (if found) from the given class loader.
     *
     * @param classLoader           the classloader to search for manifest files in
     * @param manifestFilter        a predicate filter used to limit which jar files to search for a manifest file
     * @param manifestEntryNames    an array of names to resolve from the manifest
     * @return a {@code Map<String,String>} of resolved entries
     * @implNote If this code is called from a "fat-jar" with single manifest file, then the filter predicate is not needed. The predicate filter is
     * really only needed if there are multiple jars loaded in the classpath all containing manifest files.
     */
    public static Map<String, String> resolveValuesFromJarManifest(ClassLoader classLoader, Predicate<URL> manifestFilter, String... manifestEntryNames) {
        var entries = new HashMap<String, String>();

        Arrays.stream(manifestEntryNames).forEach(manifestEntryName -> {
            var entry = resolveSingleValueFromJarManifest(classLoader, manifestEntryName, manifestFilter);
            entry.ifPresent(value -> entries.put(manifestEntryName, value));
        });

        return entries;
    }
}
