package org.kiwiproject.dropwizard.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;

import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.Runnables;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnstableApiUsage")
@DisplayName("KiwiDropwizardLifecycles")
class KiwiDropwizardLifecyclesTest {

    @Test
    void testManageStart() throws Exception {
        var lifecycle = new LifecycleEnvironment(new MetricRegistry());

        var startCalled = new AtomicBoolean();
        Runnable startAction = () -> startCalled.set(true);

        KiwiDropwizardLifecycles.manage(lifecycle, startAction, Runnables.doNothing());

        var managedObjects = lifecycle.getManagedObjects();
        assertThat(managedObjects).hasSize(1);

        var managed = first(managedObjects);
        assertThat(managed.isStarted()).isFalse();

        managed.start();
        assertThat(startCalled.get()).isTrue();
    }

    @Test
    void testManageStop() throws Exception {
        var lifecycle = new LifecycleEnvironment(new MetricRegistry());

        var stopCalled = new AtomicBoolean();
        Runnable stopAction = () -> stopCalled.set(true);

        KiwiDropwizardLifecycles.manage(lifecycle, Runnables.doNothing(), stopAction);

        var managedObjects = lifecycle.getManagedObjects();
        assertThat(managedObjects).hasSize(1);

        var managed = first(managedObjects);
        managed.start();
        assertThat(managed.isStarted()).isTrue();

        managed.stop();
        assertThat(stopCalled.get()).isTrue();
        assertThat(managed.isStopped()).isTrue();
    }
}
