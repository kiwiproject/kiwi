package org.kiwiproject.base.process;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("KillSignal")
public class KillSignalTest {

    @ParameterizedTest
    @CsvSource({
            "SIGHUP,1",
            "SIGINT,2",
            "SIGQUIT,3",
            "SIGKILL,9",
            "SIGTERM,15",
    })
    public void testSignalNumbers(String killSignalName, String expectedNumber) {
        assertThat(KillSignal.valueOf(killSignalName).number()).isEqualTo(expectedNumber);
    }

    @Test
    public void testStaticWithLeadingDash_WhenHasLeadingDash() {
        assertThat(KillSignal.withLeadingDash("-15")).isEqualTo("-15");
    }

    @Test
    public void testStaticWithLeadingDash_WhenDoesNotHaveLeadingDash() {
        assertThat(KillSignal.withLeadingDash("9")).isEqualTo("-9");
    }

    @Test
    public void testWithLeadingDash_WhenHasLeadingDash() {
        assertThat(KillSignal.SIGHUP.withLeadingDash()).isEqualTo("-1");
    }

    @Test
    public void testWithLeadingDash_WhenDoesNotHaveLeadingDash() {
        assertThat(KillSignal.SIGQUIT.withLeadingDash()).isEqualTo("-3");
    }

}