package com.td.cinema.seat.repository;

import com.td.cinema.seat.dto.projection.ExpiredHoldView;
import com.td.cinema.seat.dto.projection.ExpiredShowtimeView;
import com.td.cinema.seat.dto.projection.HeldSeatPriceView;
import com.td.cinema.seat.dto.projection.HoldStateView;
import com.td.cinema.seat.dto.projection.HoldSummaryView;
import com.td.cinema.seat.dto.projection.OperationView;
import com.td.cinema.seat.entity.SeatHold;
import com.td.cinema.seat.mapper.RepositoryProjectionMapper;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Transactional(readOnly = true)
public interface HoldRepository extends JpaRepository<SeatHold, UUID> {

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
                    "select CAST(seat_holds.hold_id AS varchar) AS \"holdId\","
                        + " CAST(seat_holds.booking_id AS varchar) AS \"bookingId\","
                        + " seat_holds.account_id AS \"accountId\", seat_holds.showtime_id AS"
                        + " \"showtimeId\", seat_holds.token_hash AS \"tokenHash\","
                        + " seat_holds.revision AS \"revision\", seat_holds.status AS \"status\","
                        + " to_char(seat_holds.started_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startedAt\","
                        + " to_char(seat_holds.expires_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"expiresAt\","
                        + " to_char(seat_holds.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " seat.seat_holds where hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    HoldStateView loadFindHold(@Param("holdId") String holdId);

    @Query(
            value =
                    "select CAST(seat_holds.hold_id AS varchar) AS \"holdId\","
                        + " CAST(seat_holds.booking_id AS varchar) AS \"bookingId\","
                        + " seat_holds.account_id AS \"accountId\", seat_holds.showtime_id AS"
                        + " \"showtimeId\", seat_holds.token_hash AS \"tokenHash\","
                        + " seat_holds.revision AS \"revision\", seat_holds.status AS \"status\","
                        + " to_char(seat_holds.started_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startedAt\","
                        + " to_char(seat_holds.expires_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"expiresAt\","
                        + " to_char(seat_holds.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " seat.seat_holds where hold_id=CAST(:holdId AS uuid) for update",
            nativeQuery = true)
    HoldStateView loadFindHoldLocked(@Param("holdId") String holdId);

    default Map<String, Object> findHold(String holdId, boolean lock) {
        return RepositoryProjectionMapper.required(
                lock ? loadFindHoldLocked(holdId) : loadFindHold(holdId));
    }

    @Query(
            value =
                    "select CAST(hold_id AS varchar) AS \"holdId\", CAST(booking_id AS varchar) AS"
                        + " \"bookingId\", account_id AS \"accountId\", showtime_id AS"
                        + " \"showtimeId\", status AS \"status\", revision AS \"revision\","
                        + " to_char(started_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startedAt\","
                        + " to_char(expires_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"expiresAt\","
                        + " to_char(updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " seat.seat_holds where hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    HoldSummaryView loadFindPublicHold(@Param("holdId") String holdId);

    default Map<String, Object> findPublicHold(String holdId) {
        return RepositoryProjectionMapper.required(loadFindPublicHold(holdId));
    }

    @Query(
            value =
                    "select s.seat_id AS \"seatId\", s.seat_no AS \"seatNo\", s.seat_type AS"
                        + " \"seatType\", st.base_price AS \"basePrice\", case when"
                        + " s.seat_type='VIP' then st.vip_surcharge else 0 end AS \"vipSurcharge\""
                        + " from seat.showtime_seats i join seat.seats s using(seat_id) join"
                        + " seat.showtimes st on st.showtime_id=i.showtime_id"
                        + " where i.held_by_hold_id=CAST(:holdId AS uuid) order by"
                        + " s.seat_no",
            nativeQuery = true)
    List<HeldSeatPriceView> loadFindHoldSeats(@Param("holdId") String holdId);

    default List<Map<String, Object>> findHoldSeats(String holdId) {
        return RepositoryProjectionMapper.rows(loadFindHoldSeats(holdId));
    }

    @Query(
            value =
                    "select hold_operations.operation_key AS \"operationKey\","
                        + " hold_operations.account_id AS \"accountId\","
                        + " hold_operations.request_hash AS \"requestHash\","
                        + " CAST(hold_operations.hold_id AS varchar) AS \"holdId\","
                        + " to_char(hold_operations.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\" from"
                        + " seat.hold_operations where operation_key=:operationKey",
            nativeQuery = true)
    OperationView loadFindOperation(@Param("operationKey") String operationKey);

    default Map<String, Object> findOperation(String operationKey) {
        return RepositoryProjectionMapper.optional(loadFindOperation(operationKey));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " seat.hold_operations(operation_key,account_id,request_hash,hold_id)"
                        + " values(:operationKey,:accountId,:fingerprint,CAST(:holdId AS uuid))",
            nativeQuery = true)
    int insertOperation(
            @Param("operationKey") String operationKey,
            @Param("accountId") long accountId,
            @Param("fingerprint") String fingerprint,
            @Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " seat.seat_holds(hold_id,account_id,showtime_id,status,revision,started_at,expires_at,token_hash,seat_count)"
                        + " values(CAST(:holdId AS"
                        + " uuid),:accountId,:showtimeId,'ACTIVE',0,:startedAt,:expiresAt,:tokenHash,:seatCount)",
            nativeQuery = true)
    int insertHold(
            @Param("holdId") String holdId,
            @Param("accountId") long accountId,
            @Param("showtimeId") String showtimeId,
            @Param("startedAt") OffsetDateTime startedAt,
            @Param("expiresAt") OffsetDateTime expiresAt,
            @Param("tokenHash") String tokenHash,
            @Param("seatCount") int seatCount);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.seat_holds set revision=revision+1,updated_at=clock_timestamp()"
                        + " where hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    int incrementRevision(@Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.seat_holds set booking_id=CAST(:bookingId AS"
                        + " uuid),status='CHECKOUT',revision=revision+1,updated_at=clock_timestamp()"
                        + " where hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    int attachBooking(@Param("bookingId") String bookingId, @Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.seat_holds set"
                        + " status='PAYMENT_PROCESSING',updated_at=clock_timestamp() where"
                        + " hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    int markPaymentProcessing(@Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "update seat.seat_holds set seat_count=:seatCount where hold_id=CAST(:holdId AS uuid)", nativeQuery = true)
    int updateSeatCount(@Param("holdId") String holdId, @Param("seatCount") int seatCount);

    @Query(
            value = "select seat_count from seat.seat_holds where hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    Integer expectedSeatCount(@Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.seat_holds set status='CONSUMED',updated_at=clock_timestamp()"
                        + " where hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    int markConsumed(@Param("holdId") String holdId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.seat_holds set"
                        + " status='RELEASED',revision=revision+1,updated_at=clock_timestamp()"
                        + " where hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    int markReleased(@Param("holdId") String holdId);

    @Query(
            value =
                    "select CAST(hold_id AS varchar) AS \"holdId\" from seat.seat_holds where"
                        + " showtime_id=:showtimeId and status in('ACTIVE','CHECKOUT') and"
                        + " expires_at<=clock_timestamp() order by hold_id for update",
            nativeQuery = true)
    List<ExpiredHoldView> loadLockExpiredHolds(@Param("showtimeId") String showtimeId);

    default List<Map<String, Object>> lockExpiredHolds(String showtimeId) {
        return RepositoryProjectionMapper.rows(loadLockExpiredHolds(showtimeId));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update seat.seat_holds set"
                        + " status='EXPIRED',revision=revision+1,updated_at=clock_timestamp() where"
                        + " hold_id=CAST(:holdId AS uuid)",
            nativeQuery = true)
    int markExpired(@Param("holdId") String holdId);

    @Query(
            value =
                    "select distinct showtime_id AS \"showtimeId\" from seat.seat_holds where"
                        + " status in('ACTIVE','CHECKOUT') and expires_at<=clock_timestamp() limit"
                        + " 50",
            nativeQuery = true)
    List<ExpiredShowtimeView> loadFindExpiringShowtimes();

    default List<Map<String, Object>> findExpiringShowtimes() {
        return RepositoryProjectionMapper.rows(loadFindExpiringShowtimes());
    }
}
