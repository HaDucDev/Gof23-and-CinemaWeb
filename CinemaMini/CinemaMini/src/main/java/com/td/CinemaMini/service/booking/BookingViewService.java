package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import com.td.CinemaMini.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import static com.td.CinemaMini.dto.Values.map;

/** Ghép đơn, phim, ghế và lịch sử giao dịch để hiển thị cho người mua vé. */
@Service
public class BookingViewService {
    private final BookingRepository bookings;
    private final BookingItemRepository items;
    private final MovieRepository movies;
    private final ShowtimeRepository showtimes;
    private final HoldSessionRepository holds;
    private final PaymentRepository payments;
    private final HoldExpirationService expiration;
    private final DatabaseClock clock;

    public BookingViewService(
            BookingRepository bookings,
            BookingItemRepository items,
            MovieRepository movies,
            ShowtimeRepository showtimes,
            HoldSessionRepository holds,
            PaymentRepository payments,
            HoldExpirationService expiration,
            DatabaseClock clock) {
        this.bookings = bookings;
        this.items = items;
        this.movies = movies;
        this.showtimes = showtimes;
        this.holds = holds;
        this.payments = payments;
        this.expiration = expiration;
        this.clock = clock;
    }

    public Map<String, Object> detail(Booking snapshot) {
        // Cập nhật phiên quá hạn trước khi đọc lại đơn, để màn hình không dùng trạng thái cũ.
        expiration.expire(snapshot.getHoldId());
        Booking booking =
                bookings.findById(snapshot.getId()).orElseThrow(BusinessException::missing);
        Showtime showtime =
                showtimes.findById(booking.getShowtimeId()).orElseThrow(BusinessException::missing);
        // Lấy lịch sử giao dịch theo thời gian để tính tổng thu, tổng hoàn và số tiền còn lại.
        List<Payment> history = payments.findByBookingIdOrderByCreatedAtAsc(booking.getId());
        BigDecimal net = BigDecimal.ZERO, paid = BigDecimal.ZERO, refunded = BigDecimal.ZERO;
        for (Payment payment : history) {
            // Dòng thu mang số tiền dương, dòng hoàn mang số tiền âm; cộng lại được tiền thực thu.
            net = net.add(payment.getAmount());
            if ("PAYMENT".equals(payment.getOperationType())) {
                paid = paid.add(payment.getAmount());
            } else {
                refunded = refunded.add(payment.getAmount().abs());
            }
        }
        // Hiển thị quyền hủy cho đơn chờ thanh toán, hoặc đơn đã xác nhận nhưng phim chưa bắt đầu.
        // Khi khách thực sự hủy, CancellationService sẽ kiểm tra lại điều kiện trong transaction.
        boolean canCancel =
                booking.getStatus() == BookingStatus.PENDING
                        || (booking.getStatus() == BookingStatus.CONFIRMED
                                && clock.now().isBefore(showtime.getStartsAt()));
        // Ghép dữ liệu cho trang chi tiết; thông tin ghế lấy từ chi tiết đơn đã lưu lúc đặt vé.
        return map(
                "booking",
                booking,
                "showtime",
                showtime,
                "movie",
                movies.findById(showtime.getMovieId()).orElseThrow(BusinessException::missing),
                "items",
                items.findByBookingIdOrderByIdAsc(booking.getId()),
                "payments",
                history,
                "netPaid",
                net,
                "paid",
                paid,
                "refunded",
                refunded,
                "hold",
                holds.findById(booking.getHoldId()).orElseThrow(BusinessException::missing),
                "canCancel",
                canCancel,
                "serverTime",
                clock.now());
    }

    public List<Map<String, Object>> list(Customer customer) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        // Chỉ lấy đơn của khách hiện tại và đưa đơn mới nhất lên đầu danh sách.
        for (Booking booking : bookings.findByCustomerIdOrderByCreatedAtDesc(customer.getId())) {
            result.add(detail(booking));
        }
        return result;
    }
}
