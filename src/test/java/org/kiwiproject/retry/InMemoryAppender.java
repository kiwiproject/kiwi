package org.kiwiproject.retry;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * A logback appender that stores logging events in an in-memory map.
 * <p>
 * This is for testing purposes only, and is not at all intended for production use!
 */
public class InMemoryAppender extends AppenderBase<ILoggingEvent> {

    private final AtomicInteger messageOrder;
    private final ConcurrentMap<Integer, ILoggingEvent> eventMap;

    public InMemoryAppender() {
        this.messageOrder = new AtomicInteger();
        this.eventMap = new ConcurrentHashMap<>();
    }

    /**
     * Assert this appender has the expected number of logging events, and if the assertion succeeds, return a
     * list containing those events.
     */
    List<ILoggingEvent> assertNumberOfLoggingEventsAndGet(int expectedEventCount) {
        var events = getOrderedEvents();
        assertThat(events).hasSize(expectedEventCount);
        return events;
    }

    @Override
    protected synchronized void append(ILoggingEvent eventObject) {
        eventMap.put(messageOrder.incrementAndGet(), eventObject);
    }

    synchronized void clearEvents() {
        messageOrder.set(0);
        eventMap.clear();
    }

    List<ILoggingEvent> getOrderedEvents() {
        return getOrderedEventStream().collect(toList());
    }

    List<String> getOrderedEventMessages() {
        return getOrderedEventStream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(toList());
    }

    private Stream<ILoggingEvent> getOrderedEventStream() {
        return eventMap.values()
                .stream()
                .sorted(comparing(ILoggingEvent::getTimeStamp));
    }
}
