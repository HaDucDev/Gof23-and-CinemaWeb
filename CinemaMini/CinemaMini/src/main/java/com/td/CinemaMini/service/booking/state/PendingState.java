package com.td.CinemaMini.service.booking.state;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import java.time.Instant;

/** Quy tắc của đơn chờ thanh toán: có thể xác nhận, hủy hoặc hết hạn. */
public class PendingState implements BookingState {
    public void confirm(Booking b, Instant now) {
        // Đánh dấu đơn đã xác nhận và ghi nhận thời điểm thanh toán được chấp nhận.
        b.setStatus(BookingStatus.CONFIRMED);
        b.setConfirmedAt(now);
    }

    public void cancel(Booking b, Instant now) {
        // Ghi trạng thái và thời điểm khách hủy đơn chưa thanh toán.
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelledAt(now);
    }

    public void expire(Booking b, Instant now) {
        // Ghi trạng thái và thời điểm kết thúc đơn do hết hạn giữ ghế.
        b.setStatus(BookingStatus.EXPIRED);
        b.setExpiredAt(now);
    }
}
