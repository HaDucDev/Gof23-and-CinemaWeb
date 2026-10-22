package com.td.cinema.booking.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings", schema = "booking")
public class Booking {
    @Id
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID value) {
        this.bookingId = value;
    }

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String value) {
        this.idempotencyKey = value;
    }

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String value) {
        this.requestHash = value;
    }

    @Column(name = "account_id")
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        this.accountId = value;
    }

    @Column(name = "customer_id")
    private Long customerId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long value) {
        this.customerId = value;
    }

    @Column(name = "showtime_id", nullable = false, length = 40)
    private String showtimeId;

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String value) {
        this.showtimeId = value;
    }

    @Column(name = "hold_id", nullable = false)
    private UUID holdId;

    public UUID getHoldId() {
        return holdId;
    }

    public void setHoldId(UUID value) {
        this.holdId = value;
    }

    @Column(name = "hold_revision", nullable = false)
    private Long holdRevision = 0L;

    public Long getHoldRevision() {
        return holdRevision;
    }

    public void setHoldRevision(Long value) {
        this.holdRevision = value;
    }

    @Column(name = "status", nullable = false, length = 32)
    private String status = "CREATING";

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    @Column(name = "amount", nullable = false, precision = 14, scale = 0)
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 0)
    private BigDecimal subtotal;

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal value) {
        this.subtotal = value;
    }

    @Column(name = "discount_percent", nullable = false)
    private Short discountPercent;

    public Short getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Short value) {
        this.discountPercent = value;
    }

    @Column(name = "customer_type", nullable = false, length = 16)
    private String customerType;

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String value) {
        this.customerType = value;
    }

    @Column(name = "movie_title", nullable = false, length = 200)
    private String movieTitle;

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String value) {
        this.movieTitle = value;
    }

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(OffsetDateTime value) {
        this.startsAt = value;
    }

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime value) {
        this.expiresAt = value;
    }

    @Column(name = "ticket_code", length = 64)
    private String ticketCode;

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String value) {
        this.ticketCode = value;
    }

    @Column(name = "payment_id")
    private UUID paymentId;

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID value) {
        this.paymentId = value;
    }

    @Column(name = "payment_method", length = 24)
    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    @Column(name = "operation_id")
    private UUID operationId;

    public UUID getOperationId() {
        return operationId;
    }

    public void setOperationId(UUID value) {
        this.operationId = value;
    }

    @Column(name = "contact_name", nullable = false, length = 120)
    private String contactName;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String value) {
        this.contactName = value;
    }

    @Column(name = "contact_email", nullable = false, length = 254)
    private String contactEmail;

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String value) {
        this.contactEmail = value;
    }

    @Column(name = "contact_phone", nullable = false, length = 30)
    private String contactPhone;

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String value) {
        this.contactPhone = value;
    }

    @Column(name = "note", length = 1000)
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String value) {
        this.note = value;
    }

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String value) {
        this.failureReason = value;
    }

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime value) {
        this.createdAt = value;
    }

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime value) {
        this.updatedAt = value;
    }
    @Column(name = "operation_type", length = 24)
    private String operationType;

    @Column(name = "saga_step", length = 32)
    private String sagaStep;

    @Column(name = "saga_status", length = 32)
    private String sagaStatus;

    @Column(name = "saga_error", columnDefinition = "text")
    private String sagaError;

    @Column(name = "saga_version", nullable = false)
    private Long sagaVersion = 0L;

    @Column(name = "saga_created_at")
    private OffsetDateTime sagaCreatedAt;

    @Column(name = "saga_updated_at")
    private OffsetDateTime sagaUpdatedAt;

    @Column(name = "saga_attempt_count", nullable = false)
    private Integer sagaAttemptCount = 0;

    @Column(name = "saga_next_attempt_at")
    private OffsetDateTime sagaNextAttemptAt;

    @Column(name = "saga_lease_token")
    private UUID sagaLeaseToken;

    @Column(name = "saga_lease_until")
    private OffsetDateTime sagaLeaseUntil;
}
