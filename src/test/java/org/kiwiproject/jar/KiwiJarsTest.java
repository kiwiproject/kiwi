package org.kiwiproject.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.secondToLast;

import com.google.common.base.Ticker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.junit.jupiter.ClearBoxTest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

@DisplayName("Jars")
@Slf4j
class KiwiJarsTest {

    /**
     * Use Guava's {@link Ticker} class as the "classInJar" for these tests.
     */
    private static final Class<Ticker> CLASS_IN_JAR = Ticker.class;

    @Test
    void shouldGetPathComponents() {
        var pathComponents = KiwiJars.getPathComponents(CLASS_IN_JAR);
        LOG.trace("pathComponents: {}", pathComponents);

        var version = secondToLast(pathComponents);
        int indexOfCom = pathComponents.indexOf("com");

        // Guava's maven coordinates (groupId:artifactId:version) are: com.google.guava:guava:<version>
        // Therefore the JAR file path will contain com/google/guava/guava/<version>/guava-<version>.jar
        // So, the path of Guava version 28.2-jre therefore must include:
        // com/google/guava/guava/28.2-jre/guava-28.2-jre.jar

        assertThat(first(pathComponents)).isEmpty();
        assertThat(pathComponents.get(indexOfCom + 1)).isEqualTo("google");
        assertThat(pathComponents.get(indexOfCom + 2)).isEqualTo("guava");
        assertThat(pathComponents.get(indexOfCom + 3)).isEqualTo("guava");
        assertThat(pathComponents.get(indexOfCom + 4)).isEqualTo(version);
        assertThat(pathComponents.get(indexOfCom + 5)).isEqualTo("guava-" + version + ".jar");
    }

    @Test
    void shouldLogExceptionAndReturnEmptyList_WhenHandlingExceptionsGettingPathComponents() {
        var securityException = new SecurityException("Access Denied!");

        var list = KiwiJars.logExceptionAndReturnEmptyList(securityException, CLASS_IN_JAR);

        assertThat(list).isEmpty();
    }

    @Test
    void shouldGetPath() {
        var path = KiwiJars.getPath(CLASS_IN_JAR).orElse(null);
        LOG.trace("path: {}", path);

        assertThat(path)
                .isNotNull()
                .startsWith("/")
                .contains("com/google/guava/guava")
                .endsWith(".jar");
    }

    @Test
    void shouldGetDirectoryPath() {
        var dirPath = KiwiJars.getDirectoryPath(CLASS_IN_JAR).orElse(null);
        LOG.trace("dirPath: {}", dirPath);

        var jarPath = KiwiJars.getPath(CLASS_IN_JAR).orElseThrow();
        var jarFile = new File(jarPath);

        assertThat(dirPath).isEqualTo(jarFile.getParent());
    }

    @Nested
    class Joined {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_WhenGivenEmptyOrNullList(List<String> list) {
            assertThat(KiwiJars.joined(list)).isEmpty();
        }

        @Test
        void shouldReturnItem_WhenGivenSingleElementList() {
            assertThat(KiwiJars.joined(List.of("item"))).contains("item");
        }

        @Test
        void shouldJoinItems_WhenGivenMultipleElementList() {
            assertThat(KiwiJars.joined(List.of("part1", "part2", "part3")))
                    .contains("part1" + File.separator + "part2" + File.separator + "part3");
        }

        @Test
        void shouldReturnItemsWithLeadingFileSeparator_WhenListHasEmptyFirstElement() {
            assertThat(KiwiJars.joined(List.of("", "part1", "part2", "part3")))
                    .contains(File.separator + "part1" + File.separator + "part2" + File.separator + "part3");
        }
    }

    @Nested
    class ReadSingleValueFromJarManifest {

        @Test
        void shouldReadAnActualValueFromTheManifest_WithGivenClassLoaderAndPredicate() throws IOException {
            var classLoader = new URLClassLoader(
                    new URL[] {Fixtures.fixturePath("KiwiJars/KiwiTestSample.jar").toUri().toURL()},
                    this.getClass().getClassLoader()
            );

            var value = KiwiJars.readSingleValueFromJarManifest(classLoader, "Sample-Attribute", url -> url.getPath().contains("KiwiTestSample"));

            assertThat(value).isPresent().hasValue("the-value");
        }

        @Test
        void shouldReturnOptionalEmptyIfManifestFileCouldNotBeFound() {
            var value = KiwiJars.readSingleValueFromJarManifest(this.getClass().getClassLoader(), "foo", url -> false);

            assertThat(value).isEmpty();
        }

        @Test
        void shouldReturnOptionalEmptyIfValueCouldNotBeFoundInManifest_UsingClassLoaderAndPredicate() {
            var value = KiwiJars.readSingleValueFromJarManifest(this.getClass().getClassLoader(), "foo", url -> true);

            assertThat(value).isEmpty();
        }

        @Test
        void shouldReturnOptionalEmptyIfValueCouldNotBeFoundInManifest_UsingClassLoader() {
            var value = KiwiJars.readSingleValueFromJarManifest(this.getClass().getClassLoader(), "foo");

            assertThat(value).isEmpty();
        }

        @Test
        void shouldReturnOptionalEmptyIfValueCouldNotBeFoundInManifest_UsingDefaultClassLoader() {
            var value = KiwiJars.readSingleValueFromJarManifest("foo");

            assertThat(value).isEmpty();
        }

    }

    @Nested
    class ReadValuesFromJarManifest {

        @Test
        void shouldReadActualValuesFromTheManifest_WithGivenClassLoaderAndPredicate() throws IOException {
            var classLoader = new URLClassLoader(
                    new URL[] {Fixtures.fixturePath("KiwiJars/KiwiTestSample.jar").toUri().toURL()},
                    this.getClass().getClassLoader()
            );

            var values = KiwiJars.readValuesFromJarManifest(classLoader,
                    url -> url.getPath().contains("KiwiTestSample"), "Sample-Attribute", "Main-Class");

            assertThat(values).contains(
                    entry("Sample-Attribute", "the-value"),
                    entry("Main-Class", "KiwiTestClass")
            );
        }

        @Test
        void shouldReturnEmptyMapIfValuesCouldNotBeFoundInManifest_UsingClassLoaderAndPredicate() {
            var values = KiwiJars.readValuesFromJarManifest(this.getClass().getClassLoader(), url -> true, "foo");

            assertThat(values).isEmpty();
        }

        @Test
        void shouldReturnEmptyMapIfValuesCouldNotBeFoundInManifest_UsingClassLoader() {
            var values = KiwiJars.readValuesFromJarManifest(this.getClass().getClassLoader(), "foo");

            assertThat(values).isEmpty();
        }

        @Test
        void shouldReturnEmptyMapIfValuesCouldNotBeFoundInManifest_UsingDefaultClassLoader() {
            var values = KiwiJars.readValuesFromJarManifest("foo");

            assertThat(values).isEmpty();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldReturnEmptyMap_ForInvalidClassLoader() {
            ClassLoader classLoader = null;
            var values = KiwiJars.readValuesFromJarManifest(classLoader, "Main-Class");

            assertThat(values).isEmpty();
        }

        @Test
        void shouldReturnEmptyMap_WhenManifestCannotBeFound() {
            var values = KiwiJars.readValuesFromJarManifest(this.getClass().getClassLoader(),
                    url -> false,  // ensures manifest won't be found
                    "Main-Class");

            assertThat(values).isEmpty();
        }
    }

    @Nested
    class ReadFirstManifestOrNull {

        @ClearBoxTest
        void shouldReturnNull_WhenUrlsIsEmpty() {
            var manifest = KiwiJars.readFirstManifestOrNull(List.of());
            assertThat(manifest).isNull();
        }

        @ClearBoxTest
        void shouldReturnNull_WhenUrlIsInvalid() throws MalformedURLException {
            var urls = List.of(URI.create("jar:file:/tmp/12345/jars/foo-1.0.0.jar!/META-INF/MANIFEST.MF").toURL());

            var manifest = KiwiJars.readFirstManifestOrNull(urls);
            assertThat(manifest).isNull();
        }
    }
}
