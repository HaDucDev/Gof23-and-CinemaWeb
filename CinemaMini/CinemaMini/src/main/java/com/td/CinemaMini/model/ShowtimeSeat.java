package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.SeatStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Trạng thái của một ghế trong một suất chiếu: còn trống, đang giữ, đã bán hoặc không mở bán.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "showtime_seat")
public class ShowtimeSeat {
    /** ID bản ghi trạng thái ghế của suất chiếu; khác với seatId là ID ghế trong phòng. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** ID suất chiếu có ghế này, tương ứng Showtime.id. */
    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    /** ID ghế trong phòng, tương ứng Seat.id; một ghế có các bản ghi riêng cho từng suất. */
    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    /** Trạng thái bán vé của ghế trong suất này: AVAILABLE, HELD, BOOKED hoặc DISABLED. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 16)
    private SeatStatus status = SeatStatus.AVAILABLE;

    /** ID phiên đang giữ ghế khi status là HELD; đặt null khi ghế được trả hoặc đã bán. */
    @Column(name = "hold_id")
    private Long holdId;

    /** ID đơn sở hữu ghế khi status là BOOKED; null nếu ghế chưa được bán trong suất này. */
    @Column(name = "booking_id")
    private Long bookingId;

    /**
     * Phiên bản bản ghi do JPA tự tăng khi cập nhật. Dùng phát hiện cập nhật đồng thời và giúp giao
     * diện bỏ qua cập nhật ghế cũ.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public Long getHoldId() {
        return holdId;
    }

    public void setHoldId(Long holdId) {
        this.holdId = holdId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
