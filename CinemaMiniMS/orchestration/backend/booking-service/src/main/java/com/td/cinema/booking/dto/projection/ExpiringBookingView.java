package com.td.cinema.booking.dto.projection;


public interface ExpiringBookingView extends ProjectionRow {
    String getBookingId();

    String getHoldId();

    Long getAccountId();
}
