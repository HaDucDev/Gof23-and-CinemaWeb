package com.td.CinemaMini.service.notification.event;

import com.td.CinemaMini.model.Booking;

/** Sự kiện đơn đã xác nhận thanh toán, dùng thông báo vé và hoạt động đặt vé. */
public final class BookingConfirmedEvent extends BookingEvent {
    public BookingConfirmedEvent(Booking b) {
        // Đơn xác nhận không kèm giao dịch hoàn nên hai thông tin hoàn tiền được để null.
        super(b, "BOOKING_CONFIRMED", null, null);
    }
}
