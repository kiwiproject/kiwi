package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

@DisplayName("KiwiObjects")
class KiwiObjectsTest {

    @Nested
    class FirstNonNullOrNull {

        @Nested
        class ShouldReturnNull {

            @Test
            void whenTwoArgs_AreNull() {
                String str = KiwiObjects.firstNonNullOrNull(null, null);
                assertThat(str).isNull();
            }

            @Test
            void whenThreeArgs_AreNull() {
                String third = null;
                //noinspection ConstantConditions
                String str = KiwiObjects.firstNonNullOrNull(null, null, third);
                assertThat(str).isNull();
            }

            @Test
            void whenThirdArgument_HasCast() {
                String str = KiwiObjects.firstNonNullOrNull(null, null, (String) null);
                assertThat(str).isNull();
            }

            @Test
            void whenNullThirdArgument_IsAmbiguous_ItThrowsNPE() {
                //noinspection ConfusingArgumentToVarargsMethod
                assertThatNullPointerException()
                        .isThrownBy(() -> KiwiObjects.firstNonNullOrNull(null, null, null));
            }

            @Test
            void whenFourArgs_AreNull() {
                String str = KiwiObjects.firstNonNullOrNull(null, null, null, null);
                assertThat(str).isNull();
            }

            @Test
            void whenLotsOfArgs_AreNull() {
                String str = KiwiObjects.firstNonNullOrNull(null, null, null, null, null, null, null, null);
                assertThat(str).isNull();
            }
        }

        @Nested
        class ShouldReturnFirstNonNull {

            @Test
            void whenFirstObject_IsNonNull() {
                String str = KiwiObjects.firstNonNullOrNull("hello", null, null, null);
                assertThat(str).isEqualTo("hello");
            }

            @Test
            void whenSecondObject_IsNonNull() {
                String str = KiwiObjects.firstNonNullOrNull(null, "world", null, null);
                assertThat(str).isEqualTo("world");
            }

            @Test
            void whenThirdObject_IsNonNull() {
                String str = KiwiObjects.firstNonNullOrNull(null, null, "hello", null);
                assertThat(str).isEqualTo("hello");
            }

            @Test
            void whenNthObject_IsNonNull() {
                String str = KiwiObjects.firstNonNullOrNull(null, null, null, null, null, null, "hello");
                assertThat(str).isEqualTo("hello");
            }
        }
    }

    @Nested
    class FirstSuppliedNonNullOrNull {

        private final Supplier<String> nullStringSupplier = () -> null;
        private final Supplier<String> stringSupplier = () -> "hello";

        @Nested
        class ShouldReturnNull {

            @Test
            void whenTwoSuppliers_ReturnNull() {
                String str = KiwiObjects.firstSuppliedNonNullOrNull(nullStringSupplier, nullStringSupplier);
                assertThat(str).isNull();
            }

            @Test
            void whenThreeSuppliers_ReturnNull() {
                String str = KiwiObjects.firstSuppliedNonNullOrNull(nullStringSupplier, nullStringSupplier, nullStringSupplier);
                assertThat(str).isNull();
            }

            @Test
            void whenLotsOfSuppliers_ReturnNull() {
                String str = KiwiObjects.firstSuppliedNonNullOrNull(
                        nullStringSupplier, nullStringSupplier, nullStringSupplier, nullStringSupplier, nullStringSupplier, nullStringSupplier
                );
                assertThat(str).isNull();
            }
        }

        @Nested
        class ShouldReturnFirstSuppliedNonNull {

            @Test
            void whenFirstSupplier_ReturnsNonNull() {
                String str = KiwiObjects.firstSuppliedNonNullOrNull(stringSupplier, nullStringSupplier);
                assertThat(str).isEqualTo("hello");
            }

            @Test
            void whenSecondSupplier_ReturnsNonNull() {
                String str = KiwiObjects.firstSuppliedNonNullOrNull(nullStringSupplier, stringSupplier);
                assertThat(str).isEqualTo("hello");
            }

            @Test
            void whenThirdSupplier_ReturnsNonNull() {
                String str = KiwiObjects.firstSuppliedNonNullOrNull(nullStringSupplier, nullStringSupplier, stringSupplier);
                assertThat(str).isEqualTo("hello");
            }

            @Test
            void whenNthSupplier_ReturnsNonNull() {
                String str = KiwiObjects.firstSuppliedNonNullOrNull(
                        nullStringSupplier, nullStringSupplier, nullStringSupplier, nullStringSupplier, stringSupplier
                );
                assertThat(str).isEqualTo("hello");
            }
        }
    }
}