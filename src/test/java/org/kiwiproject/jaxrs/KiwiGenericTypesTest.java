package org.kiwiproject.jaxrs;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiLists.third;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@DisplayName("KiwiGenericTypes")
@ExtendWith(DropwizardExtensionsSupport.class)
class KiwiGenericTypesTest {

    @Path("/genericTypes")
    @Produces(MediaType.APPLICATION_JSON)
    public static class GenericTypeTestResource {

        @GET
        @Path("/car")
        public Response getCar() {
            var car = Map.of("make", "Tesla", "model", "S", "year", 2015, "miles", 20_500.42);
            return Response.ok(car).build();
        }

        @GET
        @Path("/cars")
        public Response getCars() {
            var cars = List.of(
                    Map.of("make", "Tesla", "model", "S", "year", 2015, "miles", 20_500.42),
                    Map.of("make", "Tesla", "model", "3", "year", 2019, "miles", 142.84),
                    Map.of("make", "Tesla", "model", "Y", "year", 2020, "miles", 5_042.0)
            );
            return Response.ok(cars).build();
        }

        @GET
        @Path("/models")
        public Response getModels() {
            var models = List.of("3", "S", "X", "Y");
            return Response.ok(models).build();
        }

        @GET
        @Path("/years")
        public Response getYears() {
            var years = List.of(2015, 2019, 2020);
            return Response.ok(years).build();
        }

        @GET
        @Path("/miles")
        public Response getMilesAsDoubles() {
            var miles = List.of(20_500.42, 142.84, 5_042.0);
            return Response.ok(miles).build();
        }

        @GET
        @Path("/onSale")
        public Response getOnSale() {
            var onSale = List.of(true, false, false);
            return Response.ok(onSale).build();
        }
    }

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .addResource(new GenericTypeTestResource())
            .build();

    @Test
    void shouldSupportMapOfStringToObject() {
        Map<String, Object> map = RESOURCES.client()
                .target("/genericTypes/car")
                .request()
                .get(KiwiGenericTypes.MAP_OF_STRING_TO_OBJECT_GENERIC_TYPE);

        assertThat(map).containsOnly(
                entry("make", "Tesla"),
                entry("model", "S"),
                entry("year", 2015),
                entry("miles", 20_500.42)
        );
    }

    @Test
    void shouldSupportListOfStringObjectMaps() {
        List<Map<String, Object>> cars = RESOURCES.client()
                .target("/genericTypes/cars")
                .request()
                .get(KiwiGenericTypes.LIST_OF_MAP_OF_STRING_TO_OBJECT_GENERIC_TYPE);

        var sortedCars = cars.stream()
                .sorted(comparing(car -> car.get("model").toString()))
                .collect(toList());

        assertThat(sortedCars).hasSize(3);

        var thirdCar = third(cars);
        assertThat(thirdCar).containsOnly(
                entry("make", "Tesla"),
                entry("model", "Y"),
                entry("year", 2020),
                entry("miles", 5_042.0)
        );
    }

    @Test
    void shouldSupportListOfStrings() {
        List<String> models = RESOURCES.client()
                .target("/genericTypes/models")
                .request()
                .get(KiwiGenericTypes.LIST_OF_STRING_GENERIC_TYPE);

        assertThat(models).containsExactly("3", "S", "X", "Y");
    }

    @Test
    void shouldSupportListOfIntegers() {
        List<Integer> models = RESOURCES.client()
                .target("/genericTypes/years")
                .request()
                .get(KiwiGenericTypes.LIST_OF_INTEGER_GENERIC_TYPE);

        assertThat(models).containsExactly(2015, 2019, 2020);
    }

    @Test
    void shouldSupportListOfLongs() {
        List<Long> models = RESOURCES.client()
                .target("/genericTypes/years")
                .request()
                .get(KiwiGenericTypes.LIST_OF_LONG_GENERIC_TYPE);

        assertThat(models).containsExactly(2015L, 2019L, 2020L);
    }

    @Test
    void shouldSupportListOfDoubles() {
        List<Double> miles = RESOURCES.client()
                .target("/genericTypes/miles")
                .request()
                .get(KiwiGenericTypes.LIST_OF_DOUBLE_GENERIC_TYPE);

        assertThat(miles).containsExactly(20_500.42, 142.84, 5_042.0);
    }

    @Test
    void shouldSupportListOfFloats() {
        List<Float> miles = RESOURCES.client()
                .target("/genericTypes/miles")
                .request()
                .get(KiwiGenericTypes.LIST_OF_FLOAT_GENERIC_TYPE);

        assertThat(miles).containsExactly(20_500.42F, 142.84F, 5_042.0F);
    }

    @Test
    void shouldSupportListOfBooleans() {
        List<Boolean> onSale = RESOURCES.client()
                .target("/genericTypes/onSale")
                .request()
                .get(KiwiGenericTypes.LIST_OF_BOOLEAN_GENERIC_TYPE);

        assertThat(onSale).containsExactly(true, false, false);
    }
}
