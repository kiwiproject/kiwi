package org.kiwiproject.base.process;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnixKillSignalTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testSignalNumbers() {
        assertThat(UnixKillSignal.SIGHUP.number()).isEqualTo("1");
        assertThat(UnixKillSignal.SIGINT.number()).isEqualTo("2");
        assertThat(UnixKillSignal.SIGQUIT.number()).isEqualTo("3");
        assertThat(UnixKillSignal.SIGKILL.number()).isEqualTo("9");
        assertThat(UnixKillSignal.SIGTERM.number()).isEqualTo("15");
    }

    @Test
    public void testStaticWithLeadingDash_WhenHasLeadingDash() {
        assertThat(UnixKillSignal.withLeadingDash("-15")).isEqualTo("-15");
    }

    @Test
    public void testStaticWithLeadingDash_WhenDoesNotHaveLeadingDash() {
        assertThat(UnixKillSignal.withLeadingDash("9")).isEqualTo("-9");
    }

    @Test
    public void testWithLeadingDash_WhenHasLeadingDash() {
        assertThat(UnixKillSignal.SIGHUP.withLeadingDash()).isEqualTo("-1");
    }

    @Test
    public void testWithLeadingDash_WhenDoesNotHaveLeadingDash() {
        assertThat(UnixKillSignal.SIGQUIT.withLeadingDash()).isEqualTo("-3");
    }

}