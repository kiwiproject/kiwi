package org.kiwiproject.io;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Static I/O utilities.
 * <p>
 * The {@code closeQuietly} methods that accept {@link Closeable} were copied directly from Apache Commons I/O and
 * the deprecation warnings and annotations removed. While they should not be used often, sometimes they might come in
 * handy so we want to keep them around for posterity. Slight style modifications were made (e.g. replace {@code obj != null}
 * checks with {@code nonNull(obj}, etc. as well as adding logging. Did not bother copying all the {@code closeQuietly}
 * methods that took a specific class such as {@link java.io.Reader}, {@link java.io.Writer}, {@link java.net.Socket}, etc.
 * They all implement {@link Closeable} and were probably only there because those specific classes pre-dated Java 5 when
 * {@link Closeable} was added to the JDK, and we assume early (pre-Java 5) versions of {@code IOUtils} provided them.
 */
@UtilityClass
@Slf4j
public class KiwiIO {

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
     * @param closeable the objects to close, may be null or already closed
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
            logCloseException(closeable.getClass(), ioe);
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
     * It is only intended to simplify tidying up where normal processing has already failed
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
                logCloseException(XMLStreamReader.class, e);
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
                logCloseException(XMLStreamWriter.class, e);
            }
        }
    }

    private static void logCloseException(Class<?> typeOfObject, Exception ex) {
        String typeSimpleName = typeOfObject.getSimpleName();
        LOG.warn("Unexpected error while attempting to close {} quietly (use DEBUG-level for stack trace): {}",
                typeSimpleName, ex.getMessage());
        LOG.debug("Error closing {} instance", typeSimpleName, ex);
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
     * Return a {@link Stream} of {@link String}s from the given {@link InputStream} using the specified {@link Charset}
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
     * Read the input stream of the give {@link Process} as a String using the the specified {@link Charset} for the
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
     * Read the error stream of the give {@link Process} as a String using the the specified {@link Charset} for the
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
