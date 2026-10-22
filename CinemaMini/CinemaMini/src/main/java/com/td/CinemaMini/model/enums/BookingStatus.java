package com.td.CinemaMini.model.enums;

/** Các trạng thái của đơn đặt vé, được BookingWorkflow dùng để chọn cách xử lý phù hợp. */
public enum BookingStatus {
    /** Đơn đã tạo, khách chưa hoàn tất thanh toán và ghế vẫn thuộc phiên giữ chỗ. */
    PENDING("Chờ thanh toán"),
    /** Đơn đã thanh toán thành công và các ghế đã được bán cho đơn. */
    CONFIRMED("Đã xác nhận"),
    /** Đơn đã hủy; ghế được trả lại và có giao dịch hoàn nếu đơn đã thanh toán. */
    CANCELLED("Đã hủy"),
    /** Đơn chưa thanh toán nhưng phiên giữ ghế đã hết hạn. */
    EXPIRED("Hết hạn");
    /** Tên trạng thái đơn hiển thị trên giao diện, ví dụ "Chờ thanh toán". */
    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
