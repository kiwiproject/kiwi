package org.kiwiproject.dropwizard.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.collect.KiwiLists.first;

import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.Runnables;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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

    @Nested
    class ManageAndReturn {

        @Test
        void shouldRequireNonNullArguments() {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            Consumer<String> action = s -> {};

            assertAll(
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiDropwizardLifecycles.manageAndReturn(null, () -> "x", action, action))
                            .withMessage("lifecycle must not be null"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiDropwizardLifecycles.manageAndReturn(lifecycle, null, action, action))
                            .withMessage("supplier must not be null"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiDropwizardLifecycles.manageAndReturn(lifecycle, () -> "x", null, action))
                            .withMessage("startAction must not be null"),
                    () -> assertThatIllegalArgumentException()
                            .isThrownBy(() -> KiwiDropwizardLifecycles.manageAndReturn(lifecycle, () -> "x", action, null))
                            .withMessage("stopAction must not be null"),
                    () -> assertThatIllegalStateException()
                            .isThrownBy(() -> KiwiDropwizardLifecycles.manageAndReturn(lifecycle, () -> null, action, action))
                            .withMessage("supplier must not return null")
            );
        }

        @Test
        void shouldReturnCreatedInstance() {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            var instance = new AtomicBoolean();

            var result = KiwiDropwizardLifecycles.manageAndReturn(lifecycle, () -> instance, s -> {}, s -> {});

            assertThat(result).isSameAs(instance);
        }

        @Test
        void shouldCallStartAction() throws Exception {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            var startCalled = new AtomicBoolean();
            var stopCalled = new AtomicBoolean();

            KiwiDropwizardLifecycles.manageAndReturn(lifecycle,
                    AtomicBoolean::new,
                    instance -> startCalled.set(true),
                    instance -> stopCalled.set(true));

            first(lifecycle.getManagedObjects()).start();

            assertThat(startCalled.get()).isTrue();
            assertThat(stopCalled.get()).isFalse();
        }

        @Test
        void shouldCallStopAction() throws Exception {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            var startCalled = new AtomicBoolean();
            var stopCalled = new AtomicBoolean();

            KiwiDropwizardLifecycles.manageAndReturn(lifecycle,
                    AtomicBoolean::new,
                    instance -> startCalled.set(true),
                    instance -> stopCalled.set(true));

            var managed = first(lifecycle.getManagedObjects());
            managed.start();
            managed.stop();

            assertThat(startCalled.get()).isTrue();
            assertThat(stopCalled.get()).isTrue();
        }
    }

    @Nested
    class ManageOnlyStartAndReturn {

        @Test
        void shouldReturnCreatedInstance() {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            var instance = new AtomicBoolean();

            var result = KiwiDropwizardLifecycles.manageOnlyStartAndReturn(lifecycle, () -> instance, s -> {});

            assertThat(result).isSameAs(instance);
        }

        @Test
        void shouldCallStartAction_AndNotStopAction() throws Exception {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            var startCalled = new AtomicBoolean();

            KiwiDropwizardLifecycles.manageOnlyStartAndReturn(lifecycle,
                    AtomicBoolean::new,
                    instance -> startCalled.set(true));

            var managed = first(lifecycle.getManagedObjects());
            managed.start();
            assertThat(startCalled.get()).isTrue();

            managed.stop();
            assertThat(startCalled.get())
                    .describedAs("startCalled should still be true and stop should have been a no-op")
                    .isTrue();
        }
    }

    @Nested
    class ManageOnlyStopAndReturn {

        @Test
        void shouldReturnCreatedInstance() {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            var instance = new AtomicBoolean();

            var result = KiwiDropwizardLifecycles.manageOnlyStopAndReturn(lifecycle, () -> instance, s -> {});

            assertThat(result).isSameAs(instance);
        }

        @Test
        void shouldCallStopAction_AndNotStartAction() throws Exception {
            var lifecycle = new LifecycleEnvironment(new MetricRegistry());
            var stopCalled = new AtomicBoolean();

            KiwiDropwizardLifecycles.manageOnlyStopAndReturn(lifecycle,
                    AtomicBoolean::new,
                    instance -> stopCalled.set(true));

            var managed = first(lifecycle.getManagedObjects());
            managed.start();
            assertThat(stopCalled.get()).isFalse();

            managed.stop();
            assertThat(stopCalled.get()).isTrue();
        }
    }
}
