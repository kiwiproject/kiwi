package org.kiwiproject.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.data.MapEntry.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.collect.KiwiMaps;
import org.springframework.beans.TypeMismatchException;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@DisplayName("BeanConverter")
class BeanConverterTest {

    private TestData constructTestData() {
        return new TestData(1, "foo", KiwiMaps.newHashMap("innerFoo", "innerBar"));
    }

    @Test
    void testBasicConvertMapToTargetType() {
        var input = Map.of(
                "numberField", 1,
                "stringField", "foo",
                "mapField", Map.of("innerFoo", "innerBar")
        );

        var converter = new BeanConverter<Map<String, Object>>();
        var testData = converter.convert(input, new TestData());

        assertThat(testData.getNumberField()).isEqualTo(1);
        assertThat(testData.getStringField()).isEqualTo("foo");
        assertThat(testData.getMapField()).contains(entry("innerFoo", "innerBar"));
    }

    @Test
    void testBasicConvertTypeToMap() {
        var converter = new BeanConverter<TestData>();

        var output = converter.convert(constructTestData(), KiwiMaps.newHashMap());

        assertThat(output).contains(
                entry("numberField", 1),
                entry("stringField", "foo"),
                entry("mapField", Map.of("innerFoo", "innerBar"))
        );
    }

    @Test
    void testBasicConvertTypeToMap_WithExclusions() {
        var converter = new BeanConverter<TestData>();
        converter.setExclusionList(Set.of("numberField", "mapField"));

        assertThat(converter.getExclusionList()).contains("numberField", "mapField");

        var output = converter.convert(constructTestData(), KiwiMaps.newHashMap());

        assertThat(output)
                .contains(entry("stringField", "foo"))
                .doesNotContainKeys("numberField", "mapField");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testFailOnErrorHandling() {
        var input = Map.of("numberField", "NOT A NUMBER");

        var converter = new BeanConverter<Map>();
        assertThat(converter.isFailOnError()).isFalse(); // default value

        converter.setFailOnError(true);
        assertThat(converter.isFailOnError()).isTrue();

        assertThat(catchThrowable(() -> converter.convert(input, new TestData())))
                .isExactlyInstanceOf(TypeMismatchException.class);
    }

    @Test
    void testBasicConvertTypeToOtherType() {
        var converter = new BeanConverter<TestData>();

        var otherTestData = converter.convert(constructTestData(), new OtherTestData());

        assertThat(otherTestData.getNumberField()).isEqualTo(1);
        assertThat(otherTestData.getStringField()).isEqualTo("foo");
        assertThat(otherTestData.getMapField()).contains(entry("innerFoo", "innerBar"));
    }

    @Test
    void testConversionWithCustomRule() {
        var converter = new BeanConverter<TestData>();
        converter.addPropertyMapper("numberField", data -> {
            // increment by 1000
            var val = data.getNumberField() + 1000;
            data.setNumberField(val);
            return val;
        });

        converter.addPropertyMapper("stringField", prefixTestDataStringFunc());

        converter.addPropertyMapper("mapField", data -> {
            var m = data.getMapField();
            m.put("test", "1234");
            return m;
        });

        var result = converter.convert(constructTestData());

        assertThat(result.getNumberField()).isEqualTo(1001);
        assertThat(result.getStringField()).isEqualTo("test:foo");
        assertThat(result.getMapField()).contains(entry("innerFoo", "innerBar"), entry("test", "1234"));
    }

    @Test
    void shouldNotAllowRegisteringMapper_WhenSamePropertyAlreadyRegistered() {
        var converter = new BeanConverter<TestData>();

        assertThatCode(() -> converter.addPropertyMapper("stringField", prefixTestDataStringFunc()))
                .doesNotThrowAnyException();

        assertThatIllegalStateException()
                .isThrownBy(() -> converter.addPropertyMapper("stringField", testData -> "foo"))
                .withMessage("Mapper already registered for property: stringField");
    }

    private Function<TestData, String> prefixTestDataStringFunc() {
        return data -> {
            // prefix with 'test'
            var val = "test:" + data.getStringField();
            data.setStringField(val);
            return val;
        };
    }

    @Test
    void testConversionWithNullInput() {
        var converter = new BeanConverter<TestData>();

        assertThat(converter.convert(null)).isNull();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestData {
        private Integer numberField;
        private String stringField;
        private Map<String, Object> mapField;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OtherTestData {
        private Integer numberField;
        private String stringField;
        private Map<String, Object> mapField;
    }
}
