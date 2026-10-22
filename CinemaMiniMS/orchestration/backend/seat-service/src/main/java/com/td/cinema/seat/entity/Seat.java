package com.td.cinema.seat.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "seats", schema = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long value) {
        this.seatId = value;
    }

    @Column(name = "seat_no", nullable = false, length = 3)
    private String seatNo;

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String value) {
        this.seatNo = value;
    }

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "row_label", nullable = false, length = 1, columnDefinition = "char(1)")
    private String rowLabel;

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String value) {
        this.rowLabel = value;
    }

    @Column(name = "seat_number", nullable = false)
    private Short seatNumber;

    public Short getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Short value) {
        this.seatNumber = value;
    }

    @Column(name = "seat_type", nullable = false, length = 16)
    private String seatType;

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String value) {
        this.seatType = value;
    }
}
