package com.td.cinema.choreography.booking.service;

import com.td.cinema.choreography.booking.repository.NotificationRepository;
import com.td.cinema.choreography.booking.util.Data;
import com.td.cinema.choreography.booking.websocket.NotificationSocket;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/** Lưu thông báo của đơn trong Booking và gửi WebSocket sau commit; REST vẫn đọc được thông báo đã lưu. */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSocket socket;
    private final TransactionTemplate transactions;

    /** Nhận repository thông báo, kênh WebSocket và transaction manager của Booking. */
    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationSocket socket,
            PlatformTransactionManager manager) {
        this.notificationRepository = notificationRepository;
        this.socket = socket;
        this.transactions = new TransactionTemplate(manager);
    }

    /** Thực hiện cập nhật thông báo trong transaction cục bộ của Booking. */
    private <T> T local(Supplier<T> action) {
        return transactions.execute(status -> action.get());
    }

    /**
     * Lưu thông báo cho khách và ADMIN bằng eventId xác định để tránh trùng, rồi đăng ký gửi
     * WebSocket sau commit.
     */
    public void publishBooking(Map<String, Object> booking, String status, List<Long> admins) {
        UUID id = UUID.fromString(Data.text(booking, "bookingId"));
        String type = "BOOKING_" + status;
        UUID event = UUID.nameUUIDFromBytes((id + "|" + type).getBytes(StandardCharsets.UTF_8));
        Set<Long> recipients = new LinkedHashSet<Long>(admins);
        if (booking.get("accountId") != null) recipients.add(Data.number(booking, "accountId"));
        for (Long actor : recipients) {
            List<Map<String, Object>> added =
                    notificationRepository.insertIfNew(
                            actor,
                            event.toString(),
                            id.toString(),
                            type,
                            "Don " + id + ": " + status);
            if (!added.isEmpty()) {
                Map<String, Object> message = added.get(0);
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            /**
                             * Gửi thông báo đã lưu tới WebSocket; lỗi kết nối không làm mất bản
                             * thông báo có thể đọc qua REST.
                             */
                            @Override
                            public void afterCommit() {
                                try {
                                    socket.publish(actor, message);
                                } catch (RuntimeException ignored) {
                                    /* Thông báo đã lưu vẫn đọc được qua REST khi WebSocket lỗi. */
                                }
                            }
                        });
            }
        }
    }

    /** Lấy các thông báo và số chưa đọc của đúng tài khoản. */
    public Map<String, Object> notifications(long actor) {
        return Data.map(
                "items",
                notificationRepository.findByRecipient(actor),
                "unreadCount",
                notificationRepository.countUnread(actor));
    }

    /** Khóa và đánh dấu đã đọc thông báo thuộc người gọi trong một transaction. */
    public Map<String, Object> readNotification(long id, long actor) {
        return local(
                () -> {
                    Map<String, Object> row =
                            notificationRepository.lockOwnedNotification(id, actor);
                    notificationRepository.markRead(id);
                    return row;
                });
    }
}
