package com.td.cinema.seat.controller;

import com.td.cinema.seat.dto.request.ShowtimeCreateRequest;
import com.td.cinema.seat.service.ShowtimeService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/** API nội bộ lấy mẫu phòng chiếu và tạo suất chiếu cùng tồn kho ghế. */
@RestController
@RequestMapping("/internal")
public class ShowtimeController {
    private final ShowtimeService service;

    /** Nhận dịch vụ tạo suất chiếu và mẫu phòng. */
    public ShowtimeController(ShowtimeService service) {
        this.service = service;
    }

    /**
     * Lấy mẫu ghế và giá mặc định để quản trị viên tạo suất chiếu mới.
     * Đường dẫn: {@code GET /internal/showtime-template}.
     */
    @GetMapping("/showtime-template")
    public Map<String, Object> template() {
        return service.template();
    }

    /**
     * Tạo suất chiếu cùng tồn kho ghế sau khi kiểm tra dữ liệu request.
     * Đường dẫn: {@code POST /internal/showtimes/from-template}.
     */
    @PostMapping("/showtimes/from-template")
    public Map<String, Object> create(@Valid @RequestBody ShowtimeCreateRequest request) {
        return service.create(request);
    }
}
