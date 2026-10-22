package com.td.CinemaMini.model.enums;

/** Loại ghế trong phòng, dùng xác định có cộng phụ thu VIP khi tính giá vé hay không. */
public enum SeatType {
    /** Ghế thường ở các hàng A-C, không cộng phụ thu VIP. */
    STANDARD("Thường"),
    /** Ghế VIP ở các hàng D-E, cộng thêm phụ thu VIP của suất chiếu. */
    VIP("VIP");
    /** Tên loại ghế hiển thị trên giao diện, như "Thường" hoặc "VIP". */
    private final String label;

    SeatType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
