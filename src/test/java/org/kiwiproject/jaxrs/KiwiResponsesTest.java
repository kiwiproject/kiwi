package org.kiwiproject.jaxrs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import java.util.Arrays;
import java.util.function.Consumer;

@ExtendWith(SoftAssertionsExtension.class)
class KiwiResponsesTest {

    @Test
    void testSuccessful_ForStatusCodes(SoftAssertions softly) {
        forEachStatus(status -> {
            int statusCode = status.getStatusCode();
            boolean successfulFamily = successful(Family.familyOf(statusCode));
            softly.assertThat(KiwiResponses.successful(statusCode))
                    .describedAs("Status code: %d", statusCode)
                    .isEqualTo(successfulFamily);
        });
    }

    @Test
    void testNotSuccessful_ForStatusCodes(SoftAssertions softly) {
        forEachStatus(status -> {
            int statusCode = status.getStatusCode();
            boolean successfulFamily = successful(Family.familyOf(statusCode));
            softly.assertThat(KiwiResponses.notSuccessful(statusCode))
                    .describedAs("Status code: %d", statusCode)
                    .isNotEqualTo(successfulFamily);
        });
    }

    @Test
    void testSuccessful_ForStatus(SoftAssertions softly) {
        forEachStatus(status -> {
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(KiwiResponses.successful(status))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isEqualTo(successfulFamily);
        });
    }

    @Test
    void testNotSuccessful_ForStatus(SoftAssertions softly) {
        forEachStatus(status -> {
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(KiwiResponses.notSuccessful(status))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isNotEqualTo(successfulFamily);
        });
    }

    @Test
    void testSuccessful_ForSuccessfulStatusType(SoftAssertions softly) {
        Response.StatusType type = new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return 200;
            }

            @Override
            public Family getFamily() {
                return Family.SUCCESSFUL;
            }

            @Override
            public String getReasonPhrase() {
                return "OK";
            }
        };
        softly.assertThat(KiwiResponses.successful(type)).isTrue();
        softly.assertThat(KiwiResponses.notSuccessful(type)).isFalse();
    }

    @Test
    void testSuccessful_ForUnsuccessfulStatusType(SoftAssertions softly) {
        Response.StatusType type = new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return 500;
            }

            @Override
            public Family getFamily() {
                return Family.SERVER_ERROR;
            }

            @Override
            public String getReasonPhrase() {
                return "Server Error";
            }
        };
        softly.assertThat(KiwiResponses.successful(type)).isFalse();
        softly.assertThat(KiwiResponses.notSuccessful(type)).isTrue();
    }

    @Test
    void testSuccessful_ForResponse(SoftAssertions softly) {
        forEachStatus(status -> {
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(status.getStatusCode());
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(KiwiResponses.successful(response))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isEqualTo(successfulFamily);
        });
    }

    @Test
    void testNotSuccessful_ForResponse(SoftAssertions softly) {
        forEachStatus(status -> {
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(status.getStatusCode());
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(KiwiResponses.notSuccessful(response))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isNotEqualTo(successfulFamily);
        });
    }

    @Test
    void testNotSuccessful_ForFamily(SoftAssertions softly) {
        Arrays.stream(Family.values()).forEach(family -> {
            boolean successfulFamily = successful(family);
            softly.assertThat(KiwiResponses.notSuccessful(family))
                    .describedAs("Family: %s", family)
                    .isNotEqualTo(successfulFamily);
        });
    }

    private void forEachStatus(Consumer<Response.Status> statusConsumer) {
        Arrays.stream(Response.Status.values()).forEach(statusConsumer);
    }

    private boolean successful(Family family) {
        return family == Family.SUCCESSFUL;
    }

}