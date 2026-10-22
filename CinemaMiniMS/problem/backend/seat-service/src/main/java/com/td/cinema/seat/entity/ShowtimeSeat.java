package com.td.cinema.seat.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "showtime_seats", schema = "seat")
public class ShowtimeSeat {
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

    @Column(name = "showtime_id", nullable = false, length = 40)
    private String showtimeId;

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String value) {
        this.showtimeId = value;
    }

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long value) {
        this.seatId = value;
    }

    @Column(name = "status", nullable = false, length = 16)
    private String status = "AVAILABLE";

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    @Column(name = "held_by_booking_id")
    private UUID heldByBookingId;

    public UUID getHeldByBookingId() {
        return heldByBookingId;
    }

    public void setHeldByBookingId(UUID value) {
        this.heldByBookingId = value;
    }

    @Column(name = "held_by_hold_id")
    private UUID heldByHoldId;

    public UUID getHeldByHoldId() {
        return heldByHoldId;
    }

    public void setHeldByHoldId(UUID value) {
        this.heldByHoldId = value;
    }

    @Column(name = "hold_expires_at")
    private OffsetDateTime holdExpiresAt;

    public OffsetDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(OffsetDateTime value) {
        this.holdExpiresAt = value;
    }

    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long value) {
        this.version = value;
    }
}
