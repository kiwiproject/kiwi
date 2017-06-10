package org.kiwiproject.collect;

import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class KiwiStreamsTest {

    @Test
    public void testZip_WithFiniteStreamsOfSameLength() {
        Stream<String> stream1 = Stream.of("a", "c", "e", "g", "i", "k", "m", "o", "q", "s", "u", "w", "y");
        Stream<String> stream2 = Stream.of("b", "d", "f", "h", "j", "l", "n", "p", "r", "t", "v", "x", "z");

        List<String> zipped = KiwiStreams.zip(stream1, stream2, this::concat).collect(toList());
        assertThat(zipped).
                containsExactly("ab", "cd", "ef", "gh", "ij", "kl", "mn", "op", "qr", "st", "uv", "wx", "yz");
    }

    @Test
    public void testZip_WithFiniteStreams_WhenFirstStreamIsLongerThanSecondStream() {
        Stream<String> stream1 = Stream.of("a", "b", "c", "d");
        Stream<String> stream2 = Stream.of("x", "y", "z");

        List<String> zipped = KiwiStreams.zip(stream1, stream2, this::concat).collect(toList());
        assertThat(zipped).containsExactly("ax", "by", "cz");
    }

    @Test
    public void testZip_WithFiniteStreams_WhenSecondStreamIsLongerThanFirstStream() {
        Stream<String> stream1 = Stream.of("a", "b", "c");
        Stream<String> stream2 = Stream.of("w", "x", "y", "z");

        List<String> zipped = KiwiStreams.zip(stream1, stream2, this::concat).collect(toList());
        assertThat(zipped).containsExactly("aw", "bx", "cy");
    }

    private String concat(String s1, String s2) {
        return s1 + s2;
    }

    @Test
    public void testZip_WithInfiniteSequentialStreams() {
        Stream<Long> stream1 = Stream.iterate(0L, this::addTwo);
        Stream<Long> stream2 = Stream.iterate(1L, this::addTwo);
        Stream<Long> zipped = KiwiStreams.zip(stream1, stream2, this::sum);

        int numberToCheck = 10_000;
        List<Long> expected = buildExpectedListHavingElementCount(numberToCheck);

        assertThat(zipped.limit(numberToCheck).collect(toList())).containsExactlyElementsOf(expected);
    }

    @Test
    public void testZip_WithInfiniteParallelStreams() {
        Stream<Long> parallelStream1 = Stream.iterate(0L, this::addTwo).parallel();
        Stream<Long> parallelStream2 = Stream.iterate(1L, this::addTwo).parallel();
        Stream<Long> zipped = KiwiStreams.zip(parallelStream1, parallelStream2, this::sum);

        int numberToCheck = 10_000;
        List<Long> expected = buildExpectedListHavingElementCount(numberToCheck);

        assertThat(zipped.limit(numberToCheck).collect(toList())).containsExactlyElementsOf(expected);
    }

    @Test
    public void testZip_WithInfiniteSequentialStream_AndInfiniteParallelStream() {
        Stream<Long> stream1 = Stream.iterate(0L, this::addTwo);
        Stream<Long> parallelStream2 = Stream.iterate(1L, this::addTwo).parallel();
        Stream<Long> zipped = KiwiStreams.zip(stream1, parallelStream2, this::sum);

        int numberToCheck = 10_000;
        List<Long> expected = buildExpectedListHavingElementCount(numberToCheck);

        assertThat(zipped.limit(numberToCheck).collect(toList())).containsExactlyElementsOf(expected);
    }

    @Test
    public void testZip_WithInfiniteParallelStream_AndInfiniteSequentialStream() {
        Stream<Long> parallelStream1 = Stream.iterate(0L, this::addTwo).parallel();
        Stream<Long> stream2 = Stream.iterate(1L, this::addTwo);
        Stream<Long> zipped = KiwiStreams.zip(parallelStream1, stream2, this::sum);

        int numberToCheck = 10_000;
        List<Long> expected = buildExpectedListHavingElementCount(numberToCheck);

        assertThat(zipped.limit(numberToCheck).collect(toList())).containsExactlyElementsOf(expected);
    }

    private Long addTwo(Long value) {
        return value + 2;
    }

    private long sum(Long value1, Long value2) {
        return value1 + value2;
    }

    private List<Long> buildExpectedListHavingElementCount(int numberToCheck) {
        return Stream.iterate(1L, value -> value + 4).limit(numberToCheck).collect(toList());
    }

}