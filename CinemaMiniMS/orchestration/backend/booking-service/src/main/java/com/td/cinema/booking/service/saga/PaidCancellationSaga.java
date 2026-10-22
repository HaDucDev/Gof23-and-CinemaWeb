package com.td.cinema.booking.service.saga;

import com.td.cinema.booking.client.AccountClient;
import com.td.cinema.booking.client.PaymentClient;
import com.td.cinema.booking.client.SeatClient;
import com.td.cinema.booking.config.DatabaseSchemaValidator;
import com.td.cinema.booking.config.SagaRecoveryProperties;
import com.td.cinema.booking.dto.request.HoldCommandRequest;
import com.td.cinema.booking.dto.request.RefundRequest;
import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.util.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Điều phối hủy vé đã thanh toán (CANCEL_PAID): hoàn tiền -> trả ghế -> hoàn tất.
 * Mỗi lượt đọc bước đang lưu trong DB và chỉ thực hiện bước đó.
 * Các lời gọi HTTP đến Payment/Seat nằm ngoài transaction cục bộ của Booking.
 * Quyền xử lý có thời hạn (lease) được lưu trong DB; mã thao tác giữ nguyên khi gửi lại
 * giúp tiếp tục công việc sau khi Booking khởi động lại hoặc bị mất phản hồi.
 */
@Service
public class PaidCancellationSaga {
    private static final Logger LOG = LoggerFactory.getLogger(PaidCancellationSaga.class);
    private final BookingTransactionService store;
    private final PaymentClient payments;
    private final SeatClient seats;
    private final AccountClient accounts;
    private final ThreadPoolTaskExecutor executor;
    private final SagaRecoveryProperties settings;
    private final DatabaseSchemaValidator schema;
    // Chặn đưa trùng một đơn vào hàng đợi trong cùng tiến trình Booking.
    // Quyền thực hiện bước giữa các tiến trình được kiểm soát riêng bằng lease trong DB.
    private final Set<UUID> queued = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    public PaidCancellationSaga(BookingTransactionService store, PaymentClient payments, SeatClient seats,
            AccountClient accounts, @Qualifier("sagaExecutor") ThreadPoolTaskExecutor executor,
            SagaRecoveryProperties settings, DatabaseSchemaValidator schema) {
        this.store = store;
        this.payments = payments;
        this.seats = seats;
        this.accounts = accounts;
        this.executor = executor;
        this.settings = settings;
        this.schema = schema;
    }

    /**
     * Đưa đơn vào hàng đợi để luồng xử lý nền (worker) thực hiện một bước.
     * Đưa đơn vào hàng đợi để luồng xử lý nền (worker) thực hiện một bước.
     * Nếu hàng đợi không nhận thêm việc, công việc trong DB vẫn còn;
     * lần quét sau sẽ thử đưa vào hàng đợi lại.
     */
    public void submit(UUID id) {
        // Đơn đã nằm trong hàng đợi hoặc đang được xử lý ở tiến trình này thì không thêm lần nữa.
        if (!queued.add(id)) return;
        try {
            // Lambda này chạy trên worker do executor cấp, không phải luồng HTTP gửi yêu cầu hủy.
            executor.execute(() -> {
                try {
                    // Điểm đặt breakpoint để theo dõi bước nghiệp vụ tiếp theo sau start()/submit().
                    executeStep(id);
                } finally {
                    // Kết thúc lượt này; cho phép lần quét sau đưa đơn vào lại nếu còn việc.
                    queued.remove(id);
                }
            });
        } catch (RuntimeException error) {
            queued.remove(id);
            LOG.warn("SAGA recovery queue busy; booking={} remains pending", id);
        }
    }

    /**
     * Định kỳ tìm các đơn hủy có công việc đến hạn, không cần khách gửi yêu cầu mới.
     * Nếu Booking dừng giữa lượt, công việc có thể được nhận lại khi lease cũ hết hạn.
     */
    @Scheduled(fixedDelayString = "${cinema.saga.recovery.scan-delay-ms:2000}")
    public void recover() {
        if (!schema.ready()) return;
        try {
            for (String id : store.duePaidCancellations(settings.getBatchSize()))
                submit(UUID.fromString(id));
        } catch (RuntimeException error) {
            LOG.warn("SAGA recovery scan failed; the next scan will retry", error);
        }
    }

    /**
     * Nhận quyền xử lý rồi chạy đúng một bước REFUND_PAYMENT hoặc RELEASE_SEAT.
     * Kết quả của bước này được lưu vào DB để quyết định bước tiếp theo;
     * scheduler chỉ kích hoạt các lượt xử lý, không quyết định thứ tự nghiệp vụ.
     */
    @SuppressWarnings("unchecked")
    private void executeStep(UUID id) {
        // Mỗi lượt có mã riêng để lượt cũ không ghi đè tiến độ sau khi lượt khác đã nhận việc.
        UUID token = UUID.randomUUID();
        Map<String, Object> claim;
        try {
            // Nhận lease và tăng số lần thử trong transaction ngắn; commit trước khi gọi HTTP.
            claim = store.claimPaidCancellation(id, token, settings.getLeaseMs(), settings.getMaxAttempts());
        } catch (RuntimeException error) {
            LOG.warn("SAGA could not claim booking={}; work remains in database", id, error);
            return;
        }
        // Không nhận được quyền xử lý thì kết thúc lượt này, không gọi Payment/Seat.
        if (claim == null) return;
        Map<String, Object> booking = (Map<String, Object>) claim.get("booking");
        Map<String, Object> saga = (Map<String, Object>) claim.get("saga");
        String step = Data.text(saga, "currentStep");
        LOG.info("SAGA recovery booking={} operation={} step={} attempt={}", id,
                Data.text(saga, "operationId"), step, Data.number(saga, "attemptCount"));
        try {
            if ("REFUND_PAYMENT".equals(step)) {
                RefundRequest request = new RefundRequest();
                request.bookingId = id;
                // Giữ mã thao tác đã lưu khi thử lại sau timeout hoặc khởi động lại Booking.
                // Payment dùng mã này để nhận ra yêu cầu cũ và không ghi thêm khoản hoàn trùng.
                request.operationId = "REFUND-" + Data.text(saga, "operationId");
                Map<String, Object> result = payments.refund(
                        UUID.fromString(Data.text(booking, "paymentId")), request);
                if (!"REFUNDED".equals(Data.text(result, "status")))
                    throw new IllegalStateException("Refund not confirmed");
                // Đã xác nhận hoàn tiền: lưu bước RELEASE_SEAT và đặt lại số lần thử cho bước mới.
                store.cancellationRefunded(id, token);
            } else if ("RELEASE_SEAT".equals(step)) {
                // Đã ghi nhận hoàn tiền trước đó; lượt này chỉ trả ghế, không gọi hoàn tiền lại.
                HoldCommandRequest command = new HoldCommandRequest();
                command.bookingId = id;
                command.accountId = Data.number(booking, "accountId");
                command.revision = Data.number(booking, "holdRevision");
                command.reason = "CANCEL_PAID";
                Map<String, Object> result = seats.release(UUID.fromString(Data.text(booking, "holdId")), command);
                if (!"RELEASED".equals(Data.text(result, "status")))
                    throw new IllegalStateException("Seat release not confirmed");
                List<Long> admins = new ArrayList<Long>();
                for (Map<String, Object> admin : accounts.admins())
                    admins.add(Data.number(admin, "accountId"));
                // Lưu kết quả trả ghế, chốt CANCELLED và tạo thông báo trong cùng transaction Booking.
                store.cancellationReleased(id, token, admins);
            } else {
                throw new IllegalStateException("Unexpected cancellation step");
            }
        } catch (Exception error) {
            // Cho phép thử lại lỗi SERVICE_UNAVAILABLE, HTTP 408 hoặc 429.
            // Các lỗi còn lại được ghi nhận để ADMIN kiểm tra.
            boolean retry = error instanceof ApiException
                    && ("SERVICE_UNAVAILABLE".equals(((ApiException) error).code)
                        || ((ApiException) error).status == 408 || ((ApiException) error).status == 429);
            String code = error instanceof ApiException ? ((ApiException) error).code : "TECHNICAL_ERROR";
            try {
                // Lỗi được thử lại và còn lượt: hẹn lần tiếp theo, xóa lease.
                // Các trường hợp còn lại chuyển sang trạng thái cần ADMIN kiểm tra.
                store.cancellationAttemptFailed(id, token, code, retry,
                        settings.getRetryDelayMs(), settings.getMaxAttempts());
            } catch (RuntimeException persistenceError) {
                // Nếu không lưu được lỗi, lease còn trong DB sẽ hết hạn để công việc được nhận lại.
                // Lượt sau vẫn dùng mã thao tác cũ để tránh xử lý trùng.
                LOG.error("SAGA cannot save failure booking={}; recovery waits for lease expiry", id, persistenceError);
            }
            LOG.warn("SAGA booking={} step={} failed code={} transient={}", id, step, code, retry);
        }
    }
}
