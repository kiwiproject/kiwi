package org.kiwiproject.dropwizard.lifecycle;

import com.google.common.util.concurrent.Runnables;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import lombok.experimental.UtilityClass;

/**
 * Provides utilities related to the Dropwizard lifecycle.
 */
@UtilityClass
public class KiwiDropwizardLifecycles {

    /**
     * Creates a Dropwizard {@link Managed} whose start action is {@code startAction} and whose stop action
     * is {@code stopAction}, and attaches it to the given Dropwizard {@code lifecycle}.
     * <p>
     * Useful when you have some external object that has start and stop methods, but you don't want to clutter your
     * code by creating an anonymous inner class just to specify the start and stop actions. For example, if you have
     * an ActiveMQ {@code PooledConnectionFactory} (which has {@code start} and {@code stop} methods) you can simply
     * call this method:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, () -> factory.start(), () -> factory.stop());}
     * <p>
     * To make the code cleaner, use method references:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, factory::start, factory::stop);}
     *
     * @param lifecycle   the lifecycle to manage
     * @param startAction the action to run when Dropwizard starts the application
     * @param stopAction  the action to run when Dropwizard stops the application
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

    /**
     * Creates a Dropwizard {@link Managed} whose start action is {@code startAction},
     * and attaches it to the given Dropwizard {@code lifecycle}.
     * <p>
     * Useful when you have some external object that has a start method, but you don't want to clutter your
     * code by creating an anonymous inner class just to specify the start action. For example, if you have
     * a monitoring class that needs to start when the application starts, you can ensure that happens
     * using code like:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, () -> monitor.start());}
     * <p>
     * To make the code cleaner, use method references:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, monitor::start);}
     *
     * @param lifecycle   the lifecycle to manage
     * @param startAction the action to run when Dropwizard starts the application
     */
    public static void manageOnlyStart(LifecycleEnvironment lifecycle, Runnable startAction) {
        manage(lifecycle, startAction, Runnables.doNothing());
    }

    /**
     * Creates a Dropwizard {@link Managed} whose stop action is {@code stopAction},
     * and attaches it to the given Dropwizard {@code lifecycle}.
     * <p>
     * Useful when you have some external object that has a stop method, but you don't want to clutter your
     * code by creating an anonymous inner class just to specify the stop action. For example, if you have
     * an HTTP {@code Client} class that needs to stop when the application shuts down to ensure resources
     * are properly closed, you can ensure that happens using code like:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, () -> client.close());}
     * <p>
     * To make the code cleaner, use method references:
     * <p>
     * {@code KiwiDropwizardLifecycles.manage(lifecycle, client::close);}
     *
     * @param lifecycle   the lifecycle to manage
     * @param stopAction  the action to run when Dropwizard stops the application
     */
    public static void manageOnlyStop(LifecycleEnvironment lifecycle, Runnable stopAction) {
        manage(lifecycle, Runnables.doNothing(), stopAction);
    }
}
