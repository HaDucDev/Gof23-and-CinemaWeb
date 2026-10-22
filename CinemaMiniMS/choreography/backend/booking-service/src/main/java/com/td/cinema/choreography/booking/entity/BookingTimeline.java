package com.td.cinema.choreography.booking.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_timeline", schema = "booking")
public class BookingTimeline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeline_id", nullable = false)
    private Long timelineId;

    public Long getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(Long value) {
        this.timelineId = value;
    }

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID value) {
        this.bookingId = value;
    }

    @Column(name = "service_name", nullable = false, length = 64)
    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String value) {
        this.serviceName = value;
    }

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String value) {
        this.action = value;
    }

    @Column(name = "result", nullable = false, length = 32)
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String value) {
        this.result = value;
    }

    @Column(name = "happened_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime happenedAt;

    public OffsetDateTime getHappenedAt() {
        return happenedAt;
    }

    public void setHappenedAt(OffsetDateTime value) {
        this.happenedAt = value;
    }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail", nullable = false, columnDefinition = "jsonb")
    private String detail;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String value) {
        this.detail = value;
    }
}
