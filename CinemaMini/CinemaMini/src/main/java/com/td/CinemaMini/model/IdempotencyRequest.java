package com.td.CinemaMini.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Lưu yêu cầu đã xử lý để nhận biết khi trình duyệt gửi lại cùng thao tác và tránh xử lý trùng.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "idempotency_request")
public class IdempotencyRequest {
    /** ID bản ghi theo dõi yêu cầu trong bảng idempotency_request. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * ID tài khoản gửi yêu cầu, tương ứng UserAccount.id; khóa yêu cầu được kiểm tra riêng cho từng
     * tài khoản.
     */
    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    /**
     * Mã nhận diện một lần thao tác do client tạo. Khi gửi lại cùng thao tác, client dùng lại mã
     * này.
     */
    @Column(name = "request_key", nullable = false, length = 128)
    private String requestKey;

    /**
     * Loại thao tác, ví dụ HOLD_SEAT, RELEASE_SEAT, CREATE_BOOKING, PAYMENT hoặc CANCEL_BOOKING.
     */
    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    /**
     * Chuỗi băm SHA-256 của loại thao tác và dữ liệu gửi lên; phát hiện việc dùng lại cùng khóa cho
     * nội dung khác.
     */
    @Column(name = "payload_fingerprint", nullable = false, length = 64)
    private String payloadFingerprint;

    /**
     * Mã kết quả lưu cho yêu cầu; RequestService hiện đặt 200 khi đã xử lý xong, null nếu chưa hoàn
     * tất.
     */
    @Column(name = "response_status")
    private Short responseStatus;

    /** Nội dung kết quả dạng JSON nếu cần lưu để dùng lại; code hiện tại chưa ghi field này. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

    /**
     * Loại bản ghi kết quả: HOLD là phiên giữ ghế, BOOKING là đơn đặt vé; null nếu chưa có kết quả.
     */
    @Column(name = "reference_type", length = 32)
    private String referenceType;

    /**
     * ID phiên giữ ghế hoặc đơn đã xử lý, tùy referenceType; dùng lấy lại kết quả khi yêu cầu được
     * gửi lại.
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Thời điểm tạo bản ghi theo dõi yêu cầu. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getPayloadFingerprint() {
        return payloadFingerprint;
    }

    public void setPayloadFingerprint(String payloadFingerprint) {
        this.payloadFingerprint = payloadFingerprint;
    }

    public Short getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Short responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
