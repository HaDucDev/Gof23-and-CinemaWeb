package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import com.td.CinemaMini.service.booking.state.*;
import java.time.Instant;
import java.util.*;

/**
 * Chọn quy tắc xác nhận, hủy hoặc hết hạn theo trạng thái hiện tại của từng đơn.
 * GoF: State. Mỗi BookingWorkflow làm việc với một đơn đặt vé.
 */
public final class BookingWorkflow {
    private final Booking booking;
    private final Map<BookingStatus, BookingState> states =
            new EnumMap<BookingStatus, BookingState>(BookingStatus.class);

    public BookingWorkflow(Booking booking) {
        this.booking = booking;
        // Ánh xạ mỗi trạng thái đơn với quy tắc xử lý tương ứng.
        states.put(BookingStatus.PENDING, new PendingState());
        states.put(BookingStatus.CONFIRMED, new ConfirmedState());
        states.put(BookingStatus.CANCELLED, new CancelledState());
        states.put(BookingStatus.EXPIRED, new ExpiredState());
    }

    public void confirm(Instant now) {
        // Chỉ trạng thái hiện tại của đơn quyết định có được xác nhận thanh toán hay không.
        states.get(booking.getStatus()).confirm(booking, now);
    }

    public void cancel(Instant now) {
        // Giao thao tác hủy cho quy tắc của trạng thái hiện tại; service đã xử lý ghế và tiền.
        states.get(booking.getStatus()).cancel(booking, now);
    }

    public void expire(Instant now) {
        // Chuyển hết hạn theo quy tắc trạng thái, đồng thời lưu thời điểm hết hạn trên đơn.
        states.get(booking.getStatus()).expire(booking, now);
    }
}
