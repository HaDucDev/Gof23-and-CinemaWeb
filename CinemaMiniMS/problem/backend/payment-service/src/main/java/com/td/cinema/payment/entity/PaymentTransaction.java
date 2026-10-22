package com.td.cinema.payment.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions", schema = "payment")
@Immutable
public class PaymentTransaction {
    @Id
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID value) {
        this.transactionId = value;
    }

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

    @Column(name = "operation_type", nullable = false, length = 16)
    private String operationType;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String value) {
        this.operationType = value;
    }

    @Column(name = "amount", nullable = false, precision = 14, scale = 0)
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "VND";

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String value) {
        this.currency = value;
    }

    @Column(name = "payment_method", nullable = false, length = 24)
    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    @Column(name = "original_transaction_id")
    private UUID originalTransactionId;

    public UUID getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(UUID value) {
        this.originalTransactionId = value;
    }

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String value) {
        this.idempotencyKey = value;
    }

    @Column(name = "reference_code", nullable = false, length = 100)
    private String referenceCode;

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String value) {
        this.referenceCode = value;
    }

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime value) {
        this.createdAt = value;
    }

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String value) {
        this.reason = value;
    }
}
