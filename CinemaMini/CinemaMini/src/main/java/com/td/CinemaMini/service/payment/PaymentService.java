package com.td.CinemaMini.service.payment;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.model.enums.PaymentMethod;
import com.td.CinemaMini.model.enums.SeatStatus;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.booking.*;
import com.td.CinemaMini.service.booking.BookingService.LockedBooking;
import com.td.CinemaMini.service.booking.exception.HoldExpiredException;
import com.td.CinemaMini.service.booking.stream.SeatEventStream;
import com.td.CinemaMini.service.notification.event.BookingConfirmedEvent;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Thu và hoàn tiền cho đơn đặt vé, đồng thời lưu lịch sử giao dịch và cập nhật ghế. */
@Service
public class PaymentService {
    private final List<PaymentGateway> gateways;
    private final BookingService bookings;
    private final PaymentRepository payments;
    private final BookingItemRepository items;
    private final RequestService requests;
    private final HoldLifecycleService lifecycle;
    private final DatabaseClock clock;
    private final SeatEventStream stream;
    private final ApplicationEventPublisher events;

    public PaymentService(
            List<PaymentGateway> gateways,
            BookingService bookings,
            PaymentRepository payments,
            BookingItemRepository items,
            RequestService requests,
            HoldLifecycleService lifecycle,
            DatabaseClock clock,
            SeatEventStream stream,
            ApplicationEventPublisher events) {
        this.gateways = gateways;
        this.bookings = bookings;
        this.payments = payments;
        this.items = items;
        this.requests = requests;
        this.lifecycle = lifecycle;
        this.clock = clock;
        this.stream = stream;
        this.events = events;
    }

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public Booking pay(
            UserAccount actor, Customer customer, Long id, PaymentMethod method, String key) {
        if (method == null) {
            throw BusinessException.invalid("Vui lòng chọn phương thức thanh toán.");
        }
        // Nhận diện lần thanh toán theo tài khoản, mã yêu cầu, đơn và phương thức đã chọn.
        IdempotencyRequest request =
                requests.lock(actor.getId(), key, "PAYMENT", id + ":" + method.name());
        // Yêu cầu đã được xử lý thì trả đơn cũ, không gọi thu tiền thêm lần nữa.
        if (request.getReferenceId() != null) {
            return bookings.owned(request.getReferenceId(), customer);
        }
        // Khóa phiên, đơn và ghế; kiểm tra đơn thuộc khách trước khi xử lý tiền.
        LockedBooking locked = bookings.lock(id, customer);
        Booking booking = locked.booking;
        HoldSession hold = locked.hold;
        // Đơn đã thanh toán thì trả kết quả hiện có, kể cả khi khách gửi mã yêu cầu mới.
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            requests.complete(request, "BOOKING", id);
            return booking;
        }
        // Xử lý phiên quá hạn trước khi thu tiền; việc trả ghế vẫn được lưu khi báo lỗi hết hạn.
        if (lifecycle.expireIfDue(hold, booking, locked.seats)
                || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new HoldExpiredException();
        }
        // Chỉ thu tiền khi đơn còn chờ thanh toán và phiên đã chốt ghế sang CHECKOUT.
        if (booking.getStatus() != BookingStatus.PENDING
                || hold.getStatus() != HoldStatus.CHECKOUT) {
            throw BusinessException.conflict("Đơn không còn cho phép thanh toán.");
        }
        // Kiểm tra đủ số ghế và tất cả ghế vẫn đang được giữ bởi đúng phiên của đơn.
        if (locked.seats.size() != booking.getSeatCount()) {
            throw BusinessException.conflict("Số ghế của đơn không hợp lệ.");
        }
        for (ShowtimeSeat row : locked.seats) {
            if (row.getStatus() != SeatStatus.HELD || !hold.getId().equals(row.getHoldId())) {
                throw BusinessException.conflict("Không còn quyền giữ toàn bộ ghế.");
            }
        }
        // Cộng giá từng ghế đã chốt trong chi tiết đơn để đối chiếu với tổng tiền trước giảm.
        BigDecimal subtotal = BigDecimal.ZERO;
        for (BookingItem item : items.findByBookingIdOrderByIdAsc(id)) {
            subtotal = subtotal.add(item.getUnitPriceSnapshot());
        }
        if (subtotal.compareTo(booking.getSubtotal()) != 0) {
            throw BusinessException.conflict("Snapshot giá của đơn không nhất quán.");
        }
        // GoF: Adapter chuyển command chung sang API mock của phương thức đã chọn.
        PaymentResult result =
                gateway(method).charge(new PaymentCommand(id, booking.getTotal(), method));
        // Kiểm tra lại thời hạn sau khi gọi cổng mô phỏng, trước khi ghi nhận thanh toán.
        if (lifecycle.expireIfDue(hold, booking, locked.seats)) {
            throw new HoldExpiredException();
        }
        // Lưu dòng thu tiền dương; createdBy là UserAccount.id của người thực hiện yêu cầu.
        Payment payment = new Payment();
        payment.setBookingId(id);
        payment.setOperationType("PAYMENT");
        payment.setAmount(booking.getTotal());
        payment.setPaymentMethod(method);
        payment.setIdempotencyKey(key);
        // Lưu mã biên nhận do adapter trả về để lần hoàn tiền sau tham chiếu đúng giao dịch.
        payment.setReferenceCode(result.getReferenceCode());
        payment.setCreatedBy(actor.getId());
        payment.setCreatedAt(clock.now());
        payment.setReason("BOOKING_PAYMENT");
        payments.save(payment);
        // GoF: State Context quyết định hành vi xác nhận; Observer chỉ chạy sau commit.
        new BookingWorkflow(booking).confirm(clock.now());
        // Đóng phiên giữ ghế vì các ghế đã được thanh toán; tăng revision để loại dữ liệu cũ.
        hold.setStatus(HoldStatus.CONSUMED);
        hold.setRevision(hold.getRevision() + 1);
        // Chuyển ghế từ HELD sang BOOKED, gắn Booking.id và bỏ liên kết giữ ghế tạm thời.
        for (ShowtimeSeat row : locked.seats) {
            row.setStatus(SeatStatus.BOOKED);
            row.setBookingId(id);
            row.setHoldId(null);
        }
        // Gắn kết quả cho mã yêu cầu và phát sự kiện xác nhận; thông báo/SSE gửi sau commit.
        requests.complete(request, "BOOKING", id);
        events.publishEvent(new BookingConfirmedEvent(booking));
        stream.changed(booking.getShowtimeId());
        return booking;
    }

    // Hoàn tiền trong transaction hủy đơn, sau khi bên gọi đã khóa đơn, ghế và dòng thu gốc.
    @Transactional(propagation = Propagation.MANDATORY)
    public Payment refundLocked(UserAccount actor, Booking booking, Payment original, String key) {
        // Chỉ hoàn dòng thu thành công của đúng đơn, đúng tiền VND và đúng tổng tiền đơn.
        if (!original.getBookingId().equals(booking.getId())
                || !"PAYMENT".equals(original.getOperationType())
                || !"SUCCESS".equals(original.getStatus())
                || !"VND".equals(original.getCurrency())
                || original.getAmount().compareTo(booking.getTotal()) != 0) {
            throw BusinessException.conflict("Giao dịch thu gốc không hợp lệ.");
        }
        // Mỗi đơn chỉ có một dòng hoàn tiền; chặn yêu cầu hoàn lần thứ hai.
        if (payments.findByBookingIdAndOperationType(booking.getId(), "REFUND").isPresent()) {
            throw BusinessException.conflict("Đơn đã có giao dịch hoàn tiền.");
        }
        // Hoàn qua cùng phương thức và mã giao dịch gốc; RefundCommand truyền số tiền dương.
        PaymentResult result =
                gateway(original.getPaymentMethod()).refund(new RefundCommand(original));
        // Lưu thêm dòng hoàn tiền âm và liên kết originalPaymentId; dòng thu gốc được giữ nguyên.
        Payment refund = new Payment();
        refund.setBookingId(booking.getId());
        refund.setOperationType("REFUND");
        refund.setAmount(original.getAmount().negate());
        refund.setCurrency(original.getCurrency());
        refund.setPaymentMethod(original.getPaymentMethod());
        refund.setOriginalPaymentId(original.getId());
        refund.setIdempotencyKey(key);
        refund.setReferenceCode(result.getReferenceCode());
        refund.setCreatedBy(actor.getId());
        refund.setCreatedAt(clock.now());
        refund.setReason("CUSTOMER_CANCELLED");
        // Ghi dòng hoàn ngay để các kiểm tra ràng buộc chạy trước khi tiếp tục hoàn tất hủy đơn.
        return payments.saveAndFlush(refund);
    }

    private PaymentGateway gateway(PaymentMethod method) {
        // Chọn adapter hỗ trợ phương thức thanh toán; mỗi adapter đổi dữ liệu cho một nhóm client.
        return gateways.stream()
                .filter(g -> g.supports(method))
                .findFirst()
                .orElseThrow(() -> BusinessException.invalid("Phương thức chưa được hỗ trợ."));
    }
}
