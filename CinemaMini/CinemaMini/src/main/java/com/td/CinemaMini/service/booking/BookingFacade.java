package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.model.enums.SeatStatus;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.booking.exception.HoldExpiredException;
import com.td.CinemaMini.service.pricing.PricingService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tạo đơn từ ghế khách đang giữ, phối hợp kiểm tra quyền, thời hạn và tính tiền.
 * GoF: Facade. Các bước được thực hiện trong cùng transaction tạo đơn.
 */
@Service
public class BookingFacade {
    private final BookingService bookingService;
    private final BookingRepository bookings;
    private final ShowtimeRepository showtimes;
    private final RequestService requests;
    private final PricingService pricing;
    private final HoldLifecycleService lifecycle;
    private final DatabaseClock clock;

    public BookingFacade(
            BookingService bookingService,
            BookingRepository bookings,
            ShowtimeRepository showtimes,
            RequestService requests,
            PricingService pricing,
            HoldLifecycleService lifecycle,
            DatabaseClock clock) {
        this.bookingService = bookingService;
        this.bookings = bookings;
        this.showtimes = showtimes;
        this.requests = requests;
        this.pricing = pricing;
        this.lifecycle = lifecycle;
        this.clock = clock;
    }

    /**
     * API Facade: phương thức public mà BookingController gọi để tạo đơn đặt vé.
     * Nhận tài khoản, hồ sơ khách, thông tin đặt vé và mã nhận diện lần gửi.
     * Trả về đơn vừa tạo hoặc đơn đã có của cùng yêu cầu gửi lại.
     */
    @Transactional(noRollbackFor = HoldExpiredException.class)
    public Booking placeBooking(
            UserAccount actor, Customer customer, BookingRequest input, String key) {
        // Không cho tạo đơn dưới hồ sơ khách khác với khách đang thực hiện yêu cầu.
        if (!customer.getId().equals(input.getCustomerId())) {
            throw BusinessException.forbidden();
        }
        // Step Facade 1: Nhận diện yêu cầu gửi lại để tránh tạo trùng đơn.
        // Khóa mã yêu cầu theo tài khoản để cùng một lần gửi lại không tạo thêm đơn.
        IdempotencyRequest request =
                requests.lock(actor.getId(), key, "CREATE_BOOKING", input.fingerprint());
        // Yêu cầu này đã có kết quả: trả lại đơn cũ sau khi kiểm tra quyền sở hữu.
        if (request.getReferenceId() != null) {
            return bookingService.owned(request.getReferenceId(), customer);
        }
        // Step Facade 2: Kiểm tra phiên giữ ghế, các ghế đã chọn và thời hạn.
        // Lấy và khóa phiên mà khách gửi; kiểm tra chủ phiên và token trước khi sử dụng.
        HoldSession hold =
                bookingService.lockHold(input.getHoldId(), customer, input.getHoldToken());
        if (!hold.getShowtimeId().equals(input.getShowtimeId())) {
            throw BusinessException.forbidden();
        }
        // Một phiên chỉ dùng cho một đơn; tìm đơn đã tạo từ phiên để tránh tạo đơn thứ hai.
        Booking existing = bookings.findByHoldId(hold.getId()).orElse(null);
        if (existing != null) {
            // Khóa đơn có sẵn và cập nhật hết hạn nếu cần trước khi trả lại kết quả.
            existing = bookings.lockById(existing.getId()).orElseThrow(BusinessException::missing);
            lifecycle.expireIfDue(hold, existing, bookingService.lockHeld(hold));
            requests.complete(request, "BOOKING", existing.getId());
            return existing;
        }
        // Step Facade 2 (tiếp): Kiểm tra các ghế đã giữ và thời hạn của phiên.
        // Khóa các ghế của phiên trước khi kiểm tra trạng thái và tính tiền.
        List<ShowtimeSeat> rows = bookingService.lockHeld(hold);
        if (lifecycle.expireIfDue(hold, null, rows) || hold.getStatus() == HoldStatus.EXPIRED) {
            throw new HoldExpiredException();
        }
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw BusinessException.conflict("Phiên không còn cho phép tạo đơn.");
        }
        // Từ chối nếu khách đang gửi danh sách ghế dựa trên phiên bản cũ của phiên giữ ghế.
        if (hold.getRevision() != input.getRevision()) {
            throw new BusinessException(
                    409, "HOLD_REVISION_CONFLICT", "Phiên đã thay đổi. Vui lòng tải lại trang.");
        }
        // Đối chiếu cả trạng thái, chủ phiên và toàn bộ tập Seat.id để ngăn đổi ghế từ form.
        Set<Long> actual = new TreeSet<Long>();
        for (ShowtimeSeat row : rows) {
            if (row.getStatus() != SeatStatus.HELD
                    || !hold.getId().equals(row.getHoldId())
                    || !hold.getShowtimeId().equals(row.getShowtimeId())) {
                throw BusinessException.conflict("Không còn quyền giữ toàn bộ ghế.");
            }
            actual.add(row.getSeatId());
        }
        if (!actual.equals(new TreeSet<Long>(input.getSeatIds()))) {
            throw BusinessException.conflict("Danh sách ghế không khớp phiên hiện tại.");
        }
        // Lấy giờ bắt đầu của suất để chặn việc tạo đơn khi phim đã bắt đầu.
        Showtime showtime =
                showtimes.findById(hold.getShowtimeId()).orElseThrow(BusinessException::missing);
        if (!clock.now().isBefore(showtime.getStartsAt())) {
            throw BusinessException.conflict("Suất chiếu đã bắt đầu.");
        }
        // Step Facade 3: Tính tiền vé theo suất chiếu, loại ghế và nhóm khách.
        // Lấy mã và loại ghế để tính giá theo suất chiếu và nhóm ưu đãi của khách.
        List<Seat> selected = bookingService.physicalSeats(rows);
        PriceBreakdown price = pricing.calculate(customer, showtime, selected);
        // Kiểm tra lại thời hạn sau khi tính tiền, vì phiên có thể hết hạn trong lúc xử lý.
        if (lifecycle.expireIfDue(hold, null, rows)) {
            throw new HoldExpiredException();
        }
        // Step Facade 4: Lưu đơn chờ thanh toán và trả kết quả.
        // Lưu đơn chờ thanh toán cùng chi tiết ghế, rồi gắn kết quả vào mã yêu cầu.
        Booking result = bookingService.createPending(input, hold, showtime, price, selected);
        requests.complete(request, "BOOKING", result.getId());
        return result;
    }
}
