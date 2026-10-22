package com.td.cinema.choreography.booking.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "booking")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long value) {
        this.notificationId = value;
    }

    @Column(name = "recipient_account_id", nullable = false)
    private Long recipientAccountId;

    public Long getRecipientAccountId() {
        return recipientAccountId;
    }

    public void setRecipientAccountId(Long value) {
        this.recipientAccountId = value;
    }

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID value) {
        this.eventId = value;
    }

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID value) {
        this.bookingId = value;
    }

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String value) {
        this.content = value;
    }

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime value) {
        this.createdAt = value;
    }

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(OffsetDateTime value) {
        this.readAt = value;
    }
}
