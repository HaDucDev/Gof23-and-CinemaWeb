package com.td.cinema.choreography.seat.dto.projection;


public interface HoldSummaryView extends ProjectionRow {
    String getHoldId();

    String getBookingId();

    Long getAccountId();

    String getShowtimeId();

    String getStatus();

    Long getRevision();

    String getStartedAt();

    String getExpiresAt();

    String getUpdatedAt();
}
