package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.model.enums.SeatStatus;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.booking.exception.HoldExpiredException;
import com.td.CinemaMini.service.booking.exception.StaleHoldException;
import com.td.CinemaMini.service.booking.stream.SeatEventStream;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Thêm hoặc trả ghế trong transaction, sau khi khóa phiên, đơn và các ghế liên quan. */
@Service
public class HoldMutationService {
    private final HoldSessionRepository holds;
    private final ShowtimeRepository showtimes;
    private final BookingRepository bookings;
    private final ShowtimeSeatRepository inventory;
    private final BookingService bookingService;
    private final DatabaseClock clock;
    private final RequestService requests;
    private final HoldLifecycleService lifecycle;
    private final SeatEventStream stream;

    public HoldMutationService(
            HoldSessionRepository holds,
            ShowtimeRepository showtimes,
            BookingRepository bookings,
            ShowtimeSeatRepository inventory,
            BookingService bookingService,
            DatabaseClock clock,
            RequestService requests,
            HoldLifecycleService lifecycle,
            SeatEventStream stream) {
        this.holds = holds;
        this.showtimes = showtimes;
        this.bookings = bookings;
        this.inventory = inventory;
        this.bookingService = bookingService;
        this.clock = clock;
        this.requests = requests;
        this.lifecycle = lifecycle;
        this.stream = stream;
    }

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public HoldSession add(
            UserAccount actor,
            Customer customer,
            Long showtimeId,
            Long holdId,
            UUID token,
            Long seatId,
            long revision,
            String key) {
        if (seatId == null || showtimeId == null) {
            throw BusinessException.invalid("Thiếu suất chiếu hoặc ghế.");
        }
        // Khóa mã yêu cầu kèm toàn bộ dữ liệu chọn ghế để nhận diện đúng lần gửi lại.
        IdempotencyRequest request =
                requests.lock(
                        actor.getId(),
                        key,
                        "HOLD_SEAT",
                        showtimeId + ":" + holdId + ":" + token + ":" + seatId + ":" + revision);
        if (request.getReferenceId() != null) {
            return replay(customer, request.getReferenceId());
        }
        Showtime showtime = showtimes.findById(showtimeId).orElseThrow(BusinessException::missing);
        // Không có holdId nghĩa là lần chọn ghế đầu tiên, cần tạo phiên giữ ghế mới.
        boolean first = holdId == null;
        // Các lần sửa phiên đã có phải gửi token để chứng minh quyền sử dụng phiên.
        if (!first && token == null) {
            throw BusinessException.forbidden();
        }
        HoldSession hold;
        // Gắn phiên mới với khách và suất chiếu; hạn giữ ghế tối đa 5 phút hoặc đến giờ chiếu.
        if (first) {
            hold = new HoldSession();
            hold.setCustomerId(customer.getId());
            hold.setShowtimeId(showtimeId);
            Instant now = clock.now();
            requireFuture(showtime, now);
            hold.setStartedAt(now);
            hold.setExpiresAt(deadline(now, showtime.getStartsAt()));
            holds.saveAndFlush(hold);
        } else {
            hold = bookingService.lockHold(holdId, customer, token);
        }
        if (!hold.getShowtimeId().equals(showtimeId)) {
            throw BusinessException.forbidden();
        }
        // Khóa đơn của phiên nếu đã có, rồi tìm ghế khách muốn chọn trong đúng suất chiếu.
        Booking booking = lockBooking(hold);
        ShowtimeSeat target =
                inventory
                        .findByShowtimeIdAndSeatId(showtimeId, seatId)
                        .orElseThrow(BusinessException::missing);
        // Gom cả ghế muốn thêm và các ghế đã giữ để khóa một lượt theo ID tăng dần.
        List<Long> ids = new ArrayList<Long>();
        ids.add(target.getId());
        for (ShowtimeSeat row : inventory.findByHoldIdOrderByIdAsc(hold.getId())) {
            ids.add(row.getId());
        }
        List<ShowtimeSeat> rows = bookingService.lockInventory(ids);
        if (!first && lifecycle.expireIfDue(hold, booking, rows)) {
            throw new HoldExpiredException();
        }
        // Sau khi có khóa, kiểm tra phiên còn được sửa và dữ liệu gửi lên đúng phiên bản.
        ensureActive(hold, revision, first);
        Instant now = clock.now();
        requireFuture(showtime, now);
        // Tính lại thời điểm bắt đầu sau khi chờ khóa, để phiên mới có đúng thời gian giữ ghế.
        if (first) {
            hold.setStartedAt(now);
            hold.setExpiresAt(deadline(now, showtime.getStartsAt()));
        }
        // Dùng đối tượng ghế đã được khóa để kiểm tra và cập nhật trạng thái.
        target =
                rows.stream()
                        .filter(s -> s.getSeatId().equals(seatId))
                        .findFirst()
                        .orElseThrow(BusinessException::missing);
        // Ghế có thể đang thuộc phiên của người khác đã quá hạn.
        // Báo StaleHoldException để thoát transaction hiện tại trước khi khóa và dọn phiên đó.
        if (target.getStatus() == SeatStatus.HELD && !hold.getId().equals(target.getHoldId())) {
            HoldSession previous =
                    holds.findById(target.getHoldId()).orElseThrow(BusinessException::missing);
            if (!clock.now().isBefore(previous.getExpiresAt())) {
                throw new StaleHoldException(previous.getId());
            }
        }
        // Chỉ ghế còn trống mới được giữ thêm; một phiên được giữ tối đa 6 ghế.
        if (target.getStatus() == SeatStatus.AVAILABLE) {
            long count =
                    rows.stream()
                            .filter(
                                    s ->
                                            hold.getId().equals(s.getHoldId())
                                                    && s.getStatus() == SeatStatus.HELD)
                            .count();
            if (count >= 6) {
                throw BusinessException.invalid("Mỗi phiên chỉ được chọn tối đa 6 ghế.");
            }
            // Đánh dấu ghế thuộc phiên này và tăng revision để các tab nhận biết thay đổi.
            target.setStatus(SeatStatus.HELD);
            target.setHoldId(hold.getId());
            target.setBookingId(null);
            hold.setRevision(hold.getRevision() + 1);
            stream.changed(showtimeId);
        // Ghế đã thuộc chính phiên này thì giữ nguyên; các trạng thái khác đều bị từ chối.
        } else if (target.getStatus() != SeatStatus.HELD
                || !hold.getId().equals(target.getHoldId())) {
            throw new BusinessException(
                    409,
                    "SEAT_UNAVAILABLE",
                    "Ghế đã được giữ, đã bán hoặc không mở bán. Vui lòng chọn ghế khác.");
        }
        requests.complete(request, "HOLD", hold.getId());
        return hold;
    }

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public HoldSession remove(
            UserAccount actor,
            Customer customer,
            Long holdId,
            UUID token,
            Long seatId,
            long revision,
            String key) {
        // Trả ghế của phiên hiện có cũng phải cung cấp token.
        if (token == null) {
            throw BusinessException.forbidden();
        }
        // Nhận diện lần trả ghế theo tài khoản, mã yêu cầu và nội dung gửi lên.
        IdempotencyRequest request =
                requests.lock(
                        actor.getId(),
                        key,
                        "RELEASE_SEAT",
                        holdId + ":" + token + ":" + seatId + ":" + revision);
        if (request.getReferenceId() != null) {
            return replay(customer, request.getReferenceId());
        }
        // Khóa phiên và xác minh chủ phiên/token trước khi khóa đơn và ghế để trả chỗ.
        HoldSession hold = bookingService.lockHold(holdId, customer, token);
        Booking booking = lockBooking(hold);
        List<ShowtimeSeat> rows = bookingService.lockHeld(hold);
        if (lifecycle.expireIfDue(hold, booking, rows)) {
            throw new HoldExpiredException();
        }
        ensureActive(hold, revision, false);
        // seatId = null nghĩa là trả tất cả ghế đang giữ; có seatId thì chỉ trả ghế đó.
        boolean changed = false;
        for (ShowtimeSeat row : rows) {
            if (row.getStatus() == SeatStatus.HELD
                    && hold.getId().equals(row.getHoldId())
                    && (seatId == null || seatId.equals(row.getSeatId()))) {
                row.setStatus(SeatStatus.AVAILABLE);
                row.setHoldId(null);
                row.setBookingId(null);
                changed = true;
            }
        }
        // Nếu phiên không còn ghế nào, đóng phiên; tăng revision khi ghế hoặc trạng thái thay đổi.
        boolean empty = rows.stream().noneMatch(row -> hold.getId().equals(row.getHoldId()));
        if (empty) {
            hold.setStatus(HoldStatus.RELEASED);
        }
        if (changed || empty) {
            hold.setRevision(hold.getRevision() + 1);
            stream.changed(hold.getShowtimeId());
        }
        requests.complete(request, "HOLD", hold.getId());
        return hold;
    }

    /** Trả lại phiên của yêu cầu đã xử lý, sau khi kiểm tra quyền sở hữu và thời hạn hiện tại. */
    private HoldSession replay(Customer customer, Long id) {
        HoldSession hold = bookingService.lockHold(id, customer, null);
        Booking booking = lockBooking(hold);
        if (lifecycle.expireIfDue(hold, booking, bookingService.lockHeld(hold))) {
            throw new HoldExpiredException();
        }
        if (hold.getStatus() == HoldStatus.EXPIRED) {
            throw new HoldExpiredException();
        }
        return hold;
    }

    /** Phiên chưa tạo đơn trả về null; nếu có đơn thì khóa đơn sau khi đã khóa phiên. */
    private Booking lockBooking(HoldSession hold) {
        Booking booking = bookings.findByHoldId(hold.getId()).orElse(null);
        return booking == null
                ? null
                : bookings.lockById(booking.getId()).orElseThrow(BusinessException::missing);
    }

    /** Chỉ phiên ACTIVE được sửa ghế; phiên cũ phải có revision khớp với dữ liệu hiện tại. */
    private void ensureActive(HoldSession hold, long revision, boolean first) {
        if (hold.getStatus() == HoldStatus.EXPIRED) {
            throw new HoldExpiredException();
        }
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw BusinessException.conflict(
                    "Phiên đã chốt đơn hoặc đã kết thúc. Vui lòng tải lại trang.");
        }
        if (!first && hold.getRevision() != revision) {
            throw new BusinessException(
                    409,
                    "HOLD_REVISION_CONFLICT",
                    "Phiên đã thay đổi ở tab khác. Vui lòng đồng bộ lại.");
        }
    }

    /** Chặn giữ hoặc sửa ghế khi suất chiếu đã bắt đầu. */
    private void requireFuture(Showtime showtime, Instant now) {
        if (!now.isBefore(showtime.getStartsAt())) {
            throw BusinessException.conflict("Suất chiếu đã bắt đầu.");
        }
    }

    /** Hạn giữ ghế là thời điểm sớm hơn giữa 5 phút kể từ now và giờ bắt đầu suất chiếu. */
    private Instant deadline(Instant now, Instant start) {
        Instant max = now.plusSeconds(300);
        return max.isBefore(start) ? max : start;
    }
}
