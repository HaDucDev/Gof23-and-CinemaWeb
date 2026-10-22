package com.td.cinema.choreography.payment.dto.response;

public class ApiErrorResponse {
    public String code;
    public String message;

    public ApiErrorResponse() {}

    public ApiErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
