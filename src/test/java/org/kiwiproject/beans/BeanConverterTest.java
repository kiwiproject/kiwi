package org.kiwiproject.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.data.MapEntry.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    void testBasicConvertMapToTargetType_WithExclusions() {
        var input = Map.of(
                "numberField", 1,
                "stringField", "foo",
                "mapField", Map.of("innerFoo", "innerBar")
        );

        var converter = new BeanConverter<Map<String, Object>>();
        converter.setExclusionList(Set.of("numberField"));

        var testData = converter.convert(input, new TestData());

        assertThat(testData.getNumberField())
                .describedAs("numberField should have been excluded!")
                .isNull();
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

    @Nested
    class GetPropertyMapper {

        private BeanConverter<TestData> converter;
        private TestData testData;

        @BeforeEach
        void setUp() {
            converter = new BeanConverter<>();
            converter.addPropertyMapper("stringField", testData -> testData.getStringField() + "!!!");
            converter.addPropertyMapper("numberField", testData -> testData.getNumberField() * 5);

            testData = constructTestData();
        }

        @Test
        void shouldGetPropertyMapper() {
            Function<TestData, String> stringFieldMapper = converter.getPropertyMapper("stringField");
            var convertedString = stringFieldMapper.apply(testData);
            var expectedString = testData.getStringField() + "!!!";
            assertThat(convertedString).isEqualTo(expectedString);

            Function<TestData, Integer> numberFieldMapper = converter.getPropertyMapper("numberField");
            var convertedInteger = numberFieldMapper.apply(testData);
            var expectedNumber = testData.getNumberField() * 5;
            assertThat(convertedInteger).isEqualTo(expectedNumber);
        }

        @Test
        void shouldThrowClassCastException_WhenGivenInvalidResultType() {
            Function<TestData, Double> badResultTypeMapper = getMapperWithInvalidType();

            // The following MUST declare a variable else the exception is not thrown.
            // It does not, however, matter whether it is declared with an explicit type.
            assertThatThrownBy(() -> {
                //noinspection unused
                var aDouble = badResultTypeMapper.apply(testData);
            }).describedAs("should throw when try to assign result whether explicit type or using LVTI (var)")
                    .isExactlyInstanceOf(ClassCastException.class);
        }

        @Test
        void shouldNotThrow_GivenInvalidResultType_WhenResultNotAssigned() {
            Function<TestData, Double> badResultTypeMapper = getMapperWithInvalidType();

            assertThatCode(() -> badResultTypeMapper.apply(testData))
                    .describedAs("should not throw if mapper is called but not assigned (which admittedly is not very useful)")
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldNotThrow_GivenInvalidResultType_WhenAssignedToSuperclass() {
            Function<TestData, Double> badResultTypeMapper = getMapperWithInvalidType();

            assertThatCode(() -> {
                Number num = badResultTypeMapper.apply(testData);
                assertThat(num).isEqualTo(testData.getNumberField() * 5);
            }).describedAs("should not throw exception if assigned type is Object")
                    .doesNotThrowAnyException();
        }

        // Declare mapper to return incorrect return type (Double instead of Integer)
        private Function<TestData, Double> getMapperWithInvalidType() {
            return converter.getPropertyMapper("numberField");
        }
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
