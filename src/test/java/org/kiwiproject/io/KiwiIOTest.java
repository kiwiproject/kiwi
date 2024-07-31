package org.kiwiproject.io;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.io.KiwiIO.CloseableResource;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@DisplayName("KiwiIO")
class KiwiIOTest {

    @Nested
    class CloseQuietly {

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullReader() {
            Reader reader = null;
            assertThatCode(() -> KiwiIO.closeQuietly(reader)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Reader() {
            Reader reader = new StringReader("the quick brown fox jumped over the lazy dod");
            assertThatCode(() -> KiwiIO.closeQuietly(reader)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Reader_WhenThrowsOnClose() throws IOException {
            var reader = mock(Reader.class);
            doThrow(new IOException("I cannot read")).when(reader).close();
            assertThatCode(() -> KiwiIO.closeQuietly(reader)).doesNotThrowAnyException();
            verify(reader).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullWriter() {
            Writer writer = null;
            assertThatCode(() -> KiwiIO.closeQuietly(writer)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Writer() {
            Writer writer = new StringWriter();
            assertThatCode(() -> KiwiIO.closeQuietly(writer)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Writer_WhenThrowsOnClose() throws IOException {
            var writer = mock(Writer.class);
            doThrow(new IOException("I cannot write")).when(writer).close();
            assertThatCode(() -> KiwiIO.closeQuietly(writer)).doesNotThrowAnyException();
            verify(writer).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullInputStream() {
            InputStream stream = null;
            assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_InputStream() {
            InputStream stream = new ByteArrayInputStream(new byte[]{0, 1, 2});
            assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_InputStream_WhenThrowsOnClose() throws IOException {
            var stream = mock(InputStream.class);
            doThrow(new IOException("I cannot read")).when(stream).close();
            assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
            verify(stream).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullOutputStream() {
            OutputStream stream = null;
            assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_OutputStream() {
            OutputStream stream = new ByteArrayOutputStream();
            assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_OutputStream_WhenThrowsOnClose() throws IOException {
            var stream = mock(OutputStream.class);
            doThrow(new IOException("I cannot stream")).when(stream).close();
            assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
            verify(stream).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullSocket() {
            Socket socket = null;
            assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
        }

        @SuppressWarnings("resource")
        @Test
        void shouldClose_Socket() {
            var socket = new Socket();
            assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Socket_WhenThrowsOnClose() throws IOException {
            var socket = mock(Socket.class);
            doThrow(new IOException("I cannot read")).when(socket).close();
            assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
            verify(socket).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullSelector() {
            Selector selector = null;
            assertThatCode(() -> KiwiIO.closeQuietly(selector)).doesNotThrowAnyException();
        }

        @SuppressWarnings("resource")
        @Test
        void shouldClose_Selector() throws IOException {
            var selector = Selector.open();
            assertThatCode(() -> KiwiIO.closeQuietly(selector)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Selector_WhenThrowsOnClose() throws IOException {
            var selector = mock(Selector.class);
            doThrow(new IOException("I cannot select")).when(selector).close();
            assertThatCode(() -> KiwiIO.closeQuietly(selector)).doesNotThrowAnyException();
            verify(selector).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullServerSocket() {
            ServerSocket socket = null;
            assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
        }

        @SuppressWarnings("resource")
        @Test
        void shouldClose_ServerSocket() throws IOException {
            var serverSocket = new ServerSocket();
            assertThatCode(() -> KiwiIO.closeQuietly(serverSocket)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_ServerSocket_WhenThrowsOnClose() throws IOException {
            var serverSocket = mock(ServerSocket.class);
            doThrow(new IOException("I cannot read")).when(serverSocket).close();
            assertThatCode(() -> KiwiIO.closeQuietly(serverSocket)).doesNotThrowAnyException();
            verify(serverSocket).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_Closeables_Null() {
            Closeable[] closeables = null;
            assertThatCode(() -> KiwiIO.closeQuietly(closeables)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Closeables() {
            Reader reader = new StringReader("the quick brown fox jumped over the lazy dog");
            Writer writer = new StringWriter();
            InputStream inputStream = new ByteArrayInputStream(new byte[]{0, 1, 2});
            OutputStream outputStream = new ByteArrayOutputStream();
            assertThatCode(() -> KiwiIO.closeQuietly(reader, writer, inputStream, outputStream))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Closeables_WithSomeNullsSprinkledHereAndThere() {
            Reader reader = new StringReader("the quick brown fox jumped over the lazy dog");
            Writer writer = new StringWriter();
            InputStream inputStream = new ByteArrayInputStream(new byte[]{0, 1, 2});
            OutputStream outputStream = new ByteArrayOutputStream();
            assertThatCode(() -> KiwiIO.closeQuietly(reader, null, writer, null, null, inputStream, null, outputStream))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldClose_Closeables_WhenThrowOnClose() throws IOException {
            var socket = mock(Socket.class);
            doThrow(new IOException("I cannot read")).when(socket).close();

            var selector = mock(Selector.class);
            doThrow(new IOException("I cannot read")).when(selector).close();

            var serverSocket = mock(ServerSocket.class);
            doThrow(new IOException("I cannot read")).when(serverSocket).close();

            assertThatCode(() -> KiwiIO.closeQuietly(socket, selector, serverSocket)).doesNotThrowAnyException();

            verify(socket).close();
            verify(selector).close();
            verify(serverSocket).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullXMLStreamReader() {
            XMLStreamReader xmlStreamReader = null;
            assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamReader)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_XMLStreamReader() throws XMLStreamException {
            var xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader("<xml />"));
            assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamReader)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_XMLStreamReader_WhenThrowsOnClose() throws XMLStreamException {
            var xmlStreamReader = mock(XMLStreamReader.class);
            doThrow(new XMLStreamException("I cannot stream XML")).when(xmlStreamReader).close();
            assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamReader)).doesNotThrowAnyException();
            verify(xmlStreamReader).close();
        }

        @SuppressWarnings("ConstantValue")
        @Test
        void shouldClose_NullXMLStreamWriter() {
            XMLStreamWriter xmlStreamWriter = null;
            assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamWriter)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose_XMLStreamWriter() throws XMLStreamException {
            var xmlStreamWriter = mock(XMLStreamWriter.class);
            doNothing().when(xmlStreamWriter).close();
            assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamWriter)).doesNotThrowAnyException();
            verify(xmlStreamWriter).close();
        }

        @Test
        void shouldClose_XMLStreamWriter_WhenThrowsOnClose() throws XMLStreamException {
            var xmlStreamWriter = mock(XMLStreamWriter.class);
            doThrow(new XMLStreamException("I cannot stream XML")).when(xmlStreamWriter).close();
            assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamWriter)).doesNotThrowAnyException();
            verify(xmlStreamWriter).close();
        }
    }

    @Nested
    class CloseObjectQuietly {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRequireAtLeastOneCloseMethodName_ToCreateCloseableResource(List<String> closeMethodNames) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CloseableResource(new Closer(), closeMethodNames));
        }

        @Test
        void shouldCreateCloseableResource_WithDefaultCloseMethodNames() {
            var closeableResource = new CloseableResource(new Stopper());

            assertThat(closeableResource.closeMethodNames())
                    .containsExactly("close", "stop", "shutdown", "shutdownNow");
        }

        @Test
        void shouldCreateCloseableResource_WithSingleCloseMethodName() {
            var closeableResource = new CloseableResource(new Halter(), "halt");

            assertThat(closeableResource.closeMethodNames()).containsExactly("halt");
        }

        @Test
        void shouldIgnoreNullArguments() {
            assertAll(
                    () -> assertThatCode(() -> KiwiIO.closeObjectQuietly(null))
                            .doesNotThrowAnyException(),

                    () -> assertThatCode(() -> KiwiIO.closeObjectsQuietly((Object[]) null))
                            .doesNotThrowAnyException(),

                    () -> assertThatCode(() -> KiwiIO.closeObjectQuietly("halt", null))
                            .doesNotThrowAnyException(),

                    () -> assertThatCode(() -> KiwiIO.closeObjectsQuietly("halt", (Object[]) null))
                            .doesNotThrowAnyException()
            );
        }

        @Test
        void shouldRequireCloseMethodName() {
            assertAll(
                () -> assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiIO.closeObjectsQuietly("", new Closer(), new Closer())),

                () -> assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiIO.closeObjectQuietly("", new Stopper()))
            );
        }

        @Test
        void shouldCloseObjectWithCloseMethod() {
            var closer = new Closer();
            KiwiIO.closeObjectQuietly(closer);

            assertThat(closer.closeCalled).isTrue();
        }

        @Test
        void shouldCloseObjectWithStopMethod() {
            var stopper = new Stopper();
            KiwiIO.closeObjectQuietly(stopper);

            assertThat(stopper.stopCalled).isTrue();
        }

        @Test
        void shouldCloseObjectWithShutdownMethod() {
            var shutdown = new Shutdown();
            KiwiIO.closeObjectQuietly(shutdown);

            assertThat(shutdown.shutdownCalled).isTrue();
        }

        @Test
        void shouldCloseObjectWithShutdownNowMethod() {
            var shutdownNow = new ShutdownNow();
            KiwiIO.closeObjectQuietly(shutdownNow);

            assertThat(shutdownNow.shutdownNowCalled).isTrue();
        }

        @Test
        void shouldClose_CloseableResource() {
            var halter = new Halter();
            var closeableResource = new CloseableResource(halter, List.of("halt"));

            KiwiIO.closeObjectQuietly(closeableResource);

            assertThat(halter.haltCalled).isTrue();
        }

        @Test
        void shouldIgnore_ExceptionsOnClose_ForDefaultCloseMethods() {
            var stopper = new ThrowingStopper();

            assertThatCode(() -> KiwiIO.closeObjectQuietly(stopper)).doesNotThrowAnyException();

            assertThat(stopper.stopCalled).isTrue();
        }

        @Test
        void shouldIgnore_ExceptionsOnClose_ForCustomCloseMethods() {
            var canceller = new ThrowingCanceller();

            assertThatCode(() -> KiwiIO.closeObjectQuietly("cancel", canceller))
                    .doesNotThrowAnyException();

            assertThat(canceller.cancelCalled).isTrue();
        }

        @Test
        void shouldCloseManyObjects_WithSameCloseMethodName() {
            var halter1 = new Halter();
            var halter2 = new Halter();
            var halter3 = new Halter();

            KiwiIO.closeObjectsQuietly("halt", halter1, halter2, halter3);
            assertAll(
                    () -> assertThat(halter1.haltCalled).isTrue(),
                    () -> assertThat(halter2.haltCalled).isTrue(),
                    () -> assertThat(halter3.haltCalled).isTrue()
            );
        }

        @Test
        void shouldNotAllow_CloseableResource_WhenGivenExplicit_CloseMethodName() {
            var halter1 = new Halter();
            var halter2 = new Halter();
            var terminator = new Terminator();
            var terminatorResource = new CloseableResource(terminator, "terminate");

            var closeMethodName = "halt";
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIO.closeObjectsQuietly(closeMethodName, halter1, halter2, terminatorResource))
                    .withMessage("objects should not contain any instances of CloseableResource when a single closeMethodName (%s) is specified", closeMethodName);
        }

        @Test
        void shouldCloseManyObjects_WithDifferingCloseMethodName() {
            var closer = new Closer();
            var stopper = new Stopper();
            var shutdown = new Shutdown();
            var shutdownNow = new ShutdownNow();

            KiwiIO.closeObjectsQuietly(closer, stopper, shutdown, shutdownNow);

            assertAll(
                    () -> assertThat(closer.closeCalled).isTrue(),
                    () -> assertThat(stopper.stopCalled).isTrue(),
                    () -> assertThat(shutdown.shutdownCalled).isTrue(),
                    () -> assertThat(shutdownNow.shutdownNowCalled).isTrue()
            );
        }

        @Test
        void shouldClose_MixOfObjects_AndCloseableResources() {
            var halter = new Halter();
            var halterResource = new CloseableResource(halter, "halt");

            var terminator = new Terminator();
            var terminatorResource = new CloseableResource(terminator, "terminate");

            var stopper = new Stopper();

            KiwiIO.closeObjectsQuietly(halterResource, stopper, terminatorResource);

            assertAll(
                    () -> assertThat(halter.haltCalled).isTrue(),
                    () -> assertThat(stopper.stopCalled).isTrue(),
                    () -> assertThat(terminator.terminated).isTrue()
            );
        }

        @Test
        void shouldClose_CloseableResources() {
            var closer = new Closer();
            var closerResource = new CloseableResource(closer, "close");

            var stopper = new Stopper();
            var stopperResource = new CloseableResource(stopper, "stop");

            var halter = new Halter();
            var halterResource = new CloseableResource(halter, "halt");

            var terminator = new Terminator();
            var terminatorResource = new CloseableResource(terminator, "terminate");

            KiwiIO.closeQuietly(closerResource);
            KiwiIO.closeQuietly(stopperResource);
            KiwiIO.closeQuietly(halterResource);
            KiwiIO.closeQuietly(terminatorResource);

            assertAll(
                    () -> assertThat(closer.closeCalled).isTrue(),
                    () -> assertThat(stopper.stopCalled).isTrue(),
                    () -> assertThat(halter.haltCalled).isTrue(),
                    () -> assertThat(terminator.terminated).isTrue()
            );
        }

        static class Closer {
            boolean closeCalled;

            @SuppressWarnings("unused")
            void close() {
                closeCalled = true;
            }
        }

        static class Stopper {
            boolean stopCalled;

            @SuppressWarnings("unused")
            void stop() {
                stopCalled = true;
            }
        }

        static class Shutdown {
            boolean shutdownCalled;

            @SuppressWarnings("unused")
            void shutdown() {
                shutdownCalled = true;
            }
        }

        static class ShutdownNow {
            boolean shutdownNowCalled;

            @SuppressWarnings("unused")
            void shutdownNow() {
                shutdownNowCalled = true;
            }
        }

        static class Halter {
            boolean haltCalled;

            @SuppressWarnings("unused")
            void halt() {
                haltCalled = true;
            }
        }

        static class Terminator {
            boolean terminated;

            @SuppressWarnings("unused")
            void terminate() {
                terminated = true;
            }
        }

        static class ThrowingStopper {
            boolean stopCalled;

            @SuppressWarnings("unused")
            void stop() throws Exception {
                stopCalled = true;
                throw new Exception("stop failed!");
            }
        }

        static class ThrowingCanceller {
            boolean cancelCalled;

            @SuppressWarnings("unused")
            void cancel() throws Exception {
                cancelCalled = true;
                throw new Exception("cancel failed!");
            }
        }
    }

    @Nested
    class NewByteArrayInputStreamOfLines {

        private ByteArrayInputStream inputStream;

        @Test
        void shouldContainNothing_WhenGivenNoLines() {
            inputStream = KiwiIO.newByteArrayInputStreamOfLines();
            assertAtEndOfByteArrayInputStream(inputStream);
        }

        @Test
        void shouldAcceptOneLine() throws IOException {
            var firstLine = "the quick brown fox...blah blah";
            inputStream = KiwiIO.newByteArrayInputStreamOfLines(firstLine);

            BufferedReader reader = newBufferedReader();
            assertThat(reader.readLine()).isEqualTo(firstLine);
            assertNoMoreLines(reader);
        }

        @Test
        void shouldAcceptMultipleLines() throws IOException {
            var firstLine = "the quick brown fox...blah blah";
            var secondLine = "jumped over";
            var thirdLine = "the lazy brown dog";

            inputStream = KiwiIO.newByteArrayInputStreamOfLines(firstLine, secondLine, thirdLine);

            var reader = newBufferedReader();
            assertThat(reader.readLine()).isEqualTo(firstLine);
            assertThat(reader.readLine()).isEqualTo(secondLine);
            assertThat(reader.readLine()).isEqualTo(thirdLine);
            assertNoMoreLines(reader);
        }

        private BufferedReader newBufferedReader() {
            return new BufferedReader(new InputStreamReader(inputStream));
        }

        private void assertNoMoreLines(BufferedReader reader) throws IOException {
            assertThat(reader.readLine()).isNull();
        }
    }

    @Nested
    class NewByteArrayInputStream {

        @Test
        void shouldRequireNonNullInputString() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIO.newByteArrayInputStream(null))
                    .withMessage("value must not be null");
        }

        @Test
        void shouldAllowEmptyStrings() {
            var inputStream = KiwiIO.newByteArrayInputStream("");

            assertThat(inputStream).isEmpty();
        }

        @Test
        void shouldEncodeStringsUsingUTF8() {
            var value = "the quick brown fox jumped over the lazy brown dog at " + Instant.now();
            var inputStream = KiwiIO.newByteArrayInputStream(value);

            var expected = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));

            assertThat(inputStream).hasSameContentAs(expected);
        }
    }

    @Nested
    class NewByteArrayInputStreamWithCharset {

        @Test
        void shouldRequireNonNullInputString() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIO.newByteArrayInputStream(null, StandardCharsets.UTF_8))
                    .withMessage("value must not be null");
        }

        @Test
        void shouldRequireNonNullCharset() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiIO.newByteArrayInputStream("some string", null))
                    .withMessage("charset must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "ISO-8859-1",
            "US-ASCII",
            "UTF-8",
            "UTF-16",
            "UTF-32",
        })
        void shouldEncodeStringsUsingDifferentCharsets(String charsetName) {
            var charset = Charset.forName(charsetName);
            var value = "the quick brown fox jumped over the lazy brown dog at " + Instant.now();
            var inputStream = KiwiIO.newByteArrayInputStream(value, charset);

            var expected = new ByteArrayInputStream(value.getBytes(charset));

            assertThat(inputStream).hasSameContentAs(expected);
        }
    }

    @Nested
    class TestingProcessArgumentMethods {

        private static final String LINE_1 = "Four score and seven years ago our fathers ";
        private static final String LINE_2 = "brought forth on this continent, a new nation ";
        private static final String LINE_3 = "conceived  in Liberty, and dedicated to the ";
        private static final String LINE_4 = "proposition that all men are created equal.";

        private LocalProcess process;

        @BeforeEach
        void setUp() {
            process = new LocalProcess();
        }

        @Nested
        class ThatReturnList {

            @Test
            void shouldReturnNoLines_WhenEmptyStreams() {
                process.inputStream = KiwiIO.emptyByteArrayInputStream();
                process.errorStream = KiwiIO.emptyByteArrayInputStream();

                assertThat(KiwiIO.readLinesFromInputStreamOf(process)).isEmpty();
                assertThat(KiwiIO.readLinesFromErrorStreamOf(process)).isEmpty();
            }

            @Test
            void shouldReturnListContainingSameStrings_WhenMultipleStringsInStreamUsingDefaultCharset() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readLinesFromInputStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.readLinesFromErrorStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
            }

            @Test
            void shouldReturnListContainingSameStrings_WhenMultipleStringsInStreamUsingExplicitCharset() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readLinesFromInputStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.readLinesFromErrorStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
            }
        }

        @Nested
        class ThatReturnStream {

            @Test
            void shouldReturnAnEmptyStream_WhenEmptyStreams() {
                process.inputStream = KiwiIO.emptyByteArrayInputStream();
                process.errorStream = KiwiIO.emptyByteArrayInputStream();

                assertThat(KiwiIO.streamLinesFromInputStreamOf(process)).isEmpty();
                assertThat(KiwiIO.streamLinesFromErrorStreamOf(process)).isEmpty();
            }

            @Test
            void shouldReturnStreamContainingSameStrings_WhenMultipleStringsInStreamUsingDefaultCharset() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.streamLinesFromInputStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.streamLinesFromErrorStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
            }

            @Test
            void shouldReturnStreamContainingSameStrings_WhenMultipleStringsInStreamUsingExplicitCharset() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.streamLinesFromInputStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.streamLinesFromErrorStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
            }
        }

        @Nested
        class ThatReturnString {

            @Test
            void shouldReturnInputStreamOfProcess() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readInputStreamOf(process))
                        .isEqualTo(String.join(System.lineSeparator(), LINE_1, LINE_2, LINE_3, LINE_4));
            }

            @Test
            void shouldReturnInputStreamOfProcess_UsingExplicitCharset() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readInputStreamOf(process, StandardCharsets.US_ASCII))
                        .isEqualTo(String.join(System.lineSeparator(), LINE_1, LINE_2, LINE_3, LINE_4));
            }

            @Test
            void shouldReadErrorStreamOfProcess() {
                process.errorStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readErrorStreamOf(process))
                        .isEqualTo(String.join(System.lineSeparator(), LINE_1, LINE_2, LINE_3, LINE_4));
            }

            @Test
            void shouldReadErrorStreamOfProcess_UsingExplicitCharset() {
                process.errorStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readErrorStreamOf(process, StandardCharsets.US_ASCII))
                        .isEqualTo(String.join(System.lineSeparator(), LINE_1, LINE_2, LINE_3, LINE_4));
            }

            @Test
            void shouldReadInputStream() {
                var inputStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readInputStreamAsString(inputStream))
                        .isEqualTo(String.join(System.lineSeparator(), LINE_1, LINE_2, LINE_3, LINE_4));
            }

            @Test
            void shouldReadInputStream_UsingExplicitCharset() {
                var inputStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readInputStreamAsString(inputStream, StandardCharsets.US_ASCII))
                        .isEqualTo(String.join(System.lineSeparator(), LINE_1, LINE_2, LINE_3, LINE_4));
            }

            @SuppressWarnings("resource")
            @Test
            void shouldThrowUncheckedIOException_WhenIOExceptionIsThrown() throws IOException {
                var inputStream = mock(InputStream.class);
                var cause = new IOException("I/O error");
                when(inputStream.transferTo(any(ByteArrayOutputStream.class)))
                        .thenThrow(cause);

                assertThatThrownBy(() -> KiwiIO.readInputStreamAsString(inputStream))
                        .isExactlyInstanceOf(UncheckedIOException.class)
                        .hasMessage("Error converting InputStream to String using Charset UTF-8")
                        .hasCause(cause);
            }
        }

        InputStream newInputStreamWithCharset(Charset charset, String... strings) {
            String joined = Arrays.stream(strings).collect(joining(System.lineSeparator()));
            byte[] buffer = joined.getBytes(charset);

            return new ByteArrayInputStream(buffer);
        }
    }

    private static final class LocalProcess extends Process {

        InputStream inputStream;
        InputStream errorStream;

        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public InputStream getErrorStream() {
            return errorStream;
        }

        @SuppressWarnings("RedundantThrows")
        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
            // noo-op
        }
    }

    @Test
    void shouldCreateEmptyByteArrayInputStream() {
        var inputStream = KiwiIO.emptyByteArrayInputStream();
        assertAtEndOfByteArrayInputStream(inputStream);
    }

    private static void assertAtEndOfByteArrayInputStream(ByteArrayInputStream inputStream) {
        assertThat(inputStream.available()).isZero();
        assertThat(inputStream.read()).isEqualTo(-1);
    }
}
