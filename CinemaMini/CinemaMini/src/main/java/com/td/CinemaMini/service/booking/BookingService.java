package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.dto.*;
import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.model.enums.SeatType;
import com.td.CinemaMini.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tra quyền sở hữu đơn, khóa dữ liệu đặt vé và lưu đơn cùng các ghế đã chọn. */
@Service
public class BookingService {
    private final BookingRepository bookings;
    private final BookingItemRepository items;
    private final HoldSessionRepository holds;
    private final ShowtimeSeatRepository inventory;
    private final SeatRepository seats;

    public BookingService(
            BookingRepository bookings,
            BookingItemRepository items,
            HoldSessionRepository holds,
            ShowtimeSeatRepository inventory,
            SeatRepository seats) {
        this.bookings = bookings;
        this.items = items;
        this.holds = holds;
        this.inventory = inventory;
        this.seats = seats;
    }

    @Transactional(readOnly = true)
    public Booking owned(Long id, Customer customer) {
        // Lấy đơn theo Booking.id; chỉ trả đơn nếu thuộc đúng hồ sơ người mua vé.
        Booking booking = bookings.findById(id).orElseThrow(BusinessException::missing);
        if (!booking.getCustomerId().equals(customer.getId())) {
            throw BusinessException.forbidden();
        }
        return booking;
    }

    public HoldSession lockHold(Long id, Customer customer, UUID token) {
        // Khóa phiên giữ ghế trong database để các yêu cầu khác không đồng thời sửa phiên.
        // Kiểm tra chủ phiên; nếu token được truyền vào thì token cũng phải khớp.
        HoldSession hold = holds.lockById(id).orElseThrow(BusinessException::missing);
        if (!hold.getCustomerId().equals(customer.getId())
                || (token != null && !hold.getToken().equals(token))) {
            throw BusinessException.forbidden();
        }
        return hold;
    }

    /**
     * Khóa dữ liệu theo thứ tự: phiên giữ ghế, đơn đặt vé, rồi các ghế theo ID tăng dần.
     * Các luồng cùng dùng thứ tự này để giảm nguy cơ chờ khóa lẫn nhau.
     */
    public LockedBooking lock(Long id, Customer customer) {
        // Chỉ đọc ID trước khi chờ khóa: tránh cache entity Booking cũ trong persistence context.
        Long holdId = bookings.findHoldIdById(id).orElseThrow(BusinessException::missing);
        // Lấy và khóa phiên giữ ghế gắn với đơn, đồng thời kiểm tra phiên thuộc khách này.
        // Truyền null vì đang tra phiên từ đơn có sẵn; bước này không kiểm tra token từ form.
        HoldSession hold = lockHold(holdId, customer, null);
        // Khóa và đọc đơn sau khi đã khóa phiên, để kiểm tra dữ liệu mới nhất của đơn.
        Booking booking = bookings.lockById(id).orElseThrow(BusinessException::missing);
        if (!booking.getCustomerId().equals(customer.getId())
                || !booking.getShowtimeId().equals(hold.getShowtimeId())) {
            throw BusinessException.forbidden();
        }
        // Đổi Seat.id trong chi tiết đơn thành ShowtimeSeat.id của đúng suất chiếu.
        // Khóa theo ShowtimeSeat.id vì trạng thái một ghế được quản lý riêng cho từng suất.
        List<Long> ids = new ArrayList<Long>();
        for (BookingItem item : items.findByBookingIdOrderByIdAsc(id)) {
            ShowtimeSeat row =
                    inventory
                            .findByShowtimeIdAndSeatId(booking.getShowtimeId(), item.getSeatId())
                            .orElseThrow(BusinessException::missing);
            ids.add(row.getId());
        }
        return new LockedBooking(hold, booking, lockInventory(ids));
    }

    public List<ShowtimeSeat> lockInventory(Collection<Long> ids) {
        List<ShowtimeSeat> result = new ArrayList<ShowtimeSeat>();
        // Loại ID trùng và khóa từng ghế theo ID tăng dần để thống nhất thứ tự lấy khóa.
        for (Long id : new TreeSet<Long>(ids)) {
            result.add(inventory.lockById(id).orElseThrow(BusinessException::missing));
        }
        return result;
    }

    public List<ShowtimeSeat> lockHeld(HoldSession hold) {
        List<Long> ids = new ArrayList<Long>();
        // Tìm các dòng ghế đang liên kết với phiên, rồi khóa chúng trước khi thay đổi trạng thái.
        for (ShowtimeSeat row : inventory.findByHoldIdOrderByIdAsc(hold.getId())) {
            ids.add(row.getId());
        }
        return lockInventory(ids);
    }

    public Booking createPending(
            BookingRequest request,
            HoldSession hold,
            Showtime showtime,
            PriceBreakdown price,
            List<Seat> selected) {
        // Tạo đơn chờ thanh toán và sinh mã đơn CM- để khách nhận diện đơn đặt vé.
        Booking booking = new Booking();
        booking.setCode(
                "CM-"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 16)
                                .toUpperCase(Locale.ROOT));
        // Liên kết đơn với phiên, chủ đơn và suất chiếu đã được kiểm tra trước đó.
        booking.setHoldId(hold.getId());
        booking.setCustomerId(hold.getCustomerId());
        booking.setShowtimeId(hold.getShowtimeId());
        // Lưu thông tin liên hệ khách nhập cho đơn; thông tin này có thể khác hồ sơ đăng ký.
        booking.setContactName(request.getContactName());
        booking.setContactEmail(request.getContactEmail());
        booking.setContactPhone(request.getContactPhone());
        booking.setNote(request.getNote());
        booking.setSeatCount((short) selected.size());
        // Chốt chính sách ưu đãi và số tiền tại lúc tạo đơn để lưu lại giá đã áp dụng.
        booking.setPricingPolicySnapshot(price.getPolicy());
        booking.setDiscountPercent(price.getDiscountPercent());
        booking.setSubtotal(price.getSubtotal());
        booking.setDiscountAmount(price.getDiscountAmount());
        booking.setTotal(price.getTotal());
        // Lưu đơn và lấy Booking.id trước khi tạo các dòng chi tiết ghế.
        bookings.saveAndFlush(booking);
        // Mỗi ghế có một chi tiết đơn, lưu mã ghế, loại ghế và giá tại thời điểm đặt vé.
        // Thông tin đã lưu giúp lịch sử đơn giữ nguyên khi giá hoặc cấu hình ghế thay đổi.
        for (Seat seat : selected) {
            BookingItem item = new BookingItem();
            item.setBookingId(booking.getId());
            item.setSeatId(seat.getId());
            item.setSeatCodeSnapshot(seat.getCode());
            item.setSeatTypeSnapshot(seat.getSeatType());
            item.setBasePriceSnapshot(showtime.getBasePrice());
            item.setVipSurchargeSnapshot(
                    seat.getSeatType() == SeatType.VIP
                            ? showtime.getVipSurcharge()
                            : BigDecimal.ZERO);
            item.setUnitPriceSnapshot(
                    item.getBasePriceSnapshot().add(item.getVipSurchargeSnapshot()));
            items.save(item);
        }
        // Chuyển phiên sang giai đoạn chờ thanh toán; revision tăng để nhận biết dữ liệu cũ.
        hold.setStatus(HoldStatus.CHECKOUT);
        hold.setRevision(hold.getRevision() + 1);
        return booking;
    }

    public List<Seat> physicalSeats(List<ShowtimeSeat> rows) {
        List<Seat> result = new ArrayList<Seat>();
        // Lấy thông tin ghế gốc như mã ghế, loại ghế để tính giá và lưu chi tiết đơn.
        for (ShowtimeSeat row : rows) {
            result.add(seats.findById(row.getSeatId()).orElseThrow(BusinessException::missing));
        }
        return result;
    }

    /** Gom phiên, đơn và danh sách ghế đã được khóa cho bước thanh toán hoặc hủy đơn. */
    public static final class LockedBooking {
        public final HoldSession hold;
        public final Booking booking;
        public final List<ShowtimeSeat> seats;

        LockedBooking(HoldSession hold, Booking booking, List<ShowtimeSeat> seats) {
            this.hold = hold;
            this.booking = booking;
            this.seats = seats;
        }
    }
}
