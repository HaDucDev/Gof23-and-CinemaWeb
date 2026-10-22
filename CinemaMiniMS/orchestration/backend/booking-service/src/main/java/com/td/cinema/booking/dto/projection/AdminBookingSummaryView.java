package com.td.cinema.booking.dto.projection;

import java.math.BigDecimal;

public interface AdminBookingSummaryView extends ProjectionRow {
    String getBookingId();

    Long getAccountId();

    String getShowtimeId();

    String getMovieTitle();

    String getStartsAt();

    String getStatus();

    BigDecimal getAmount();

    String getTicketCode();

    String getCreatedAt();
}
