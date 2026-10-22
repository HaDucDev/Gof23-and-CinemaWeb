package com.td.cinema.seat.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "seat_holds", schema = "seat")
public class SeatHold {
    @Id
    @Column(name = "hold_id", nullable = false)
    private UUID holdId;

    public UUID getHoldId() {
        return holdId;
    }

    public void setHoldId(UUID value) {
        this.holdId = value;
    }

    @Column(name = "booking_id")
    private UUID bookingId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID value) {
        this.bookingId = value;
    }

    @Column(name = "account_id")
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        this.accountId = value;
    }

    @Column(name = "showtime_id", nullable = false, length = 40)
    private String showtimeId;

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String value) {
        this.showtimeId = value;
    }

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String value) {
        this.tokenHash = value;
    }

    @Column(name = "revision", nullable = false)
    private Long revision = 0L;

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long value) {
        this.revision = value;
    }

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ACTIVE";

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    @Column(name = "started_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime startedAt;

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime value) {
        this.startedAt = value;
    }

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime value) {
        this.expiresAt = value;
    }

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime value) {
        this.updatedAt = value;
    }
    @Column(name = "seat_count", nullable = false)
    private Short seatCount;
}
