package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.PaymentMethod;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Một giao dịch thu hoặc hoàn tiền của đơn vé; giao dịch hoàn được thêm thành dòng riêng.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "payment")
public class Payment {
    /** ID giao dịch trong bảng payment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** ID đơn vé được thu hoặc hoàn tiền, tương ứng Booking.id. */
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    /** Loại giao dịch: PAYMENT là thu tiền, REFUND là hoàn tiền. */
    @Column(name = "operation_type", nullable = false, length = 16)
    private String operationType;

    /**
     * Số tiền giao dịch tính bằng đồng: dương khi thu, âm khi hoàn. Dòng hoàn lưu số tiền âm của
     * giao dịch thu gốc.
     */
    @Column(name = "amount", nullable = false, precision = 14, scale = 0)
    private BigDecimal amount;

    /** Mã đơn vị tiền tệ; hệ thống hiện dùng VND. */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "VND";

    /** Phương thức dùng để thanh toán hoặc hoàn tiền, như MOMO, VISA hoặc CASH. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "payment_method", nullable = false, length = 24)
    private PaymentMethod paymentMethod;

    /** Kết quả giao dịch; phiên bản mô phỏng hiện chỉ ghi SUCCESS cho giao dịch thành công. */
    @Column(name = "status", nullable = false, length = 16)
    private String status = "SUCCESS";

    /**
     * ID giao dịch thu gốc khi dòng này là hoàn tiền, tương ứng Payment.id; null đối với dòng thu
     * tiền.
     */
    @Column(name = "original_payment_id")
    private Long originalPaymentId;

    /**
     * Khóa của yêu cầu thu hoặc hoàn tiền; dùng cùng createdBy để tránh ghi trùng thao tác của một
     * tài khoản.
     */
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    /**
     * Mã giao dịch do bộ mô phỏng thanh toán trả về; dùng nhận diện riêng từng lần thu hoặc hoàn.
     */
    @Column(name = "reference_code", nullable = false, length = 100)
    private String referenceCode;

    /** ID tài khoản thực hiện thao tác thu hoặc hoàn tiền, tương ứng UserAccount.id. */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /** Thời điểm giao dịch được ghi nhận. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * Lý do ghi giao dịch: BOOKING_PAYMENT khi thanh toán vé, CUSTOMER_CANCELLED khi hoàn do khách
     * hủy.
     */
    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOriginalPaymentId() {
        return originalPaymentId;
    }

    public void setOriginalPaymentId(Long originalPaymentId) {
        this.originalPaymentId = originalPaymentId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
