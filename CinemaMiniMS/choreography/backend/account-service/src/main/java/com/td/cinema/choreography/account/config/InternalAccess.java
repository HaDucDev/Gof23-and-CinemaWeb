package com.td.cinema.choreography.account.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalAccess extends OncePerRequestFilter {
    private final byte[] key;

    public InternalAccess(@Value("${cinema.internal-key}") String key) {
        if (key.trim().length() < 24)
            throw new IllegalStateException(
                    "INTERNAL_API_KEY must contain at least 24 characters.");
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

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
