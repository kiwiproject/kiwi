package org.kiwiproject.hibernate.usertype;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Value;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.Serializable;

@DisplayName("ArrayUserTypes (Unit)")
class ArrayUserTypesUnitTest {

    private AbstractArrayUserType arrayUserType;

    @BeforeEach
    void setUp() {
        arrayUserType = new TextArrayUserType();
    }

    @Nested
    class AbstractArrayUserTypeMethods {

        @Nested
        @ExtendWith(SoftAssertionsExtension.class)
        class EqualsAndHashCode {

            @Test
            void shouldCompareUsingObjectsEquals(SoftAssertions softly) {
                var obj1 = new Foo("an object", 42);
                var obj2 = new Foo("an object", 42);
                var obj3 = new Foo("another object", 84);

                softly.assertThat(arrayUserType.equals(obj1, obj1)).isTrue();
                softly.assertThat(arrayUserType.equals(obj1, obj2)).isTrue();
                softly.assertThat(arrayUserType.equals(obj1, obj2)).isTrue();
                softly.assertThat(arrayUserType.equals(obj1, obj3)).isFalse();
                softly.assertThat(arrayUserType.equals(obj2, obj3)).isFalse();
            }

            @Test
            void shouldReturnZeroHashCode_GivenNullObject() {
                assertThat(arrayUserType.hashCode(null)).isZero();
            }

            @Test
            void shouldReturnObjectHashCode_GivenNonNullObject() {
                var obj = new Foo("object", 42);
                assertThat(arrayUserType.hashCode(obj)).hasSameHashCodeAs(obj);
            }

            @Value
            class Foo {
                String name;
                int number;
            }
        }

        @Nested
        class Disassemble {

            @Test
            void shouldCastAndReturnSameInstance() {
                var entity = new SerializableObject();
                assertThat(arrayUserType.disassemble(entity)).isSameAs(entity);
            }
        }

        @Nested
        class Assemble {

            @Test
            void shouldReturnSameInstance() {
                var cached = new SerializableObject();
                var owner = new Object();
                assertThat(arrayUserType.assemble(cached, owner)).isSameAs(cached);
            }
        }

        @Nested
        class Replace {

            @Test
            void shouldReturnSameInstance() {
                var original = new Object();
                assertThat(arrayUserType.replace(original, null, null)).isSameAs(original);
            }
        }

        class SerializableObject implements Serializable {
        }
    }

    @Nested
    class BigintArrayUserTypeMethods {

        private BigintArrayUserType bigintArrayUserType;

        @BeforeEach
        void setUp() {
            bigintArrayUserType = new BigintArrayUserType();
        }

        @Test
        void shouldReturnExpectedDatabaseTypeName() {
            assertThat(bigintArrayUserType.databaseTypeName()).isEqualTo("bigint");
        }

        @Test
        void shouldReturnExpectedClass() {
            assertThat(bigintArrayUserType.returnedClass()).isEqualTo(Long[].class);
        }
    }

    @Nested
    class TextArrayUserTypeMethods {

        private TextArrayUserType textArrayUserType;

        @BeforeEach
        void setUp() {
            textArrayUserType = new TextArrayUserType();
        }

        @Test
        void shouldReturnExpectedDatabaseTypeName() {
            assertThat(textArrayUserType.databaseTypeName()).isEqualTo("text");
        }

        @Test
        void shouldReturnExpectedClass() {
            assertThat(textArrayUserType.returnedClass()).isEqualTo(String[].class);
        }
    }
}
