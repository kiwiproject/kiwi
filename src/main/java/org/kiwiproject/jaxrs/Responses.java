package org.kiwiproject.jaxrs;

import lombok.experimental.UtilityClass;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

@UtilityClass
public class Responses {

    public static boolean successful(Response response) {
        return successful(response.getStatus());
    }

    public static boolean notSuccessful(Response response) {
        return notSuccessful(response.getStatus());
    }

    public static boolean successful(Response.Status status) {
        return successful(status.getStatusCode());
    }

    public static boolean successful(Response.StatusType status) {
        return successful(status.getStatusCode());
    }

    public static boolean notSuccessful(Response.StatusType status) {
        return notSuccessful(status.getStatusCode());
    }

    public static boolean notSuccessful(Response.Status status) {
        return notSuccessful(status.getStatusCode());
    }

    public static boolean successful(int statusCode) {
        return successful(Family.familyOf(statusCode));
    }

    public static boolean notSuccessful(int statusCode) {
        return !successful(statusCode);
    }

    public static boolean successful(Family family) {
        return family == Family.SUCCESSFUL;
    }

    public static boolean notSuccessful(Family family) {
        return !successful(family);
    }

}
