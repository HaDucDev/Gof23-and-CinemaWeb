package com.td.CinemaMini.service.notification.listener;

import com.td.CinemaMini.service.notification.NotificationService;
import com.td.CinemaMini.service.notification.event.BookingEvent;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

/** GoF: Observer. Nhận sự kiện đặt vé để lưu thông báo riêng cho chủ đơn. */
@Component
public class ConfirmationNotificationListener {
    private static final Logger log =
            LoggerFactory.getLogger(ConfirmationNotificationListener.class);
    private final NotificationService service;

    public ConfirmationNotificationListener(NotificationService service) {
        this.service = service;
    }

    // Chỉ thông báo đơn đã được lưu thành công; xử lý trong transaction mới của service nhận.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBooking(BookingEvent event) {
        try {
            // Tìm tài khoản của chủ đơn từ Customer.id trong sự kiện rồi lưu thông báo cho tài
            // khoản đó.
            service.notifyCustomer(event);
        } catch (RuntimeException error) {
            // Lỗi thông báo được ghi lại để kiểm tra, không làm mất kết quả đặt vé đã lưu.
            log.error("Không lưu được thông báo cho đơn {}", event.getCode(), error);
        }
    }
}
