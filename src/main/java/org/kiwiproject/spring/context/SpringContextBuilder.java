package org.kiwiproject.spring.context;

import static com.google.common.base.Preconditions.checkState;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for easily constructing Spring ApplicationContext instances using either XML or annotation-based
 * configuration. The generated ApplicationContext instance is built by creating a parent context, which contains
 * registered singleton beans, and then a child context. This allows specific singletons, e.g., a data source or a
 * Dropwizard configuration object, to be accessible from the child context. The parent context beans can be referenced
 * in either XML or annotation configurations.
 * <p>
 * The methods return an instance of this class, so they can be changed together. Once the configuration is
 * set up, call {@link #build()} to create the ApplicationContext.
 * <p>
 * By default, this builder registers JVM shutdown hooks on the created Spring contexts.
 * This is convenient for standalone applications, but can cause premature shutdown of Spring-managed resources
 * (such as a {@code DataSource} and {@code EntityManagerFactory}) when the Spring context is managed by an
 * external lifecycle manager (for example, Dropwizard {@code Managed} objects). In those cases, call
 * {@link #withoutShutdownHooks()} and close the Spring context explicitly as part of the application's
 * shutdown sequence.
 */
public class SpringContextBuilder {

    private final Map<String, Object> parentContextBeans;
    private final List<Class<?>> annotatedClasses;
    private final List<String> configLocations;

    private boolean registerShutdownHooks;

    /**
     * Create a context builder.
     */
    public SpringContextBuilder() {
        parentContextBeans = new LinkedHashMap<>();
        annotatedClasses = new ArrayList<>();
        configLocations = new ArrayList<>();
        registerShutdownHooks = true;
    }

    /**
     * Adds the specified bean to the parent context.
     *
     * @param name the bean name
     * @param bean the bean instance
     * @return the builder instance
     */
    public SpringContextBuilder addParentContextBean(String name, Object bean) {
        parentContextBeans.put(name, bean);
        return this;
    }

    /**
     * Adds an annotation-based Spring {@code @Configuration} class.
     *
     * @param aClass class containing Spring configuration. Should be a class annotated with {@code @Configuration}.
     * @return the builder instance
     */
    public SpringContextBuilder addAnnotationConfiguration(Class<?> aClass) {
        checkConfigLocationsIsEmpty();
        annotatedClasses.add(aClass);
        return this;
    }

    /**
     * Adds multiple annotation-based Spring {@code @Configuration} classes.
     *
     * @param classes classes containing Spring configuration. Should be classes annotated with {@code @Configuration}.
     * @return the builder instance
     */
    public SpringContextBuilder withAnnotationConfigurations(Class<?>... classes) {
        checkConfigLocationsIsEmpty();
        Collections.addAll(annotatedClasses, classes);
        return this;
    }

    private void checkConfigLocationsIsEmpty() {
        checkState(configLocations.isEmpty(),
                "XML config locations have already been specified - annotated classes cannot be added!");
    }

    /**
     * Add a Spring XML configuration location.
     *
     * @param location the XML config location, e.g. {@code applicationContext.xml}
     * @return the builder instance
     */
    public SpringContextBuilder addXmlConfigLocation(String location) {
        checkAnnotatedClassesIsEmpty();
        configLocations.add(location);
        return this;
    }

    /**
     * Add multiple Spring XML configuration locations
     *
     * @param locations the XML config locations, e.g. {@code applicationContext-core.xml},
     *                  {@code applicationContext-dao.xml}
     * @return the builder instance
     */
    public SpringContextBuilder withXmlConfigLocations(String... locations) {
        checkAnnotatedClassesIsEmpty();
        Collections.addAll(configLocations, locations);
        return this;
    }

    private void checkAnnotatedClassesIsEmpty() {
        checkState(annotatedClasses.isEmpty(),
                "Annotated classes have already been specified - XML config locations cannot be added!");
    }

    /**
     * Disables registration of JVM shutdown hooks on the parent and child Spring {@link ApplicationContext} instances.
     * <p>
     * This is recommended when the Spring context is managed by an external lifecycle manager (for example,
     * Dropwizard {@code Managed} objects). In such environments, registering shutdown hooks can cause the Spring
     * context (and resources such as a {@code DataSource} and/or {@code EntityManagerFactory}) to be closed
     * prematurely during application shutdown.
     *
     * @return the builder instance
     */
    public SpringContextBuilder withoutShutdownHooks() {
        this.registerShutdownHooks = false;
        return this;
    }

    /**
     * Generate the ApplicationContext.
     *
     * @return the ApplicationContext defined by this builder
     */
    public ApplicationContext build() {
        var parent = buildParentApplicationContext();
        return buildContext(parent);
    }

    private ApplicationContext buildParentApplicationContext() {
        var parent = new AnnotationConfigApplicationContext();
        parent.refresh();
        var beanFactory = parent.getBeanFactory();
        parentContextBeans.forEach(beanFactory::registerSingleton);
        registerShutdownHookIfEnabled(parent);
        parent.start();
        return parent;
    }

    private ApplicationContext buildContext(ApplicationContext parent) {
        if (annotatedClasses.isEmpty()) {
            return buildXmlContext(parent);
        }

        return buildAnnotationContext(parent);
    }

    private ApplicationContext buildAnnotationContext(ApplicationContext parent) {
        var annotationContext = new AnnotationConfigApplicationContext();
        annotationContext.setParent(parent);
        annotatedClasses.forEach(annotationContext::register);
        annotationContext.refresh();
        registerShutdownHookIfEnabled(annotationContext);
        annotationContext.start();
        return annotationContext;
    }

    private ApplicationContext buildXmlContext(ApplicationContext parent) {
        var xmlContext = new ClassPathXmlApplicationContext();
        xmlContext.setParent(parent);
        xmlContext.setConfigLocations(configLocations.toArray(new String[0]));
        xmlContext.refresh();
        registerShutdownHookIfEnabled(xmlContext);
        xmlContext.start();
        return xmlContext;
    }

    private void registerShutdownHookIfEnabled(ConfigurableApplicationContext context) {
        if (registerShutdownHooks) {
            context.registerShutdownHook();
        }
    }
}
