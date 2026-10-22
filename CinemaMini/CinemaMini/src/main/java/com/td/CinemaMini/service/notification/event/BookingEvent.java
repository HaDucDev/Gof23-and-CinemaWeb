package com.td.CinemaMini.service.notification.event;

import com.td.CinemaMini.model.Booking;
import java.math.BigDecimal;
import java.util.UUID;

/** GoF: Observer. Giữ thông tin đơn tại lúc phát sự kiện để các listener tạo thông báo. */
public class BookingEvent {
    /** Mã duy nhất của lần phát sự kiện; dùng tránh lưu thông báo trùng cho cùng người nhận. */
    private final UUID eventId = UUID.randomUUID();
    /** Booking.id của đơn vừa xác nhận, hủy hoặc hết hạn. */
    private final Long bookingId;
    /** Customer.id của chủ đơn; cần tra Customer.accountId để tìm tài khoản nhận thông báo. */
    private final Long customerId;
    /** Payment.id của dòng hoàn tiền, null nếu sự kiện không kèm khoản hoàn. */
    private final Long refundId;
    /** Mã đơn CM- hiển thị trong nội dung thông báo. */
    private final String code;
    /** Loại sự kiện để chọn nội dung xác nhận, hủy hoặc hết hạn. */
    private final String type;
    /** Tổng tiền VND đã chốt trên đơn tại lúc phát sự kiện. */
    private final BigDecimal total;
    /** Số tiền hoàn VND dạng dương để hiển thị, null nếu không có khoản hoàn. */
    private final BigDecimal refundAmount;

    protected BookingEvent(Booking b, String type, Long refundId, BigDecimal refundAmount) {
        // Chụp dữ liệu của đơn vào các trường final để các listener đọc cùng thông tin sự kiện.
        bookingId = b.getId();
        customerId = b.getCustomerId();
        code = b.getCode();
        total = b.getTotal();
        this.type = type;
        this.refundId = refundId;
        this.refundAmount = refundAmount;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getRefundId() {
        return refundId;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
}
