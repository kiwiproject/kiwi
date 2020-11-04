package org.kiwiproject.hibernate.usertype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.Value;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.hibernate.type.SerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.Serializable;

@DisplayName("JSONBUserType (Unit)")
class JSONBUserTypeUnitTest {

    private JSONBUserType jsonbUserType;

    @BeforeEach
    void setUp() {
        jsonbUserType = new JSONBUserType();
    }

    @Test
    void shouldReturnObjectAsExpectedClass() {
        assertThat(jsonbUserType.returnedClass()).isEqualTo(Object.class);
    }

    @Nested
    @ExtendWith(SoftAssertionsExtension.class)
    class EqualsAndHashCode {

        @Test
        void shouldCompareUsingObjectsEquals(SoftAssertions softly) {
            var obj1 = new Bar("an object", 42);
            var obj2 = new Bar("an object", 42);
            var obj3 = new Bar("another object", 84);

            softly.assertThat(jsonbUserType.equals(obj1, obj1)).isTrue();
            softly.assertThat(jsonbUserType.equals(obj1, obj2)).isTrue();
            softly.assertThat(jsonbUserType.equals(obj1, obj2)).isTrue();
            softly.assertThat(jsonbUserType.equals(obj1, obj3)).isFalse();
            softly.assertThat(jsonbUserType.equals(obj2, obj3)).isFalse();
        }

        @Test
        void shouldThrowIllegalArgumentException_GivenNullObject() {
            //noinspection ResultOfMethodCallIgnored
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> jsonbUserType.hashCode(null));
        }

        @Test
        void shouldReturnObjectHashCode_GivenNonNullObject() {
            var obj = new Bar("object", 42);
            assertThat(jsonbUserType.hashCode(obj)).hasSameHashCodeAs(obj);
        }

        @Value
        class Bar {
            String name;
            int number;
        }
    }

    @Nested
    class Disassemble {

        @Test
        void shouldReturnSameInstance_WhenSerializable() {
            var entity = new SerializableObject();
            assertThat(jsonbUserType.disassemble(entity)).isSameAs(entity);
        }

        @Test
        void shouldThrowSerializationException_GivenNonSerializableObject() {
            var entity = new NotSerializableObject();
            assertThatThrownBy(() -> jsonbUserType.disassemble(entity))
                    .isExactlyInstanceOf(SerializationException.class);
        }
    }

    @Nested
    class Assemble {

        @Test
        void shouldReturnSameInstance() {
            var cached = new SerializableObject();
            var owner = new Object();
            assertThat(jsonbUserType.assemble(cached, owner)).isSameAs(cached);
        }
    }

    @Nested
    class Replace {

        @Test
        void shouldReturnSameInstance() {
            var original = new Object();
            assertThat(jsonbUserType.replace(original, null, null)).isSameAs(original);
        }
    }

    private static class SerializableObject implements Serializable {
    }

    private static class NotSerializableObject {
    }
}
