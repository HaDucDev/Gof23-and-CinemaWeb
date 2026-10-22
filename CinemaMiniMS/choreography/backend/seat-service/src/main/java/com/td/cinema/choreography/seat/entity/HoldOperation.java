package com.td.cinema.choreography.seat.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "hold_operations", schema = "seat")
public class HoldOperation {
    @Id
    @Column(name = "operation_key", nullable = false, length = 128)
    private String operationKey;

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String value) {
        this.operationKey = value;
    }

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        this.accountId = value;
    }

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String value) {
        this.requestHash = value;
    }

    @Column(name = "hold_id", nullable = false)
    private UUID holdId;

    public UUID getHoldId() {
        return holdId;
    }

    public void setHoldId(UUID value) {
        this.holdId = value;
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
