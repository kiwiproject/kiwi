package org.kiwiproject.collect;

import static java.util.stream.Collectors.toList;

import lombok.Value;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@DisplayName("KiwiStreams")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiStreamsTest {

    @Test
    void testFindFirst(SoftAssertions softly) {
        softly.assertThat(KiwiStreams.findFirst(newNumberStream(), String.class)).isEmpty();
        softly.assertThat(KiwiStreams.findFirst(newNumberList().stream(), String.class)).isEmpty();

        softly.assertThat(KiwiStreams.findFirst(newNumberStream(), Integer.class)).hasValue(24);
        softly.assertThat(KiwiStreams.findFirst(newNumberList().stream(), Integer.class)).hasValue(24);

        softly.assertThat(KiwiStreams.findFirst(newNumberStream(), Long.class)).hasValue(42L);
        softly.assertThat(KiwiStreams.findFirst(newNumberList().stream(), Long.class)).hasValue(42L);

        softly.assertThat(KiwiStreams.findFirst(newNumberStream(), Double.class)).hasValue(84.0);
        softly.assertThat(KiwiStreams.findFirst(newNumberList().stream(), Double.class)).hasValue(84.0);

        softly.assertThat(KiwiStreams.findFirst(newStreamOfABC(), A.class)).contains(new A(1));
        softly.assertThat(KiwiStreams.findFirst(newStreamOfABC(), B.class)).contains(new B(7));
        softly.assertThat(KiwiStreams.findFirst(newStreamOfABC(), C.class)).contains(new C(3));

        softly.assertThat(KiwiStreams.findFirst(streamOf42(), Object.class)).hasValue(42);
        softly.assertThat(KiwiStreams.findFirst(streamOf42(), Integer.class)).hasValue(42);
        softly.assertThat(KiwiStreams.findFirst(streamOf42(), Double.class)).hasValue(42.0);
        softly.assertThat(KiwiStreams.findFirst(streamOf42(), String.class)).hasValue("42");
        softly.assertThat(KiwiStreams.findFirst(streamOf42(), Float.class)).isEmpty();
    }

    @Test
    void testFindFirst_WithPredicate(SoftAssertions softly) {
        softly.assertThat(KiwiStreams.findFirst(newNumberStream(), String.class,
                value -> value.length() > 10)).isEmpty();
        softly.assertThat(KiwiStreams.findFirst(newNumberList().stream(), String.class,
                value -> value.length() > 10)).isEmpty();

        softly.assertThat(KiwiStreams.findFirst(newNumberStream(), Integer.class,
                value -> value > 100)).isEmpty();
        softly.assertThat(KiwiStreams.findFirst(newNumberList().stream(), Integer.class,
                value -> value > 100)).isEmpty();

        softly.assertThat(KiwiStreams.findFirst(newNumberStream(), Integer.class,
                value -> value > 30)).hasValue(64);
        softly.assertThat(KiwiStreams.findFirst(newNumberList().stream(), Integer.class,
                value -> value > 30)).hasValue(64);

        softly.assertThat(KiwiStreams.findFirst(newStreamOfABC(), A.class,
                a -> a.id > 5)).contains(new A(8));
        softly.assertThat(KiwiStreams.findFirst(newStreamOfABC(), B.class,
                b -> b.id > 10)).isEmpty();
        softly.assertThat(KiwiStreams.findFirst(newStreamOfABC(), C.class,
                c -> c.id == 6)).contains(new C(6));

        softly.assertThat(KiwiStreams.findFirst(streamOf42(), Integer.class,
                value -> value > 10)).hasValue(42);
        softly.assertThat(KiwiStreams.findFirst(streamOf42(), Integer.class,
                value -> value < 10)).isEmpty();
        softly.assertThat(KiwiStreams.findFirst(streamOf42(), Double.class,
                value -> value >= 42)).hasValue(42.0);
        softly.assertThat(KiwiStreams.findFirst(streamOf42(), String.class,
                value -> value.length() >= 2)).hasValue("42");
    }

    private Stream<? extends Number> newNumberStream() {
        return Stream.of(24, 42L, 64, 84.0, 96L, 256.0);
    }

    private List<Number> newNumberList() {
        return newNumberStream().collect(toList());
    }

    @Value
    private static class A {
        int id;
    }

    @Value
    private static class B {
        int id;
    }

    @Value
    private static class C {
        int id;
    }

    private Stream<Object> newStreamOfABC() {
        return Stream.of(new A(1), new A(2), new C(3), new A(4), new A(5),
                new C(6), new B(7), new A(8));
    }

    private Stream<? extends Serializable> streamOf42() {
        return Stream.of(42, 42.0, "42");
    }
}