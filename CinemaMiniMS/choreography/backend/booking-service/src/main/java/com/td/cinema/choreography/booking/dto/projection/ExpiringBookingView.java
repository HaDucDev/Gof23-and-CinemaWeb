package com.td.cinema.choreography.booking.dto.projection;


public interface ExpiringBookingView extends ProjectionRow {
    String getBookingId();

    String getHoldId();

    Long getAccountId();
}
