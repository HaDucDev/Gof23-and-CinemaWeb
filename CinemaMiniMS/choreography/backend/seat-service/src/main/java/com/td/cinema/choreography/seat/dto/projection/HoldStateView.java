package com.td.cinema.choreography.seat.dto.projection;


public interface HoldStateView extends ProjectionRow {
    String getHoldId();

    String getBookingId();

    Long getAccountId();

    String getShowtimeId();

    String getTokenHash();

    Long getRevision();

    String getStatus();

    String getStartedAt();

    String getExpiresAt();

    String getUpdatedAt();
}
