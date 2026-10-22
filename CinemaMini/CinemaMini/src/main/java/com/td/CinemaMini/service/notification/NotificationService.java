package com.td.CinemaMini.service.notification;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.Role;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.booking.DatabaseClock;
import com.td.CinemaMini.service.notification.event.BookingEvent;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Lưu thông báo theo tài khoản nhận và báo trình duyệt tải lại danh sách thông báo. */
@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final CustomerRepository customers;
    private final UserAccountRepository accounts;
    private final BookingRepository bookings;
    private final BookingItemRepository items;
    private final NotificationSocket socket;
    private final DatabaseClock clock;

    public NotificationService(
            NotificationRepository notifications,
            CustomerRepository customers,
            UserAccountRepository accounts,
            BookingRepository bookings,
            BookingItemRepository items,
            NotificationSocket socket,
            DatabaseClock clock) {
        this.notifications = notifications;
        this.customers = customers;
        this.accounts = accounts;
        this.bookings = bookings;
        this.items = items;
        this.socket = socket;
        this.clock = clock;
    }

    /** Lưu thông báo cho người mua vé trong transaction mới sau khi nghiệp vụ đặt vé đã commit. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyCustomer(BookingEvent event) {
        // Đọc đơn và đối chiếu Customer.id trong sự kiện để tránh gửi thông báo sai chủ đơn.
        Booking booking =
                bookings.findById(event.getBookingId()).orElseThrow(BusinessException::missing);
        if (!booking.getCustomerId().equals(event.getCustomerId())) {
            throw BusinessException.forbidden();
        }
        // Từ hồ sơ người mua vé, lấy accountId để tìm tài khoản nhận thông báo.
        Customer customer =
                customers.findById(event.getCustomerId()).orElseThrow(BusinessException::missing);
        // recipient là tài khoản đăng nhập: Notification.recipientId lưu UserAccount.id.
        UserAccount recipient =
                accounts.findById(customer.getAccountId()).orElseThrow(BusinessException::missing);
        store(recipient, event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyAdmins(BookingEvent event) {
        // Mỗi tài khoản ADMIN nhận một bản thông báo riêng để theo dõi hoạt động đặt vé.
        for (UserAccount admin : accounts.findByRole(Role.ADMIN)) {
            store(admin, event);
        }
    }

    private void store(UserAccount recipient, BookingEvent event) {
        // Dùng eventId và tài khoản nhận để tránh lưu lại thông báo cho cùng một sự kiện.
        if (notifications.existsByRecipientIdAndEventId(recipient.getId(), event.getEventId())) {
            return;
        }
        // Lưu nội dung và liên kết tới đơn; thời điểm tạo dùng cùng nguồn giờ database.
        Notification notification = new Notification();
        notification.setRecipientId(recipient.getId());
        notification.setEventId(event.getEventId());
        notification.setType(event.getType());
        notification.setBookingId(event.getBookingId());
        notification.setContent(content(event));
        notification.setCreatedAt(clock.now());
        notifications.saveAndFlush(notification);
        // Chỉ gửi tín hiệu qua WebSocket sau khi bản thông báo đã được lưu thành công.
        AfterCommit.run(() -> socket.changed(recipient.getEmail()));
    }

    private String content(BookingEvent event) {
        NumberFormat money = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
        String prefix = "Đơn " + event.getCode();
        // Đơn hết hạn nhắc khách chọn ghế lại vì thời gian giữ chỗ đã kết thúc.
        if ("BOOKING_EXPIRED".equals(event.getType())) {
            return prefix + " đã hết thời gian giữ chỗ. Vui lòng chọn ghế lại.";
        }
        // Đơn hủy cần phân biệt có hoàn tiền hay chỉ trả ghế của đơn chưa thanh toán.
        if ("BOOKING_CANCELLED".equals(event.getType())) {
            if (event.getRefundId() != null) {
                return prefix
                        + " đã hủy; đã hoàn "
                        + money.format(event.getRefundAmount())
                        + " đồng theo phương thức gốc. Giao dịch hoàn #"
                        + event.getRefundId()
                        + ".";
            }
            return prefix + " đã hủy. Ghế đã được trả lại; không có khoản hoàn tiền.";
        }
        // Thông báo xác nhận dùng mã ghế đã lưu trong chi tiết đơn để đúng với vé khách đã đặt.
        List<String> codes = new ArrayList<String>();
        for (BookingItem item : items.findByBookingIdOrderByIdAsc(event.getBookingId())) {
            codes.add(item.getSeatCodeSnapshot());
        }
        return prefix
                + " đã xác nhận. Ghế "
                + String.join(", ", codes)
                + " — "
                + money.format(event.getTotal())
                + " đồng.";
    }

    @Transactional
    public void markRead(UserAccount actor, Long id) {
        // Lấy thông báo theo Notification.id rồi kiểm tra người nhận là tài khoản đang đăng nhập.
        Notification notification =
                notifications.findById(id).orElseThrow(BusinessException::missing);
        if (!notification.getRecipientId().equals(actor.getId())) {
            throw BusinessException.forbidden();
        }
        // Chỉ ghi thời điểm đọc lần đầu; đọc lại không thay đổi mốc đã lưu.
        if (notification.getReadAt() == null) {
            notification.setReadAt(clock.now());
        }
        // Cập nhật các tab của tài khoản này sau khi lưu trạng thái đã đọc.
        AfterCommit.run(() -> socket.changed(actor.getEmail()));
    }
}
