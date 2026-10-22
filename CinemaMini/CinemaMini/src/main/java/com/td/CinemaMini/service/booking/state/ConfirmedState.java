package com.td.CinemaMini.service.booking.state;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import java.time.Instant;

/** Quy tắc của đơn đã xác nhận; service hủy kiểm tra giờ chiếu và hoàn tiền trước bước này. */
public class ConfirmedState implements BookingState {
    public void cancel(Booking b, Instant now) {
        // Ghi nhận đơn đã hủy và thời điểm hủy; việc hoàn tiền và trả ghế do service phụ trách.
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelledAt(now);
    }
}
