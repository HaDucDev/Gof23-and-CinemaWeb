package com.td.cinema.choreography.account.controller;

import com.td.cinema.choreography.account.service.HealthService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Cung cấp API kiểm tra mức độ sẵn sàng của service cho công cụ khởi động và giám sát. */
@RestController
public class HealthController {
    private final HealthService service;

    /** Nhận dịch vụ kiểm tra sẵn sàng của chính ứng dụng này. */
    public HealthController(HealthService service) {
        this.service = service;
    }

    /**
     * Trả trạng thái sẵn sàng sau khi service đã kiểm tra schema và kết nối được database.
     * Đường dẫn: {@code GET /internal/health}.
     */
    @GetMapping("/internal/health")
    public Map<String, Object> health() {
        return service.status();
    }
}
