package org.kiwiproject.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

@DisplayName("KiwiEntities")
class KiwiEntitiesTest {

    private static final GenericType<Map<String, Object>> MAP_GENERIC_TYPE = new GenericType<>() {
    };

    @Nested
    class SafeReadEntityAsString {

        @Test
        void shouldGetEntity_WhenReadSucceeds() {
            var response = mock(Response.class);
            when(response.readEntity(String.class)).thenReturn("entity");

            var entity = KiwiEntities.safeReadEntity(response).orElse("");
            assertThat(entity).isEqualTo("entity");
        }

        @Test
        void shouldHaveEmptyOptional_WhenReadFails() {
            var response = mock(Response.class);
            when(response.readEntity(String.class)).thenThrow(new IllegalStateException("oops"));

            Optional<String> entity = KiwiEntities.safeReadEntity(response);
            assertThat(entity).isEmpty();
        }
    }

    @Nested
    class SafeReadEntityAsStringWithDefaultMessage {

        @Test
        void shouldGetEntity_WhenReadSucceeds() {
            var response = mock(Response.class);
            when(response.readEntity(String.class)).thenReturn("the entity");

            var entity = KiwiEntities.safeReadEntity(response, "default message");
            assertThat(entity).isEqualTo("the entity");
        }

        @Test
        void shouldReturnDefaultMessage_WhenReadFails() {
            var response = mock(Response.class);
            when(response.readEntity(String.class)).thenThrow(new ProcessingException("oops"));

            var entity = KiwiEntities.safeReadEntity(response, "default message");
            assertThat(entity).isEqualTo("default message");
        }
    }

    @Nested
    class SafeReadEntityAsStringWithDefaultMessageSupplier {

        @Test
        void shouldGetEntity_WhenReadSucceeds() {
            var response = mock(Response.class);
            when(response.readEntity(String.class)).thenReturn("the entity");

            var entity = KiwiEntities.safeReadEntity(response, () -> "default message");
            assertThat(entity).isEqualTo("the entity");
        }

        @Test
        void shouldReturnDefaultMessage_WhenReadFails() {
            var response = mock(Response.class);
            when(response.readEntity(String.class)).thenThrow(new ProcessingException("oops"));

            var entity = KiwiEntities.safeReadEntity(response, () -> "default message");
            assertThat(entity).isEqualTo("default message");
        }
    }

    @Nested
    class SafeReadEntityAsClass {

        @Test
        void shouldGetEntity_WhenReadSucceeds() {
            var response = mock(Response.class);
            var tesla = new Car("Tesla", "S", 2016);
            when(response.readEntity(Car.class)).thenReturn(tesla);

            var entity = KiwiEntities.safeReadEntity(response, Car.class).orElse(null);
            assertThat(entity).isEqualTo(tesla);
        }

        @Test
        void shouldHaveEmptyOptional_WhenReadFails() {
            var response = mock(Response.class);
            when(response.readEntity(Car.class)).thenThrow(new IllegalStateException("doh!"));

            Optional<Car> entity = KiwiEntities.safeReadEntity(response, Car.class);
            assertThat(entity).isEmpty();
        }
    }

    @Nested
    class SafeReadEntityAsGenericType {

        @Test
        void shouldGetEntity_WhenReadSucceeds() {
            var response = mock(Response.class);
            when(response.readEntity(MAP_GENERIC_TYPE)).thenReturn(Map.of("answer", 42));

            var entity = KiwiEntities.safeReadEntity(response, MAP_GENERIC_TYPE).orElse(Map.of());
            assertThat(entity).containsOnly(entry("answer", 42));
        }

        @Test
        void shouldHaveEmptyOptional_WhenReadFails() {
            var response = mock(Response.class);
            when(response.readEntity(MAP_GENERIC_TYPE)).thenThrow(new IllegalStateException("oh no!"));

            Optional<Map<String, Object>> entity = KiwiEntities.safeReadEntity(response, MAP_GENERIC_TYPE);
            assertThat(entity).isEmpty();
        }
    }

    @Value
    static class Car {
        String make;
        String model;
        int year;
    }
}
