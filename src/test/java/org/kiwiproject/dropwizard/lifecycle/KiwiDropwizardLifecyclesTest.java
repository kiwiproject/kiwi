package org.kiwiproject.dropwizard.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;

import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.Runnables;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("KiwiDropwizardLifecycles")
class KiwiDropwizardLifecyclesTest {

    @Test
    void shouldManageStart() throws Exception {
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
        assertThat(managed.isStarted()).isTrue();
    }

    @Test
    void shouldManageStop() throws Exception {
        var lifecycle = new LifecycleEnvironment(new MetricRegistry());

        var stopCalled = new AtomicBoolean();
        Runnable stopAction = () -> stopCalled.set(true);

        KiwiDropwizardLifecycles.manage(lifecycle, Runnables.doNothing(), stopAction);

        var managedObjects = lifecycle.getManagedObjects();
        assertThat(managedObjects).hasSize(1);

        // Make sure it's started, so that AbstractLifeCycle#stop will stop it
        var managed = first(managedObjects);
        managed.start();
        assertThat(managed.isStarted())
                .describedAs("Precondition: Managed object must be started")
                .isTrue();

        managed.stop();
        assertThat(stopCalled.get()).isTrue();
        assertThat(managed.isStopped()).isTrue();
    }

    @Test
    void shouldManageOnlyStart() throws Exception {
        var lifecycle = new LifecycleEnvironment(new MetricRegistry());

        var startCalled = new AtomicBoolean();
        Runnable startAction = () -> startCalled.set(true);

        KiwiDropwizardLifecycles.manageOnlyStart(lifecycle, startAction);

        var managedObjects = lifecycle.getManagedObjects();
        assertThat(managedObjects).hasSize(1);

        var managed = first(managedObjects);
        assertThat(managed.isStarted()).isFalse();

        managed.start();
        assertThat(startCalled.get()).isTrue();
        assertThat(managed.isStarted()).isTrue();
    }

    @Test
    void shouldManageOnlyStop() throws Exception {
        var lifecycle = new LifecycleEnvironment(new MetricRegistry());

        var stopCalled = new AtomicBoolean();
        Runnable stopAction = () -> stopCalled.set(true);

        KiwiDropwizardLifecycles.manageOnlyStop(lifecycle, stopAction);

        var managedObjects = lifecycle.getManagedObjects();
        assertThat(managedObjects).hasSize(1);

        // Make sure it's started, so that AbstractLifeCycle#stop will stop it
        var managed = first(managedObjects);
        managed.start();
        assertThat(managed.isStarted())
                .describedAs("Precondition: Managed object must be started")
                .isTrue();

        managed.stop();
        assertThat(stopCalled.get()).isTrue();
        assertThat(managed.isStopped()).isTrue();
    }
}
