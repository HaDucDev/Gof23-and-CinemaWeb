package com.td.cinema.booking.service;

import com.td.cinema.booking.client.SeatClient;
import com.td.cinema.booking.dto.request.HoldChangeRequest;
import com.td.cinema.booking.dto.request.HoldCreateRequest;
import com.td.cinema.booking.dto.request.HoldReleaseRequest;
import com.td.cinema.booking.util.Data;

import org.springframework.stereotype.Service;

import java.util.*;

/** Chuẩn hóa yêu cầu giữ ghế rồi gọi Seat, nơi sở hữu trạng thái và cơ chế khóa ghế. */
@Service
public class HoldService {
    private final SeatClient seat;

    /** Nhận client Seat, nơi sở hữu phiên giữ ghế và tồn kho ghế. */
    public HoldService(SeatClient seat) {
        this.seat = seat;
    }

    /** Kiểm tra Idempotency-Key rồi gọi Seat tạo phiên giữ ghế cho khách. */
    public Map<String, Object> create(long actor, String key, HoldCreateRequest request) {
        return seat.createHold(actor, Data.key(key), request);
    }

    /** Gọi Seat lấy phiên của đúng tài khoản, bao gồm xử lý phiên hết hạn ở phía Seat. */
    public Map<String, Object> get(UUID id, long actor) {
        return seat.hold(id, actor);
    }

    /** Gọi Seat thay tập ghế bằng token/revision và khóa chống gửi trùng. */
    public Map<String, Object> change(UUID id, long actor, String key, HoldChangeRequest request) {
        return seat.changeHold(id, actor, Data.key(key), request);
    }

    /** Gọi Seat trả toàn bộ ghế của phiên chưa tạo đơn. */
    public Map<String, Object> abandon(
            UUID id, long actor, String key, HoldReleaseRequest request) {
        return seat.abandonHold(id, actor, Data.key(key), request);
    }
}
