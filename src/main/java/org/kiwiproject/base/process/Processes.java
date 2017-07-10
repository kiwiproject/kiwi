package org.kiwiproject.base.process;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Static utilities for working with processes.
 */
@UtilityClass
public class Processes {

    public static Process launch(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            return builder.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Error launching command: " + command, e);
        }
    }

    public static Process launch(String... command) {
        return launch(Arrays.asList(command));
    }

    public static boolean killForcibly(Process process, long timeout, TimeUnit unit)
            throws InterruptedException {
        return process.destroyForcibly().waitFor(timeout, unit);
    }
}
