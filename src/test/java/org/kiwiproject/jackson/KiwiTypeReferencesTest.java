package org.kiwiproject.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.json.JsonHelper;

import java.util.List;
import java.util.Map;

@DisplayName("KiwiTypeReferences")
class KiwiTypeReferencesTest {

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

    @Value
    private static class Car {
        String make;
        String model;
        int year;
        double miles;
        String bodyStyle;
    }

    @Test
    void shouldSupportMapOfStringToObject() {
        var modelS = new Car("Tesla", "S", 2015, 20_500.42, "sedan");
        var json = JSON_HELPER.toJson(modelS);

        var modelSMap = JSON_HELPER.toObject(json, KiwiTypeReferences.MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);

        assertThat(modelSMap).containsOnly(
                entry("make", "Tesla"),
                entry("model", "S"),
                entry("year", 2015),
                entry("miles", 20_500.42),
                entry("bodyStyle", "sedan")
        );
    }

    @Test
    void shouldSupportListOfStringObjectMaps() {
        var modelS = new Car("Tesla", "S", 2015, 20_500.42, "sedan");
        var modelX = new Car("Tesla", "X", 2019, 14_542.84, "SUV");
        var json = JSON_HELPER.toJson(List.of(modelS, modelX));

        var carsList = JSON_HELPER.toObject(json, KiwiTypeReferences.LIST_OF_MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);

        assertThat(carsList).containsOnly(
                Map.of("make", "Tesla", "model", "S", "year", 2015, "miles", 20_500.42, "bodyStyle", "sedan"),
                Map.of("make", "Tesla", "model", "X", "year", 2019, "miles", 14_542.84, "bodyStyle", "SUV")
        );
    }

    @Test
    void shouldSupportListOfStrings() {
        var json = JSON_HELPER.toJson(List.of("Model 3", "Model S", "Model X", "Model Y"));

        var models = JSON_HELPER.toObject(json, KiwiTypeReferences.LIST_OF_STRING_TYPE_REFERENCE);
        assertThat(models).containsExactly("Model 3", "Model S", "Model X", "Model Y");
    }

    @Test
    void shouldSupportListOfIntegers() {
        var json = JSON_HELPER.toJson(List.of(2015, 2013, 2020, 2019));

        var years = JSON_HELPER.toObject(json, KiwiTypeReferences.LIST_OF_INTEGER_TYPE_REFERENCE);
        assertThat(years).containsExactly(2015, 2013, 2020, 2019);
    }

    @Test
    void shouldSupportListOfLongs() {
        var json = JSON_HELPER.toJson(List.of(2015, 2013, 2020, 2019));

        var years = JSON_HELPER.toObject(json, KiwiTypeReferences.LIST_OF_LONG_TYPE_REFERENCE);
        assertThat(years).containsExactly(2015L, 2013L, 2020L, 2019L);
    }

    @Test
    void shouldSupportListOfDoubles() {
        var json = JSON_HELPER.toJson(List.of(20_500.42, 14_542.84, 6_900.42));

        var miles = JSON_HELPER.toObject(json, KiwiTypeReferences.LIST_OF_DOUBLE_TYPE_REFERENCE);
        assertThat(miles).containsExactly(20_500.42, 14_542.84, 6_900.42);
    }

    @Test
    void shouldSupportListOfFloats() {
        var json = JSON_HELPER.toJson(List.of(20_500.42, 14_542.84, 6_900.42));

        var miles = JSON_HELPER.toObject(json, KiwiTypeReferences.LIST_OF_FLOAT_TYPE_REFERENCE);
        assertThat(miles).containsExactly(20_500.42F, 14_542.84F, 6_900.42F);
    }

    @Test
    void shouldSupportListOfBooleans() {
        var json = JSON_HELPER.toJson(List.of(true, false, false, true));

        var bools = JSON_HELPER.toObject(json, KiwiTypeReferences.LIST_OF_BOOLEAN_TYPE_REFERENCE);
        assertThat(bools).containsExactly(true, false, false, true);
    }

    @Test
    void shouldSupportSetOfStrings() {
        var json = JSON_HELPER.toJson(List.of("sedan", "sedan", "SUV", "sedan", "SUV", "SUV", "sedan"));

        var bodyStyles = JSON_HELPER.toObject(json, KiwiTypeReferences.SET_OF_STRING_TYPE_REFERENCE);
        assertThat(bodyStyles).containsExactlyInAnyOrder("sedan", "SUV");
    }
}
