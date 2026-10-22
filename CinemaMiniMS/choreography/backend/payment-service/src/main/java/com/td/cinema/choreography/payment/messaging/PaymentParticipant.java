package com.td.cinema.choreography.payment.messaging;
import com.td.cinema.choreography.payment.dto.request.*;
import com.td.cinema.choreography.payment.service.PaymentService;
import com.td.cinema.choreography.payment.util.Data;
import org.springframework.stereotype.Service;
import java.util.*;

/** Quy tắc thu/hoàn của Payment; mỗi sự kiện được xử lý trong Local Transaction của Payment. */
@Service
public class PaymentParticipant {
    private final PaymentService payments;
    public PaymentParticipant(PaymentService payments) { this.payments = payments; }
    /**
     * Chỉ charge sau SeatPaymentReady, rồi tạo PaymentSucceeded hoặc PaymentFailed theo kết quả.
     * Yêu cầu CANCEL_PAID gọi refund như thao tác bù trừ khoản thu đã commit và tạo PaymentRefunded.
     * Lỗi kỹ thuật xác nhận ghế không tự tạo yêu cầu refund trong phiên bản hiện tại.
     */
    public SagaEvent onEvent(SagaEvent event) {
        Map<String, Object> payment;
        SagaEvent reply;
        // Đặt breakpoint ở nhánh sự kiện muốn demo; hàm này chạy trong transaction của EventTransactions.
        if ("SeatPaymentReady".equals(event.type)) {
            // Seat đã kiểm tra phiên giữ ghế. Payment chỉ thu ở bước này, không nhận PaymentRequested.
            if (!"PAY".equals(event.operationType)) throw new IllegalArgumentException("Invalid charge operation");
            ChargeRequest request = new ChargeRequest();
            request.bookingId = event.bookingId; request.operationId = event.operationId.toString();
            request.amount = event.amount; request.paymentMethod = event.paymentMethod;
            payment = payments.charge(request);
            String status = Data.text(payment, "status");
            if ("REFUNDED".equals(status)) return null; // Old delivery cannot recreate a charge after cancellation.
            // FAIL là kết quả nghiệp vụ để Seat trả ghế; không phải exception để đưa vào queue .dead.
            reply = event.response("SUCCEEDED".equals(status) ? "PaymentSucceeded" : "PaymentFailed");
            reply.reason = Data.text(payment, "failureReason");
        } else if ("BookingCancellationRequested".equals(event.type)) {
            // Queue nhận cả hủy chưa thu tiền; trường hợp đó Payment không có khoản thu cần hoàn nên bỏ qua.
            if (!"CANCEL_PAID".equals(event.operationType)) return null;
            if (event.paymentId == null) throw new IllegalArgumentException("Paid cancellation needs original payment ID");
            RefundRequest request = new RefundRequest();
            request.bookingId = event.bookingId; request.operationId = event.operationId.toString();
            // Điểm debug hoàn tiền: thay đổi SQL và receipt chỉ commit khi EventTransactions kết thúc.
            payment = payments.refund(event.paymentId, request);
            // Phản hồi tới saga.events; binding đưa một bản cho Booking và một bản cho Seat.
            // Broker confirm của lần gửi này không chứng minh Seat đã trả ghế.
            reply = event.response("PaymentRefunded");
        } else throw new IllegalArgumentException("Unsupported Payment event " + event.type);
        reply.paymentId = UUID.fromString(Data.text(payment, "paymentId"));
        return reply;
    }
}
