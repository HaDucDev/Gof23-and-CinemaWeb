package com.td.cinema.booking.client;

import com.td.cinema.booking.dto.request.HoldChangeRequest;
import com.td.cinema.booking.dto.request.HoldCommandRequest;
import com.td.cinema.booking.dto.request.HoldCreateRequest;
import com.td.cinema.booking.dto.request.HoldReleaseRequest;
import com.td.cinema.booking.dto.request.ShowtimeCreateRequest;
import com.td.cinema.booking.dto.response.HoldResponse;
import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.mapper.ApiProjectionMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class SeatClient {
    private final RestClient http;
    private final ClientResponseHandler responses;
    private final ApiProjectionMapper mapper;
    private static final ParameterizedTypeReference<List<Map<String, Object>>> ROWS =
            new ParameterizedTypeReference<List<Map<String, Object>>>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> OBJECT =
            new ParameterizedTypeReference<Map<String, Object>>() {};

    public SeatClient(
            @Qualifier("seatRestClient") RestClient http,
            ClientResponseHandler responses,
            ApiProjectionMapper mapper) {
        this.http = http;
        this.responses = responses;
        this.mapper = mapper;
    }

    public List<Map<String, Object>> movies() {
        return responses.execute(
                "Seat", () -> http.get().uri("/internal/movies").retrieve().body(ROWS));
    }

    public List<Map<String, Object>> showtimes(Long movieId, String date) {
        return responses.execute(
                "Seat",
                () ->
                        http.get()
                                .uri(
                                        builder -> {
                                            builder.path("/internal/showtimes");
                                            if (movieId != null)
                                                builder.queryParam("movieId", movieId);
                                            if (date != null) builder.queryParam("date", date);
                                            return builder.build();
                                        })
                                .retrieve()
                                .body(ROWS));
    }

    public List<Map<String, Object>> inventory(String showtime, long actor) {
        return responses.execute(
                "Seat",
                () ->
                        http.get()
                                .uri(
                                        "/internal/showtimes/{id}/seats?accountId={actor}",
                                        showtime,
                                        actor)
                                .retrieve()
                                .body(ROWS));
    }

    public Map<String, Object> template() {
        return responses.execute(
                "Seat",
                () -> http.get().uri("/internal/showtime-template").retrieve().body(OBJECT));
    }

    public Map<String, Object> createShowtime(ShowtimeCreateRequest request) {
        return responses.execute(
                "Seat",
                () ->
                        http.post()
                                .uri("/internal/showtimes/from-template")
                                .body(request)
                                .retrieve()
                                .body(OBJECT));
    }

    public Map<String, Object> createHold(long actor, String key, HoldCreateRequest request) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.post()
                                        .uri("/internal/holds")
                                        .header("X-Account-Id", String.valueOf(actor))
                                        .header("Idempotency-Key", key)
                                        .body(request)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    public Map<String, Object> hold(UUID id, long actor) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.get()
                                        .uri("/internal/holds/{id}?accountId={actor}", id, actor)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    public Map<String, Object> changeHold(
            UUID id, long actor, String key, HoldChangeRequest request) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.put()
                                        .uri("/internal/holds/{id}/seats", id)
                                        .header("X-Account-Id", String.valueOf(actor))
                                        .header("Idempotency-Key", key)
                                        .body(request)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    public Map<String, Object> abandonHold(
            UUID id, long actor, String key, HoldReleaseRequest request) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.method(HttpMethod.DELETE)
                                        .uri("/internal/holds/{id}", id)
                                        .header("X-Account-Id", String.valueOf(actor))
                                        .header("Idempotency-Key", key)
                                        .body(request)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    public Map<String, Object> checkout(UUID id, HoldCommandRequest request) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.post()
                                        .uri("/internal/holds/{id}/checkout", id)
                                        .body(request)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    public Map<String, Object> beginPayment(UUID id, HoldCommandRequest request) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.post()
                                        .uri("/internal/holds/{id}/begin-payment", id)
                                        .body(request)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    public Map<String, Object> confirm(UUID id, HoldCommandRequest request) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.post()
                                        .uri("/internal/holds/{id}/confirm", id)
                                        .body(request)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    public Map<String, Object> release(UUID id, HoldCommandRequest request) {
        return hold(
                responses.execute(
                        "Seat",
                        () ->
                                http.post()
                                        .uri("/internal/holds/{id}/release", id)
                                        .body(request)
                                        .retrieve()
                                        .body(HoldResponse.class)));
    }

    private Map<String, Object> hold(HoldResponse response) {
        if (response.holdId == null
                || response.accountId == null
                || response.status == null
                || response.revision < 0
                || response.seats == null
                || response.showtime == null)
            throw new ApiException(
                    502, "INVALID_SERVICE_RESPONSE", "Phien giu ghe tra ve khong hop le.");
        return mapper.projection(response);
    }
}
