package org.kiwiproject.io;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentContainsOnlyNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotEmpty;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotInstanceOf;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotBlank;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Static I/O utilities.
 * <p>
 * The {@code closeQuietly} methods that accept {@link Closeable} were copied directly from Apache Commons I/O and
 * the deprecation warnings and annotations removed. While they should not be used often, sometimes they might come in
 * handy, so we want to keep them around for posterity. Slight style modifications were made (e.g., replace {@code obj != null}
 * checks with {@code nonNull(obj}, etc.) as well as adding logging. Did not bother copying all the {@code closeQuietly}
 * methods that took a specific class such as {@link java.io.Reader}, {@link java.io.Writer}, {@link java.net.Socket}, etc.
 * They all implement {@link Closeable} and were probably only there because those specific classes pre-dated Java 5 when
 * {@link Closeable} was added to the JDK, and we assume early (pre-Java 5) versions of {@code IOUtils} provided them.
 */
@UtilityClass
@Slf4j
public class KiwiIO {

    private static final List<String> DEFAULT_CLOSE_METHOD_NAMES =
            List.of("close", "stop", "shutdown", "shutdownNow");

    /**
     * Closes a <code>Closeable</code> unconditionally.
     * <p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored. This is typically used in
     * finally blocks.
     * <p>
     * Example code:
     * </p>
     * <pre>
     * Closeable closeable = null;
     * try {
     *     closeable = new FileReader(&quot;foo.txt&quot;);
     *     // process closeable
     *     closeable.close();
     * } catch (Exception e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(closeable);
     * }
     * </pre>
     * <p>
     * Closing all streams:
     * </p>
     * <pre>
     * try {
     *     return IOUtils.copy(inputStream, outputStream);
     * } finally {
     *     IOUtils.closeQuietly(inputStream);
     *     IOUtils.closeQuietly(outputStream);
     * }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @implNote Copied from Apache Commons I/O's IOUtils once it became deprecated with the message "Please use
     * the try-with-resources statement or handle suppressed exceptions manually."
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Closeable closeable) {
        try {
            if (nonNull(closeable)) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            logCloseError(closeable.getClass(), ioe);
        }
    }

    /**
     * Closes a <code>Closeable</code> unconditionally.
     * <p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
     * <p>
     * This is typically used in finally blocks to ensure that the closeable is closed
     * even if an Exception was thrown before the normal close statement was reached.
     * <br>
     * <b>It should not be used to replace the close statement(s)
     * which should be present for the non-exceptional case.</b>
     * <br>
     * It is only intended to simplify tidying up where normal processing has already failed,
     * and reporting close failure as well is not necessary or useful.
     * <p>
     * Example code:
     * </p>
     * <pre>
     * Closeable closeable = null;
     * try {
     *     closeable = new FileReader(&quot;foo.txt&quot;);
     *     // processing using the closeable; may throw an Exception
     *     closeable.close(); // Normal close - exceptions not ignored
     * } catch (Exception e) {
     *     // error handling
     * } finally {
     *     <b>IOUtils.closeQuietly(closeable); // In case normal close was skipped due to Exception</b>
     * }
     * </pre>
     * <p>
     * Closing all streams:
     * <br>
     * <pre>
     * try {
     *     return IOUtils.copy(inputStream, outputStream);
     * } finally {
     *     IOUtils.closeQuietly(inputStream, outputStream);
     * }
     * </pre>
     *
     * @param closeables the objects to close, may be null or already closed
     * @implNote Copied from Apache Commons I/O's IOUtils once it became deprecated with the message "Please use
     * the try-with-resources statement or handle suppressed exceptions manually."
     * @see #closeQuietly(Closeable)
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Closeable... closeables) {
        if (isNull(closeables)) {
            return;
        }
        for (final Closeable closeable : closeables) {
            closeQuietly(closeable);
        }
    }

    /**
     * Closes an {@link XMLStreamReader} unconditionally.
     * <p>
     * <em>Since {@link XMLStreamReader} does not implement {@link Closeable}, you cannot use a try-with-resources.</em>
     *
     * @param xmlStreamReader the {@link XMLStreamReader} to close
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(XMLStreamReader xmlStreamReader) {
        if (nonNull(xmlStreamReader)) {
            try {
                xmlStreamReader.close();
            } catch (Exception e) {
                logCloseError(XMLStreamReader.class, e);
            }
        }
    }

    /**
     * Closes an {@link XMLStreamWriter} unconditionally.
     * <p>
     * <em>Since {@link XMLStreamWriter} does not implement {@link Closeable}, you cannot use a try-with-resources.</em>
     *
     * @param xmlStreamWriter the {@link XMLStreamWriter} to close
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(XMLStreamWriter xmlStreamWriter) {
        if (nonNull(xmlStreamWriter)) {
            try {
                xmlStreamWriter.close();
            } catch (Exception e) {
                logCloseError(XMLStreamWriter.class, e);
            }
        }
    }


    /**
     * Represents a resource that can be closed using a "close" method.
     * <p>
     * Allows multiple close method names to be specified, which can be useful
     * in situations where you want to close several resources that have
     * different "close" methods. For example, any {@link AutoCloseable}
     * contains a "close" method while any {@link java.util.concurrent.ExecutorService}
     * has both "shutdown" and "shutdownNow" methods.
     * <p>
     * If you only need a single clsoe method name, or the default close
     * method names, you can use one of the secondary constructors.
     *
     * @param object the resource that can be closed
     * @param closeMethodNames a non-null, non-empty list of close method names
     */
    public record CloseableResource(@Nullable Object object, List<String> closeMethodNames) {
        public CloseableResource {
            checkArgumentContainsOnlyNotBlank(closeMethodNames,
                    "closeMethodNames must not be null or empty, or contain any blanks");
        }

        /**
         * Create a new instance with a default set of close method names.
         *
         * @param object the resource that can be closed
         * @see KiwiIO#defaultCloseMethodNames()
         */
        public CloseableResource(@Nullable Object object) {
            this(object, DEFAULT_CLOSE_METHOD_NAMES);
        }

        /**
         * Create a new instance with a single close method name.
         *
         * @param object the resource that can be closed
         * @param closeMethodName the single close method name
         */
        public CloseableResource(@Nullable Object object, String closeMethodName) {
            this(
                object,
                List.of(requireNotBlank(closeMethodName, "closeMethodName must not be blank"))
            );
        }
    }

    /**
     * Return the default method names used when closing objects using
     * any of the methods to close generic {@link Object}.
     * <p>
     * These method names are tried in order when attempting to close
     * an Object when no explicit close method name is provided.
     * <p>
     * The default names are
     *
     * @return the default close method names
     */
    public static List<String> defaultCloseMethodNames() {
        return DEFAULT_CLOSE_METHOD_NAMES;
    }

    /**
     * Closes an object unconditionally. This method ignores null objects and exceptions.
     * <p>
     * The object may be a {@link CloseableResource}.
     * <p>
     * Uses the default close method names.
     *
     * @param object the object to close, may be null or already closed
     * @see #defaultCloseMethodNames()
     */
    public static void closeObjectQuietly(Object object) {
        if (isNull(object)) {
            return;
        }

        var closeableResource = asCloseableResource(object);
        closeResourceQuietly(closeableResource);
    }

    /**
     * Closes an object unconditionally. This method ignores null objects and exceptions.
     * <p>
     * The object may not be a {@link CloseableResource}, since it could contain a different
     * close method name.
     *
     * @param closeMethodName the name of the close method
     * @param object the object to close, may be null or already closed
     * @throws IllegalArgumentException if closeMethodName is blank or object is a CloseableResource
     */
    public static void closeObjectQuietly(String closeMethodName, Object object) {
        checkArgumentNotBlank(closeMethodName, "closeMethodName must not be blank");
        checkArgumentNotInstanceOf(object, CloseableResource.class,
                "object must not be a CloseableResource");
        closeResourceQuietly(new CloseableResource(object, List.of(closeMethodName)));
    }

    /**
     * Closes one or more objects unconditionally. This method ignores null objects and exceptions.
     * <p>
     * The objects may contain {@link CloseableResource} and/or other closeable objects.
     * <p>
     * Uses the default close method names.
     *
     * @param objects the objects to close, may be null or already closed
     * @see #defaultCloseMethodNames()
     */
    public static void closeObjectsQuietly(Object... objects) {
        if (isNull(objects)) {
            return;
        }

        Arrays.stream(objects)
            .filter(Objects::nonNull)
            .map(KiwiIO::asCloseableResource)
            .forEach(KiwiIO::closeResourceQuietly);
    }

    private static CloseableResource asCloseableResource(Object object) {
        return (object instanceof CloseableResource closeableResource) ?
                closeableResource : new CloseableResource(object, DEFAULT_CLOSE_METHOD_NAMES);
    }

    /**
     * Closes one or more objects unconditionally. This method ignores null objects and exceptions.
     * <p>
     * The objects should not contain any {@link CloseableResource} instances. The reason is that
     * those could specify a different close method name.
     *
     * @param closeMethodName the name of the close method
     * @param objects the objects to close, may be null or already closed
     * @throws IllegalArgumentException of objects contains any CloseableResource instances
     */
    public static void closeObjectsQuietly(String closeMethodName, Object... objects) {
        if (isNull(objects)) {
            return;
        }

       checkDoesNotContainAnyCloseableResources(closeMethodName, objects);

        Arrays.stream(objects)
            .filter(Objects::nonNull)
            .map(object -> new CloseableResource(object, List.of(closeMethodName)))
            .forEach(KiwiIO::closeResourceQuietly);
    }

    private static void checkDoesNotContainAnyCloseableResources(String closeMethodName, Object... objects) {
        for (var object : objects) {
            checkIsNotCloseableResource(closeMethodName, object);
        }
    }

    private static void checkIsNotCloseableResource(String closeMethodName, Object object) {
        checkArgument(
                isNotCloseableResource(object),
                "objects should not contain any instances of CloseableResource when a single closeMethodName (%s) is specified",
                closeMethodName);
    }

    private static boolean isNotCloseableResource(Object object) {
        return !(object instanceof CloseableResource);
    }

    /**
     * Closes a resource unconditionally. This method ignores null objects and exceptions.
     * <p>
     * The object inside the resource may be null or already closed. The resource must
     * contain at least one close method name.
     *
     * @param closeableResource the resource to close, must not be null
     * @throws IllegalArgumentException if the closeableResource is null or has no close method names
     */
    public static void closeResourceQuietly(CloseableResource closeableResource) {
        checkArgumentNotNull(closeableResource, "closeableResource must not be null");

        var closeMethodNames = closeableResource.closeMethodNames();
        checkArgumentNotEmpty(closeMethodNames, "closeMethodNames must not be empty");

        var object = closeableResource.object();
        if (isNull(object)) {
            return;
        }

        var objectType = object.getClass();
        var typeName = objectType.getName();

        closeMethodNames.stream()
                .map(methodName -> tryClose(object, objectType, typeName, methodName))
                .filter(CloseResult::succeeded)
                .findFirst()
                .ifPresentOrElse(
                        successResult -> LOG.trace("Successfully closed a {} using {}", typeName, successResult.methodName()),
                        () -> LOG.warn("All attempts to close a {} failed. Tried using methods: {}", typeName, closeMethodNames));
    }

    private CloseResult tryClose(Object object, Class<?> objectType, String typeName, String closeMethodName) {
        try {
            LOG.trace("Attempting to close a {} using {}", typeName, closeMethodName);
            var methodHandle = MethodHandles.lookup()
                    .findVirtual(objectType, closeMethodName, methodType(Void.TYPE));
            methodHandle.invoke(object);
            return new CloseResult(true, closeMethodName, null);
        } catch (Throwable error) {
            LOG.trace("Unable to close a {} using {}", typeName, closeMethodName, error);
            return new CloseResult(false, closeMethodName, error);
        }
    }

    private record CloseResult(boolean succeeded, String methodName, Throwable error) {
    }

    private static void logCloseError(Class<?> typeOfObject, Throwable error) {
        LOG.warn("Unexpected error while attempting to close {} quietly (use DEBUG-level for stack trace): {}",
                typeOfObject.getSimpleName(), error.getMessage());
        LOG.debug("Error closing {} instance", typeOfObject.getName(), error);
    }

    /**
     * Return a newly constructed {@link ByteArrayInputStream} containing the given {@code lines} separated by
     * the {@link System#lineSeparator()} and using UTF-8 as the {@link Charset} when converting the joined
     * lines to bytes.
     *
     * @param lines the lines to convert
     * @return a ByteArrayInputStream containing the given lines, encoded using UTF-8
     */
    public static ByteArrayInputStream newByteArrayInputStreamOfLines(String... lines) {
        if (lines.length == 0) {
            return emptyByteArrayInputStream();
        }

        String joined = Arrays.stream(lines).collect(joining(System.lineSeparator()));
        byte[] buffer = joined.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(buffer);
    }

    /**
     * Creates a new {@link ByteArrayInputStream} containing the bytes of the given string using the
     * UTF-8 character set.
     * <p>
     * Note: The UTF-8 character set is widely used and supports a vast range of characters, making it
     * suitable for most applications. However, if the string was encoded using a different character
     * set, use the other version of this method that accepts a {@link Charset} to specify the character
     * set that was used to encode the string.
     *
     * @param value the string from which to create the ByteArrayInputStream
     * @return a new ByteArrayInputStream initialized with bytes from the provided string
     * @throws IllegalArgumentException if the input string is null
     */
    public static ByteArrayInputStream newByteArrayInputStream(String value) {
        return newByteArrayInputStream(value, StandardCharsets.UTF_8);
    }

    /**
     * Creates a new {@link ByteArrayInputStream} containing the bytes of the given string using the
     * specified character set.
     *
     * @param value   the string from which to create the ByteArrayInputStream
     * @param charset the character set used to encode the string as bytes
     * @return a new ByteArrayInputStream initialized with bytes from the provided string
     * @throws IllegalArgumentException if the input string or charset is null
     */
    public static ByteArrayInputStream newByteArrayInputStream(String value, Charset charset) {
        checkArgumentNotNull(value, "value must not be null");
        checkArgumentNotNull(charset, "charset must not be null");

        byte[] bytes = value.getBytes(charset);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Return a newly constructed, empty {@link ByteArrayInputStream}.
     *
     * @return new ByteArrayInputStream
     */
    public static ByteArrayInputStream emptyByteArrayInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    /**
     * Return a {@link List} of {@link String}s from the error stream of the given {@link Process} using {@code UTF-8}
     * for the String encoding.
     *
     * @param process the process
     * @return the list of UTF-8 encoded strings from the process' error stream
     */
    public static List<String> readLinesFromErrorStreamOf(Process process) {
        return readLinesFrom(process.getErrorStream(), StandardCharsets.UTF_8);
    }

    /**
     * Return a {@link List} of {@link String}s from the error stream of the given {@link Process} using the specified
     * {@link Charset} for the String encoding.
     *
     * @param process the process
     * @param charset the charset
     * @return the list of UTF-8 encoded strings from the process' error stream
     */
    public static List<String> readLinesFromErrorStreamOf(Process process, Charset charset) {
        return readLinesFrom(process.getErrorStream(), charset);
    }

    /**
     * Return a {@link List} of {@link String}s from the input stream of the given {@link Process} using {@code UTF-8}
     * for the String encoding.
     *
     * @param process the process
     * @return the list of UTF-8 encoded strings from the process' input stream
     */
    public static List<String> readLinesFromInputStreamOf(Process process) {
        return readLinesFrom(process.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Return a {@link List} of {@link String}s from the input stream of the given {@link Process} using the specified
     * {@link Charset} for the String encoding.
     *
     * @param process the process
     * @param charset the charset
     * @return the list of UTF-8 encoded strings from the process' input stream
     */
    public static List<String> readLinesFromInputStreamOf(Process process, Charset charset) {
        return readLinesFrom(process.getInputStream(), charset);
    }

    /**
     * Return a {@link List} of {@link String}s from the input stream using the specified {@link Charset} for the
     * String encoding.
     *
     * @param stream  the stream
     * @param charset the charset
     * @return a list of strings from the input stream, encoded using the specified charset
     */
    public static List<String> readLinesFrom(InputStream stream, Charset charset) {
        return streamLinesFrom(stream, charset).toList();
    }

    /**
     * Return a {@link Stream} of {@link String}s from the error stream of the given {@link Process} using {@code UTF-8}
     * for the String encoding.
     *
     * @param process the process
     * @return the stream of UTF-8 encoded strings from the process' error stream
     */
    public static Stream<String> streamLinesFromErrorStreamOf(Process process) {
        return streamLinesFrom(process.getErrorStream(), StandardCharsets.UTF_8);
    }

    /**
     * Return a {@link Stream} of {@link String}s from the error stream of the given {@link Process} using the specified
     * {@link Charset} for the String encoding.
     *
     * @param process the process
     * @param charset the charset
     * @return the stream of strings from the process' error stream, encoded using the specified charset
     */
    public static Stream<String> streamLinesFromErrorStreamOf(Process process, Charset charset) {
        return streamLinesFrom(process.getErrorStream(), charset);
    }

    /**
     * Return a {@link Stream} of {@link String}s from the input stream of the given {@link Process} using {@code UTF-8}
     * for the String encoding.
     *
     * @param process the process
     * @return the stream of UTF-8 encoded strings from the process' input stream
     */
    public static Stream<String> streamLinesFromInputStreamOf(Process process) {
        return streamLinesFrom(process.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Return a {@link Stream} of {@link String}s from the input stream of the given {@link Process} using the specified
     * {@link Charset} for the String encoding.
     *
     * @param process the process
     * @param charset the charset
     * @return the stream of strings from the process' input stream, encoded using the specified charset
     */
    public static Stream<String> streamLinesFromInputStreamOf(Process process, Charset charset) {
        return streamLinesFrom(process.getInputStream(), charset);
    }

    /**
     * Return a {@link Stream} of {@link String}s from the {@link InputStream} using the specified {@link Charset}
     * for the String encoding.
     *
     * @param stream  the stream
     * @param charset the charset
     * @return the stream of strings from the input stream, encoded using the specified charset
     */
    public static Stream<String> streamLinesFrom(InputStream stream, Charset charset) {
        return new BufferedReader(new InputStreamReader(stream, charset)).lines();
    }

    /**
     * Read the input stream of the give {@link Process} as a String using {@code UTF-8} as the String encoding.
     * <p>
     * Note that process output may contain one or more lines, which will therefore include line termination
     * characters within or at the end of the returned string.
     *
     * @param process the process
     * @return the process' input stream as a UTF-8 encoded string
     * @see Process#getInputStream()
     */
    public static String readInputStreamOf(Process process) {
        return readInputStreamOf(process, StandardCharsets.UTF_8);
    }

    /**
     * Read the input stream of the give {@link Process} as a String using the specified {@link Charset} for the
     * string encoding.
     * <p>
     * Note that process output may contain one or more lines, which will therefore include line termination
     * characters within or at the end of the returned string.
     *
     * @param process the process
     * @param charset the charset
     * @return the process' input stream as a string, encoded using the specified charset
     * @see Process#getInputStream()
     */
    public static String readInputStreamOf(Process process, Charset charset) {
        return readInputStreamAsString(process.getInputStream(), charset);
    }

    /**
     * Read the error stream of the give {@link Process} as a String using {@code UTF-8} as the string encoding.
     * <p>
     * Note that process output may contain one or more lines, which will therefore include line termination
     * characters within or at the end of the returned string.
     *
     * @param process the process
     * @return the process' error stream as a UTF-8 encoded string
     * @see Process#getErrorStream()
     */
    public static String readErrorStreamOf(Process process) {
        return readErrorStreamOf(process, StandardCharsets.UTF_8);
    }

    /**
     * Read the error stream of the give {@link Process} as a String using the specified {@link Charset} for the
     * string encoding.
     * <p>
     * Note that process output may contain one or more lines, which will therefore include line termination
     * characters within or at the end of the returned string.
     *
     * @param process the process
     * @param charset the charset
     * @return the process' error stream as a string, encoded using the specified charset
     * @see Process#getErrorStream()
     */
    public static String readErrorStreamOf(Process process, Charset charset) {
        return readInputStreamAsString(process.getErrorStream(), charset);
    }

    /**
     * Convert the given {@link InputStream} to a {@code UTF-8} encoded String.
     *
     * @param inputStream the input stream
     * @return the input stream as a UTF-8 encoded string
     */
    public static String readInputStreamAsString(InputStream inputStream) {
        return readInputStreamAsString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * Convert the given {@link InputStream} to a String using the given {@link Charset} for the string encoding.
     *
     * @param inputStream the input stream
     * @param charset     the charset
     * @return the input stream as a string, encoded using the specified charset
     */
    public static String readInputStreamAsString(InputStream inputStream, Charset charset) {
        try {
            var outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            return outputStream.toString(charset);
        } catch (IOException e) {
            throw new UncheckedIOException("Error converting InputStream to String using Charset " + charset, e);
        }
    }

}
