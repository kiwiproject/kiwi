package org.kiwiproject.json;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("FlexibleJsonModel")
@ExtendWith(SoftAssertionsExtension.class)
class FlexibleJsonModelTest {

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

    @Nested
    class Deserialization {

        @Test
        void shouldInflateObject_WithExplicitProperties(SoftAssertions softly) {
            var json = fixture("FlexibleJsonModelTests/customObject.json");

            var customObject = JSON_HELPER.toObject(json, CustomObject.class);

            softly.assertThat(customObject.getMyProp1()).isEqualTo("testing123");
            softly.assertThat(customObject.getMyProp2()).isEqualTo(99);
            assertThat(customObject.getExtraFields()).containsOnly(
                    entry("extraProp1", 42),
                    entry("extraProp2", "pizza")
            );
        }

        @Test
        void shouldInflateObject_WithNoExplicitProperties() {
            var json = fixture("FlexibleJsonModelTests/customObject.json");

            var noPropsObject = JSON_HELPER.toObject(json, NoPropsObject.class);
            assertThat(noPropsObject.getExtraFields()).containsOnly(
                    entry("myProp1", "testing123"),
                    entry("myProp2", 99),
                    entry("extraProp1", 42),
                    entry("extraProp2", "pizza")
            );
        }
    }

    @Nested
    class Serialization {

        @Test
        void shouldWriteJson_WithExplicitProperties() {
            var customObject = new CustomObject("value 1", 84);
            customObject.setExtraFields("a", 1);
            customObject.setExtraFields("z", 26);

            var json = JSON_HELPER.toJson(customObject, JsonHelper.OutputFormat.PRETTY);

            var mapFromJson = JSON_HELPER.toMap(json);

            assertThat(mapFromJson).containsExactly(
                    entry("myProp1", "value 1"),
                    entry("myProp2", 84),
                    entry("a", 1),
                    entry("z", 26)
            );
        }

        @Test
        void shouldWriteJson_WithNoExplicitProperties() {
            var noPropsObject = new NoPropsObject();
            noPropsObject.setExtraFields("e", 5);
            noPropsObject.setExtraFields("f", 6);
            noPropsObject.setExtraFields("g", 7);

            var json = JSON_HELPER.toJson(noPropsObject, JsonHelper.OutputFormat.PRETTY);

            var mapFromJson = JSON_HELPER.toMap(json);

            assertThat(mapFromJson).containsExactly(
                    entry("e", 5),
                    entry("f", 6),
                    entry("g", 7)
            );
        }
    }

    @Nested
    class ToStringMethod {

        // These tests rely on the subclasses calling super.toString()

        @Test
        void shouldIncludeExtraProperties() {
            var noPropsObject = new NoPropsObject();
            noPropsObject.setExtraFields("e", 5);
            noPropsObject.setExtraFields("f", 6);

            var str = noPropsObject.toString();

            assertThat(str).contains("e=5", "f=6");
        }

        @Test
        void shouldIncludeDefinedAndExtraProperties() {
            var customObject = new CustomObject("some value", 4242);
            customObject.setExtraFields("extra1", "extra-val-1");
            customObject.setExtraFields("extra2", "extra-val-2");

            var str = customObject.toString();

            assertThat(str).contains(
                    "myProp1=some value",
                    "myProp2=4242",
                    "extra1=extra-val-1",
                    "extra2=extra-val-2"
            );
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @ToString(callSuper = true)
    private static class CustomObject extends FlexibleJsonModel {
        String myProp1;
        int myProp2;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @ToString(callSuper = true)
    private static class NoPropsObject extends FlexibleJsonModel {
    }
}