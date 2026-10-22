package com.td.cinema.booking.dto.projection;


public interface NotificationView extends ProjectionRow {
    Long getNotificationId();

    String getBookingId();

    String getType();

    String getContent();

    String getCreatedAt();

    String getReadAt();
}
