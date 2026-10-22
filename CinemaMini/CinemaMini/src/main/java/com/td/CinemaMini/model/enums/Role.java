package com.td.CinemaMini.model.enums;

/** Quyền truy cập của tài khoản, dùng phân biệt khách mua vé và admin quản lý rạp. */
public enum Role {
    /** Khách hàng có hồ sơ Customer để giữ ghế, đặt vé và xem đơn của mình. */
    USER("Khách hàng"),
    /** Người quản lý suất chiếu, giao dịch và hoạt động đặt vé; không cần hồ sơ Customer. */
    ADMIN("Quản trị");
    /** Tên quyền hiển thị trên giao diện, như "Khách hàng" hoặc "Quản trị". */
    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
