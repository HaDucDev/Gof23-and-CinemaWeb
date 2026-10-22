package com.td.cinema.booking.client;

import com.td.cinema.booking.dto.request.ChargeRequest;
import com.td.cinema.booking.dto.request.RefundRequest;
import com.td.cinema.booking.dto.request.SimulationRequest;
import com.td.cinema.booking.dto.response.PaymentResponse;
import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.mapper.ApiProjectionMapper;

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

    public Map<String, Object> charge(ChargeRequest request) {
        return payment(
                responses.execute(
                        "Payment",
                        () ->
                                http.post()
                                        .uri("/internal/payments/charge")
                                        .body(request)
                                        .retrieve()
                                        .body(PaymentResponse.class)));
    }

    public Map<String, Object> refund(UUID id, RefundRequest request) {
        return payment(
                responses.execute(
                        "Payment",
                        () ->
                                http.post()
                                        .uri("/internal/payments/{id}/refund", id)
                                        .body(request)
                                        .retrieve()
                                        .body(PaymentResponse.class)));
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

    private Map<String, Object> payment(PaymentResponse response) {
        if (response.paymentId == null
                || !Arrays.asList("SUCCEEDED", "FAILED", "REFUNDED").contains(response.status))
            throw new ApiException(
                    502, "INVALID_SERVICE_RESPONSE", "Ket qua Payment khong hop le.");
        return mapper.projection(response);
    }
}
