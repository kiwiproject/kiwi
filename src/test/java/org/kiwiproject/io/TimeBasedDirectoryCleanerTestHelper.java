package org.kiwiproject.io;

import static java.util.stream.Collectors.toList;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class TimeBasedDirectoryCleanerTestHelper {

    private final Path cleanerPath;

    public TimeBasedDirectoryCleanerTestHelper(Path path) {
        this.cleanerPath = path;
    }

    public void createDirectoriesWithFiles(int start, int end) {
        IntStream.rangeClosed(start, end)
                .forEach(value -> newFolderInTempFolderWithFiles("folder" + value));
    }

    private void newFolderInTempFolderWithFiles(String folderName) {
        try {
            var newFolder = cleanerPath.resolve(folderName);
            Files.createDirectory(newFolder);

            var numFilesToCreate = ThreadLocalRandom.current().nextInt(1, 5);
            IntStream.rangeClosed(1, numFilesToCreate).forEach(value -> newFileInFolder(newFolder, value));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void newFileInFolder(Path folder, int value) {
        var fileName = "file" + value;
        var file = folder.resolve(fileName).toFile();
        try {
            FileUtils.writeStringToFile(file, fileName, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<File> filesInTempFolder() throws IOException {
        try (var paths = Files.list(cleanerPath)) {
            return paths.map(Path::toFile).collect(toList());
        }
    }
}
