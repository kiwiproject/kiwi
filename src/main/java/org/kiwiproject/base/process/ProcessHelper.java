package org.kiwiproject.base.process;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for working with processes. Use this instead of {@link Processes} if you need to be able to easily
 * mock process related things.
 */
public class ProcessHelper {

    public Process launch(List<String> command) {
        return Processes.launch(command);
    }

    public Process launch(String... command) {
        return Processes.launch(command);
    }

    public boolean killForcibly(Process process, long timeout, TimeUnit unit)
            throws InterruptedException {
        return Processes.killForcibly(process, timeout, unit);
    }

}
