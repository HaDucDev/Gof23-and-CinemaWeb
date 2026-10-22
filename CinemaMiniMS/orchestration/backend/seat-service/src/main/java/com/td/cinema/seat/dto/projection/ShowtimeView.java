package com.td.cinema.seat.dto.projection;

import java.math.BigDecimal;

public interface ShowtimeView extends ProjectionRow {
    String getShowtimeId();

    Long getMovieId();

    String getStartsAt();

    String getEndsAt();

    String getRoomAvailableAt();

    Short getDurationMinutes();

    Short getCleanupMinutes();

    BigDecimal getBasePrice();

    BigDecimal getVipSurcharge();

    String getCreatedAt();

    String getTitle();
}
