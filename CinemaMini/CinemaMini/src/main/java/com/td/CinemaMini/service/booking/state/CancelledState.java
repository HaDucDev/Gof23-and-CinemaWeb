package com.td.CinemaMini.service.booking.state;

import com.td.CinemaMini.model.Booking;
import java.time.Instant;

/** Đơn đã hủy chấp nhận việc hủy lại và giữ nguyên kết quả trước đó. */
public class CancelledState implements BookingState {
    public void cancel(Booking b, Instant now) {
        /* Request lặp giữ nguyên kết quả. */
    }
}
