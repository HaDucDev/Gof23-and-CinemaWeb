package com.td.cinema.choreography.booking.repository;

import com.td.cinema.choreography.booking.dto.projection.NotificationIdView;
import com.td.cinema.choreography.booking.dto.projection.NotificationView;
import com.td.cinema.choreography.booking.entity.Notification;
import com.td.cinema.choreography.booking.mapper.RepositoryProjectionMapper;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "insert into"
                        + " booking.notifications(recipient_account_id,event_id,booking_id,type,content)"
                        + " values(:accountId,CAST(:eventId AS uuid),CAST(:bookingId AS"
                        + " uuid),:type,:content) on conflict(recipient_account_id,event_id) do"
                        + " nothing",
            nativeQuery = true)
    int insertUnique(
            @Param("accountId") Long accountId,
            @Param("eventId") String eventId,
            @Param("bookingId") String bookingId,
            @Param("type") String type,
            @Param("content") String content);

    @Query(
            value =
                    "select notification_id AS \"notificationId\", CAST(booking_id AS varchar) AS"
                        + " \"bookingId\", type AS \"type\", content AS \"content\","
                        + " to_char(created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\", to_char(read_at"
                        + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"readAt\""
                        + " from booking.notifications where recipient_account_id=:accountId and"
                        + " event_id=CAST(:eventId AS uuid)",
            nativeQuery = true)
    NotificationView loadInserted(
            @Param("accountId") Long accountId, @Param("eventId") String eventId);

    default List<Map<String, Object>> insertIfNew(
            Long accountId, String eventId, String bookingId, String type, String content) {
        if (insertUnique(accountId, eventId, bookingId, type, content) == 0)
            return Collections.emptyList();
        return Collections.singletonList(
                RepositoryProjectionMapper.required(loadInserted(accountId, eventId)));
    }

    @Query(
            value =
                    "select notification_id AS \"notificationId\", CAST(booking_id AS varchar) AS"
                        + " \"bookingId\", type AS \"type\", content AS \"content\","
                        + " to_char(created_at AT TIME ZONE 'UTC',"
                        + " 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"createdAt\", to_char(read_at"
                        + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') AS \"readAt\""
                        + " from booking.notifications where recipient_account_id=:accountId order"
                        + " by created_at desc,notification_id desc limit 200",
            nativeQuery = true)
    List<NotificationView> loadFindByRecipient(@Param("accountId") long accountId);

    default List<Map<String, Object>> findByRecipient(long accountId) {
        return RepositoryProjectionMapper.rows(loadFindByRecipient(accountId));
    }

    @Query(
            value =
                    "select count(*) from booking.notifications where"
                        + " recipient_account_id=:accountId and read_at is null",
            nativeQuery = true)
    Long countUnread(@Param("accountId") long accountId);

    @Query(
            value =
                    "select notification_id AS \"notificationId\" from booking.notifications where"
                        + " notification_id=:notificationId and recipient_account_id=:accountId for"
                        + " update",
            nativeQuery = true)
    NotificationIdView loadLockOwnedNotification(
            @Param("notificationId") long notificationId, @Param("accountId") long accountId);

    default Map<String, Object> lockOwnedNotification(long notificationId, long accountId) {
        return RepositoryProjectionMapper.required(
                loadLockOwnedNotification(notificationId, accountId));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(
            value =
                    "update booking.notifications set read_at=coalesce(read_at,clock_timestamp())"
                        + " where notification_id=:notificationId",
            nativeQuery = true)
    int markRead(@Param("notificationId") long notificationId);
}
