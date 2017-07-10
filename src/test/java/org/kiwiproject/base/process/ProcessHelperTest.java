package org.kiwiproject.base.process;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assume.assumeTrue;

public class ProcessHelperTest {

    private ProcessHelper processes;

    @Before
    public void setUp() {
        assumeTrue("This test should only run on UNIX-like operating systems"
                        + " (because we currently use UnixProcesses in several methods here)",
                SystemUtils.IS_OS_UNIX);
        processes = new ProcessHelper();
    }

    @Test
    public void testLaunch_UsingList() {
        Process proc = processes.launch(newArrayList("sleep", "2"));
        assertProcessIsAliveThenKill(proc);
    }

    @Test
    public void testLaunch_UsingVarargs() {
        Process proc = processes.launch("sleep", "2");
        assertProcessIsAliveThenKill(proc);
    }

    @Test
    public void testLaunch_WithMalformedCommand() {
        assertThatThrownBy(() -> processes.launch("bad", "foo", "bar"))
                .isExactlyInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(IOException.class)
                .hasMessageContaining("Error launching command")
                .hasMessageContaining("bad")
                .hasMessageContaining("foo")
                .hasMessageContaining("bar");
    }

    private void assertProcessIsAliveThenKill(Process theProcess) {
        try {
            assertThat(theProcess.isAlive()).isTrue();
        } finally {
            if (nonNull(theProcess)) {
                int pid = UnixProcesses.processId(theProcess);
                killProcessQuietly(pid);
            }
        }
    }

    private void killProcessQuietly(int pid) {
        try {
            new ProcessBuilder("kill", String.valueOf(pid)).start();
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void testKillForcibly() throws IOException, InterruptedException {
        Process process = new ProcessBuilder("sleep", "30").start();
        assertThat(process.isAlive()).isTrue();

        boolean killedBeforeTimeout = processes.killForcibly(process, 2, TimeUnit.SECONDS);
        assertThat(killedBeforeTimeout).isTrue();
    }

}