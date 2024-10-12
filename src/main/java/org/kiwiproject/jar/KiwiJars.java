package org.kiwiproject.jar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * Utilities for working with Java JAR files.
 */
@Slf4j
@UtilityClass
public class KiwiJars {

    private static final String QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator);

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

            return newArrayList(decodedJarPath.trim().split(QUOTED_FILE_SEPARATOR));
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
     * Get the value of a main attribute from the manifest file (if found) in the current class loader.
     *
     * @param mainAttributeName The name of the main attribute to resolve
     * @return an {@link Optional} containing the resolved value or {@code Optional.empty()} if not
     */
    public static Optional<String> readSingleValueFromJarManifest(String mainAttributeName) {
        return readSingleValueFromJarManifest(KiwiJars.class.getClassLoader(), mainAttributeName, null);
    }

    /**
     * Get the value of a main attribute from the manifest file (if found) from the given class loader.
     *
     * @param classLoader       The class loader to find the manifest file to search
     * @param mainAttributeName The name of the main attribute to resolve
     * @return an {@link Optional} containing the resolved value or {@code Optional.empty()} if not
     */
    public static Optional<String> readSingleValueFromJarManifest(ClassLoader classLoader,
                                                                  String mainAttributeName) {
        return readSingleValueFromJarManifest(classLoader, mainAttributeName, null);
    }

    /**
     * Get the value of a main attribute from the manifest file (if found) from the given class loader
     * and filtering manifests using the given Predicate.
     *
     * @param classLoader       The class loader to find the manifest file to search
     * @param mainAttributeName The name of the main attribute to resolve
     * @param manifestFilter    An optional filter that can be used to filter down manifest files if there are more than one.
     * @return an {@link Optional} containing the resolved value or {@code Optional.empty()} if not
     * @implNote If this code is called from a "fat-jar" with a single manifest file, then the filter predicate is unnecessary.
     * The predicate filter is really only necessary if there are multiple jars loaded in the classpath all containing manifest files.
     */
    @SuppressWarnings("java:S2259")
    public static Optional<String> readSingleValueFromJarManifest(ClassLoader classLoader,
                                                                  String mainAttributeName,
                                                                  @Nullable Predicate<URL> manifestFilter) {
        try {
            var manifest = findManifestOrNull(classLoader, manifestFilter);
            if (isNull(manifest)) {
                return Optional.empty();
            }

            var value = manifest.getMainAttributes().getValue(mainAttributeName);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            LOG.warn("Unable to get main attribute {} from JAR manifest", mainAttributeName, e);
            return Optional.empty();
        }
    }

    /**
     * Get the values of the given main attribute names from the manifest (if found) from the current class loader.
     *
     * @param mainAttributeNames an array of main attribute names to resolve from the manifest
     * @return a {@code Map<String, String>} of resolved main attributes
     */
    public static Map<String, String> readValuesFromJarManifest(String... mainAttributeNames) {
        return readValuesFromJarManifest(KiwiJars.class.getClassLoader(), null, mainAttributeNames);
    }

    /**
     * Get the values of the given main attribute names from the manifest (if found) from the given class loader.
     *
     * @param classLoader           the classloader to search for manifest files in
     * @param mainAttributeNames    an array of names to resolve from the manifest
     * @return a {@code Map<String, String>} of resolved main attributes
     */
    public static Map<String, String> readValuesFromJarManifest(ClassLoader classLoader,
                                                                String... mainAttributeNames) {
        return readValuesFromJarManifest(classLoader, null, mainAttributeNames);
    }

    /**
     * Get the values of the given main attribute names from the manifest (if found) from the given class loader
     * and filtering manifests using the given Predicate.
     *
     * @param classLoader           the classloader to search for manifest files in
     * @param manifestFilter        a predicate filter used to limit which jar files to search for a manifest file
     * @param mainAttributeNames    an array of names to resolve from the manifest
     * @return a {@code Map<String, String>} of resolved main attributes
     * @implNote If this code is called from a "fat-jar" with a single manifest file, then the filter predicate is unnecessary.
     * The predicate filter is really only necessary if there are multiple jars loaded in the classpath all containing manifest files.
     */
    public static Map<String, String> readValuesFromJarManifest(ClassLoader classLoader,
                                                                @Nullable Predicate<URL> manifestFilter,
                                                                String... mainAttributeNames) {

        var uniqueNames = Set.of(mainAttributeNames);
        return readMainAttributesFromJarManifest(classLoader,
                entry -> uniqueNames.contains(entry.getKey()),
                manifestFilter);
    }

    /**
     * Get the values of all main attributes from the manifest (if found) from the current class loader.
     *
     * @return a {@code Map<String, String>} of all main attributes
     */
    public static Map<String, String> readAllMainAttributesFromJarManifest() {
        return readAllMainAttributesFromJarManifest(KiwiJars.class.getClassLoader(), null);
    }

    /**
     * Get the values of all main attributes from the manifest (if found) from the given class loader.
     *
     * @param classLoader the classloader to search for manifest files in
     * @return a {@code Map<String, String>} of all main attributes
     */
    public static Map<String, String> readAllMainAttributesFromJarManifest(ClassLoader classLoader) {
        return readAllMainAttributesFromJarManifest(classLoader, null);
    }

    /**
     * Get the values of all main attributes from the manifest (if found) from the given class loader
     * and filtering manifests using the given Predicate.
     *
     * @param classLoader    the classloader to search for manifest files in
     * @param manifestFilter a predicate filter used to limit which jar files to search for a manifest file
     * @return a {@code Map<String, String>} of all main attributes
     */
    public static Map<String, String> readAllMainAttributesFromJarManifest(ClassLoader classLoader,
                                                                           @Nullable Predicate<URL> manifestFilter) {
        return readMainAttributesFromJarManifest(classLoader, entry -> true, manifestFilter);
    }

    private static Map<String, String> readMainAttributesFromJarManifest(ClassLoader classLoader,
                                                                         Predicate<Map.Entry<String, String>> entryPredicate,
                                                                         @Nullable Predicate<URL> manifestFilter) {
        try {
            var manifest = findManifestOrNull(classLoader, manifestFilter);
            if (isNull(manifest)) {
                return Map.of();
            }

            return manifest.getMainAttributes()
                    .entrySet()
                    .stream()
                    .map(KiwiJars::toEntryOfStringToString)
                    .filter(entryPredicate)
                    .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
            
        } catch (Exception e) {
            LOG.warn("Unable to get main attributes from JAR manifest", e);
            return Map.of();
        }
    }

    private static Map.Entry<String, String> toEntryOfStringToString(Map.Entry<Object, Object> e) {
        return Map.entry(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
    }

    @Nullable
    private static Manifest findManifestOrNull(ClassLoader classLoader,
                                               @Nullable Predicate<URL> manifestFilter) throws IOException {

        var urls = findManifestUrls(classLoader, manifestFilter);
        if (isNullOrEmpty(urls)) {
            LOG.warn("There are no manifest URLs!" +
                    " The ClassLoader may have returned no resources or the manifestFilter did not match any URLs.");
            return null;
        }

        return readFirstManifestOrNull(urls);
    }

    private static List<URL> findManifestUrls(ClassLoader classLoader,
                                              @Nullable Predicate<URL> manifestFilter) throws IOException {
        if (isNull(manifestFilter)) {
            var manifestUrl = Optional.ofNullable(classLoader.getResource("META-INF/MANIFEST.MF"));
            return manifestUrl.map(List::of).orElseGet(List::of);
        }

        var urlIterator = classLoader.getResources("META-INF/MANIFEST.MF").asIterator();
        Iterable<URL> urlIterable = () -> urlIterator;

        return StreamSupport
                .stream((urlIterable).spliterator(), false)
                .filter(manifestFilter)
                .toList();
    }

    @VisibleForTesting
    @Nullable
    static Manifest readFirstManifestOrNull(List<URL> urls) {
        LOG.trace("Using manifest URL(s): {}", urls);

        var manifest = urls.stream()
                .map(KiwiJars::readManifest)
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(null);

        if (isNull(manifest)) {
            LOG.warn("Unable to get a manifest using URLs: {}", urls);
        }

        return manifest;
    }

    private static Optional<Manifest> readManifest(URL url) {
        try (var in = url.openStream()) {
            return Optional.of(new Manifest(in));
        } catch (Exception e) {
            LOG.warn("Unable to read manifest from URL: {}", url, e);
            return Optional.empty();
        }
    }
}
