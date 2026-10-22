package com.td.cinema.seat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "movies", schema = "seat")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long value) {
        this.movieId = value;
    }

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    @Column(name = "duration_minutes", nullable = false)
    private Short durationMinutes;

    public Short getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Short value) {
        this.durationMinutes = value;
    }

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description = "";

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }
}
