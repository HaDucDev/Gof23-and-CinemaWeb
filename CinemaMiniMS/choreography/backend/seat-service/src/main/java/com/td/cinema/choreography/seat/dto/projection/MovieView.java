package com.td.cinema.choreography.seat.dto.projection;


public interface MovieView extends ProjectionRow {
    Long getMovieId();

    String getTitle();

    Short getDurationMinutes();

    String getDescription();
}
