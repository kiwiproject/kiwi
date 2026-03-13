package org.kiwiproject.dropwizard.lifecycle;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.util.concurrent.Runnables;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import lombok.experimental.UtilityClass;
import org.kiwiproject.util.function.KiwiConsumers;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
     * an ActiveMQ {@code PooledConnectionFactory} (which has {@code start} and {@code stop} methods) you can
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
     * a monitoring class that needs to start when the application starts, you can ensure that it happens
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

    /**
     * Creates an instance of type {@code T} using the given {@code supplier}, attaches it to the
     * given Dropwizard {@code lifecycle} with the given start and stop actions, and returns the
     * created instance.
     * <p>
     * Useful when you need to create, manage, and retain a reference to an object that does not
     * implement {@link Managed}. For example:
     * <p>
     * {@code var client = KiwiDropwizardLifecycles.manageAndReturn(lifecycle, () -> new Client(config), Client::start, Client::close);}
     *
     * @param lifecycle   the lifecycle to manage
     * @param supplier    creates the instance to manage
     * @param startAction the action to run on the instance when Dropwizard starts the application
     * @param stopAction  the action to run on the instance when Dropwizard stops the application
     * @param <T>         the type of the managed instance
     * @return the created instance
     * @throws IllegalArgumentException if any argument is null
     * @throws IllegalStateException if the supplier returns null
     */
    public static <T> T manageAndReturn(LifecycleEnvironment lifecycle,
                                        Supplier<T> supplier,
                                        Consumer<T> startAction,
                                        Consumer<T> stopAction) {
        checkArgumentNotNull(lifecycle, "lifecycle must not be null");
        checkArgumentNotNull(supplier, "supplier must not be null");
        checkArgumentNotNull(startAction, "startAction must not be null");
        checkArgumentNotNull(stopAction, "stopAction must not be null");

        T instance = supplier.get();
        checkState(nonNull(instance), "supplier must not return null");

        manage(lifecycle,
                () -> startAction.accept(instance),
                () -> stopAction.accept(instance));

        return instance;
    }

    /**
     * Creates an instance of type {@code T} using the given {@code supplier}, attaches it to the
     * given Dropwizard {@code lifecycle} with the given start action (and a no-op stop action),
     * and returns the created instance.
     * <p>
     * Useful when you need to create, manage, and retain a reference to an object that does not
     * implement {@link Managed} and only requires a start action. For example:
     * <p>
     * {@code var monitor = KiwiDropwizardLifecycles.manageOnlyStartAndReturn(lifecycle, Monitor::new, Monitor::start);}
     *
     * @param lifecycle   the lifecycle to manage
     * @param supplier    creates the instance to manage
     * @param startAction the action to run on the instance when Dropwizard starts the application
     * @param <T>         the type of the managed instance
     * @return the created instance
     * @throws IllegalArgumentException if any argument is null
     * @throws IllegalStateException if the supplier returns null
     */
    public static <T> T manageOnlyStartAndReturn(LifecycleEnvironment lifecycle,
                                                 Supplier<T> supplier,
                                                 Consumer<T> startAction) {
        return manageAndReturn(lifecycle, supplier, startAction, KiwiConsumers.noOp());
    }

    /**
     * Creates an instance of type {@code T} using the given {@code supplier}, attaches it to the
     * given Dropwizard {@code lifecycle} with the given stop action (and a no-op start action),
     * and returns the created instance.
     * <p>
     * Useful when you need to create, manage, and retain a reference to an object that does not
     * implement {@link Managed} and only requires a stop action. For example:
     * <p>
     * {@code var client = KiwiDropwizardLifecycles.manageOnlyStopAndReturn(lifecycle, () -> new Client(config), Client::close);}
     *
     * @param lifecycle  the lifecycle to manage
     * @param supplier   creates the instance to manage
     * @param stopAction the action to run on the instance when Dropwizard stops the application
     * @param <T>        the type of the managed instance
     * @return the created instance
     * @throws IllegalArgumentException if any argument is null
     * @throws IllegalStateException if the supplier returns null
     */
    public static <T> T manageOnlyStopAndReturn(LifecycleEnvironment lifecycle,
                                                Supplier<T> supplier,
                                                Consumer<T> stopAction) {
        return manageAndReturn(lifecycle, supplier, KiwiConsumers.noOp(), stopAction);
    }
}
