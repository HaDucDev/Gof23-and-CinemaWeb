package com.td.cinema.payment.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
public class Payment {
    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID value) {
        this.paymentId = value;
    }

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID value) {
        this.bookingId = value;
    }

    @Column(name = "amount", nullable = false, precision = 14, scale = 0)
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    @Column(name = "status", nullable = false, length = 32)
    private String status = "INIT";

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
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

    @Column(name = "payment_method", nullable = false, length = 24)
    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    @Column(name = "simulated_outcome", nullable = false, length = 16)
    private String simulatedOutcome;

    public String getSimulatedOutcome() {
        return simulatedOutcome;
    }

    public void setSimulatedOutcome(String value) {
        this.simulatedOutcome = value;
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
}
