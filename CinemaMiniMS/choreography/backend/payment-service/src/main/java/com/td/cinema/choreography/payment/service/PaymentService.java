package com.td.cinema.choreography.payment.service;

import com.td.cinema.choreography.payment.dto.request.ChargeRequest;
import com.td.cinema.choreography.payment.dto.request.RefundRequest;
import com.td.cinema.choreography.payment.dto.request.SimulationRequest;
import com.td.cinema.choreography.payment.exception.ApiException;
import com.td.cinema.choreography.payment.model.PaymentOutcome;
import com.td.cinema.choreography.payment.repository.PaymentRepository;
import com.td.cinema.choreography.payment.util.Data;
import com.td.cinema.choreography.payment.util.BookingFlowLog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Mô phỏng thu/hoàn tiền với chống xử lý trùng; kết quả thanh toán và sổ thu/hoàn được lưu trong Payment. */
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    // Mỗi bản demo có Payment riêng; khởi động lại sẽ đặt kết quả giả lập về SUCCESS.
    private volatile Map<String, Object> simulationState = Data.map(
            "outcome", "SUCCESS", "updatedAt", OffsetDateTime.now(ZoneOffset.UTC).toString());

    /** Nhận repository lưu kết quả thanh toán và sổ thu/hoàn của Payment. */
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /** Trả bản sao cấu hình kết quả giả lập để bên gọi không sửa trực tiếp trạng thái đang dùng. */
    public Map<String, Object> simulation() {
        return new LinkedHashMap<String, Object>(simulationState);
    }

    /**
     * Kiểm tra và lưu SUCCESS/FAIL trong RAM; khởi động lại Payment sẽ về SUCCESS, giao dịch cũ
     * không đổi.
     */
    public Map<String, Object> configure(SimulationRequest request) {
        PaymentOutcome.parse(request.outcome);
        simulationState = Data.map("outcome", request.outcome,
                "updatedAt", OffsetDateTime.now(ZoneOffset.UTC).toString());
        return simulation();
    }

    /**
     * Khóa theo đơn và kiểm tra yêu cầu trùng trước khi ghi kết quả SUCCEEDED/FAILED.
     * Chỉ SUCCEEDED mới ghi dòng thu tiền; FAILED là kết quả nghiệp vụ bình thường, không phải
     * service bị tắt.
     */
    @Transactional
    public Map<String, Object> charge(ChargeRequest request) {
        String method = Data.paymentMethod(request.paymentMethod);
        String operation = Data.key(request.operationId);
        paymentRepository.lockOperation("charge:" + request.bookingId);
        String fingerprint =
                Data.hash(request.bookingId + "|" + request.amount.toPlainString() + "|" + method);
        Map<String, Object> existing =
                paymentRepository.findChargeByBookingOrKey(request.bookingId.toString(), operation);
        if (existing != null) {
            if (!fingerprint.equals(Data.text(existing, "requestHash"))
                    || !operation.equals(Data.text(existing, "idempotencyKey")))
                throw ApiException.conflict("Khoa thanh toan da duoc dung voi noi dung khac.");
            return existing;
        }
        if (request.amount.scale() > 0) throw ApiException.bad("So tien phai la so nguyen dong.");
        String outcome = Data.text(simulation(), "outcome");
        boolean success = PaymentOutcome.parse(outcome) == PaymentOutcome.SUCCESS;
        UUID paymentId = UUID.randomUUID();
        paymentRepository.insertPayment(
                paymentId.toString(),
                request.bookingId.toString(),
                request.amount,
                success ? "SUCCEEDED" : "FAILED",
                operation,
                fingerprint,
                method,
                outcome,
                success ? null : "Thanh toan gia lap bi tu choi.");
        if (success)
            paymentRepository.appendCharge(
                    paymentId.toString(),
                    request.bookingId.toString(),
                    request.amount,
                    method,
                    operation,
                    "PAY-" + paymentId,
                    "Thanh toan ve");
        BookingFlowLog.committed(request.bookingId, "Payment", "CHARGE_PAYMENT",
                success ? "SUCCEEDED" : "FAILED");
        return get(paymentId);
    }

    /**
     * Khóa khoản thu, kiểm tra đúng đơn và hoàn trùng rồi ghi khoản hoàn liên kết giao dịch gốc.
     * Chỉ hoàn Payment SUCCEEDED; không dùng công tắc giả lập thu tiền để quyết định kết quả hoàn.
     * Đây là Compensating Transaction cho khoản thu đã commit: thêm dòng REFUND mới, giữ lịch sử
     * thu gốc. Trong Saga hiện tại, luồng CANCEL_PAID gọi hàm này; Payment FAILED không cần refund.
     */
    @Transactional
    public Map<String, Object> refund(UUID id, RefundRequest request) {
        paymentRepository.lockOperation("refund:" + id);
        Map<String, Object> payment = paymentRepository.lockPayment(id.toString());
        if (!request.bookingId.toString().equals(Data.text(payment, "bookingId")))
            throw ApiException.conflict("Khoan thu khong thuoc don.");
        String key = Data.key(request.operationId);
        Map<String, Object> prior = paymentRepository.findRefund(id.toString());
        if (prior != null) {
            if (!key.equals(Data.text(prior, "idempotencyKey")))
                throw ApiException.conflict("Khoan thu da hoan voi thao tac khac.");
            return get(id);
        }
        if (!"SUCCEEDED".equals(Data.text(payment, "status")))
            throw ApiException.conflict("Chi hoan khoan thu thanh cong.");
        Map<String, Object> original = paymentRepository.findOriginalCharge(id.toString());
        // Hoàn tiền theo phương thức gốc, không dùng công tắc giả lập kết quả thu tiền.
        paymentRepository.appendRefund(
                id.toString(),
                request.bookingId.toString(),
                Data.money(payment, "amount").negate(),
                Data.text(original, "paymentMethod"),
                Data.text(original, "transactionId"),
                key,
                "REF-" + id,
                "Huy ve truoc gio chieu");
        paymentRepository.markRefunded(id.toString());
        BookingFlowLog.committed(request.bookingId, "Payment", "REFUND_PAYMENT", "REFUNDED");
        return get(id);
    }

    /** Lấy kết quả thanh toán theo paymentId từ database Payment. */
    public Map<String, Object> get(UUID id) {
        return paymentRepository.findPayment(id.toString());
    }

    /** Trả Payment và toàn bộ dòng thu/hoàn của một đơn để Booking ghép vào chi tiết vé. */
    public Map<String, Object> byBooking(UUID bookingId) {
        return Data.map(
                "payment",
                paymentRepository.findByBooking(bookingId.toString()),
                "transactions",
                paymentRepository.findTransactionsByBooking(bookingId.toString()));
    }

    /** Đọc sổ giao dịch với giới hạn 1–500 dòng để phục vụ quản trị. */
    public List<Map<String, Object>> ledger(int limit) {
        return paymentRepository.findLedger(Math.min(500, Math.max(1, limit)));
    }
}
