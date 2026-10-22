package com.td.cinema.booking.repository;
import com.td.cinema.booking.dto.projection.BookingTimelineView;
import com.td.cinema.booking.entity.BookingTimeline;
import com.td.cinema.booking.mapper.RepositoryProjectionMapper;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Observation log only; it never schedules or resumes a workflow. */
@Transactional(readOnly = true)
public interface OperationLogRepository extends JpaRepository<BookingTimeline, Long> {
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
