package com.td.CinemaMini.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Một lần chiếu của một bộ phim, gồm giờ chiếu, thời gian dọn phòng và giá vé áp dụng.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "showtime")
public class Showtime {
    /** ID suất chiếu trong bảng showtime. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** ID phim được chiếu ở suất này, tương ứng Movie.id. */
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    /** Thời điểm bắt đầu chiếu phim; khách chỉ được đặt và thanh toán trước thời điểm này. */
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    /**
     * Thời điểm phim kết thúc, bằng startsAt cộng durationMinutes; chưa gồm thời gian dọn phòng.
     */
    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    /**
     * Thời điểm phòng sẵn sàng cho suất tiếp theo, bằng endsAt cộng cleanupMinutes; dùng kiểm tra
     * lịch chồng lấn.
     */
    @Column(name = "room_available_at", nullable = false)
    private Instant roomAvailableAt;

    /** Thời lượng phim tính bằng phút, được lấy từ Movie khi tạo suất và lưu riêng cho suất này. */
    @Column(name = "duration_minutes", nullable = false)
    private Short durationMinutes;

    /** Số phút dọn phòng sau phim; mặc định 20 phút. */
    @Column(name = "cleanup_minutes", nullable = false)
    private Short cleanupMinutes = (short) 20;

    /** Giá cơ bản cho một ghế của suất này trước giảm giá, tính bằng đồng. */
    @Column(name = "base_price", nullable = false, precision = 14, scale = 0)
    private BigDecimal basePrice = new BigDecimal("80000");

    /**
     * Phụ thu cộng thêm cho một ghế VIP của suất này, tính bằng đồng; ghế thường không cộng khoản
     * này.
     */
    @Column(name = "vip_surcharge", nullable = false, precision = 14, scale = 0)
    private BigDecimal vipSurcharge = new BigDecimal("20000");

    /** Thời điểm suất chiếu được tạo. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public Instant getRoomAvailableAt() {
        return roomAvailableAt;
    }

    public void setRoomAvailableAt(Instant roomAvailableAt) {
        this.roomAvailableAt = roomAvailableAt;
    }

    public Short getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Short durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Short getCleanupMinutes() {
        return cleanupMinutes;
    }

    public void setCleanupMinutes(Short cleanupMinutes) {
        this.cleanupMinutes = cleanupMinutes;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getVipSurcharge() {
        return vipSurcharge;
    }

    public void setVipSurcharge(BigDecimal vipSurcharge) {
        this.vipSurcharge = vipSurcharge;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
