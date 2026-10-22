package com.td.cinema.choreography.seat.util;

import com.td.cinema.choreography.seat.exception.ApiException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    public static String key(String value) {
        return required(value, "Idempotency-Key", 128);
    }

    public static String hash(String value) {
        try {
            byte[] bytes =
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format("%02x", b & 255));
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<String> seats(List<String> values) {
        if (values == null || values.isEmpty() || values.size() > 6)
            throw ApiException.bad("Chon tu 1 den 6 ghe.");
        TreeSet<String> sorted = new TreeSet<String>();
        for (String value : values) {
            if (value == null || !value.matches("[A-E][1-6]"))
                throw ApiException.bad("Ma ghe khong hop le.");
            sorted.add(value);
        }
        if (sorted.size() != values.size()) throw ApiException.bad("Danh sach ghe bi trung.");
        return new ArrayList<String>(sorted);
    }
}
