package org.kiwiproject.json;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.testing.FixtureHelpers;
import lombok.Value;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.collect.KiwiMaps;
import org.kiwiproject.json.JsonHelper.MergeOption;
import org.kiwiproject.json.JsonHelper.OutputFormat;

import java.util.List;
import java.util.Map;

/**
 * Contains tests of the more "advanced" JsonHelper functionality: listing paths, getting data from specific paths,
 * merging and updating objects, comparing difference between objects, etc.
 */
@DisplayName("JsonHelper Advanced Features")
@ExtendWith(SoftAssertionsExtension.class)
class JsonHelperAdvancedFeaturesTest {

    private static final String SAMPLE_JSON = FixtureHelpers.fixture("JsonHelperTests/sample.json");

    private static final TypeReference<List<String>> LIST_OF_STRING_TYPE_REF = new TypeReference<>() {
    };

    private JsonHelper jsonHelper;

    @BeforeEach
    void setUp() {
        jsonHelper = JsonHelper.newDropwizardJsonHelper();
    }

    @Nested
    class SerializationRoundTripSmokeTests {

        @Test
        void shouldRoundTripFromJsonToObjectAndBack(SoftAssertions softly) {
            var originalObject = newSampleObject();

            var json = jsonHelper.toJson(originalObject);
            var objectFromJson = jsonHelper.toObject(json, SampleObject.class);
            softly.assertThat(objectFromJson).isEqualTo(originalObject);

            var jsonDefaultOutputFormat = jsonHelper.toJson(originalObject, OutputFormat.DEFAULT);
            softly.assertThat(jsonDefaultOutputFormat).isEqualTo(json);

            var jsonPrettyOutputFormat = jsonHelper.toJson(originalObject, OutputFormat.PRETTY);
            softly.assertThat(jsonPrettyOutputFormat).isNotEqualTo(jsonDefaultOutputFormat);

            var objectFromPrettyJson = jsonHelper.toObject(jsonPrettyOutputFormat, SampleObject.class);
            softly.assertThat(objectFromPrettyJson).isEqualTo(originalObject);

            softly.assertThat(jsonHelper.jsonEquals(jsonDefaultOutputFormat, jsonPrettyOutputFormat)).isTrue();
        }

        @Test
        void shouldRespectJsonViews(SoftAssertions softly) {
            var originalObject = newSampleObject();

            var limitedJson = jsonHelper.toJson(originalObject, OutputFormat.DEFAULT, TestJsonViews.Limited.class);
            var limitedJsonMap = jsonHelper.toMap(limitedJson);
            softly.assertThat(limitedJsonMap).containsKey("requiresLimited");
            softly.assertThat(limitedJsonMap).doesNotContainKey("requiresAll");

            var fullJson = jsonHelper.toJson(originalObject, OutputFormat.DEFAULT, TestJsonViews.All.class);
            var fullJsonMap = jsonHelper.toMap(fullJson);
            softly.assertThat(fullJsonMap).containsKeys("requiresLimited", "requiresAll");
        }
    }

    @Nested
    class ToFlatMapOfString {

        @Test
        void shouldReturnNull_GivenNullObject() {
            assertThat(jsonHelper.toFlatMap(null)).isNull();
        }

        @Test
        void shouldFlattenToStringKeyValuePairs() {
            var sampleObject = newSampleObject();

            var map = jsonHelper.toFlatMap(sampleObject);

            assertThat(map).containsOnly(
                    entry("stringVar", "string"),
                    entry("intVar", "123"),
                    entry("doubleVar", "4.567"),
                    entry("objectVar", "string-object"),
                    entry("stringList.[0]", "a"),
                    entry("stringList.[1]", "b"),
                    entry("stringList.[2]", "c"),
                    entry("objectList.[0].foo1", "bar1"),
                    entry("objectList.[1].foo2", "bar2"),
                    entry("objectMap.key1", "val1"),
                    entry("objectMap.key2", "val2"),
                    entry("objectMap.key3", "val3"),
                    entry("objectMap.key4.[0]", "4.1"),
                    entry("objectMap.key4.[1]", "4.2"),
                    entry("requiresLimited", "requires-limited"),
                    entry("requiresAll", "requires-all")
            );
        }
    }

    @Nested
    class ToFlatMapOfGenericType {

        @Test
        void shouldReturnNull_GivenNullObject() {
            assertThat(jsonHelper.toFlatMap(null, Object.class)).isNull();
        }

        @Test
        void shouldFlattenToKeyValuePairs() {
            var sampleObject = newSampleObject();

            var map = jsonHelper.toFlatMap(sampleObject, Object.class);

            assertThat(map).containsOnly(
                    entry("stringVar", "string"),
                    entry("intVar", 123),
                    entry("doubleVar", 4.567),
                    entry("objectVar", "string-object"),
                    entry("stringList.[0]", "a"),
                    entry("stringList.[1]", "b"),
                    entry("stringList.[2]", "c"),
                    entry("objectList.[0].foo1", "bar1"),
                    entry("objectList.[1].foo2", "bar2"),
                    entry("objectMap.key1", "val1"),
                    entry("objectMap.key2", "val2"),
                    entry("objectMap.key3", "val3"),
                    entry("objectMap.key4.[0]", 4.1),
                    entry("objectMap.key4.[1]", 4.2),
                    entry("requiresLimited", "requires-limited"),
                    entry("requiresAll", "requires-all")
            );
        }
    }

    @Nested
    class Copy {

        @Test
        void shouldCopyNullObjects_AsNull(SoftAssertions softly) {
            softly.assertThat(jsonHelper.copy((SampleObject) null)).isNull();
        }

        @Test
        void shouldCopyObjects(SoftAssertions softly) {
            var original = newSampleObject();

            var copy = jsonHelper.copy(original);

            softly.assertThat(copy).isNotSameAs(original);
            softly.assertThat(copy).isEqualTo(original);
        }

        @Test
        void shouldCopyNullObjects_ToDifferentTargetClass_AsNull(SoftAssertions softly) {
            softly.assertThat(jsonHelper.copy(null, LimitedSampleObject.class)).isNull();
        }

        @Test
        void shouldCopyObjects_ToDifferentTargetClass() {
            var original = newSampleObject();

            var mapCopy = jsonHelper.copy(original, Map.class);

            var secondCopy = jsonHelper.copy(mapCopy, SampleObject.class);
            assertThat(secondCopy).isEqualTo(original);
        }

        @Test
        void shouldCopyObjects_ToTargetClassContainingFewerProperties(SoftAssertions softly) {
            var original = newSampleObject();

            var copy = jsonHelper.copy(original, LimitedSampleObject.class);

            softly.assertThat(copy.getDoubleVar()).isEqualTo(original.getDoubleVar());
            softly.assertThat(copy.getObjectVar()).isEqualTo(original.getObjectVar());
            softly.assertThat(copy.getStringList()).isEqualTo(original.getStringList());
        }
    }

    @Nested
    class CopyIgnoringPaths {

        @Test
        void shouldCopyIgnoringPaths(SoftAssertions softly) {
            var original = newSampleObject();

            var copy = jsonHelper.copyIgnoringPaths(original, SampleObject.class,
                    "stringVar", "doubleVar", "objectList");

            softly.assertThat(copy)
                    .usingRecursiveComparison()
                    .ignoringFields("stringVar", "doubleVar", "objectList")
                    .isEqualTo(original);
            softly.assertThat(copy.getStringVar()).isNull();
            softly.assertThat(copy.getDoubleVar()).isNull();
            softly.assertThat(copy.getObjectList()).isNull();
        }
    }

    @Nested
    class Convert {

        @Test
        void shouldConvert_GivenJson_AndTargetClassString() throws JsonProcessingException {
            var json = jsonHelper.getObjectMapper().writeValueAsString(newSampleObject());

            var convertedJson = jsonHelper.convert(json, String.class);

            assertThat(convertedJson).isEqualTo(json);
        }

        @Test
        void shouldConvert_GivenTargetClass(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            var limitedObject = jsonHelper.convert(sampleObject, LimitedSampleObject.class);

            softly.assertThat(limitedObject.getDoubleVar()).isEqualTo(sampleObject.getDoubleVar());
            softly.assertThat(limitedObject.getObjectVar()).isEqualTo(sampleObject.getObjectVar());
            softly.assertThat(limitedObject.getStringList()).isEqualTo(sampleObject.getStringList());
        }

        @Test
        void shouldConvert_GivenTypeReference(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            TypeReference<LimitedSampleObject> targetType = new TypeReference<>() {
            };
            var limitedObject = jsonHelper.convert(sampleObject, targetType);

            softly.assertThat(limitedObject.getDoubleVar()).isEqualTo(sampleObject.getDoubleVar());
            softly.assertThat(limitedObject.getObjectVar()).isEqualTo(sampleObject.getObjectVar());
            softly.assertThat(limitedObject.getStringList()).isEqualTo(sampleObject.getStringList());
        }

        @Test
        void shouldConvertToMap() {
            var sampleObject = newSampleObject();

            var map = jsonHelper.convertToMap(sampleObject);
            assertThat(map).containsOnly(
                    entry("stringVar", "string"),
                    entry("intVar", 123),
                    entry("doubleVar", 4.567),
                    entry("objectVar", "string-object"),
                    entry("stringList", List.of("a", "b", "c")),
                    entry("objectList", List.of(Map.of("foo1", "bar1"), Map.of("foo2", "bar2"))),
                    entry("objectMap", Map.of("key1", "val1", "key2", "val2", "key3", "val3", "key4", List.of(4.1, 4.2))),
                    entry("requiresLimited", "requires-limited"),
                    entry("requiresAll", "requires-all")
            );
        }

        @Test
        void shouldConvertFromMap(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var map = jsonHelper.convertToMap(sampleObject);

            var convertedFromMap = jsonHelper.convert(map, SampleObject.class);
            softly.assertThat(convertedFromMap).isEqualTo(sampleObject);

            softly.assertThat(jsonHelper.jsonEquals(sampleObject, convertedFromMap)).isTrue();
        }

        @Test
        void shouldConvertNull_GivenTargetClass_ToNull() {
            assertThat(jsonHelper.convert(null, Integer.class)).isNull();
        }

        @Test
        void shouldConvertNull_GivenTypeReference_ToNull() {
            var targetType = new TypeReference<String>() {
            };
            assertThat(jsonHelper.convert(null, targetType)).isNull();
        }

        @Test
        void shouldConvertNull_ToNullMap() {
            assertThat(jsonHelper.convertToMap(null)).isNull();
        }
    }

    @Nested
    class GetPath {

        @Test
        void shouldReturnNull_GivenNullInput() {
            assertThat(jsonHelper.getPath((Object) null, "somePath", String.class)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  ", "\t", "\n  \t\n "})
        void shouldReturnNull_GivenBlankInput(String value) {
            assertThat(jsonHelper.getPath(value, "anotherPath", String.class)).isNull();
        }

        @Test
        void shouldGetPath_GivenString(SoftAssertions softly) throws JsonProcessingException {
            var sampleObject = newSampleObject();
            var json = jsonHelper.getObjectMapper().writeValueAsString(sampleObject);

            assertInputObjectPathValues(softly, json, sampleObject);
        }

        @Test
        void shouldGetPath_GivenObject(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            assertInputObjectPathValues(softly, sampleObject, sampleObject);
        }

        @Test
        void shouldGetPath_GivenTypeReference() throws JsonProcessingException {

            var map = Map.of("array", List.of("a", "b", "c"));
            var json = jsonHelper.getObjectMapper().writeValueAsString(map);

            assertThat(jsonHelper.getPath(json, "array", LIST_OF_STRING_TYPE_REF))
                    .containsExactly("a", "b", "c");
        }

        private void assertInputObjectPathValues(SoftAssertions softly, Object inputObject, SampleObject sampleObject) {
            softly.assertThat(jsonHelper.getPath(inputObject, "stringVar", String.class))
                    .isEqualTo(sampleObject.getStringVar());

            softly.assertThat(jsonHelper.getPath(inputObject, "intVar", Integer.class))
                    .isEqualTo(sampleObject.getIntVar());

            softly.assertThat(jsonHelper.getPath(inputObject, "doubleVar", Double.class))
                    .isEqualTo(sampleObject.getDoubleVar());

            softly.assertThat(jsonHelper.getPath(inputObject, "objectVar", Object.class))
                    .isEqualTo(sampleObject.getObjectVar());

            var targetStringListType = new TypeReference<List<String>>() {
            };
            softly.assertThat(jsonHelper.getPath(inputObject, "stringList", targetStringListType))
                    .isEqualTo(sampleObject.getStringList());

            var targetObjectListType = new TypeReference<List<Object>>() {
            };
            softly.assertThat(jsonHelper.getPath(inputObject, "objectList", targetObjectListType))
                    .isEqualTo(sampleObject.getObjectList());

            var targetMapType = new TypeReference<Map<String, Object>>() {
            };
            softly.assertThat(jsonHelper.getPath(inputObject, "objectMap", targetMapType))
                    .isEqualTo(sampleObject.getObjectMap());
        }

        @Test
        void shouldGetNestedAndArrayPathsInObjects(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            softly.assertThat(jsonHelper.getPath(sampleObject, "stringList.[0]", String.class))
                    .isEqualTo("a");

            softly.assertThat(jsonHelper.getPath(sampleObject, "objectList.[0].foo1", String.class))
                    .isEqualTo("bar1");

            var jsonWithoutSomePaths = jsonHelper.toJsonIgnoringPaths(sampleObject,
                    "stringList.[0]", "objectList.[0].foo1");

            softly.assertThat(jsonHelper.getPath(jsonWithoutSomePaths, "stringList.[0]", String.class))
                    .describedAs("'a' was removed")
                    .isEqualTo("b");

            softly.assertThat(jsonHelper.getPath(jsonWithoutSomePaths, "objectList.[0].foo1", String.class))
                    .describedAs("'foo1' was removed")
                    .isNull();
        }

        @Test
        void shouldGetNestedAndArrayPathsInJson(SoftAssertions softly) {
            var json = FixtureHelpers.fixture("JsonHelperTests/sampleComplexObject.json");

            softly.assertThat(jsonHelper.getPath(json, "root.str", String.class))
                    .isEqualTo("string");

            softly.assertThat(jsonHelper.getPath(json, "root.bool", Boolean.class))
                    .isEqualTo(true);

            softly.assertThat(jsonHelper.getPath(json, "root.number", Double.class))
                    .isEqualTo(1.234);

            softly.assertThat(jsonHelper.getPath(json, "root.obj.foo", String.class))
                    .isEqualTo("bar");

            softly.assertThat(jsonHelper.getPath(json, "root.array.[0]", String.class))
                    .isEqualTo("a");

            softly.assertThat(jsonHelper.getPath(json, "root.array.[1]", String.class))
                    .isEqualTo("b");

            softly.assertThat(jsonHelper.getPath(json, "root.array.[2]", String.class))
                    .isEqualTo("c");

            softly.assertThat(jsonHelper.getPath(json, "root.objArray.[0].a", String.class))
                    .isEqualTo("aaa");

            softly.assertThat(jsonHelper.getPath(json, "root.objArray.[1].b", String.class))
                    .isEqualTo("bbb");

            softly.assertThat(jsonHelper.getPath(json, "root.objArray.[2].c", String.class))
                    .isEqualTo("ccc");
        }
    }

    @Nested
    class RemovePath {

        @Test
        void shouldReturnNullNode_GivenNullInput() {
            var node = jsonHelper.removePath(null, "password");
            assertThat(node.isNull()).isTrue();
        }

        @Test
        void shouldReturnEmptyTextNode_GivenBlankInput() {
            var node = jsonHelper.removePath("  ", "password");
            assertThat(node.asText()).isBlank();
        }

        @Test
        void shouldReturnEqualObject_WhenGivenPath_ThatDoesNotExist() {
            var map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");

            var updatedNode = jsonHelper.removePath(map, "x");

            assertThat(jsonHelper.jsonEquals(map, updatedNode)).isTrue();
        }

        @Test
        void shouldRemoveSimplePaths(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            var stringVarRemoved = jsonHelper.removePath(sampleObject, "stringVar");
            softly.assertThat(stringVarRemoved.has("stringVar")).isFalse();

            var doubleVarRemoved = jsonHelper.removePath(stringVarRemoved, "doubleVar");
            softly.assertThat(doubleVarRemoved.has("stringVar")).isFalse();
            softly.assertThat(doubleVarRemoved.has("doubleVar")).isFalse();
        }

        @Test
        void shouldRemoveNestedPaths(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            var node1 = jsonHelper.removePath(sampleObject, "stringList.[1]");
            var node2 = jsonHelper.removePath(node1, "objectList.[0].foo1");
            var node3 = jsonHelper.removePath(node2, "objectMap.key3");
            var node4 = jsonHelper.removePath(node3, "objectMap.key4.[0]");

            var newSampleObject = jsonHelper.convert(node4, SampleObject.class);

            softly.assertThat(newSampleObject.getStringList())
                    .containsExactly("a", "c");

            softly.assertThat(newSampleObject.getObjectList())
                    .containsExactly(
                            Map.of(),
                            Map.of("foo2", "bar2")
                    );

            softly.assertThat(newSampleObject.getObjectMap())
                    .containsOnlyKeys("key1", "key2", "key4")
                    .contains(entry("key4", List.of(4.2)));
        }
    }

    @Nested
    class UpdatePath {

        @Test
        void shouldReturnNull_GivenNullInput() {
            var updatedObject = jsonHelper.updatePath(null, "intVar", 42, SampleObject.class);
            assertThat(updatedObject).isNull();
        }

        @Test
        void shouldReturnObject_GivenEmptyJson() {
            var updatedObject = jsonHelper.updatePath("{}", "stringVar", "the new value", SampleObject.class);

            assertThat(updatedObject.getStringVar()).isEqualTo("the new value");
        }

        @Test
        void shouldUpdateSimplePaths(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            var updatedObject1 = jsonHelper.updatePath(sampleObject, "stringVar", "a new value", SampleObject.class);
            softly.assertThat(sampleObject.getStringVar()).isEqualTo("string");
            softly.assertThat(updatedObject1.getStringVar()).isEqualTo("a new value");
            softly.assertThat(updatedObject1)
                    .describedAs("should return a new instance")
                    .isNotSameAs(sampleObject);

            var updatedObject2 = jsonHelper.updatePath(updatedObject1, "intVar", 42, SampleObject.class);
            softly.assertThat(updatedObject2.getIntVar()).isEqualTo(42);
        }

        @Test
        void shouldUpdateNestedPaths(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            var updatedObject1 = jsonHelper.updatePath(sampleObject, "stringList.[2]", "z", SampleObject.class);
            softly.assertThat(sampleObject.getStringList()).isEqualTo(List.of("a", "b", "c"));
            softly.assertThat(updatedObject1.getStringList())
                    .describedAs("should insert 'z' at index 2")
                    .containsExactly("a", "b", "z", "c");
            softly.assertThat(updatedObject1)
                    .describedAs("should return a new instance")
                    .isNotSameAs(sampleObject);

            var updatedObject2 = jsonHelper.updatePath(updatedObject1, "objectMap.key3", List.of(1, 2, 3), SampleObject.class);
            softly.assertThat(updatedObject2.getObjectMap())
                    .containsEntry("key3", List.of(1, 2, 3));
        }

        @Test
        void shouldUpdateAndConvertToDifferentObject(SoftAssertions softly) {
            var sampleObject = newSampleObject();

            var limitedObject = jsonHelper.updatePath(sampleObject, "doubleVar", 9.8765, LimitedSampleObject.class);
            softly.assertThat(limitedObject.getDoubleVar())
                    .isEqualTo(9.8765);

            var lossySampleObject = jsonHelper.updatePath(limitedObject, "stringList", List.of("e", "f"), SampleObject.class);
            softly.assertThat(lossySampleObject.getDoubleVar())
                    .isEqualTo(9.8765);
            softly.assertThat(lossySampleObject.getStringList())
                    .containsExactly("e", "f");
            softly.assertThat(lossySampleObject.getIntVar()).isNull();
        }
    }

    @Nested
    class JsonDiffAndEquals {

        @Test
        void shouldCompareTwoNullObjects() {
            var diffs = jsonHelper.jsonDiff((String) null, null);

            assertThat(diffs).isEmpty();
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void shouldCompare_WhenInputObjectIsNull(SoftAssertions softly) {
            var map1 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            Map<String, Object> map2 = null;

            var diffs = jsonHelper.jsonDiff(map1, map2);

            softly.assertThat(diffs)
                    .containsOnlyKeys("a", "b", "c")
                    .containsEntry("a", newArrayList("aaa", null))
                    .containsEntry("b", newArrayList("bbb", null))
                    .containsEntry("c", newArrayList("ccc", null));

            softly.assertThat(jsonHelper.jsonEquals(map1, map2)).isFalse();
        }

        @Test
        void shouldCompareTwoEqualObjects(SoftAssertions softly) {
            var map1 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map2 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");

            var diffs = jsonHelper.jsonDiff(map1, map2);

            softly.assertThat(diffs).isEmpty();

            softly.assertThat(jsonHelper.jsonEquals(map1, map2)).isTrue();
        }

        @Test
        void shouldCompareEqualObjects_ContainingNestedPaths(SoftAssertions softly) {
            var sampleObject1 = newSampleObject();
            var sampleObject2 = newSampleObject();

            softly.assertThat(jsonHelper.jsonEquals(sampleObject1, sampleObject2))
                    .isTrue();

            var updatedObject = jsonHelper.updatePath(sampleObject1, "objectMap.key1", "updated-val1", SampleObject.class);
            softly.assertThat(jsonHelper.jsonEquals(updatedObject, sampleObject2))
                    .isFalse();

            softly.assertThat(jsonHelper.getPath(sampleObject1, "objectMap.key1", String.class))
                    .isEqualTo("val1");
            softly.assertThat(jsonHelper.getPath(updatedObject, "objectMap.key1", String.class))
                    .isEqualTo("updated-val1");
        }

        @Test
        void shouldCompareTwoObjects(SoftAssertions softly) {
            var map1 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map2 = Map.of("b", "bbb", "c", "ccc", "d", "ddd");

            var diffs = jsonHelper.jsonDiff(map1, map2);

            softly.assertThat(diffs)
                    .containsOnlyKeys("a", "d")
                    .containsEntry("a", newArrayList("aaa", null))
                    .containsEntry("d", newArrayList(null, "ddd"));

            softly.assertThat(jsonHelper.jsonEquals(map1, map2)).isFalse();
            softly.assertThat(jsonHelper.jsonEqualsIgnoringPaths(map1, map2, "a", "d")).isTrue();
        }

        @Test
        void shouldCompareTwoObjects_OfDifferentTypes(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var map = jsonHelper.toMap(SAMPLE_JSON);

            var diffs = jsonHelper.jsonDiff(sampleObject, map);

            softly.assertThat(diffs)
                    .containsOnlyKeys("requiresAll", "requiresLimited")
                    .containsEntry("requiresAll", newArrayList("requires-all", null))
                    .containsEntry("requiresLimited", newArrayList("requires-limited", null));

            softly.assertThat(jsonHelper.jsonEquals(sampleObject, map)).isFalse();
        }

        @Test
        void shouldCompareMoreThanTwoObjectsForEquality() {
            var map1 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map2 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map3 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map4 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");

            assertThat(jsonHelper.jsonEquals(map1, map2, map3, map4)).isTrue();
        }

        @Test
        void shouldCompareObjects_IgnoringFields(SoftAssertions softly) {
            var map1 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map2 = Map.of("b", "bbb", "c", "ccc", "d", "ddd");

            var diffs = jsonHelper.jsonDiff(map1, map2, "a", "d");

            softly.assertThat(diffs).isEmpty();

            softly.assertThat(jsonHelper.jsonEquals(map1, map2))
                    .describedAs("should not be equal since not ignoring fields")
                    .isFalse();
        }

        @Test
        void shouldCompareObjects_OfDifferentTypes_IgnoringFields() {
            var sampleObject = newSampleObject();
            var map = jsonHelper.toMap(SAMPLE_JSON);

            var diffs = jsonHelper.jsonDiff(sampleObject, map, "requiresAll", "requiresLimited");

            assertThat(diffs).isEmpty();
        }

        @Test
        void shouldCompare_MoreThanTwoObjects(SoftAssertions softly) {
            var map1 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map2 = Map.of("b", "bbb", "c", "ccc", "d", "ddd");
            var map3 = Map.of("b", "bbb", "c", "ccc", "d", "dddd");
            var map4 = Map.of("b", "bbb", "c", "ccc", "d", "ddd", "e", "eee");

            var diffs = jsonHelper.jsonDiff(List.of(map1, map2, map3, map4));

            softly.assertThat(diffs)
                    .containsOnlyKeys("a", "d", "e")
                    .containsEntry("a", newArrayList("aaa", null, null, null))
                    .containsEntry("d", newArrayList(null, "ddd", "dddd", "ddd"))
                    .containsEntry("e", newArrayList(null, null, null, "eee"));

            softly.assertThat(jsonHelper.jsonEquals(map1, map2)).isFalse();
            softly.assertThat(jsonHelper.jsonEqualsIgnoringPaths(map1, map2, "a", "d")).isTrue();

            softly.assertThat(jsonHelper.jsonEquals(map2, map3)).isFalse();
            softly.assertThat(jsonHelper.jsonEqualsIgnoringPaths(map2, map3, "d")).isTrue();

            softly.assertThat(jsonHelper.jsonEquals(map3, map4)).isFalse();
            softly.assertThat(jsonHelper.jsonEqualsIgnoringPaths(map3, map4, "d", "e")).isTrue();
        }
    }

    @Nested
    class JsonPathsEqual {

        @Test
        void shouldCompareSimpleObjects(SoftAssertions softly) {
            var map1 = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
            var map2 = Map.of("b", "bbb", "c", "ccc", "d", "ddd");

            softly.assertThat(jsonHelper.jsonPathsEqual(map1, map2, "a", String.class)).isFalse();
            softly.assertThat(jsonHelper.jsonPathsEqual(map1, map2, "b", String.class)).isTrue();
            softly.assertThat(jsonHelper.jsonPathsEqual(map1, map2, "c", String.class)).isTrue();
            softly.assertThat(jsonHelper.jsonPathsEqual(map1, map2, "d", String.class)).isFalse();
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldCompareObjects_ThatContainNestedProperties(SoftAssertions softly) {
            var sampleObject1 = newSampleObject();
            var sampleObject2 = newSampleObject();

            softly.assertThat(jsonHelper.jsonEquals(sampleObject1, sampleObject2))
                    .isTrue();

            softly.assertThat(jsonHelper.jsonPathsEqual(sampleObject1, sampleObject2, "stringList", String.class))
                    .isTrue();

            softly.assertThat(jsonHelper.jsonPathsEqual(sampleObject1, sampleObject2, "objectMap.key1", String.class))
                    .isTrue();

            var sampleMap = jsonHelper.toMap(SAMPLE_JSON);
            var stringList = (List<String>) sampleMap.get("stringList");
            stringList.add(0, "*");

            var objectMap = (Map<String, Object>) sampleMap.get("objectMap");
            objectMap.put("key2", "updated-key2");

            softly.assertThat(jsonHelper.jsonPathsEqual(sampleObject1, sampleMap, "stringList", String.class))
                    .isFalse();

            softly.assertThat(jsonHelper.jsonPathsEqual(sampleObject1, sampleMap, "objectMap.key1", String.class))
                    .isTrue();

            softly.assertThat(jsonHelper.jsonPathsEqual(sampleObject1, sampleMap, "objectMap.key2", String.class))
                    .isFalse();
        }
    }

    @Nested
    class Merge {

        @Test
        void shouldMergeTwoObjects(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var updates = Map.of(
                    "stringVar", "modifiedString",
                    "intVar", 999,
                    "objectMap", Map.of("key4", "val4")
            );

            var mergedObject = jsonHelper.mergeObjects(sampleObject, updates);
            assertMergedObjectIsNotSameInstance(softly, mergedObject, sampleObject);

            softly.assertThat(mergedObject.getStringVar()).isEqualTo("modifiedString");
            softly.assertThat(mergedObject.getIntVar()).isEqualTo(999);
            softly.assertThat(mergedObject.getObjectMap())
                    .containsOnlyKeys("key1", "key2", "key3", "key4")
                    .containsEntry("key4", "val4");
        }

        @Test
        void shouldMergeObjectAndJson(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var updateJson = jsonHelper.toJsonFromKeyValuePairs(
                    "doubleVar", 42.24,
                    "objectVar", Sets.newLinkedHashSet("orange", "apple"),
                    "objectMap", Map.of("key4", "val4")
            );

            var mergedObject = jsonHelper.mergeObjects(sampleObject, updateJson);
            assertMergedObjectIsNotSameInstance(softly, mergedObject, sampleObject);

            softly.assertThat(mergedObject.getDoubleVar()).isEqualTo(42.24);
            softly.assertThat(mergedObject.getObjectVar()).isEqualTo(List.of("orange", "apple"));
            softly.assertThat(mergedObject.getObjectMap())
                    .containsOnlyKeys("key1", "key2", "key3", "key4")
                    .containsEntry("key4", "val4");
        }

        @Test
        void shouldNotMergeArraysByDefault(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var updates = Map.of(
                    "stringList", List.of("d", "e", "f")
            );

            var mergedObject = jsonHelper.mergeObjects(sampleObject, updates);
            assertMergedObjectIsNotSameInstance(softly, mergedObject, sampleObject);

            softly.assertThat(mergedObject.getStringList())
                    .describedAs("default merge should replace array properties")
                    .containsExactly("d", "e", "f");
        }

        @Test
        void shouldAllowMergingArrays(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var updates = Map.of(
                    "stringList", List.of("d", "e", "f")
            );

            var mergedObject = jsonHelper.mergeObjects(sampleObject, updates, MergeOption.MERGE_ARRAYS);
            assertMergedObjectIsNotSameInstance(softly, mergedObject, sampleObject);

            softly.assertThat(mergedObject.getStringList())
                    .describedAs("should merge array properties when directed")
                    .containsExactly("a", "b", "c", "d", "e", "f");
        }

        @Test
        void shouldUpdateToNullValues_ByDefault(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var updates = KiwiMaps.newHashMap(
                    "stringVar", null,
                    "intVar", null,
                    "objectMap", null
            );

            var mergedObject = jsonHelper.mergeObjects(sampleObject, updates);
            assertMergedObjectIsNotSameInstance(softly, mergedObject, sampleObject);

            softly.assertThat(mergedObject.getStringVar()).isNull();
            softly.assertThat(mergedObject.getIntVar()).isNull();
            softly.assertThat(mergedObject.getObjectMap()).isNull();
        }

        @Test
        void shouldNotUpdateToNullValues_WhenDirectedToIgnoreNulls(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var updates = KiwiMaps.newHashMap(
                    "stringVar", null,
                    "intVar", null,
                    "doubleVar", 3.14159,
                    "objectMap", null
            );

            var mergedObject = jsonHelper.mergeObjects(sampleObject, updates, MergeOption.MERGE_ARRAYS, MergeOption.IGNORE_NULLS);
            assertMergedObjectIsNotSameInstance(softly, mergedObject, sampleObject);

            softly.assertThat(mergedObject.getStringVar()).isEqualTo(sampleObject.getStringVar());
            softly.assertThat(mergedObject.getIntVar()).isEqualTo(sampleObject.getIntVar());
            softly.assertThat(mergedObject.getDoubleVar()).isEqualTo(3.14159);
            softly.assertThat(mergedObject.getObjectMap()).isEqualTo(sampleObject.getObjectMap());
        }

        private void assertMergedObjectIsNotSameInstance(SoftAssertions softly,
                                                         SampleObject mergedObject,
                                                         SampleObject sampleObject) {
            softly.assertThat(mergedObject)
                    .describedAs("mergedObject should not be same instance as sampleObject")
                    .isNotSameAs(sampleObject);
        }

        @Test
        void shouldMergeTwoJsonNodeObjects(SoftAssertions softly) {
            var sampleJson = jsonHelper.toJson(newSampleObject());
            var updateJson = jsonHelper.toJsonFromKeyValuePairs(
                    "stringVar", "modifiedString",
                    "intVar", 999,
                    "objectMap", Map.of("key4", "val4")
            );

            var sampleNode = readJsonAsTree(sampleJson);
            var updateNode = readJsonAsTree(updateJson);

            var mergedNode = jsonHelper.mergeNodes(sampleNode, updateNode);
            assertMergedNodeIsSameInstance(softly, mergedNode, sampleNode);

            var mergedObject = reconstructSampleObjectFromMergedNode(mergedNode);

            softly.assertThat(mergedObject.getStringVar()).isEqualTo("modifiedString");
            softly.assertThat(mergedObject.getIntVar()).isEqualTo(999);
            softly.assertThat(mergedObject.getObjectMap())
                    .containsOnlyKeys("key1", "key2", "key3", "key4")
                    .containsEntry("key1", "val1")
                    .containsEntry("key2", "val2")
                    .containsEntry("key3", "val3")
                    .containsEntry("key4", "val4");
        }

        @Test
        void shouldMergeTwoJsonNodeObjects_AndAllowMergingArrays(SoftAssertions softly) {
            var sampleJson = jsonHelper.toJson(newSampleObject());
            var updateJson = jsonHelper.toJsonFromKeyValuePairs(
                    "stringList", List.of("a", "c", "s", "t"),
                    "objectMap", Map.of("key4", List.of(4.3, 4.4))
            );

            var sampleNode = readJsonAsTree(sampleJson);
            var updateNode = readJsonAsTree(updateJson);

            var mergedNode = jsonHelper.mergeNodes(sampleNode, updateNode, MergeOption.MERGE_ARRAYS);
            assertMergedNodeIsSameInstance(softly, mergedNode, sampleNode);

            var mergedObject = reconstructSampleObjectFromMergedNode(mergedNode);

            softly.assertThat(mergedObject.getStringList())
                    .describedAs("should concatenate lists/arrays including duplicate elements")
                    .containsExactly("a", "b", "c", "a", "c", "s", "t");
            softly.assertThat(mergedObject.getObjectMap())
                    .containsOnlyKeys("key1", "key2", "key3", "key4")
                    .containsEntry("key4", List.of(4.1, 4.2, 4.3, 4.4));
        }

        @Test
        void shouldMergeTwoJsonNodeObjects_AndAllowIgnoringNulls(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var sampleJson = jsonHelper.toJson(sampleObject);
            var updateJson = jsonHelper.toJsonFromKeyValuePairs(
                    "stringVar", null,
                    "intVar", null,
                    "stringList", null,
                    "objectList", null,
                    "objectMap", null
            );

            var sampleNode = readJsonAsTree(sampleJson);
            var updateNode = readJsonAsTree(updateJson);

            var mergedNode = jsonHelper.mergeNodes(sampleNode, updateNode, MergeOption.IGNORE_NULLS);
            assertMergedNodeIsSameInstance(softly, mergedNode, sampleNode);

            var mergedObject = reconstructSampleObjectFromMergedNode(mergedNode);

            softly.assertThat(mergedObject.getStringVar()).isEqualTo(sampleObject.getStringVar());
            softly.assertThat(mergedObject.getIntVar()).isEqualTo(sampleObject.getIntVar());
            softly.assertThat(mergedObject.getStringList()).isEqualTo(sampleObject.getStringList());
            softly.assertThat(mergedObject.getObjectList()).isEqualTo(sampleObject.getObjectList());
            softly.assertThat(mergedObject.getObjectMap()).isEqualTo(sampleObject.getObjectMap());
        }

        @Test
        void shouldMergeTwoJsonNodeObjects_WithComplexUpdateJson_WhenMergingArrays(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var sampleJson = jsonHelper.toJson(sampleObject);
            var updateJson = jsonHelper.toJsonFromKeyValuePairs(
                    "stringVar", null,
                    "intVar", 42,
                    "objectVar", "new-string-object",
                    "stringList", List.of("x", "y", "z"),
                    "objectList", List.of(
                            Map.of("foo3", "bar3")
                    ),
                    "objectMap", KiwiMaps.newHashMap(
                            "key4", null,
                            "key5", "val5",
                            "key6", List.of("ab", "bc", "cd")
                    )
            );

            var sampleNode = readJsonAsTree(sampleJson);
            var updateNode = readJsonAsTree(updateJson);

            var mergedNode = jsonHelper.mergeNodes(sampleNode, updateNode, MergeOption.MERGE_ARRAYS);
            assertMergedNodeIsSameInstance(softly, mergedNode, sampleNode);

            var mergedObject = reconstructSampleObjectFromMergedNode(mergedNode);

            softly.assertThat(mergedObject.getStringVar()).isNull();
            softly.assertThat(mergedObject.getIntVar()).isEqualTo(42);
            softly.assertThat(mergedObject.getDoubleVar()).isEqualTo(4.567);
            softly.assertThat(mergedObject.getStringList()).containsExactly("a", "b", "c", "x", "y", "z");
            softly.assertThat(mergedObject.getObjectList()).containsExactly(
                    Map.of("foo1", "bar1"),
                    Map.of("foo2", "bar2"),
                    Map.of("foo3", "bar3")
            );
            softly.assertThat(mergedObject.getObjectMap())
                    .containsOnlyKeys("key1", "key2", "key3", "key4", "key5", "key6")
                    .containsEntry("key4", null)
                    .containsEntry("key5", "val5")
                    .containsEntry("key6", List.of("ab", "bc", "cd"));
        }

        @Test
        void shouldMergeTwoJsonNodeObjects_WithComplexUpdateJson_WhenMergingArraysAndIgnoringNulls(SoftAssertions softly) {
            var sampleObject = newSampleObject();
            var sampleJson = jsonHelper.toJson(sampleObject);
            var updateJson = jsonHelper.toJsonFromKeyValuePairs(
                    "stringVar", null,
                    "intVar", 42,
                    "objectVar", "new-string-object",
                    "stringList", List.of("t", "u", "v"),
                    "objectList", List.of(
                            Map.of("foo42", "bar42"),
                            Map.of("foo84", "bar84")
                    ),
                    "objectMap", KiwiMaps.newHashMap(
                            "key4", null,
                            "key5", "val5",
                            "key6", List.of("ab", "bc", "cd")
                    )
            );

            var sampleNode = readJsonAsTree(sampleJson);
            var updateNode = readJsonAsTree(updateJson);

            var mergeOptions = new MergeOption[]{MergeOption.MERGE_ARRAYS, MergeOption.IGNORE_NULLS};
            var mergedNode = jsonHelper.mergeNodes(sampleNode, updateNode, mergeOptions);
            assertMergedNodeIsSameInstance(softly, mergedNode, sampleNode);

            var mergedObject = reconstructSampleObjectFromMergedNode(mergedNode);

            softly.assertThat(mergedObject.getStringVar()).isEqualTo("string");
            softly.assertThat(mergedObject.getStringList()).containsExactly("a", "b", "c", "t", "u", "v");
            softly.assertThat(mergedObject.getObjectList()).containsExactly(
                    Map.of("foo1", "bar1"),
                    Map.of("foo2", "bar2"),
                    Map.of("foo42", "bar42"),
                    Map.of("foo84", "bar84")
            );
            softly.assertThat(mergedObject.getObjectMap())
                    .containsOnlyKeys("key1", "key2", "key3", "key4", "key5", "key6")
                    .containsEntry("key4", List.of(4.1, 4.2))
                    .containsEntry("key5", "val5")
                    .containsEntry("key6", List.of("ab", "bc", "cd"));
        }

        private void assertMergedNodeIsSameInstance(SoftAssertions softly,
                                                    JsonNode mergedNode,
                                                    JsonNode sampleNode) {
            softly.assertThat(mergedNode)
                    .describedAs("sampleNode should have been mutated in place and returned")
                    .isSameAs(sampleNode);
        }

        private SampleObject reconstructSampleObjectFromMergedNode(JsonNode mergedNode) {
            var mergedNodeJson = mergedNode.toString();
            return jsonHelper.toObject(mergedNodeJson, SampleObject.class);
        }

        private JsonNode readJsonAsTree(String updateJson) {
            try {
                return jsonHelper.getObjectMapper().readTree(updateJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeJsonException(e);
            }
        }
    }

    @Nested
    class ListObjectPaths {

        @Test
        void shouldListPaths_GivenJsonInput() {
            var json = jsonHelper.toJsonFromKeyValuePairs("firstName", "Bob", "lastName", "Smith", "age", 42);

            var paths = jsonHelper.listObjectPaths(json);

            assertThat(paths).containsOnlyOnce("firstName", "lastName", "age");
        }

        @Test
        void shouldListPaths_OfComplexObjects() {
            var sampleObject = newSampleObject();

            var paths = jsonHelper.listObjectPaths(sampleObject);

            assertThat(paths).containsOnlyOnce(
                    "stringVar",
                    "intVar",
                    "doubleVar",
                    "objectVar",
                    "stringList.[0]",
                    "stringList.[1]",
                    "stringList.[2]",
                    "objectList.[0].foo1",
                    "objectList.[1].foo2",
                    "objectMap.key1",
                    "objectMap.key2",
                    "objectMap.key3",
                    "objectMap.key4.[0]",
                    "objectMap.key4.[1]",
                    "requiresLimited",
                    "requiresAll"
            );
        }
    }

    private SampleObject newSampleObject() {
        var sampleData = jsonHelper.toObject(SAMPLE_JSON, SampleObject.class);
        validateSampleObject(sampleData);
        return sampleData;
    }

    private static void validateSampleObject(SampleObject object) {
        assertThat(object.getStringVar()).isEqualTo("string");
        assertThat(object.getIntVar()).isEqualTo(123);
        assertThat(object.getDoubleVar()).isEqualTo(4.567);
        assertThat(object.getObjectVar()).isEqualTo("string-object");
        assertThat(object.getStringList()).containsExactly("a", "b", "c");
        assertThat(object.getObjectList()).containsOnlyOnce(
                Map.of("foo1", "bar1"),
                Map.of("foo2", "bar2")
        );
        assertThat(object.getObjectMap()).containsOnly(
                entry("key1", "val1"),
                entry("key2", "val2"),
                entry("key3", "val3"),
                entry("key4", List.of(4.1, 4.2))
        );
    }

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SampleObject {

        String stringVar;
        Integer intVar;
        Double doubleVar;
        Object objectVar;
        List<String> stringList;
        List<Object> objectList;
        Map<String, Object> objectMap;

        @SuppressWarnings("unused")
        @JsonView(TestJsonViews.Limited.class)
        String getRequiresLimited() {
            return "requires-limited";
        }

        @SuppressWarnings("unused")
        @JsonView(TestJsonViews.All.class)
        String getRequiresAll() {
            return "requires-all";
        }
    }

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LimitedSampleObject {

        Double doubleVar;
        Object objectVar;
        List<String> stringList;
    }

    static class TestJsonViews {

        static class Limited {
        }

        static class All extends Limited {
        }
    }
}
