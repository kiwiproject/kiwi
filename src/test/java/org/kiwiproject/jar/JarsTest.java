package org.kiwiproject.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.secondToLast;

import com.google.common.base.Ticker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.io.File;
import java.util.List;

@DisplayName("Jars")
@Slf4j
class JarsTest {

    /**
     * Use Guava's {@link Ticker} class as the "classInJar" for these tests.
     */
    private static final Class<Ticker> CLASS_IN_JAR = Ticker.class;

    @Test
    void shouldGetPathComponents() {
        var pathComponents = Jars.getPathComponents(CLASS_IN_JAR);
        LOG.trace("pathComponents: {}", pathComponents);

        var version = secondToLast(pathComponents);
        int indexOfCom = pathComponents.indexOf("com");

        // Guava's maven coordinates (groupId:artifactId:version) are: com.google.guava:guava:<version>
        // Therefore the JAR file path will contain com/google/guava/guava/<version>/guava-<version>.jar
        // So, the path of Guava version 28.2-jre therefore must include:
        // com/google/guava/guava/28.2-jre/guava-28.2-jre.jar

        assertThat(first(pathComponents)).isEqualTo("");
        assertThat(pathComponents.get(indexOfCom + 1)).isEqualTo("google");
        assertThat(pathComponents.get(indexOfCom + 2)).isEqualTo("guava");
        assertThat(pathComponents.get(indexOfCom + 3)).isEqualTo("guava");
        assertThat(pathComponents.get(indexOfCom + 4)).isEqualTo(version);
        assertThat(pathComponents.get(indexOfCom + 5)).isEqualTo("guava-" + version + ".jar");
    }

    @Test
    void shouldLogExceptionAndReturnEmptyList_WhenHandlingExceptionsGettingPathComponents() {
        var securityException = new SecurityException("Access Denied!");

        var list = Jars.logExceptionAndReturnEmptyList(securityException, CLASS_IN_JAR);

        assertThat(list).isEmpty();
    }

    @Test
    void shouldGetPath() {
        var path = Jars.getPath(CLASS_IN_JAR).orElse(null);
        LOG.trace("path: {}", path);

        assertThat(path)
                .isNotNull()
                .startsWith("/")
                .contains("com/google/guava/guava")
                .endsWith(".jar");
    }

    @Test
    void shouldGetDirectoryPath() {
        var dirPath = Jars.getDirectoryPath(CLASS_IN_JAR).orElse(null);
        LOG.trace("dirPath: {}", dirPath);

        var jarPath = Jars.getPath(CLASS_IN_JAR).orElseThrow();
        var jarFile = new File(jarPath);

        assertThat(dirPath).isEqualTo(jarFile.getParent());
    }

    @Nested
    class Joined {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_WhenGivenEmptyOrNullList(List<String> list) {
            assertThat(Jars.joined(list)).isEmpty();
        }

        @Test
        void shouldReturnItem_WhenGivenSingleElementList() {
            assertThat(Jars.joined(List.of("item"))).contains("item");
        }

        @Test
        void shouldJoinItems_WhenGivenMultipleElementList() {
            assertThat(Jars.joined(List.of("part1", "part2", "part3")))
                    .contains("part1" + File.separator + "part2" + File.separator + "part3");
        }

        @Test
        void shouldReturnItemsWithLeadingFileSeparator_WhenListHasEmptyFirstElement() {
            assertThat(Jars.joined(List.of("", "part1", "part2", "part3")))
                    .contains(File.separator + "part1" + File.separator + "part2" + File.separator + "part3");
        }
    }
}
