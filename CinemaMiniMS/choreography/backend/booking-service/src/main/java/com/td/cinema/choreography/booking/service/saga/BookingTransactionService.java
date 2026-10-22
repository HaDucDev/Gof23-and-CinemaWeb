package com.td.cinema.choreography.booking.service.saga;

import com.td.cinema.choreography.booking.dto.request.BookingCreateRequest;
import com.td.cinema.choreography.booking.exception.ApiException;
import com.td.cinema.choreography.booking.model.PriceQuote;
import com.td.cinema.choreography.booking.repository.BookingRepository;
import com.td.cinema.choreography.booking.repository.ProgressRepository;
import com.td.cinema.choreography.booking.service.NotificationService;
import com.td.cinema.choreography.booking.messaging.*;
import com.td.cinema.choreography.booking.service.PricingService;
import com.td.cinema.choreography.booking.util.Data;
import com.td.cinema.choreography.booking.util.BookingFlowLog;

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
 * Phát yêu cầu sau commit và tổng hợp sự kiện trả về; không giữ vai trò điều phối REST.
 */
@Service
public class BookingTransactionService {
    private final BookingRepository bookingRepository;

    private final ProgressRepository progressRepository;

    private final EventPublisher publisher;
    private final TransactionTemplate transactions;
    private final PricingService pricing;
    private final ObjectMapper json;
    private final NotificationService notifications;

    /**
     * Nhận các thành phần lưu đơn, tiến trình và thông báo để thực hiện transaction cục bộ của
     * Booking.
     * Nhận thêm bộ phát sự kiện để các thay đổi nghiệp vụ chỉ được công bố sau commit.
     */
    public BookingTransactionService(
            BookingRepository bookingRepository,
            ProgressRepository progressRepository,
            PlatformTransactionManager manager,
            PricingService pricing,
            ObjectMapper json,
            NotificationService notifications, EventPublisher publisher) {
        this.bookingRepository = bookingRepository;

        this.progressRepository = progressRepository;
        this.transactions = new TransactionTemplate(manager);
        this.pricing = pricing;
        this.json = json;
        this.notifications = notifications;
        this.publisher = publisher;
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
        row.put("saga", progressRepository.findPublicState(id.toString()));
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
     * Kiểm tra quyền, trạng thái và request trùng rồi lưu yêu cầu Pay/hủy; phát sự kiện sau commit
     * cho các bên tự phản ứng.
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
                    progressRepository.begin(id.toString(), operationType, operationId.toString());
                    remember(requestKey, actor, kind, fingerprint, id);
                    timeline(id, "Booking", operationType, "ACCEPTED", null);
                    BookingFlowLog.committed(id, "Booking", operationType, "ACCEPTED");
                    // Đây mới là đăng ký gửi, chưa gửi TCP ngay: sau commit PAYMENT_PROCESSING/CANCELLING
                    // EventPublisher mới publish PaymentRequested/BookingCancellationRequested tới saga.events.
                    // HTTP chấp nhận yêu cầu không có nghĩa consumer đã thu/hoàn/trả ghế xong.
                    publisher.afterCommit(requestEvent(raw(id), operationType));
                    return Data.map("run", true, "booking", raw(id));
                });
    }

    /**
     * Dựng sự kiện yêu cầu Pay/hủy với bookingId, operationId và eventId xác định để đối chiếu và
     * chống xử lý trùng.
     */
    private SagaEvent requestEvent(Map<String, Object> row, String kind) {
        SagaEvent event = new SagaEvent();
        event.bookingId = UUID.fromString(Data.text(row, "bookingId"));
        event.holdId = UUID.fromString(Data.text(row, "holdId"));
        event.operationId = UUID.fromString(Data.text(row, "operationId"));
        event.correlationId = event.bookingId;
        event.eventId = UUID.nameUUIDFromBytes((event.operationId + "|request").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // Publisher dùng tên này làm routing key; binding của từng service quyết định queue nào nhận được.
        event.type = "PAY".equals(kind) ? "PaymentRequested" : "BookingCancellationRequested";
        event.operationType = kind;
        event.accountId = Data.number(row, "accountId");
        event.revision = Data.number(row, "holdRevision");
        event.amount = Data.money(row, "amount");
        event.paymentMethod = Data.text(row, "paymentMethod");
        event.reason = Data.text(row, "failureReason");
        if (row.get("paymentId") != null) event.paymentId = UUID.fromString(Data.text(row, "paymentId"));
        return event;
    }

    /**
     * Khóa đơn và tổng hợp bằng chứng Payment/Seat nhận từ sự kiện cho đúng thao tác hiện tại.
     * Chỉ cấp vé khi đủ xác nhận tiền và ghế; chỉ hủy khi đủ bằng chứng bù trừ, không gửi lệnh REST
     * từ hàm này.
     */
    public SagaEvent observe(SagaEvent event, List<Long> admins) {
        // Điểm debug kết quả nhận từ RabbitMQ: kiểm tra event.type, operationId và bằng chứng đã lưu.
        // Mỗi sự kiện được xử lý riêng; PaymentRefunded có thể tới trước SeatReleased một khoảng thời gian.
        Map<String, Object> row = locked(event.bookingId);
        if (!event.holdId.toString().equals(Data.text(row, "holdId"))
                || event.accountId.longValue() != Data.number(row, "accountId"))
            throw new IllegalArgumentException("Event does not belong to booking");
        String status = Data.text(row, "status");
        if ("HoldExpired".equals(event.type)) {
            if (Arrays.asList("PENDING", "CREATING").contains(status))
                finish(row, "EXPIRED", "Phien giu cho het han.", admins);
            return null;
        }
        // Sự kiện đến muộn không được sửa thao tác hủy mới hơn hoặc đơn đã kết thúc.
        if (!event.operationId.toString().equals(Data.text(row, "operationId"))
                || Arrays.asList("CONFIRMED", "CANCELLED", "EXPIRED").contains(status)) return null;
        Map<String, Object> facts = progressRepository.findPublicState(event.bookingId.toString());
        if (facts == null || !event.operationType.equals(Data.text(facts, "operationType")))
            throw new IllegalArgumentException("Event operation does not match local projection");
        String payment = Data.text(facts, "paymentStatus"), seat = Data.text(facts, "seatStatus");
        if ("PaymentSucceeded".equals(event.type) || "PaymentFailed".equals(event.type)
                || "PaymentRefunded".equals(event.type)) {
            if (event.paymentId == null) throw new IllegalArgumentException("Missing payment ID");
            String knownPayment = Data.text(row, "paymentId");
            if (knownPayment != null && !knownPayment.equals(event.paymentId.toString()))
                throw new IllegalArgumentException("Payment ID mismatch");
            bookingRepository.attachPayment(event.paymentId.toString(), event.bookingId.toString());
            payment = "PaymentSucceeded".equals(event.type) ? "SUCCEEDED"
                    : "PaymentFailed".equals(event.type) ? "FAILED" : "REFUNDED";
            if (("PAY".equals(event.operationType) && "REFUNDED".equals(payment))
                    || (!"PAY".equals(event.operationType) && !"REFUNDED".equals(payment)))
                throw new IllegalArgumentException("Unexpected payment fact for operation");
        } else if ("SeatConfirmed".equals(event.type)) seat = "CONFIRMED";
        else if ("SeatReleased".equals(event.type)) seat = "RELEASED";
        else if ("SeatPaymentRejected".equals(event.type)) {
            payment = "REJECTED";
            if (Arrays.asList("EXPIRED", "RELEASED").contains(event.holdStatus)) seat = event.holdStatus;
        } else throw new IllegalArgumentException("Unsupported Booking event " + event.type);
        progressRepository.observe(event.bookingId.toString(), payment, seat, event.type, event.reason);
        timeline(event.bookingId, event.type.startsWith("Payment") ? "Payment" : "Seat", event.type, "OBSERVED", event.eventId.toString());
        BookingFlowLog.committed(event.bookingId, event.type.startsWith("Payment") ? "Payment" : "Seat", event.type, "OBSERVED");
        String kind = Data.text(facts, "operationType");
        // Trạng thái cuối dựa trên xác nhận nghiệp vụ của cả tiền và ghế, không dựa vào queue trống/ACK.
        // CANCEL_PAID mới chỉ REFUNDED thì vẫn CANCELLING; phải có cả RELEASED mới chuyển CANCELLED.
        if ("PAY".equals(kind) && "SUCCEEDED".equals(payment) && "CONFIRMED".equals(seat))
            finish(row, "CONFIRMED", null, admins);
        else if ("PAY".equals(kind) && "REJECTED".equals(payment) && "EXPIRED".equals(seat))
            finish(row, "EXPIRED", event.reason, admins);
        else if ("RELEASED".equals(seat) && (("PAY".equals(kind) && Arrays.asList("FAILED", "REJECTED").contains(payment))
                || ("CANCEL_PAID".equals(kind) && "REFUNDED".equals(payment)) || "CANCEL_UNPAID".equals(kind)))
            finish(row, "CANCELLED", event.reason == null ? Data.text(row, "failureReason") : event.reason, admins);
        else if ("PAY".equals(kind) && Arrays.asList("FAILED", "REJECTED").contains(payment))
            bookingRepository.markCompensating(event.bookingId.toString());
        return null;
    }

    /**
     * Chốt trạng thái cuối, cấp mã vé khi CONFIRMED và lưu thông báo cho khách/ADMIN trong
     * transaction Booking.
     */
    private void finish(Map<String, Object> row, String status, String reason, List<Long> admins) {
        String id = Data.text(row, "bookingId");
        String ticket = "CONFIRMED".equals(status) ? "TICKET-" + id.toUpperCase(Locale.ROOT) : Data.text(row, "ticketCode");
        bookingRepository.finishBooking(status, ticket, reason, id);
        timeline(UUID.fromString(id), "Booking", "FINISH", status, reason);
        BookingFlowLog.committed(UUID.fromString(id), "Booking", "FINISH", status);
        notifications.publishBooking(row, status, admins);
    }

    /** Lưu bước vào database; hàm nghiệp vụ gọi sẽ đăng ký log sau commit tại vị trí của chính nó. */
    private void timeline(UUID id, String service, String action, String result, String message) {
        progressRepository.appendTimeline(
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
        return progressRepository.findTimeline(id.toString());
    }

}
