package com.td.cinema.booking.controller;

import com.td.cinema.booking.dto.request.HoldChangeRequest;
import com.td.cinema.booking.dto.request.HoldCreateRequest;
import com.td.cinema.booking.dto.request.HoldReleaseRequest;
import com.td.cinema.booking.service.HoldService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** API giữ và thay đổi ghế trước khi tạo đơn; lấy chủ sở hữu từ phiên đăng nhập. */
@RestController
@RequestMapping("/api/holds")
public class HoldController {
    private final HoldService service;

    /** Nhận dịch vụ giữ ghế để chuyển tiếp thao tác của người dùng sang Seat. */
    public HoldController(HoldService service) {
        this.service = service;
    }

    /**
     * Tạo phiên giữ 1–6 ghế cho tài khoản đang đăng nhập với khóa chống gửi trùng yêu cầu.
     * Đường dẫn: {@code POST /api/holds}.
     */
    @PostMapping
    public Object create(
            Authentication auth,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody HoldCreateRequest request) {
        return service.create(actor(auth), key, request);
    }

    /**
     * Đọc lại phiên giữ ghế của người dùng để khôi phục lựa chọn hoặc cập nhật thời hạn.
     * Đường dẫn: {@code GET /api/holds/{id}}.
     */
    @GetMapping("/{id}")
    public Object get(@PathVariable UUID id, Authentication auth) {
        return service.get(id, actor(auth));
    }

    /**
     * Thay danh sách ghế trong phiên bằng token và revision hiện tại để tránh ghi đè dữ liệu cũ.
     * Đường dẫn: {@code PUT /api/holds/{id}/seats}.
     */
    @PutMapping("/{id}/seats")
    public Object change(
            @PathVariable UUID id,
            Authentication auth,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody HoldChangeRequest request) {
        return service.change(id, actor(auth), key, request);
    }

    /**
     * Trả toàn bộ ghế của phiên chưa tạo đơn; các đơn đã tạo phải đi qua luồng hủy Booking.
     * Đường dẫn: {@code DELETE /api/holds/{id}}.
     */
    @DeleteMapping("/{id}")
    public Object abandon(
            @PathVariable UUID id,
            Authentication auth,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody HoldReleaseRequest request) {
        return service.abandon(id, actor(auth), key, request);
    }

    /** Lấy mã chủ phiên giữ ghế từ Authentication thay vì dữ liệu do frontend cung cấp. */
    private long actor(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}
