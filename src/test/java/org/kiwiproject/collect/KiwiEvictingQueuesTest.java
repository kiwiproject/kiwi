package org.kiwiproject.collect;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Queue;
import java.util.stream.IntStream;

@DisplayName("KiwiEvictingQueues")
class KiwiEvictingQueuesTest {

    @Nested
    class SynchronizedEvictingQueue {

        @Test
        void shouldCreateEvictingQueueWithMaxOf100Items() {
            var queue = KiwiEvictingQueues.synchronizedEvictingQueue();
            IntStream.rangeClosed(1, 150).forEach(queue::add);
            assertThat(queue).hasSize(100);
        }

        @Test
        void shouldEvictLeastRecentItems() {
            Queue<Integer> queue = KiwiEvictingQueues.synchronizedEvictingQueue();
            IntStream.rangeClosed(1, 150).forEach(queue::add);

            var queueItems = new ArrayList<>(queue);
            var expectedItems = IntStream.rangeClosed(51, 150).boxed().collect(toList());
            assertThat(queueItems).containsExactlyElementsOf(expectedItems);
        }
    }

    @Nested
    class WithMaxSize {

        @Test
        void shouldCreateEvictingQueueWithSpecifiedMaxItems() {
            var queue = KiwiEvictingQueues.synchronizedEvictingQueue(200);
            IntStream.rangeClosed(1, 250).forEach(queue::add);
            assertThat(queue).hasSize(200);
        }
    }
}
