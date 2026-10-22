package com.td.cinema.choreography.seat.repository;

import com.td.cinema.choreography.seat.dto.projection.MovieView;
import com.td.cinema.choreography.seat.dto.projection.SeatIdView;
import com.td.cinema.choreography.seat.dto.projection.SeatTemplateView;
import com.td.cinema.choreography.seat.dto.projection.ShowtimeIdView;
import com.td.cinema.choreography.seat.dto.projection.ShowtimeView;
import com.td.cinema.choreography.seat.entity.Showtime;
import com.td.cinema.choreography.seat.mapper.RepositoryProjectionMapper;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Transactional(readOnly = true)
public interface ShowtimeRepository extends JpaRepository<Showtime, String> {

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
                    "select seat_no AS \"seatNo\", seat_type AS \"seatType\" from seat.seats order"
                        + " by seat_no",
            nativeQuery = true)
    List<SeatTemplateView> loadFindTemplateSeats();

    default List<Map<String, Object>> findTemplateSeats() {
        return RepositoryProjectionMapper.rows(loadFindTemplateSeats());
    }

    @Query(
            value =
                    "select movies.movie_id AS \"movieId\", movies.title AS \"title\","
                        + " movies.duration_minutes AS \"durationMinutes\", movies.description AS"
                        + " \"description\" from seat.movies where movie_id=:movieId",
            nativeQuery = true)
    MovieView loadFindMovie(@Param("movieId") long movieId);

    default Map<String, Object> findMovie(long movieId) {
        return RepositoryProjectionMapper.required(loadFindMovie(movieId));
    }

    @Query(
            value =
                    "select showtime_id AS \"showtimeId\" from seat.showtimes where"
                        + " starts_at<:availableAt and room_available_at>:startsAt",
            nativeQuery = true)
    ShowtimeIdView loadFindOverlappingShowtime(
            @Param("availableAt") OffsetDateTime availableAt,
            @Param("startsAt") OffsetDateTime startsAt);

    default Map<String, Object> findOverlappingShowtime(
            OffsetDateTime availableAt, OffsetDateTime startsAt) {
        return RepositoryProjectionMapper.optional(
                loadFindOverlappingShowtime(availableAt, startsAt));
    }

    @Transactional
    default int insertShowtime(
            String showtimeId,
            long movieId,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime availableAt,
            long durationMinutes,
            BigDecimal basePrice,
            BigDecimal vipSurcharge) {
        Showtime value = new Showtime();
        value.setShowtimeId(showtimeId);
        value.setMovieId(movieId);
        value.setStartsAt(startsAt);
        value.setEndsAt(endsAt);
        value.setRoomAvailableAt(availableAt);
        value.setDurationMinutes((short) durationMinutes);
        value.setCleanupMinutes((short) 20);
        value.setBasePrice(basePrice);
        value.setVipSurcharge(vipSurcharge);
        saveAndFlush(value);
        return 1;
    }

    @Query(
            value =
                    "select seat_id AS \"seatId\", seat_no AS \"seatNo\" from seat.seats order by"
                        + " seat_no",
            nativeQuery = true)
    List<SeatIdView> loadFindPhysicalSeats();

    default List<Map<String, Object>> findPhysicalSeats() {
        return RepositoryProjectionMapper.rows(loadFindPhysicalSeats());
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into seat.showtime_seats(showtime_id,seat_id,status)"
                        + " values(:showtimeId,:seatId,:status)",
            nativeQuery = true)
    int insertInventory(
            @Param("showtimeId") String showtimeId,
            @Param("seatId") long seatId,
            @Param("status") String status);

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
                        + " where showtime_id=:showtimeId",
            nativeQuery = true)
    ShowtimeView loadFindShowtimeDetail(@Param("showtimeId") String showtimeId);

    default Map<String, Object> findShowtimeDetail(String showtimeId) {
        return RepositoryProjectionMapper.required(loadFindShowtimeDetail(showtimeId));
    }
}
