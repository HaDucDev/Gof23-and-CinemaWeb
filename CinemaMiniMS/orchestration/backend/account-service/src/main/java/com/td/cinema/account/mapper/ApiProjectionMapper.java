package com.td.cinema.account.mapper;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class ApiProjectionMapper {
    private final ObjectMapper json;

    public ApiProjectionMapper(ObjectMapper json) {
        this.json = json;
    }

    public <T> T response(Map<String, Object> row, Class<T> type) {
        return json.convertValue(row, type);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> projection(Object response) {
        return json.convertValue(response, Map.class);
    }
}
