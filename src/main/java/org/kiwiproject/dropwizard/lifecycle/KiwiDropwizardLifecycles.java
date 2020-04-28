package org.kiwiproject.dropwizard.lifecycle;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import lombok.experimental.UtilityClass;

/**
 * Provides utilities related to the Dropwizard lifecycle
 */
@UtilityClass
public class KiwiDropwizardLifecycles {

    /**
     * Creates a Dropwizard {@link Managed} whose start action is {@code startAction} and whose stop action
     * is {@code stopAction}, and attaches it to the given Dropwizard {@code lifecycle}.
     * <p>
     * Useful when you have some external object that has start and stop methods, but you don't want to clutter your
     * code by creating anonymous inner class just to specify the start and stop actions. For example if you have an
     * ActiveMQ {@code PooledConnectionFactory} (which has {@code start} and {@code stop} methods) you can simply
     * call this method:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, () -> factory.start(), () -> factory.stop());}
     * <p>
     * To make the code cleaner, use method references:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, factory::start, factory::stop);}
     */
    public static void manage(LifecycleEnvironment lifecycle, Runnable startAction, Runnable stopAction) {
        lifecycle.manage(new Managed() {
            @Override
            public void start() {
                startAction.run();
            }

            @Override
            public void stop() {
                stopAction.run();
            }
        });
    }
}
