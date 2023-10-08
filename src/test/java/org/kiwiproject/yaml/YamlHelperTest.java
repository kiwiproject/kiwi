package org.kiwiproject.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.collect.KiwiMaps.newLinkedHashMap;
import static org.kiwiproject.jackson.KiwiTypeReferences.MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.util.BlankStringSource;

import java.util.List;
import java.util.Map;

@DisplayName("YamlHelper")
class YamlHelperTest {

    private static final TypeReference<Person> PERSON_TYPE_REFERENCE = new TypeReference<>() {
    };

    private static final TypeReference<List<Person>> PERSON_LIST_TYPE_REFERENCE = new TypeReference<>() {
    };

    private YamlHelper yamlHelper;

    @BeforeEach
    void setUp() {
        yamlHelper = new YamlHelper();
    }

    @Nested
    class Constructors {

        @Test
        void shouldNotPermitNullObjectMapper() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new YamlHelper(null));
        }

        @Test
        void shouldRejectObjectMapper_ThatDoesNotSupportYamlFormat() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new YamlHelper(new ObjectMapper()));
        }

        @Test
        void shouldConstructWithObjectMapper_ThatSupportsYamlFormat() {
            var helper = new YamlHelper(new YAMLMapper());
            assertThat(helper).isNotNull();
        }
    }

    @Nested
    class ToYaml {

        @Test
        void shouldConvertMap() {
            var yaml = yamlHelper.toYaml(newLinkedHashMap("firstName", "Bob", "lastName", "Sackamano", "age", 34));

            assertThat(yaml).isEqualTo(expectedYamlForBob());
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldConvertComplexMap() throws JsonProcessingException {
            var bob = new Person("Bob", "Sackamano", 24);
            var john = new Person("John", "Doe", 35);
            var yaml = yamlHelper.toYaml(newLinkedHashMap(
                    "bob", bob,
                    "john", john
            ));

            var map = new YAMLMapper().readValue(yaml, MAP_OF_STRING_TO_OBJECT_TYPE_REFERENCE);

            assertThat(map).containsOnlyKeys("bob", "john");

            var bobMap = (Map<String, Object>) map.get("bob");
            assertThat(bobMap).containsOnly(
                    entry("firstName", "Bob"),
                    entry("lastName", "Sackamano"),
                    entry("age", 24)
            );

            var johnMap = (Map<String, Object>) map.get("john");
            assertThat(johnMap).containsOnly(
                    entry("firstName", "John"),
                    entry("lastName", "Doe"),
                    entry("age", 35)
            );
        }

        @Test
        void shouldConvertObjects() {
            var bob = new Person("Bob", "Sackamano", 34);
            var yaml = yamlHelper.toYaml(bob);

            assertThat(yaml).isEqualTo(expectedYamlForBob());
        }

        @Test
        void shouldConvertListOfObjects() {
            var bob = new Person("Bob", "Sackamano", 34);
            var alice = new Person("Alice", "Jones", 27);
            var carlos = new Person("Carlos", "Sanchéz", 42);
            var people = List.of(bob, alice, carlos);

            var yaml = yamlHelper.toYaml(people);

            assertThat(yaml).isEqualTo(Fixtures.fixture("YamlHelperTest/people.yml"));
        }
    }

    @Nested
    class ToYamlWithView {

        @Test
        void shouldUseViews() {
            var bob = new Person("Anakin", "Skywalker", 20);

            var yaml = yamlHelper.toYaml(bob, PersonViews.Public.class);

            assertThat(yaml).isEqualTo(Fixtures.fixture("YamlHelperTest/anakin-public.yml"));
        }
    }

    private static String expectedYamlForBob() {
        return Fixtures.fixture("YamlHelperTest/bob.yaml");
    }

    @Nested
    class ToObjectWithTargetClass {

        @Test
        void shouldDeserializeYaml() {
            var yaml = Fixtures.fixture("YamlHelperTest/bob.yaml");

            var person = yamlHelper.toObject(yaml, Person.class);

            assertThat(person.getFirstName()).isEqualTo("Bob");
            assertThat(person.getLastName()).isEqualTo("Sackamano");
            assertThat(person.getAge()).isEqualTo(34);
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldThrow_GivenBlankArgument(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> yamlHelper.toObject(value, Person.class));
        }

        @Test
        void shouldThrow_WhenInvalidYaml() {
            assertThatThrownBy(() -> yamlHelper.toObject("invalid YAML", Person.class))
                    .isExactlyInstanceOf(RuntimeYamlException.class)
                    .hasCauseInstanceOf(JsonProcessingException.class);
        }
    }

    @Nested
    class ToObjectWithTargetTypeReference {

        @Test
        void shouldDeserializeYaml() {
            var yaml = Fixtures.fixture("YamlHelperTest/anakin-public.yml");

            var person = yamlHelper.toObject(yaml, PERSON_TYPE_REFERENCE);

            assertThat(person.getFirstName()).isEqualTo("Anakin");
            assertThat(person.getLastName()).isEqualTo("Skywalker");
            assertThat(person.getAge()).isNull();
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldThrow_GivenBlankArgument(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> yamlHelper.toObject(value, PERSON_TYPE_REFERENCE));
        }

        @Test
        void shouldThrow_WhenInvalidYaml() {
            assertThatThrownBy(() -> yamlHelper.toObject("invalid YAML", PERSON_TYPE_REFERENCE))
                    .isExactlyInstanceOf(RuntimeYamlException.class)
                    .hasCauseInstanceOf(JsonProcessingException.class);
        }
    }

    @Nested
    class ToObjectList {

        @Test
        void shouldDeserializeYaml_ToListOfTargetType() {
            var yaml = Fixtures.fixture("YamlHelperTest/people.yml");

            var people = yamlHelper.toObjectList(yaml, PERSON_LIST_TYPE_REFERENCE);

            assertThat(people).extracting("firstName", "lastName", "age")
                    .containsExactly(
                            tuple("Bob", "Sackamano", 34),
                            tuple("Alice", "Jones", 27),
                            tuple("Carlos", "Sanchéz", 42)
                    );
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldThrow_GivenBlankArgument(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> yamlHelper.toObjectList(value, PERSON_LIST_TYPE_REFERENCE));
        }
    }

    @Nested
    class ToMap {

        @Test
        void shouldDeserializeYaml_ToMap() {
            var yaml = Fixtures.fixture("YamlHelperTest/languages.yml");

            var languages = yamlHelper.toMap(yaml);

            assertThat(languages)
                    .containsOnlyKeys("clojure", "kotlin", "java", "javascript", "python", "ruby")
                    .containsEntry("clojure", Map.of("creator", "Hickey", "year", 2007))
                    .containsEntry("kotlin", Map.of("creator", "Breslav", "year", 2011))
                    .containsEntry("java", Map.of("creator", "Gosling", "year", 1995))
                    .containsEntry("javascript", Map.of("creator", "Eich", "year", 1995))
                    .containsEntry("python", Map.of("creator", "van Rossum", "year", 1990))
                    .containsEntry("ruby", Map.of("creator", "Matz", "year", 1996));
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldThrow_GivenBlankArgument(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> yamlHelper.toMap(value));
        }

        @Test
        void shouldThrow_WhenInvalidYaml() {
            assertThatThrownBy(() -> yamlHelper.toMap("invalid YAML"))
                    .isExactlyInstanceOf(RuntimeYamlException.class)
                    .hasCauseInstanceOf(JsonProcessingException.class);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Person {

        @JsonView(PersonViews.Public.class)
        private String firstName;

        @JsonView(PersonViews.Public.class)
        private String lastName;

        @JsonView(PersonViews.Private.class)
        private Integer age;
    }

    static class PersonViews {
        static class Public {
        }

        static class Private extends PersonViews.Public {
        }
    }
}
