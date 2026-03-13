package org.kiwiproject.dropwizard.lifecycle;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import io.dropwizard.lifecycle.Managed;
import org.kiwiproject.io.KiwiIO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A Dropwizard {@link Managed} that manages the lifecycle of a Spring
 * {@link ConfigurableApplicationContext}, closing it (and optionally its
 * parent contexts) during application shutdown.
 * <p>
 * Use the static factory methods to create instances:
 * <ul>
 *   <li>{@link #notClosingParents(ConfigurableApplicationContext)} - closes only the given context</li>
 *   <li>{@link #closingParents(ConfigurableApplicationContext)} - closes the given context and walks
 *       up the parent chain, closing any parent contexts that are also
 *       {@link ConfigurableApplicationContext} instances</li>
 * </ul>
 * <p>
 * This is intended for use with {@link org.kiwiproject.spring.context.SpringContextBuilder}.
 * When using {@link org.kiwiproject.spring.context.SpringContextBuilder#withoutShutdownHooks()},
 * use {@link #closingParents(ConfigurableApplicationContext)} to ensure both the child and parent
 * contexts created by the builder are properly closed during shutdown.
 */
public class ManagedSpringContext implements Managed {

    private final ConfigurableApplicationContext context;
    private final boolean closeParents;

    private ManagedSpringContext(ConfigurableApplicationContext context, boolean closeParents) {
        checkArgumentNotNull(context, "context must not be null");
        this.context = context;
        this.closeParents = closeParents;
    }

    /**
     * Creates a new {@link ManagedSpringContext} that closes only the given context when stopped,
     * leaving any parent contexts open.
     *
     * @param context the Spring context to manage
     * @return a new ManagedSpringContext
     * @throws IllegalArgumentException if context is null
     */
    public static ManagedSpringContext notClosingParents(ConfigurableApplicationContext context) {
        return new ManagedSpringContext(context, false);
    }

    /**
     * Creates a new {@link ManagedSpringContext} that closes the given context and walks up the
     * parent chain, closing any parent contexts that are also
     * {@link ConfigurableApplicationContext} instances.
     * <p>
     * Use this when the Spring context was built with
     * {@link org.kiwiproject.spring.context.SpringContextBuilder#withoutShutdownHooks()}, which
     * creates a parent context that must be explicitly closed.
     *
     * @param context the Spring context to manage
     * @return a new ManagedSpringContext
     * @throws IllegalArgumentException if context is null
     */
    public static ManagedSpringContext closingParents(ConfigurableApplicationContext context) {
        return new ManagedSpringContext(context, true);
    }

    /**
     * No-op. The Spring context is expected to already be started when this managed object is created.
     */
    @Override
    public void start() {
        // no-op: the context is already started
    }

    /**
     * Closes the managed Spring context. If this instance was created with
     * {@link #closingParents(ConfigurableApplicationContext)}, also walks up the parent chain and
     * closes any parent contexts that are {@link ConfigurableApplicationContext} instances.
     */
    @Override
    public void stop() {
        if (closeParents) {
            closeContextAndParents(context);
        } else {
            KiwiIO.closeQuietly(context);
        }
    }

    private static void closeContextAndParents(ConfigurableApplicationContext context) {
        KiwiIO.closeQuietly(context);
        ApplicationContext parent = context.getParent();
        while (parent instanceof ConfigurableApplicationContext configurableParent) {
            KiwiIO.closeQuietly(configurableParent);
            parent = configurableParent.getParent();
        }
    }
}
