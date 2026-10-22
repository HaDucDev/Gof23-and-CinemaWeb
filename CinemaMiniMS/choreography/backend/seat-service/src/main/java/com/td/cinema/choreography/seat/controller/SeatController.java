package com.td.cinema.choreography.seat.controller;

import com.td.cinema.choreography.seat.dto.request.HoldChangeRequest;
import com.td.cinema.choreography.seat.dto.request.HoldCommandRequest;
import com.td.cinema.choreography.seat.dto.request.HoldCreateRequest;
import com.td.cinema.choreography.seat.dto.request.HoldReleaseRequest;
import com.td.cinema.choreography.seat.dto.response.HoldResponse;
import com.td.cinema.choreography.seat.mapper.ApiProjectionMapper;
import com.td.cinema.choreography.seat.service.CatalogService;
import com.td.cinema.choreography.seat.service.SeatService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/** API nội bộ quản lý sơ đồ ghế và vòng đời phiên giữ ghế cho Booking. */
@RestController
@RequestMapping("/internal")
public class SeatController {
    private final SeatService service;
    private final CatalogService catalog;
    private final ApiProjectionMapper mapper;

    /** Nhận các dịch vụ ghế, danh mục và bộ chuyển đổi DTO phiên giữ ghế. */
    public SeatController(SeatService service, CatalogService catalog, ApiProjectionMapper mapper) {
        this.service = service;
        this.catalog = catalog;
        this.mapper = mapper;
    }

    /**
     * Cung cấp danh sách phim cho Booking qua API nội bộ.
     * Đường dẫn: {@code GET /internal/movies}.
     */
    @GetMapping("/movies")
    public List<Map<String, Object>> movies() {
        return catalog.movies();
    }

    /**
     * Cung cấp các suất chiếu theo bộ lọc phim và ngày.
     * Đường dẫn: {@code GET /internal/showtimes}.
     */
    @GetMapping("/showtimes")
    public List<Map<String, Object>> showtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String date) {
        return catalog.showtimes(movieId, date);
    }

    /**
     * Trả trạng thái tồn kho ghế của một suất, có thông tin ghế thuộc người đang xem.
     * Đường dẫn: {@code GET /internal/showtimes/{id}/seats}.
     */
    @GetMapping("/showtimes/{id}/seats")
    public List<Map<String, Object>> seats(
            @PathVariable String id, @RequestParam(defaultValue = "0") long accountId) {
        return service.inventory(id, accountId);
    }

    /**
     * Đăng ký kết nối SSE của một suất để Booking chuyển tiếp thay đổi ghế tới frontend.
     * Đường dẫn: {@code GET /internal/showtimes/{id}/seat-events}.
     */
    @GetMapping(value = "/showtimes/{id}/seat-events", produces = "text/event-stream")
    public SseEmitter events(@PathVariable String id) {
        return catalog.subscribe(id);
    }

    /**
     * Nhận yêu cầu giữ ghế cho tài khoản được Booking xác định và chuyển cho SeatService xử lý.
     * Đường dẫn: {@code POST /internal/holds}.
     */
    @PostMapping("/holds")
    public HoldResponse create(
            @RequestHeader("X-Account-Id") long actor,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody HoldCreateRequest request) {
        return response(service.create(actor, key, request));
    }

    /**
     * Đọc phiên giữ ghế sau khi SeatService kiểm tra chủ sở hữu và thời hạn.
     * Đường dẫn: {@code GET /internal/holds/{id}}.
     */
    @GetMapping("/holds/{id}")
    public HoldResponse get(@PathVariable UUID id, @RequestParam long accountId) {
        return response(service.get(id, accountId));
    }

    /**
     * Thay đổi tập ghế đang giữ bằng token, revision và khóa chống gửi trùng.
     * Đường dẫn: {@code PUT /internal/holds/{id}/seats}.
     */
    @PutMapping("/holds/{id}/seats")
    public HoldResponse change(
            @PathVariable UUID id,
            @RequestHeader("X-Account-Id") long actor,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody HoldChangeRequest request) {
        return response(service.change(id, actor, key, request));
    }

    /**
     * Trả một phiên giữ ghế chưa gắn vào đơn.
     * Đường dẫn: {@code DELETE /internal/holds/{id}}.
     */
    @DeleteMapping("/holds/{id}")
    public HoldResponse abandon(
            @PathVariable UUID id,
            @RequestHeader("X-Account-Id") long actor,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody HoldReleaseRequest request) {
        return response(service.abandon(id, actor, key, request));
    }

    /**
     * Gắn phiên giữ ghế vào đơn Booking để chuyển sang bước chờ thanh toán.
     * Đường dẫn: {@code POST /internal/holds/{id}/checkout}.
     */
    @PostMapping("/holds/{id}/checkout")
    public HoldResponse checkout(
            @PathVariable UUID id, @Valid @RequestBody HoldCommandRequest request) {
        return response(service.attach(id, request));
    }

    /** Chuyển dữ liệu phiên từ service thành HoldResponse theo hợp đồng API. */
    private HoldResponse response(Map<String, Object> row) {
        return mapper.response(row, HoldResponse.class);
    }
}
