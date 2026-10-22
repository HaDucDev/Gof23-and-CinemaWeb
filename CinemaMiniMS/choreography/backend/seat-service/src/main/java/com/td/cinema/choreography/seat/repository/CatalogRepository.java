package com.td.cinema.choreography.seat.repository;

import com.td.cinema.choreography.seat.dto.projection.MovieView;
import com.td.cinema.choreography.seat.dto.projection.ShowtimeIdView;
import com.td.cinema.choreography.seat.dto.projection.ShowtimeStartView;
import com.td.cinema.choreography.seat.dto.projection.ShowtimeView;
import com.td.cinema.choreography.seat.entity.Movie;
import com.td.cinema.choreography.seat.mapper.RepositoryProjectionMapper;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Transactional(readOnly = true)
public interface CatalogRepository extends JpaRepository<Movie, Long> {

    @Query(
            value = "select 1 from pg_advisory_xact_lock(hashtextextended(:operationKey,0))",
            nativeQuery = true)
    Integer acquireTransactionLock(@Param("operationKey") String operationKey);

    default void lockOperation(String key) {
        acquireTransactionLock(key);
    }

    @Query(
            value =
                    "select to_char(clock_timestamp() AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"')",
            nativeQuery = true)
    String databaseClock();

    default OffsetDateTime currentTime() {
        return OffsetDateTime.parse(databaseClock());
    }

    @Query(
            value =
                    "select st.showtime_id AS \"showtimeId\", st.movie_id AS \"movieId\","
                        + " to_char(st.starts_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startsAt\","
                        + " to_char(st.ends_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"endsAt\","
                        + " to_char(st.room_available_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"roomAvailableAt\","
                        + " st.duration_minutes AS \"durationMinutes\", st.cleanup_minutes AS"
                        + " \"cleanupMinutes\", st.base_price AS \"basePrice\", st.vip_surcharge AS"
                        + " \"vipSurcharge\", to_char(st.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\", m.title AS"
                        + " \"title\" from seat.showtimes st join seat.movies m using(movie_id)"
                        + " where st.showtime_id=:showtimeId",
            nativeQuery = true)
    ShowtimeView loadFindShowtimeDetail(@Param("showtimeId") String showtimeId);

    default Map<String, Object> findShowtimeDetail(String showtimeId) {
        return RepositoryProjectionMapper.required(loadFindShowtimeDetail(showtimeId));
    }

    @Query(
            value =
                    "select to_char(starts_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startsAt\" from"
                        + " seat.showtimes where showtime_id=:showtimeId",
            nativeQuery = true)
    ShowtimeStartView loadFindShowtimeStart(@Param("showtimeId") String showtimeId);

    default Map<String, Object> findShowtimeStart(String showtimeId) {
        return RepositoryProjectionMapper.required(loadFindShowtimeStart(showtimeId));
    }

    @Query(
            value =
                    "select showtime_id AS \"showtimeId\" from seat.showtimes where"
                        + " showtime_id=:showtimeId",
            nativeQuery = true)
    ShowtimeIdView loadRequireShowtime(@Param("showtimeId") String showtimeId);

    default Map<String, Object> requireShowtime(String showtimeId) {
        return RepositoryProjectionMapper.required(loadRequireShowtime(showtimeId));
    }

    @Query(
            value =
                    "select movies.movie_id AS \"movieId\", movies.title AS \"title\","
                        + " movies.duration_minutes AS \"durationMinutes\", movies.description AS"
                        + " \"description\" from seat.movies order by title",
            nativeQuery = true)
    List<MovieView> loadFindMovies();

    default List<Map<String, Object>> findMovies() {
        return RepositoryProjectionMapper.rows(loadFindMovies());
    }

    @Query(
            value =
                    "select st.showtime_id AS \"showtimeId\", st.movie_id AS \"movieId\","
                        + " to_char(st.starts_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startsAt\","
                        + " to_char(st.ends_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"endsAt\","
                        + " to_char(st.room_available_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"roomAvailableAt\","
                        + " st.duration_minutes AS \"durationMinutes\", st.cleanup_minutes AS"
                        + " \"cleanupMinutes\", st.base_price AS \"basePrice\", st.vip_surcharge AS"
                        + " \"vipSurcharge\", to_char(st.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\", m.title AS"
                        + " \"title\" from seat.showtimes st join seat.movies m using(movie_id)"
                        + " where (CAST(:movieId AS bigint) is null or"
                        + " st.movie_id=CAST(:movieFilter AS bigint)) and (CAST(:date AS date) is"
                        + " null or (st.starts_at at time zone"
                        + " 'Asia/Ho_Chi_Minh')::date=CAST(:dateFilter AS date)) order by"
                        + " st.starts_at limit 500",
            nativeQuery = true)
    List<ShowtimeView> loadFindShowtimes(
            @Param("movieId") Long movieId,
            @Param("movieFilter") Long movieFilter,
            @Param("date") String date,
            @Param("dateFilter") String dateFilter);

    default List<Map<String, Object>> findShowtimes(
            Long movieId, Long movieFilter, String date, String dateFilter) {
        return RepositoryProjectionMapper.rows(
                loadFindShowtimes(movieId, movieFilter, date, dateFilter));
    }
}
