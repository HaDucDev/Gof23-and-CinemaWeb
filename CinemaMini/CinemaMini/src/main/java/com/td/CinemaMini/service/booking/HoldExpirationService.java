package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Khóa và xử lý phiên giữ ghế quá hạn trong transaction riêng. */
@Service
public class HoldExpirationService {
    private final HoldSessionRepository holds;
    private final BookingRepository bookings;
    private final BookingService bookingService;
    private final HoldLifecycleService lifecycle;

    public HoldExpirationService(
            HoldSessionRepository holds,
            BookingRepository bookings,
            BookingService bookingService,
            HoldLifecycleService lifecycle) {
        this.holds = holds;
        this.bookings = bookings;
        this.bookingService = bookingService;
        this.lifecycle = lifecycle;
    }

    // Lưu việc trả ghế quá hạn độc lập với yêu cầu đang xử lý, kể cả khi yêu cầu đó bị hủy.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expire(Long id) {
        // Khóa phiên trước; bỏ qua phiên không tồn tại hoặc đã rời giai đoạn giữ/chờ thanh toán.
        HoldSession hold = holds.lockById(id).orElse(null);
        if (hold == null
                || (hold.getStatus() != HoldStatus.ACTIVE
                        && hold.getStatus() != HoldStatus.CHECKOUT)) {
            return;
        }
        // Nếu phiên đã tạo đơn, khóa cả đơn trước khi chuyển đơn sang trạng thái hết hạn.
        Booking booking = bookings.findByHoldId(id).orElse(null);
        if (booking != null) {
            booking = bookings.lockById(booking.getId()).orElseThrow(BusinessException::missing);
        }
        // Khóa các ghế liên quan rồi kiểm tra giờ hết hạn bằng thời gian database.
        lifecycle.expireIfDue(hold, booking, bookingService.lockHeld(hold));
    }
}
