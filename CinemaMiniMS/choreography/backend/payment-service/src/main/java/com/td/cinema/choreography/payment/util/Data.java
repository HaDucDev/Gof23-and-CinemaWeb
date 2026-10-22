package com.td.cinema.choreography.payment.util;

import com.td.cinema.choreography.payment.exception.ApiException;

import java.math.BigDecimal;
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

    public static BigDecimal money(Map<String, Object> value, String key) {
        return new BigDecimal(String.valueOf(value.get(key)));
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

    public static String paymentMethod(String method) {
        if (!Arrays.asList(
                        "CASH",
                        "BANK_TRANSFER",
                        "DOMESTIC_ATM",
                        "VISA",
                        "MASTERCARD",
                        "MOMO",
                        "ZALOPAY",
                        "VNPAY")
                .contains(method)) throw ApiException.bad("Phuong thuc thanh toan khong hop le.");
        return method;
    }
}
