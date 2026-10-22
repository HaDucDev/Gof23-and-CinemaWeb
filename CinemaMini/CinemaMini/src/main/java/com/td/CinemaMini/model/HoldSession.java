package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.HoldStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Phiên giữ các ghế của một khách trong một suất chiếu trước khi hoàn tất thanh toán.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "hold_session")
public class HoldSession {
    /** ID phiên giữ ghế trong bảng hold_session. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Mã UUID của phiên; trình duyệt gửi lại cùng ID phiên để service kiểm tra yêu cầu giữ hoặc trả
     * ghế.
     */
    @Column(name = "token", nullable = false)
    private UUID token = UUID.randomUUID();

    /** ID hồ sơ khách đang sở hữu phiên giữ ghế, tương ứng Customer.id. */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** ID suất chiếu mà phiên đang giữ ghế, tương ứng Showtime.id. */
    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    /** Trạng thái phiên: đang chọn ghế, chờ thanh toán, đã dùng, đã trả ghế hoặc hết hạn. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 16)
    private HoldStatus status = HoldStatus.ACTIVE;

    /** Thời điểm bắt đầu giữ ghế của phiên. */
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    /**
     * Hạn giữ ghế dùng chung cho cả phiên: tối đa 5 phút từ startedAt và không muộn hơn giờ chiếu.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Số phiên bản dữ liệu của phiên giữ ghế. Service tăng số này khi phiên thay đổi và so với số
     * trình duyệt gửi lên để phát hiện yêu cầu dùng dữ liệu cũ.
     */
    @Column(name = "revision", nullable = false)
    private Long revision = 0L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public HoldStatus getStatus() {
        return status;
    }

    public void setStatus(HoldStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }
}
