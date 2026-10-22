package com.td.cinema.booking.service.saga;

import com.td.cinema.booking.dto.request.BookingCreateRequest;
import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.model.PriceQuote;
import com.td.cinema.booking.repository.BookingRepository;
import com.td.cinema.booking.repository.SagaRepository;
import com.td.cinema.booking.service.NotificationService;
import com.td.cinema.booking.service.PricingService;
import com.td.cinema.booking.util.Data;
import com.td.cinema.booking.util.BookingFlowLog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.*;

import tools.jackson.databind.ObjectMapper;

import java.math.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * Lưu dữ liệu và tiến trình xử lý trong transaction cục bộ của Booking, không mở transaction chung
 * cho các service.
 */
@Service
public class BookingTransactionService {
    private final BookingRepository bookingRepository;

    private final SagaRepository sagaRepository;

    private final TransactionTemplate transactions;
    private final PricingService pricing;
    private final ObjectMapper json;
    private final NotificationService notifications;

    /** Nhận các thành phần lưu đơn, tiến trình và thông báo để thực hiện transaction cục bộ của Booking. */
    public BookingTransactionService(
            BookingRepository bookingRepository,
            SagaRepository sagaRepository,
            PlatformTransactionManager manager,
            PricingService pricing,
            ObjectMapper json,
            NotificationService notifications) {
        this.bookingRepository = bookingRepository;

        this.sagaRepository = sagaRepository;
        this.transactions = new TransactionTemplate(manager);
        this.pricing = pricing;
        this.json = json;
        this.notifications = notifications;
    }

    /**
     * Mở hoặc tham gia Local Transaction trong database Booking bằng TransactionTemplate.
     * Propagation REQUIRED mặc định dùng chung transaction cục bộ nếu bên gọi đã mở trước đó;
     * nó không gộp transaction của Seat hoặc Payment vào cùng transaction Booking.
     */
    private <T> T local(Supplier<T> work) {
        return transactions.execute(status -> work.get());
    }

    /** Đọc bản ghi đơn dùng nội bộ, chưa lọc các trường kỹ thuật hoặc kiểm tra quyền của người dùng. */
    public Map<String, Object> raw(UUID id) {
        return bookingRepository.findBooking(id.toString());
    }

    /** Đọc và khóa dòng đơn để các cập nhật đồng thời không ghi đè trạng thái của nhau. */
    private Map<String, Object> locked(UUID id) {
        return bookingRepository.lockBooking(id.toString());
    }

    /** Kiểm tra tài khoản sở hữu đơn; dùng lỗi 404 nếu không khớp để không tiết lộ dữ liệu khách khác. */
    private void owns(Map<String, Object> row, long actor) {
        if (row.get("accountId") == null || Data.number(row, "accountId") != actor)
            throw new ApiException(404, "NOT_FOUND", "Khong tim thay don.");
    }

    /**
     * Tìm request đã xử lý và kiểm tra cùng tài khoản, loại thao tác, dấu vân tay nội dung trước
     * khi dùng lại kết quả.
     */
    private Map<String, Object> operation(String key, long actor, String kind, String fingerprint) {
        Map<String, Object> row = bookingRepository.findOperation(key);
        if (row != null
                && (Data.number(row, "accountId") != actor
                        || !kind.equals(Data.text(row, "operationType"))
                        || !fingerprint.equals(Data.text(row, "payloadFingerprint"))))
            throw ApiException.conflict("Idempotency-Key da duoc dung voi noi dung khac.");
        return row;
    }

    /** Lưu khóa chống xử lý trùng cùng loại thao tác, chủ sở hữu và dấu vân tay request. */
    private void remember(String key, long actor, String kind, String fingerprint, UUID id) {
        bookingRepository.insertOperation(key, kind, fingerprint, actor, id.toString());
    }

    /** Kiểm tra quyền xem, bỏ các trường nội bộ nhạy cảm và bổ sung ghế/trạng thái xử lý cho dữ liệu đơn. */
    public Map<String, Object> view(UUID id, long actor, boolean admin) {
        Map<String, Object> row = raw(id);
        if (!admin) owns(row, actor);
        row.remove("requestHash");
        row.remove("idempotencyKey");
        row.remove("mockPaymentOutcome");
        row.put("items", bookingRepository.findItems(id.toString()));
        row.put("saga", sagaRepository.findPublicState(id.toString()));
        return row;
    }

    /** Lấy lịch sử đơn của một tài khoản từ schema Booking. */
    public List<Map<String, Object>> mine(long actor) {
        return bookingRepository.findByAccount(actor);
    }

    /** Lấy danh sách đơn gần đây cho trang quản trị. */
    public List<Map<String, Object>> adminList() {
        return bookingRepository.findRecentBookings();
    }

    /**
     * Kiểm tra request trùng và phiên giữ ghế, tính giá phía server rồi lưu đơn CREATING cùng ảnh
     * chụp thông tin ghế.
     */
    @SuppressWarnings("unchecked")
    public UUID beginCreate(
            long actor,
            String key,
            BookingCreateRequest request,
            Map<String, Object> profile,
            Map<String, Object> hold) {
        final String requestKey = Data.key(key);
        final String fingerprint =
                Data.hash(
                        request.holdId
                                + "|"
                                + Data.hash(request.token)
                                + "|"
                                + request.revision
                                + "|"
                                + request.contactName
                                + "|"
                                + request.contactEmail
                                + "|"
                                + request.contactPhone
                                + "|"
                                + request.note);
        return local(
                () -> {
                    bookingRepository.lockOperation("booking-key:" + requestKey);
                    Map<String, Object> prior = operation(requestKey, actor, "CREATE", fingerprint);
                    if (prior != null) return UUID.fromString(Data.text(prior, "bookingId"));
                    if (!"ACTIVE".equals(Data.text(hold, "status"))
                            || Data.number(hold, "revision") != request.revision
                            || !request.token.equals(Data.text(hold, "token")))
                        throw ApiException.conflict("Phien giu cho da thay doi hoac het han.");
                    List<Map<String, Object>> seats = (List<Map<String, Object>>) hold.get("seats");
                    if (seats == null || seats.isEmpty() || seats.size() > 6)
                        throw ApiException.conflict("Phien can tu 1 den 6 ghe.");
                    Map<String, Object> show = (Map<String, Object>) hold.get("showtime");
                    PriceQuote quote = pricing.quote(Data.text(profile, "customerType"), seats);
                    String group = quote.customerType;
                    int percent = quote.discountPercent;
                    BigDecimal subtotal = quote.subtotal, total = quote.total;
                    UUID id = UUID.randomUUID();
                    bookingRepository.insertBooking(
                            id.toString(),
                            requestKey,
                            fingerprint,
                            actor,
                            profile.get("customerId"),
                            Data.text(hold, "showtimeId"),
                            request.holdId.toString(),
                            request.revision,
                            total,
                            subtotal,
                            percent,
                            group,
                            Data.text(show, "title"),
                            OffsetDateTime.parse(Data.text(show, "startsAt")),
                            OffsetDateTime.parse(Data.text(hold, "expiresAt")),
                            request.contactName.trim(),
                            request.contactEmail.trim().toLowerCase(Locale.ROOT),
                            request.contactPhone == null ? "" : request.contactPhone.trim(),
                            request.note);
                    for (Map<String, Object> seat : seats)
                        bookingRepository.insertItem(
                                id.toString(),
                                Data.number(seat, "seatId"),
                                Data.text(seat, "seatNo"),
                                Data.text(seat, "seatType"),
                                Data.money(seat, "basePrice"),
                                Data.money(seat, "vipSurcharge"),
                                Data.money(seat, "basePrice")
                                        .add(Data.money(seat, "vipSurcharge")));
                    remember(requestKey, actor, "CREATE", fingerprint, id);
                    timeline(id, "Booking", "CREATE_BOOKING", "CREATING", null);
                    BookingFlowLog.committed(id, "Booking", "CREATE_BOOKING", "CREATING");
                    return id;
                });
    }

    /**
     * Ghi revision do Seat xác nhận và chuyển đơn CREATING sang PENDING sau khi gắn phiên giữ ghế
     * thành công.
     */
    public void checkout(UUID id, long revision) {
        local(
                () -> {
                    Map<String, Object> row = locked(id);
                    if ("CREATING".equals(Data.text(row, "status"))) {
                        bookingRepository.markPending(revision, id.toString());
                        timeline(id, "Seat", "CHECKOUT", "PENDING", null);
                        BookingFlowLog.committed(id, "Seat", "CHECKOUT", "PENDING");
                    }
                    return null;
                });
    }

    /**
     * Tiếp nhận thao tác trong một transaction cục bộ Booking: khóa đơn, kiểm tra quyền,
     * chống xử lý trùng, lưu trạng thái đơn, bước Saga, khóa yêu cầu và timeline ACCEPTED.
     * Không gọi HTTP tới Payment/Seat trong transaction này.
     * Trả run=true khi có công việc mới; run=false khi không cần khởi chạy thêm lượt xử lý.
     */
    public Map<String, Object> enqueue(
            UUID id, long actor, String key, String kind, String detail) {
        final String requestKey = Data.key(key),
                fingerprint = Data.hash(id + "|" + kind + "|" + detail);
        return local(
                () -> {
                    bookingRepository.lockOperation("booking-key:" + requestKey);
                    Map<String, Object> row = locked(id);
                    owns(row, actor);
                    Map<String, Object> prior = operation(requestKey, actor, kind, fingerprint);
                    if (prior != null) return Data.map("run", false, "booking", row);
                    String status = Data.text(row, "status");
                    if ("PAY".equals(kind)) {
                        Data.paymentMethod(detail);
                        if (!"PENDING".equals(status))
                            throw ApiException.conflict("Don khong o trang thai cho thanh toan.");
                    } else {
                        if ("CANCELLED".equals(status)) {
                            remember(requestKey, actor, kind, fingerprint, id);
                            return Data.map("run", false, "booking", row);
                        }
                        if (!Arrays.asList("PENDING", "CONFIRMED").contains(status))
                            throw ApiException.conflict("Don dang xu ly hoac khong the huy.");
                        if (!OffsetDateTime.parse(Data.text(row, "startsAt"))
                                .isAfter(bookingRepository.currentTime()))
                            throw ApiException.conflict("Da qua thoi diem duoc huy ve.");
                    }
                    // Chỉ tạo mã thao tác mới sau khi đã kiểm tra yêu cầu trùng.
                    // Các lượt worker thử lại sẽ dùng mã này, không tạo một lần hủy mới.
                    UUID operationId = UUID.randomUUID();
                    // kind từ API chỉ là PAY hoặc CANCEL; loại hủy phụ thuộc trạng thái đơn gốc:
                    // CONFIRMED -> CANCEL_PAID, PENDING -> CANCEL_UNPAID.
                    String operationType =
                            "PAY".equals(kind)
                                    ? "PAY"
                                    : ("CONFIRMED".equals(status)
                                            ? "CANCEL_PAID"
                                            : "CANCEL_UNPAID");
                    bookingRepository.beginOperation(
                            "PAY".equals(kind) ? "PAYMENT_PROCESSING" : "CANCELLING",
                            operationId.toString(),
                            "PAY".equals(kind) ? detail : null,
                            "PAY".equals(kind) ? null : detail,
                            id.toString());
                    // Lưu điểm bắt đầu để worker biết phải làm bước nào khi đọc lại từ DB:
                    // PAY -> BEGIN_PAYMENT; CANCEL_PAID -> REFUND_PAYMENT;
                    // CANCEL_UNPAID -> RELEASE_SEAT vì chưa có tiền cần hoàn.
                    sagaRepository.beginSaga(
                            id.toString(),
                            "PAY".equals(kind)
                                    ? "BEGIN_PAYMENT"
                                    : ("CANCEL_PAID".equals(operationType)
                                            ? "REFUND_PAYMENT"
                                            : "RELEASE_SEAT"),
                            operationType,
                            operationId.toString());
                    remember(requestKey, actor, kind, fingerprint, id);
                    timeline(id, "Booking", operationType, "ACCEPTED", null);
                    BookingFlowLog.committed(id, "Booking", operationType, "ACCEPTED");
                    return Data.map("run", true, "booking", raw(id));
                });
    }

    /** Đọc trạng thái Saga đang lưu tại Booking để bộ điều phối biết thao tác và bước hiện hành. */
    public Map<String, Object> saga(UUID id) {
        return sagaRepository.findState(id.toString());
    }

    /** Tìm các đơn hủy đã thanh toán đến hạn xử lý; không quét PAY hoặc hủy đơn chưa thanh toán. */
    public List<String> duePaidCancellations(int batchSize) {
        return sagaRepository.findDuePaidCancellations(batchSize);
    }

    /**
     * Lưu và commit quyền xử lý có thời hạn (lease) trước khi gọi HTTP.
     * Mã token ngăn lượt xử lý cũ ghi đè tiến độ khi một lượt khác đã nhận quyền mới.
     * Không giữ khóa DB hoặc transaction Booking mở trong lúc chờ service khác.
     */
    public Map<String, Object> claimPaidCancellation(UUID id, UUID token, long leaseMs, int maxAttempts) {
        return local(() -> {
            if (sagaRepository.claimPaidCancellation(id.toString(), token.toString(), leaseMs, maxAttempts) == 0) {
                if (sagaRepository.exhaustPaidCancellation(id.toString(), maxAttempts) > 0) {
                    timeline(id, "Booking", "TECHNICAL_ERROR", "NEEDS_REVIEW", "RETRY_EXHAUSTED");
                    BookingFlowLog.committed(id, "Booking", "TECHNICAL_ERROR", "NEEDS_REVIEW");
                }
                return null;
            }
            Map<String, Object> state = saga(id);
            String step = Data.text(state, "currentStep");
            String service = "REFUND_PAYMENT".equals(step) ? "Payment" : "Seat";
            timeline(id, service, step, "COMMAND", "attempt=" + Data.number(state, "attemptCount"));
            BookingFlowLog.committed(id, service, step, "COMMAND");
            return Data.map("booking", raw(id), "saga", state);
        });
    }

    /**
     * Ghi nhận đã hoàn tiền và chuyển sang bước trả ghế trong cùng transaction Booking.
     * Những lượt sau đọc bước RELEASE_SEAT nên không thực hiện hoàn tiền lại.
     */
    public boolean cancellationRefunded(UUID id, UUID token) {
        return local(() -> {
            if (sagaRepository.completeCancellationRefund(id.toString(), token.toString()) == 0) return false;
            timeline(id, "Payment", "REFUND_PAYMENT", "REFUNDED", null);
            BookingFlowLog.committed(id, "Payment", "REFUND_PAYMENT", "REFUNDED");
            return true;
        });
    }

    /**
     * Chỉ nhận kết quả của lượt còn đúng token; lưu xác nhận trả ghế,
     * trạng thái hủy hoàn tất và thông báo trong cùng transaction Booking.
     */
    public boolean cancellationReleased(UUID id, UUID token, List<Long> admins) {
        return local(() -> {
            Map<String, Object> row = locked(id), state = saga(id);
            if (!"CANCELLING".equals(Data.text(row, "status"))
                    || !"CANCEL_PAID".equals(Data.text(state, "operationType"))
                    || !"RELEASE_SEAT".equals(Data.text(state, "currentStep"))
                    || !token.toString().equals(Data.text(state, "leaseToken"))) return false;
            timeline(id, "Seat", "RELEASE_SEAT", "RELEASED", null);
            BookingFlowLog.committed(id, "Seat", "RELEASE_SEAT", "RELEASED");
            finish(id, "CANCELLED", Data.text(row, "failureReason"), admins);
            return true;
        });
    }

    /**
     * Lỗi tạm thời và còn lượt thử: lưu lịch thử lại vào DB, xóa lease của lượt vừa chạy.
     * Lỗi không được thử lại hoặc đã hết lượt: chuyển sang FAILED_NEEDS_REVIEW để ADMIN kiểm tra.
     */
    public boolean cancellationAttemptFailed(UUID id, UUID token, String code,
            boolean transientFailure, long delayMs, int maxAttempts) {
        return local(() -> {
            locked(id);
            Map<String, Object> state = saga(id);
            if (!token.toString().equals(Data.text(state, "leaseToken"))) return false;
            long attempts = Data.number(state, "attemptCount");
            boolean retry = transientFailure && attempts < maxAttempts;
            String error = transientFailure && !retry ? "RETRY_EXHAUSTED:" + code : code;
            if (sagaRepository.failCancellationAttempt(id.toString(), token.toString(),
                    retry ? "WAITING_RETRY" : "FAILED_NEEDS_REVIEW", error, retry, delayMs) == 0) return false;
            String step = Data.text(state, "currentStep");
            String result = retry ? "RETRY_SCHEDULED" : "NEEDS_REVIEW";
            timeline(id, "Booking", step, result, "attempt=" + attempts + "; " + error);
            BookingFlowLog.committed(id, "Booking", step, result);
            return true;
        });
    }

    /** Lưu bước xử lý và kết quả quan sát; log server được ghi sau commit của bản ghi nhật ký. */
    public void step(UUID id, String step, String service, String result) {
        // Giữ dòng store.step(...) của Orchestrator trước khi đi vào callback TransactionTemplate.
        Runnable log = BookingFlowLog.deferredFromCaller(BookingTransactionService.class, id, service, step, result);
        local(
                () -> {
                    sagaRepository.updateStep(step, id.toString());
                    timeline(id, service, step, result, null);
                    log.run();
                    return null;
                });
    }

    /** Gắn paymentId vào đơn để đối chiếu Payment; có paymentId chưa có nghĩa đã thu tiền thành công. */
    public void paid(UUID id, String paymentId) {
        local(
                () -> {
                    bookingRepository.attachPayment(paymentId, id.toString());
                    return null;
                });
    }

    /** Đánh dấu đang bù trừ sau thanh toán thất bại; chưa chốt đơn hủy khi Seat chưa xác nhận trả ghế. */
    public void compensating(UUID id) {
        local(
                () -> {
                    bookingRepository.markCompensating(id.toString());
                    sagaRepository.markCompensating(id.toString());
                    return null;
                });
    }

    /**
     * Chốt trạng thái cuối, cấp mã vé khi CONFIRMED và lưu thông báo cho khách/ADMIN trong
     * transaction Booking.
     */
    public void finish(UUID id, String status, String reason, List<Long> admins) {
        local(
                () -> {
                    Map<String, Object> row = locked(id);
                    if (Arrays.asList("CANCELLED", "EXPIRED").contains(Data.text(row, "status")))
                        return null;
                    String ticket =
                            "CONFIRMED".equals(status)
                                    ? "TICKET-" + id.toString().toUpperCase(Locale.ROOT)
                                    : Data.text(row, "ticketCode");
                    bookingRepository.finishBooking(status, ticket, reason, id.toString());
                    sagaRepository.markCompleted(id.toString());
                    timeline(id, "Booking", "FINISH", status, reason);
                    BookingFlowLog.committed(id, "Booking", "FINISH", status);
                    notifications.publishBooking(row, status, admins);
                    return null;
                });
    }

    /**
     * Ghi nhận lỗi kỹ thuật cần kiểm tra, giữ nguyên kết quả chưa xác định thay vì tự coi thanh
     * toán đã thất bại.
     */
    public void uncertain(UUID id, String code) {
        local(
                () -> {
                    sagaRepository.markUncertain(code, id.toString());
                    timeline(id, "Booking", "TECHNICAL_ERROR", "NEEDS_REVIEW", code);
                    BookingFlowLog.committed(id, "Booking", "TECHNICAL_ERROR", "NEEDS_REVIEW");
                    return null;
                });
    }

    /** Chỉ đánh dấu hết hạn cho đơn chưa bắt đầu xử lý thanh toán và lưu thông báo tương ứng. */
    public void expire(UUID id, List<Long> admins) {
        local(
                () -> {
                    Map<String, Object> row = locked(id);
                    if (Arrays.asList("PENDING", "CREATING").contains(Data.text(row, "status"))) {
                        bookingRepository.markExpired(id.toString());
                        timeline(id, "Booking", "HOLD_EXPIRED", "EXPIRED", null);
                        BookingFlowLog.committed(id, "Booking", "HOLD_EXPIRED", "EXPIRED");
                        notifications.publishBooking(row, "EXPIRED", admins);
                    }
                    return null;
                });
    }

    /** Lưu bước vào database; hàm nghiệp vụ gọi sẽ đăng ký log sau commit tại vị trí của chính nó. */
    private void timeline(UUID id, String service, String action, String result, String message) {
        sagaRepository.appendTimeline(
                id.toString(),
                service,
                action,
                result,
                json.writeValueAsString(Data.map("message", message)));
    }

    /** Kiểm tra quyền xem đơn rồi lấy các bước trong nhật ký theo thứ tự thời gian. */
    public List<Map<String, Object>> timeline(UUID id, long actor, boolean admin) {
        Map<String, Object> row = raw(id);
        if (!admin) owns(row, actor);
        return sagaRepository.findTimeline(id.toString());
    }

    /** Lấy các đơn chờ đã quá hạn để luồng nền đối chiếu với kết quả hết hạn ở Seat. */
    public List<Map<String, Object>> expiring() {
        return bookingRepository.findExpiringBookings();
    }
}
