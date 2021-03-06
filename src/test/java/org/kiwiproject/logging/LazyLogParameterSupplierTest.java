package org.kiwiproject.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.logging.LazyLogParameterSupplier.lazy;
import static org.kiwiproject.util.MiscConstants.POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("LazyLogParameterSupplier")
@Slf4j
class LazyLogParameterSupplierTest {

    private AtomicBoolean wasCalled;

    @BeforeEach
    void setUp() {
        wasCalled = new AtomicBoolean(false);
    }

    @Test
    void shouldGetFromSupplier_WhenLogLevel_IsActive() {
        LOG.info("The result at {} level is {}", "info", lazy(this::expensiveToCreateString));
        assertThat(wasCalled)
                .describedAs(POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING)
                .isTrue();
    }

    @Test
    void shouldGetFromSupplier_WhenLogLevel_IsActive_AndSupplierReturnsNull() {
        LOG.info("The result at {} level is {}", "info", lazy(this::expensiveReturningNull));
        assertThat(wasCalled)
                .describedAs(POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING)
                .isTrue();
    }

    @Test
    void shouldGetFromSupplier_WhenLogLevel_IsActive_AndSupplierReturnsComplexObject() {
        LOG.debug("The result at {} level is {}", "debug", lazy(this::expensiveToCreateThing));
        assertThat(wasCalled)
                .describedAs(POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING)
                .isTrue();
    }

    @Test
    void shouldNotGetFromSupplier_WhenLogLevel_IsNotActive() {
        LOG.trace("The result at {} level is {}", "trace", lazy(this::expensiveToCreateNumber));
        assertThat(wasCalled)
                .describedAs(POSSIBLE_DROPWIZARD_LOGGING_FAILURE_WARNING)
                .isFalse();
    }

    private String expensiveToCreateString() {
        wasCalled.set(true);
        return "42";
    }

    private Object expensiveReturningNull() {
        wasCalled.set(true);
        return null;
    }

    private long expensiveToCreateNumber() {
        wasCalled.set(true);
        return 42;
    }

    private Thing expensiveToCreateThing() {
        wasCalled.set(true);
        return new Thing(42L, "The Blob", "It's blobby!");
    }

    @Value
    static class Thing {
        Long id;
        String name;
        String description;
    }
}
