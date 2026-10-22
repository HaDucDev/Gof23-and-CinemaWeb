package com.td.cinema.choreography.booking.exception;

public class ApiException extends RuntimeException {
    public final int status;
    public final String code;

    public ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException conflict(String message) {
        return new ApiException(409, "CONFLICT", message);
    }

    public static ApiException bad(String message) {
        return new ApiException(400, "INVALID_REQUEST", message);
    }
}
