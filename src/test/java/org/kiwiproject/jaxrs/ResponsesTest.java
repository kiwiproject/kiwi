package org.kiwiproject.jaxrs;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponsesTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testSuccessful_ForStatusCodes() {
        forEachStatus(status -> {
            int statusCode = status.getStatusCode();
            boolean successfulFamily = successful(Family.familyOf(statusCode));
            softly.assertThat(Responses.successful(statusCode))
                    .describedAs("Status code: %d", statusCode)
                    .isEqualTo(successfulFamily);
        });
    }

    @Test
    public void testNotSuccessful_ForStatusCodes() {
        forEachStatus(status -> {
            int statusCode = status.getStatusCode();
            boolean successfulFamily = successful(Family.familyOf(statusCode));
            softly.assertThat(Responses.notSuccessful(statusCode))
                    .describedAs("Status code: %d", statusCode)
                    .isNotEqualTo(successfulFamily);
        });
    }

    @Test
    public void testSuccessful_ForStatus() {
        forEachStatus(status -> {
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(Responses.successful(status))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isEqualTo(successfulFamily);
        });
    }

    @Test
    public void testNotSuccessful_ForStatus() {
        forEachStatus(status -> {
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(Responses.notSuccessful(status))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isNotEqualTo(successfulFamily);
        });
    }

    @Test
    public void testSuccessful_ForSuccessfulStatusType() {
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
        softly.assertThat(Responses.successful(type)).isTrue();
        softly.assertThat(Responses.notSuccessful(type)).isFalse();
    }

    @Test
    public void testSuccessful_ForUnsuccessfulStatusType() {
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
        softly.assertThat(Responses.successful(type)).isFalse();
        softly.assertThat(Responses.notSuccessful(type)).isTrue();
    }

    @Test
    public void testSuccessful_ForResponse() {
        forEachStatus(status -> {
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(status.getStatusCode());
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(Responses.successful(response))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isEqualTo(successfulFamily);
        });
    }

    @Test
    public void testNotSuccessful_ForResponse() {
        forEachStatus(status -> {
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(status.getStatusCode());
            boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
            softly.assertThat(Responses.notSuccessful(response))
                    .describedAs("Status: %s (%d)", status, status.getStatusCode())
                    .isNotEqualTo(successfulFamily);
        });
    }

    @Test
    public void testNotSuccessful_ForFamily() {
        Arrays.stream(Family.values()).forEach(family -> {
            boolean successfulFamily = successful(family);
            softly.assertThat(Responses.notSuccessful(family))
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