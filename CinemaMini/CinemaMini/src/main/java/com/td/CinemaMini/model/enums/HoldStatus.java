package com.td.CinemaMini.model.enums;

/** Các trạng thái của phiên giữ ghế từ lúc chọn ghế đến khi thanh toán, trả ghế hoặc hết hạn. */
public enum HoldStatus {
    /** Khách đang chọn ghế; phiên còn cho thêm hoặc bỏ ghế nếu chưa hết hạn. */
    ACTIVE("Đang chọn ghế"),
    /** Đã tạo đơn chờ thanh toán; danh sách ghế được giữ nguyên để hoàn tất đơn. */
    CHECKOUT("Chờ thanh toán"),
    /** Phiên đã dùng để thanh toán thành công; vẫn giữ trạng thái này nếu khách hủy vé về sau. */
    CONSUMED("Đã sử dụng"),
    /** Các ghế đã được trả và phiên kết thúc trước khi thanh toán. */
    RELEASED("Đã trả ghế"),
    /** Đã hết thời gian giữ ghế; các ghế đang giữ của phiên được trả lại. */
    EXPIRED("Hết hạn");
    /** Tên trạng thái phiên giữ ghế hiển thị trên giao diện. */
    private final String label;

    HoldStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
