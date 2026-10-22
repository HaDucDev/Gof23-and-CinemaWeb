package com.td.cinema.booking.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "booking_items", schema = "booking")
public class BookingItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long value) {
        this.itemId = value;
    }

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID value) {
        this.bookingId = value;
    }

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long value) {
        this.seatId = value;
    }

    @Column(name = "seat_no_snapshot", nullable = false, length = 3)
    private String seatNoSnapshot;

    public String getSeatNoSnapshot() {
        return seatNoSnapshot;
    }

    public void setSeatNoSnapshot(String value) {
        this.seatNoSnapshot = value;
    }

    @Column(name = "seat_type_snapshot", nullable = false, length = 16)
    private String seatTypeSnapshot;

    public String getSeatTypeSnapshot() {
        return seatTypeSnapshot;
    }

    public void setSeatTypeSnapshot(String value) {
        this.seatTypeSnapshot = value;
    }

    @Column(name = "base_price_snapshot", nullable = false, precision = 14, scale = 0)
    private BigDecimal basePriceSnapshot;

    public BigDecimal getBasePriceSnapshot() {
        return basePriceSnapshot;
    }

    public void setBasePriceSnapshot(BigDecimal value) {
        this.basePriceSnapshot = value;
    }

    @Column(name = "vip_surcharge_snapshot", nullable = false, precision = 14, scale = 0)
    private BigDecimal vipSurchargeSnapshot = new BigDecimal("0");

    public BigDecimal getVipSurchargeSnapshot() {
        return vipSurchargeSnapshot;
    }

    public void setVipSurchargeSnapshot(BigDecimal value) {
        this.vipSurchargeSnapshot = value;
    }

    @Column(name = "unit_price_snapshot", nullable = false, precision = 14, scale = 0)
    private BigDecimal unitPriceSnapshot;

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(BigDecimal value) {
        this.unitPriceSnapshot = value;
    }
}
