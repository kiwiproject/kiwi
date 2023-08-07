package org.kiwiproject.logging;

import static java.util.Objects.isNull;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

/**
 * Provides lazy evaluation of one or more replacement parameters in a logging statement, for example when
 * using SLF4J. This is useful when one or more values might be relatively expensive to compute, such as
 * serializing an object to JSON for DEBUG logging but not wanting to incur to JSON serialization cost at
 * higher log levels.
 * <p>
 * While many log frameworks now provide a way to pass a {@link Supplier} as an argument, not all do (e.g. SLF4J
 * version 1.7.x and earlier) and not all provide as much flexibility as you might want in order to mix and match
 * lazy and non-lazy arguments. This simple class lets you mix and match lazy and non-lazy parameters whether the
 * logging API supports this or not.
 * <p>
 * To use, just statically import methods from {@link LazyLogParameterSupplier} and use them to wrap one or more
 * replacement parameters in the same logging statement. Note again you do not need to make all the
 * parameters lazy.
 * <p>
 * Examples:
 * <pre>
 * // Explicitly create the parameter supplier and pass it to the lazy method
 * Supplier&lt;String&gt; jsonSupplier = () -&gt; jsonHelper.toJson(thing);
 * LOG.debug("Thing {} took {} millis to fetch. JSON value: {}",
 *         thing.getId(), fetchMillis, lazy(jsonSupplier));
 *
 * // Pass a Supplier as a lambda to the lazy method for one or more parameters
 * LOG.debug("Thing {} took {} millis to fetch. JSON value: {}",
 *         thing.getId(), fetchMillis, lazy(() -&gt; jsonHelper.toJson(thing)));
 *
 * // If thingToJson converts the thing to JSON it can be a simple lambda...
 * LOG.debug("Thing {} took {} millis to fetch. JSON value: {}",
 *         thing.getId(), fetchMillis, lazy(() -&gt; thingToJson()));
 *
 * // ...or a method reference
 * LOG.debug("Thing {} took {} millis to fetch. JSON value: {}",
 *         thing.getId(), fetchMillis, lazy(this::thingToJson));
 * </pre>
 *
 * @implNote adapted from my
 * <a href="https://github.com/sleberknight/slf4j-lazy-params">slf4j-lazy-params</a> repository
 */
@UtilityClass
public class LazyLogParameterSupplier {

    /**
     * Delays execution of the {@code original} supplier in a log replacement value. This also permits some
     * replacement parameters to use delayed (lazy) execution while others can be regular parameters.
     * <p>
     * Example usage (assuming SLF4J API, and using static import and a method reference):
     * <pre>
     * LOG.debug("Foo JSON with id {} is: {}", foo.getId(), lazy(foo::toJson));
     * </pre>
     *
     * @param original supplier of a replacement value that might be expensive to compute, e.g. serialize an
     *                 object to JSON
     * @return a {@link Supplier} that wraps {@code original}
     */
    public static Supplier<Object> lazy(Supplier<Object> original) {
        return new ObjectSupplierWrapper(original);
    }

    /**
     * A {@link Supplier} that returns the original object, and whose toString method returns a null-safe
     * representation of the supplied object. This null-safe toString is what the logging frameworks will
     * ultimately insert into the logging output.
     */
    @AllArgsConstructor
    private static class ObjectSupplierWrapper implements Supplier<Object> {

        private final Supplier<Object> original;

        @Override
        public Object get() {
            return original.get();
        }

        @Override
        public String toString() {
            var value = get();
            return isNull(value) ? "null" : value.toString();
        }
    }
}
