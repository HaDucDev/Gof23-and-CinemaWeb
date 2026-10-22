package com.td.cinema.booking.dto.projection;

import java.math.BigDecimal;

public interface MyBookingSummaryView extends ProjectionRow {
    String getBookingId();

    String getShowtimeId();

    String getMovieTitle();

    String getStartsAt();

    String getStatus();

    BigDecimal getAmount();

    String getTicketCode();

    String getCreatedAt();
}
