package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertHasMapEntity;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseType;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.validation.KiwiValidations;

import java.time.Year;
import java.util.List;

@DisplayName("ConstraintViolationExceptionMapper")
class ConstraintViolationExceptionMapperTest {

    private static final int UNPROCESSABLE_ENTITY_STATUS = 422;

    private ConstraintViolationExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ConstraintViolationExceptionMapper();
    }

    // This is here only so that if Response.Status ever adds 422 Unprocessable Entity, we'll know about it...
    @Nested
    class FromStatusCode {

        @Test
        void shouldReturnNullFor422() {
            assertThat(Response.Status.fromStatusCode(UNPROCESSABLE_ENTITY_STATUS)).isNull();
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

            assertResponseStatusCode(response, UNPROCESSABLE_ENTITY_STATUS);
            assertResponseType(response, MediaType.APPLICATION_JSON);

            var entity = assertHasMapEntity(response);
            assertThat(entity).containsOnlyKeys("errors");

            @SuppressWarnings("unchecked")
            var errors = (List<ErrorMessage>) entity.get("errors");

            assertThat(errors).containsExactlyInAnyOrder(
                    new ErrorMessage(UNPROCESSABLE_ENTITY_STATUS, "must not be blank", "model"),
                    new ErrorMessage(UNPROCESSABLE_ENTITY_STATUS, "must be greater than or equal to 0", "mileage")
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

            assertResponseStatusCode(response, UNPROCESSABLE_ENTITY_STATUS);
            assertResponseType(response, MediaType.APPLICATION_JSON);

            var entity = assertHasMapEntity(response);
            assertThat(entity).containsOnlyKeys("errors");

            @SuppressWarnings("unchecked")
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
