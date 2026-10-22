package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.BookingStatus;
import com.td.CinemaMini.model.enums.CustomerType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Đơn đặt vé của một khách cho một suất chiếu, gồm liên hệ, giá đã tính và trạng thái đơn.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "booking")
public class Booking {
    /** ID của đơn đặt vé trong bảng booking. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Mã đơn hiển thị cho khách và admin, ví dụ CM-...; khác với ID số dùng trong database. */
    @Column(name = "code", nullable = false, length = 40)
    private String code;

    /** ID phiên giữ ghế đã dùng để tạo đơn này, tương ứng HoldSession.id. */
    @Column(name = "hold_id", nullable = false)
    private Long holdId;

    /**
     * ID hồ sơ khách sở hữu đơn, tương ứng Customer.id; dùng để kiểm tra quyền xem và xử lý đơn.
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** ID suất chiếu mà khách đặt vé, tương ứng Showtime.id. */
    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    /** Trạng thái hiện tại của đơn: chờ thanh toán, đã xác nhận, đã hủy hoặc hết hạn. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 16)
    private BookingStatus status = BookingStatus.PENDING;

    /**
     * Tên người liên hệ cho đơn vé, do người đặt vé nhập trên form; có thể khác tên trong hồ sơ.
     */
    @Column(name = "contact_name", nullable = false, length = 120)
    private String contactName;

    /**
     * Email liên hệ do người đặt vé nhập cho đơn này; có thể khác email đăng nhập trong
     * UserAccount.
     */
    @Column(name = "contact_email", nullable = false, length = 254)
    private String contactEmail;

    /** Số điện thoại liên hệ do người đặt vé nhập cho đơn này; có thể để trống. */
    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    /** Ghi chú do người đặt vé nhập cho đơn này, tối đa 1000 ký tự; có thể để trống. */
    @Column(name = "note", length = 1000)
    private String note;

    /** Số ghế trong đơn, từ 1 đến 6; phải khớp số dòng BookingItem của đơn. */
    @Column(name = "seat_count", nullable = false)
    private Short seatCount;

    /**
     * Nhóm khách được dùng để tính giá lúc tạo đơn: khách thường, sinh viên hoặc thành viên. Giữ
     * lại giá trị này để biết đơn đã áp dụng ưu đãi nào, kể cả khi hồ sơ khách thay đổi.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "pricing_policy_snapshot", nullable = false, length = 16)
    private CustomerType pricingPolicySnapshot;

    /** Phần trăm giảm giá được áp dụng cho đơn; ví dụ 10 nghĩa là giảm 10%. */
    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    /** Tổng tiền các ghế trước giảm giá, đã gồm phụ thu VIP, tính bằng đồng. */
    @Column(name = "subtotal", nullable = false, precision = 14, scale = 0)
    private BigDecimal subtotal;

    /** Số tiền được giảm, tính bằng đồng: subtotal trừ total. */
    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 0)
    private BigDecimal discountAmount;

    /** Tổng tiền khách cần thanh toán sau giảm giá và làm tròn đến đồng. */
    @Column(name = "total", nullable = false, precision = 14, scale = 0)
    private BigDecimal total;

    /** Thời điểm tạo đơn đặt vé. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /** Thời điểm đơn được xác nhận sau khi thanh toán thành công; null nếu chưa từng xác nhận. */
    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    /** Thời điểm đơn được hủy; null nếu đơn chưa bị hủy. */
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    /** Thời điểm hệ thống chuyển đơn sang hết hạn; null nếu chưa chuyển sang trạng thái này. */
    @Column(name = "expired_at")
    private Instant expiredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getHoldId() {
        return holdId;
    }

    public void setHoldId(Long holdId) {
        this.holdId = holdId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Short getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Short seatCount) {
        this.seatCount = seatCount;
    }

    public CustomerType getPricingPolicySnapshot() {
        return pricingPolicySnapshot;
    }

    public void setPricingPolicySnapshot(CustomerType pricingPolicySnapshot) {
        this.pricingPolicySnapshot = pricingPolicySnapshot;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }
}
