package org.kiwiproject.base.process;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("OsCommand")
class OsCommandTest {

    @Test
    void shouldReturnPartsAsArray() {
        OsCommand command = () -> List.of("sdk", "install", "java", "20.0.2-zulu");
        assertThat(command.partsAsArray()).containsExactly("sdk", "install", "java", "20.0.2-zulu");
    }
}
