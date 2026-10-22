package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.SeatType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Một ghế thuộc một đơn đặt vé; lưu mã ghế, loại ghế và giá tại lúc tạo đơn.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "booking_item")
public class BookingItem {
    /** ID dòng chi tiết ghế trong đơn. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** ID đơn chứa ghế này, tương ứng Booking.id. */
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    /** ID ghế trong phòng mà khách đã chọn, tương ứng Seat.id. */
    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    /** Mã ghế được lưu lại lúc tạo đơn, ví dụ A1; dùng để hiển thị vé và lịch sử đơn. */
    @Column(name = "seat_code_snapshot", nullable = false, length = 3)
    private String seatCodeSnapshot;

    /** Loại ghế lúc tạo đơn: STANDARD hoặc VIP; giữ lại để xem thông tin của đơn về sau. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "seat_type_snapshot", nullable = false, length = 16)
    private SeatType seatTypeSnapshot;

    /** Giá cơ bản của một ghế lấy từ suất chiếu lúc tạo đơn, tính bằng đồng. */
    @Column(name = "base_price_snapshot", nullable = false, precision = 14, scale = 0)
    private BigDecimal basePriceSnapshot;

    /** Phụ thu VIP của ghế lúc tạo đơn, tính bằng đồng; bằng 0 nếu là ghế thường. */
    @Column(name = "vip_surcharge_snapshot", nullable = false, precision = 14, scale = 0)
    private BigDecimal vipSurchargeSnapshot;

    /**
     * Giá của ghế trước giảm giá cho khách: basePriceSnapshot cộng vipSurchargeSnapshot, tính bằng
     * đồng.
     */
    @Column(name = "unit_price_snapshot", nullable = false, precision = 14, scale = 0)
    private BigDecimal unitPriceSnapshot;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getSeatCodeSnapshot() {
        return seatCodeSnapshot;
    }

    public void setSeatCodeSnapshot(String seatCodeSnapshot) {
        this.seatCodeSnapshot = seatCodeSnapshot;
    }

    public SeatType getSeatTypeSnapshot() {
        return seatTypeSnapshot;
    }

    public void setSeatTypeSnapshot(SeatType seatTypeSnapshot) {
        this.seatTypeSnapshot = seatTypeSnapshot;
    }

    public BigDecimal getBasePriceSnapshot() {
        return basePriceSnapshot;
    }

    public void setBasePriceSnapshot(BigDecimal basePriceSnapshot) {
        this.basePriceSnapshot = basePriceSnapshot;
    }

    public BigDecimal getVipSurchargeSnapshot() {
        return vipSurchargeSnapshot;
    }

    public void setVipSurchargeSnapshot(BigDecimal vipSurchargeSnapshot) {
        this.vipSurchargeSnapshot = vipSurchargeSnapshot;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }
}
