package com.td.cinema.seat.dto.projection;


public interface MovieView extends ProjectionRow {
    Long getMovieId();

    String getTitle();

    Short getDurationMinutes();

    String getDescription();
}
