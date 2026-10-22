package com.td.cinema.booking.controller;

import com.td.cinema.booking.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** API xem và đánh dấu đã đọc các thông báo thuộc tài khoản đang đăng nhập. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    /** Nhận dịch vụ lưu và đọc thông báo của người dùng. */
    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /**
     * Lấy danh sách thông báo và số chưa đọc của tài khoản đang đăng nhập.
     * Đường dẫn: {@code GET /api/notifications}.
     */
    @GetMapping
    public Object mine(Authentication auth) {
        return service.notifications(Long.parseLong(auth.getName()));
    }

    /**
     * Đánh dấu đã đọc một thông báo thuộc tài khoản hiện tại.
     * Đường dẫn: {@code POST /api/notifications/{id}/read}.
     */
    @PostMapping("/{id}/read")
    public Object read(@PathVariable long id, Authentication auth) {
        return service.readNotification(id, Long.parseLong(auth.getName()));
    }
}
