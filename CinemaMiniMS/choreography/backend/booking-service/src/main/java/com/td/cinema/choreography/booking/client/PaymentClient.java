package com.td.cinema.choreography.booking.client;

import com.td.cinema.choreography.booking.dto.request.SimulationRequest;
import com.td.cinema.choreography.booking.mapper.ApiProjectionMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class PaymentClient {
    private final RestClient http;
    private final ClientResponseHandler responses;
    private final ApiProjectionMapper mapper;
    private static final ParameterizedTypeReference<List<Map<String, Object>>> ROWS =
            new ParameterizedTypeReference<List<Map<String, Object>>>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> OBJECT =
            new ParameterizedTypeReference<Map<String, Object>>() {};

    public PaymentClient(
            @Qualifier("paymentRestClient") RestClient http,
            ClientResponseHandler responses,
            ApiProjectionMapper mapper) {
        this.http = http;
        this.responses = responses;
        this.mapper = mapper;
    }

    public Map<String, Object> byBooking(UUID id) {
        return responses.execute(
                "Payment",
                () ->
                        http.get()
                                .uri("/internal/payments/by-booking/{id}", id)
                                .retrieve()
                                .body(OBJECT));
    }

    public List<Map<String, Object>> ledger(int limit) {
        return responses.execute(
                "Payment",
                () ->
                        http.get()
                                .uri("/internal/payments?limit={limit}", limit)
                                .retrieve()
                                .body(ROWS));
    }

    public Map<String, Object> simulation() {
        return responses.execute(
                "Payment",
                () -> http.get().uri("/internal/payment-simulation").retrieve().body(OBJECT));
    }

    public Map<String, Object> configure(SimulationRequest request) {
        return responses.execute(
                "Payment",
                () ->
                        http.put()
                                .uri("/internal/payment-simulation")
                                .body(request)
                                .retrieve()
                                .body(OBJECT));
    }

}
