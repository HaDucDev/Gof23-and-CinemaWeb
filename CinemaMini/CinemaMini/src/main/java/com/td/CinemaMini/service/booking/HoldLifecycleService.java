package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.model.enums.SeatStatus;
import com.td.CinemaMini.service.booking.stream.SeatEventStream;
import com.td.CinemaMini.service.notification.event.BookingExpiredEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Thực hiện thay đổi trạng thái phiên, đơn và ghế khi hết hạn hoặc trả ghế. */
@Service
public class HoldLifecycleService {
    private final DatabaseClock clock;
    private final SeatEventStream stream;
    private final ApplicationEventPublisher events;

    public HoldLifecycleService(
            DatabaseClock clock, SeatEventStream stream, ApplicationEventPublisher events) {
        this.clock = clock;
        this.stream = stream;
        this.events = events;
    }

    // Bắt buộc chạy trong transaction của bên gọi, sau khi phiên, đơn và ghế đã được khóa.
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean expireIfDue(HoldSession hold, Booking booking, List<ShowtimeSeat> rows) {
        // Chỉ phiên đang giữ ghế hoặc đang chờ thanh toán mới có thể bị xử lý hết hạn.
        if (hold.getStatus() != HoldStatus.ACTIVE && hold.getStatus() != HoldStatus.CHECKOUT) {
            return false;
        }
        // Lấy giờ hiện tại sau khi chờ khóa; phiên còn thời gian thì chưa được trả ghế.
        Instant now = clock.now();
        if (now.isBefore(hold.getExpiresAt())) {
            return false;
        }
        // Bảo vệ đơn đã thanh toán: không trả ghế của đơn đã xác nhận theo hạn giữ ghế cũ.
        if (booking != null && booking.getStatus() == BookingStatus.CONFIRMED) {
            return false;
        }
        // Trả ghế của phiên rồi tăng revision để các yêu cầu dùng dữ liệu cũ bị nhận diện.
        release(hold, rows);
        hold.setStatus(HoldStatus.EXPIRED);
        hold.setRevision(hold.getRevision() + 1);
        // Đồng thời chuyển đơn chờ thanh toán sang hết hạn và phát sự kiện cho các nơi nhận.
        if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
            new BookingWorkflow(booking).expire(now);
            events.publishEvent(new BookingExpiredEvent(booking));
        }
        // Yêu cầu cập nhật sơ đồ ghế sau khi transaction lưu thay đổi thành công.
        stream.changed(hold.getShowtimeId());
        return true;
    }

    public void release(HoldSession hold, List<ShowtimeSeat> rows) {
        // Chỉ mở lại ghế HELD thuộc chính phiên này; ghế đã bán hoặc thuộc phiên khác giữ nguyên.
        for (ShowtimeSeat row : rows) {
            if (row.getStatus() == SeatStatus.HELD && hold.getId().equals(row.getHoldId())) {
                row.setStatus(SeatStatus.AVAILABLE);
                row.setHoldId(null);
                row.setBookingId(null);
            }
        }
    }
}
