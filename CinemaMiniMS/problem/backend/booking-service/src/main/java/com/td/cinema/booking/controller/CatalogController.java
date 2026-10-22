package com.td.cinema.booking.controller;

import com.td.cinema.booking.service.CatalogService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FilterOutputStream;
import java.io.IOException;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.*;

/** API phim, suất chiếu và sơ đồ ghế; chuyển tiếp cập nhật ghế SSE từ Seat tới trình duyệt. */
@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService service;

    /** Nhận dịch vụ danh mục để lấy phim, suất và luồng cập nhật ghế. */
    public CatalogController(CatalogService service) {
        this.service = service;
    }

    /**
     * Trả danh sách phim để frontend hiển thị trang chọn phim.
     * Đường dẫn: {@code GET /api/movies}.
     */
    @GetMapping("/movies")
    public Object movies() {
        return service.movies();
    }

    /**
     * Lấy các suất chiếu, có thể lọc theo phim và ngày.
     * Đường dẫn: {@code GET /api/showtimes}.
     */
    @GetMapping("/showtimes")
    public Object showtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String date) {
        return service.showtimes(movieId, date);
    }

    /**
     * Lấy sơ đồ ghế; nếu đã đăng nhập thì dùng tài khoản hiện tại để xác định ghế khách đang giữ.
     * Đường dẫn: {@code GET /api/showtimes/{id}/seats}.
     */
    @GetMapping("/showtimes/{id}/seats")
    public Object seats(@PathVariable String id, Authentication auth) {
        return service.seats(id, auth == null ? 0 : Long.parseLong(auth.getName()));
    }

    /**
     * Mở luồng SSE từ Booking tới trình duyệt và chuyển tiếp các cập nhật ghế từ Seat.
     * Đường dẫn: {@code GET /api/showtimes/{id}/seat-events}.
     */
    @GetMapping(value = "/showtimes/{id}/seat-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> events(
            @PathVariable String id, HttpServletResponse response) {
        StreamingResponseBody relay = service.events(id);
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(output -> relay.writeTo(new FilterOutputStream(output) {
                    /** Đẩy dữ liệu SSE ra trình duyệt ngay sau mỗi lần ghi, không chờ đầy bộ đệm HTTP. */
                    @Override
                    public void flush() throws IOException {
                        super.flush();
                        // Luồng do Spring 7 cung cấp không tự flush; SSE cần đẩy từng sự kiện ngay.
                        response.flushBuffer();
                    }
                }));
    }
}
