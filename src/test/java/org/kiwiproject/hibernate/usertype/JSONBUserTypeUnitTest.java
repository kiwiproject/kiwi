package org.kiwiproject.hibernate.usertype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("JSONBUserType (Unit)")
class JSONBUserTypeUnitTest {

    private JSONBUserType jsonbUserType;

    @BeforeEach
    void setUp() {
        jsonbUserType = new JSONBUserType();
    }

    @Test
    void shouldReturnObjectAsExpectedClass() {
        assertThat(jsonbUserType.returnedClass()).isEqualTo(String.class);
    }

    @Nested
    @ExtendWith(SoftAssertionsExtension.class)
    class EqualsAndHashCode {

        @Test
        void shouldCompareUsingObjectsEquals(SoftAssertions softly) {
            var obj1 = "an object";
            var obj2 = "an object";
            var obj3 = "another object";

            softly.assertThat(jsonbUserType.equals(obj1, obj1)).isTrue();
            softly.assertThat(jsonbUserType.equals(obj1, obj2)).isTrue();
            softly.assertThat(jsonbUserType.equals(obj1, obj2)).isTrue();
            softly.assertThat(jsonbUserType.equals(obj1, obj3)).isFalse();
            softly.assertThat(jsonbUserType.equals(obj2, obj3)).isFalse();
        }

        @Test
        void shouldThrowIllegalArgumentException_GivenNullObject() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> jsonbUserType.hashCode(null));
        }

        @Test
        void shouldReturnObjectHashCode_GivenNonNullObject() {
            var obj = "{}";
            assertThat(jsonbUserType.hashCode(obj)).hasSameHashCodeAs(obj);
        }
    }

    @Nested
    class Disassemble {

        @Test
        void shouldReturnSameInstance_WhenSerializable() {
            var entity = "{}";
            assertThat(jsonbUserType.disassemble(entity)).isSameAs(entity);
        }
    }

    @Nested
    class Assemble {

        @Test
        void shouldReturnSameInstance() {
            var cached = "{}";
            var owner = new Object();
            assertThat(jsonbUserType.assemble(cached, owner)).isSameAs(cached);
        }
    }

    @Nested
    class Replace {

        @Test
        void shouldReturnSameInstance() {
            var original = "{}";
            assertThat(jsonbUserType.replace(original, null, null)).isSameAs(original);
        }
    }

}
