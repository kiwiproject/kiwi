package org.kiwiproject.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

@DisplayName("KiwiPaths")
class KiwiPathsTest {

    @Test
    void testPathFromFileName_WhenValidRelativeFileName() {
        var path = KiwiPaths.pathFromResourceName("logback-test.xml");

        assertThat(path.toFile()).exists();
        assertThat(path.toString()).endsWith("/logback-test.xml");
    }

    @Test
    void testPathFromFileName_WhenFileDoesNotExist() {
        assertThatThrownBy(() -> KiwiPaths.pathFromResourceName("does-not-exist"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInternalCreateAndThrowUncheckedException() {
        var resourceName = "foo.txt";
        var syntaxException = new URISyntaxException(resourceName, "the reason");

        var unchecked = KiwiPaths.newUncheckedException(resourceName, syntaxException);

        assertThat(unchecked)
                .hasMessage("Cannot convert to URI: " + resourceName)
                .hasCause(syntaxException);
    }
}
