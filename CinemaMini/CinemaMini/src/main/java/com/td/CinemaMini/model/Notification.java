package com.td.CinemaMini.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Thông báo trong ứng dụng dành cho một tài khoản khách hoặc admin về một đơn đặt vé.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "notification")
public class Notification {
    /** ID thông báo trong bảng notification. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * ID tài khoản nhận thông báo, tương ứng UserAccount.id; có thể là tài khoản khách hoặc admin.
     */
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    /**
     * Mã UUID của sự kiện tạo ra thông báo. Kết hợp với recipientId để tránh lưu trùng cùng sự kiện
     * cho một người.
     */
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    /** Loại thông báo: BOOKING_CONFIRMED, BOOKING_CANCELLED hoặc BOOKING_EXPIRED. */
    @Column(name = "type", nullable = false, length = 32)
    private String type;

    /** Nội dung thông báo hiển thị cho người nhận, như mã đơn, ghế, số tiền hoặc kết quả hủy vé. */
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /** ID đơn được nhắc tới trong thông báo, tương ứng Booking.id. */
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    /** Thời điểm thông báo được tạo. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /** Thời điểm người nhận đánh dấu đã đọc thông báo; null nghĩa là chưa đọc. */
    @Column(name = "read_at")
    private Instant readAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
