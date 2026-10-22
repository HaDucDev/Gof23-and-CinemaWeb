package com.td.cinema.booking.controller;

import com.td.cinema.booking.dto.request.BookingCreateRequest;
import com.td.cinema.booking.dto.request.CancelRequest;
import com.td.cinema.booking.dto.request.PayRequest;
import com.td.cinema.booking.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

/** API tạo đơn, thanh toán, hủy và xem vé của người dùng đang đăng nhập. */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    /** Nhận dịch vụ đặt vé để controller chỉ phụ trách giao tiếp HTTP và tài khoản người gọi. */
    public BookingController(BookingService service) {
        this.service = service;
    }

    /**
     * Tạo đơn từ phiên giữ ghế của người dùng; Idempotency-Key giúp gửi lại cùng yêu cầu mà không
     * tạo trùng đơn.
     * Đường dẫn: {@code POST /api/bookings}.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            Authentication auth,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody BookingCreateRequest request) {
        UUID id = service.create(actor(auth), key, request);
        return ResponseEntity.created(location(id)).body(service.view(id, actor(auth), false));
    }

    /**
     * Lấy các đơn vé thuộc tài khoản đang đăng nhập.
     * Đường dẫn: {@code GET /api/bookings}.
     */
    @GetMapping
    public Object mine(Authentication auth) {
        return service.mine(actor(auth));
    }

    /**
     * Lấy thông tin đơn, ghế và lịch sử thanh toán sau khi kiểm tra quyền sở hữu.
     * Đường dẫn: {@code GET /api/bookings/{id}}.
     */
    @GetMapping("/{id}")
    public Object detail(@PathVariable UUID id, Authentication auth) {
        return service.detail(id, actor(auth), false);
    }

    /**
     * Tiếp nhận yêu cầu thanh toán để xử lý bất đồng bộ; phản hồi đã nhận chưa đồng nghĩa nghiệp vụ
     * đã hoàn tất.
     * Đường dẫn: {@code POST /api/bookings/{id}/payments}.
     */
    @PostMapping("/{id}/payments")
    public ResponseEntity<Map<String, Object>> pay(
            @PathVariable UUID id,
            Authentication auth,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody PayRequest request) {
        return ResponseEntity.accepted()
                .location(location(id))
                .body(service.pay(id, actor(auth), key, request));
    }

    /**
     * Tiếp nhận yêu cầu hủy đơn hoặc hủy vé đã thanh toán để xử lý bất đồng bộ; phản hồi đã nhận
     * chưa đồng nghĩa nghiệp vụ đã hoàn tất.
     * Đường dẫn: {@code POST /api/bookings/{id}/cancel}.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(
            @PathVariable UUID id,
            Authentication auth,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody(required = false) CancelRequest request) {
        return ResponseEntity.accepted()
                .location(location(id))
                .body(service.cancel(id, actor(auth), key, request));
    }

    /**
     * Đọc nhật ký xử lý của đơn thuộc người dùng; giao diện khách không cần hiển thị dữ liệu kỹ
     * thuật này.
     * Đường dẫn: {@code GET /api/bookings/{id}/timeline}.
     */
    @GetMapping("/{id}/timeline")
    public Object timeline(@PathVariable UUID id, Authentication auth) {
        return service.timeline(id, actor(auth), false);
    }

    /** Đọc mã tài khoản từ Authentication để không tin mã chủ sở hữu do trình duyệt tự gửi. */
    private long actor(Authentication auth) {
        return Long.parseLong(auth.getName());
    }

    /** Tạo URI chuẩn của đơn dùng trong header Location của phản hồi HTTP. */
    private URI location(UUID id) {
        return URI.create("/api/bookings/" + id);
    }
}
