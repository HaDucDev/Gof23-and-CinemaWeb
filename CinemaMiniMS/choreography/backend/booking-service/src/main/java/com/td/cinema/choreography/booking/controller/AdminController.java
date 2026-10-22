package com.td.cinema.choreography.booking.controller;

import com.td.cinema.choreography.booking.dto.request.ShowtimeCreateRequest;
import com.td.cinema.choreography.booking.dto.request.SimulationRequest;
import com.td.cinema.choreography.booking.service.AdminService;
import com.td.cinema.choreography.booking.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/** API quản trị suất chiếu, đơn vé và giao dịch; SecurityConfig giới hạn cho vai trò ADMIN. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final BookingService bookings;

    /** Nhận dịch vụ quản trị và dịch vụ đơn vé để phục vụ các API ADMIN. */
    public AdminController(AdminService admin, BookingService bookings) {
        this.admin = admin;
        this.bookings = bookings;
    }

    /**
     * Trả dữ liệu phim, suất chiếu và mẫu phòng cho màn hình quản trị.
     * Đường dẫn: {@code GET /api/admin/showtimes}.
     */
    @GetMapping("/showtimes")
    public Object showtimes() {
        return admin.showtimes();
    }

    /**
     * Nhận yêu cầu tạo suất chiếu từ mẫu và chuyển cho Seat kiểm tra, lưu dữ liệu.
     * Đường dẫn: {@code POST /api/admin/showtimes/from-template}.
     */
    @PostMapping("/showtimes/from-template")
    public Object createShowtime(@Valid @RequestBody ShowtimeCreateRequest request) {
        return admin.addShowtime(request);
    }

    /**
     * Lấy danh sách đơn gần đây để quản trị viên theo dõi.
     * Đường dẫn: {@code GET /api/admin/bookings}.
     */
    @GetMapping("/bookings")
    public Object bookings() {
        return bookings.adminList();
    }

    /**
     * Lấy chi tiết một đơn theo quyền quản trị, kèm dữ liệu thanh toán.
     * Đường dẫn: {@code GET /api/admin/bookings/{id}}.
     */
    @GetMapping("/bookings/{id}")
    public Object booking(@PathVariable UUID id) {
        return bookings.detail(id, 0, true);
    }

    /**
     * Lấy nhật ký kỹ thuật của đơn để quản trị viên xem các bước liên service khi demo hoặc kiểm
     * tra lỗi.
     * Đường dẫn: {@code GET /api/admin/bookings/{id}/timeline}.
     */
    @GetMapping("/bookings/{id}/timeline")
    public Object timeline(@PathVariable UUID id) {
        return bookings.timeline(id, 0, true);
    }

    /**
     * Lấy sổ thu/hoàn tiền với giới hạn số dòng yêu cầu.
     * Đường dẫn: {@code GET /api/admin/payments}.
     */
    @GetMapping("/payments")
    public Object payments(@RequestParam(defaultValue = "100") int limit) {
        return admin.ledger(limit);
    }

    /**
     * Đọc lựa chọn SUCCESS/FAIL hiện đang được Payment dùng cho lần thanh toán mới.
     * Đường dẫn: {@code GET /api/admin/payment-simulation}.
     */
    @GetMapping("/payment-simulation")
    public Object simulation() {
        return admin.simulation();
    }

    /**
     * Đổi kết quả thanh toán giả lập tại Payment; không thay đổi kết quả của giao dịch đã xử lý.
     * Đường dẫn: {@code PUT /api/admin/payment-simulation}.
     */
    @PutMapping("/payment-simulation")
    public Object configure(@Valid @RequestBody SimulationRequest request) {
        return admin.configure(request);
    }
}
