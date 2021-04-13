package org.kiwiproject.stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("IntStreams")
class IntStreamsTest {

    @Nested
    class IndicesOf {

        @Test
        void shouldReturnEmptyStringWhenGivenEmptyList() {
            var stream = IntStreams.indicesOf(List.of());
            var indices = stream.boxed().collect(toList());

            assertThat(indices).isEmpty();
        }

        @Test
        void shouldReturnIndices() {
            var strings = List.of("a", "b", "c", "d");

            var stream = IntStreams.indicesOf(strings);
            var indices = stream.boxed().collect(toList());

            assertThat(indices).containsExactly(0, 1, 2, 3);
        }
    }

}
