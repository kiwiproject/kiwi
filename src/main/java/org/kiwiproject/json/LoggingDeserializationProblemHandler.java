package org.kiwiproject.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

/**
 * A Jackson {@link DeserializationProblemHandler} that logs and keeps track of unknown properties during JSON
 * deserialization. Any unexpected/unknown properties are logged as warnings and stored in-memory in a
 * {@link ConcurrentMap}.
 * <p>
 * Optionally, you can supply a {@link BiConsumer} to the constructor if you want to be notified when unknown
 * properties are encountered. This consumer should be thread-safe!
 * <p>
 * Note that jackson-core and jackson-databind must be available at runtime.
 *
 * @implNote Currently the in-memory map will continue to grow unbounded. In the expected scenario, unknown properties
 * will be relatively rare so we don't expect huge numbers of them. In addition, only unique unknown properties are
 * stored. The {@link #clearUnexpectedPaths()} method is provided to allow clearing all unexpected paths, for example
 * read the unexpected paths for analytics and then clear them.
 */
@Slf4j
public class LoggingDeserializationProblemHandler extends DeserializationProblemHandler {

    private final ConcurrentMap<String, String> unexpectedPaths = new ConcurrentHashMap<>();
    private final BiConsumer<String, Class<?>> unknownPropertyConsumer;

    /**
     * Create a new instance with a no-op {@code unknownPropertyConsumer}.
     */
    public LoggingDeserializationProblemHandler() {
        this((propertyName, aClass) -> { /* no-op */});
    }

    /**
     * Create a new instance with the given {@code unknownPropertyConsumer}.
     *
     * @param unknownPropertyConsumer the consumer to be notified whenever unknown properties are encountered
     */
    public LoggingDeserializationProblemHandler(BiConsumer<String, Class<?>> unknownPropertyConsumer) {
        this.unknownPropertyConsumer = unknownPropertyConsumer;
    }

    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt,
                                         JsonParser p,
                                         JsonDeserializer<?> deserializer,
                                         Object beanOrClass,
                                         String propertyName) throws IOException {

        var path = p.getParsingContext().pathAsPointer().toString().replace("/", ".");
        var className = beanOrClass.getClass().getName();

        LOG.warn("Unable to deserialize path: '{}' for class: {}", path, className);

        p.skipChildren();

        var key = className + path;
        var val = className + " -> " + path;
        unexpectedPaths.putIfAbsent(key, val);

        notifyConsumer(beanOrClass, path);

        return true;  // to indicate the problem is "resolved"
    }

    private void notifyConsumer(Object beanOrClass, String path) {
        try {
            unknownPropertyConsumer.accept(path, beanOrClass.getClass());
        } catch (Exception e) {
            LOG.error("unknownPropertyConsumer threw exception", e);
        }
    }

    /**
     * Clear all unexpected path information.
     */
    public void clearUnexpectedPaths() {
        unexpectedPaths.clear();
    }

    /**
     * Return the <em>unique</em> unexpected paths in the format {@code className -> propertyName}.
     *
     * @return unexpected paths
     */
    public Set<String> getUnexpectedPaths() {
        return Set.copyOf(unexpectedPaths.values());
    }

    /**
     * Return the <em>unique</em> unexpected paths in the format {@code className.propertyName}.
     *
     * @return unexpected paths
     */
    public Set<String> getUnexpectedPropertyPaths() {
        return Set.copyOf(unexpectedPaths.keySet());
    }

    /**
     * Current total count of <em>unique</em> unknown properties.
     *
     * @return total count of unique unknown property paths
     */
    public long getUnknownPropertyCount() {
        return unexpectedPaths.size();
    }
}
