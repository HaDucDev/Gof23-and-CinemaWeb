package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.model.enums.SeatStatus;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.booking.BookingService.LockedBooking;
import com.td.CinemaMini.service.booking.exception.HoldExpiredException;
import com.td.CinemaMini.service.booking.stream.SeatEventStream;
import com.td.CinemaMini.service.notification.event.BookingCancelledEvent;
import com.td.CinemaMini.service.payment.PaymentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Hủy đơn thuộc khách, hoàn tiền khi cần và trả lại các ghế của đơn. */
@Service
public class CancellationService {
    private final BookingService bookings;
    private final ShowtimeRepository showtimes;
    private final PaymentRepository payments;
    private final PaymentService paymentService;
    private final RequestService requests;
    private final HoldLifecycleService lifecycle;
    private final DatabaseClock clock;
    private final SeatEventStream stream;
    private final ApplicationEventPublisher events;

    public CancellationService(
            BookingService bookings,
            ShowtimeRepository showtimes,
            PaymentRepository payments,
            PaymentService paymentService,
            RequestService requests,
            HoldLifecycleService lifecycle,
            DatabaseClock clock,
            SeatEventStream stream,
            ApplicationEventPublisher events) {
        this.bookings = bookings;
        this.showtimes = showtimes;
        this.payments = payments;
        this.paymentService = paymentService;
        this.requests = requests;
        this.lifecycle = lifecycle;
        this.clock = clock;
        this.stream = stream;
        this.events = events;
    }

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public Booking cancel(UserAccount actor, Customer customer, Long id, String key) {
        // Khóa mã yêu cầu hủy để gửi lại cùng thao tác không phát sinh thêm lần hoàn tiền.
        IdempotencyRequest request =
                requests.lock(actor.getId(), key, "CANCEL_BOOKING", id.toString());
        if (request.getReferenceId() != null) {
            return bookings.owned(request.getReferenceId(), customer);
        }
        // Khóa phiên, đơn và ghế; đồng thời kiểm tra đơn thuộc người mua đang thao tác.
        LockedBooking locked = bookings.lock(id, customer);
        Booking booking = locked.booking;
        HoldSession hold = locked.hold;
        // Đơn đã hủy thì ghi nhận yêu cầu hoàn tất và trả lại kết quả hiện có.
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            requests.complete(request, "BOOKING", id);
            return booking;
        }
        // Đơn đã hết thời gian giữ ghế được xử lý hết hạn, thay vì tiếp tục quy trình hủy.
        if (lifecycle.expireIfDue(hold, booking, locked.seats)
                || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new HoldExpiredException();
        }
        Payment refund = null;
        // Đơn đã trả tiền chỉ được hủy trước giờ chiếu; cần hoàn khoản thu gốc cho khách.
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            Showtime showtime =
                    showtimes
                            .findById(booking.getShowtimeId())
                            .orElseThrow(BusinessException::missing);
            if (!clock.now().isBefore(showtime.getStartsAt())) {
                throw BusinessException.conflict("Không thể hủy vé khi suất chiếu đã bắt đầu.");
            }
            // Lấy khóa dòng thu sau phiên, đơn và ghế; không sửa dòng thu gốc.
            Payment original =
                    payments.lockOriginal(id)
                            .orElseThrow(
                                    () ->
                                            BusinessException.conflict(
                                                    "Không tìm thấy dòng thanh toán gốc."));
            // Tạo dòng hoàn tiền từ giao dịch gốc trong cùng transaction đang khóa đơn và ghế.
            refund = paymentService.refundLocked(actor, booking, original, key);
            // Kiểm tra lại giờ chiếu sau khi gọi hoàn tiền, trước khi ghi nhận hủy đơn.
            if (!clock.now().isBefore(showtime.getStartsAt())) {
                throw BusinessException.conflict(
                        "Suất chiếu đã bắt đầu; yêu cầu hủy không được ghi nhận.");
            }
            // Chỉ trả những ghế đã bán cho chính đơn đang hủy, tránh sửa ghế của đơn khác.
            for (ShowtimeSeat row : locked.seats) {
                if (row.getStatus() == SeatStatus.BOOKED && id.equals(row.getBookingId())) {
                    row.setStatus(SeatStatus.AVAILABLE);
                    row.setBookingId(null);
                    row.setHoldId(null);
                }
            }
            // Phiên lịch sử vẫn CONSUMED sau khi hoàn tiền.
        // Đơn chưa trả tiền được hủy bằng cách trả ghế và đóng phiên giữ ghế.
        } else if (booking.getStatus() == BookingStatus.PENDING) {
            if (payments.findByBookingIdAndOperationType(id, "PAYMENT").isPresent()) {
                throw BusinessException.conflict("Đơn có lịch sử tiền không nhất quán.");
            }
            lifecycle.release(hold, locked.seats);
            hold.setStatus(HoldStatus.RELEASED);
            hold.setRevision(hold.getRevision() + 1);
        }
        // Áp dụng quy tắc trạng thái của đơn, rồi lưu kết quả cho mã yêu cầu hủy.
        new BookingWorkflow(booking).cancel(clock.now());
        requests.complete(request, "BOOKING", id);
        // Phát sự kiện hủy để lưu thông báo cho khách và admin sau khi dữ liệu được lưu thành công.
        events.publishEvent(
                new BookingCancelledEvent(
                        booking,
                        refund == null ? null : refund.getId(),
                        refund == null ? null : refund.getAmount().abs()));
        stream.changed(booking.getShowtimeId());
        return booking;
    }
}
