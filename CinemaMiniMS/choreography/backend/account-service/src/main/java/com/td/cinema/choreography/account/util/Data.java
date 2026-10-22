package com.td.cinema.choreography.account.util;

import com.td.cinema.choreography.account.exception.ApiException;

import java.util.*;

public final class Data {
    private Data() {}

    public static Map<String, Object> map(Object... items) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < items.length; i += 2) result.put((String) items[i], items[i + 1]);
        return result;
    }

    public static String text(Map<String, Object> value, String key) {
        Object v = value.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public static long number(Map<String, Object> value, String key) {
        return ((Number) value.get(key)).longValue();
    }

    public static String required(String value, String name, int max) {
        if (value == null || value.trim().isEmpty() || value.length() > max)
            throw ApiException.bad(name + " khong hop le.");
        return value.trim();
    }
}
