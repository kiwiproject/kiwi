package org.kiwiproject.dropwizard.lifecycle;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@DisplayName("ManagedSpringContext")
class ManagedSpringContextTest {

    @Nested
    class NotClosingParents {

        @Test
        void shouldRequireNonNullContext() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ManagedSpringContext.notClosingParents(null))
                    .withMessage("context must not be null");
        }

        @Test
        void start_shouldBeNoOp() {
            var context = mock(ConfigurableApplicationContext.class);
            ManagedSpringContext.notClosingParents(context).start();
            verifyNoInteractions(context);
        }

        @Test
        void stop_shouldCloseOnlyTheContext() {
            var parent = mock(ConfigurableApplicationContext.class);
            var context = mock(ConfigurableApplicationContext.class);
            when(context.getParent()).thenReturn(parent);

            ManagedSpringContext.notClosingParents(context).stop();

            verify(context).close();
            verifyNoInteractions(parent);
        }
    }

    @Nested
    class ClosingParents {

        @Test
        void shouldRequireNonNullContext() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ManagedSpringContext.closingParents(null))
                    .withMessage("context must not be null");
        }

        @Test
        void start_shouldBeNoOp() {
            var context = mock(ConfigurableApplicationContext.class);
            ManagedSpringContext.closingParents(context).start();
            verifyNoInteractions(context);
        }

        @Test
        void stop_shouldCloseContextWhenThereAreNoParents() {
            var context = mock(ConfigurableApplicationContext.class);
            when(context.getParent()).thenReturn(null);

            ManagedSpringContext.closingParents(context).stop();

            verify(context).close();
        }

        @Test
        void stop_shouldCloseContextAndSingleConfigurableParent() {
            var parent = mock(ConfigurableApplicationContext.class);
            when(parent.getParent()).thenReturn(null);

            var context = mock(ConfigurableApplicationContext.class);
            when(context.getParent()).thenReturn(parent);

            ManagedSpringContext.closingParents(context).stop();

            verify(context).close();
            verify(parent).close();
        }

        @Test
        void stop_shouldCloseContextAndMultipleConfigurableParents() {
            var grandparent = mock(ConfigurableApplicationContext.class);
            when(grandparent.getParent()).thenReturn(null);

            var parent = mock(ConfigurableApplicationContext.class);
            when(parent.getParent()).thenReturn(grandparent);

            var context = mock(ConfigurableApplicationContext.class);
            when(context.getParent()).thenReturn(parent);

            ManagedSpringContext.closingParents(context).stop();

            verify(context).close();
            verify(parent).close();
            verify(grandparent).close();
        }

        @Test
        void stop_shouldStopWalkingParentChain_WhenParentIsNotConfigurable() {
            var nonConfigurableParent = mock(ApplicationContext.class);

            var context = mock(ConfigurableApplicationContext.class);
            when(context.getParent()).thenReturn(nonConfigurableParent);

            ManagedSpringContext.closingParents(context).stop();

            verify(context).close();
            verifyNoInteractions(nonConfigurableParent);
        }
    }
}
