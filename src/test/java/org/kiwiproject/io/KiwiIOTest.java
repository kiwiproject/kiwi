package org.kiwiproject.io;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
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
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@DisplayName("KiwiIO")
class KiwiIOTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullReader() {
        Reader reader = null;
        assertThatCode(() -> KiwiIO.closeQuietly(reader)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Reader() {
        Reader reader = new StringReader("the quick brown fox jumped over the lazy dod");
        assertThatCode(() -> KiwiIO.closeQuietly(reader)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Reader_WhenThrowsOnClose() throws IOException {
        Reader reader = mock(Reader.class);
        doThrow(new IOException("I cannot read")).when(reader).close();
        assertThatCode(() -> KiwiIO.closeQuietly(reader)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullWriter() {
        Writer writer = null;
        assertThatCode(() -> KiwiIO.closeQuietly(writer)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Writer() {
        Writer writer = new StringWriter();
        assertThatCode(() -> KiwiIO.closeQuietly(writer)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Writer_WhenThrowsOnClose() throws IOException {
        Writer writer = mock(Writer.class);
        doThrow(new IOException("I cannot write")).when(writer).close();
        assertThatCode(() -> KiwiIO.closeQuietly(writer)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullInputStream() {
        InputStream stream = null;
        assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_InputStream() {
        InputStream stream = new ByteArrayInputStream(new byte[]{0, 1, 2});
        assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_InputStream_WhenThrowsOnClose() throws IOException {
        InputStream stream = mock(InputStream.class);
        doThrow(new IOException("I cannot read")).when(stream).close();
        assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullOutputStream() {
        OutputStream stream = null;
        assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_OutputStream() {
        OutputStream stream = new ByteArrayOutputStream();
        assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_OutputStream_WhenThrowsOnClose() throws IOException {
        OutputStream stream = mock(OutputStream.class);
        doThrow(new IOException("I cannot stream")).when(stream).close();
        assertThatCode(() -> KiwiIO.closeQuietly(stream)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullSocket() {
        Socket socket = null;
        assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Socket() {
        Socket socket = new Socket();
        assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Socket_WhenThrowsOnClose() throws IOException {
        Socket socket = mock(Socket.class);
        doThrow(new IOException("I cannot read")).when(socket).close();
        assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullSelector() {
        Selector selector = null;
        assertThatCode(() -> KiwiIO.closeQuietly(selector)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Selector() throws IOException {
        Selector selector = Selector.open();
        assertThatCode(() -> KiwiIO.closeQuietly(selector)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Selector_WhenThrowsOnClose() throws IOException {
        Selector socket = mock(Selector.class);
        doThrow(new IOException("I cannot select")).when(socket).close();
        assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullServerSocket() {
        ServerSocket socket = null;
        assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_ServerSocket() throws IOException {
        ServerSocket socket = new ServerSocket();
        assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_ServerSocket_WhenThrowsOnClose() throws IOException {
        ServerSocket socket = mock(ServerSocket.class);
        doThrow(new IOException("I cannot read")).when(socket).close();
        assertThatCode(() -> KiwiIO.closeQuietly(socket)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_Closeables_Null() {
        Closeable[] closeables = null;
        assertThatCode(() -> KiwiIO.closeQuietly(closeables)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Closeables() {
        Reader reader = new StringReader("the quick brown fox jumped over the lazy dog");
        Writer writer = new StringWriter();
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0, 1, 2});
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThatCode(() -> KiwiIO.closeQuietly(reader, writer, inputStream, outputStream))
                .doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Closeables_WithSomeNullsSprinkledHereAndThere() {
        Reader reader = new StringReader("the quick brown fox jumped over the lazy dog");
        Writer writer = new StringWriter();
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0, 1, 2});
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThatCode(() -> KiwiIO.closeQuietly(reader, null, writer, null, null, inputStream, null, outputStream))
                .doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_Closeables_WhenThrowOnClose() throws IOException {
        Socket socket = mock(Socket.class);
        doThrow(new IOException("I cannot read")).when(socket).close();

        Selector selector = mock(Selector.class);
        doThrow(new IOException("I cannot read")).when(selector).close();

        ServerSocket serverSocket = mock(ServerSocket.class);
        doThrow(new IOException("I cannot read")).when(serverSocket).close();

        assertThatCode(() -> KiwiIO.closeQuietly(socket, selector, serverSocket)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullXMLStreamReader() {
        XMLStreamReader xmlStreamReader = null;
        assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamReader)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_XMLStreamReader() throws XMLStreamException {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader("<xml />"));
        assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamReader)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_XMLStreamReader_WhenThrowsOnClose() throws XMLStreamException {
        XMLStreamReader xmlStreamReader = mock(XMLStreamReader.class);
        doThrow(new XMLStreamException("I cannot stream XML")).when(xmlStreamReader).close();
        assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamReader)).doesNotThrowAnyException();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCloseQuietly_NullXMLStreamWriter() {
        XMLStreamWriter xmlStreamWriter = null;
        assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamWriter)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_XMLStreamWriter() throws XMLStreamException {
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(new StringWriter());
        assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamWriter)).doesNotThrowAnyException();
    }

    @Test
    void testCloseQuietly_XMLStreamWriter_WhenThrowsOnClose() throws XMLStreamException {
        XMLStreamWriter xmlStreamWriter = mock(XMLStreamWriter.class);
        doThrow(new XMLStreamException("I cannot stream XML")).when(xmlStreamWriter).close();
        assertThatCode(() -> KiwiIO.closeQuietly(xmlStreamWriter)).doesNotThrowAnyException();
    }

    @Nested
    class NewByteArrayInputStreamOfLines {

        private ByteArrayInputStream inputStream;

        @Test
        void testNoLines() {
            inputStream = KiwiIO.newByteArrayInputStreamOfLines();
            assertAtEndOfByteArrayInputStream(inputStream);
        }

        @Test
        void testOneLine() throws IOException {
            String firstLine = "the quick brown fox...blah blah";
            inputStream = KiwiIO.newByteArrayInputStreamOfLines(firstLine);

            BufferedReader reader = newBufferedReader();
            assertThat(reader.readLine()).isEqualTo(firstLine);
            assertNoMoreLines(reader);
        }

        @Test
        void testMoreThanOneLine() throws IOException {
            String firstLine = "the quick brown fox...blah blah";
            String secondLine = "jumped over";
            String thirdLine = "the lazy brown dog";

            inputStream = KiwiIO.newByteArrayInputStreamOfLines(firstLine, secondLine, thirdLine);

            BufferedReader reader = newBufferedReader();
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
        class WhenEvaluatingTheListReturningMethodsUsing {

            @Test
            void emptyStreams_ReturnsNoLines() {
                process.inputStream = KiwiIO.emptyByteArrayInputStream();
                process.errorStream = KiwiIO.emptyByteArrayInputStream();

                assertThat(KiwiIO.readLinesFromInputStreamOf(process)).isEmpty();
                assertThat(KiwiIO.readLinesFromErrorStreamOf(process)).isEmpty();
            }

            @Test
            void multipleStringsInStreamUsingDefaultCharset_ReturnsListContainingSameStrings() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readLinesFromInputStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.readLinesFromErrorStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
            }

            @Test
            void multipleStringsInStreamUsingExplicitCharset_ReturnsListContainingSameStrings() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.readLinesFromInputStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.readLinesFromErrorStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
            }
        }

        @Nested
        class WhenEvaluatingTheStreamReturningMethodsUsing {

            @Test
            void emptyStreams_ReturnsAnEmptyStream() {
                process.inputStream = KiwiIO.emptyByteArrayInputStream();
                process.errorStream = KiwiIO.emptyByteArrayInputStream();

                assertThat(KiwiIO.streamLinesFromInputStreamOf(process)).isEmpty();
                assertThat(KiwiIO.streamLinesFromErrorStreamOf(process)).isEmpty();
            }

            @Test
            void multipleStringsInStreamUsingDefaultCharset_ReturnsStreamContainingSameStrings() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.UTF_8, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.streamLinesFromInputStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.streamLinesFromErrorStreamOf(process)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
            }

            @Test
            void multipleStringsInStreamUsingExplicitCharset_ReturnsStreamContainingSameStrings() {
                process.inputStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);
                process.errorStream = newInputStreamWithCharset(StandardCharsets.US_ASCII, LINE_1, LINE_2, LINE_3, LINE_4);

                assertThat(KiwiIO.streamLinesFromInputStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
                assertThat(KiwiIO.streamLinesFromErrorStreamOf(process, StandardCharsets.US_ASCII)).containsExactly(LINE_1, LINE_2, LINE_3, LINE_4);
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
    void testEmptyByteArrayInputStream() {
        ByteArrayInputStream inputStream = KiwiIO.emptyByteArrayInputStream();
        assertAtEndOfByteArrayInputStream(inputStream);
    }

    private static void assertAtEndOfByteArrayInputStream(ByteArrayInputStream inputStream) {
        assertThat(inputStream.available()).isZero();
        assertThat(inputStream.read()).isEqualTo(-1);
    }
}