package com.td.cinema.account.controller;

import com.td.cinema.account.dto.request.LoginRequest;
import com.td.cinema.account.dto.request.RegisterRequest;
import com.td.cinema.account.dto.response.AccountProfileResponse;
import com.td.cinema.account.mapper.ApiProjectionMapper;
import com.td.cinema.account.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/** API nội bộ để Booking đăng ký, xác thực và lấy thông tin tài khoản từ Account service. */
@RestController
@RequestMapping("/internal")
public class AccountController {
    private final AccountService service;
    private final ApiProjectionMapper mapper;

    /** Nhận dịch vụ tài khoản và bộ chuyển đổi DTO cho các API nội bộ. */
    public AccountController(AccountService service, ApiProjectionMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Nhận dữ liệu đăng ký từ Booking và trả hồ sơ tài khoản mới theo DTO phản hồi.
     * Đường dẫn: {@code POST /internal/auth/register}.
     */
    @PostMapping("/auth/register")
    public AccountProfileResponse register(@Valid @RequestBody RegisterRequest request) {
        return mapper.response(service.register(request), AccountProfileResponse.class);
    }

    /**
     * Nhận yêu cầu kiểm tra thông tin đăng nhập và trả hồ sơ tài khoản đã được Account xác thực.
     * Đường dẫn: {@code POST /internal/auth/login}.
     */
    @PostMapping("/auth/login")
    public AccountProfileResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.response(service.login(request), AccountProfileResponse.class);
    }

    /**
     * Lấy hồ sơ tài khoản đang hoạt động theo mã tài khoản để các service sử dụng.
     * Đường dẫn: {@code GET /internal/accounts/{id}}.
     */
    @GetMapping("/accounts/{id}")
    public AccountProfileResponse get(@PathVariable long id) {
        return mapper.response(service.get(id), AccountProfileResponse.class);
    }

    /**
     * Lấy danh sách quản trị viên đang hoạt động để Booking xác định người nhận thông báo.
     * Đường dẫn: {@code GET /internal/accounts/admins}.
     */
    @GetMapping("/accounts/admins")
    public List<Map<String, Object>> admins() {
        return service.admins();
    }
}
