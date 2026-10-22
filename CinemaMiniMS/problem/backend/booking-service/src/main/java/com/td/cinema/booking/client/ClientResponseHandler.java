package com.td.cinema.booking.client;

import com.td.cinema.booking.dto.response.ApiErrorResponse;
import com.td.cinema.booking.exception.ApiException;

import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import tools.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

@Component
public class ClientResponseHandler {
    private final ObjectMapper json;

    public ClientResponseHandler(ObjectMapper json) {
        this.json = json;
    }

    public <T> T execute(String service, Supplier<T> request) {
        try {
            T result = request.get();
            if (result == null)
                throw new ApiException(
                        502,
                        "INVALID_SERVICE_RESPONSE",
                        "Service " + service + " tra response rong.");
            return result;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                String code = "UPSTREAM_REJECTED", message = "Service tu choi yeu cau.";
                try {
                    ApiErrorResponse error =
                            json.readValue(e.getResponseBodyAsString(), ApiErrorResponse.class);
                    if (error.code != null) code = error.code;
                    if (error.message != null) message = error.message;
                } catch (Exception ignored) {
                }
                throw new ApiException(e.getStatusCode().value(), code, message);
            }
            throw unavailable(service);
        } catch (RestClientException e) {
            throw unavailable(service);
        }
    }

    private ApiException unavailable(String service) {
        return new ApiException(
                502,
                "SERVICE_UNAVAILABLE",
                "Chua nhan duoc ket qua xac dinh tu service " + service + ".");
    }
}
