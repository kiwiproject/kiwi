package org.kiwiproject.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.secondToLast;

import io.dropwizard.jdbi.args.OptionalArgumentFactory;
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

    @Test
    void shouldGetPathComponents() {
        var pathComponents = Jars.getPathComponents(OptionalArgumentFactory.class);
        LOG.trace("pathComponents: {}", pathComponents);

        var version = secondToLast(pathComponents);
        int indexOfIo = pathComponents.indexOf("io");

        assertThat(first(pathComponents)).isEqualTo("");
        assertThat(pathComponents.get(indexOfIo + 1)).isEqualTo("dropwizard");
        assertThat(pathComponents.get(indexOfIo + 2)).isEqualTo("dropwizard-jdbi");
        assertThat(pathComponents.get(indexOfIo + 3)).isEqualTo(version);
        assertThat(pathComponents.get(indexOfIo + 4)).isEqualTo("dropwizard-jdbi-" + version + ".jar");
    }

    @Test
    void shouldLogExceptionAndReturnEmptyList_WhenHandlingExceptionsGettingPathComponents() {
        var securityException = new SecurityException("Access Denied!");

        var list = Jars.logExceptionAndReturnEmptyList(securityException, OptionalArgumentFactory.class);

        assertThat(list).isEmpty();
    }

    @Test
    void shouldGetPath() {
        var path = Jars.getPath(OptionalArgumentFactory.class).orElse(null);
        LOG.trace("path: {}", path);

        assertThat(path)
                .isNotNull()
                .startsWith("/")
                .contains("io/dropwizard/dropwizard-jdbi")
                .endsWith(".jar");
    }

    @Test
    void shouldGetDirectoryPath() {
        var dirPath = Jars.getDirectoryPath(OptionalArgumentFactory.class).orElse(null);
        LOG.trace("dirPath: {}", dirPath);

        var jarPath = Jars.getPath(OptionalArgumentFactory.class).orElseThrow();
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