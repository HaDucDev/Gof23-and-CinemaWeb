package com.td.cinema.choreography.payment.mapper;

import com.td.cinema.choreography.payment.dto.projection.JsonField;
import com.td.cinema.choreography.payment.dto.projection.ProjectionRow;
import com.td.cinema.choreography.payment.exception.ApiException;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.beans.Introspector;
import java.lang.reflect.*;
import java.util.*;

public final class RepositoryProjectionMapper {
    private static final ObjectMapper JSON = JsonMapper.builder().build();

    private RepositoryProjectionMapper() {}

    public static Map<String, Object> required(ProjectionRow value) {
        if (value == null) throw new ApiException(404, "NOT_FOUND", "Khong tim thay du lieu.");
        return optional(value);
    }

    public static Map<String, Object> optional(ProjectionRow value) {
        if (value == null) return null;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Set<Method> methods = new LinkedHashSet<Method>();
        if (Proxy.isProxyClass(value.getClass())) {
            for (Class<?> contract : value.getClass().getInterfaces())
                if (ProjectionRow.class.isAssignableFrom(contract))
                    methods.addAll(Arrays.asList(contract.getMethods()));
        } else methods.addAll(Arrays.asList(value.getClass().getMethods()));
        try {
            for (Method method : methods) {
                String name = method.getName();
                if (!name.startsWith("get")
                        || name.equals("getClass")
                        || method.getParameterCount() != 0) continue;
                Object field = method.invoke(value);
                if (field != null && method.isAnnotationPresent(JsonField.class))
                    field = JSON.readValue((String) field, Object.class);
                result.put(Introspector.decapitalize(name.substring(3)), field);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot map typed repository projection", e);
        }
        return result;
    }

    public static List<Map<String, Object>> rows(List<? extends ProjectionRow> values) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ProjectionRow value : values) result.add(required(value));
        return result;
    }
}
