package com.td.cinema.choreography.booking.service;

import com.td.cinema.choreography.booking.client.PaymentClient;
import com.td.cinema.choreography.booking.client.SeatClient;
import com.td.cinema.choreography.booking.dto.request.ShowtimeCreateRequest;
import com.td.cinema.choreography.booking.dto.request.SimulationRequest;
import com.td.cinema.choreography.booking.util.Data;

import org.springframework.stereotype.Service;

import java.util.*;

/** Tổng hợp dữ liệu quản trị từ Seat và Payment, giữ quyền sở hữu dữ liệu ở từng service. */
@Service
public class AdminService {
    private final SeatClient seat;
    private final PaymentClient payment;

    /** Nhận client Seat và Payment để tổng hợp nghiệp vụ quản trị mà không đọc chéo schema. */
    public AdminService(SeatClient seat, PaymentClient payment) {
        this.seat = seat;
        this.payment = payment;
    }

    /** Gộp suất chiếu, danh sách phim và mẫu phòng từ Seat thành dữ liệu cho màn hình quản trị. */
    public Map<String, Object> showtimes() {
        return Data.map(
                "showtimes",
                seat.showtimes(null, null),
                "movies",
                seat.movies(),
                "template",
                seat.template());
    }

    /** Gửi yêu cầu tạo suất chiếu sang Seat, nơi kiểm tra lịch phòng và tạo tồn kho ghế. */
    public Map<String, Object> addShowtime(ShowtimeCreateRequest request) {
        return seat.createShowtime(request);
    }

    /** Lấy sổ thu/hoàn từ Payment với giới hạn 1–500 dòng. */
    public List<Map<String, Object>> ledger(int limit) {
        return payment.ledger(Math.min(500, Math.max(1, limit)));
    }

    /** Đọc lựa chọn SUCCESS/FAIL đang được Payment áp dụng. */
    public Map<String, Object> simulation() {
        return payment.simulation();
    }

    /** Chuyển lựa chọn SUCCESS/FAIL sang Payment cho các lần thanh toán mới. */
    public Map<String, Object> configure(SimulationRequest request) {
        return payment.configure(request);
    }
}
