package com.td.cinema.booking.service;

import com.td.cinema.booking.client.AccountClient;
import com.td.cinema.booking.client.PaymentClient;
import com.td.cinema.booking.client.SeatClient;
import com.td.cinema.booking.dto.request.*;
import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.util.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Luồng REST đồng bộ của bản Problem. Nhánh thanh toán FAILED cố ý bỏ bước trả ghế để minh họa
 * thiếu bù trừ.
 */
@Service
public class BookingWithoutSagaService {
    private static final Logger LOG = LoggerFactory.getLogger(BookingWithoutSagaService.class);
    private final BookingStore store;
    private final AccountClient accounts;
    private final SeatClient seats;
    private final PaymentClient payments;

    /** Nhận nơi lưu đơn và các client Account, Seat, Payment cho luồng REST đồng bộ trước Saga. */
    public BookingWithoutSagaService(BookingStore store, AccountClient accounts,
            SeatClient seats, PaymentClient payments) {
        this.store = store;
        this.accounts = accounts;
        this.seats = seats;
        this.payments = payments;
    }

    /** Lấy hồ sơ khách và phiên giữ ghế, tạo đơn CREATING rồi checkout với Seat để đưa đơn sang PENDING. */
    public UUID create(long actor, String key, BookingCreateRequest request) {
        Map<String, Object> profile = accounts.profile(actor);
        Map<String, Object> hold = seats.hold(request.holdId, actor);
        UUID id = store.beginCreate(actor, key, request, profile, hold);
        Map<String, Object> booking = store.raw(id);
        if ("CREATING".equals(Data.text(booking, "status"))) {
            HoldCommandRequest command = command(booking);
            command.token = request.token;
            command.revision = request.revision;
            Map<String, Object> attached = seats.checkout(request.holdId, command);
            store.checkout(id, Data.number(attached, "revision"));
        }
        return id;
    }

    /** Dựng lệnh Seat từ mã đơn, chủ sở hữu và revision phiên đã được Booking lưu. */
    private HoldCommandRequest command(Map<String, Object> booking) {
        HoldCommandRequest command = new HoldCommandRequest();
        command.bookingId = UUID.fromString(Data.text(booking, "bookingId"));
        command.accountId = Data.number(booking, "accountId");
        command.revision = Data.number(booking, "holdRevision");
        return command;
    }

    /** Lấy mã các ADMIN đang hoạt động từ Account để gửi thông báo nghiệp vụ. */
    private List<Long> admins() {
        List<Long> result = new ArrayList<Long>();
        for (Map<String, Object> row : accounts.admins()) result.add(Data.number(row, "accountId"));
        return result;
    }

    /**
     * Thực hiện Pay hoặc hủy bằng các lời gọi REST tuần tự và ghi kết quả vào Booking.
     * Nhánh FAILED cố ý hủy đơn nhưng thiếu Seat.release; lỗi kỹ thuật được đánh dấu cần kiểm tra,
     * không giả thành kết quả FAILED.
     */
    @SuppressWarnings("unchecked")
    public void execute(UUID id, long actor, String key, String kind, String detail) {
        // Lấy người nhận thông báo trước khi tiếp nhận thao tác thay đổi trạng thái đơn.
        List<Long> adminIds = admins();
        Map<String, Object> accepted = store.beginOperation(id, actor, key, kind, detail);
        if (!Boolean.TRUE.equals(accepted.get("run"))) return;
        Map<String, Object> booking = (Map<String, Object>) accepted.get("booking");
        String operation = Data.text(booking, "operationId");
        String operationType = Data.text(accepted, "operationType");
        UUID holdId = UUID.fromString(Data.text(booking, "holdId"));
        HoldCommandRequest command = command(booking);
        try {
            if ("PAY".equals(operationType)) {
                store.step(id, "BEGIN_PAYMENT", "Seat", "COMMAND");
                try {
                    seats.beginPayment(holdId, command);
                } catch (ApiException error) {
                    if (error.status != 409 && error.status != 410) throw error;
                    Map<String, Object> hold = seats.hold(holdId, actor);
                    if ("EXPIRED".equals(Data.text(hold, "status"))) {
                        store.finish(id, "EXPIRED", "Het han giu ghe truoc thanh toan", adminIds);
                        return;
                    }
                    throw error;
                }
                store.step(id, "BEGIN_PAYMENT", "Seat", "ACCEPTED");
                ChargeRequest charge = new ChargeRequest();
                charge.bookingId = id;
                charge.amount = Data.money(booking, "amount");
                charge.paymentMethod = Data.text(booking, "paymentMethod");
                charge.operationId = "PAY-" + operation;
                store.step(id, "CHARGE_PAYMENT", "Payment", "COMMAND");
                Map<String, Object> result = payments.charge(charge);
                String status = Data.text(result, "status");
                store.paid(id, Data.text(result, "paymentId"));
                store.step(id, "CHARGE_PAYMENT", "Payment", status);
                if ("FAILED".equals(status)) {
                    // LỖI CỐ Ý KHI DEMO: Seat đã commit phiên PAYMENT_PROCESSING.
                    // Thiếu seats.release(..., PAYMENT_FAILED), nên hủy Booking vẫn chưa trả ghế.
                    store.finish(id, "CANCELLED", "Thanh toan gia lap bi tu choi", adminIds);
                    return;
                }
                if (!"SUCCEEDED".equals(status)) throw new IllegalStateException("Unexpected payment status");
                store.step(id, "CONFIRM_SEAT", "Seat", "COMMAND");
                seats.confirm(holdId, command);
                store.step(id, "CONFIRM_SEAT", "Seat", "SOLD");
                store.finish(id, "CONFIRMED", null, adminIds);
                return;
            }
            // Khách chủ động hủy vẫn đi qua luồng trả ghế/hoàn tiền bình thường.
            // Luồng này không sửa đơn đã CANCELLED do nhánh thanh toán FAILED ở trên.
            if ("CANCEL_PAID".equals(operationType)) {
                RefundRequest refund = new RefundRequest();
                refund.bookingId = id;
                refund.operationId = "REFUND-" + operation;
                store.step(id, "REFUND_PAYMENT", "Payment", "COMMAND");
                Map<String, Object> refunded = payments.refund(
                        UUID.fromString(Data.text(booking, "paymentId")), refund);
                if (!"REFUNDED".equals(Data.text(refunded, "status")))
                    throw new IllegalStateException("Unexpected refund status");
                store.step(id, "REFUND_PAYMENT", "Payment", "REFUNDED");
            }
            command.reason = operationType;
            store.step(id, "RELEASE_SEAT", "Seat", "COMMAND");
            seats.release(holdId, command);
            store.step(id, "RELEASE_SEAT", "Seat", "RELEASED");
            store.finish(id, "CANCELLED", Data.text(booking, "failureReason"), adminIds);
        } catch (Exception error) {
            String code = error instanceof ApiException ? ((ApiException) error).code : "TECHNICAL_ERROR";
            store.uncertain(id, code);
            LOG.warn("Problem operation {} needs manual review: {}", id, code);
            throw new ApiException(502, "NEEDS_REVIEW", "Chua xac dinh duoc ket qua. Can kiem tra nhat ky xu ly.");
        }
    }

    /**
     * Định kỳ đối chiếu các đơn chờ quá hạn với trạng thái Seat; không tự sửa phiên đang
     * PAYMENT_PROCESSING.
     */
    @Scheduled(fixedDelay = 2000L)
    public void expire() {
        // Chỉ xử lý phiên chờ thông thường hết hạn; không tự sửa PAYMENT_PROCESSING.
        for (Map<String, Object> row : store.expiring()) {
            try {
                Map<String, Object> hold = seats.hold(UUID.fromString(Data.text(row, "holdId")),
                        Data.number(row, "accountId"));
                if ("EXPIRED".equals(Data.text(hold, "status")))
                    store.expire(UUID.fromString(Data.text(row, "bookingId")), admins());
            } catch (Exception error) {
                LOG.debug("Waiting for ordinary Seat hold expiration");
            }
        }
    }
}
