package com.td.cinema.choreography.booking.repository;
import com.td.cinema.choreography.booking.dto.projection.*;
import com.td.cinema.choreography.booking.entity.Booking;
import com.td.cinema.choreography.booking.mapper.RepositoryProjectionMapper;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Transactional(readOnly = true)
public interface ProgressRepository extends JpaRepository<Booking, UUID> {
    @Modifying @Transactional
    @Query(
            value = "update booking.bookings set operation_type=:kind,operation_id=cast(:operation as "
                    + "uuid),payment_status=null,seat_status=null,last_event=null,progress_reason=null,updated_at=clock_timestamp()"
                    + " where booking_id=cast(:id as uuid)",
            nativeQuery = true)
    int begin(@Param("id") String id, @Param("kind") String kind, @Param("operation") String operation);
    @Query(
            value = "select operation_type as \"operationType\", cast(operation_id as varchar) as \"operationId\", "
                    + "payment_status as \"paymentStatus\", seat_status as \"seatStatus\", last_event as \"lastEvent\", "
                    + "progress_reason as \"reason\" from booking.bookings where booking_id=cast(:id as uuid) and "
                    + "operation_type is not null",
            nativeQuery = true)
    ProgressView loadProgress(@Param("id") String id);
    default Map<String,Object> findPublicState(String id) { return RepositoryProjectionMapper.optional(loadProgress(id)); }
    @Modifying @Transactional
    @Query(
            value = "update booking.bookings set "
                    + "payment_status=:payment,seat_status=:seat,last_event=:event,progress_reason=coalesce(:reason,progress_reason),updated_at=clock_timestamp()"
                    + " where booking_id=cast(:id as uuid)",
            nativeQuery = true)
    int observe(@Param("id") String id, @Param("payment") String payment, @Param("seat") String seat, @Param("event") String event, @Param("reason") String reason);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " booking.booking_timeline(booking_id,service_name,action,result,detail)"
                        + " values(CAST(:bookingId AS"
                        + " uuid),:service,:action,:result,CAST(:detailJson AS jsonb))",
            nativeQuery = true)
    int appendTimeline(
            @Param("bookingId") String bookingId,
            @Param("service") String service,
            @Param("action") String action,
            @Param("result") String result,
            @Param("detailJson") String detailJson);

    @Query(
            value =
                    "select service_name AS \"serviceName\", action AS \"action\", result AS"
                        + " \"result\", to_char(happened_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"happenedAt\", CAST(detail AS"
                        + " text) AS \"detail\" from booking.booking_timeline where"
                        + " booking_id=CAST(:bookingId AS uuid) order by happened_at,timeline_id",
            nativeQuery = true)
    List<BookingTimelineView> loadFindTimeline(@Param("bookingId") String bookingId);

    default List<Map<String, Object>> findTimeline(String bookingId) {
        return RepositoryProjectionMapper.rows(loadFindTimeline(bookingId));
    }
}
