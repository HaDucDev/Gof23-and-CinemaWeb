package com.td.cinema.booking.service.saga;

import com.td.cinema.booking.client.AccountClient;
import com.td.cinema.booking.client.PaymentClient;
import com.td.cinema.booking.client.SeatClient;
import com.td.cinema.booking.dto.request.BookingCreateRequest;
import com.td.cinema.booking.dto.request.ChargeRequest;
import com.td.cinema.booking.dto.request.HoldCommandRequest;
import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.util.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Điểm tiếp nhận và phân luồng xử lý Saga bên trong microservice Booking.
 * Thanh toán/hủy đơn chưa thanh toán chạy ở run(); hủy vé đã thanh toán
 * chuyển sang PaidCancellationSaga để lưu bước và tiếp tục sau lỗi.
 */
@Service
public class BookingSagaOrchestrator {
    private final BookingTransactionService store;
    private final AccountClient accounts;
    private final SeatClient seats;
    private final PaymentClient payments;
    private final ThreadPoolTaskExecutor executor;
    private final PaidCancellationSaga cancellations;
    private static final Logger LOG = LoggerFactory.getLogger(BookingSagaOrchestrator.class);

    /** Nhận nơi lưu tiến trình, các client liên service và nhóm luồng chạy Saga nền. */
    public BookingSagaOrchestrator(
            BookingTransactionService store,
            AccountClient accounts,
            SeatClient seats,
            PaymentClient payments,
            @Qualifier("sagaExecutor") ThreadPoolTaskExecutor executor,
            PaidCancellationSaga cancellations) {
        this.store = store;
        this.accounts = accounts;
        this.seats = seats;
        this.payments = payments;
        this.executor = executor;
        this.cancellations = cancellations;
    }

    /** Lấy hồ sơ khách và phiên giữ ghế, tạo đơn CREATING rồi checkout với Seat để đưa đơn sang PENDING. */
    public UUID create(long actor, String key, BookingCreateRequest request) {
        Map<String, Object> profile = accounts.profile(actor);
        Map<String, Object> hold = seats.hold(request.holdId, actor);
        UUID id = store.beginCreate(actor, key, request, profile, hold);
        if ("CREATING".equals(Data.text(store.raw(id), "status"))) {
            HoldCommandRequest checkout = command(store.raw(id));
            checkout.token = request.token;
            checkout.revision = request.revision;
            Map<String, Object> attached = seats.checkout(request.holdId, checkout);
            store.checkout(id, Data.number(attached, "revision"));
        }
        return id;
    }

    /**
     * Tiếp nhận yêu cầu từ BookingService, lưu tiến trình rồi giao cho luồng xử lý nền.
     * Hàm này không chờ toàn bộ nghiệp vụ thanh toán hoặc hủy vé hoàn tất.
     *
     * Trong demo hủy vé đã mua: kind là CANCEL; enqueue đọc đơn đang CONFIRMED
     * để xác định operationType=CANCEL_PAID, rồi nhánh dưới giao việc cho PaidCancellationSaga.
     * Mỗi lớp là một thành phần Java trong cùng ứng dụng Booking, không phải service chạy riêng.
     *
     * @param id mã đơn booking_id đang được thao tác
     * @param actor mã tài khoản gửi yêu cầu, dùng để kiểm tra quyền với đơn
     * @param key khóa chống xử lý trùng của yêu cầu, giữ nguyên khi gửi lại cùng nội dung
     * @param kind PAY khi thanh toán hoặc CANCEL khi khách yêu cầu hủy
     * @param detail phương thức thanh toán nếu PAY, hoặc lý do hủy nếu CANCEL
     */
    public void start(UUID id, long actor, String key, String kind, String detail) {
        // 1. Kiểm tra quyền/trạng thái và yêu cầu trùng, rồi lưu thao tác vào DB Booking.
        // Với CANCEL_PAID: lưu status=CANCELLING, operation_id, saga_step=REFUND_PAYMENT
        // và saga_status=RUNNING trong cùng transaction; commit trước khi gửi công việc đi.
        // enqueue chỉ làm việc với DB Booking, chưa gọi Payment hoàn tiền hay Seat trả ghế.
        Map<String, Object> accepted = store.enqueue(id, actor, key, kind, detail);

        // 2. "run" là cờ trong kết quả tiếp nhận, không phải lời gọi hàm run() phía dưới.
        // true: yêu cầu mới cần được thực hiện.
        // false: yêu cầu đã được ghi nhận với cùng khóa, hoặc đơn đã hủy; không khởi chạy thêm.
        if (Boolean.TRUE.equals(accepted.get("run"))) {
            // 3. Đọc loại thao tác đã được phân loại và lưu trong DB, không suy đoán từ kind.
            // kind=CANCEL có thể là CANCEL_PAID (đã mua vé) hoặc CANCEL_UNPAID (chưa thanh toán).
            if ("CANCEL_PAID".equals(Data.text(store.saga(id), "operationType"))) {
                // Demo hủy vé đã thanh toán đi vào đây.
                // submit đưa việc vào executor; worker đọc bước trong DB để hoàn tiền hoặc trả ghế.
                // Nếu chưa gửi được vào hàng đợi, việc trong DB vẫn còn để scheduler nhận lại.
                // Scheduler cũng có thể đã nhận việc ngay sau enqueue commit; submit có chặn đưa trùng.
                // Khi debug, đặt breakpoint tiếp ở PaidCancellationSaga.submit()/executeStep();
                // mã xử lý bước chạy trên worker, không nằm tiếp trong luồng gọi start() này.
                cancellations.submit(id);

                // Chỉ kết thúc việc tiếp nhận ở start(), không có nghĩa đơn đã CANCELLED.
                // Không chạy tiếp run(id) vì CANCEL_PAID đã có luồng điều phối riêng ở trên.
                return;
            }

            // 4. PAY và CANCEL_UNPAID dùng luồng run(id) bên dưới.
            // Hai nhánh này chưa có cơ chế tự nhận lại công việc như CANCEL_PAID trong demo.
            try {
                // Lambda là công việc giao cho worker: worker sẽ gọi run(id).
                // Luồng tiếp nhận chỉ gửi việc, không đợi run(id) chạy xong.
                executor.execute(() -> run(id));
            } catch (RuntimeException e) {
                // Bắt lỗi khi gửi việc vào executor, ví dụ hàng đợi không nhận thêm việc.
                // Đây không phải chỗ bắt lỗi REST Payment/Seat; lỗi đó được xử lý trong run(id).
                // Ghi FAILED_NEEDS_REVIEW để ADMIN kiểm tra thay vì báo nghiệp vụ đã hoàn tất.
                store.uncertain(id, "WORKER_QUEUE_FULL");
            }
        }
    }

    /** Dựng lệnh Seat từ mã đơn, chủ sở hữu và revision phiên đã được Booking lưu. */
    private HoldCommandRequest command(Map<String, Object> booking) {
        HoldCommandRequest result = new HoldCommandRequest();
        result.bookingId = UUID.fromString(Data.text(booking, "bookingId"));
        result.accountId = Data.number(booking, "accountId");
        result.revision = Data.number(booking, "holdRevision");
        return result;
    }

    /** Lấy mã các ADMIN đang hoạt động từ Account để gửi thông báo nghiệp vụ. */
    private List<Long> admins() {
        List<Long> ids = new ArrayList<Long>();
        for (Map<String, Object> admin : accounts.admins())
            ids.add(Data.number(admin, "accountId"));
        return ids;
    }

    /**
     * Điều phối từng bước Seat và Payment qua REST.
     * Thanh toán FAILED phải trả ghế trước khi chốt CANCELLED; hủy vé đã thu tiền phải hoàn tiền
     * rồi trả ghế (nhánh CANCEL_PAID do PaidCancellationSaga phục hồi theo từng bước).
     * run() không mở transaction phân tán: các lần store.* và request Seat/Payment commit riêng.
     * Khi Payment FAILED, release(PAYMENT_FAILED) là transaction bù trừ ở Seat; không gọi refund
     * vì chưa thu tiền. Lỗi kỹ thuật đi vào uncertain(), chưa có tự refund mọi lỗi sau khi charge.
     */
    private void run(UUID id) {
        try {
            Map<String, Object> booking = store.raw(id), saga = store.saga(id);
            String kind = Data.text(saga, "operationType"),
                    hold = Data.text(booking, "holdId"),
                    operation = Data.text(saga, "operationId");
            List<Long> admins = admins();
            HoldCommandRequest command = command(booking);
            if ("PAY".equals(kind)) {
                store.step(id, "BEGIN_PAYMENT", "Seat", "COMMAND");
                try {
                    seats.beginPayment(UUID.fromString(hold), command);
                } catch (ApiException e) {
                    if (e.status != 409 && e.status != 410) throw e;
                    Map<String, Object> current =
                            seats.hold(UUID.fromString(hold), command.accountId);
                    if ("EXPIRED".equals(Data.text(current, "status"))) {
                        store.finish(id, "EXPIRED", "Het han giu ghe truoc thanh toan", admins);
                        return;
                    }
                    throw e;
                }
                store.step(id, "BEGIN_PAYMENT", "Seat", "ACCEPTED");
                store.step(id, "CHARGE_PAYMENT", "Payment", "COMMAND");
                ChargeRequest charge = new ChargeRequest();
                charge.bookingId = id;
                charge.amount = Data.money(booking, "amount");
                charge.paymentMethod = Data.text(booking, "paymentMethod");
                charge.operationId = "PAY-" + operation;
                Map<String, Object> result = payments.charge(charge);
                String status = Data.text(result, "status");
                store.paid(id, Data.text(result, "paymentId"));
                store.step(id, "CHARGE_PAYMENT", "Payment", status);
                if ("SUCCEEDED".equals(status)) {
                    store.step(id, "CONFIRM_SEAT", "Seat", "COMMAND");
                    seats.confirm(UUID.fromString(hold), command);
                    store.step(id, "CONFIRM_SEAT", "Seat", "SOLD");
                    store.finish(id, "CONFIRMED", null, admins);
                } else if ("FAILED".equals(status)) {
                    store.compensating(id);
                    command.reason = "PAYMENT_FAILED";
                    store.step(id, "RELEASE_SEAT", "Seat", "COMMAND");
                    seats.release(UUID.fromString(hold), command);
                    store.step(id, "RELEASE_SEAT", "Seat", "RELEASED");
                    store.finish(id, "CANCELLED", "Thanh toan gia lap bi tu choi", admins);
                } else throw new IllegalStateException("Unexpected charge result");
            } else {
                if (!"CANCEL_UNPAID".equals(kind)) throw new IllegalStateException("Unexpected operation");
                command.reason = kind;
                store.step(id, "RELEASE_SEAT", "Seat", "COMMAND");
                seats.release(UUID.fromString(hold), command);
                store.step(id, "RELEASE_SEAT", "Seat", "RELEASED");
                store.finish(id, "CANCELLED", Data.text(booking, "failureReason"), admins);
            }
        } catch (Exception e) {
            String code = e instanceof ApiException ? ((ApiException) e).code : "TECHNICAL_ERROR";
            store.uncertain(id, code);
            LOG.warn("Saga {} needs review: {}", id, code);
        }
    }

    /**
     * Định kỳ đối chiếu các đơn chờ quá hạn với trạng thái Seat; không tự sửa phiên đang
     * PAYMENT_PROCESSING.
     */
    @Scheduled(fixedDelay = 2000L)
    public void expire() {
        for (Map<String, Object> row : store.expiring())
            try {
                Map<String, Object> hold =
                        seats.hold(
                                UUID.fromString(Data.text(row, "holdId")),
                                Data.number(row, "accountId"));
                if ("EXPIRED".equals(Data.text(hold, "status")))
                    store.expire(UUID.fromString(Data.text(row, "bookingId")), admins());
            } catch (Exception e) {
                LOG.debug("Waiting for Seat to resolve hold expiration");
            }
    }
}
