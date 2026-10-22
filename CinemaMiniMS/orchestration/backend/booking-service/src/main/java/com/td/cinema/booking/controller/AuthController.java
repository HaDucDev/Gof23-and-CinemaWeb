package com.td.cinema.booking.controller;

import com.td.cinema.booking.dto.request.LoginRequest;
import com.td.cinema.booking.dto.request.RegisterRequest;
import com.td.cinema.booking.service.AuthService;
import com.td.cinema.booking.service.SessionService;
import com.td.cinema.booking.util.Data;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** API đăng ký, đăng nhập, đăng xuất và quản lý thông tin phiên của người dùng ở Booking. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final SessionService sessions;

    /** Nhận dịch vụ gọi Account và dịch vụ quản lý session tại Booking. */
    public AuthController(AuthService service, SessionService sessions) {
        this.service = service;
        this.sessions = sessions;
    }

    /**
     * Cấp tên header và token CSRF để frontend gửi kèm các request thay đổi dữ liệu.
     * Đường dẫn: {@code GET /api/auth/csrf}.
     */
    @GetMapping("/csrf")
    public Map<String, Object> csrf(CsrfToken token) {
        return Data.map("headerName", token.getHeaderName(), "token", token.getToken());
    }

    /**
     * Chuyển yêu cầu tạo tài khoản khách sang Account qua AuthService.
     * Đường dẫn: {@code POST /api/auth/register}.
     */
    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    /**
     * Xác thực qua Account rồi tạo phiên đăng nhập ở Booking cho các request tiếp theo.
     * Đường dẫn: {@code POST /api/auth/login}.
     */
    @PostMapping("/login")
    public Map<String, Object> login(
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        Map<String, Object> account = service.login(body);
        sessions.establish(account, request, response);
        return account;
    }

    /**
     * Lấy hồ sơ tài khoản được xác định từ phiên đăng nhập hiện tại.
     * Đường dẫn: {@code GET /api/auth/me}.
     */
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        return service.profile(Long.parseLong(auth.getName()));
    }

    /**
     * Kết thúc phiên đăng nhập và đóng các kết nối thông báo của phiên đó.
     * Đường dẫn: {@code POST /api/auth/logout}.
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        return sessions.logout(request, response);
    }
}
