package org.kiwiproject.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class KiwiIteratorsTest {

    private static final int NUMBER_OF_ITERATIONS = 5_000;
    private static final String VALUE_1 = "dark";
    private static final String VALUE_2 = "light";

    private List<String> colorShades;

    @BeforeEach
    void setUp() {
        colorShades = Lists.newArrayList(VALUE_1, VALUE_2);
    }

    @Test
    void testCycleForever_ThrowsIllegalArgumentException_WhenSupplyEmptyIterable() {
        assertThatThrownBy(() -> KiwiIterators.cycleForever(new ArrayList<>()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("need at least 1 element to cycle");
    }

    @Test
    void testCycleForever_ThrowsIllegalArgumentException_WhenDoNotPassAnyVarArgs() {
        assertThatThrownBy(KiwiIterators::cycleForever)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("need at least 1 element to cycle");
    }

    @Test
    void testCycleForever_DoesNotPermitRemovingElements() {
        var cycler = KiwiIterators.cycleForever(colorShades);

        assertThatThrownBy(cycler::remove)
                .isExactlyInstanceOf(UnsupportedOperationException.class)
                .hasMessageStartingWith("unsupported");
    }

    @Test
    void testCycleForever_ModificationsToOriginalCollection_ShouldNotAffectCycler() {
        Iterator<String> cycleForever = KiwiIterators.cycleForever(colorShades);
        colorShades.add("lighter");
        colorShades.add("medium");
        colorShades.add("darker");
        assertThat(cycleForever.next()).isIn(VALUE_1, VALUE_2);
        assertThat(cycleForever.next()).isIn(VALUE_1, VALUE_2);
        assertThat(cycleForever.next()).isIn(VALUE_1, VALUE_2);

        colorShades.clear();

        assertThat(cycleForever.next()).isIn(VALUE_1, VALUE_2);
        assertThat(cycleForever.next()).isIn(VALUE_1, VALUE_2);
        assertThat(cycleForever.next()).isIn(VALUE_1, VALUE_2);
    }

    @Test
    void testCycleForever_DoesNotSupport_forEachRemaining() {
        Iterator<String> cycleForever = KiwiIterators.cycleForever(colorShades);
        assertThatThrownBy(() -> cycleForever.forEachRemaining(System.out::println))
                .isExactlyInstanceOf(UnsupportedOperationException.class)
                .hasMessageStartingWith("unsupported");
    }

    @Test
    void testCycleForever_WhenOnlyOneElement() {
        Iterator<Integer> cycleForever = KiwiIterators.cycleForever(42);
        for (int i = 0; i < 500; i++) {
            assertThat(cycleForever.next()).isEqualTo(42);
        }
    }

    @Test
    void testCycleForever_ThroughLongList() {
        Iterator<Integer> cycleForever = KiwiIterators.cycleForever(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertCyclesThroughNumbersInNaturalOrder(cycleForever);
    }

    private void assertCyclesThroughNumbersInNaturalOrder(Iterator<Integer> cycleForever) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertThat(cycleForever.next()).isEqualTo(j);
            }
        }
    }

    @Test
    void testCycleForever_CyclingManyTimes_UsingOnlySingleThread() {
        Iterator<String> cycleForever = KiwiIterators.cycleForever(colorShades);
        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            String next = cycleForever.next();
            assertThat(next).isEqualTo(isEven(i) ? VALUE_1 : VALUE_2);
        }
    }

    private boolean isEven(int value) {
        return (value & 1) == 0;
    }

    @Test
    void testCycleForever_CyclingManyTimes_UsingMultipleThreads_ShouldNeverFail()
            throws InterruptedException, TimeoutException, ExecutionException {

        Iterator<String> cycleForever = KiwiIterators.cycleForever(colorShades);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            completionService.submit(cycleForever::next);
        }

        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            Future<String> future = completionService.poll(100, TimeUnit.MILLISECONDS);
            assertPollDidNotTimeoutWaitingForAValue(future);

            String value = future.get(5, TimeUnit.MILLISECONDS);
            assertThat(value).isIn(VALUE_1, VALUE_2);
        }

        List<Runnable> tasksAwaitingExecution = executorService.shutdownNow();
        assertThat(tasksAwaitingExecution).isEmpty();
    }

    private void assertPollDidNotTimeoutWaitingForAValue(Future<String> future) {
        assertThat(future).isNotNull();
    }

}