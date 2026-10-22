package com.td.cinema.choreography.seat.dto.projection;


public interface InventoryLockView extends ProjectionRow {
    Long getId();

    String getShowtimeId();

    Long getSeatId();

    String getStatus();

    String getHeldByBookingId();

    String getHeldByHoldId();

    String getHoldExpiresAt();

    Long getVersion();

    String getSeatNo();
}
