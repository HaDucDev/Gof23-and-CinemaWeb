package com.td.CinemaMini.service.booking.state;

import com.td.CinemaMini.model.Booking;
import java.time.Instant;
import com.td.CinemaMini.exception.BusinessException;

/** GoF: Behavioral / State. Mỗi trạng thái thực hiện hành vi hợp lệ của đơn. */
public interface BookingState {
    /** Mặc định từ chối xác nhận; trạng thái cho phép phải định nghĩa cách xử lý riêng. */
    default void confirm(Booking booking, Instant now) {
        throw BusinessException.conflict("Trạng thái đơn không cho phép thanh toán.");
    }

    /** Mặc định từ chối hủy; trạng thái cho phép phải định nghĩa cách xử lý riêng. */
    default void cancel(Booking booking, Instant now) {
        throw BusinessException.conflict("Trạng thái đơn không cho phép hủy.");
    }

    /** Mặc định từ chối hết hạn; trạng thái cho phép phải định nghĩa cách xử lý riêng. */
    default void expire(Booking booking, Instant now) {
        throw BusinessException.conflict("Trạng thái đơn không cho phép hết hạn.");
    }
}
