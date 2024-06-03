package org.kiwiproject.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiMaps.newLinkedHashMap;
import static org.kiwiproject.jackson.KiwiTypeReferences.LIST_OF_MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE;
import static org.kiwiproject.jackson.KiwiTypeReferences.MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.json.JsonHelper.OutputFormat;
import org.kiwiproject.junit.jupiter.ClearBoxTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Contains tests of the "basic" JsonHelper functionality: converting objects to and from JSON.
 */
@DisplayName("JsonHelper Basics")
class JsonHelperBasicsTest {

    private JsonHelper jsonHelper;

    @BeforeEach
    void setUp() {
        jsonHelper = JsonHelper.newDropwizardJsonHelper();
    }

    @Nested
    class OutputFormatEnum {

        @ParameterizedTest
        @CsvSource({
                "false, DEFAULT",
                "false, DEFAULT",
                "False, DEFAULT",
                "true, PRETTY",
                "TRUE, PRETTY",
                "True, PRETTY",
                "trUe, PRETTY",
        })
        void shouldConvertStrings(String value, OutputFormat expectedFormat) {
            assertThat(OutputFormat.ofPrettyValue(value)).isEqualTo(expectedFormat);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldConvertNullAndEmptyStrings(String value) {
            assertThat(OutputFormat.ofPrettyValue(value)).isEqualTo(OutputFormat.DEFAULT);
        }

        @ParameterizedTest
        @CsvSource({
                ", DEFAULT",
                "FALSE, DEFAULT",
                "TRUE, PRETTY"
        })
        void shouldConvertBooleans(Boolean value, OutputFormat expectedFormat) {
            assertThat(OutputFormat.ofPrettyValue(value)).isEqualTo(expectedFormat);
        }
    }

    @Test
    void shouldConfigureObjectMapper_ToReadMillis() {
        var now = Instant.now();
        var nowMillis = now.toEpochMilli();
        var json = "{ \"timestamp\": " + nowMillis + "}";

        var plainJsonHelper = new JsonHelper();
        var holder = plainJsonHelper.toObject(json, TimestampHolder.class);

        assertThat(holder.getTimestamp()).isEqualTo(now.truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void shouldConfigureObjectMapper_ToWriteMillis() {
        var now = Instant.now();
        var holder = new TimestampHolder(now);

        var plainJsonHelper = new JsonHelper();
        var json = plainJsonHelper.toJson(holder);

        assertThat(json).isEqualTo("{\"timestamp\":" + now.toEpochMilli() + "}");
    }

    @Test
    void shouldCreateNewDropwizardObjectMapper() throws JsonProcessingException {
        var mapper = JsonHelper.newDropwizardObjectMapper();

        var foo = newFoo();
        var json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(foo);

        var deserializedFoo = jsonHelper.toObject(json, Foo.class);

        var truncatedFoo = newFoo(foo.getInstant().truncatedTo(ChronoUnit.MILLIS));
        assertThat(deserializedFoo).isEqualTo(truncatedFoo);
    }

    @Test
    void shouldCreateNewDropwizardJsonHelper() {
        var foo = newFoo();
        var json = jsonHelper.toJson(foo, OutputFormat.PRETTY);

        var deserializedFoo = this.jsonHelper.toObject(json, Foo.class);

        var truncatedFoo = newFoo(foo.getInstant().truncatedTo(ChronoUnit.MILLIS));
        assertThat(deserializedFoo).isEqualTo(truncatedFoo);
    }

    @Test
    void shouldDeserializeDurationsUsingISO8601Format() {
        var durations = jsonHelper.toObjectList("[ \"PT2M\", \"PT3H5M\" ]", new TypeReference<List<Duration>>() {
        });

        assertThat(durations).containsExactly(Duration.ofMinutes(2), Duration.ofMinutes(185));
    }

    @Value
    static class TimestampHolder {
        Instant timestamp;
    }

    @Test
    void shouldGetObjectMapper() {
        var mapper = new ObjectMapper();
        var customJsonHelper = new JsonHelper(mapper);

        assertThat(customJsonHelper.getObjectMapper()).isSameAs(mapper);
    }

    @Test
    void shouldNotAllowNullObjectMapperInConstructor() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JsonHelper(null));
    }

    @Nested
    class IsJson {

        @ParameterizedTest
        @CsvSource({
                "{\"foo\": true}, true",
                "<html></html>, false",
                "not json, false"
        })
        void shouldCheckIsJson(String content, boolean expectedResult) {
            assertThat(jsonHelper.isJson(content)).isEqualTo(expectedResult);
        }
    }

    @Nested
    class DetectJson {

        @ParameterizedTest
        @CsvSource({
                "{\"foo\": true}, true",
                "<html></html>, false",
                "not json, false"
        })
        void shouldDetectIfJson(String content, boolean expectedResult) {
            var result = jsonHelper.detectJson(content);

            assertThat(result.hasDetectionResult()).isTrue();
            assertThat(result.isJson()).isEqualTo(expectedResult);
            assertThat(result.hasError()).isFalse();
            assertThat(result.getErrorOrNull()).isNull();
        }

        @ClearBoxTest
        void shouldCatchIOExceptions() throws IOException {
            var formatDetector = mock(DataFormatDetector.class);
            when(formatDetector.findFormat(any(byte[].class))).thenThrow(new IOException("Whoops!"));

            var content = "{}";
            var result = JsonHelper.detectJson(content, StandardCharsets.UTF_8, formatDetector);
            assertThat(result.hasDetectionResult()).isFalse();
            assertThat(result.isJson()).isFalse();
            assertThat(result.hasError()).isTrue();
            assertThat(result.getErrorOrNull())
                    .isExactlyInstanceOf(IOException.class)
                    .hasMessage("Whoops!");

            verify(formatDetector).findFormat(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Nested
    class ToJson {

        @Test
        void shouldReturnNull_WhenGivenNull() {
            assertThat(jsonHelper.toJson(null)).isNull();
        }

        @Test
        void shouldReturnGivenString_WhenAlreadyJson() {
            var json = "{\"foo\": true, \"bar\": 42}";

            assertThat(jsonHelper.toJson(json)).isSameAs(json);
        }

        @Test
        void shouldQuoteStringContent() {
            var notJson = "not-json-content";

            assertThat(jsonHelper.toJson(notJson)).isEqualTo("\"" + notJson + "\"");
        }

        @Test
        void shouldSerializeBasicTypes() {
            var sampleJson = Fixtures.fixture("JsonHelperTests/sample.json");
            assertThat(jsonHelper.toJson(sampleJson)).isEqualTo(sampleJson);

            assertThat(jsonHelper.toJson(1)).isEqualTo("1");

            assertThat(jsonHelper.toJson(1.2345)).isEqualTo("1.2345");

            assertThat(jsonHelper.toJson(List.of("a", "b", "c")))
                    .isEqualToIgnoringWhitespace(Fixtures.fixture("JsonHelperTests/sampleList.json"));
        }

        @Test
        void shouldThrowRuntimeJsonException_WhenErrorOccurs() {
            var person = new ThrowingPerson("Alice", "Smith", 27);

            assertThatThrownBy(() -> jsonHelper.toJson(person))
                    .isExactlyInstanceOf(RuntimeJsonException.class)
                    .hasMessageContaining("some weird error")
                    .hasCauseExactlyInstanceOf(JsonMappingException.class)
                    .hasRootCauseMessage("some weird error");
        }

        @Test
        void shouldHandleSimpleMaps() {
            var map = newLinkedHashMap("firstName", "Bob", "lastName", "Smith", "age", 34);

            assertThat(jsonHelper.toJson(map)).isEqualTo(expectedJsonForBob());
        }

        @Test
        void shouldHandleComplexMaps() throws JsonProcessingException {
            var bob = new Person("Bob", "Smith", 24);
            var john = new Person("John", "Doe", 35);
            var json = jsonHelper.toJson(Map.of(
                    "bob", bob,
                    "john", john
            ));

            var personMap = JsonHelper.newDropwizardObjectMapper()
                    .readValue(json, new TypeReference<Map<String, Person>>() {
                    });

            assertThat(personMap).containsOnlyKeys("bob", "john");

            var bobFromMap = personMap.get("bob");
            assertThat(bobFromMap).isEqualTo(bob);

            var johnFromMap = personMap.get("john");
            assertThat(johnFromMap).isEqualTo(john);
        }

        @Test
        void shouldHandleSimpleObjects() {
            var bob = new Person("Bob", "Smith", 34);
            var json = jsonHelper.toJson(bob);

            assertThat(json).isEqualTo(expectedJsonForBob());
        }

        @Test
        void shouldApplyJsonViews() {
            var bob = new Person("Bob", "Smith", 34);
            var json = jsonHelper.toJson(bob, OutputFormat.PRETTY, PersonViews.Public.class);

            assertThat(json).isEqualTo("{" + System.lineSeparator() +
                    "  \"firstName\" : \"Bob\"," + System.lineSeparator() +
                    "  \"lastName\" : \"Smith\"" + System.lineSeparator() +
                    "}"
            );
        }

        @Test
        void shouldAllowIgnoringPaths() {
            var bob = new Person("Bob", "Smith", 34);
            var json = jsonHelper.toJsonIgnoringPaths(bob, "firstName", "age");
            assertThat(json).isEqualTo("{\"lastName\":\"Smith\"}");

            var emptyJson = jsonHelper.toJsonIgnoringPaths(bob, "firstName", "lastName", "age");
            assertThat(emptyJson).isEqualTo("{}");
        }

        @Test
        void shouldIgnoreNestedPaths() {
            var bob = buildUser();

            var sanitizedJson = jsonHelper.toJsonIgnoringPaths(bob,
                    "email", "username", "password", "ssn", "luckyNumbers", "homeAddress", "workAddress.street1", "workAddress.street2");

            var sanitizedBob = jsonHelper.toMap(sanitizedJson);
            assertThat(sanitizedBob).containsOnlyKeys("firstName", "lastName", "workAddress");

            @SuppressWarnings("unchecked")
            var workAddress = (Map<String, Object>) sanitizedBob.get("workAddress");
            assertThat(workAddress).doesNotContainKeys("street1", "street2");
        }
    }

    @Nested
    class ToJsonFromKeyValuePairs {

        @Test
        void shouldHandleDegenerateCase_WhenNoArgsAreGiven() {
            assertThat(jsonHelper.toJsonFromKeyValuePairs()).isEqualTo("{}");
        }

        @Test
        void shouldThrow_WhenGivenOddNumberOfArguments() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> jsonHelper.toJsonFromKeyValuePairs("foo"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> jsonHelper.toJsonFromKeyValuePairs("foo", "bar", "baz"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> jsonHelper.toJsonFromKeyValuePairs("foo", "bar", "baz", "qux", "corge"));
        }

        @Test
        void shouldGenerateJson() {
            var json = jsonHelper.toJsonFromKeyValuePairs("firstName", "Bob", "lastName", "Smith", "age", 34);

            assertThat(json).isEqualTo(expectedJsonForBob());
        }
    }

    @Nested
    class ToObject {

        @Test
        void shouldDeserializeJson_GivenTargetClass() {
            var originalUser = buildUser();
            var json = jsonHelper.toJson(originalUser);

            var userFromJson = jsonHelper.toObject(json, User.class);

            assertThat(userFromJson).usingRecursiveComparison().isEqualTo(originalUser);
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnNull_GivenTargetClass_AndBlankInput(String value) {
            assertThat(jsonHelper.toObject(value, User.class)).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNull_GivenTargetClass_AndNullOrEmptyInput(String value) {
            assertThat(jsonHelper.toObject(value, User.class)).isNull();
        }

        @Test
        void shouldDeserializeJson_GivenTargetTypeReference() {
            var originalUser = buildUser();
            var json = jsonHelper.toJson(originalUser);

            var userFromJson = jsonHelper.toObject(json, new TypeReference<User>() {
            });

            assertThat(userFromJson).usingRecursiveComparison().isEqualTo(originalUser);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnNull_GivenTargetTypeReference_AndBlankInput(String value) {
            var user = jsonHelper.toObject(value, new TypeReference<User>() {
            });

            assertThat(user).isNull();
        }

        @Test
        void shouldReturnSameString_WhenGivenJson_AndTargetClass_IsString() {
            var json = "{ \"foo\": true, \"bar\": 42, \"baz\": \"quux\" }";

            var string = jsonHelper.toObject(json, String.class);

            assertThat(string).isSameAs(json);
        }

        @Test
        void shouldThrow_WhenGivenBadJson() {
            var json = "BAD_JSON_INPUT";

            assertThatThrownBy(() -> jsonHelper.toObject(json, Person.class))
                    .isExactlyInstanceOf(RuntimeJsonException.class)
                    .hasCauseExactlyInstanceOf(JsonParseException.class);

            var targetPersonType = new TypeReference<Person>() {
            };
            assertThatThrownBy(() -> jsonHelper.toObject(json, targetPersonType))
                    .isExactlyInstanceOf(RuntimeJsonException.class)
                    .hasCauseExactlyInstanceOf(JsonParseException.class);
        }
    }

    @Nested
    class ToObjectOrDefault {

        @Test
        void shouldDeserializeJson_WhenGivenValidInput() {
            var user = buildUser();
            var json = jsonHelper.toJson(user);

            var userFromJson = jsonHelper.toObjectOrDefault(json, User.class, User.builder().build());

            assertThat(userFromJson).usingRecursiveComparison().isEqualTo(user);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnDefaultValue_WhenGivenBlankInput(String value) {
            var defaultFoo = newFoo();

            assertThat(jsonHelper.toObjectOrDefault(value, Foo.class, defaultFoo)).isSameAs(defaultFoo);
        }
    }

    @Nested
    class ToObjectOrSupply {

        @Test
        void shouldDeserializeJson_WhenGivenValidInput() {
            var user = buildUser();
            var json = jsonHelper.toJson(user);

            var userFromJson = jsonHelper.toObjectOrSupply(json, User.class, () -> User.builder().build());

            assertThat(userFromJson).usingRecursiveComparison().isEqualTo(user);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnSuppliedValue_WhenGivenBlankInput(String value) {
            var foo = newFoo();
            Supplier<Foo> fooSupplier = () -> foo;

            assertThat(jsonHelper.toObjectOrSupply(value, Foo.class, fooSupplier)).isSameAs(foo);
        }
    }

    @Nested
    class ToObjectOptional {

        @Test
        void shouldDeserializeJson_WhenGivenValidInput() {
            var alice = new Person("Alice", "Jones", 31);
            var json = jsonHelper.toJson(alice);

            var personFromJson = jsonHelper.toObjectOptional(json, Person.class);

            assertThat(personFromJson).contains(alice);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_WhenGivenNullOrEmptyInput(String value) {
            assertThat(jsonHelper.toObjectOptional(value, Person.class)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnEmptyOptional_WhenGivenBlankInput(String value) {
            assertThat(jsonHelper.toObjectOptional(value, Person.class)).isEmpty();
        }
    }

    @Nested
    class ToObjectList {

        @Test
        void shouldDeserializeJson_ToSpecificType_WhenGivenValidInput() {
            var bobJson = jsonHelper.toJsonFromKeyValuePairs("firstName", "Bob", "lastName", "Smith", "age", 34);
            var jasonJson = jsonHelper.toJsonFromKeyValuePairs("firstName", "Jason", "lastName", "Whatever", "age", 27);
            var json = "[" + bobJson + "," + jasonJson + "]";

            var people = jsonHelper.toObjectList(json, new TypeReference<List<Person>>() {
            });

            assertThat(people)
                    .usingRecursiveFieldByFieldElementComparatorOnFields("firstName", "lastName", "age")
                    .containsOnlyOnce(
                            new Person("Bob", "Smith", 34),
                            new Person("Jason", "Whatever", 27)
                    );
        }

        @Test
        void shouldDeserializeJson_ToListOfMaps_WhenGivenValidInput() {
            var bobJson = jsonHelper.toJsonFromKeyValuePairs("firstName", "Bob", "lastName", "Smith", "age", 34);
            var jasonJson = jsonHelper.toJsonFromKeyValuePairs("firstName", "Jason", "lastName", "Whatever", "age", 27);
            var json = "[" + bobJson + "," + jasonJson + "]";

            var people = jsonHelper.toObjectList(json, LIST_OF_MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);

            assertThat(people)
                    .containsOnlyOnce(
                            newLinkedHashMap("firstName", "Bob", "lastName", "Smith", "age", 34),
                            newLinkedHashMap("firstName", "Jason", "lastName", "Whatever", "age", 27)
                    );
        }

        @Test
        void shouldReturnEmptyList_WhenGivenEmptyJsonArray() {
            var people = jsonHelper.toObjectList("[]", new TypeReference<List<Person>>() {
            });

            assertThat(people).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnNull_WhenGivenBlankInput(String value) {
            var people = jsonHelper.toObjectList(value, new TypeReference<List<Person>>() {
            });

            assertThat(people).isNull();
        }
    }

    @Nested
    class ToMap {

        @Test
        void shouldDeserializeJson_WhenGivenValidInput() {
            var bobJson = jsonHelper.toJsonFromKeyValuePairs("firstName", "Bob", "lastName", "Smith", "age", 34);

            var bobMap = jsonHelper.toMap(bobJson);

            assertThat(bobMap).containsOnly(
                    entry("firstName", "Bob"),
                    entry("lastName", "Smith"),
                    entry("age", 34)
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnNull_WhenGivenBlankInput(String value) {
            var people = jsonHelper.toMap(value);

            assertThat(people).isNull();
        }

        @Test
        void shouldThrow_WhenGivenBadJson() {
            var json = "BAD_JSON_INPUT";

            assertThatThrownBy(() -> jsonHelper.toMap(json))
                    .isExactlyInstanceOf(RuntimeJsonException.class)
                    .hasCauseExactlyInstanceOf(JsonParseException.class);
        }

        @Test
        void shouldDeserializeJson_GivenTypeReference_AndValidInput() {
            var aliceJson = jsonHelper.toJsonFromKeyValuePairs("1", "Alice", "2", "Jones", "3", 29);

            var aliceMap = jsonHelper.toMap(aliceJson, new TypeReference<Map<Integer, String>>() {
            });

            assertThat(aliceMap).containsOnly(
                    entry(1, "Alice"),
                    entry(2, "Jones"),
                    entry(3, "29")
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", " \n "})
        void shouldReturnNull_GivenTypeReference_AndBlankInput(String value) {
            var people = jsonHelper.toMap(value, MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);

            assertThat(people).isNull();
        }

        @Test
        void shouldThrow_GivenTypeReference_AndBadJson() {
            var json = "BAD_JSON_INPUT";

            assertThatThrownBy(() -> jsonHelper.toMap(json, MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE))
                    .isExactlyInstanceOf(RuntimeJsonException.class)
                    .hasCauseExactlyInstanceOf(JsonParseException.class);
        }
    }

    private String expectedJsonForBob() {
        return "{\"firstName\":\"Bob\",\"lastName\":\"Smith\",\"age\":34}";
    }

    private static User buildUser() {
        return User.builder()
                .firstName("Bob")
                .lastName("Shenanigans")
                .email("bob@shenanigans.family")
                .username("b_shenanigans")
                .password("secret")
                .ssn("123-45-6789")
                .luckyNumbers(List.of(24, 42, 84))
                .homeAddress(Address.builder()
                        .street1("123 S. Main St.")
                        .street2("Apt #405")
                        .city("Snickersville")
                        .state("VA")
                        .zipCode("12345")
                        .build())
                .workAddress(Address.builder()
                        .street1("456 N. Business Way")
                        .street2("Suite 66")
                        .city("Commerce")
                        .state("VA")
                        .zipCode("23456")
                        .build())
                .build();
    }

    @Value
    private static class Foo {
        Instant instant;
        Date date;
        Timestamp timestamp;
        LocalDateTime localDateTime;
        ZonedDateTime zonedDateTime;
        Duration duration;
    }

    private Foo newFoo() {
        return newFoo(Instant.now());
    }

    private Foo newFoo(Instant instant) {
        var date = Date.from(instant);
        var timestamp = Timestamp.from(instant);
        var utcZoneId = ZoneId.of("UTC").normalized();
        var localDT = LocalDateTime.ofInstant(instant, utcZoneId);
        var zonedDT = ZonedDateTime.from(instant.atOffset(ZoneOffset.of("Z")).atZoneSameInstant(utcZoneId));
        var duration = Duration.ofMinutes(1);

        return new Foo(instant, date, timestamp, localDT, zonedDT, duration);
    }

    // Cannot use @Value because we need to be able to extend Person
    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    static class Person {

        @JsonView(PersonViews.Public.class)
        String firstName;

        @JsonView(PersonViews.Public.class)
        String lastName;

        @JsonView(PersonViews.Private.class)
        int age;
    }

    static class ThrowingPerson extends Person {

        public ThrowingPerson(String firstName, String lastName, int age) {
            super(firstName, lastName, age);
        }

        @Override
        public String getLastName() {
            throw new RuntimeException("some weird error");
        }
    }

    static class PersonViews {
        static class Public {
        }

        static class Private extends Public {
        }
    }

    @Value
    @Builder
    static class User {
        String firstName;
        String lastName;
        String email;
        String username;
        String password;
        String ssn;
        Address homeAddress;
        Address workAddress;

        @Builder.Default
        List<Integer> luckyNumbers = new ArrayList<>();
    }

    @Value
    @Builder
    static class Address {
        String street1;
        String street2;
        String city;
        String state;
        String zipCode;
    }
}
