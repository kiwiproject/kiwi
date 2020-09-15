package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertHasMapEntity;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertResponseMediaType;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertStatusCode;

import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.validation.KiwiValidations;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Year;
import java.util.List;

@DisplayName("ConstraintViolationExceptionMapper")
class ConstraintViolationExceptionMapperTest {

    private ConstraintViolationExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ConstraintViolationExceptionMapper();
    }

    // This is here only so that if Response.Status ever adds 422, we'll know about it...
    @Nested
    class FromStatusCode {

        @Test
        void shouldReturnNullFor422() {
            assertThat(Response.Status.fromStatusCode(422)).isNull();
        }
    }

    @Nested
    class BuildResponse {

        @Test
        void shouldBuildResponse() {
            var car = Car.builder()
                    .make("Tesla")
                    .model("")
                    .year(Year.of(2016))
                    .mileage(-100)
                    .build();
            var violations = KiwiValidations.validate(car);

            var response = ConstraintViolationExceptionMapper.buildResponse(violations);

            var expectedStatusCode = 422;
            assertStatusCode(response, expectedStatusCode);
            assertResponseMediaType(response, MediaType.APPLICATION_JSON);

            var entity = assertHasMapEntity(response);
            assertThat(entity).containsOnlyKeys("errors");

            //noinspection unchecked
            var errors = (List<ErrorMessage>) entity.get("errors");

            assertThat(errors).containsExactlyInAnyOrder(
                    new ErrorMessage(expectedStatusCode, "must not be blank", "model"),
                    new ErrorMessage(expectedStatusCode, "must be greater than or equal to 0", "mileage")
            );
        }
    }

    @Nested
    class ToResponse {

        @Test
        void shouldConvertToResponse() {
            var car = Car.builder()
                    .make("")
                    .model("")
                    .year(Year.of(2100))
                    .mileage(-100)
                    .build();
            var violations = KiwiValidations.validate(car);

            var response = mapper.toResponse(new ConstraintViolationException(violations));

            var expectedStatusCode = 422;
            assertStatusCode(response, expectedStatusCode);
            assertResponseMediaType(response, MediaType.APPLICATION_JSON);

            var entity = assertHasMapEntity(response);
            assertThat(entity).containsOnlyKeys("errors");

            //noinspection unchecked
            var errors = (List<ErrorMessage>) entity.get("errors");
            assertThat(errors).hasSameSizeAs(violations);
        }
    }

    @Value
    @Builder
    private static class Car {

        @NotBlank
        String make;

        @NotBlank
        String model;

        @NotNull
        @PastOrPresent
        Year year;

        @Min(0)
        @Max(300_000)
        int mileage;
    }
}
