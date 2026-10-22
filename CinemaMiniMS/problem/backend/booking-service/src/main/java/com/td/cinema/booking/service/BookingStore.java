package com.td.cinema.booking.service;

import com.td.cinema.booking.dto.request.BookingCreateRequest;
import com.td.cinema.booking.exception.ApiException;
import com.td.cinema.booking.model.PriceQuote;
import com.td.cinema.booking.repository.BookingRepository;
import com.td.cinema.booking.repository.OperationLogRepository;
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
 * Lưu đơn và nhật ký bằng transaction cục bộ của Booking. Các lời gọi REST liên service nằm ngoài
 * lớp này.
 */
@Service
public class BookingStore {
    private final BookingRepository bookingRepository;

    private final OperationLogRepository operationLogRepository;

    private final TransactionTemplate transactions;
    private final PricingService pricing;
    private final ObjectMapper json;
    private final NotificationService notifications;

    /** Nhận repository, transaction manager, bộ tính giá và dịch vụ thông báo của Booking. */
    public BookingStore(
            BookingRepository bookingRepository,
            OperationLogRepository operationLogRepository,
            PlatformTransactionManager manager,
            PricingService pricing,
            ObjectMapper json,
            NotificationService notifications) {
        this.bookingRepository = bookingRepository;

        this.operationLogRepository = operationLogRepository;
        this.transactions = new TransactionTemplate(manager);
        this.pricing = pricing;
        this.json = json;
        this.notifications = notifications;
    }

    /** Thực hiện một transaction cục bộ của Booking; không bao trùm các lời gọi HTTP tới service khác. */
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
        row.put("saga", null);
        String reason = row.get("failureReason") == null ? "" : row.get("failureReason").toString();
        row.put("processingIssue", reason.startsWith("TECHNICAL_ERROR:") ? reason : null);
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

    /** Khóa đơn, kiểm tra quyền và trạng thái rồi tiếp nhận PAY hoặc hủy bằng khóa chống xử lý trùng. */
    public Map<String, Object> beginOperation(
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
                    UUID operationId = UUID.randomUUID();
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
                    remember(requestKey, actor, kind, fingerprint, id);
                    timeline(id, "Booking", operationType, "ACCEPTED", null);
                    BookingFlowLog.committed(id, "Booking", operationType, "ACCEPTED");
                    return Data.map("run", true, "booking", raw(id), "operationType", operationType);
                });
    }


    /** Lưu bước xử lý và kết quả quan sát; log server được ghi sau commit của bản ghi nhật ký. */
    public void step(UUID id, String step, String service, String result) {
        // Giữ dòng store.step(...) của luồng gọi trước khi đi vào callback TransactionTemplate.
        Runnable log = BookingFlowLog.deferredFromCaller(BookingStore.class, id, service, step, result);
        local(
                () -> {
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
                    bookingRepository.recordTechnicalIssue("TECHNICAL_ERROR:" + code, id.toString());
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
        operationLogRepository.appendTimeline(
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
        return operationLogRepository.findTimeline(id.toString());
    }

    /** Lấy các đơn chờ đã quá hạn để luồng nền đối chiếu với kết quả hết hạn ở Seat. */
    public List<Map<String, Object>> expiring() {
        return bookingRepository.findExpiringBookings();
    }
}
