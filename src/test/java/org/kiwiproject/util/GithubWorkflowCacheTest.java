package org.kiwiproject.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A "test" intended to help debug problems with the GitHub cache action. It only logs some information.
 */
@DisplayName("GithubWorkflowCache")
@Slf4j
@SuppressWarnings("java:S2699")
class GithubWorkflowCacheTest {

    private String userDir;

    @BeforeEach
    void setUp() {
        userDir = System.getProperty("user.home");
        LOG.info("Home directory: {}", System.getProperty("user.home"));
    }

    @Test
    void shouldDisplayHomeDirectory() throws IOException {
        logFilesInDirectory(Path.of(userDir));
    }

    @Test
    void shouldDisplayMavenCache() throws IOException {
        logFilesInDirectory(Path.of(userDir, ".m2"));
    }

    @Test
    void shouldDisplaySonarCache() throws IOException {
        logFilesInDirectory(Path.of(userDir, ".sonar"));
        logFilesInDirectory(Path.of(userDir, ".sonar", "cache"));
    }

    @Test
    void shouldDisplayEmbeddedMongoCache() throws IOException {
        logFilesInDirectory(Path.of(userDir, ".embedmongo"));
    }

    private static void logFilesInDirectory(Path directory) throws IOException {
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            LOG.info("Listing {}:", directory);
            Files.list(directory).forEach(path -> LOG.info("{}", path.toAbsolutePath()));
        } else {
            LOG.warn("Directory {} was not found!", directory.toAbsolutePath());
        }

        LOG.info("----");
    }
}
