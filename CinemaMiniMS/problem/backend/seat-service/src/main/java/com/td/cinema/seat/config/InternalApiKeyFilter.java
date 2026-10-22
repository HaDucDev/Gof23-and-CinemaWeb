package com.td.cinema.seat.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Bộ lọc bảo vệ API /internal/ bằng khóa X-Internal-Key dùng giữa các service. */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {
    private final byte[] key;

    /** Nạp khóa nội bộ từ cấu hình và từ chối khởi động nếu khóa ngắn hơn 24 ký tự. */
    public InternalApiKeyFilter(@Value("${cinema.internal-key}") String key) {
        if (key.trim().length() < 24)
            throw new IllegalStateException(
                    "INTERNAL_API_KEY must contain at least 24 characters.");
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Kiểm tra X-Internal-Key với các đường dẫn /internal/; khóa thiếu hoặc sai trả 403 trước khi
     * vào controller.
     * So sánh bằng MessageDigest.isEqual thay vì so sánh chuỗi thông thường.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/internal/")) {
            String candidate = request.getHeader("X-Internal-Key");
            if (candidate == null
                    || !MessageDigest.isEqual(key, candidate.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(403);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
