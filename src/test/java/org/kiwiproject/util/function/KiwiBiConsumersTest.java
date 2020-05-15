package org.kiwiproject.util.function;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.UUIDs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

class KiwiBiConsumersTest {

    @Test
    void shouldProvideNoOpBiConsumer() {
        var scaffold = new BiConsumerNoOpScaffold();
        var originalNumbers = List.copyOf(scaffold.getNumbers());
        var originalText = List.copyOf(scaffold.getText());
        assertThat(originalNumbers).hasSize(1);
        assertThat(originalText).hasSize(1);

        scaffold.mutateValues(KiwiBiConsumers.noOp());
        assertThat(scaffold.getNumbers()).isEqualTo(originalNumbers);
        assertThat(scaffold.getText()).isEqualTo(originalText);

        scaffold.mutateValues();
        assertThat(scaffold.getNumbers()).hasSize(2).isNotEqualTo(originalNumbers);
        assertThat(scaffold.getText()).hasSize(2).isNotEqualTo(originalText);
    }

    static class BiConsumerNoOpScaffold {

        @Getter
        private final List<Integer> numbers = new ArrayList<>();

        @Getter
        private final List<String> text = new ArrayList<>();

        public BiConsumerNoOpScaffold() {
            mutateValues();
        }

        void mutateValues() {
            mutateValues((numbers, text) -> {
                numbers.add(ThreadLocalRandom.current().nextInt() % 100);
                text.add(UUIDs.randomUUIDString());
            });
        }

        void mutateValues(BiConsumer<List<Integer>, List<String>> block) {
            block.accept(numbers, text);
        }
    }
}
