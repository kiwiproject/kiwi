package org.kiwiproject.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiMaps.newLinkedHashMap;

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
import org.kiwiproject.internal.Fixtures;

import java.util.List;
import java.util.Map;

@DisplayName("YamlHelper")
class YamlHelperTest {

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

            var map = new YAMLMapper().readValue(yaml, new TypeReference<Map<String, Object>>() {
            });

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