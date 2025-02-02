package org.kiwiproject.concurrent;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple task implementation that provides a runnable and supplier interface method and a counter that can
 * be checked for number of completed executions. It simulates a task that takes some time to complete.
 */
@Slf4j
class ConcurrentTask {

    private static final KiwiEnvironment ENV = new DefaultEnvironment();

    private final String name;
    private final AtomicInteger counter;
    private final long durationMillis;

    private RuntimeException exceptionToThrow;

    ConcurrentTask(String name) {
        this(name, Duration.ofMillis(10));
    }

    ConcurrentTask(String name, Duration duration) {
        this.name = name;
        this.counter = new AtomicInteger();
        this.durationMillis = duration.toMillis();
    }

    ConcurrentTask withException(RuntimeException exceptionToThrow) {
        this.exceptionToThrow = exceptionToThrow;
        return this;
    }

    void run() {
        supply();
    }

    Integer supply() {
        LOG.debug("executing concurrent task {} with duration of: {}ms", name, durationMillis);
        try {
            var startTime = System.nanoTime();
            performWait();
            long endTime = System.nanoTime();
            long elapsed = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            var completionStatus = isNull(exceptionToThrow) ? "successfully" : "exceptionally";
            LOG.debug("performed task {} {} in: {}ms", name, completionStatus, elapsed);

            var updatedCount = counter.incrementAndGet();

            if (nonNull(exceptionToThrow)) {
                throw exceptionToThrow;
            }

            return updatedCount;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.debug("Wait interrupted for task {}", name, e);
        }

        return counter.get();
    }

    Integer getCurrentCount() {
        return counter.get();
    }

    private void performWait() throws InterruptedException {
        ENV.sleep(durationMillis);
    }
}
