package com.td.cinema.choreography.seat.messaging;
import com.td.cinema.choreography.seat.dto.request.HoldCommandRequest;
import com.td.cinema.choreography.seat.service.SeatService;
import com.td.cinema.choreography.seat.util.Data;
import org.springframework.stereotype.Service;
import java.util.*;

/** Quy tắc phản ứng của Seat; EventTransactions bao quanh mỗi sự kiện bằng Local Transaction. */
@Service
public class SeatParticipant {
    private final SeatService seats;
    public SeatParticipant(SeatService seats) { this.seats = seats; }
    /**
     * Nhận PaymentFailed để chạy Compensating Transaction trả ghế HELD bằng release(PAYMENT_FAILED).
     * PaymentSucceeded xác nhận bán ghế; PaymentRefunded cho phép trả ghế SOLD của lượt hủy đã thu.
     * Phản hồi SeatReleased/SeatConfirmed chỉ được EventPublisher phát sau khi commit.
     */
    public SagaEvent onEvent(SagaEvent event) {
        HoldCommandRequest command = new HoldCommandRequest();
        command.bookingId = event.bookingId; command.accountId = event.accountId;
        command.revision = event.revision == null ? 0 : event.revision;
        // Đặt breakpoint ở nhánh sự kiện cần demo; hàm này chạy bên trong transaction của EventTransactions.
        if ("PaymentRequested".equals(event.type)) {
            // Booking chỉ yêu cầu thanh toán; Seat phải kiểm tra/khóa phiên trước khi Payment được thu tiền.
            Map<String, Object> hold = seats.beginPayment(event.holdId, command);
            String status = Data.text(hold, "status");
            if ("CONSUMED".equals(status)) return null; // Late payment request cannot restart a completed sale.
            SagaEvent reply = event.response("PAYMENT_PROCESSING".equals(status) ? "SeatPaymentReady" : "SeatPaymentRejected");
            reply.holdStatus = status;
            if (!"PAYMENT_PROCESSING".equals(status)) reply.reason = "Phien het han hoac khong the thanh toan.";
            return reply;
        }
        if ("BookingCancellationRequested".equals(event.type)) {
            // Queue Seat nhận cả CANCEL_PAID nhưng bỏ qua yêu cầu đó, chờ PaymentRefunded mới trả ghế đã bán.
            // Hủy đơn chưa thu tiền thì không cần chờ Payment, có thể trả ghế giữ ngay.
            if (!"CANCEL_UNPAID".equals(event.operationType)) return null;
            command.reason = "CANCEL_UNPAID";
        } else if ("PaymentSucceeded".equals(event.type)) {
            if (event.paymentId == null) throw new IllegalArgumentException("Missing payment ID");
            // Chuyển ghế sang đã bán trong SQL; SeatConfirmed chỉ được gửi sau khi transaction commit.
            seats.confirm(event.holdId, command);
            return event.response("SeatConfirmed");
        } else if ("PaymentFailed".equals(event.type)) command.reason = "PAYMENT_FAILED";
        else if ("PaymentRefunded".equals(event.type)) {
            // Đây là bằng chứng hoàn tiền của CANCEL_PAID, cho phép trả ghế đã bán.
            // Nếu Seat tắt khi Payment phát sự kiện này, message chờ ở queue cho tới khi Seat chạy lại.
            if (event.paymentId == null || !"CANCEL_PAID".equals(event.operationType))
                throw new IllegalArgumentException("Invalid refund event");
            command.reason = "CANCEL_PAID";
        } else throw new IllegalArgumentException("Unsupported Seat event " + event.type);
        // Điểm debug trả ghế: SQL commit ở cuối EventTransactions, không phải ngay sau dòng gọi release().
        Map<String, Object> released = seats.release(event.holdId, command);
        // Tạo phản hồi để Booking có bằng chứng ghế đã trả; EventTransactions lo lưu và gửi sau commit.
        SagaEvent reply = event.response("SeatReleased");
        reply.holdStatus = Data.text(released, "status");
        return reply;
    }
}
