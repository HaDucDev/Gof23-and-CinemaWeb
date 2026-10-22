package com.td.CinemaMini.model.enums;

/** Trạng thái bán vé của ghế trong từng suất chiếu, được lưu trong ShowtimeSeat. */
public enum SeatStatus {
    /** Ghế còn trống, chưa thuộc phiên giữ hoặc đơn nào và có thể được chọn. */
    AVAILABLE("Còn trống"),
    /** Ghế đang thuộc một phiên giữ; holdId cho biết phiên đó. */
    HELD("Đang giữ"),
    /** Ghế đã bán cho một đơn thanh toán thành công; bookingId cho biết đơn đó. */
    BOOKED("Đã bán"),
    /** Ghế không mở bán trong suất chiếu này. */
    DISABLED("Không mở bán");
    /** Tên trạng thái ghế hiển thị trên sơ đồ ghế. */
    private final String label;

    SeatStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
