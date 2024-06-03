package org.kiwiproject.util.function;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.UUIDs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@DisplayName("KiwiConsumers")
class KiwiConsumersTest {

    @Test
    void shouldProvideNoOpConsumer() {
        var scaffold = new ConsumerNoOpScaffold();
        var originalText = List.copyOf(scaffold.getText());
        assertThat(originalText).hasSize(1);

        scaffold.mutateValue(KiwiConsumers.noOp());
        assertThat(scaffold.getText()).isEqualTo(originalText);

        scaffold.mutateValue();
        assertThat(scaffold.getText()).hasSize(2).isNotEqualTo(originalText);
    }

    @Getter
    static class ConsumerNoOpScaffold {

        private final List<String> text = new ArrayList<>();

        ConsumerNoOpScaffold() {
            mutateValue();
        }

        void mutateValue() {
            mutateValue(theText -> theText.add(UUIDs.randomUUIDString()));
        }

        void mutateValue(Consumer<List<String>> block) {
            block.accept(text);
        }
    }
}
