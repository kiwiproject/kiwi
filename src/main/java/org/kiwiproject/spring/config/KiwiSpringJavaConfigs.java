package org.kiwiproject.spring.config;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toCollection;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Set;

/**
 * Static utility methods for Spring Java-based configuration, i.e. Spring JavaConfig.
 */
@UtilityClass
public class KiwiSpringJavaConfigs {

    /**
     * Determines entity packages using specified entity classes. Typically this will be used when configuring a
     * Hibernate SessionFactory using Spring's LocalSessionFactoryBean.
     *
     * @param classes one or more entity classes
     * @return comma-separated list of packages
     * @implNote While this could be made into a generic utility, we have chosen to keep it named explicitly
     * so that it makes more sense when reading code in a Spring JavaConfig class. In addition, it is a very
     * specific use-case that isn't common.
     */
    public static String packagesToScanForEntities(Class<?>... classes) {
        checkArgumentNotNull(classes, "Null varargs array argument is not allowed");
        checkArgument(classes.length > 0, "At least one entity class must be specified");

        Set<String> packages = Arrays.stream(classes)
                .map(clazz -> clazz.getPackage().getName())
                .collect(toCollection(Sets::newTreeSet));

        return String.join(",", packages);
    }
}
