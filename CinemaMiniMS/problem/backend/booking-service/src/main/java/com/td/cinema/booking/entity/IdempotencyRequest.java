package com.td.cinema.booking.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_requests", schema = "booking")
public class IdempotencyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        this.id = value;
    }

    @Column(name = "request_key", nullable = false, length = 128)
    private String requestKey;

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String value) {
        this.requestKey = value;
    }

    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String value) {
        this.operationType = value;
    }

    @Column(name = "payload_fingerprint", nullable = false, length = 64)
    private String payloadFingerprint;

    public String getPayloadFingerprint() {
        return payloadFingerprint;
    }

    public void setPayloadFingerprint(String value) {
        this.payloadFingerprint = value;
    }

    @Column(name = "account_id")
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        this.accountId = value;
    }

    @Column(name = "booking_id")
    private UUID bookingId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID value) {
        this.bookingId = value;
    }

    @Column(name = "response_status")
    private Short responseStatus;

    public Short getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Short value) {
        this.responseStatus = value;
    }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String value) {
        this.responseBody = value;
    }

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime value) {
        this.createdAt = value;
    }
}
