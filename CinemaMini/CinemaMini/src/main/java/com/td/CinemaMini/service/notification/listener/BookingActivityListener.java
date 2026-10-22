package com.td.CinemaMini.service.notification.listener;

import com.td.CinemaMini.service.notification.NotificationService;
import com.td.CinemaMini.service.notification.event.BookingEvent;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

/** GoF: Observer. Nhận sự kiện đặt vé để lưu thông báo hoạt động cho các tài khoản ADMIN. */
@Component
public class BookingActivityListener {
    private static final Logger log = LoggerFactory.getLogger(BookingActivityListener.class);
    private final NotificationService service;

    public BookingActivityListener(NotificationService service) {
        this.service = service;
    }

    // Chỉ nhận sự kiện sau khi transaction đặt vé lưu thành công; không nhận kết quả bị rollback.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBooking(BookingEvent event) {
        try {
            // NotificationService mở transaction mới để lưu thông báo riêng cho từng admin.
            service.notifyAdmins(event);
        } catch (RuntimeException error) {
            // Nếu lưu thông báo thất bại thì ghi log; kết quả đặt vé đã commit vẫn được giữ.
            log.error("Không lưu được hoạt động admin cho đơn {}", event.getCode(), error);
        }
    }
}
