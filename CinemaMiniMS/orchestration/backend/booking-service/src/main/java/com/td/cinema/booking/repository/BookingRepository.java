package com.td.cinema.booking.repository;

import com.td.cinema.booking.dto.projection.AdminBookingSummaryView;
import com.td.cinema.booking.dto.projection.BookingItemView;
import com.td.cinema.booking.dto.projection.BookingView;
import com.td.cinema.booking.dto.projection.ExpiringBookingView;
import com.td.cinema.booking.dto.projection.IdempotencyRequestView;
import com.td.cinema.booking.dto.projection.MyBookingSummaryView;
import com.td.cinema.booking.entity.Booking;
import com.td.cinema.booking.mapper.RepositoryProjectionMapper;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Transactional(readOnly = true)
public interface BookingRepository extends JpaRepository<Booking, UUID> {

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
                    "select CAST(bookings.booking_id AS varchar) AS \"bookingId\","
                        + " bookings.idempotency_key AS \"idempotencyKey\", bookings.request_hash"
                        + " AS \"requestHash\", bookings.account_id AS \"accountId\","
                        + " bookings.customer_id AS \"customerId\", bookings.showtime_id AS"
                        + " \"showtimeId\", CAST(bookings.hold_id AS varchar) AS \"holdId\","
                        + " bookings.hold_revision AS \"holdRevision\", bookings.status AS"
                        + " \"status\", bookings.amount AS \"amount\", bookings.subtotal AS"
                        + " \"subtotal\", bookings.discount_percent AS \"discountPercent\","
                        + " bookings.customer_type AS \"customerType\", bookings.movie_title AS"
                        + " \"movieTitle\", to_char(bookings.starts_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startsAt\","
                        + " to_char(bookings.expires_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"expiresAt\","
                        + " bookings.ticket_code AS \"ticketCode\", CAST(bookings.payment_id AS"
                        + " varchar) AS \"paymentId\", bookings.payment_method AS"
                        + " \"paymentMethod\", CAST(bookings.operation_id AS varchar) AS"
                        + " \"operationId\", bookings.contact_name AS \"contactName\","
                        + " bookings.contact_email AS \"contactEmail\", bookings.contact_phone AS"
                        + " \"contactPhone\", bookings.note AS \"note\", bookings.failure_reason AS"
                        + " \"failureReason\", to_char(bookings.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " to_char(bookings.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " booking.bookings where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    BookingView loadFindBooking(@Param("bookingId") String bookingId);

    default Map<String, Object> findBooking(String bookingId) {
        return RepositoryProjectionMapper.required(loadFindBooking(bookingId));
    }

    @Query(
            value =
                    "select CAST(bookings.booking_id AS varchar) AS \"bookingId\","
                        + " bookings.idempotency_key AS \"idempotencyKey\", bookings.request_hash"
                        + " AS \"requestHash\", bookings.account_id AS \"accountId\","
                        + " bookings.customer_id AS \"customerId\", bookings.showtime_id AS"
                        + " \"showtimeId\", CAST(bookings.hold_id AS varchar) AS \"holdId\","
                        + " bookings.hold_revision AS \"holdRevision\", bookings.status AS"
                        + " \"status\", bookings.amount AS \"amount\", bookings.subtotal AS"
                        + " \"subtotal\", bookings.discount_percent AS \"discountPercent\","
                        + " bookings.customer_type AS \"customerType\", bookings.movie_title AS"
                        + " \"movieTitle\", to_char(bookings.starts_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startsAt\","
                        + " to_char(bookings.expires_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"expiresAt\","
                        + " bookings.ticket_code AS \"ticketCode\", CAST(bookings.payment_id AS"
                        + " varchar) AS \"paymentId\", bookings.payment_method AS"
                        + " \"paymentMethod\", CAST(bookings.operation_id AS varchar) AS"
                        + " \"operationId\", bookings.contact_name AS \"contactName\","
                        + " bookings.contact_email AS \"contactEmail\", bookings.contact_phone AS"
                        + " \"contactPhone\", bookings.note AS \"note\", bookings.failure_reason AS"
                        + " \"failureReason\", to_char(bookings.created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\","
                        + " to_char(bookings.updated_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"updatedAt\" from"
                        + " booking.bookings where booking_id=CAST(:bookingId AS uuid) for update",
            nativeQuery = true)
    BookingView loadLockBooking(@Param("bookingId") String bookingId);

    default Map<String, Object> lockBooking(String bookingId) {
        return RepositoryProjectionMapper.required(loadLockBooking(bookingId));
    }

    @Query(
            value =
                    "select idempotency_requests.id AS \"id\", idempotency_requests.request_key AS"
                        + " \"requestKey\", idempotency_requests.operation_type AS"
                        + " \"operationType\", idempotency_requests.payload_fingerprint AS"
                        + " \"payloadFingerprint\", idempotency_requests.account_id AS"
                        + " \"accountId\", CAST(idempotency_requests.booking_id AS varchar) AS"
                        + " \"bookingId\", idempotency_requests.response_status AS"
                        + " \"responseStatus\", CAST(idempotency_requests.response_body AS text) AS"
                        + " \"responseBody\", to_char(idempotency_requests.created_at AT TIME ZONE"
                        + " 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\" from"
                        + " booking.idempotency_requests where request_key=:requestKey",
            nativeQuery = true)
    IdempotencyRequestView loadFindOperation(@Param("requestKey") String requestKey);

    default Map<String, Object> findOperation(String requestKey) {
        return RepositoryProjectionMapper.optional(loadFindOperation(requestKey));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " booking.idempotency_requests(request_key,operation_type,payload_fingerprint,account_id,booking_id,response_status)"
                        + " values(:requestKey,:operationType,:fingerprint,:accountId,CAST(:bookingId"
                        + " AS uuid),202)",
            nativeQuery = true)
    int insertOperation(
            @Param("requestKey") String requestKey,
            @Param("operationType") String operationType,
            @Param("fingerprint") String fingerprint,
            @Param("accountId") long accountId,
            @Param("bookingId") String bookingId);

    @Query(
            value =
                    "select seat_id AS \"seatId\", seat_no_snapshot AS \"seatNoSnapshot\","
                        + " seat_type_snapshot AS \"seatTypeSnapshot\", base_price_snapshot AS"
                        + " \"basePriceSnapshot\", vip_surcharge_snapshot AS"
                        + " \"vipSurchargeSnapshot\", unit_price_snapshot AS \"unitPriceSnapshot\""
                        + " from booking.booking_items where booking_id=CAST(:bookingId AS uuid)"
                        + " order by seat_no_snapshot",
            nativeQuery = true)
    List<BookingItemView> loadFindItems(@Param("bookingId") String bookingId);

    default List<Map<String, Object>> findItems(String bookingId) {
        return RepositoryProjectionMapper.rows(loadFindItems(bookingId));
    }

    @Query(
            value =
                    "select CAST(booking_id AS varchar) AS \"bookingId\", showtime_id AS"
                        + " \"showtimeId\", movie_title AS \"movieTitle\", to_char(starts_at AT"
                        + " TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startsAt\","
                        + " status AS \"status\", amount AS \"amount\", ticket_code AS"
                        + " \"ticketCode\", to_char(created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\" from"
                        + " booking.bookings where account_id=:accountId order by created_at desc"
                        + " limit 200",
            nativeQuery = true)
    List<MyBookingSummaryView> loadFindByAccount(@Param("accountId") long accountId);

    default List<Map<String, Object>> findByAccount(long accountId) {
        return RepositoryProjectionMapper.rows(loadFindByAccount(accountId));
    }

    @Query(
            value =
                    "select CAST(booking_id AS varchar) AS \"bookingId\", account_id AS"
                            + " \"accountId\", showtime_id AS \"showtimeId\", movie_title AS"
                            + " \"movieTitle\", to_char(starts_at AT TIME ZONE 'UTC',"
                            + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"startsAt\", status AS"
                            + " \"status\", amount AS \"amount\", ticket_code AS \"ticketCode\","
                            + " to_char(created_at AT TIME ZONE 'UTC',"
                            + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\" from"
                            + " booking.bookings order by created_at desc limit 200",
            nativeQuery = true)
    List<AdminBookingSummaryView> loadFindRecentBookings();

    default List<Map<String, Object>> findRecentBookings() {
        return RepositoryProjectionMapper.rows(loadFindRecentBookings());
    }

    @Transactional
    default int insertBooking(
            String bookingId,
            String requestKey,
            String fingerprint,
            long accountId,
            Object customerId,
            String showtimeId,
            String holdId,
            long holdRevision,
            BigDecimal total,
            BigDecimal subtotal,
            int discountPercent,
            String customerType,
            String movieTitle,
            OffsetDateTime startsAt,
            OffsetDateTime expiresAt,
            String contactName,
            String contactEmail,
            String contactPhone,
            String note) {
        Booking value = new Booking();
        value.setBookingId(UUID.fromString(bookingId));
        value.setIdempotencyKey(requestKey);
        value.setRequestHash(fingerprint);
        value.setAccountId(accountId);
        value.setCustomerId(customerId == null ? null : ((Number) customerId).longValue());
        value.setShowtimeId(showtimeId);
        value.setHoldId(UUID.fromString(holdId));
        value.setHoldRevision(holdRevision);
        value.setStatus("CREATING");
        value.setAmount(total);
        value.setSubtotal(subtotal);
        value.setDiscountPercent((short) discountPercent);
        value.setCustomerType(customerType);
        value.setMovieTitle(movieTitle);
        value.setStartsAt(startsAt);
        value.setExpiresAt(expiresAt);
        value.setContactName(contactName);
        value.setContactEmail(contactEmail);
        value.setContactPhone(contactPhone);
        value.setNote(note);
        saveAndFlush(value);
        return 1;
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " booking.booking_items(booking_id,seat_id,seat_no_snapshot,seat_type_snapshot,base_price_snapshot,vip_surcharge_snapshot,unit_price_snapshot)"
                        + " values(CAST(:bookingId AS"
                        + " uuid),:seatId,:seatNo,:seatType,:basePrice,:vipSurcharge,:unitPrice)",
            nativeQuery = true)
    int insertItem(
            @Param("bookingId") String bookingId,
            @Param("seatId") long seatId,
            @Param("seatNo") String seatNo,
            @Param("seatType") String seatType,
            @Param("basePrice") BigDecimal basePrice,
            @Param("vipSurcharge") BigDecimal vipSurcharge,
            @Param("unitPrice") BigDecimal unitPrice);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update booking.bookings set"
                        + " status='PENDING',hold_revision=:holdRevision,updated_at=clock_timestamp()"
                        + " where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    int markPending(@Param("holdRevision") long holdRevision, @Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update booking.bookings set status=:status,operation_id=CAST(:operationId AS"
                        + " uuid),payment_method=coalesce(CAST(:paymentMethod AS"
                        + " varchar),payment_method),failure_reason=CAST(:reason AS"
                        + " text),updated_at=clock_timestamp() where booking_id=CAST(:bookingId AS"
                        + " uuid)",
            nativeQuery = true)
    int beginOperation(
            @Param("status") String status,
            @Param("operationId") String operationId,
            @Param("paymentMethod") String paymentMethod,
            @Param("reason") String reason,
            @Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update booking.bookings set payment_id=CAST(:paymentId AS"
                        + " uuid),updated_at=clock_timestamp() where booking_id=CAST(:bookingId AS"
                        + " uuid)",
            nativeQuery = true)
    int attachPayment(@Param("paymentId") String paymentId, @Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update booking.bookings set status='COMPENSATING',updated_at=clock_timestamp()"
                            + " where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    int markCompensating(@Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update booking.bookings set status=:status,ticket_code=CAST(:ticketCode AS"
                        + " varchar),failure_reason=CAST(:reason AS"
                        + " text),updated_at=clock_timestamp() where booking_id=CAST(:bookingId AS"
                        + " uuid)",
            nativeQuery = true)
    int finishBooking(
            @Param("status") String status,
            @Param("ticketCode") String ticketCode,
            @Param("reason") String reason,
            @Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update booking.bookings set status='EXPIRED',failure_reason='Het han giu"
                        + " ghe',updated_at=clock_timestamp() where booking_id=CAST(:bookingId AS"
                        + " uuid)",
            nativeQuery = true)
    int markExpired(@Param("bookingId") String bookingId);

    @Query(
            value =
                    "select CAST(booking_id AS varchar) AS \"bookingId\", CAST(hold_id AS varchar)"
                        + " AS \"holdId\", account_id AS \"accountId\" from booking.bookings where"
                        + " status in('PENDING','CREATING') and expires_at<=clock_timestamp() limit"
                        + " 50",
            nativeQuery = true)
    List<ExpiringBookingView> loadFindExpiringBookings();

    default List<Map<String, Object>> findExpiringBookings() {
        return RepositoryProjectionMapper.rows(loadFindExpiringBookings());
    }
}
