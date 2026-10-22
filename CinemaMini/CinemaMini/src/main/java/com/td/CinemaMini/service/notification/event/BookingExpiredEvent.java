package com.td.CinemaMini.service.notification.event;

import com.td.CinemaMini.model.Booking;

/** Sự kiện đơn chờ thanh toán hết hạn giữ ghế, dùng nhắc khách chọn ghế lại. */
public final class BookingExpiredEvent extends BookingEvent {
    public BookingExpiredEvent(Booking b) {
        // Đơn hết hạn giữ ghế không kèm khoản hoàn tiền.
        super(b, "BOOKING_EXPIRED", null, null);
    }
}
