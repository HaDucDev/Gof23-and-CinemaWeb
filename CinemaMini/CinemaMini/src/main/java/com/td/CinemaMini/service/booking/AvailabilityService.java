package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.model.enums.SeatStatus;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.pricing.PricingService;
import java.util.*;
import org.springframework.stereotype.Service;
import static com.td.CinemaMini.dto.Values.map;

/** Chuẩn bị trạng thái ghế, thông tin giữ ghế và báo giá cho màn hình chọn ghế. */
@Service
public class AvailabilityService {
    private final ShowtimeRepository showtimes;
    private final ShowtimeSeatRepository inventory;
    private final SeatRepository seats;
    private final HoldSessionRepository holds;
    private final BookingRepository bookings;
    private final HoldExpirationService expiration;
    private final PricingService pricing;
    private final DatabaseClock clock;

    public AvailabilityService(
            ShowtimeRepository showtimes,
            ShowtimeSeatRepository inventory,
            SeatRepository seats,
            HoldSessionRepository holds,
            BookingRepository bookings,
            HoldExpirationService expiration,
            PricingService pricing,
            DatabaseClock clock) {
        this.showtimes = showtimes;
        this.inventory = inventory;
        this.seats = seats;
        this.holds = holds;
        this.bookings = bookings;
        this.expiration = expiration;
        this.pricing = pricing;
        this.clock = clock;
    }

    public Map<String, Object> snapshot(Long showtimeId) {
        Showtime showtime = showtimes.findById(showtimeId).orElseThrow(BusinessException::missing);
        // Lập bảng tra Seat.id để ghép trạng thái ghế của suất với mã ghế, hàng và loại ghế.
        Map<Long, Seat> physical = new HashMap<Long, Seat>();
        for (Seat seat : seats.findAll()) {
            physical.put(seat.getId(), seat);
        }
        // Mỗi dòng trả về gồm thông tin ghế, trạng thái hiện tại, phiên bản và giá trước ưu đãi.
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (ShowtimeSeat row : inventory.findByShowtimeIdOrderByIdAsc(showtimeId)) {
            Seat seat = physical.get(row.getSeatId());
            if (seat == null) {
                throw BusinessException.missing();
            }
            rows.add(
                    map(
                            "id",
                            row.getId(),
                            "seatId",
                            seat.getId(),
                            "code",
                            seat.getCode(),
                            "row",
                            seat.getRowLabel(),
                            "number",
                            seat.getSeatNumber(),
                            "type",
                            seat.getSeatType().name(),
                            "status",
                            row.getStatus().name(),
                            "version",
                            row.getVersion(),
                            "unitPrice",
                            pricing.unitPrice(showtime, seat)));
        }
        // Sắp xếp mã ghế để trình duyệt dựng sơ đồ nhất quán; kèm giờ database để tính thời hạn.
        rows.sort(Comparator.comparing(row -> (String) row.get("code")));
        return map("seats", rows, "serverTime", clock.now(), "startsAt", showtime.getStartsAt());
    }

    public Map<String, Object> holdView(Long id, Customer customer) {
        // Kiểm tra phiên thuộc khách trước khi trả token và danh sách ghế đang giữ.
        HoldSession own = holds.findById(id).orElseThrow(BusinessException::missing);
        if (!own.getCustomerId().equals(customer.getId())) {
            throw BusinessException.forbidden();
        }
        // Xử lý phiên quá hạn trong transaction riêng, rồi đọc lại để hiển thị trạng thái mới.
        expiration.expire(id);
        HoldSession hold = holds.findById(id).orElseThrow(BusinessException::missing);
        List<Long> selected = new ArrayList<Long>();
        List<Seat> physical = new ArrayList<Seat>();
        // Chỉ các ghế còn ở trạng thái HELD được đưa vào danh sách khách đang giữ.
        for (ShowtimeSeat row : inventory.findByHoldIdOrderByIdAsc(id)) {
            if (row.getStatus() == SeatStatus.HELD) {
                selected.add(row.getSeatId());
                physical.add(
                        seats.findById(row.getSeatId()).orElseThrow(BusinessException::missing));
            }
        }
        Showtime showtime =
                showtimes.findById(hold.getShowtimeId()).orElseThrow(BusinessException::missing);
        // Nếu phiên đã tạo đơn, trả thêm Booking.id để màn hình tiếp tục đúng đơn đó.
        Booking booking = bookings.findByHoldId(id).orElse(null);
        return map(
                "id",
                id,
                "token",
                hold.getToken(),
                "showtimeId",
                hold.getShowtimeId(),
                "status",
                hold.getStatus().name(),
                "revision",
                hold.getRevision(),
                "expiresAt",
                hold.getExpiresAt(),
                "serverTime",
                clock.now(),
                "seatIds",
                selected,
                "price",
                pricing.calculate(customer, showtime, physical),
                "bookingId",
                booking == null ? null : booking.getId());
    }

    public Map<String, Object> myHold(Long showtimeId, Customer customer) {
        // Tìm phiên ACTIVE hoặc CHECKOUT mới nhất của khách trong suất đang xem.
        List<HoldSession> found =
                holds.findByCustomerIdAndShowtimeIdAndStatusInOrderByIdDesc(
                        customer.getId(),
                        showtimeId,
                        Arrays.asList(HoldStatus.ACTIVE, HoldStatus.CHECKOUT));
        return map("hold", found.isEmpty() ? null : holdView(found.get(0).getId(), customer));
    }

    public PriceBreakdown quote(Long showtimeId, Customer customer, List<Long> ids) {
        // Báo giá chỉ nhận từ 1 đến 6 Seat.id khác nhau, không chứa phần tử null.
        if (ids == null
                || ids.isEmpty()
                || ids.size() > 6
                || ids.contains(null)
                || new HashSet<Long>(ids).size() != ids.size()) {
            throw BusinessException.invalid("Chọn từ 1 đến 6 ghế khác nhau.");
        }
        Showtime showtime = showtimes.findById(showtimeId).orElseThrow(BusinessException::missing);
        List<Seat> selected = new ArrayList<Seat>();
        // Kiểm tra mỗi Seat.id có dòng ghế trong suất chiếu, rồi lấy loại ghế để tính tiền.
        // Bước báo giá chỉ đọc dữ liệu; quyền giữ ghế được kiểm tra khi giữ ghế và tạo đơn.
        for (Long id : ids) {
            inventory
                    .findByShowtimeIdAndSeatId(showtimeId, id)
                    .orElseThrow(BusinessException::missing);
            selected.add(seats.findById(id).orElseThrow(BusinessException::missing));
        }
        return pricing.calculate(customer, showtime, selected);
    }
}
