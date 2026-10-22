package com.td.cinema.choreography.booking.client;

import com.td.cinema.choreography.booking.dto.request.LoginRequest;
import com.td.cinema.choreography.booking.dto.request.RegisterRequest;
import com.td.cinema.choreography.booking.dto.response.AccountProfileResponse;
import com.td.cinema.choreography.booking.exception.ApiException;
import com.td.cinema.choreography.booking.mapper.ApiProjectionMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class AccountClient {
    private final RestClient http;
    private final ClientResponseHandler responses;
    private final ApiProjectionMapper mapper;
    private static final ParameterizedTypeReference<List<Map<String, Object>>> ROWS =
            new ParameterizedTypeReference<List<Map<String, Object>>>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> OBJECT =
            new ParameterizedTypeReference<Map<String, Object>>() {};

    public AccountClient(
            @Qualifier("accountRestClient") RestClient http,
            ClientResponseHandler responses,
            ApiProjectionMapper mapper) {
        this.http = http;
        this.responses = responses;
        this.mapper = mapper;
    }

    public Map<String, Object> profile(long actor) {
        return profile(
                responses.execute(
                        "Account",
                        () ->
                                http.get()
                                        .uri("/internal/accounts/{id}", actor)
                                        .retrieve()
                                        .body(AccountProfileResponse.class)));
    }

    public Map<String, Object> register(RegisterRequest request) {
        return profile(
                responses.execute(
                        "Account",
                        () ->
                                http.post()
                                        .uri("/internal/auth/register")
                                        .body(request)
                                        .retrieve()
                                        .body(AccountProfileResponse.class)));
    }

    public Map<String, Object> login(LoginRequest request) {
        return profile(
                responses.execute(
                        "Account",
                        () ->
                                http.post()
                                        .uri("/internal/auth/login")
                                        .body(request)
                                        .retrieve()
                                        .body(AccountProfileResponse.class)));
    }

    public List<Map<String, Object>> admins() {
        return responses.execute(
                "Account", () -> http.get().uri("/internal/accounts/admins").retrieve().body(ROWS));
    }

    private Map<String, Object> profile(AccountProfileResponse response) {
        if (response.accountId < 1
                || !response.active
                || !Arrays.asList("USER", "ADMIN").contains(response.role))
            throw new ApiException(502, "INVALID_SERVICE_RESPONSE", "Ho so Account khong hop le.");
        return mapper.projection(response);
    }
}
