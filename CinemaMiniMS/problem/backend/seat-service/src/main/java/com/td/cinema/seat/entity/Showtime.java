package com.td.cinema.seat.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "showtimes", schema = "seat")
public class Showtime {
    @Id
    @Column(name = "showtime_id", nullable = false, length = 40)
    private String showtimeId;

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String value) {
        this.showtimeId = value;
    }

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long value) {
        this.movieId = value;
    }

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(OffsetDateTime value) {
        this.startsAt = value;
    }

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(OffsetDateTime value) {
        this.endsAt = value;
    }

    @Column(name = "room_available_at", nullable = false)
    private OffsetDateTime roomAvailableAt;

    public OffsetDateTime getRoomAvailableAt() {
        return roomAvailableAt;
    }

    public void setRoomAvailableAt(OffsetDateTime value) {
        this.roomAvailableAt = value;
    }

    @Column(name = "duration_minutes", nullable = false)
    private Short durationMinutes;

    public Short getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Short value) {
        this.durationMinutes = value;
    }

    @Column(name = "cleanup_minutes", nullable = false)
    private Short cleanupMinutes = (short) 20;

    public Short getCleanupMinutes() {
        return cleanupMinutes;
    }

    public void setCleanupMinutes(Short value) {
        this.cleanupMinutes = value;
    }

    @Column(name = "base_price", nullable = false, precision = 14, scale = 0)
    private BigDecimal basePrice = new BigDecimal("80000");

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal value) {
        this.basePrice = value;
    }

    @Column(name = "vip_surcharge", nullable = false, precision = 14, scale = 0)
    private BigDecimal vipSurcharge = new BigDecimal("20000");

    public BigDecimal getVipSurcharge() {
        return vipSurcharge;
    }

    public void setVipSurcharge(BigDecimal value) {
        this.vipSurcharge = value;
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
