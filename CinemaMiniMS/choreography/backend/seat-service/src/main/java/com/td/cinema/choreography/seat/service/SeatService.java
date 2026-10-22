package com.td.cinema.choreography.seat.service;

import com.td.cinema.choreography.seat.dto.request.HoldChangeRequest;
import com.td.cinema.choreography.seat.dto.request.HoldCommandRequest;
import com.td.cinema.choreography.seat.dto.request.HoldCreateRequest;
import com.td.cinema.choreography.seat.dto.request.HoldReleaseRequest;
import com.td.cinema.choreography.seat.exception.ApiException;
import com.td.cinema.choreography.seat.model.HoldStatus;
import com.td.cinema.choreography.seat.repository.CatalogRepository;
import com.td.cinema.choreography.seat.repository.HoldRepository;
import com.td.cinema.choreography.seat.repository.SeatInventoryRepository;
import com.td.cinema.choreography.seat.stream.SeatEvents;
import com.td.cinema.choreography.seat.util.Data;
import com.td.cinema.choreography.seat.util.BookingFlowLog;
import com.td.cinema.choreography.seat.messaging.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Quản lý giữ, bán và trả ghế trong transaction cục bộ; khóa tồn kho để tránh nhiều khách lấy cùng ghế. */
@Service
public class SeatService {
    private final CatalogRepository catalogRepository;
    private final HoldRepository holdRepository;
    private final SeatInventoryRepository seatInventoryRepository;

    private final SeatEvents events;
    private final String secret;
    private final EventPublisher publisher;

    /**
     * Nhận các repository của Seat, bộ phát cập nhật ghế và khóa dùng tạo token phiên giữ ghế.
     * Nhận thêm bộ phát sự kiện để các thay đổi nghiệp vụ chỉ được công bố sau commit.
     */
    public SeatService(
            CatalogRepository catalogRepository,
            HoldRepository holdRepository,
            SeatInventoryRepository seatInventoryRepository,
            SeatEvents events,
            @Value("${cinema.internal-key}") String secret, EventPublisher publisher) {
        this.catalogRepository = catalogRepository;
        this.holdRepository = holdRepository;
        this.seatInventoryRepository = seatInventoryRepository;
        this.events = events;
        this.secret = secret;
        this.publisher = publisher;
    }

    /** Tạo token HMAC gắn với holdId và accountId để chứng minh quyền thao tác phiên giữ ghế. */
    private String token(UUID id, long actor) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            mac.doFinal(
                                    ("hold:" + id + ":" + actor).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Đọc bản ghi giữ ghế; tham số lock quyết định có khóa dòng để cập nhật hay không. */
    private Map<String, Object> hold(UUID id, boolean lock) {
        return holdRepository.findHold(id.toString(), lock);
    }

    /** Kiểm tra người gọi sở hữu phiên; trả 404 khi sai chủ để không tiết lộ phiên của khách khác. */
    private void owner(Map<String, Object> hold, long actor) {
        if (hold.get("accountId") == null || Data.number(hold, "accountId") != actor)
            throw new ApiException(404, "NOT_FOUND", "Khong tim thay phien giu cho.");
    }

    /** Kiểm tra token và revision hiện tại, chặn yêu cầu dùng dữ liệu phiên đã cũ. */
    private void revision(Map<String, Object> hold, String token, long revision) {
        if (token == null || !Data.hash(token).equals(Data.text(hold, "tokenHash")))
            throw new ApiException(403, "INVALID_HOLD_TOKEN", "Token giu cho khong hop le.");
        if (Data.number(hold, "revision") != revision)
            throw ApiException.conflict("Phien da thay doi. Doc lai phien truoc khi thao tac.");
    }

    /** Lấy khóa transaction theo suất chiếu để các thao tác tranh cùng tồn kho ghế được tuần tự hóa. */
    private void lockInventory(Map<String, Object> hold) {
        holdRepository.lockOperation("inventory:" + Data.text(hold, "showtimeId"));
    }

    /** Dựng dữ liệu phiên cho đúng chủ sở hữu, kèm token, danh sách ghế và thông tin suất chiếu. */
    private Map<String, Object> view(UUID id, long actor) {
        Map<String, Object> result = holdRepository.findPublicHold(id.toString());
        owner(result, actor);
        result.put("token", token(id, actor));
        result.put("seats", holdRepository.findHoldSeats(id.toString()));
        result.put(
                "showtime", catalogRepository.findShowtimeDetail(Data.text(result, "showtimeId")));
        return result;
    }

    /** Tìm kết quả đã xử lý của cùng khóa; chỉ cho dùng lại nếu chủ sở hữu và dấu vân tay request khớp. */
    private UUID replay(String key, long actor, String fingerprint) {
        Map<String, Object> prior = holdRepository.findOperation(key);
        if (prior == null) return null;
        if (Data.number(prior, "accountId") != actor
                || !fingerprint.equals(Data.text(prior, "requestHash")))
            throw ApiException.conflict("Khoa thao tac da duoc dung voi noi dung khac.");
        return UUID.fromString(Data.text(prior, "holdId"));
    }

    /** Ghi khóa và dấu vân tay request để lần gửi lại không tạo thao tác giữ ghế mới. */
    private void remember(String key, long actor, String fingerprint, UUID hold) {
        holdRepository.insertOperation(key, actor, fingerprint, hold.toString());
    }

    /**
     * Tạo phiên giữ 1–6 ghế sau khi khóa tồn kho và kiểm tra ghế trống; hạn giữ tối đa 5 phút và
     * không vượt giờ chiếu.
     */
    @Transactional
    public Map<String, Object> create(long actor, String key, HoldCreateRequest request) {
        key = Data.key(key);
        List<String> selected = Data.seats(request.seatNos);
        String fingerprint = Data.hash("CREATE|" + request.showtimeId + "|" + selected);
        holdRepository.lockOperation("hold-key:" + key);
        UUID prior = replay(key, actor, fingerprint);
        if (prior != null) return view(prior, actor);
        holdRepository.lockOperation("inventory:" + request.showtimeId);
        expireShow(request.showtimeId);
        Map<String, Object> show = catalogRepository.findShowtimeStart(request.showtimeId);
        OffsetDateTime now = holdRepository.currentTime(),
                start = OffsetDateTime.parse(Data.text(show, "startsAt"));
        if (!start.isAfter(now)) throw ApiException.conflict("Suat chieu da bat dau.");
        OffsetDateTime deadline = now.plusMinutes(5).isBefore(start) ? now.plusMinutes(5) : start;
        UUID id = UUID.randomUUID();
        holdRepository.insertHold(
                id.toString(),
                actor,
                request.showtimeId,
                now,
                deadline,
                Data.hash(token(id, actor)),
                selected.size());
        setSeats(id, request.showtimeId, deadline, selected);
        remember(key, actor, fingerprint, id);
        events.changed(request.showtimeId);
        return view(id, actor);
    }

    /** Kiểm tra chủ sở hữu, xử lý phiên quá hạn của suất rồi trả dữ liệu giữ ghế mới nhất. */
    @Transactional
    public Map<String, Object> get(UUID id, long actor) {
        Map<String, Object> found = hold(id, false);
        owner(found, actor);
        lockInventory(found);
        expireShow(Data.text(found, "showtimeId"));
        return view(id, actor);
    }

    /**
     * Thay tập ghế của phiên ACTIVE sau khi kiểm tra token/revision; tăng revision để các request
     * cũ không ghi đè.
     */
    @Transactional
    public Map<String, Object> change(UUID id, long actor, String key, HoldChangeRequest request) {
        key = Data.key(key);
        List<String> selected = Data.seats(request.seatNos);
        String fingerprint =
                Data.hash(
                        "CHANGE|"
                                + id
                                + "|"
                                + request.revision
                                + "|"
                                + Data.hash(request.token)
                                + "|"
                                + selected);
        holdRepository.lockOperation("hold-key:" + key);
        UUID prior = replay(key, actor, fingerprint);
        if (prior != null) return view(prior, actor);
        Map<String, Object> current = hold(id, false);
        owner(current, actor);
        lockInventory(current);
        expireShow(Data.text(current, "showtimeId"));
        current = hold(id, true);
        revision(current, request.token, request.revision);
        if (!HoldStatus.ACTIVE.name().equals(Data.text(current, "status")))
            throw ApiException.conflict("Chi thay ghe truoc khi tao don va truoc khi het han.");
        setSeats(
                id,
                Data.text(current, "showtimeId"),
                OffsetDateTime.parse(Data.text(current, "expiresAt")),
                selected);
        holdRepository.incrementRevision(id.toString());
        remember(key, actor, fingerprint, id);
        events.changed(Data.text(current, "showtimeId"));
        return view(id, actor);
    }

    /**
     * Khóa sơ đồ ghế, xác nhận toàn bộ ghế yêu cầu đều khả dụng rồi thay tập ghế của phiên trong
     * cùng transaction.
     */
    private void setSeats(
            UUID id, String showtime, OffsetDateTime deadline, List<String> selected) {
        List<Map<String, Object>> inventory =
                seatInventoryRepository.lockShowtimeInventory(showtime);
        List<Map<String, Object>> wanted = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : inventory)
            if (selected.contains(Data.text(row, "seatNo"))) {
                if (!"AVAILABLE".equals(Data.text(row, "status"))
                        && !("HELD".equals(Data.text(row, "status"))
                                && id.toString().equals(Data.text(row, "heldByHoldId"))))
                    throw ApiException.conflict(
                            "Ghe " + Data.text(row, "seatNo") + " khong con trong.");
                wanted.add(row);
            }
        if (wanted.size() != selected.size()) throw ApiException.bad("Danh sach ghe khong hop le.");
        seatInventoryRepository.clearHeldInventory(id.toString());
        for (Map<String, Object> row : wanted) {
            long seat = Data.number(row, "seatId");
            seatInventoryRepository.holdSeat(id.toString(), deadline, showtime, seat);
        }
        holdRepository.updateSeatCount(id.toString(), selected.size());
    }

    /** Kiểm tra chủ sở hữu, token và revision rồi gắn phiên giữ ghế với bookingId để bắt đầu checkout. */
    @Transactional
    public Map<String, Object> attach(UUID id, HoldCommandRequest request) {
        Map<String, Object> current = hold(id, false);
        owner(current, request.accountId);
        lockInventory(current);
        expireShow(Data.text(current, "showtimeId"));
        current = hold(id, true);
        if (!Data.hash(Data.required(request.token, "Token", 200))
                .equals(Data.text(current, "tokenHash")))
            throw new ApiException(403, "INVALID_HOLD_TOKEN", "Token khong hop le.");
        if (HoldStatus.CHECKOUT.name().equals(Data.text(current, "status"))
                && request.bookingId.toString().equals(Data.text(current, "bookingId")))
            return view(id, request.accountId);
        revision(current, request.token, request.revision);
        if (!HoldStatus.ACTIVE.name().equals(Data.text(current, "status")))
            throw ApiException.conflict("Phien khong the tao don.");
        holdRepository.attachBooking(request.bookingId.toString(), id.toString());
        seatInventoryRepository.attachBooking(request.bookingId.toString(), id.toString());
        BookingFlowLog.committed(request.bookingId, "Seat", "CHECKOUT", "HELD");
        return view(id, request.accountId);
    }

    /** Xác nhận request đồng thời thuộc đúng tài khoản và đúng đơn đã gắn với phiên giữ ghế. */
    private void bookingOwner(Map<String, Object> current, HoldCommandRequest request) {
        owner(current, request.accountId);
        if (!request.bookingId.toString().equals(Data.text(current, "bookingId")))
            throw ApiException.conflict("Phien khong thuoc don.");
    }

    /**
     * Đưa phiên CHECKOUT hợp lệ sang PAYMENT_PROCESSING hoặc trả trạng thái từ chối để consumer
     * phát sự kiện phù hợp.
     * Nếu revision đã cũ, trả ghế và đánh dấu RELEASED thay vì bắt đầu thu tiền.
     */
    @Transactional
    public Map<String, Object> beginPayment(UUID id, HoldCommandRequest request) {
        Map<String, Object> current = hold(id, false);
        bookingOwner(current, request);
        lockInventory(current);
        expireShow(Data.text(current, "showtimeId"));
        current = hold(id, true);
        if (HoldStatus.PAYMENT_PROCESSING.name().equals(Data.text(current, "status")))
            return view(id, request.accountId);
        if (!HoldStatus.CHECKOUT.name().equals(Data.text(current, "status")))
            return view(id, request.accountId); // Trả trạng thái từ chối nghiệp vụ để consumer xử lý.
        if (Data.number(current, "revision") != request.revision) {
            clearInventory(id, false);
            holdRepository.markReleased(id.toString());
            events.changed(Data.text(current, "showtimeId"));
            return view(id, request.accountId);
        }
        holdRepository.markPaymentProcessing(id.toString());
        BookingFlowLog.committed(request.bookingId, "Seat", "BEGIN_PAYMENT", "PAYMENT_PROCESSING");
        return view(id, request.accountId);
    }

    /**
     * Chuyển toàn bộ ghế đang giữ của đơn sang SOLD và phiên sang CONSUMED trong cùng transaction.
     * Chỉ xác nhận khi đủ đúng tập ghế; cập nhật SSE được phát sau commit.
     */
    @Transactional
    public Map<String, Object> confirm(UUID id, HoldCommandRequest request) {
        Map<String, Object> current = hold(id, false);
        bookingOwner(current, request);
        lockInventory(current);
        current = hold(id, true);
        if (HoldStatus.CONSUMED.name().equals(Data.text(current, "status")))
            return view(id, request.accountId);
        if (!HoldStatus.PAYMENT_PROCESSING.name().equals(Data.text(current, "status")))
            throw ApiException.conflict("Phien chua nhan thanh toan.");
        int count = holdRepository.expectedSeatCount(id.toString());
        int changed =
                seatInventoryRepository.sellHeldSeats(id.toString(), request.bookingId.toString());
        if (count < 1 || count != changed) throw ApiException.conflict("Tap ghe da thay doi.");
        holdRepository.markConsumed(id.toString());
        BookingFlowLog.committed(request.bookingId, "Seat", "CONFIRM_SEAT", "SOLD");
        events.changed(Data.text(current, "showtimeId"));
        return view(id, request.accountId);
    }

    /**
     * Kiểm tra lý do hủy/bù trừ tương ứng với trạng thái phiên rồi trả đủ ghế và đánh dấu RELEASED.
     * Bước này phải được luồng Booking hoặc sự kiện Saga gọi; hủy đơn ở Booking không tự đổi ghế
     * tại Seat.
     * Đây là Compensating Transaction ở lượt xử lý sau để bù việc giữ/bán ghế đã commit, không
     * rollback transaction cũ. PAYMENT_FAILED trả ghế HELD; CANCEL_PAID trả ghế SOLD sau hoàn tiền.
     * Nếu đang trong transaction của event handler, hàm tham gia theo REQUIRED mặc định.
     */
    @Transactional
    public Map<String, Object> release(UUID id, HoldCommandRequest request) {
        Map<String, Object> current = hold(id, false);
        bookingOwner(current, request);
        lockInventory(current);
        current = hold(id, true);
        String status = Data.text(current, "status");
        if (HoldStatus.RELEASED.name().equals(status) || HoldStatus.EXPIRED.name().equals(status))
            return view(id, request.accountId);
        boolean allowed =
                ("CANCEL_PAID".equals(request.reason) && HoldStatus.CONSUMED.name().equals(status))
                        || ("PAYMENT_FAILED".equals(request.reason)
                                && HoldStatus.PAYMENT_PROCESSING.name().equals(status))
                        || ("CANCEL_UNPAID".equals(request.reason)
                                && HoldStatus.CHECKOUT.name().equals(status));
        if (!allowed) throw ApiException.conflict("Khong duoc tra ghe o trang thai hien tai.");
        int expected = holdRepository.expectedSeatCount(id.toString());
        int cleared = clearInventory(id, "CANCEL_PAID".equals(request.reason));
        if (expected < 1 || expected != cleared)
            throw ApiException.conflict("Chua xac nhan tra du tap ghe cua phien.");
        holdRepository.markReleased(id.toString());
        BookingFlowLog.committed(request.bookingId, "Seat", "RELEASE_SEAT", "RELEASED");
        events.changed(Data.text(current, "showtimeId"));
        return view(id, request.accountId);
    }

    /**
     * Trả ghế của phiên ACTIVE chưa tạo đơn sau khi kiểm tra chủ sở hữu, token/revision và chống
     * gửi trùng.
     */
    @Transactional
    public Map<String, Object> abandon(
            UUID id, long actor, String key, HoldReleaseRequest request) {
        key = Data.key(key);
        String fingerprint =
                Data.hash(
                        "ABANDON|" + id + "|" + request.revision + "|" + Data.hash(request.token));
        holdRepository.lockOperation("hold-key:" + key);
        UUID prior = replay(key, actor, fingerprint);
        if (prior != null) return view(prior, actor);
        Map<String, Object> current = hold(id, false);
        owner(current, actor);
        lockInventory(current);
        current = hold(id, true);
        revision(current, request.token, request.revision);
        if (!HoldStatus.ACTIVE.name().equals(Data.text(current, "status")))
            throw ApiException.conflict("Chi tra phien chua tao don; huy don bang API booking.");
        clearInventory(id, false);
        holdRepository.markReleased(id.toString());
        remember(key, actor, fingerprint, id);
        events.changed(Data.text(current, "showtimeId"));
        return view(id, actor);
    }

    /** Giải phóng ghế thuộc đúng holdId, chỉ tác động trạng thái HELD hoặc SOLD được chỉ định. */
    private int clearInventory(UUID id, boolean sold) {
        return seatInventoryRepository.clearInventory(id.toString(), sold ? "SOLD" : "HELD");
    }

    /**
     * Trả ghế của các phiên giữ/chờ thanh toán đã hết hạn; không tự xử lý phiên PAYMENT_PROCESSING.
     * Phiên đã gắn đơn phát thêm HoldExpired sau commit để Booking cập nhật kết quả.
     */
    private void expireShow(String showtime) {
        for (Map<String, Object> expired : holdRepository.lockExpiredHolds(showtime)) {
            UUID id = UUID.fromString(Data.text(expired, "holdId"));
            clearInventory(id, false);
            Map<String, Object> current = hold(id, true);
            holdRepository.markExpired(id.toString());
            if (current.get("bookingId") != null) {
                SagaEvent expiredEvent = new SagaEvent();
                expiredEvent.bookingId = UUID.fromString(Data.text(current, "bookingId"));
                expiredEvent.holdId = id;
                expiredEvent.correlationId = expiredEvent.bookingId;
                expiredEvent.accountId = Data.number(current, "accountId");
                expiredEvent.eventId = UUID.nameUUIDFromBytes((id + "|HoldExpired").getBytes(StandardCharsets.UTF_8));
                expiredEvent.type = "HoldExpired";
                expiredEvent.holdStatus = "EXPIRED";
                expiredEvent.reason = "Phien giu cho het han.";
                publisher.afterCommit(expiredEvent);
            }
            events.changed(showtime);
        }
    }

    /** Khóa tồn kho của suất rồi xử lý các phiên giữ ghế quá hạn trong transaction cục bộ. */
    @Transactional
    public void expire(String showtime) {
        holdRepository.lockOperation("inventory:" + showtime);
        expireShow(showtime);
    }

    /** Khóa suất, giải phóng phiên hết hạn và trả sơ đồ ghế kèm thông tin ghế do người gọi đang giữ. */
    @Transactional
    public List<Map<String, Object>> inventory(String showtime, long actor) {
        holdRepository.lockOperation("inventory:" + showtime);
        expireShow(showtime);
        catalogRepository.requireShowtime(showtime);
        return seatInventoryRepository.findInventory(actor, showtime);
    }

    /** Liệt kê các suất có phiên cần xử lý hết hạn để tác vụ định kỳ quét đúng phạm vi. */
    public List<Map<String, Object>> expiringShowtimes() {
        return holdRepository.findExpiringShowtimes();
    }
}
