package org.kiwiproject.dropwizard.jdbi3;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.core.spi.JdbiPlugin;

import java.util.Optional;

/**
 * Utilities for JDBI 3.
 */
@UtilityClass
@Slf4j
class Jdbi3Helpers {

    /**
     * Get a plugin instance for the given class name.
     *
     * @param pluginClassName the plugin class name
     * @return an Optional containing a plugin instance, or empty Optional if plugin class not available or an error
     * occurs. If an error occurs, that fact is logged at WARN level.
     */
    static Optional<JdbiPlugin> getPluginInstance(String pluginClassName) {
        var result = isPluginAvailable(pluginClassName);

        if (isTrue(result.getLeft())) {
            try {
                var pluginClass = result.getRight();
                var pluginInstance = (JdbiPlugin) pluginClass.getDeclaredConstructor().newInstance();
                return Optional.of(pluginInstance);
            } catch (Exception e) {
                LOG.warn("Error instantiating plugin for class: {}", pluginClassName);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private static Pair<Boolean, Class<?>> isPluginAvailable(String pluginClassName) {
        try {
            var pluginClass = Class.forName(pluginClassName);
            return Pair.of(true, pluginClass);
        } catch (ClassNotFoundException e) {
            return Pair.of(false, null);
        }
    }
}
