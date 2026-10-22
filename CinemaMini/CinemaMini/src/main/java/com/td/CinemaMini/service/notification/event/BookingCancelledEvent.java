package com.td.CinemaMini.service.notification.event;

import com.td.CinemaMini.model.Booking;
import java.math.BigDecimal;

/** Sự kiện đơn đã hủy; có thể kèm ID và số tiền hoàn nếu đơn trước đó đã thanh toán. */
public final class BookingCancelledEvent extends BookingEvent {
    public BookingCancelledEvent(Booking b, Long refundId, BigDecimal amount) {
        // refundId là ID dòng hoàn; amount là số tiền hoàn dương, null khi đơn hủy không có hoàn.
        super(b, "BOOKING_CANCELLED", refundId, amount);
    }
}
