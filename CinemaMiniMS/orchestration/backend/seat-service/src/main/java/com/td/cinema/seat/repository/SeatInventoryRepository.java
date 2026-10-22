package com.td.cinema.seat.repository;

import com.td.cinema.seat.dto.projection.InventoryLockView;
import com.td.cinema.seat.dto.projection.SeatAvailabilityView;
import com.td.cinema.seat.entity.ShowtimeSeat;
import com.td.cinema.seat.mapper.RepositoryProjectionMapper;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Transactional(readOnly = true)
public interface SeatInventoryRepository extends JpaRepository<ShowtimeSeat, Long> {

    @Query(
            value =
                    "select i.id AS \"id\", i.showtime_id AS \"showtimeId\", i.seat_id AS"
                        + " \"seatId\", i.status AS \"status\", CAST(i.held_by_booking_id AS"
                        + " varchar) AS \"heldByBookingId\", CAST(i.held_by_hold_id AS varchar) AS"
                        + " \"heldByHoldId\", to_char(i.hold_expires_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"holdExpiresAt\", i.version AS"
                        + " \"version\", s.seat_no AS \"seatNo\" from seat.showtime_seats i join"
                        + " seat.seats s using(seat_id) where i.showtime_id=:showtimeId order by"
                        + " s.seat_no for update of i",
            nativeQuery = true)
    List<InventoryLockView> loadLockShowtimeInventory(@Param("showtimeId") String showtimeId);

    default List<Map<String, Object>> lockShowtimeInventory(String showtimeId) {
        return RepositoryProjectionMapper.rows(loadLockShowtimeInventory(showtimeId));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.showtime_seats set"
                        + " status='AVAILABLE',held_by_hold_id=null,held_by_booking_id=null,hold_expires_at=null,version=version+1"
                        + " where held_by_hold_id=CAST(:holdId AS uuid) and status='HELD'",
            nativeQuery = true)
    int clearHeldInventory(@Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.showtime_seats set status='HELD',held_by_hold_id=CAST(:holdId AS"
                        + " uuid),hold_expires_at=:expiresAt,version=version+1 where"
                        + " showtime_id=:showtimeId and seat_id=:seatId",
            nativeQuery = true)
    int holdSeat(
            @Param("holdId") String holdId,
            @Param("expiresAt") OffsetDateTime expiresAt,
            @Param("showtimeId") String showtimeId,
            @Param("seatId") long seatId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.showtime_seats set held_by_booking_id=CAST(:bookingId AS uuid)"
                        + " where held_by_hold_id=CAST(:holdId AS uuid) and status='HELD'",
            nativeQuery = true)
    int attachBooking(@Param("bookingId") String bookingId, @Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.showtime_seats set"
                        + " status='SOLD',hold_expires_at=null,version=version+1 where"
                        + " held_by_hold_id=CAST(:holdId AS uuid) and"
                        + " held_by_booking_id=CAST(:bookingId AS uuid) and status='HELD'",
            nativeQuery = true)
    int sellHeldSeats(@Param("holdId") String holdId, @Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.showtime_seats set"
                        + " status='AVAILABLE',held_by_hold_id=null,held_by_booking_id=null,hold_expires_at=null,version=version+1"
                        + " where held_by_hold_id=CAST(:holdId AS uuid) and status=:expectedStatus",
            nativeQuery = true)
    int clearInventory(
            @Param("holdId") String holdId, @Param("expectedStatus") String expectedStatus);

    @Query(
            value =
                    "select s.seat_id AS \"seatId\", s.seat_no AS \"seatNo\", s.seat_type AS"
                        + " \"seatType\", i.status AS \"status\", to_char(i.hold_expires_at AT TIME"
                        + " ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"holdExpiresAt\","
                        + " i.version AS \"version\", coalesce(h.account_id=:accountId,false) AS"
                        + " \"heldByMe\", st.base_price AS \"basePrice\", case when"
                        + " s.seat_type='VIP' then st.vip_surcharge else 0 end AS \"vipSurcharge\""
                        + " from seat.showtime_seats i join seat.seats s using(seat_id) join"
                        + " seat.showtimes st using(showtime_id) left join seat.seat_holds h on"
                        + " h.hold_id=i.held_by_hold_id where i.showtime_id=:showtimeId order by"
                        + " s.seat_no",
            nativeQuery = true)
    List<SeatAvailabilityView> loadFindInventory(
            @Param("accountId") long accountId, @Param("showtimeId") String showtimeId);

    default List<Map<String, Object>> findInventory(long accountId, String showtimeId) {
        return RepositoryProjectionMapper.rows(loadFindInventory(accountId, showtimeId));
    }
}
