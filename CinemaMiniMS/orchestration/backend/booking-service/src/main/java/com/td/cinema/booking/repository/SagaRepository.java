package com.td.cinema.booking.repository;

import com.td.cinema.booking.dto.projection.*;
import com.td.cinema.booking.entity.Booking;
import com.td.cinema.booking.mapper.RepositoryProjectionMapper;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Lưu bước và trạng thái Saga hiện tại trên đơn Booking; bảng timeline lưu lịch sử các mốc xử lý. */
@Transactional(readOnly = true)
public interface SagaRepository extends JpaRepository<Booking, UUID> {
    String DUE_PAID_CANCELLATION = "operation_type='CANCEL_PAID' and status='CANCELLING' "
            + "and saga_step in ('REFUND_PAYMENT','RELEASE_SEAT') "
            + "and saga_status in ('RUNNING','WAITING_RETRY') "
            + "and (saga_next_attempt_at is null or saga_next_attempt_at<=clock_timestamp()) "
            + "and (saga_lease_until is null or saga_lease_until<=clock_timestamp())";

    @Query(
            value = "select saga_step as \"currentStep\", saga_status as \"status\", operation_type as "
                    + "\"operationType\", saga_error as \"lastError\", to_char(saga_updated_at AT TIME ZONE 'UTC', "
                    + "'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') as \"updatedAt\", saga_attempt_count as \"attemptCount\", "
                    + "to_char(saga_next_attempt_at AT TIME ZONE 'UTC','YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') "
                    + "as \"nextAttemptAt\" from booking.bookings where "
                    + "booking_id=CAST(:bookingId AS uuid) and operation_type is not null",
            nativeQuery = true)
    SagaSummaryView loadFindPublicState(@Param("bookingId") String bookingId);

    default Map<String, Object> findPublicState(String bookingId) {
        return RepositoryProjectionMapper.optional(loadFindPublicState(bookingId));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value = "update booking.bookings set "
                    + "saga_step=:step,saga_status='RUNNING',operation_type=:operationType,operation_id=CAST(:operationId"
                    + " AS "
                    + "uuid),saga_error=null,saga_attempt_count=0,saga_next_attempt_at=null,"
                    + "saga_lease_token=null,saga_lease_until=null,"
                    + "saga_version=saga_version+1,saga_created_at=coalesce(saga_created_at,clock_timestamp()),saga_updated_at=clock_timestamp()"
                    + " where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    int beginSaga(@Param("bookingId") String bookingId, @Param("step") String step, @Param("operationType") String operationType, @Param("operationId") String operationId);

    @Query(
            value = "select CAST(booking_id AS varchar) as \"bookingId\", saga_step as \"currentStep\", saga_status "
                    + "as \"status\", operation_type as \"operationType\", CAST(operation_id AS varchar) as "
                    + "\"operationId\", saga_error as \"lastError\", saga_version as \"version\", to_char(saga_created_at"
                    + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') as \"createdAt\", "
                    + "to_char(saga_updated_at AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') as \"updatedAt\" "
                    + ",saga_attempt_count as \"attemptCount\", CAST(saga_lease_token AS varchar) as \"leaseToken\" "
                    + "from booking.bookings where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    SagaStateView loadFindState(@Param("bookingId") String bookingId);

    default Map<String, Object> findState(String bookingId) {
        return RepositoryProjectionMapper.required(loadFindState(bookingId));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value = "update booking.bookings set "
                    + "saga_step=:step,saga_version=saga_version+1,saga_updated_at=clock_timestamp() where "
                    + "booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    int updateStep(@Param("step") String step, @Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value = "update booking.bookings set saga_status='COMPENSATING',saga_updated_at=clock_timestamp() "
                    + "where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    int markCompensating(@Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value = "update booking.bookings set "
                    + "saga_step='DONE',saga_status='COMPLETED',saga_error=null,"
                    + "saga_next_attempt_at=null,saga_lease_token=null,saga_lease_until=null,"
                    + "saga_version=saga_version+1,saga_updated_at=clock_timestamp()"
                    + " where booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    int markCompleted(@Param("bookingId") String bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value = "update booking.bookings set "
                    + "saga_status='FAILED_NEEDS_REVIEW',saga_error=:code,saga_updated_at=clock_timestamp() where "
                    + "booking_id=CAST(:bookingId AS uuid)",
            nativeQuery = true)
    int markUncertain(@Param("code") String code, @Param("bookingId") String bookingId);

    /**
     * Chỉ tìm các đơn đủ điều kiện, chưa cấp quyền xử lý.
     * Câu UPDATE có kiểm tra điều kiện ở hàm claimPaidCancellation bên dưới mới nhận quyền cho một lượt.
     */
    @Query(value = "select CAST(booking_id AS varchar) from booking.bookings where "
            + DUE_PAID_CANCELLATION + " order by saga_next_attempt_at nulls first,saga_created_at limit :batchSize",
            nativeQuery = true)
    List<String> findDuePaidCancellations(@Param("batchSize") int batchSize);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "update booking.bookings set saga_lease_token=CAST(:token AS uuid),"
            + "saga_lease_until=clock_timestamp()+(:leaseMs * interval '1 millisecond'),"
            + "saga_next_attempt_at=null,saga_attempt_count=saga_attempt_count+1,saga_status='RUNNING',"
            + "saga_version=saga_version+1,saga_updated_at=clock_timestamp() "
            + "where booking_id=CAST(:id AS uuid) and " + DUE_PAID_CANCELLATION
            + " and saga_attempt_count<:maxAttempts", nativeQuery = true)
    int claimPaidCancellation(@Param("id") String id, @Param("token") String token,
            @Param("leaseMs") long leaseMs, @Param("maxAttempts") int maxAttempts);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "update booking.bookings set saga_status='FAILED_NEEDS_REVIEW',"
            + "saga_error='RETRY_EXHAUSTED',saga_lease_token=null,saga_lease_until=null,"
            + "saga_next_attempt_at=null,saga_updated_at=clock_timestamp() "
            + "where booking_id=CAST(:id AS uuid) and " + DUE_PAID_CANCELLATION
            + " and saga_attempt_count>=:maxAttempts", nativeQuery = true)
    int exhaustPaidCancellation(@Param("id") String id, @Param("maxAttempts") int maxAttempts);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "update booking.bookings set saga_step='RELEASE_SEAT',saga_status='RUNNING',"
            + "saga_error=null,saga_attempt_count=0,saga_next_attempt_at=clock_timestamp(),"
            + "saga_lease_token=null,saga_lease_until=null,saga_version=saga_version+1,"
            + "saga_updated_at=clock_timestamp() where booking_id=CAST(:id AS uuid) "
            + "and operation_type='CANCEL_PAID' and status='CANCELLING' and saga_step='REFUND_PAYMENT' "
            + "and saga_lease_token=CAST(:token AS uuid)", nativeQuery = true)
    int completeCancellationRefund(@Param("id") String id, @Param("token") String token);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "update booking.bookings set saga_status=:sagaStatus,saga_error=:code,"
            + "saga_next_attempt_at=case when :retry then clock_timestamp()+(:delayMs * interval '1 millisecond') "
            + "else null end,saga_lease_token=null,saga_lease_until=null,"
            + "saga_version=saga_version+1,saga_updated_at=clock_timestamp() "
            + "where booking_id=CAST(:id AS uuid) and operation_type='CANCEL_PAID' "
            + "and status='CANCELLING' and saga_lease_token=CAST(:token AS uuid)", nativeQuery = true)
    int failCancellationAttempt(@Param("id") String id, @Param("token") String token,
            @Param("sagaStatus") String sagaStatus, @Param("code") String code,
            @Param("retry") boolean retry, @Param("delayMs") long delayMs);

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
